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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.filechange.IConfigChangedListener;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.NetconfTimeStamp;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.NetconfTimeStampImpl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.InternalConnectionStatus;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.singleton.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.api.ServiceGroupIdentifier;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev240911.NetconfNodeAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev240911.netconf.node.augment.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev240911.network.topology.topology.topology.types.TopologyNetconf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ConnectionLogStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EventlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementConnectionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementConnectionEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SourceType;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionStatusHousekeepingService
        implements ClusterSingletonService, AutoCloseable, IConfigChangedListener {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectionStatusHousekeepingService.class);

    private static final long INTERVAL_SECONDS = 30;
    private static final InstanceIdentifier<Topology> NETCONF_TOPO_IID =
            InstanceIdentifier.create(NetworkTopology.class).child(Topology.class,
                    new TopologyKey(new TopologyId(TopologyNetconf.QNAME.getLocalName())));
    private static final ServiceGroupIdentifier IDENT = new ServiceGroupIdentifier("ConnectionStatusHousekeepingService");

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
    private final DataBroker dataBroker;
    private final DataProvider dataProvider;
    private final Runnable runner = () -> doClean();
    private final HouseKeepingConfig config;
    private final ConfigurationFileRepresentation cfg;

    private final Registration cssRegistration2;
    private boolean isMaster;
    private Future<?> taskReference;
    private int eventNumber;
    private volatile boolean enabled;

    public ConnectionStatusHousekeepingService(ConfigurationFileRepresentation cfg,
            ClusterSingletonServiceProvider clusterSingletonServiceProvider, DataBroker dataBroker,
            DataProvider dataProvider) {
        this.config = new HouseKeepingConfig(cfg);
        this.cfg = cfg;
        cfg.registerConfigChangedListener(this);
        this.dataBroker = dataBroker;
        this.dataProvider = dataProvider;
        this.eventNumber = 0;

        setEnabled(this.config.isEnabled());
        this.start();

        this.cssRegistration2 = clusterSingletonServiceProvider.registerClusterSingletonService(this);
    }

    private void setEnabled(boolean pEnabled) {
        LOG.info("ConnectionStatusHousekeepingService status change from {} to {}", enabled, pEnabled);
        this.enabled = pEnabled;
    }

    private boolean isEnabled() {
        return this.enabled;
    }

    private void start() {
        if (taskReference != null) {
            taskReference.cancel(false);
        }
        if (!isMaster) {
            LOG.info("Do not start. not the master node");
        } else {
            LOG.info("Starting scheduler with interval {}", INTERVAL_SECONDS);
            this.taskReference =
                    this.scheduler.scheduleAtFixedRate(runner, INTERVAL_SECONDS, INTERVAL_SECONDS, TimeUnit.SECONDS);
        }
    }

    private void doClean() {
        if (!isEnabled()) {
            LOG.debug("service is disabled by config");
            return;
        }
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
                NetconfTimeStamp ts = NetconfTimeStampImpl.getConverter();
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
                        this.dataProvider.writeEventLog(new EventlogBuilder().setNodeId("SDN-Controller")
                                .setTimestamp(new DateAndTime(ts.getTimeStamp())).setObjectId(item.getNodeId())
                                .setAttributeName("status").setNewValue(String.valueOf(mdsalStatus))
                                .setCounter(popEvntNumber()).setSourceType(SourceType.Controller).build());
                        if ((item.requireIsRequired() == null || item.requireIsRequired() == false)
                                && mdsalStatus == ConnectionLogStatus.Disconnected) {
                            LOG.info("removing entry for node {} ({}) from database due missing MD-SAL entry",
                                    item.getNodeId(), mdsalStatus);
                            this.dataProvider.removeNetworkConnection(nodeId);
                        } else {
                            NetworkElementConnectionBuilder ne =
                                    new NetworkElementConnectionBuilder().setStatus(mdsalStatus);

                            this.dataProvider.updateNetworkConnection22(ne.build(), nodeId);
                        }
                    } else {
                        LOG.trace("no difference");
                    }
                }
            }

        } catch (Exception e) {
            LOG.warn("problem executing housekeeping task: {}", e);
        }
        LOG.debug("finish housekeeping");
    }

    private Integer popEvntNumber() {
        return eventNumber++;
    }

    private ConnectionLogStatus getMDSalConnectionStatus(String nodeId) {

        @NonNull
        InstanceIdentifier<Node> instanceIdentifier =
                NETCONF_TOPO_IID.child(Node.class, new NodeKey(new NodeId(nodeId)));
        //Implicit close of try with resource is not handled correctly by underlying opendaylight NETCONF service
        ReadTransaction trans = this.dataBroker.newReadOnlyTransaction();
        try {
            FluentFuture<Optional<Node>> optionalNode =
                    trans.read(LogicalDatastoreType.OPERATIONAL, instanceIdentifier);
            Optional<Node> nodeOpt = optionalNode.get();
            if (nodeOpt.isEmpty()) {
                LOG.warn("unable to get node info");
                return null;
            }
            Node node = nodeOpt.get();
            LOG.debug("node is {}", node);
            final var aug = node.augmentation(NetconfNodeAugment.class);
            NetconfNode nNode = aug!=null?aug.getNetconfNode():null;
            LOG.debug("nnode is {}", nNode);
            if (nNode != null) {
                return InternalConnectionStatus.statusFromNodeStatus(nNode.getConnectionStatus());
            }
        } catch (NoSuchElementException e) {
            return ConnectionLogStatus.Disconnected;
        } catch (InterruptedException e) {
            LOG.warn("InterruptedException unable to get node info: ", e);
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {// | TimeoutException e) {
            LOG.warn("ExecutionException unable to get node info: ", e);
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
        if (this.cfg != null) {
            this.cfg.unregisterConfigChangedListener(this);
        }
        this.scheduler.shutdown();
        if (this.cssRegistration2 != null)
            this.cssRegistration2.close();
    }

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

    @Override
    public void onConfigChanged() {

        setEnabled(this.config.isEnabled());
    }
}
