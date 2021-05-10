/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=======================================================
 *
 */

package org.onap.ccsdk.features.lib.npm.utils;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class NpmUtilTest {

    @Test
    public void testGetListFromJsonString() {
        String content = "{\"key\":\"value\"}";
        List<JsonNode> jsonNodeForString = NpmUtils.getListFromJsonString(content, JsonNode.class);
        assertEquals(1, jsonNodeForString.size());

        content = "[{\"key\":\"value\"}, {\"key\":\"value2\"}]";
        jsonNodeForString = NpmUtils.getListFromJsonString(content, JsonNode.class);
        assertEquals(2, jsonNodeForString.size());
    }

}
