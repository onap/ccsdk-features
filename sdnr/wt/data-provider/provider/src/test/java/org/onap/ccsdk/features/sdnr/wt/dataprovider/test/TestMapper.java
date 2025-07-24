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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONObject;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.YangToolsMapper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementConnection;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementConnectionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementDeviceType;

public class TestMapper {

    private static final YangToolsMapper MAPPER = new YangToolsMapper();

    @Test
    public void testYangGenEnumMapperDeser() {
        NetworkElementConnection con = null;
        try {
            con = MAPPER.readValue("{\"device-type\":\"O-RAN\"}", NetworkElementConnection.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertEquals(NetworkElementDeviceType.ORAN, con.getDeviceType());
        try {
            con = MAPPER.readValue("{\"device-type\":\"ORAN\"}", NetworkElementConnection.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertEquals(NetworkElementDeviceType.ORAN, con.getDeviceType());
        try {
            con = MAPPER.readValue("{\"device-type\":\"O-ROADM\"}", NetworkElementConnection.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertEquals(NetworkElementDeviceType.OROADM, con.getDeviceType());
        try {
            con = MAPPER.readValue("{\"device-type\":\"O-ROADM\"}", NetworkElementConnection.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertEquals(NetworkElementDeviceType.OROADM, con.getDeviceType());
    }

    @Test
    public void testYangGenEnumMapperSer() {
        NetworkElementConnection con =
                new NetworkElementConnectionBuilder().setDeviceType(NetworkElementDeviceType.ORAN).build();
        String str = null;
        try {
            str = MAPPER.writeValueAsString(con);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertEquals("O-RAN", new JSONObject(str).getString("device-type"));
        con = new NetworkElementConnectionBuilder().setDeviceType(NetworkElementDeviceType.OROADM).build();
        str = null;
        try {
            str = MAPPER.writeValueAsString(con);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertEquals("O-ROADM", new JSONObject(str).getString("device-type"));
    }
}
