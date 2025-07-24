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
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Threadpool for running n instances per key T
 *
 * @param <T> type of key for the pools
 * @param <S> type of arg to create a runner
 * @author jack
 */
public class KeyBasedThreadpool<T, S> implements GenericRunnableFactoryCallback<T> {

    private static final Logger LOG = LoggerFactory.getLogger(KeyBasedThreadpool.class);
    private final ConcurrentLinkedQueue<Entry<T, S>> queue;
    private final List<T> runningKeys;
    private final int keyPoolSize;
    private final GenericRunnableFactory<T, S> factory;
    private final ExecutorService executor;

    /**
     * @param poolSize    overall maximum amount of threads
     * @param keyPoolSize amount of threads per key
     * @param factory     factory for creating runnables to start
     */
    public KeyBasedThreadpool(int poolSize, int keyPoolSize, GenericRunnableFactory<T, S> factory) {
        this.queue = new ConcurrentLinkedQueue<>();
        this.keyPoolSize = keyPoolSize;
        this.factory = factory;
        this.executor = Executors.newFixedThreadPool(poolSize);
        this.runningKeys = Collections.synchronizedList(new ArrayList<>());
        LOG.info("starting key-based threadpool with keysize={} and size={}", keyPoolSize, poolSize);
    }

    public synchronized void execute(T key, S arg) {
        if (this.isKeyPoolSizeReached(key)) {
            LOG.debug("pool size for key {} reached. add to queue", key);
            queue.add(new SimpleEntry<>(key, arg));
        } else {
            LOG.debug("starting executor for key {}.", key);
            this.runningKeys.add(key);
            this.executor.execute(new RunnableWrapper<>(this.factory.create(key, arg), key, this));
        }
    }

    private void executeNext() {
        Entry<T, S> entry = this.queue.peek();
        if (entry != null) {
            LOG.debug("executing next for key {} with arg {}", entry.getKey(), entry.getValue());
            if (!this.isKeyPoolSizeReached(entry.getKey())) {
                this.queue.poll();
                this.runningKeys.add(entry.getKey());
                this.executor.execute(new RunnableWrapper<>(this.factory.create(entry.getKey(), entry.getValue()),
                        entry.getKey(), this));
            } else {
                LOG.debug("key pool size reached. waiting for someone else to stop");
            }
        } else {
            LOG.debug("nothing to execute. queue is empty.");
        }
    }

    private boolean isKeyPoolSizeReached(T key) {
        LOG.trace("running keys size={}", this.runningKeys.size());
        if (this.keyPoolSize == 1) {
            return this.runningKeys.contains(key);
        }
        return this.runningKeys.stream().filter(e -> e == key).count() >= this.keyPoolSize;
    }

    @Override
    public synchronized void onFinish(T key) {
        LOG.debug("executor finished for key {}.", key);
        this.runningKeys.remove(key);
        this.executeNext();
    }

    public synchronized void join() {
        LOG.debug("wait for all executors to finish");
        while (!this.isEmpty()) {
            try {
                wait(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public synchronized void join(final int millis) {
        int waitBeforeKill = millis;
        while (!this.runningKeys.isEmpty() && !this.queue.isEmpty()) {
            try {
                wait(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            waitBeforeKill -= 1000;
            if (waitBeforeKill <= 0) {
                LOG.debug("join after {} milliseconds still not finished. killing", millis);
                this.queue.clear();
                this.runningKeys.clear();

            }
        }
    }

    public boolean isEmpty() {
        return this.queue.isEmpty() && this.runningKeys.isEmpty();
    }

    private static class RunnableWrapper<T> implements Runnable {

        private final Runnable inner;
        private final GenericRunnableFactoryCallback<T> callback;
        private final T key;

        public RunnableWrapper(Runnable inner, T key, GenericRunnableFactoryCallback<T> cb) {
            this.inner = inner;
            this.callback = cb;
            this.key = key;
        }

        @Override
        public void run() {
            this.inner.run();
            this.callback.onFinish(this.key);
        }
    }
}
