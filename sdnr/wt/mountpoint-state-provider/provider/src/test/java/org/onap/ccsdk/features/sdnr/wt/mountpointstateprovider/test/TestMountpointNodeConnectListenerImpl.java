/*******************************************************************************
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
 ******************************************************************************/

package org.onap.ccsdk.features.sdnr.wt.mountpointstateprovider.test;

import static org.junit.Assert.assertNotEquals;

import java.util.Optional;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.mountpointstateprovider.impl.MountpointNodeConnectListenerImpl;
import org.onap.ccsdk.features.sdnr.wt.mountpointstateprovider.impl.MountpointStatePublisher;
import org.onap.ccsdk.features.sdnr.wt.mountpointstateprovider.test.mock.NetconfAccessorMock;
import org.onap.ccsdk.features.sdnr.wt.mountpointstateprovider.test.mock.NetconfNodeMock;
import org.onap.ccsdk.features.sdnr.wt.mountpointstateprovider.test.mock.odlapi.DataBrokerMountpointMock;
import org.onap.ccsdk.features.sdnr.wt.mountpointstateprovider.test.mock.odlapi.MountPointMockNew;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.MountPoint;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

public class TestMountpointNodeConnectListenerImpl {

    MountpointNodeConnectListenerImpl nodeConnectListener = new MountpointNodeConnectListenerImpl();
    NetconfNodeMock netconfNodeMock = new NetconfNodeMock();
    NetconfNode netconfNode = netconfNodeMock.getNetconfNode();
    NodeId nNodeId = new NodeId("nSky");
    DataBroker netconfNodeDataBroker = new DataBrokerMountpointMock();
    MountPoint mountpoint = new MountPointMockNew();
    NetconfAccessor accessor = new NetconfAccessorMock(nNodeId, netconfNode, mountpoint, netconfNodeDataBroker);

    @Test
    public void testOnEnterConnected() {
        nodeConnectListener.onEnterConnected(accessor);
        assertNotEquals(MountpointStatePublisher.stateObjects.size(), 0);
    }

    @Test
    public void testOnLeaveConnected() {
        nodeConnectListener.onLeaveConnected(nNodeId, Optional.of(netconfNode));
        assertNotEquals(MountpointStatePublisher.stateObjects.size(), 0);
    }

    @Test
    public void testClose() throws Exception {
        //assertEquals(MountpointStatePublisher.stateObjects.size(), 0);
        nodeConnectListener.close();
    }

}
