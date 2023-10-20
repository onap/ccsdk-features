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
package org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.access;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.DomContext;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.access.binding.NetconfBindingAccessorImpl;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.access.dom.NetconfDomAccessorImpl;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.MountPoint;
import org.opendaylight.mdsal.binding.api.MountPointService;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMMountPoint;
import org.opendaylight.mdsal.dom.api.DOMMountPointService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev221225.network.topology.topology.topology.types.TopologyNetconf;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetconfCommunicatorManager {

    private static final Logger LOG = LoggerFactory.getLogger(NetconfCommunicatorManager.class);

    private static final @NonNull InstanceIdentifier<Topology> NETCONF_TOPO_IID =
            InstanceIdentifier.create(NetworkTopology.class).child(Topology.class,
                    new TopologyKey(new TopologyId(TopologyNetconf.QNAME.getLocalName())));

    private static final @NonNull InstanceIdentifier<Node> NETCONF_NODE_TOPO_IID =
            InstanceIdentifier.create(NetworkTopology.class)
                    .child(Topology.class, new TopologyKey(new TopologyId(TopologyNetconf.QNAME.getLocalName())))
                    .child(Node.class);

    private final MountPointService mountPointService;
    private final DOMMountPointService domMountPointService;
    private final DomContext domContext;

    public NetconfCommunicatorManager(MountPointService mountPointService, DOMMountPointService domMountPointService,
            DomContext domContext) {
        super();
        this.mountPointService = mountPointService;
        this.domMountPointService = domMountPointService;
        this.domContext = domContext;
    }

    public Optional<NetconfBindingAccessor> getNetconfBindingAccessor(NetconfAccessorImpl accessor) {
        String mountPointNodeName = accessor.getNodeId().getValue();
        InstanceIdentifier<Node> instanceIdentifier =
                NETCONF_TOPO_IID.child(Node.class, new NodeKey(new NodeId(mountPointNodeName)));

        final Optional<MountPoint> optionalMountPoint = mountPointService.getMountPoint(instanceIdentifier);
        if (!optionalMountPoint.isPresent()) {
            LOG.warn("No mountpoint available for Netconf device :: Name : {} ", mountPointNodeName);
        } else {
            final MountPoint mountPoint = optionalMountPoint.get();

            LOG.debug("Mountpoint with id: {} class:{}", mountPoint.getIdentifier(), mountPoint.getClass().getName());

            Optional<DataBroker> optionalNetconfNodeDatabroker = mountPoint.getService(DataBroker.class);

            if (!optionalNetconfNodeDatabroker.isPresent()) {
                LOG.debug("Slave mountpoint {} without databroker", mountPointNodeName);
            } else {
                LOG.debug("Master mountpoint {}", mountPointNodeName);
                return Optional.of(
                        new NetconfBindingAccessorImpl(accessor, optionalNetconfNodeDatabroker.get(), mountPoint));
            }
        }
        return Optional.empty();
    }

    public Optional<NetconfDomAccessor> getNetconfDomAccessor(NetconfAccessorImpl accessor) {

        final YangInstanceIdentifier mountpointPath = YangInstanceIdentifier.builder().node(NetworkTopology.QNAME)
                .node(Topology.QNAME)
                .nodeWithKey(Topology.QNAME, QName.create(Topology.QNAME, "topology-id").intern(), "topology-netconf")
                .node(Node.QNAME)
                .nodeWithKey(Node.QNAME, QName.create(Node.QNAME, "node-id").intern(), accessor.getNodeId().getValue())
                .build();
        final Optional<DOMMountPoint> oMountPoint = domMountPointService.getMountPoint(mountpointPath);
        if (oMountPoint.isEmpty()) {
            return Optional.empty();
        }

        final Optional<DOMDataBroker> domDataBroker = oMountPoint.get().getService(DOMDataBroker.class);
        if (domDataBroker.isPresent()) {
            return Optional
                    .of(new NetconfDomAccessorImpl(accessor, domDataBroker.get(), oMountPoint.get(), domContext));
        }
        return Optional.empty();
    }

}
