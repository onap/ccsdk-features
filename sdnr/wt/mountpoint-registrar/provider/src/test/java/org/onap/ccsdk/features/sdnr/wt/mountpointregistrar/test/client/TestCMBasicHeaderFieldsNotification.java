/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt mountpoint-registrar
 * =================================================================================================
 * Copyright (C) 2021 Samsung Electronics Intellectual Property. All rights reserved.
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
package org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.test.client;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.vesdomain.cm.CMBasicHeaderFieldsNotification;

public class TestCMBasicHeaderFieldsNotification {

    private CMBasicHeaderFieldsNotification cmBasicFields;

    @Test
    public void testCMBasicFieldsBuilder() {
        cmBasicFields = cmBasicFields.builder()
            .withCMNodeId("test-node")
            .withCMSequence("1")
            .withCMOccurrenceTime("2021-10-18T15:25:19.948Z")
            .withSourceId("src_device_id_1732")
            .withNotificationType("notifyMOIChanges")
            .build();

        assertEquals("test-node", cmBasicFields.getCmNodeId());
        assertEquals("1", cmBasicFields.getCmSequence());
        assertEquals("src_device_id_1732", cmBasicFields.getSourceId());
        assertEquals("2021-10-18T15:25:19.948Z", cmBasicFields.getCmOccurrenceTime());
        assertEquals("notifyMOIChanges", cmBasicFields.getNotificationType());
    }
}