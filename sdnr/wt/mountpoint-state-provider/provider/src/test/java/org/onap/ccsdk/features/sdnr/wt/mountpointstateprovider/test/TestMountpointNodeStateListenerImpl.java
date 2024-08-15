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


import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.NetconfNetworkElementService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorService;
import org.onap.ccsdk.features.sdnr.wt.mountpointstateprovider.impl.MountpointNodeStateListenerImpl;
import org.onap.ccsdk.features.sdnr.wt.mountpointstateprovider.impl.MountpointStatePublisher;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNodeStateService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Host;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.device.rev240118.ConnectionOper.ConnectionStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev221225.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.common.Uint16;

public class TestMountpointNodeStateListenerImpl extends Mockito {

    NetconfNodeStateService netconfNodeStateServiceMock = mock(NetconfNodeStateService.class);
    MountpointNodeStateListenerImpl nodeStateListener =
            new MountpointNodeStateListenerImpl(netconfNodeStateServiceMock);
    MountpointStatePublisher mountpointStatePublisher;
    NetconfNode netconfNode;
    NodeId nNodeId = new NodeId("nSky");
    VESCollectorService vesCollectorService;

    @Before
    public void initialize() {
        DeviceManagerServiceProvider serviceProvider = mock(DeviceManagerServiceProvider.class);
        vesCollectorService = mock(VESCollectorService.class);
        NetconfNetworkElementService netconfNetworkElementService = mock(NetconfNetworkElementService.class);
        when(netconfNetworkElementService.getServiceProvider()).thenReturn(serviceProvider);
        when(serviceProvider.getVESCollectorService()).thenReturn(vesCollectorService);
        mountpointStatePublisher = new MountpointStatePublisher(vesCollectorService);
        nodeStateListener.start(mountpointStatePublisher);
        netconfNode = mock(NetconfNode.class);
        when(netconfNode.getHost()).thenReturn(new Host(new IpAddress(new Ipv4Address("1.2.3.4"))));
        when(netconfNode.getPort()).thenReturn(new PortNumber(Uint16.valueOf(2230)));
        when(netconfNode.getConnectionStatus()).thenReturn(ConnectionStatus.Connected);
    }

    @Test
    public void testOnCreated() {
        assertNotNull(nNodeId);
        assertNotNull(netconfNode);
        nodeStateListener.onCreated(nNodeId, netconfNode);
        assertNotEquals(mountpointStatePublisher.getStateObjects().size(), 0);
    }

    @Test
    public void testOnStateChange() {
        nodeStateListener.onStateChange(nNodeId, netconfNode);
        assertNotEquals(mountpointStatePublisher.getStateObjects().size(), 0);
    }

    @Test
    public void testOnRemoved() {
        nodeStateListener.onRemoved(nNodeId);
        assertNotEquals(mountpointStatePublisher.getStateObjects().size(), 0);
    }

}
