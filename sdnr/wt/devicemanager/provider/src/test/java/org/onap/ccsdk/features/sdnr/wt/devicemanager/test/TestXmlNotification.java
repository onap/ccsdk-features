/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk feature sdnr wt
 *  ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.ccsdk.features.sdnr.wt.devicemanager.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.util.InternalDateAndTime;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.util.InternalSeverity;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.ObjectCreationNotificationXml;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.ObjectDeletionNotificationXml;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.ProblemNotificationXml;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.SourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestXmlNotification {

    private static final Logger log = LoggerFactory.getLogger(TestXmlNotification.class);

    @Test
    public void test() {

        ProblemNotificationXml notification = new ProblemNotificationXml("TestMointpoint", "network-element",
                "problemName", InternalSeverity.Critical, 123, InternalDateAndTime.getTestpattern());

        notification.getFaultlog(SourceType.Unknown);

        notification.getFaultcurrent();

        notification.isNotManagedAsCurrentProblem();

        ProblemNotificationXml.debugResultList(log, "uuid", Arrays.asList(notification), 0);

    }

    @Test
    public void testNoAlarm() {

        ProblemNotificationXml notification;
        notification = new ProblemNotificationXml("TestMointpoint", "network-element", "problemName",
                InternalSeverity.Critical, 123, InternalDateAndTime.getTestpattern());

        assertFalse("Critical", notification.isNoAlarmIndication());

        notification = new ProblemNotificationXml("TestMointpoint", "network-element", "problemName",
                InternalSeverity.NonAlarmed, 123, InternalDateAndTime.getTestpattern());

        assertTrue("NonAlarm", notification.isNoAlarmIndication());

    }

    @Test
    public void testObjectCreationNotification() {

        ObjectCreationNotificationXml notification;
        notification =
                new ObjectCreationNotificationXml("TestMointpoint1", 1, InternalDateAndTime.getTestpattern(), "Id1");

    }

    @Test
    public void testDeletionCreationNotification() {

        ObjectDeletionNotificationXml notification;
        notification =
                new ObjectDeletionNotificationXml("TestMointpoint2", 2, InternalDateAndTime.getTestpattern(), "Id2");

    }


}
