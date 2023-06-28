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
package org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.access;

import java.util.Objects;
import java.util.Optional;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.NetconfNodeStateServiceImpl;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.access.dom.DomContext;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev221225.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.device.rev221225.ConnectionOper.ConnectionStatus;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetconfAccessorImpl implements NetconfAccessor {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(NetconfAccessorImpl.class);

    private final NodeId nodeId;
    private final NetconfNode netconfNode;
    private final Capabilities capabilities;
    private final NetconfCommunicatorManager netconfCommunicatorManager;
    private final DomContext domContext;
    private final NetconfNodeStateServiceImpl netconfNodeStateService;
    /**
     * Contains all data to access and manage netconf device
     *
     * @param netconfCommunicatorManager
     *
     * @param nodeId of managed netconf node
     * @param netconfNode information
     * @param domContext
     * @param dataBroker to access node
     * @param mountpoint of netconfNode
     */
     public NetconfAccessorImpl(NodeId nodeId, NetconfNode netconfNode,
            NetconfCommunicatorManager netconfCommunicatorManager, DomContext domContext, NetconfNodeStateServiceImpl netconfNodeStateService) {
        super();
        this.nodeId = Objects.requireNonNull(nodeId);
        this.netconfNode = Objects.requireNonNull(netconfNode);
        this.netconfCommunicatorManager = Objects.requireNonNull(netconfCommunicatorManager);
        this.domContext = Objects.requireNonNull(domContext);
        this.netconfNodeStateService = Objects.requireNonNull(netconfNodeStateService);

        ConnectionStatus csts = netconfNode.getConnectionStatus();
        if (csts == null) {
            throw new IllegalStateException(String.format("connection status for %s is not connected", nodeId));
        }
        Capabilities tmp = Capabilities.getAvailableCapabilities(netconfNode);
        if (tmp.getCapabilities().size() <= 0) {
            throw new IllegalStateException(String.format("no capabilities found for %s", nodeId));
        }
        this.capabilities = tmp;
    }

    public NetconfAccessorImpl(NetconfAccessorImpl accessor) {
        this.nodeId = accessor.getNodeId();
        this.netconfNode = accessor.getNetconfNode();
        this.capabilities = accessor.getCapabilites();
        this.netconfCommunicatorManager = accessor.netconfCommunicatorManager;
        this.domContext = accessor.domContext;
        this.netconfNodeStateService = accessor.netconfNodeStateService;
    }

    @Override
    public NodeId getNodeId() {
        return nodeId;
    }

    @Override
    public NetconfNode getNetconfNode() {
        return netconfNode;
    }

    @Override
    public Capabilities getCapabilites() {
        return capabilities;
    }

    @Override
    public Optional<NetconfBindingAccessor> getNetconfBindingAccessor() {
        return netconfCommunicatorManager.getNetconfBindingAccessor(this);
    }

    @Override
    public Optional<NetconfDomAccessor> getNetconfDomAccessor() {
        return netconfCommunicatorManager.getNetconfDomAccessor(this);
    }

    @Override
    public DataBroker getControllerBindingDataBroker() {
        return netconfNodeStateService.getDataBroker();
    }

    @Override
    public DOMDataBroker getControllerDOMDataBroker() {
        return netconfNodeStateService.getDOMDataBroker();
    }

    /**
     * check if nc-notifications.yang is supported by the device
     */
    @Override
    public boolean isNotificationsRFC5277Supported() {
        return getCapabilites().isSupportingNamespace("urn:ietf:params:netconf:capability:notification:1.0");
    }

}
