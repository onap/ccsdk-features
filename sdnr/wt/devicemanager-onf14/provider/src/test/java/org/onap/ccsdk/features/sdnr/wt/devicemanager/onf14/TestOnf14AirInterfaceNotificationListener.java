/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property. All rights reserved.
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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.impl.Onf14AirInterfaceNotificationListener;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.test.mock.NetconfAccessorMock;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.FaultService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.NotificationService;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.AttributeValueChangedNotification;
import org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.ObjectCreationNotification;
import org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.ObjectDeletionNotification;
import org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.ProblemNotification;
import org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.SEVERITYTYPE;
import org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.SEVERITYTYPECRITICAL;
import org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.SEVERITYTYPEMAJOR;
import org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.SEVERITYTYPEMINOR;
import org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.SEVERITYTYPENONALARMED;
import org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.SEVERITYTYPEWARNING;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.UniversalId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

public class TestOnf14AirInterfaceNotificationListener extends Mockito {
    private NetconfAccessor accessor;
    private NodeId nodeId;
    private DeviceManagerServiceProvider serviceProvider;
    private FaultService faultService;
    private DataProvider databaseService;
    private NotificationService notificationService;

    private ObjectDeletionNotification deletionNotif;
    private ObjectCreationNotification creationNotif;
    private ProblemNotification problemNotif;
    private AttributeValueChangedNotification attrValChangedNotif;

    @Before
    public void init() {
        accessor = mock(NetconfAccessorMock.class);
        nodeId = mock(NodeId.class);
        serviceProvider = mock(DeviceManagerServiceProvider.class);
        faultService = mock(FaultService.class);
        databaseService = mock(DataProvider.class);
        notificationService = mock(NotificationService.class);

        problemNotif = mock(ProblemNotification.class);
        deletionNotif = mock(ObjectDeletionNotification.class);
        creationNotif = mock(ObjectCreationNotification.class);
        attrValChangedNotif = mock(AttributeValueChangedNotification.class);

        when(accessor.getNodeId()).thenReturn(nodeId);
        when(problemNotif.getCounter()).thenReturn(10);
        when(problemNotif.getTimestamp()).thenReturn(new DateAndTime("2020-02-05T12:30:45.283Z"));
        when(problemNotif.getObjectIdRef()).thenReturn(new UniversalId("12345678-0123-0abc-abcd-0123456789AB"));
        when(problemNotif.getProblem()).thenReturn("modulationIsDownShifted");

        when(attrValChangedNotif.getAttributeName()).thenReturn("12345678-0123-2345-abcd-0123456789AB");
        when(attrValChangedNotif.getCounter()).thenReturn(20);
        when(attrValChangedNotif.getNewValue()).thenReturn("new-value");
        when(attrValChangedNotif.getObjectIdRef()).thenReturn(new UniversalId("12345678-0123-1234-abcd-0123456789AB"));
        when(attrValChangedNotif.getTimestamp()).thenReturn(new DateAndTime("2020-02-05T12:30:45.283Z"));

        when(creationNotif.getObjectType()).thenReturn("air-interface-name");
        when(creationNotif.getCounter()).thenReturn(20);
        when(creationNotif.getObjectIdRef()).thenReturn(new UniversalId("12345678-0123-1234-abcd-0123456789AB"));
        when(creationNotif.getTimestamp()).thenReturn(new DateAndTime("2020-02-05T12:30:45.283Z"));

        when(deletionNotif.getCounter()).thenReturn(20);
        when(deletionNotif.getObjectIdRef()).thenReturn(new UniversalId("12345678-0123-1234-abcd-0123456789AB"));
        when(deletionNotif.getTimestamp()).thenReturn(new DateAndTime("2020-02-05T12:30:45.283Z"));

        when(serviceProvider.getFaultService()).thenReturn(faultService);
        when(serviceProvider.getDataProvider()).thenReturn(databaseService);
        when(serviceProvider.getNotificationService()).thenReturn(notificationService);
    }

    @Test
    public void testOtherNotif() {
        Onf14AirInterfaceNotificationListener notifListener =
                new Onf14AirInterfaceNotificationListener(accessor, serviceProvider);

        notifListener.onObjectDeletionNotification(deletionNotif);
        notifListener.onObjectCreationNotification(creationNotif);
        notifListener.onAttributeValueChangedNotification(attrValChangedNotif);
    }

    @Test
    public void testProblemNotifCritical() {
        Onf14AirInterfaceNotificationListener notifListener =
                new Onf14AirInterfaceNotificationListener(accessor, serviceProvider);

        when(problemNotif.getSeverity()).thenAnswer(new Answer<Class<? extends SEVERITYTYPE>>() {
            @Override
            public Class<? extends SEVERITYTYPE> answer(InvocationOnMock invocation) throws Throwable {
                return SEVERITYTYPECRITICAL.class;
            }
        });

        notifListener.onProblemNotification(problemNotif);
    }

    @Test
    public void testProblemNotifMajor() {
        Onf14AirInterfaceNotificationListener notifListener =
                new Onf14AirInterfaceNotificationListener(accessor, serviceProvider);

        when(problemNotif.getSeverity()).thenAnswer(new Answer<Class<? extends SEVERITYTYPE>>() {
            @Override
            public Class<? extends SEVERITYTYPE> answer(InvocationOnMock invocation) throws Throwable {
                return SEVERITYTYPEMAJOR.class;
            }
        });

        notifListener.onProblemNotification(problemNotif);
    }

    @Test
    public void testProblemNotifMinor() {
        Onf14AirInterfaceNotificationListener notifListener =
                new Onf14AirInterfaceNotificationListener(accessor, serviceProvider);

        when(problemNotif.getSeverity()).thenAnswer(new Answer<Class<? extends SEVERITYTYPE>>() {
            @Override
            public Class<? extends SEVERITYTYPE> answer(InvocationOnMock invocation) throws Throwable {
                return SEVERITYTYPEMINOR.class;
            }
        });

        notifListener.onProblemNotification(problemNotif);
    }

    @Test
    public void testProblemNotifWarning() {
        Onf14AirInterfaceNotificationListener notifListener =
                new Onf14AirInterfaceNotificationListener(accessor, serviceProvider);

        when(problemNotif.getSeverity()).thenAnswer(new Answer<Class<? extends SEVERITYTYPE>>() {
            @Override
            public Class<? extends SEVERITYTYPE> answer(InvocationOnMock invocation) throws Throwable {
                return SEVERITYTYPEWARNING.class;
            }
        });

        notifListener.onProblemNotification(problemNotif);
    }

    @Test
    public void testProblemNotifNonalarmed() {
        Onf14AirInterfaceNotificationListener notifListener =
                new Onf14AirInterfaceNotificationListener(accessor, serviceProvider);

        when(problemNotif.getSeverity()).thenAnswer(new Answer<Class<? extends SEVERITYTYPE>>() {
            @Override
            public Class<? extends SEVERITYTYPE> answer(InvocationOnMock invocation) throws Throwable {
                return SEVERITYTYPENONALARMED.class;
            }
        });

        notifListener.onProblemNotification(problemNotif);
    }

    @Test
    public void testProblemNotifNull() {
        Onf14AirInterfaceNotificationListener notifListener =
                new Onf14AirInterfaceNotificationListener(accessor, serviceProvider);

        when(problemNotif.getSeverity()).thenReturn(null);

        notifListener.onProblemNotification(problemNotif);
    }

}
