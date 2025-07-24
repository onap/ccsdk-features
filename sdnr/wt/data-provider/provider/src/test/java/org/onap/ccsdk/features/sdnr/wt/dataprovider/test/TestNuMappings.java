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

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.yangtools.DataProviderYangToolsMapper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Faultcurrent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultcurrentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SeverityType;

public class TestNuMappings {

    @Test
    public void testMapObjectToJson() throws IOException {
        DataProviderYangToolsMapper mapper = new DataProviderYangToolsMapper();

        Faultcurrent c = new FaultcurrentBuilder().setSeverity(SeverityType.Critical).build();
        String json = mapper.writeValueAsString(c);
        assertTrue("Critical expected", json.contains(SeverityType.Critical.getName()));
    }

    @Test
    public void testMapJsonToObject() throws IOException {
        DataProviderYangToolsMapper mapper = new DataProviderYangToolsMapper();

        Faultcurrent f = mapper.readValue("{\"severity\":\"Critical\"}", Faultcurrent.class);
        assertTrue("Critical expected", f.getSeverity().equals(SeverityType.Critical));
    }

}
