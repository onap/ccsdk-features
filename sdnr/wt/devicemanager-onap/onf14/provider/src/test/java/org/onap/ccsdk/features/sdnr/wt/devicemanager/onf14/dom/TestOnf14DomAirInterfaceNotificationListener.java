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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom;

import java.time.Instant;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.util.Onf14DevicemanagerQNames;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.notifications.Onf14DomAirInterfaceNotificationListener;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.util.NetconfDeviceNotification;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.FaultService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.NotificationService;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.WebsocketManagerService;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;

public class TestOnf14DomAirInterfaceNotificationListener extends Mockito {
    private NetconfDomAccessor accessor;
    private NodeId nodeId;
    private DeviceManagerServiceProvider serviceProvider;
    private FaultService faultService;
    private DataProvider databaseService;
    private NotificationService notificationService;

    private @NonNull WebsocketManagerService websocketService;

    @Before
    public void init() {
        accessor = mock(NetconfDomAccessor.class);
        nodeId = mock(NodeId.class);
        serviceProvider = mock(DeviceManagerServiceProvider.class);
        faultService = mock(FaultService.class);
        databaseService = mock(DataProvider.class);
        notificationService = mock(NotificationService.class);
        websocketService = mock(WebsocketManagerService.class);

        when(accessor.getNodeId()).thenReturn(nodeId);
        when(serviceProvider.getFaultService()).thenReturn(faultService);
        when(serviceProvider.getDataProvider()).thenReturn(databaseService);
        when(serviceProvider.getNotificationService()).thenReturn(notificationService);
        when(serviceProvider.getWebsocketService()).thenReturn(websocketService);
    }

    @Test
    public void testOtherNotif() {
        Onf14DomAirInterfaceNotificationListener notifListener =
                new Onf14DomAirInterfaceNotificationListener(accessor, serviceProvider);

        NetconfDeviceNotification ndn =
                new NetconfDeviceNotification(createAirInterfaceCreationNotification(), Instant.now());
        notifListener.onNotification(ndn);
        ndn = new NetconfDeviceNotification(createAirInterfaceAVCNotification(), Instant.now());
        notifListener.onNotification(ndn);
        ndn = new NetconfDeviceNotification(createAirInterfaceDeletionNotification(), Instant.now());
        notifListener.onNotification(ndn);
        ndn = new NetconfDeviceNotification(createAirInterfaceProblemNotification(), Instant.now());
        notifListener.onNotification(ndn);
    }

    private ContainerNode createAirInterfaceProblemNotification() {
        return Builders.containerBuilder()
                .withNodeIdentifier(
                        NodeIdentifier.create(Onf14DevicemanagerQNames.AIR_INTERFACE_OBJECT_PROBLEM_NOTIFICATION))
                .withChild(ImmutableNodes
                        .leafNode(Onf14DevicemanagerQNames.AIR_INTERFACE_OBJECT_PROBLEM_NOTIFICATION_COUNTER, "47"))
                .withChild(ImmutableNodes.leafNode(
                        Onf14DevicemanagerQNames.AIR_INTERFACE_OBJECT_PROBLEM_NOTIFICATION_PROBLEM,
                        "12345678-0123-2345-abcd-0123456789AB"))
                .withChild(ImmutableNodes.leafNode(
                        Onf14DevicemanagerQNames.AIR_INTERFACE_OBJECT_PROBLEM_NOTIFICATION_OBJECT_ID_REF,
                        "12345678-0123-1234-abcd-0123456789AB"))
                .withChild(ImmutableNodes.leafNode(
                        Onf14DevicemanagerQNames.AIR_INTERFACE_OBJECT_PROBLEM_NOTIFICATION_TIMESTAMP,
                        "2022-02-05T12:30:45.283Z"))
                .withChild(ImmutableNodes.leafNode(
                        Onf14DevicemanagerQNames.AIR_INTERFACE_OBJECT_PROBLEM_NOTIFICATION_SEVERITY,
                        "SEVERITY_TYPE_CRITICAL"))
                .build();
    }

    private ContainerNode createAirInterfaceAVCNotification() {
        return Builders.containerBuilder()
                .withNodeIdentifier(
                        NodeIdentifier.create(Onf14DevicemanagerQNames.AIR_INTERFACE_OBJECT_AVC_NOTIFICATION))
                .withChild(ImmutableNodes
                        .leafNode(Onf14DevicemanagerQNames.AIR_INTERFACE_OBJECT_AVC_NOTIFICATION_COUNTER, "47"))
                .withChild(ImmutableNodes.leafNode(
                        Onf14DevicemanagerQNames.AIR_INTERFACE_OBJECT_AVC_NOTIFICATION_ATTRIBUTE_NAME,
                        "12345678-0123-2345-abcd-0123456789AB"))
                .withChild(ImmutableNodes.leafNode(
                        Onf14DevicemanagerQNames.AIR_INTERFACE_OBJECT_AVC_NOTIFICATION_NEW_VALUE, "new-value"))
                .withChild(ImmutableNodes.leafNode(
                        Onf14DevicemanagerQNames.AIR_INTERFACE_OBJECT_AVC_NOTIFICATION_OBJECT_ID_REF,
                        "12345678-0123-1234-abcd-0123456789AB"))
                .withChild(ImmutableNodes.leafNode(
                        Onf14DevicemanagerQNames.AIR_INTERFACE_OBJECT_AVC_NOTIFICATION_TIMESTAMP,
                        "2022-02-05T12:30:45.283Z"))
                .build();
    }

    private ContainerNode createAirInterfaceDeletionNotification() {
        return Builders.containerBuilder()
                .withNodeIdentifier(
                        NodeIdentifier.create(Onf14DevicemanagerQNames.AIR_INTERFACE_OBJECT_DELETE_NOTIFICATION))
                .withChild(ImmutableNodes
                        .leafNode(Onf14DevicemanagerQNames.AIR_INTERFACE_OBJECT_DELETE_NOTIFICATION_COUNTER, "47"))
                .withChild(ImmutableNodes.leafNode(
                        Onf14DevicemanagerQNames.AIR_INTERFACE_OBJECT_DELETE_NOTIFICATION_TIMESTAMP,
                        "2022-02-05T12:30:45.283Z"))
                .withChild(ImmutableNodes.leafNode(
                        Onf14DevicemanagerQNames.AIR_INTERFACE_OBJECT_DELETE_NOTIFICATION_OBJECT_ID_REF,
                        "12345678-0123-1234-abcd-0123456789AB"))
                .build();
    }

    private ContainerNode createAirInterfaceCreationNotification() {
        return Builders.containerBuilder()
                .withNodeIdentifier(
                        NodeIdentifier.create(Onf14DevicemanagerQNames.AIR_INTERFACE_OBJECT_CREATE_NOTIFICATION))
                .withChild(ImmutableNodes
                        .leafNode(Onf14DevicemanagerQNames.AIR_INTERFACE_OBJECT_CREATE_NOTIFICATION_COUNTER, "47"))
                .withChild(ImmutableNodes.leafNode(
                        Onf14DevicemanagerQNames.AIR_INTERFACE_OBJECT_CREATE_NOTIFICATION_OBJECT_TYPE,
                        "air-interface-name"))
                .withChild(ImmutableNodes.leafNode(
                        Onf14DevicemanagerQNames.AIR_INTERFACE_OBJECT_CREATE_NOTIFICATION_TIMESTAMP,
                        "2022-02-05T12:30:45.283Z"))
                .withChild(ImmutableNodes.leafNode(
                        Onf14DevicemanagerQNames.AIR_INTERFACE_OBJECT_CREATE_NOTIFICATION_OBJECT_ID_REF,
                        "12345678-0123-1234-abcd-0123456789AB"))
                .build();
    }

}
