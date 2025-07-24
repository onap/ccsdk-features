/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2021 highstreet technologies GmbH Intellectual Property.
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.data.NotificationOutput;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.YangToolsMapper;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.ObjectCreationNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.ObjectCreationNotificationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestSerializer {

    private static final Logger LOG = LoggerFactory.getLogger(TestSerializer.class);
     private static final YangToolsMapper mapper = new YangToolsMapper();
    private static final String TIMESTAMP = "2020-04-01T10:20:40.0Z";
    private static final String NODEID = "node1";

    @Test
    public void test1() {
        ObjectCreationNotification notification = new ObjectCreationNotificationBuilder().setCounter(Integer.valueOf(5)).build();
        NotificationOutput output = new NotificationOutput(notification, NODEID, ObjectCreationNotification.QNAME,DateAndTime.getDefaultInstance(TIMESTAMP));
        String sOutput=null;
        try {
            sOutput = mapper.writeValueAsString(output);
            LOG.debug(sOutput);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertNotNull(sOutput);
        assertTrue(sOutput.contains("\"type\""));
    }
}
