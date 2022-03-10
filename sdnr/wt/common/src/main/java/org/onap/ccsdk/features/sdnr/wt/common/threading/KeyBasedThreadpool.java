/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 *
 */
package org.onap.ccsdk.features.sdnr.wt.common.threading;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Threadpool for running n instances per key T
 *
 * @author jack
 *
 * @param <T>
 * @param <S>
 */
public class KeyBasedThreadpool<T, S> implements GenericRunnableFactoryCallback<T> {

    private final Queue<Entry<T, S>> queue;
    private final List<T> runningKeys;
    private final int keyPoolSize;
    private final GenericRunnableFactory<T, S> factory;
    private final ExecutorService executor;

    /**
     *
     * @param poolSize overall maximum amount of threads
     * @param keyPoolSize amount of threads per key
     * @param runner runnable to start
     */
    public KeyBasedThreadpool(int poolSize, int keyPoolSize, GenericRunnableFactory<T, S> factory) {
        this.queue = new ConcurrentLinkedQueue<>();
        this.keyPoolSize = keyPoolSize;
        this.factory = factory;
        this.executor = Executors.newFixedThreadPool(poolSize);
        this.runningKeys = new ArrayList<>();
    }

    public void execute(T key, S arg) {
        if (this.isKeyPoolSizeReached(key)) {
            queue.add(new SimpleEntry<>(key, arg));
        } else {
            this.runningKeys.add(key);
            this.executor.execute(this.factory.create(arg, this));
        }

    }

    private void executeNext() {
        Entry<T, S> entry = this.queue.peek();
        if (!this.isKeyPoolSizeReached(entry.getKey())) {
            this.queue.poll();
            this.runningKeys.add(entry.getKey());
            this.executor.execute(this.factory.create(entry.getValue(), this));
        }
    }

    private boolean isKeyPoolSizeReached(T key) {
        return this.runningKeys.stream().filter(e -> e == key).count() >= this.keyPoolSize;
    }

    @Override
    public void onFinish(T key) {
        this.runningKeys.remove(key);
        this.executeNext();
    }


}
