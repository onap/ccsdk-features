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

import static org.junit.Assert.*;

import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.vesdomain.cm.CMBasicHeaderFieldsNotification;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.vesdomain.cm.CMNotification;

public class TestCMNotificationBuilder {

    private CMNotification cmNotification;

    @Test
    public void testCMNotificationBuilderWithAllDefinedFields() {
        cmNotification = cmNotification.builder()
            .withCMBasicHeaderFieldsNotification(CMBasicHeaderFieldsNotification.builder()
                .withCMNodeId("test-node")
                .withCMSequence("1")
                .withCMOccurrenceTime("2021-10-18T15:25:19.948Z")
                .withSourceId("src_device_id_1732")
                .withNotificationType("notifyMOIChanges")
                .build())
            .withCMNotificationId("123")
            .withCMSourceIndicator("UNKNOWN")
            .withCMPath("http://samsung.com/ves=1")
            .withCMOperation("CREATE")
            .withCMValue("value")
            .build();

        assertEquals("test-node", cmNotification.getBasicHeaderFields().getCmNodeId());
        assertEquals("1", cmNotification.getBasicHeaderFields().getCmSequence());
        assertEquals("src_device_id_1732", cmNotification.getBasicHeaderFields().getSourceId());
        assertEquals("2021-10-18T15:25:19.948Z", cmNotification.getBasicHeaderFields().getCmOccurrenceTime());
        assertEquals("notifyMOIChanges", cmNotification.getBasicHeaderFields().getNotificationType());
        assertEquals("123", cmNotification.getCmNotificationId());
        assertEquals("UNKNOWN", cmNotification.getCmSourceIndicator());
        assertEquals("http://samsung.com/ves=1", cmNotification.getCmPath());
        assertEquals("CREATE", cmNotification.getCmOperation());
        assertEquals("value", cmNotification.getCmValue());
    }

    @Test
    public void testCMNotificationBuilderWithDefaultCMOperation() {
        cmNotification = cmNotification.builder()
            .withCMBasicHeaderFieldsNotification(CMBasicHeaderFieldsNotification.builder()
                .withCMNodeId("test-node")
                .withCMSequence("1")
                .withCMOccurrenceTime("2021-10-18T15:25:19.948Z")
                .withSourceId("src_device_id_1732")
                .withNotificationType("notifyMOIChanges")
                .build())
            .withCMNotificationId("123")
            .withCMSourceIndicator("UNKNOWN")
            .withCMPath("http://samsung.com/ves=1")
            .build();

        assertEquals("test-node", cmNotification.getBasicHeaderFields().getCmNodeId());
        assertEquals("NULL", cmNotification.getCmOperation());
        assertNull(cmNotification.getCmValue());
    }
}