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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.openroadm.impl.OpenroadmDeviceChangeNotificationListener;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.opendaylight.yang.gen.v1.http.org.openroadm.common.types.rev191129.RpcStatus;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.ChangeNotification;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.CreateTechInfoNotification;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.OrgOpenroadmDevice;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.change.notification.Edit;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf.base._1._0.rev110601.EditOperationType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class TestOpenRoadmDeviceChangeNotification {

    private NetconfAccessor accessor = mock(NetconfAccessor.class);
    private DataProvider databaseProvider = mock(DataProvider.class);
    static ChangeNotification notification = mock(ChangeNotification.class);
    static CreateTechInfoNotification notificationTechInfo = mock(CreateTechInfoNotification.class);
    final EditOperationType operation = EditOperationType.Merge;
    private NodeId nodeId = new NodeId("RoadmA2");
    Edit change = mock(Edit.class);
    final Class<OrgOpenroadmDevice> clazzRoadm = OrgOpenroadmDevice.class;
    OpenroadmDeviceChangeNotificationListener changeListener =
            new OpenroadmDeviceChangeNotificationListener(accessor, databaseProvider);
    InstanceIdentifier<?> target = InstanceIdentifier.builder(clazzRoadm).build();

    @Before
    public void init() {
        doReturn(target).when(change).getTarget();
        when(change.getOperation()).thenReturn(operation);
        when(accessor.getNodeId()).thenReturn(nodeId);
    }

    @Test
    public void testOnChangeNotification() {
        when(notification.getChangeTime()).thenReturn(new DateAndTime("2017-10-22T15:23:43Z"));
        changeListener.onChangeNotification(notification);
    }

    @Test
    public void testCreateTechInfoNotification() {
        when(notificationTechInfo.getShelfId()).thenReturn("Shelf688");
        when(notificationTechInfo.getStatus()).thenReturn(RpcStatus.Successful);
        changeListener.onCreateTechInfoNotification(notificationTechInfo);
    }

}
