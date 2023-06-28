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
package org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.access;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.NetconfNodeStateServiceImpl;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.access.dom.DomContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev221225.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev221225.network.topology.topology.topology.types.TopologyNetconf;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetconfAccessorManager {

    private static final Logger LOG = LoggerFactory.getLogger(NetconfNodeStateServiceImpl.class);

    private static final @NonNull InstanceIdentifier<Topology> NETCONF_TOPO_IID =
            InstanceIdentifier.create(NetworkTopology.class).child(Topology.class,
                    new TopologyKey(new TopologyId(TopologyNetconf.QNAME.getLocalName())));

    private final ConcurrentHashMap<NodeId, NetconfAccessor> accessorList;
    private final NetconfCommunicatorManager netconfCommunicatorManager;
    private final DomContext domContext;
    private final NetconfNodeStateServiceImpl netconfNodeStateService;

    public NetconfAccessorManager(NetconfCommunicatorManager netconfCommunicatorManager, DomContext domContext, NetconfNodeStateServiceImpl netconfNodeStateService) {
        this.netconfCommunicatorManager = Objects.requireNonNull(netconfCommunicatorManager);
        this.domContext = Objects.requireNonNull(domContext);
        this.accessorList = new ConcurrentHashMap<>();
        this.netconfNodeStateService = Objects.requireNonNull(netconfNodeStateService);
    }

    public NetconfAccessor getAccessor(NodeId nNodeId, NetconfNode netconfNode) {
        NetconfAccessor res = new NetconfAccessorImpl(nNodeId, netconfNode, netconfCommunicatorManager, domContext, netconfNodeStateService);
        NetconfAccessor previouse = accessorList.putIfAbsent(nNodeId, res);
        if (Objects.nonNull(previouse)) {
            LOG.warn("Accessor with name already available. Don't add {}", nNodeId);
        }
        return res;
    }

    public boolean containes(NodeId nNodeId) {
        return accessorList.containsKey(nNodeId);
    }

    public void removeAccessor(NodeId nNodeId) {
        NetconfAccessor previouse = accessorList.remove(nNodeId);
        if (Objects.nonNull(previouse)) {
            LOG.warn("Accessor with name was not available during remove {}", nNodeId);
        }

    }



}
