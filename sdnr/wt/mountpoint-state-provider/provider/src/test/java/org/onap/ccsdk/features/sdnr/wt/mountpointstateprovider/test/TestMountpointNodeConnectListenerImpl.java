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

package org.onap.ccsdk.features.sdnr.wt.mountpointstateprovider.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.NetconfNetworkElementService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorService;
import org.onap.ccsdk.features.sdnr.wt.mountpointstateprovider.impl.MountpointNodeConnectListenerImpl;
import org.onap.ccsdk.features.sdnr.wt.mountpointstateprovider.impl.MountpointStatePublisher;
import org.onap.ccsdk.features.sdnr.wt.mountpointstateprovider.test.mock.NetconfAccessorMock;
import org.onap.ccsdk.features.sdnr.wt.mountpointstateprovider.test.mock.NetconfNodeMock;
import org.onap.ccsdk.features.sdnr.wt.mountpointstateprovider.test.mock.NetconfNodeStateServiceMock;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

public class TestMountpointNodeConnectListenerImpl {

    DeviceManagerServiceProvider serviceProvider;
    MountpointStatePublisher mountpointStatePublisher;
    NetconfNodeStateServiceMock netconfNodeStateServiceMock;
    MountpointNodeConnectListenerImpl nodeConnectListener;
    NetconfNodeMock netconfNodeMock;
    NetconfNode netconfNode;
    NodeId nNodeId;
    NetconfAccessor accessor;
    VESCollectorService vesCollectorService;

    @Before
    public void initialize() throws IOException {
        serviceProvider = mock(DeviceManagerServiceProvider.class);
        netconfNodeStateServiceMock = new NetconfNodeStateServiceMock();
        netconfNodeMock = new NetconfNodeMock();
        netconfNode = netconfNodeMock.getNetconfNode();
        vesCollectorService = mock(VESCollectorService.class);
        NetconfNetworkElementService netconfNetworkElementService = mock(NetconfNetworkElementService.class);
        nNodeId = new NodeId("nSky");
        accessor = new NetconfAccessorMock(nNodeId, netconfNode);

        mountpointStatePublisher = new MountpointStatePublisher(vesCollectorService);
        when(netconfNetworkElementService.getServiceProvider()).thenReturn(serviceProvider);
        when(serviceProvider.getVESCollectorService()).thenReturn(vesCollectorService);

        nodeConnectListener = new MountpointNodeConnectListenerImpl(netconfNodeStateServiceMock);
        nodeConnectListener.start(mountpointStatePublisher);
    }

    @Test
    public void testOnEnterConnected() {
        nodeConnectListener.onEnterConnected(accessor);
        assertNotEquals(mountpointStatePublisher.getStateObjects().size(), 0);
    }

    @Test
    public void testOnLeaveConnected() {
        nodeConnectListener.onLeaveConnected(nNodeId, Optional.of(netconfNode));
        assertNotEquals(mountpointStatePublisher.getStateObjects().size(), 0);
    }

    @Test
    public void testClose() throws Exception {
        assertEquals(mountpointStatePublisher.getStateObjects().size(), 0);
        nodeConnectListener.close();
    }

}
