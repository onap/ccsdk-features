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

import java.time.Duration;
import java.time.Instant;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.utils.RateFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link #RateFilter} Problems of to many notifications during mount of thousand of devices.
 *
 * <pre>
 *  Testcase (e: 17 Event received, rateMaxCount=3)
 *         eee                           e  e e e e e  e  e e e e    e         e                e
 *  ---//--|--------------|-----//-------|--------------|--------------|--------------|---//----|--------------|
 *         P1:1           P2:1           P1:2           P2:2           P3:2          P4:2       P1:3
 *         1000-1002      2000           3500 10 millis
 *Overload              no             no            yes            yes             no              no
 * </pre>
 *
 */
public class RateFilterTest {

    private static final Logger LOG = LoggerFactory.getLogger(RateFilterTest.class.getName());

    private static int MILLIS = 1000;
    private static long[] now = { 1000, 1001, 1002, //P1:1 0-2
            3500, 3550, 3560, 3570, 3580, 3590, 3800, //P1:2 3500 3-9
            4510, 4520, 4530, 4540, 4900, //P2:2 4500 10-14
            5700, //P3:2 5500 15
            7000, 8000};//P1:3 16-17
    private static int idx;

    @Test
    public void test() {
        RateFilter rateFilter = new RateFilter(Duration.ofMillis(MILLIS), 4, () -> getNow());
        LOG.info("Init done");

        for (int t=0; t < 20; t++) {
            LOG.info("{}", t);
            rateFilter.filterEvent();
            LOG.info("{}", rateFilter.getOverloadStatus());
        }

    }

    Instant getNow() {
        long res;
        if (idx < now.length) {
            res = now[idx];
        } else {
            int lastIdx = now.length - 1;
            res = now[lastIdx] + (idx - lastIdx) * MILLIS;
        }
        idx++;
        return Instant.ofEpochMilli(res);
    }

}
