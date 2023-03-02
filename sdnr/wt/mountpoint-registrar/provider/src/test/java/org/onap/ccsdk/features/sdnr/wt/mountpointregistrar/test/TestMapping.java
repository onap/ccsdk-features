/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
 * =================================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * ============LICENSE_END==========================================================================
 */
package org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SeverityType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.PushFaultNotificationInputBuilder;

public class TestMapping {

    private final ObjectMapper mapper = new ObjectMapper();


    private static Optional<SeverityType> getSeverity(String faultSeverity) {
        return Optional.ofNullable(SeverityType.forName(faultSeverity)); // <-- mapping provided by generated classes. Manual mapping beneficial.
    }

    private String updateFaultPayload(String faultNodeId, String faultCounter, String faultOccurrenceTime,
            String faultObjectId, String faultReason, String faultSeverity) throws JsonProcessingException {

        PushFaultNotificationInputBuilder faultNotificationBuilder = new PushFaultNotificationInputBuilder();
        faultNotificationBuilder.setNodeId(faultNodeId);
        faultNotificationBuilder.setCounter(Integer.valueOf(faultCounter));
        faultNotificationBuilder.setTimestamp(new DateAndTime(faultOccurrenceTime));
        faultNotificationBuilder.setObjectId(faultObjectId);
        faultNotificationBuilder.setProblem(faultReason);
        Optional<SeverityType> oSeverity = getSeverity(faultSeverity);
        if (oSeverity.isPresent()) {
            faultNotificationBuilder.setSeverity(oSeverity.get());
        } else {
            // Do something to handle the problem
        }
        return mapper.writeValueAsString(faultNotificationBuilder);
    }


    @Test
    public void test() throws JsonProcessingException {
        DateAndTime dt = new DateAndTime("2017-03-01T09:15:00.0Z");

        String result = updateFaultPayload("f1", "34", dt.getValue(), "fefef", "reason", "Critical");

        System.out.println("Res: " + result);
    }

}
