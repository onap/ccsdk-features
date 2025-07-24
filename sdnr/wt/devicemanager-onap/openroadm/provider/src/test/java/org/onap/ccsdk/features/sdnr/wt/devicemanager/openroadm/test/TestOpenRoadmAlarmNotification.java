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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.openroadm.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.openroadm.impl.OpenroadmFaultNotificationListener;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.FaultService;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.WebsocketManagerService;
import org.opendaylight.yang.gen.v1.http.org.openroadm.alarm.rev191129.AlarmNotification;
import org.opendaylight.yang.gen.v1.http.org.openroadm.alarm.rev191129.Severity;
import org.opendaylight.yang.gen.v1.http.org.openroadm.alarm.rev191129.alarm.ProbableCause;
import org.opendaylight.yang.gen.v1.http.org.openroadm.alarm.rev191129.alarm.ProbableCauseBuilder;
import org.opendaylight.yang.gen.v1.http.org.openroadm.alarm.rev191129.alarm.Resource;
import org.opendaylight.yang.gen.v1.http.org.openroadm.alarm.rev191129.alarm.ResourceBuilder;
import org.opendaylight.yang.gen.v1.http.org.openroadm.common.node.types.rev191129.NodeIdType;
import org.opendaylight.yang.gen.v1.http.org.openroadm.probablecause.rev191129.ProbableCauseEnum;
import org.opendaylight.yang.gen.v1.http.org.openroadm.resource.rev191129.resource.Device;
import org.opendaylight.yang.gen.v1.http.org.openroadm.resource.rev191129.resource.DeviceBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

public class TestOpenRoadmAlarmNotification {
    private static final String myCircuitId = "Test_Id";
    private static final String myId = "Alarm_Id";
    DateAndTime myRaiseTime = new DateAndTime("2020-02-25T10:08:06.7Z");
    ProbableCause myProbableCause =
            new ProbableCauseBuilder().setCause(ProbableCauseEnum.AutomaticLaserShutdown).build();
    Device device = new DeviceBuilder().setNodeId(NodeIdType.getDefaultInstance("zNhe2i5")).build();
    org.opendaylight.yang.gen.v1.http.org.openroadm.resource.rev191129.resource.resource.resource.CircuitPack resVal =
            new org.opendaylight.yang.gen.v1.http.org.openroadm.resource.rev191129.resource.resource.resource.CircuitPackBuilder()
            .setCircuitPackName("Slot-0-Port-A").build();
    org.opendaylight.yang.gen.v1.http.org.openroadm.resource.rev191129.resource.Resource affectedResource =
            new org.opendaylight.yang.gen.v1.http.org.openroadm.resource.rev191129.resource.ResourceBuilder().setResource(resVal).build();
    Resource myResource = new ResourceBuilder().setResource(affectedResource).setDevice(device).build();


    static DeviceManagerServiceProvider serviceProvider;
    static @NonNull FaultService faultService;
    static AlarmNotification notification;
    Severity severity;
    static NetconfBindingAccessor accessor;
    static WebsocketManagerService notificationService;
    static NodeId nNodeId = new NodeId("nSky");
    @BeforeClass
    public static void init() throws InterruptedException, IOException {

        accessor = mock(NetconfBindingAccessor.class);
        serviceProvider = mock(DeviceManagerServiceProvider.class);
        faultService = mock(FaultService.class);
        notificationService = mock(WebsocketManagerService.class);
    }



    @Test
    public void testNotification() {
        severity = Severity.Critical;
        when(accessor.getNodeId()).thenReturn(nNodeId);
        when(serviceProvider.getFaultService()).thenReturn(faultService);
        when(serviceProvider.getWebsocketService()).thenReturn(notificationService);
        OpenroadmFaultNotificationListener alarmListener =
                new OpenroadmFaultNotificationListener(accessor, serviceProvider);
        notification = mock(AlarmNotification.class);

        when(notification.getId()).thenReturn(myId);
        when(notification.getCircuitId()).thenReturn(myCircuitId);
        when(notification.getRaiseTime()).thenReturn(myRaiseTime);
        when(notification.getProbableCause()).thenReturn(myProbableCause);
        when(notification.getResource()).thenReturn(myResource);
        when(notification.getSeverity()).thenReturn(severity);
        alarmListener.onAlarmNotification(notification);
        System.out.println(notification.getId());
        assertEquals(myId, notification.getId());
        assertEquals(myCircuitId, notification.getCircuitId());
        assertEquals(myRaiseTime, notification.getRaiseTime());
        assertEquals(myProbableCause, notification.getProbableCause());
        assertEquals(myResource, notification.getResource());
        assertEquals(severity, notification.getSeverity());


    }

}
