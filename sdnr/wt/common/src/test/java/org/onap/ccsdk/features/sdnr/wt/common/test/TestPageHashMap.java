/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
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

import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.AggregationEntries;

public class TestPageHashMap {

    @Test
    public void testGenericTypes() {
        AggregationEntries agg = new AggregationEntries();

        for (int t = 0; t < 100; t++) {
            agg.put("Key" + t, Long.valueOf(t));
        }

        int x = 0;
        for (String k : agg.keySet()) {
            if (x++ >= 10)
                break;
            System.out.println("Keys: " + k);
        }

        String[] res;

        res = agg.getKeysAsPagedStringList(5, 10);
        System.out.println("Entries: " + res.length);
        for (int t = 0; t < res.length; t++) {
            System.out.println("Entry " + t + ": " + res[t]);
        }

        res = agg.getKeysAsPagedStringList(5, 10);
        System.out.println("Entries: " + res.length);
        for (int t = 0; t < res.length; t++) {
            System.out.println("Entry " + t + ": " + res[t]);
        }


    }


}
