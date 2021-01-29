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

import java.io.IOException;
import org.jline.utils.Log;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.yangtools.YangToolsMapper2;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.types.rev191129.PmDataType;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.types.rev191129.PmDataTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.PmdataEntity;

public class TestYangGenSalMappingOpenRoadm {

    @Test
    public void testOpenroadmPM() throws IOException, ClassNotFoundException {
        out(method());
        // @formatter:off
        String jsonString = "{\n"
                + "    \"time-stamp\":\"2020-09-24T22:59:48.6Z\", \n"
                + "    \"node-name\":\"Rdm-1\",\n"
                + "    \"uuid-interface\":\"physical-link\",\n"
                + "    \"scanner-id\":\"ryyyyyyyryryr\",\n"
                + "    \"granularity-period\":\"Period24Hours\",\n"
                + "    \"performance-data\":{\n"
                + "         \"measurement\":[{\n"
                + "            \"pm-value\":{\"uint64\":1464170942461338033},\n"
                + "            \"pm-key\":\"org.opendaylight"
                + ".yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.openroadm.pm.types.rev200413.OpticalPowerInputOSCMin\"\n"
                + "         }]\n"
                + "        }\n"
                + "}\n";
        // @formatter:on
        // Map to JSON String to Object
        PmDataTypeBuilder builder = new PmDataTypeBuilder();
        builder.setInt64("10");
        PmDataType pmDataType = builder.build();
        out("Result is: "+pmDataType);

        YangToolsMapper2<PmdataEntity> mapper2 = new YangToolsMapper2<>(PmdataEntity.class, null);
        out("Created mapper");
        PmdataEntity generatepmdNode = mapper2.readValue(jsonString.getBytes(), PmdataEntity.class);
        out(generatepmdNode.toString()); // Print it with specified indentation
    }

    /*
     * --------------------------------- Private
     */

    private static String method() {
        String nameofCurrMethod = new Throwable().getStackTrace()[1].getMethodName();
        return nameofCurrMethod;
    }

    private static void out(String text) {
        System.out.println("----------------------");
        System.out.println(text);
        Log.info("Log: "+text);
    }

}
