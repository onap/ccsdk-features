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
package org.onap.ccsdk.features.sdnr.wt.websocketmanager2.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.time.Duration;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.utils.RateFilterManager;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.utils.RateFilterManager.RateFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link #RateFilter} Problems of to many notifications during mount of thousand of devices.
 *
 * <pre>
 *  Testcase (e: 17 Event received, rateMaxCount=3)
 *         1 3                           4  5 6 7 8 9 10 11    14   15        16               17     18
 *     t            t           t          t              t              t              t         t              t
 *           eee                           e  e e e e e  e  e e e e    e         e                e      e
 *    ---//--|--------------|-----//-------|--------------|--------------|--------------|---//----|--------------|
 *           P1:1           P2:1           P1:2           P2:2           P3:2          P4:2       P1:3
 * ms  500   1000-1002      2000           3500           4500           5500           6500      7500           8500
 *Overload              no             no            yes                      yes     no              no
 * </pre>
 *
 */
public class RateFilterTest {

    private static final Logger LOG = LoggerFactory.getLogger(RateFilterTest.class.getName());

    private static int INTEGRATIONTIMEMILLIS = 1000;
    private static long EVENTS_PER_INTERVALL = 4;
    private static long RATE_PER_MINUTE = EVENTS_PER_INTERVALL * 60;
    /* Negative event time indicates timer event */
    private static long[] now = {-500, 1000, 1010, 1020, //P1:1 1-3
            -1500, -2500, -3500, 3500, 3550, 3560, 3570, 3580, 3590, 3800, //P1:2 3500 4-10
            -4500, 4510, 4520, 4530, 4540, 4900, //P2:2 4500 11-15
            -5500, 5700, //P3:2 5500 16
            -6500, -7500, 7500, 8000};//P1:3 17-18
    private static boolean[] overload = {false, false, false, false, //P1:1 1-3
            false, false, false, false, false, false, false, false, false, false, //P1:2 3500 4-10
            true, true, true, true, true, true, //P2:2 4500 11-15
            true, true, //P3:2 5500 16
            false, false, false, false};//P1:3 17-18

    private static int idx;
    private static long millis;

    @Test
    public void testStates() {
        reset();
        RateFilterManager rateFilterManager =
                new RateFilterManager(Duration.ofMillis(INTEGRATIONTIMEMILLIS), false, () -> getNow());
        RateFilter rateFilter = rateFilterManager.getRateFilter(RATE_PER_MINUTE);
        LOG.info("Init done");
        assertEquals("Events per integration period", EVENTS_PER_INTERVALL, rateFilter.getMaxEventsPerIntegration());

        for (int t = 1; t < 30; t++) {
            boolean expected = tick();
            if (millis < 0) {
                LOG.info("{} - timer {}", t, millis);
                rateFilter.timer();
            } else {
                LOG.info("{} - event {}", t, millis);
                rateFilter.event();
            }
            LOG.info("Overload={} {}", rateFilter.getOverloadStatus(), expected);
            assertEquals("Filter activity", expected, rateFilter.getOverloadStatus());
        }
        rateFilter.close();
    }

    @Test
    public void testThread() throws InterruptedException {
        LOG.info("testThread");
        reset();
        RateFilterManager rateFilterManager = new RateFilterManager(Duration.ofMillis(INTEGRATIONTIMEMILLIS));
        RateFilter rateFilter = rateFilterManager.getRateFilter(RATE_PER_MINUTE);

        tick();
        Thread.sleep(2000);

        Object objectYouNeedToLockOn = new Object();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            long localMillis;

            @Override
            public void run() {
                long xLocalMillis = localMillis += 10;
                long xMillis = Math.abs(millis);
                if (xLocalMillis >= xMillis) {
                    LOG.info("aTime:{} Millis:{} Idx={}", xLocalMillis, xMillis, idx);
                    boolean expected = tick();
                    if (millis > 0) {
                        //Skip negatives .. handled by timer
                        rateFilter.event();
                        boolean actual = rateFilter.getOverloadStatus();
                        LOG.info("bTime:{} Millis:{} Idx={} Overload={} Expected={} {}", xLocalMillis, xMillis, idx,
                                actual, expected, actual == expected ? "" : "XXXX");
                        if (idx >= 30) {
                            LOG.info("Test is ending");
                            synchronized (objectYouNeedToLockOn) {
                                objectYouNeedToLockOn.notify();
                            }
                            timer.cancel();
                        }
                        assertEquals("Filter activity", expected, rateFilter.getOverloadStatus());
                    }
                }
            }
        }, 0, 10);
        synchronized (objectYouNeedToLockOn) {
            objectYouNeedToLockOn.wait();
        }
        //rateFilter.close();
        LOG.info("Test end");
    }

    @Test
    public void testMultipleClients() {
        RateFilterManager rateFilterManager = new RateFilterManager(Duration.ofMillis(INTEGRATIONTIMEMILLIS));
        RateFilter rateFilter1 = rateFilterManager.getRateFilter(RATE_PER_MINUTE);
        assertEquals("Multiple clients", 1, rateFilter1.getClients());
        RateFilter rateFilter2 = rateFilterManager.getRateFilter(RATE_PER_MINUTE);
        assertEquals("Multiple clients", 2, rateFilter1.getClients());
        RateFilter rateFilter3 = rateFilterManager.getRateFilter(RATE_PER_MINUTE);
        assertEquals("Multiple clients", 3, rateFilter1.getClients());

        assertEquals("Similar instances", rateFilter1, rateFilter3);

        RateFilter rateFilterOther = rateFilterManager.getRateFilter(2*RATE_PER_MINUTE);
        assertNotEquals("Different instances", rateFilter1, rateFilterOther);
        rateFilterOther.close();

        rateFilter3.close();
        assertEquals("Multiple clients", 2, rateFilter1.getClients());
        rateFilter2.close();
        assertEquals("Multiple clients", 1, rateFilter1.getClients());
        rateFilter1.close();
        assertEquals("Multiple clients", 0, rateFilter1.getClients());

        rateFilterManager.close();
    }

    private Instant getNow() {
        LOG.debug("Now:{}", millis);
        return Instant.ofEpochMilli(Math.abs(millis));
    }

    private void reset() {
        idx = 0;
    }

    private boolean tick() {
        if (idx < now.length) {
            millis = now[idx];
        } else {
            int lastIdx = now.length - 1;
            millis = now[lastIdx] + (idx - lastIdx) * INTEGRATIONTIMEMILLIS;
        }
        boolean expected = idx < overload.length ? overload[idx] : false;
        idx++;
        return expected;
    }

}
