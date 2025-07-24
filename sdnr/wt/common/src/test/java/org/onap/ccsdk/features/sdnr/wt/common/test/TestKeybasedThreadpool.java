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
package org.onap.ccsdk.features.sdnr.wt.common.test;

import static org.junit.Assert.assertTrue;

import java.util.Random;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.threading.GenericRunnableFactory;
import org.onap.ccsdk.features.sdnr.wt.common.threading.KeyBasedThreadpool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestKeybasedThreadpool {

    private static final Logger LOG  = LoggerFactory.getLogger(TestKeybasedThreadpool.class);
    private static final String KEY_A = "a";
    private static final String KEY_B = "b";
    private static final String KEY_C = "c";
    private static final String KEY_D = "d";

    @Test
    public void test1() {
        GenericRunnableFactory<String, TestClass> factory1 =
                new GenericRunnableFactory<>() {
                    @Override
                    public Runnable create(final String key, final TestClass arg) {
                        return () -> {
                            final String key2 = arg.value;
                            final long sleep = arg.sleep;
                            LOG.info("{}: sleeping now for {} seconds", key2, sleep);
                            try {
                                Thread.sleep(sleep * 1000);
                            } catch (InterruptedException e) {
                                LOG.error("InterruptedException", e);
                                Thread.currentThread().interrupt();
                            }
                            LOG.info("{}: finished", key2);
                        };
                    }
                };
        LOG.info("starting");
        KeyBasedThreadpool<String, TestClass> threadpool = new KeyBasedThreadpool<String, TestClass>(10, 1, factory1);
        threadpool.execute(KEY_A, new TestClass(KEY_A));
        threadpool.execute(KEY_A, new TestClass(KEY_A));
        threadpool.execute(KEY_A, new TestClass(KEY_A));
        threadpool.execute(KEY_B, new TestClass(KEY_B));
        threadpool.execute(KEY_C, new TestClass(KEY_C));
        threadpool.execute(KEY_D, new TestClass(KEY_D));
        threadpool.execute(KEY_D, new TestClass(KEY_D));
        threadpool.join();
        assertTrue(threadpool.isEmpty());
        LOG.info("done");
    }

    private static int counter=0;


    public class TestClass {
        protected final long sleep;
        private final String value;

        public TestClass(String value) {

            this.value = value+ String.valueOf(counter++);
            Random rnd = new Random();
            this.sleep = rnd.nextInt(3);
            LOG.info("instatiate {}",this);
        }

        @Override
        public String toString() {
            return "TestClass [sleep=" + sleep + ", value=" + value + "]";
        }
    }
}
