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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf;

import static org.junit.Assert.fail;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.YangToolsMapper;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.UniversalId;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.ProblemNotification;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.ProblemNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.SeverityType;

public class TestSerializer {

    @Test
    public void testProblemNotification() {
        ProblemNotification notification = new ProblemNotificationBuilder().setCounter(32)
                .setObjectIdRef(new UniversalId("abc")).setProblem("problem").setSeverity(SeverityType.Critical)
                .setTimeStamp(DateAndTime.getDefaultInstance("2020-01-01T01:01:02.0Z")).build();


        YangToolsMapper mapper = new YangToolsMapper();
        String result=null;
        try {
            result = mapper.writeValueAsString(notification);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        System.out.println(result);
    }
}
