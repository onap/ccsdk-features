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
package org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.test.example;

import java.util.Arrays;
import org.mockito.Mockito;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.NetconfNodeStateServiceImpl;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.access.NetconfAccessorImpl;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.access.NetconfCommunicatorManager;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.access.dom.DomContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.device.rev221225.ConnectionOper.ConnectionStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.device.rev221225.connection.oper.AvailableCapabilitiesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.device.rev221225.connection.oper.available.capabilities.AvailableCapabilityBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev221225.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev221225.NetconfNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeBuilder;

public class TestNetconfHelper extends Mockito {

    /**
     * Provide a test node.
     * @param nodeIdString
     */
    public static Node getTestNode(NodeId nodeId, String capabilityString) {
        NetconfNodeBuilder netconfNodeBuilder = new NetconfNodeBuilder();
        netconfNodeBuilder.setConnectionStatus(ConnectionStatus.Connected);
        AvailableCapabilityBuilder availableCapabilityBuilder = new AvailableCapabilityBuilder();
        availableCapabilityBuilder.setCapability(capabilityString);
        AvailableCapabilitiesBuilder availableCapabilitesBuilder = new AvailableCapabilitiesBuilder();
        availableCapabilitesBuilder.setAvailableCapability(Arrays.asList(availableCapabilityBuilder.build()));
        netconfNodeBuilder.setAvailableCapabilities(availableCapabilitesBuilder.build());
        NetconfNode rootNodeNetconf = netconfNodeBuilder.build();

        NodeBuilder nodeBuilder = new NodeBuilder();

        nodeBuilder.addAugmentation(rootNodeNetconf);
        nodeBuilder.setNodeId(nodeId);
        return nodeBuilder.build();
    }

    /**
     * Provide a test NetconfAccessorImpl
     * @return object NetconfAccessorImpl
     */
    public static NetconfAccessorImpl getNetconfAcessorImpl() {
        NetconfCommunicatorManager netconfCommunicatorManager = mock(NetconfCommunicatorManager.class);
        DomContext domContext = mock(DomContext.class);
        String nodeIdString = "Test";
        String capabilityStringForNetworkElement = "network-element";
        NodeId nodeId = new NodeId(nodeIdString);
        NetconfNode testNode = TestNetconfHelper.getTestNode(nodeId, capabilityStringForNetworkElement)
                .augmentation(NetconfNode.class);

        NetconfNodeStateServiceImpl netconfNodeStateService = mock(NetconfNodeStateServiceImpl.class);
        NetconfAccessorImpl netconfAccessor =
                new NetconfAccessorImpl(nodeId, testNode, netconfCommunicatorManager, domContext, netconfNodeStateService);
        return netconfAccessor;
    }
}
