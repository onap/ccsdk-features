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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.test;

import static org.junit.Assert.fail;

import java.io.IOException;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.YangToolsMapper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._15m.list.output.Data;

public class TestDataMappings {

    // @formatter:off
    private static final String PMDATA15M_SERVERDB_JSON = "{\n"
            + "\"node-name\": \"sim2\",\n"
            + "\"uuid-interface\": \"LP-MWPS-TTP-01\",\n"
            + "\"layer-protocol-name\": \"MWPS\",\n"
            + "\"radio-signal-id\": \"Test11\",\n"
            + "\"time-stamp\": \"2017-07-04T14:00:00.0Z\",\n"
            + "\"granularity-period\": \"Period15Min\",\n"
            + "\"scanner-id\": \"PM_RADIO_15M_9\",\n"
            + "\"performance-data\": {\n"
            + "\"es\": 0,\n"
            + "\"rx-level-avg\": -41,\n"
            + "\"time2-states\": -1,\n"
            + "\"time4-states-s\": 0,\n"
            + "\"time4-states\": 0,\n"
            + "\"time8-states\": 0,\n"
            + "\"time16-states-s\": -1,\n"
            + "\"time16-states\": 0,\n"
            + "\"time32-states\": 0,\n"
            + "\"time64-states\": 0,\n"
            + "\"time128-states\": 0,\n"
            + "\"time256-states\": 900,\n"
            + "\"time512-states\": -1,\n"
            + "\"time512-states-l\": -1,\n"
            + "\"time1024-states\": -1,\n"
            + "\"time1024-states-l\": -1,\n"
            + "\"time2048-states\": -1,\n"
            + "\"time2048-states-l\": -1,\n"
            + "\"time4096-states\": -1,\n"
            + "\"time4096-states-l\": -1,\n"
            + "\"time8192-states\": -1,\n"
            + "\"time8192-states-l\": -1,\n"
            + "\"snir-min\": -99,\n"
            + "\"snir-max\": -99,\n"
            + "\"snir-avg\": -99,\n"
            + "\"xpd-min\": -99,\n"
            + "\"xpd-max\": -99,\n"
            + "\"xpd-avg\": -99,\n"
            + "\"rf-temp-min\": -99,\n"
            + "\"rf-temp-max\": -99,\n"
            + "\"rf-temp-avg\": -99,\n"
            + "\"defect-blocks-sum\": -1,\n"
            + "\"time-period\": 900,\n"
            + "\"tx-level-min\": 25,\n"
            + "\"tx-level-max\": 25,\n"
            + "\"tx-level-avg\": 25,\n"
            + "\"rx-level-min\": -41,\n"
            + "\"rx-level-max\": -41,\n"
            + "\"unavailability\": 0,\n"
            + "\"ses\": 0,\n"
            + "\"cses\": 0\n"
            + "},\n"
            + "\"suspect-interval-flag\": false\n"
            + "}";
 // @formatter:on
    @Test
    public void testPmData15m() throws ClassNotFoundException {

        YangToolsMapper mapper = new YangToolsMapper();
        try {
            Data data = mapper.readValue(PMDATA15M_SERVERDB_JSON.getBytes(), Data.class);
            System.out.println(data);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Can not parse data");
        }
    }

}
