package io.advantageous.boon.primitive;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;


/*
 * Copyright 2013-2014 Richard M. Hightower
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * __________                              _____          __   .__
 * \______   \ ____   ____   ____   /\    /     \ _____  |  | _|__| ____    ____
 *  |    |  _//  _ \ /  _ \ /    \  \/   /  \ /  \\__  \ |  |/ /  |/    \  / ___\
 *  |    |   (  <_> |  <_> )   |  \ /\  /    Y    \/ __ \|    <|  |   |  \/ /_/  >
 *  |______  /\____/ \____/|___|  / \/  \____|__  (____  /__|_ \__|___|  /\___  /
 *         \/                   \/              \/     \/     \/       \//_____/
 *      ____.                     ___________   _____    ______________.___.
 *     |    |____ ___  _______    \_   _____/  /  _  \  /   _____/\__  |   |
 *     |    \__  \\  \/ /\__  \    |    __)_  /  /_\  \ \_____  \  /   |   |
 * /\__|    |/ __ \\   /  / __ \_  |        \/    |    \/        \ \____   |
 * \________(____  /\_/  (____  / /_______  /\____|__  /_______  / / ______|
 *               \/           \/          \/         \/        \/  \/
 */

/** This supports both LRU and FIFO.
 *  Single threaded access.
 */
public class SimpleLRUCache<K, V>  {

    Map<K, V> map = new LinkedHashMap();


    private static class InternalCacheLinkedList<K, V> extends LinkedHashMap<K, V> {
        final int limit;

        InternalCacheLinkedList( final int limit, final boolean lru ) {
            super( 16, 0.75f, lru );
            this.limit = limit;
        }

        protected final boolean removeEldestEntry( final Map.Entry<K, V> eldest ) {
            return super.size() > limit;
        }
    }


    public SimpleLRUCache( final int limit ) {

            map = new InternalCacheLinkedList<>( limit, true );

    }


    public void put( K key, V value ) {
        map.put( key, value );
    }

    public V get( K key ) {
        return map.get( key );
    }

    public V getSilent( K key ) {
        V value = map.get( key );
        if ( value != null ) {
            map.remove( key );
            map.put( key, value );
        }
        return value;
    }

    public void remove( K key ) {
        map.remove( key );
    }

    public int size() {
        return map.size();
    }

    public String toString() {
        return map.toString();
    }

    public Collection<V> values() {
        return new ArrayList<>(this.map.values());
    }

    public Collection<K> keys() {
        return new ArrayList<>(this.map.keySet());
    }

}
