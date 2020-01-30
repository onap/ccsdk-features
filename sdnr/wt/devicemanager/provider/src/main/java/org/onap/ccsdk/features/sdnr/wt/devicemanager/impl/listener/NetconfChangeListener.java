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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.listener;

import java.util.Collection;

import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.NetconfNodeService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.NetconfNodeService.Action;
import org.opendaylight.mdsal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.network.topology.topology.topology.types.TopologyNetconf;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// 07.09.18 Switched to DataTreeChangeListener from ClusteredDataTreeChangeListener -> DM Service is
// running at all nodes
// This is not correct
public class NetconfChangeListener implements ClusteredDataTreeChangeListener<Node>, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(NetconfChangeListener.class);

    private static final InstanceIdentifier<Node> NETCONF_NODE_TOPO_IID =
            InstanceIdentifier.create(NetworkTopology.class)
                    .child(Topology.class, new TopologyKey(new TopologyId(TopologyNetconf.QNAME.getLocalName())))
                    .child(Node.class);
    // Name of ODL controller NETCONF instance
    private static final NodeId CONTROLLER = new NodeId("controller-config");

    private final NetconfNodeService deviceManagerService;
    private final DataBroker dataBroker;
    private ListenerRegistration<NetconfChangeListener> dlcReg;

    public NetconfChangeListener(NetconfNodeService deviceManagerService, DataBroker dataBroker) {
        this.deviceManagerService = deviceManagerService;
        this.dataBroker = dataBroker;
    }

    public void register() {
        DataTreeIdentifier<Node> treeId = DataTreeIdentifier.create(LogicalDatastoreType.OPERATIONAL, NETCONF_NODE_TOPO_IID);

        dlcReg = dataBroker.registerDataTreeChangeListener(treeId, this);
    }

    @Override
    public void close() {
        if (dlcReg != null) {
            dlcReg.close();
        }
    }
    /**
     * Listener function to select the right node from DataObjectModification
     */
    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<Node>> changes) {
        LOG.debug("OnDataChange, TreeChange, changes:{}", changes.size());

        for (final DataTreeModification<Node> change : changes) {
            final DataObjectModification<Node> root = change.getRootNode();
            final ModificationType modificationType = root.getModificationType();
            if (LOG.isTraceEnabled()) {
                LOG.trace("Handle this modificationType:{} path:{} root:{}", modificationType, change.getRootPath(),
                        root);
            }
            switch (modificationType) {
                case SUBTREE_MODIFIED:
                    // Change of subtree information
                    // update(change); OLD
                    doProcessing(Action.UPDATE, root.getDataAfter());
                    break;
                case WRITE:
                    // Create or modify top level node
                    // Treat an overwrite as an update
                    boolean update = root.getDataBefore() != null;
                    if (update) {
                        // update(change);
                        doProcessing(Action.UPDATE, root.getDataAfter());
                    } else {
                        // add(change);
                        doProcessing(Action.CREATE, root.getDataAfter());
                    }
                    break;
                case DELETE:
                    // Node removed
                    // remove(change);
                    doProcessing(Action.REMOVE, root.getDataBefore());
                    break;
            }
        }
    }

    /*
     * ----------------------------------------------------------------
     */

    /**
     * Process event and forward to clients if Node is a NetconfNode
     * @param action
     * @param node Basis node
     */
    private void doProcessing(Action action, Node node) {

        NodeId nodeId = null;
        NetconfNode nnode = null;

        try {
            if (node != null) {
                nodeId = node.key().getNodeId(); //Never null
                nnode = node.augmentation(NetconfNode.class);
            }

            if (node == null || nnode == null) {
                LOG.warn("Unexpected node {}, netconf node {} id {}", node, nnode, nodeId);
            } else {
                // Do not forward any controller related events to devicemanager
                if (nodeId.equals(CONTROLLER)) {
                    LOG.debug("Stop processing for [{}]", nodeId);
                } else {
                      // Action forwarded to devicehandler
                       deviceManagerService.netconfNodeChangeHandler(action, nodeId, nnode);
                }
            }
        } catch (NullPointerException e) {
            LOG.warn("Unexpected null .. stop processing.", e);
        }
    }

}
