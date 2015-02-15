package io.advantageous.qbit.http.jetty.impl;

import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.websocket.WebSocket;
import io.advantageous.qbit.http.websocket.WebSocketMessage;
import io.advantageous.qbit.util.MultiMap;
import org.boon.Str;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.Fields;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static org.boon.Boon.puts;
import static org.boon.Boon.sputs;

/**
 * Created by rhightower on 2/14/15.
 */
public class JettyQBitHttpClient implements HttpClient {

    private final Logger logger = LoggerFactory.getLogger(JettyQBitHttpClient.class);
    private final boolean debug = true || GlobalConstants.DEBUG || logger.isDebugEnabled();
    private final org.eclipse.jetty.client.HttpClient httpClient = new
            org.eclipse.jetty.client.HttpClient();
    private final WebSocketClient webSocketClient = new WebSocketClient();
    private final String host;
    private final int port;


    public JettyQBitHttpClient(final String host, final int port) {
        this.host = host;
        this.port = port;
    }


    @Override
    public void sendHttpRequest(HttpRequest request) {
        final Request jettyRequest = createJettyRequest(request);
        jettyRequest.send(createJettyListener(request));
    }

    private Request createJettyRequest(HttpRequest request) {
        final String uri = createURIString(request);
        final HttpMethod jettyMethod = getHttpMethod(request);
        final Request jettyRequest = httpClient.newRequest(uri)
                .method(jettyMethod);

        if (jettyMethod==HttpMethod.POST || jettyMethod==HttpMethod.PUT) {
            jettyRequest.content(new BytesContentProvider(request.getContentType(), request.getBody()));
        }
        copyParams(request, jettyRequest);
        copyHeaders(request, jettyRequest);
        return jettyRequest;
    }

    private String createURIString(HttpRequest request) {
        return Str.add("http://", host, ":", Integer.toString(port), request.getUri());
    }

    private void copyParams(HttpRequest request, Request jettyRequest) {
        final MultiMap<String, String> params = request.getParams();
        final Iterator<Map.Entry<String, Collection<String>>> iterator = params.iterator();
        final Fields paramFields = jettyRequest.getParams();

        while (iterator.hasNext()) {
            final Map.Entry<String, Collection<String>> entry = iterator.next();
            final String paramName = entry.getKey();
            final Collection<String> values = entry.getValue();

            for (String value : values) {
                paramFields.add(paramName, value);

                if (debug) puts("Adding Params", paramName, value);
            }
        }
    }


    private void copyHeaders(HttpRequest request, Request jettyRequest) {
        final MultiMap<String, String> headers = request.getHeaders();
        final Iterator<Map.Entry<String, Collection<String>>> iterator = headers.iterator();
        final HttpFields headerFields = jettyRequest.getHeaders();
        while (iterator.hasNext()) {
            final Map.Entry<String, Collection<String>> entry = iterator.next();
            final String paramName = entry.getKey();
            final Collection<String> values = entry.getValue();
            for (String value : values) {
                headerFields.add(paramName, value);
                if (debug) puts("Adding Header", paramName, value);
            }
        }
    }
    private BufferingResponseListener createJettyListener(final HttpRequest request) {
        return new BufferingResponseListener(1_000_000) {

            @Override
            public void onComplete(Result result) {

                if (!result.isFailed()) {
                    byte[] responseContent = getContent();

                    if (request.getResponse().isText()) {
                        String responseString = new String(responseContent, StandardCharsets.UTF_8);

                        request.getResponse().response(result.getResponse().getStatus(),
                                result.getResponse().getHeaders().get(HttpHeader.CONTENT_TYPE),
                                responseString);
                    } else {
                        request.getResponse().response(result.getResponse().getStatus(),
                                result.getResponse().getHeaders().get(HttpHeader.CONTENT_TYPE),
                                responseContent);

                    }
                }

            }
        };
    }

    private HttpMethod getHttpMethod(HttpRequest request) {
        final String method = request.getMethod();
        return HttpMethod.fromString(method.toUpperCase());
    }

    private Map<String, WebSocket> webSocketMap = new ConcurrentHashMap<>();

    @Override
    public void sendWebSocketMessage(final WebSocketMessage webSocketMessage) {

        /** This is a bad design... the goal is to refactor and get rid of WebSocketMessage.*/
        WebSocket existingWebSocket = webSocketMap.remove(webSocketMessage.getUri());

        if (existingWebSocket!=null && existingWebSocket.isOpen()) {
            try {
                existingWebSocket.setTextMessageConsumer(s -> {
                    webSocketMessage.getSender().sendText(s);
                });
                existingWebSocket.setBinaryMessageConsumer(s -> {
                    webSocketMessage.getSender().sendBytes(s);
                });
                existingWebSocket.sendText(webSocketMessage.body().toString());
                webSocketMap.put(webSocketMessage.getUri(), existingWebSocket);
            }catch (Exception ex) {
                logger.debug(
                        sputs(
                                "Problem while sending WebSocket message",
                                host, port, webSocketMessage.getUri()), ex);
                existingWebSocket = null;
            }
        }
        openWebSocketAndSendMessage(webSocketMessage, existingWebSocket);
    }

    private void openWebSocketAndSendMessage(final WebSocketMessage webSocketMessage,
                                             final WebSocket existingWebSocket) {
        if (existingWebSocket==null) {

            final ClientUpgradeRequest request = new ClientUpgradeRequest();
            final String uri = Str.add("ws://", host, ":", Integer.toString(port), webSocketMessage.getUri());

            JettyNativeClientWebSocketHandler webSocketHandler =
                    new JettyNativeClientWebSocketHandler(webSocketMessage.getUri(),
                            host, port, webSocket ->
                    {
                        webSocket.setTextMessageConsumer(s -> {
                            webSocketMessage.getSender().sendText(s);
                        });
                        webSocket.setBinaryMessageConsumer(s -> {
                            webSocketMessage.getSender().sendBytes(s);
                        });
                        webSocket.sendText(webSocketMessage.getMessage().toString());
                        webSocketMap.put(webSocketMessage.getUri(), webSocket);
                    });

            try {
                webSocketClient.connect(webSocketHandler, new URI(uri), request);
            } catch (Exception e) {

                logger.error("problem connecting WebSocket " + webSocketMessage.address(), e);
            }
        }
    }

    @Override
    public void periodicFlushCallback(Consumer<Void> periodicFlushCallback) {

    }

    @Override
    public HttpClient start() {
        try {
            httpClient.start();
        } catch (Exception e) {

            throw new IllegalStateException("Unable to start httpClient Jetty support", e);
        }


        try {
            webSocketClient.start();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to start websocket Jetty support", e);
        }

        return this;
    }

    @Override
    public void flush() {

    }

    @Override
    public void stop() {

        try {
            httpClient.stop();
            webSocketClient.stop();
        } catch (Exception e) {

            logger.warn("problem stopping", e);
        }

    }
}
