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
package org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import edu.umd.cs.findbugs.annotations.Nullable;

import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.filechange.IConfigChangedListener;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.IEntityDataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.StatusChangedHandler.StatusKey;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNodeConnectListener;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNodeStateListener;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNodeStateService;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.VesNotificationListener;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.conf.NetconfStateConfig;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.conf.odlAkka.AkkaConfig;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.conf.odlAkka.ClusterConfig;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.conf.odlGeo.GeoConfig;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.rpc.NetconfnodeStateServiceRpcApiImpl;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.rpc.RpcApigetStateCallback;
import org.opendaylight.mdsal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.api.MountPoint;
import org.opendaylight.mdsal.binding.api.MountPointService;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceRegistration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNodeConnectionStatus.ConnectionStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.netconf.node.connection.status.ClusteredConnectionStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.network.topology.topology.topology.types.TopologyNetconf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netconfnode.state.rev191011.GetStatusInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netconfnode.state.rev191011.GetStatusOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetconfNodeStateServiceImpl
        implements NetconfNodeStateService, RpcApigetStateCallback, AutoCloseable, IConfigChangedListener {

    private static final Logger LOG = LoggerFactory.getLogger(NetconfNodeStateServiceImpl.class);
    private static final String APPLICATION_NAME = "NetconfNodeStateService";
    @SuppressWarnings("unused")
    private static final String CONFIGURATIONFILE = "etc/netconfnode-status-service.properties";

    @SuppressWarnings("null")
    private static final @NonNull InstanceIdentifier<Topology> NETCONF_TOPO_IID =
            InstanceIdentifier.create(NetworkTopology.class).child(Topology.class,
                    new TopologyKey(new TopologyId(TopologyNetconf.QNAME.getLocalName())));

    @SuppressWarnings("null")
    private static final @NonNull InstanceIdentifier<Node> NETCONF_NODE_TOPO_IID =
            InstanceIdentifier.create(NetworkTopology.class)
                    .child(Topology.class, new TopologyKey(new TopologyId(TopologyNetconf.QNAME.getLocalName())))
                    .child(Node.class);

    private static final DataTreeIdentifier<Node> NETCONF_NODE_TOPO_TREE_ID =
            DataTreeIdentifier.create(LogicalDatastoreType.OPERATIONAL, NETCONF_NODE_TOPO_IID);

    // Name of ODL controller NETCONF instance
    private static final NodeId CONTROLLER = new NodeId("controller-config");

    // -- OSGi services, provided
    private DataBroker dataBroker;
    private MountPointService mountPointService;
    private RpcProviderService rpcProviderRegistry;
    private IEntityDataProvider iEntityDataProvider;
    @SuppressWarnings("unused")
    private NotificationPublishService notificationPublishService;
    @SuppressWarnings("unused")
    private ClusterSingletonServiceProvider clusterSingletonServiceProvider;

    // -- Parameter
    private ListenerRegistration<L1> listenerL1;
    private ListenerRegistration<L2> listenerL2;
    @SuppressWarnings("unused")
    private ClusterSingletonServiceRegistration cssRegistration;

    private NetconfnodeStateServiceRpcApiImpl rpcApiService;

    /** Indication if init() function called and fully executed **/
    private Boolean initializationSuccessful;

    /** Manager accessor objects for connection **/
    private final NetconfAccessorManager accessorManager;

    /** List of all registered listeners **/
    private final List<NetconfNodeConnectListener> netconfNodeConnectListenerList;

    /** List of all registered listeners **/
    private final List<NetconfNodeStateListener> netconfNodeStateListenerList;

    /** List of all registered listeners **/
    private final List<VesNotificationListener> vesNotificationListenerList;

    /** Indicates if running in cluster configuration **/
    private boolean isCluster;

    /** Indicates the name of the cluster **/
    private String clusterName;

    /** nodeId to threadPool (size=1) for datatreechange handling) **/
    private final Map<String, ExecutorService> handlingPool;

    private boolean handleDataTreeAsync;

    private ConfigurationFileRepresentation configFileRepresentation;
    private NetconfStateConfig config;

    /** Blueprint **/
    public NetconfNodeStateServiceImpl() {
        LOG.info("Creating provider for {}", APPLICATION_NAME);

        this.dataBroker = null;
        this.mountPointService = null;
        this.rpcProviderRegistry = null;
        this.notificationPublishService = null;
        this.clusterSingletonServiceProvider = null;

        this.listenerL1 = null;
        this.listenerL2 = null;
        this.initializationSuccessful = false;
        this.netconfNodeConnectListenerList = new CopyOnWriteArrayList<>();
        this.netconfNodeStateListenerList = new CopyOnWriteArrayList<>();
        this.vesNotificationListenerList = new CopyOnWriteArrayList<>();
        this.accessorManager = new NetconfAccessorManager();
        this.handlingPool = new HashMap<>();

    }

    public void setDataBroker(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public void setRpcProviderRegistry(RpcProviderService rpcProviderRegistry) {
        this.rpcProviderRegistry = rpcProviderRegistry;
    }

    public void setNotificationPublishService(NotificationPublishService notificationPublishService) {
        this.notificationPublishService = notificationPublishService;
    }

    public void setMountPointService(MountPointService mountPointService) {
        this.mountPointService = mountPointService;
    }

    public void setClusterSingletonService(ClusterSingletonServiceProvider clusterSingletonService) {
        this.clusterSingletonServiceProvider = clusterSingletonService;
    }

    public void setEntityDataProvider(IEntityDataProvider iEntityDataProvider) {
        this.iEntityDataProvider = iEntityDataProvider;
    }

    /** Blueprint initialization **/
    public void init() {

        LOG.info("Session Initiated start {}", APPLICATION_NAME);

        // Start RPC Service
        this.rpcApiService = new NetconfnodeStateServiceRpcApiImpl(rpcProviderRegistry, vesNotificationListenerList);
        // Get configuration
        this.configFileRepresentation = new ConfigurationFileRepresentation(CONFIGURATIONFILE);
        this.config = new NetconfStateConfig(this.configFileRepresentation);
        this.handleDataTreeAsync = this.config.handleAsync();
        this.configFileRepresentation.registerConfigChangedListener(this);

        // Akka setup
        AkkaConfig akkaConfig = getAkkaConfig();
        this.isCluster = akkaConfig == null ? false : akkaConfig.isCluster();
        this.clusterName = akkaConfig == null ? "" : akkaConfig.getClusterConfig().getClusterSeedNodeName("abc");

        // Provide status information
        ClusterConfig cc = akkaConfig == null ? null : akkaConfig.getClusterConfig();
        this.iEntityDataProvider.setStatus(StatusKey.CLUSTER_SIZE,
                cc == null ? "1" : String.format("%d", cc.getClusterSize()));

        // RPC Service for specific services
        this.rpcApiService.setStatusCallback(this);

        LOG.debug("start NetconfSubscriptionManager Service");
        //this.netconfChangeListener = new NetconfChangeListener(this, dataBroker);
        //this.netconfChangeListener.register();
        //DataTreeIdentifier<Node> treeId = new DataTreeIdentifier<>(LogicalDatastoreType.OPERATIONAL, NETCONF_NODE_TOPO_IID);

        listenerL1 = dataBroker.registerDataTreeChangeListener(NETCONF_NODE_TOPO_TREE_ID, new L1());
        listenerL2 = dataBroker.registerDataTreeChangeListener(NETCONF_NODE_TOPO_TREE_ID, new L2());

        this.initializationSuccessful = true;

        LOG.info("Session Initiated end. Initialization done {}", initializationSuccessful);

    }

    /** Blueprint destroy-method method */
    public void destroy() {
        close();
    }

    /**
     * Getter
     * 
     * @return NetconfnodeStateServiceRpcApiImpl
     */
    public NetconfnodeStateServiceRpcApiImpl getNetconfnodeStateServiceRpcApiImpl() {
        return rpcApiService;
    }

    @Override
    public GetStatusOutputBuilder getStatus(GetStatusInput input) {
        return new GetStatusOutputBuilder();
    }

    @Override
    public <L extends NetconfNodeConnectListener> @NonNull ListenerRegistration<L> registerNetconfNodeConnectListener(
            final @NonNull L netconfNodeConnectListener) {
        LOG.info("Register connect listener {}", netconfNodeConnectListener.getClass().getName());
        netconfNodeConnectListenerList.add(netconfNodeConnectListener);

        return new ListenerRegistration<L>() {
            @Override
            public @NonNull L getInstance() {
                return netconfNodeConnectListener;
            }

            @Override
            public void close() {
                LOG.info("Remove connect listener {}", netconfNodeConnectListener);
                netconfNodeConnectListenerList.remove(netconfNodeConnectListener);
            }
        };
    }

    @Override
    public <L extends NetconfNodeStateListener> @NonNull ListenerRegistration<L> registerNetconfNodeStateListener(
            @NonNull L netconfNodeStateListener) {
        LOG.info("Register state listener {}", netconfNodeStateListener.getClass().getName());
        netconfNodeStateListenerList.add(netconfNodeStateListener);

        return new ListenerRegistration<L>() {
            @Override
            public @NonNull L getInstance() {
                return netconfNodeStateListener;
            }

            @Override
            public void close() {
                LOG.info("Remove state listener {}", netconfNodeStateListener);
                netconfNodeStateListenerList.remove(netconfNodeStateListener);
            }
        };
    }

    @Override
    public <L extends VesNotificationListener> @NonNull ListenerRegistration<L> registerVesNotifications(
            @NonNull L vesNotificationListener) {
        LOG.info("Register Ves notification listener {}", vesNotificationListener.getClass().getName());
        vesNotificationListenerList.add(vesNotificationListener);

        return new ListenerRegistration<L>() {
            @Override
            public @NonNull L getInstance() {
                return vesNotificationListener;
            }

            @Override
            public void close() {
                LOG.info("Remove Ves notification listener {}", vesNotificationListener);
                vesNotificationListenerList.remove(vesNotificationListener);
            }
        };
    }

    @Override
    public void close() {
        LOG.info("Closing start ...");
        try {
            close(rpcApiService, listenerL1, listenerL2);
        } catch (Exception e) {
            LOG.debug("Closing", e);
        }
        LOG.info("Closing done");
    }

    /**
     * Used to close all Services, that should support AutoCloseable Pattern
     *
     * @param toClose
     * @throws Exception
     */
    private void close(AutoCloseable... toCloseList) throws Exception {
        for (AutoCloseable element : toCloseList) {
            if (element != null) {
                element.close();
            }
        }
        this.configFileRepresentation.unregisterConfigChangedListener(this);
    }

    /**
     * Indication if init() of this bundle successfully done.
     * 
     * @return true if init() was successful. False if not done or not successful.
     */
    public boolean isInitializationSuccessful() {
        return this.initializationSuccessful;
    }

    /*-------------------------------------------------------------------------------------------
     * Functions for interface DeviceManagerService
     */

    /**
     * For each mounted device a mountpoint is created and this listener is called. Mountpoint was created or existing.
     * Managed device is now fully connected to node/mountpoint.
     * 
     * @param nNodeId id of the mountpoint
     * @param netconfNode mountpoint contents
     */
    private void enterConnectedState(NodeId nNodeId, NetconfNode netconfNode) {

        String mountPointNodeName = nNodeId.getValue();
        LOG.info("Access connected state for mountpoint {}", mountPointNodeName);

        boolean preConditionMissing = false;
        if (mountPointService == null) {
            preConditionMissing = true;
            LOG.warn("No mountservice available.");
        }
        if (!initializationSuccessful) {
            preConditionMissing = true;
            LOG.warn("Devicemanager initialization still pending.");
        }
        if (preConditionMissing) {
            return;
        }

        boolean isNetconfNodeMaster = isNetconfNodeMaster(netconfNode);
        LOG.info("isNetconfNodeMaster indication {} for mountpoint {}", isNetconfNodeMaster, mountPointNodeName);
        if (isNetconfNodeMaster) {

            InstanceIdentifier<Node> instanceIdentifier =
                    NETCONF_TOPO_IID.child(Node.class, new NodeKey(new NodeId(mountPointNodeName)));

            Optional<MountPoint> optionalMountPoint = mountPointService.getMountPoint(instanceIdentifier);
            if (!optionalMountPoint.isPresent()) {
                LOG.warn("No mountpoint available for Netconf device :: Name : {} ", mountPointNodeName);
            } else {
                // Mountpoint is present for sure
                MountPoint mountPoint = optionalMountPoint.get();
                // BindingDOMDataBrokerAdapter.BUILDER_FACTORY;
                LOG.info("Mountpoint with id: {} class:{}", mountPoint.getIdentifier(),
                        mountPoint.getClass().getName());

                Optional<DataBroker> optionalNetconfNodeDatabroker = mountPoint.getService(DataBroker.class);

                if (!optionalNetconfNodeDatabroker.isPresent()) {
                    LOG.info("Slave mountpoint {} without databroker", mountPointNodeName);
                } else {
                    LOG.info("Master mountpoint {}", mountPointNodeName);
                    DataBroker netconfNodeDataBroker = optionalNetconfNodeDatabroker.get();
                    NetconfAccessor acessor =
                            accessorManager.getAccessor(nNodeId, netconfNode, netconfNodeDataBroker, mountPoint);
                    /*
                     * --> Call Listers for onConnect() Indication
                       for (all)
                     */
                    netconfNodeConnectListenerList.forEach(item -> {
                        try {
                            item.onEnterConnected(acessor);
                        } catch (Exception e) {
                            LOG.info("Exception during onEnterConnected listener call", e);
                        }
                    });

                    LOG.info("Connect indication forwarded for {}", mountPointNodeName);
                }
            }
        }
    }

    /**
     * Leave the connected status to a non connected or removed status for master mountpoint
     * 
     * @param action that occurred
     * @param nNodeId id of the mountpoint
     * @param netconfNode mountpoint contents or not available on remove
     */
    private void leaveConnectedState(NodeId nNodeId, Optional<NetconfNode> optionalNetconfNode) {
        String mountPointNodeName = nNodeId.getValue();
        LOG.info("leaveConnectedState id {}", mountPointNodeName);

        if (this.accessorManager.containes(nNodeId)) {
            netconfNodeConnectListenerList.forEach(item -> {
                try {
                    if (item != null) {
                        item.onLeaveConnected(nNodeId, optionalNetconfNode);
                    } else {
                        LOG.warn("Unexpeced null item during onleave");
                    }
                } catch (Exception e) {
                    LOG.info("Exception during onLeaveConnected listener call", e);
                }
            });
            LOG.info("Remove Master mountpoint {}", mountPointNodeName);
            this.accessorManager.removeAccessor(nNodeId);
        } else {
            LOG.info("Master mountpoint already removed {}", mountPointNodeName);
        }
    }

    // ---- onDataTreeChangedHandler

    private void handleDataTreeChange(DataObjectModification<Node> root, NodeId nodeId,
            ModificationType modificationTyp) {
        // Move status into boolean flags for
        boolean connectedBefore, connectedAfter, created;
        NetconfNode nNodeAfter = getNetconfNode(root.getDataAfter());
        connectedAfter = isConnected(nNodeAfter);
        if (root.getDataBefore() != null) {
            // It is an update or delete
            NetconfNode nodeBefore = getNetconfNode(root.getDataBefore());
            connectedBefore = isConnected(nodeBefore);
            created = false;
        } else {
            // It is a create
            connectedBefore = false;
            created = true;
        }
        LOG.info("L1 NETCONF id:{} t:{} created {} before:{} after:{} akkaIsCluster:{} cl stat:{}", nodeId,
                modificationTyp, created, connectedBefore, connectedAfter, isCluster,
                getClusteredConnectionStatus(nNodeAfter));
        switch (modificationTyp) {
            case SUBTREE_MODIFIED: // Create or modify sub level node
            case WRITE: // Create or modify top level node
                // Treat an overwrite as an update
                // leaveConnected state.before = connected; state.after != connected
                // enterConnected state.after == connected
                // => Here create or update by checking root.getDataBefore() != null
                boolean handled = false;
                if (created) {
                    handled = true;
                    netconfNodeStateListenerList.forEach(item -> {
                        try {
                            item.onCreated(nodeId, nNodeAfter);
                        } catch (Exception e) {
                            LOG.info("Exception during onCreated listener call", e);
                        }
                    });
                }
                if (!connectedBefore && connectedAfter) {
                    handled = true;
                    enterConnectedState(nodeId, nNodeAfter);
                }
                if (connectedBefore && !connectedAfter) {
                    handled = true;
                    leaveConnectedState(nodeId, Optional.of(nNodeAfter));
                }
                if (!handled) {
                    //Change if not handled by the messages before
                    netconfNodeStateListenerList.forEach(item -> {
                        try {
                            item.onStateChange(nodeId, nNodeAfter);
                        } catch (Exception e) {
                            LOG.info("Exception during onStateChange listener call", e);
                        }
                    });
                }
                // doProcessing(update ? Action.UPDATE : Action.CREATE, nodeId, root);
                break;
            case DELETE:
                // Node removed
                // leaveconnected state.before = connected;
                if (!connectedBefore) {
                    leaveConnectedState(nodeId, Optional.empty());
                }
                netconfNodeStateListenerList.forEach(item -> {
                    try {
                        item.onRemoved(nodeId);
                    } catch (Exception e) {
                        LOG.info("Exception during onRemoved listener call", e);
                    }
                });
                // doProcessing(Action.REMOVE, nodeId, root);
                break;
        }
    }

    private void onDataTreeChangedHandler(@NonNull Collection<DataTreeModification<Node>> changes) {
        for (final DataTreeModification<Node> change : changes) {

            final DataObjectModification<Node> root = change.getRootNode();
            if (LOG.isTraceEnabled()) {
                LOG.trace("Handle this modificationType:{} path:{} root:{}", root.getModificationType(),
                        change.getRootPath(), root);
            }

            // Catch potential nullpointer exceptions ..
            try {
                ModificationType modificationTyp = root.getModificationType();
                Node node = modificationTyp == ModificationType.DELETE ? root.getDataBefore() : root.getDataAfter();
                NodeId nodeId = node != null ? node.getNodeId() : null;
                if (nodeId == null) {
                    LOG.warn("L1 without nodeid.");
                } else {
                    if (nodeId.equals(CONTROLLER)) {
                        // Do not forward any controller related events to devicemanager
                        LOG.debug("Stop processing for [{}]", nodeId);
                    } else {
                        if (modificationTyp == null) {
                            LOG.warn("L1 empty modification type");
                        } else {
                            if (this.handleDataTreeAsync) {
                                ExecutorService executor = this.handlingPool.getOrDefault(nodeId.getValue(), null);
                                if (executor == null) {
                                    executor = Executors.newFixedThreadPool(5);
                                    this.handlingPool.put(nodeId.getValue(), executor);
                                }
                                executor.execute(new Thread() {
                                    @Override
                                    public void run() {
                                        handleDataTreeChange(root, nodeId, modificationTyp);
                                    }
                                });

                            } else {
                                handleDataTreeChange(root, nodeId, modificationTyp);
                            }
                        }
                    }
                }
            } catch (NullPointerException | IllegalStateException e) {
                LOG.info("Data not available at ", e);
            }
        } //for
        LOG.info("datatreechanged handler completed");
    }

    // ---- subclasses for listeners

    /**
     * Clustered listener function to select the right node from DataObjectModification. Called at all nodes.
     */
    private class L1 implements ClusteredDataTreeChangeListener<Node> {
        @Override
        public void onDataTreeChanged(@NonNull Collection<DataTreeModification<Node>> changes) {
            LOG.info("L1 TreeChange enter changes:{}", changes.size());
            //Debug AkkTimeout NetconfNodeStateServiceImpl.this.pool.execute(new Thread( () -> onDataTreeChangedHandler(changes)));
            onDataTreeChangedHandler(changes);
            LOG.info("L1 TreeChange leave");
        }
    }

    /**
     * Data change, called at leader/master
     */
    private class L2 implements DataTreeChangeListener<Node> {

        @Override
        public void onDataTreeChanged(@NonNull Collection<DataTreeModification<Node>> changes) {
            LOG.info("L2 TreeChange enter changes:{}", changes.size());
            // Do nothing
            LOG.info("L2 TreeChange leave");
        }
    }

    /* --- private helpers --- */
    private static @Nullable NetconfNode getNetconfNode(Node node) {
        return node != null ? node.augmentation(NetconfNode.class) : null;
    }

    private static boolean isConnected(NetconfNode nNode) {
        return nNode != null ? ConnectionStatus.Connected.equals(nNode.getConnectionStatus()) : false;
    }

    private static @Nullable ClusteredConnectionStatus getClusteredConnectionStatus(NetconfNode node) {
        return node != null ? node.getClusteredConnectionStatus() : null;
    }

    /* -- LOG related functions -- */

    /** Analyze configuration **/
    private static @Nullable AkkaConfig getAkkaConfig() {
        AkkaConfig akkaConfig;
        try {
            akkaConfig = AkkaConfig.load();
            LOG.debug("akka.conf loaded: " + akkaConfig.toString());
        } catch (Exception e1) {
            akkaConfig = null;
            LOG.warn("problem loading akka.conf: " + e1.getMessage());
        }
        if (akkaConfig != null && akkaConfig.isCluster()) {
            LOG.info("cluster mode detected");
            if (GeoConfig.fileExists()) {
                try {
                    LOG.debug("try to load geoconfig");
                    GeoConfig.load();
                } catch (Exception err) {
                    LOG.warn("problem loading geoconfig: " + err.getMessage());
                }
            } else {
                LOG.debug("no geoconfig file found");
            }
        } else {
            LOG.info("single node mode detected");
        }
        return akkaConfig;
    }

    private boolean isNetconfNodeMaster(NetconfNode nNode) {
        if (this.isCluster) {
            LOG.debug("check if me is responsible for node");
            ClusteredConnectionStatus ccs = nNode.getClusteredConnectionStatus();
            @SuppressWarnings("null")
            @NonNull
            String masterNodeName =
                    ccs == null || ccs.getNetconfMasterNode() == null ? "null" : ccs.getNetconfMasterNode();
            LOG.debug("sdnMasterNode=" + masterNodeName + " and sdnMyNode=" + clusterName);
            if (!masterNodeName.equals(clusterName)) {
                LOG.debug("netconf change but me is not master for this node");
                return false;
            }
        }
        return true;
    }

    @Override
    public void onConfigChanged() {
        this.handleDataTreeAsync = this.config.handleAsync();

    }
}
