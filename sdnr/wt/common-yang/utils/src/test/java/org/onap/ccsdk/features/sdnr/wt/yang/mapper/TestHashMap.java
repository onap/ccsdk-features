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
package org.onap.ccsdk.features.sdnr.wt.yang.mapper;

import static org.junit.Assert.assertNotNull;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class TestHashMap {

    String mapDataString = "[\n"
            + "    {\n"
            + "        \"Name\":  \"System Idle Process\",\n"
            + "        \"CreationDate\":  \"20160409121836.675345+330\"\n"
            + "    },\n"
            + "    {\n"
            + "        \"Name\":  \"System\",\n"
            + "        \"CreationDate\":  \"20160409121836.675345+330\"\n"
            + "    },\n"
            + "    {\n"
            + "        \"Name\":  \"smss.exe\",\n"
            + "        \"CreationDate\":  \"20160409121836.684966+330\"\n"
            + "    }\n"
            + "]";


    @Test
    public void test() throws JsonParseException, JsonMappingException, IOException {
        byte[] mapData = mapDataString.getBytes();
        List<Map<String, Object>> myMap;
        ObjectMapper objectMapper=new ObjectMapper();
        objectMapper.registerModule(new YangToolsModule());


        myMap = objectMapper.readValue(mapData, new TypeReference<>() {
        });
        assertNotNull(myMap);
        System.out.println("Type1: "+myMap.getClass().getSimpleName());
        System.out.println("Type2: "+myMap.get(0).getClass().getSimpleName());
        System.out.println("Map is: "+myMap);    }

    private class YangToolsModule extends SimpleModule {

        private static final long serialVersionUID = 1L;

        public YangToolsModule() {
            super();
            setDeserializerModifier(new YangToolsDeserializerModifier());
        }
    }

    private class YangToolsDeserializerModifier extends BeanDeserializerModifier {
    }


}
