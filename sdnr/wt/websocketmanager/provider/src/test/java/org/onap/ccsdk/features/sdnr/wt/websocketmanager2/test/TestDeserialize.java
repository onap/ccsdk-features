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

import static org.junit.Assert.fail;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.data.ScopeRegistration;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.YangToolsMapper;

public class TestDeserialize {

    private static final String SCOPE_REGISTRATION_JSON = "{\n" + "  \"data\":\"scopes\",\n" + "  \"scopes\":[\n"
            + "    {\n" + "      \"node-id\":\"ROADM-A\",\n" + "      \"schema\":{\n"
            + "          \"namespace\":\"onf:params:xml:ns:yang:microwave-model\",\n"
            + "          \"revision\":\"2018-10-10\",\n" + "          \"notification\":[\"problem-notification\"]\n"
            + "       }\n" + "    }\n" + "  ]\n" + "}";
    private static final String SCOPE_REGISTRATION2_JSON = "{\n" + "  \"data\":\"scopes\",\n" + "  \"scopes\":[\n"
            + "    {\n" + "      \"node-id\":\"ROADM-A\",\n" + "      \"schema\":{\n"
            + "          \"namespace\":\"onf:params:xml:ns:yang:microwave-model\",\n"
            + "          \"revision\":\"2018-10-10\",\n" + "          \"notification\":[\"problem-notification\"]\n"
            + "       }\n" + "    }\n" + "  ],\n" + "  \"ratio\":\"120/min\"\n" + "}";
    private static final String SCOPE_REGISTRATION3_INVALID_JSON = "{\n" + "  \"data\":\"scopes\",\n"
            + "  \"scopes\":[\n" + "    {\n" + "      \"node-id\":\"ROADM-A\",\n" + "      \"schema\":{\n"
            + "          \"namespace\":\"onf:params:xml:ns:yang:microwave-model\",\n"
            + "          \"revision\":\"2018-10-10\",\n" + "          \"notification\":[\"problem-notification\"]\n"
            + "       }\n" + "    }\n" + "  ],\n" + "  \"ratio\":\"120/sec\"\n" + "}";

    @Test
    public void testScopeRegistration() {
        YangToolsMapper mapper = new YangToolsMapper();
        ScopeRegistration obj = null;
        try {
            obj = mapper.readValue(SCOPE_REGISTRATION_JSON, ScopeRegistration.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        System.out.println(obj);
    }

    @Test
    public void testScopeRegistration2() {
        YangToolsMapper mapper = new YangToolsMapper();
        ScopeRegistration obj = null;
        try {
            obj = mapper.readValue(SCOPE_REGISTRATION2_JSON, ScopeRegistration.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        System.out.println(obj);
    }

    @Test
    public void testScopeRegistration3() {
        YangToolsMapper mapper = new YangToolsMapper();
        try {
            mapper.readValue(SCOPE_REGISTRATION3_INVALID_JSON, ScopeRegistration.class);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            // e.printStackTrace();
            return;
        }
        fail("json should not contain a valid ratio");
    }
}
