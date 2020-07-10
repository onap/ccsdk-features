/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk feature sdnr wt
 *  ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.ccsdk.features.sdnr.wt.devicemanager.housekeeping;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.util.NetworkElementConnectionEntitiyUtil;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.InternalConnectionStatus;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.network.topology.topology.topology.types.TopologyNetconf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ConnectionLogStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.NetworkElementConnectionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.NetworkElementConnectionEntity;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionStatusHousekeepingService implements ClusterSingletonService, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectionStatusHousekeepingService.class);

    private static final long INTERVAL_SECONDS = 30;
    private static final InstanceIdentifier<Topology> NETCONF_TOPO_IID =
            InstanceIdentifier.create(NetworkTopology.class).child(Topology.class,
                    new TopologyKey(new TopologyId(TopologyNetconf.QNAME.getLocalName())));
    private static final ServiceGroupIdentifier IDENT =
            ServiceGroupIdentifier.create("ConnectionStatusHousekeepingService");

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
    private final DataBroker dataBroker;
    private final DataProvider dataProvider;
    private boolean isMaster;
    private Future<?> taskReference;

    private final Runnable runner = () -> doClean();

    public ConnectionStatusHousekeepingService(DataBroker dataBroker, DataProvider dataProvider) {
        this.dataBroker = dataBroker;
        this.dataProvider = dataProvider;
        this.start();
    }

    public void start() {
        if (taskReference != null) {
            taskReference.cancel(false);
        }
        if (!isMaster) {
            LOG.info("do not start. not the master node");
            return;
        }
        LOG.info("starting scheduler with interval {}", INTERVAL_SECONDS);
        this.taskReference =
                this.scheduler.scheduleAtFixedRate(runner, INTERVAL_SECONDS, INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private void doClean() {
        LOG.debug("start housekeeping");
        // get all devices from networkelement-connection index
        try {
            List<NetworkElementConnectionEntity> list = this.dataProvider.getNetworkElementConnections();

            ConnectionLogStatus dbStatus;
            ConnectionLogStatus mdsalStatus;
            String nodeId;
            if (list == null || list.size() <= 0) {
                LOG.trace("no items in list.");
            } else {
                //check all db entries and sync connection status
                for (NetworkElementConnectionEntity item : list) {

                    // compare with MD-SAL
                    nodeId = item.getNodeId();
                    LOG.trace("check status of {}", nodeId);
                    dbStatus = item.getStatus();
                    mdsalStatus = this.getMDSalConnectionStatus(nodeId);
                    if (mdsalStatus == null) {
                        LOG.trace("unable to get connection status. jump over");
                        continue;
                    }
                    // if different then update db
                    if (dbStatus != mdsalStatus) {
                        LOG.trace("status is inconsistent db={}, mdsal={}. updating db", dbStatus, mdsalStatus);
                        if ((item.isIsRequired() == null || item.isIsRequired() == false)
                                && mdsalStatus == ConnectionLogStatus.Disconnected) {
                            LOG.info("removing entry for node {} ({}) from database due missing MD-SAL entry",
                                    item.getNodeId(), mdsalStatus);
                            this.dataProvider.removeNetworkConnection(nodeId);
                        } else {
                            this.dataProvider.updateNetworkConnectionDeviceType(
                                    new NetworkElementConnectionBuilder().setStatus(mdsalStatus).build(), nodeId);
                        }
                    } else {
                        LOG.trace("no difference");
                    }

                }
            }
            //check all md-sal entries and add non-existing to db
            //			List<Node> mdsalNodes = this.getMDSalNodes();
            //			NodeId nid;
            //			for (Node mdsalNode : mdsalNodes) {
            //				nid = mdsalNode.getNodeId();
            //				if (nid == null) {
            //					continue;
            //				}
            //				nodeId = nid.getValue();
            //				if (nodeId == null) {
            //					continue;
            //				}
            //				if (contains(list, nodeId)) {
            //					LOG.debug("found mountpoint for {} without db entry. creating.",nodeId);
            //					this.dataProvider.updateNetworkConnection22(NetworkElementConnectionEntitiyUtil
            //							.getNetworkConnection(nodeId, mdsalNode.augmentation(NetconfNode.class)), nodeId);
            //				}
            //			}

        } catch (Exception e) {
            LOG.warn("problem executing housekeeping task: {}", e);
        }
        LOG.debug("finish housekeeping");
    }

    /**
     * @param list
     * @param nodeId
     * @return
     */
    //	private boolean contains(List<NetworkElementConnectionEntity> list, @NonNull String nodeId) {
    //		if(list==null || list.size()<=0) {
    //			return false;
    //		}
    //		for(NetworkElementConnectionEntity item:list) {
    //			if(item!=null && nodeId.equals(item.getNodeId())) {
    //				return true;
    //			}
    //		}
    //		return false;
    //	}
    //
    //	private List<Node> getMDSalNodes(){
    //    	ReadTransaction trans = this.dataBroker.newReadOnlyTransaction();
    //        FluentFuture<Optional<Topology>> optionalTopology =trans.read(LogicalDatastoreType.OPERATIONAL, NETCONF_TOPO_IID);
    //        List<Node> nodes = new ArrayList<>();
    //        try {
    //        	Topology topo = optionalTopology.get(20, TimeUnit.SECONDS).get();
    //        	List<Node> topoNodes=topo.getNode();
    //        	if(topoNodes!=null){
    //        		nodes.addAll(topoNodes);
    //        	}
    //        }
    //        catch(Exception e) {
    //        	LOG.warn("unable to read netconf topology for housekeeping: {}",e);
    //        }
    //        return nodes;
    //    }
    private ConnectionLogStatus getMDSalConnectionStatus(String nodeId) {

        @SuppressWarnings("null")
        @NonNull
        InstanceIdentifier<Node> instanceIdentifier =
                NETCONF_TOPO_IID.child(Node.class, new NodeKey(new NodeId(nodeId)));
        ReadTransaction trans = this.dataBroker.newReadOnlyTransaction();
        FluentFuture<Optional<Node>> optionalNode = trans.read(LogicalDatastoreType.OPERATIONAL, instanceIdentifier);
        try {
            Node node = optionalNode.get(5, TimeUnit.SECONDS).get();
            LOG.debug("node is {}", node);
            NetconfNode nNode = node.augmentation(NetconfNode.class);
            LOG.debug("nnode is {}", nNode);
            if (nNode != null) {
                return InternalConnectionStatus.statusFromNodeStatus(nNode.getConnectionStatus());
            }
        } catch (NoSuchElementException e) {
            return ConnectionLogStatus.Disconnected;
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            LOG.warn("unable to get node info: {}", e);
        } finally {
            trans.close();
        }

        return null;
    }

    @Override
    public void close() throws Exception {
        if (taskReference != null) {
            taskReference.cancel(false);
        }
        this.scheduler.shutdown();
    }

    @SuppressWarnings("null")
    @Override
    public @NonNull ServiceGroupIdentifier getIdentifier() {
        return IDENT;
    }

    @Override
    public void instantiateServiceInstance() {
        LOG.info("We take Leadership");
        this.isMaster = true;
        this.start();
    }

    @Override
    public ListenableFuture<? extends Object> closeServiceInstance() {
        LOG.info("We lost Leadership");
        this.isMaster = false;
        this.start();
        return Futures.immediateFuture(null);
    }
}
