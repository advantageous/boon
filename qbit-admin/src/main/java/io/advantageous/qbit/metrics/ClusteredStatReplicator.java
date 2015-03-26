package io.advantageous.qbit.metrics;

import io.advantageous.boon.Pair;
import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.annotation.QueueCallback;
import io.advantageous.qbit.annotation.QueueCallbackType;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.service.discovery.*;
import io.advantageous.qbit.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.advantageous.boon.Boon.sputs;

/**
 * Clustered Stat Replicator
 * Created by rhightower on 3/24/15.
 */
public class ClusteredStatReplicator implements StatReplicator, ServiceChangedEventChannel {


    private final ServiceDiscovery serviceDiscovery;
    private final StatReplicatorProvider statReplicatorProvider;
    private final ConcurrentHashMap<String, Pair<ServiceDefinition,StatReplicator>>
            replicatorsMap = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(ClusteredStatReplicator.class);
    private final boolean debug = GlobalConstants.DEBUG || logger.isDebugEnabled();
    private final boolean trace = logger.isTraceEnabled();
    private final String serviceName;
    private final ServicePool servicePool;
    private final String localServiceId;
    private final Timer timer;
    private final int tallyInterval;
    private final int flushInterval;
    private long currentTime;
    private long lastReconnectTime;
    private long lastSendTime;
    private long lastReplicatorFlush = 0;
    private ConcurrentHashMap<String, LocalCount> countMap = new ConcurrentHashMap<>();
    private List<Pair<ServiceDefinition,StatReplicator>> statReplicators = new ArrayList<>();


    final static class LocalCount {

        int count;
        String name;

    }


    public ClusteredStatReplicator(final String serviceName,
                                   final ServiceDiscovery serviceDiscovery,
                                   final StatReplicatorProvider statReplicatorProvider,
                                   final String localServiceId,
                                   final Timer timer,
                                   final int tallyInterval,
                                   final int flushInterval) {
        this.serviceDiscovery = serviceDiscovery;
        this.statReplicatorProvider = statReplicatorProvider;
        this.serviceName = serviceName;
        this.localServiceId=localServiceId;
        this.servicePool = new ServicePool(serviceName, null);
        this.timer = timer;
        this.tallyInterval = tallyInterval;
        this.flushInterval = flushInterval;

    }

    @Override
    public void replicateCount(final String name, final int count, final long now) {

        if (trace) logger.trace(sputs("ClusteredStatReplicator::replicateCount()",
                serviceName, name, count, now));

        if (debug) {
            if (statReplicators.size() == 0) {
                logger.debug(sputs("ClusteredStatReplicator::replicateCount", name, count, now ));
            }
        }

        LocalCount localCount = countMap.get(name);

        if (localCount==null) {
            localCount = new LocalCount();
            localCount.name = name;
            countMap.put(name, localCount);
        }
        localCount.count += count;


    }

    private void doRecordCount(Pair<ServiceDefinition,StatReplicator> statReplicator,
                               final String name, final int count, final long now) {

        try {
            statReplicator.getSecond().replicateCount(name, count, now);
        } catch (Exception ex) {
            if (debug) logger.debug(sputs("ClusteredStatReplicator::Replicator failed"), ex);
            if (debug) logger.debug(sputs("ClusteredStatReplicator::Replicator failed", statReplicator ));

        }
    }


    @QueueCallback({QueueCallbackType.IDLE,
            QueueCallbackType.EMPTY,
            QueueCallbackType.LIMIT})
    void process() {



        currentTime = timer.now();

        sendIfNeeded();
        checkForReconnect();
    }

    private void sendIfNeeded() {

        long duration = currentTime - lastSendTime;

        if (duration > tallyInterval) {
            this.lastSendTime = currentTime;

            final Collection<LocalCount> countCollection = this.countMap.values();


            for (LocalCount localCount : countCollection) {

                if (localCount.count > 0) {
                    statReplicators.forEach(
                            statReplicator -> doRecordCount(statReplicator, localCount.name, localCount.count, currentTime)
                    );
                }
                localCount.count = 0;
            }
            if (countMap.size()>10_000_000) {
                countMap.clear();
            }
            flushReplicatorsAll();
        }




    }


    private void flushReplicatorsAll() {

        if (currentTime - lastReplicatorFlush > flushInterval) {
            lastReplicatorFlush = currentTime;

            final List<Pair<ServiceDefinition,StatReplicator>> badReplicators = new ArrayList<>();
            statReplicators.forEach(
                    statReplicator -> flushReplicator(statReplicator, badReplicators)
            );
            badReplicators.forEach(statReplicator -> {

                try {
                    statReplicator.getSecond().stop();
                } catch (Exception ex) {
                    if (debug) logger.debug("Failed to stop failed node", ex);
                }
                statReplicators.remove(statReplicator);
                replicatorsMap.remove(statReplicator.getFirst().getId());
            }
            );

            if (debug) {

                logger.debug(sputs("ClusteredStatReplicator::flushReplicatorsAll()",
                        badReplicators.size()));
                badReplicators.forEach(statReplicator -> logger.debug(sputs(statReplicator)));
            }
        }
    }


    private void checkForReconnect() {
        long duration = currentTime - lastReconnectTime;
        if (duration > 10_000) {
            doCheckReconnect();
        }

    }

    public void doCheckReconnect() {

        lastReconnectTime = currentTime;
        final List<ServiceDefinition> services = servicePool.services();


        //services.forEach(serviceDefinition -> addIfNotExists(serviceDefinition));

        if ((services.size()-1) != this.statReplicators.size()) {
            if (debug) logger.debug(sputs("DOING RECONNECT", services.size() - 1,
                    this.statReplicators.size()));

            shutDownReplicators();
            services.forEach(this::addService);
        }
    }

    private void addIfNotExists(ServiceDefinition serviceDefinition) {
        Pair<ServiceDefinition, StatReplicator> pair = this.replicatorsMap.get(serviceDefinition.getId());
        if (pair==null) {
            addService(serviceDefinition);
        } else {
            try {
                if (!pair.getSecond().connected()) {
                    addService(serviceDefinition);
                }
            } catch (Exception ex) {
                if (debug)logger.debug("Unable to add service or check to see if it is connected", ex);
            }
        }
    }

    private void shutDownReplicators() {
        if (debug) logger.debug("Shutting down replicators");
        for (Pair<ServiceDefinition,StatReplicator> statReplicator : statReplicators) {
            try {
                statReplicator.getSecond().stop();

            } catch (Exception ex) {
                logger.debug("Shutdown replicator failed", ex);
            }

            if (debug) logger.debug("Shutting down replicator");
        }
        statReplicators.clear();
        replicatorsMap.clear();
    }


    private void flushReplicator(final Pair<ServiceDefinition,StatReplicator> statReplicator,
                                 final List<Pair<ServiceDefinition,StatReplicator>> badReplicators) {


        try {
            ServiceProxyUtils.flushServiceProxy(statReplicator.getSecond());
        } catch (Exception exception) {
            badReplicators.add(statReplicator);
            logger.info("Replicator failed" + statReplicator, exception);
        }
    }


    /**
     * Event handler
     *
     * @param serviceName service name
     */
    @Override
    public void servicePoolChanged(final String serviceName) {

        if (trace) logger.trace(sputs("ClusteredStatReplicator::servicePoolChanged()", serviceName));
        if (this.serviceName.equals(serviceName)) {
            updateServicePool(serviceName);

        } else {
            if (debug) logger.debug(sputs("ClusteredStatReplicator::servicePoolChanged()",
                    "got event for another service", serviceName));
        }


    }

    private void updateServicePool(String serviceName) {

        final List<ServiceDefinition> nodes = serviceDiscovery.loadServices(serviceName);
        servicePool.setHealthyNodes(nodes, new ServicePoolListener() {
            @Override
            public void servicePoolChanged(String serviceName) {
            }

            @Override
            public void serviceAdded(String serviceName, ServiceDefinition serviceDefinition) {
                addService(serviceDefinition);
            }

            @Override
            public void serviceRemoved(String serviceName, ServiceDefinition serviceDefinition) {
                removeService(serviceDefinition);
            }

        });
    }

    private void removeService(final ServiceDefinition serviceDefinition) {

        if (trace) logger.trace(sputs("ClusteredStatReplicator::removeService()",
                serviceName, serviceDefinition));

        this.replicatorsMap.remove(serviceDefinition.getId());
        this.statReplicators = new ArrayList<>(replicatorsMap.values());
    }

    private void addService(final ServiceDefinition serviceDefinition) {

        if (trace) logger.trace(sputs("ClusteredStatReplicator::addService()", serviceDefinition));

        if (serviceDefinition.getId().equals(localServiceId)) {
            return;
        }

        final StatReplicator statReplicator = statReplicatorProvider.provide(serviceDefinition);
        this.replicatorsMap.put(serviceDefinition.getId(), Pair.pair(serviceDefinition,statReplicator));
        this.statReplicators = new ArrayList<>(replicatorsMap.values());

    }

    @Override
    public void flush() {
        process();
    }
}
