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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.impl;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.common.database.HtDatabaseClient;
import org.onap.ccsdk.features.sdnr.wt.database.config.EsConfig;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.IEntityDataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.DeviceManagerService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.DeviceMonitoredNe;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.FactoryRegistration;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.NetconfNetworkElementService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.NetworkElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.NetworkElementFactory;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.UnkownDevicemanagerServiceException;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.aaiconnector.impl.AaiProviderClient;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.archiveservice.ArchiveCleanService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.onfcore.ONFCoreNetworkElementFactory;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.onfcore.ONFCoreNetworkElementRepresentation;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.dcaeconnector.impl.DcaeForwarderInternal;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.dcaeconnector.impl.DcaeForwarderImpl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.dcaeconnector.impl.DcaeProviderClient;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.devicemonitor.impl.DeviceMonitor;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.devicemonitor.impl.DeviceMonitorImpl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.housekeeping.ConnectionStatusHousekeepingService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.housekeeping.ResyncNetworkElementHouskeepingService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.conf.odlAkka.AkkaConfig;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.conf.odlGeo.GeoConfig;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.handler.ODLEventListenerHandler;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.handler.RpcPushNotificationsHandler;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.listener.NetconfChangeListener;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.util.GenericTransactionUtils;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.WebSocketServiceClientInternal;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.WebSocketServiceClientDummyImpl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.WebSocketServiceClientImpl2;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.maintenance.impl.MaintenanceServiceImpl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.performancemanager.impl.PerformanceManagerImpl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.performancemanager.impl.database.service.MicrowaveHistoricalPerformanceWriterService;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.INetconfAcessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNodeStateService;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.TransactionUtils;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.MountPoint;
import org.opendaylight.mdsal.binding.api.MountPointService;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceRegistration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNodeConnectionStatus.ConnectionStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.netconf.node.connection.status.ClusteredConnectionStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.network.topology.topology.topology.types.TopologyNetconf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.websocketmanager.rev150105.WebsocketmanagerService;
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

/**
 * Devicemanager
 * - Handles startup and closedown of network element handlers for netconf session
 * - Provide common services for network element specific components
 */
public class DeviceManagerImpl implements NetconfNetworkElementService, DeviceManagerServiceProvider, NetconfNodeService, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceManagerImpl.class);
    private static final String APPLICATION_NAME = "DeviceManager";
    private static final String MYDBKEYNAMEBASE = "SDN-Controller";
    private static final String CONFIGURATIONFILE = "etc/devicemanager.properties";
    public static final long DATABASE_TIMEOUT_MS = 120*1000L;


    // http://sendateodl:8181/restconf/operational/network-topology:network-topology/topology/topology-netconf
    private static final InstanceIdentifier<Topology> NETCONF_TOPO_IID =
            InstanceIdentifier.create(NetworkTopology.class).child(Topology.class,
                    new TopologyKey(new TopologyId(TopologyNetconf.QNAME.getLocalName())));
    @SuppressWarnings("unused")
    private static final String STARTUPLOG_FILENAME = "etc/devicemanager.startup.log";
    // private static final String STARTUPLOG_FILENAME2 = "data/cache/devicemanager.startup.log";

    // MDSAL Services
    private DataBroker dataBroker;
    private MountPointService mountPointService;
    private RpcProviderService rpcProviderRegistry;
    @SuppressWarnings("unused")
    private NotificationPublishService notificationPublishService;
    private ClusterSingletonServiceProvider clusterSingletonServiceProvider;
    private WebsocketmanagerService websocketmanagerService;
    private IEntityDataProvider iEntityDataProvider;

    // Devicemanager common services for network element handler
    private @Nullable WebSocketServiceClientInternal webSocketService;
    private ODLEventListenerHandler odlEventListenerHandler;
    private NetconfChangeListener netconfChangeListener;
    private DeviceManagerApiServiceImpl rpcApiService;
    private PerformanceManagerImpl performanceManager;
    private DcaeProviderClient dcaeProviderClient;
    private AaiProviderClient aaiProviderClient;
    private DcaeForwarderInternal aotsDcaeForwarder;
    private DeviceMonitor deviceMonitor;
    private MaintenanceServiceImpl maintenanceService;
    private DevicemanagerNotificationDelayService notificationDelayService;
    private ResyncNetworkElementHouskeepingService resyncNetworkElementHouskeepingService;
    private ArchiveCleanService archiveCleanService;
    private ConnectionStatusHousekeepingService housekeepingService;
    private NetconfNodeStateService netconfNodeStateService;
    private DataProvider dataProvider;
    private HtDatabaseClient htDatabaseClient;
    // Handler
    private RpcPushNotificationsHandler rpcPushNotificationsHandler;
    private DeviceManagerNetconfConnectHandler forTest;
    private final TransactionUtils transactionUtils;
    // Attributes
    private final Object networkelementLock;
    private final ConcurrentHashMap<String, NetworkElement> networkElementRepresentations;
    private final List<MyNetworkElementFactory<? extends NetworkElementFactory>> factoryList;
    private AkkaConfig akkaConfig;
    private ClusterSingletonServiceRegistration cssRegistration;
    private ClusterSingletonServiceRegistration cssRegistration2;
    private Boolean devicemanagerInitializationOk;

    // Blueprint 1
    public DeviceManagerImpl() {
        LOG.info("Creating provider for {}", APPLICATION_NAME);
        this.devicemanagerInitializationOk = false;
        this.factoryList = new CopyOnWriteArrayList<>();
        this.networkelementLock = new Object();
        this.networkElementRepresentations = new ConcurrentHashMap<>();
        this.transactionUtils = new GenericTransactionUtils();

        this.dataBroker = null;
        this.mountPointService = null;
        this.rpcProviderRegistry = null;
        this.notificationPublishService = null;
        this.clusterSingletonServiceProvider = null;
        this.websocketmanagerService = null;
        this.iEntityDataProvider = null;

        this.webSocketService = null;
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
    public void setNetconfNodeStateService(NetconfNodeStateService netconfNodeStateService) {
        this.netconfNodeStateService = netconfNodeStateService;
    }
    public void setWebsocketmanagerService(WebsocketmanagerService websocketmanagerService) {
        this.websocketmanagerService = websocketmanagerService;
    }
    public void setEntityDataProvider(IEntityDataProvider iEntityDataProvider) {
        this.iEntityDataProvider = iEntityDataProvider;
    }

    public void init() throws Exception {

        LOG.info("Session Initiated start {}", APPLICATION_NAME);
        this.iEntityDataProvider.setReadyStatus(false);

        // Register network element factories and related init functions
        registerMyNetworkElementFactory(new ONFCoreNetworkElementFactory(), (a,b,c) -> initONFCoremodel(a,b,(ONFCoreNetworkElementRepresentation)c));
        //registerMyNetworkElementFactory(new ORanNetworkElementFactory(), (a,b,c) -> initORan(a,b,(ONFCoreNetworkElementRepresentation)c));
        //registerMyNetworkElementFactory(new NtsNetworkElementFactory(), (a,b,c) -> initNts(a,b,(ONFCoreNetworkElementRepresentation)c));
        //registerMyNetworkElementFactory(new GRanNetworkElementFactory(), (a,b,c) -> initGRan(a,b,(ONFCoreNetworkElementRepresentation)c));

        this.dataProvider = iEntityDataProvider.getDataProvider();        // Get configuration

        ConfigurationFileRepresentation config = new ConfigurationFileRepresentation(CONFIGURATIONFILE);

        this.akkaConfig = loadClusterConfiguration();
        this.notificationDelayService = new DevicemanagerNotificationDelayService(config);

        EsConfig dbConfig = new EsConfig(config);
        LOG.debug("esConfig=" + dbConfig.toString());
        // Start database
        // TODO Remove this database client
        this.htDatabaseClient = new HtDatabaseClient(dbConfig.getHosts());
        this.htDatabaseClient.waitForYellowStatus(DATABASE_TIMEOUT_MS);

        // start service for device maintenance service
        this.maintenanceService = new MaintenanceServiceImpl(htDatabaseClient);

        // Websockets
        try {
            this.webSocketService = new WebSocketServiceClientImpl2(websocketmanagerService);
        } catch (Exception e) {
            LOG.error("Can not start websocket service. Loading mock class.", e);
            this.webSocketService = new WebSocketServiceClientDummyImpl();
        }
        // DCAE
        this.dcaeProviderClient = new DcaeProviderClient(config, dbConfig.getCluster(), this);

        this.aaiProviderClient = new AaiProviderClient(config, this);
        // EM
        String myDbKeyNameExtended = MYDBKEYNAMEBASE + "-" + dbConfig.getCluster();

        this.aotsDcaeForwarder = new DcaeForwarderImpl(null, dcaeProviderClient, maintenanceService);
        this.rpcPushNotificationsHandler = new RpcPushNotificationsHandler(webSocketService,
                dataProvider, aotsDcaeForwarder);
        this.odlEventListenerHandler = new ODLEventListenerHandler(myDbKeyNameExtended, webSocketService,
                dataProvider, aotsDcaeForwarder);
        this.archiveCleanService = new ArchiveCleanService(config, dataProvider);
        this.housekeepingService = new ConnectionStatusHousekeepingService(this.dataBroker,
                dataProvider);
        this.cssRegistration = this.clusterSingletonServiceProvider
                .registerClusterSingletonService(this.archiveCleanService);
        this.cssRegistration2 = this.clusterSingletonServiceProvider
                .registerClusterSingletonService(this.housekeepingService);
        // PM
        this.performanceManager = new PerformanceManagerImpl(60, new MicrowaveHistoricalPerformanceWriterService(htDatabaseClient), config);

        // DM
        // DeviceMonitor has to be available before netconfSubscriptionManager is
        // configured
        LOG.debug("start DeviceMonitor Service");
        this.deviceMonitor = new DeviceMonitorImpl(dataBroker, odlEventListenerHandler, config);

        // ResyncNetworkElementHouskeepingService
        this.resyncNetworkElementHouskeepingService = new ResyncNetworkElementHouskeepingService(
                this, mountPointService, odlEventListenerHandler,
                dataProvider, deviceMonitor);

        // RPC Service for specific services
        // Start RPC Service
        LOG.debug("start rpc service");
        this.rpcApiService = new DeviceManagerApiServiceImpl(rpcProviderRegistry, maintenanceService,
                resyncNetworkElementHouskeepingService, rpcPushNotificationsHandler);

        // netconfSubscriptionManager should be the last one because this is a callback
        // service
        LOG.debug("start NetconfSubscriptionManager Service");
        // this.netconfSubscriptionManager = new
        // NetconfSubscriptionManagerOfDeviceManager(this, dataBroker);
        // this.netconfSubscriptionManager.register();
        this.netconfChangeListener = new NetconfChangeListener(this, dataBroker);
        this.netconfChangeListener.register();

        this.forTest = new DeviceManagerNetconfConnectHandler(netconfNodeStateService);

        writeToEventLog(APPLICATION_NAME, "startup", "done");
        this.devicemanagerInitializationOk = true;

        LOG.info("Session Initiated end. Initialization done {}", devicemanagerInitializationOk);
        this.iEntityDataProvider.setReadyStatus(true);

    }

    @Override
    public void close() {
        LOG.info("DeviceManagerImpl closing ...");
        close(performanceManager);
        close(dcaeProviderClient);
        close(aaiProviderClient);
        close(deviceMonitor);
        close(htDatabaseClient);
        close(netconfChangeListener);
        close(maintenanceService);
        close(rpcApiService);
        close(notificationDelayService);
        close(archiveCleanService);
        close(housekeepingService);
        close(forTest);
        close(cssRegistration, cssRegistration2);
        LOG.info("DeviceManagerImpl closing done");
    }

    @Override
    public @NonNull <L extends NetworkElementFactory> FactoryRegistration<L> registerNetworkElementFactory(@NonNull L factory) {
        LOG.info("Factory registration {}", factory.getClass().getName());
        MyNetworkElementFactory<L> myFactory = new MyNetworkElementFactory<>(factory, (a,b,c) -> initDefault(a,b,c));
        factoryList.add(myFactory);
        return new FactoryRegistration<L>() {

            @Override
            public @NonNull L getInstance() {
                return myFactory.getFactory();
            }

            @Override
            public void close() {
                factoryList.remove(myFactory);
            }

        };
    }

    private <L extends NetworkElementFactory> void registerMyNetworkElementFactory(@NonNull L factory,
            Register<String, MountPoint, NetworkElement> init) {
        factoryList.add(new MyNetworkElementFactory<>(factory, init));
    }


    @Override
    public @NonNull DataProvider getDataProvider() {
        return this.dataProvider;
    }

    /**
     * Used to close all Services, that should support AutoCloseable Pattern
     * @param toClose
     */
    private void close(AutoCloseable... toCloseList) {
        for (AutoCloseable element : toCloseList) {
            if (element != null) {
                try {
                    element.close();
                } catch (Exception e) {
                    LOG.warn("Problem during close {}", e);
                }
            }
        }
    }

    /*-------------------------------------------------------------------------------------------
     * Functions for interface DeviceManagerService
     */

    /**
     * For each mounted device a mountpoint is created and this listener is called.
     * Mountpoint was created or existing. Managed device is now fully connected to node/mountpoint.
     * @param action provide action
     * @param nNodeId id of the mountpoint
     * @param nNode mountpoint contents
     */
    public void startListenerOnNodeForConnectedState(Action action, NodeId nNodeId, NetconfNode nNode) {

        String mountPointNodeName = nNodeId.getValue();
        LOG.info("Starting Event listener on Netconf for mountpoint {} Action {}", mountPointNodeName, action);

        boolean preConditionMissing = false;
        if (mountPointService == null) {
            preConditionMissing = true;
            LOG.warn("No mountservice available.");
        }
        if (!devicemanagerInitializationOk) {
            preConditionMissing = true;
            LOG.warn("Devicemanager initialization still pending.");
        }
        if (preConditionMissing) {
            return;
        }

        if (!isNetconfNodeMaster(nNode)) {
            // Change Devicemonitor-status to connected ... for non master mountpoints.
            deviceMonitor.deviceConnectSlaveIndication(mountPointNodeName);
        } else {

            InstanceIdentifier<Node> instanceIdentifier = NETCONF_TOPO_IID.child(Node.class,
                    new NodeKey(nNodeId));

            Optional<MountPoint> optionalMountPoint = waitForMountpoint(instanceIdentifier, mountPointNodeName);


            if (!optionalMountPoint.isPresent()) {
                LOG.warn("Event listener timeout while waiting for mount point for Netconf device :: Name : {} ",
                        mountPointNodeName);
            } else {
                // Mountpoint is present for sure
                MountPoint mountPoint = optionalMountPoint.get();
                // BindingDOMDataBrokerAdapter.BUILDER_FACTORY;
                LOG.info("Mountpoint with id: {} class {} toString {}", mountPoint.getIdentifier(),
                        mountPoint.getClass().getName(), mountPoint);

                Optional<DataBroker> optionalNetconfNodeDatabroker = mountPoint.getService(DataBroker.class);
                if (!optionalNetconfNodeDatabroker.isPresent()) {
                    LOG.info("Slave mountpoint {} without databroker", mountPointNodeName);
                } else {

                    // It is master for mountpoint and all data are available.
                    // Make sure that specific mountPointNodeName is handled only once.
                    // be aware that startListenerOnNodeForConnectedState could be called multiple
                    // times for same mountPointNodeName.
                    // networkElementRepresentations contains handled NEs at master node.

                    synchronized (networkelementLock) {
                        if (networkElementRepresentations.containsKey(mountPointNodeName)) {
                            LOG.warn("Mountpoint {} already registered. Leave startup procedure.", mountPointNodeName);
                            return;
                        }
                    }
                    // update db with connect status
                    sendUpdateNotification(mountPointNodeName, nNode.getConnectionStatus(), nNode);

                    DataBroker netconfNodeDataBroker = optionalNetconfNodeDatabroker.get();
                    LOG.info("Master mountpoint {}", mountPointNodeName);
                    INetconfAcessor acessor = new NetconfAccessor(nNodeId, nNode, netconfNodeDataBroker, mountPoint, transactionUtils);

                    for (MyNetworkElementFactory<? extends NetworkElementFactory> f : factoryList) {
                        Optional<NetworkElement> optionalNe = f.getFactory().create(acessor, this);
                        if (optionalNe.isPresent()) {
                            f.getInit().register(mountPointNodeName, mountPoint, optionalNe.get());
                            break; //Use the first provided
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull <L extends DeviceManagerService> L getService(Class<L> serviceInterface) throws UnkownDevicemanagerServiceException {
        if (serviceInterface.isInstance(webSocketService)) {
            return (L) this.webSocketService;
        } else if (serviceInterface.isInstance(aotsDcaeForwarder)) {
            return (L) this.aotsDcaeForwarder;
        } else if (serviceInterface.isInstance(notificationDelayService)) {
            return (L) notificationDelayService;
        }
        throw new UnkownDevicemanagerServiceException("Unknown service ",serviceInterface);
    }

    // Deviceinitialization

    @FunctionalInterface
    interface Register<X, Y, Z> {
        public void register(X mountPointNodeName, Y mountPoint, Z ne);
    }

    private class MyNetworkElementFactory<L extends NetworkElementFactory> {

        private final Register<String, MountPoint, NetworkElement> init;
        private final @NonNull L factory;

        @SuppressWarnings("null")
        public MyNetworkElementFactory(@NonNull L factory, Register<String, MountPoint, NetworkElement> init) {
            super();
            if (init == null || factory == null) {
                throw new IllegalArgumentException("Null not allowed here.");
            }
            this.init = init;
            this.factory = factory;
        }
        public Register<String, MountPoint, NetworkElement> getInit() {
            return init;
        }
        public @NonNull L getFactory() {
            return factory;
        }
    }

    /**
     * Execute register command, for network element
     * @param mountPointNodeName  of new network element
     * @param mountPoint of new network element
     * @param inNe that needs to register
     */
    private void initDefault(String mountPointNodeName, MountPoint mountPoint, NetworkElement inNe) {
        // sendUpdateNotification(mountPointNodeName, nNode.getConnectionStatus(), nNode);

        // TODO
        putToNetworkElementRepresentations(mountPointNodeName, inNe);
        deviceMonitor.deviceConnectMasterIndication(mountPointNodeName, inNe);

        inNe.register();
    }

    private void initONFCoremodel(String mountPointNodeName, MountPoint mountPoint,
            ONFCoreNetworkElementRepresentation ne) {
        putToNetworkElementRepresentations(mountPointNodeName, ne);
        // create automatic empty maintenance entry into db before reading and listening
        // for problems
        maintenanceService.createIfNotExists(mountPointNodeName);

        // Setup microwaveEventListener for notification service
        // MicrowaveEventListener microwaveEventListener = new
        // MicrowaveEventListener(mountPointNodeName, websocketmanagerService,
        // xmlMapper, databaseClientEvents);

        ne.doRegisterEventListener(mountPoint);

        // Register netconf stream
        NetconfNotification.registerNotificationStream(mountPointNodeName, mountPoint, "NETCONF");

        // -- Read data from NE
        ne.initialReadFromNetworkElement();

        if (aaiProviderClient != null) {
            aaiProviderClient.onDeviceRegistered(mountPointNodeName);
        }
        // -- Register NE to performance manager
        if (performanceManager != null) {
            performanceManager.registration(mountPointNodeName, ne);
        }

        deviceMonitor.deviceConnectMasterIndication(mountPointNodeName, (DeviceMonitoredNe)ne);

        LOG.info("Starting Event listener finished. Added Netconf device {}", mountPointNodeName);
    }

    private void initORan(String mountPointNodeName, MountPoint mountPoint, ONFCoreNetworkElementRepresentation neORan) {
        // sendUpdateNotification(mountPointNodeName, nNode.getConnectionStatus(), nNode);
        putToNetworkElementRepresentations(mountPointNodeName, neORan);

        maintenanceService.createIfNotExists(mountPointNodeName);

        deviceMonitor.deviceConnectMasterIndication(mountPointNodeName, (DeviceMonitoredNe)neORan);

        // -- Read data from NE
        neORan.initialReadFromNetworkElement();
        neORan.doRegisterEventListener(mountPoint);
        NetconfNotification.registerNotificationStream(mountPointNodeName, mountPoint, "NETCONF");
    }

    private void initNts(String mountPointNodeName, MountPoint mountPoint, ONFCoreNetworkElementRepresentation neNts) {
        // sendUpdateNotification(mountPointNodeName, nNode.getConnectionStatus(), nNode);
        putToNetworkElementRepresentations(mountPointNodeName, neNts);
        deviceMonitor.deviceConnectMasterIndication(mountPointNodeName, (DeviceMonitoredNe)neNts);

        // -- Read data from NE
        neNts.initialReadFromNetworkElement();
    }
    private void initGRan(String mountPointNodeName, MountPoint mountPoint, ONFCoreNetworkElementRepresentation neGRan) {
        // sendUpdateNotification(mountPointNodeName, nNode.getConnectionStatus(), nNode);
        putToNetworkElementRepresentations(mountPointNodeName, neGRan);
        deviceMonitor.deviceConnectMasterIndication(mountPointNodeName, (DeviceMonitoredNe)neGRan);

        // -- Read data from NE
        neGRan.initialReadFromNetworkElement();
    }
    /**
     * @param instanceIdentifier
     * @param mountPointNodeName
     * @return
     */
    private Optional<MountPoint> waitForMountpoint(InstanceIdentifier<Node> instanceIdentifier,
            String mountPointNodeName) {
        Optional<MountPoint> optionalMountPoint = null;
        int timeout = 10000;
        while (!(optionalMountPoint = mountPointService.getMountPoint(instanceIdentifier)).isPresent()
                && timeout > 0) {
            LOG.info("Event listener waiting for mount point for Netconf device :: Name : {}", mountPointNodeName);
            sleepMs(1000);
            timeout -= 1000;
        }
        return optionalMountPoint;
    }

    private void putToNetworkElementRepresentations(String mountPointNodeName, NetworkElement ne) {
        NetworkElement result;
        synchronized (networkelementLock) {
            result = networkElementRepresentations.put(mountPointNodeName, ne);
        }
        if (result != null) {
            LOG.warn("NE list was not empty as expected, but contained {} ", result.getNodeId());
        } else {
            odlEventListenerHandler.connectIndication(mountPointNodeName, ne.getDeviceType());
        }
    }

    /**
     * Mountpoint created or existing. Managed device is actually disconnected from node/ mountpoint.
     * Origin state: Connecting, Connected
     * Target state: are UnableToConnect or Connecting
     * @param action create or update
     * @param nNodeId id of the mountpoint
     * @param nNode mountpoint contents
     */
    public void enterNonConnectedState(Action action, NodeId nNodeId, NetconfNode nNode) {
        String mountPointNodeName = nNodeId.getValue();
        ConnectionStatus csts = nNode.getConnectionStatus();
        if (isNetconfNodeMaster(nNode)) {
            sendUpdateNotification(mountPointNodeName, csts,nNode);
        }

        // Handling if mountpoint exist. connected -> connecting/UnableToConnect
        stopListenerOnNodeForConnectedState(mountPointNodeName);

        deviceMonitor.deviceDisconnectIndication(mountPointNodeName);

    }

    /**
     * Mountpoint removed indication.
     * @param nNodeId id of the mountpoint
     */
    public void removeMountpointState(NodeId nNodeId) {
        String mountPointNodeName = nNodeId.getValue();
        LOG.info("mountpointNodeRemoved {}", nNodeId.getValue());

        stopListenerOnNodeForConnectedState(mountPointNodeName);
        deviceMonitor.removeMountpointIndication(mountPointNodeName);
        if (odlEventListenerHandler != null) {
            odlEventListenerHandler.deRegistration(mountPointNodeName);
        }
    }

    /**
     * Do all tasks necessary to move from mountpoint state connected -> connecting
     * @param mountPointNodeName provided
     * @param ne representing the device connected to mountpoint
     */
    private void stopListenerOnNodeForConnectedState( String mountPointNodeName) {
        NetworkElement ne = networkElementRepresentations.remove(mountPointNodeName);
        if (ne != null) {
            this.maintenanceService.deleteIfNotRequired(mountPointNodeName);
            ne.deregister();
            if (performanceManager != null) {
                performanceManager.deRegistration(mountPointNodeName);
            }
            if (aaiProviderClient != null) {
                aaiProviderClient.onDeviceUnregistered(mountPointNodeName);
            }
        }
    }

    private void sendUpdateNotification(String mountPointNodeName, ConnectionStatus csts, NetconfNode nNode) {
        LOG.info("update ConnectedState for device :: Name : {} ConnectionStatus {}", mountPointNodeName, csts);
        if (odlEventListenerHandler != null) {
            odlEventListenerHandler.updateRegistration(mountPointNodeName, ConnectionStatus.class.getSimpleName(),
                    csts != null ? csts.getName() : "null", nNode);
        }
    }

    /**
     * Handle netconf/mountpoint changes
     */
    @Override
    public void netconfNodeChangeHandler(Action action, NodeId nodeId, NetconfNode nNode) {

        @Nullable ConnectionStatus csts = nNode.getConnectionStatus();
        @Nullable ClusteredConnectionStatus ccsts = nNode.getClusteredConnectionStatus();
        String nodeIdString = nodeId.getValue();

        LOG.debug("NETCONF Node processing with id {} action {} status {} cluster status {}", nodeId,
                    action, csts, ccsts);

        boolean isCluster = akkaConfig == null && akkaConfig.isCluster();
        if (isCluster && ccsts == null) {
            LOG.debug("NETCONF Node {} {} does not provide cluster status. Stop execution.", nodeIdString, action);
        } else {
            switch (action) {
                case REMOVE:
                    removeMountpointState(nodeId); // Stop Monitor
                    break;
                case CREATE:
                    if (odlEventListenerHandler != null) {
                        odlEventListenerHandler.registration(nodeIdString,nNode);
                    }
                    createOrUpdateMountpointState(action, csts, nodeId, nNode);
                    break;
                case UPDATE:
                    createOrUpdateMountpointState(action, csts, nodeId, nNode);
                    break;
            }
        }
    }

    private void createOrUpdateMountpointState(Action action, @Nullable ConnectionStatus csts, NodeId nodeId, NetconfNode nNode) {
        if (csts != null) {
            switch (csts) {
                case Connected: {
                    startListenerOnNodeForConnectedState(action, nodeId, nNode);
                    break;
                }
                case UnableToConnect:
                case Connecting: {
                    enterNonConnectedState(action, nodeId, nNode);
                    break;
                }
            }
        } else {
            LOG.debug("NETCONF Node handled with null status for action", action);
        }
    }

    /*-------------------------------------------------------------------------------------------
     * Functions
     */

    public ArchiveCleanService getArchiveCleanService() {
        return this.archiveCleanService;
    }

    public DataProvider getDatabaseClientEvents() {
        return dataProvider;
    }

    @Override
    public DeviceManagerServiceProvider getServiceProvider() {
        return this;
    }

    /**
     * Indication if init() of devicemanager successfully done.
     * @return true if init() was sucessfull. False if not done or not successfull.
     */
    public boolean isDevicemanagerInitializationOk() {
        return this.devicemanagerInitializationOk;
    }

    /**
     * Get NE object. Used by DCAE Service
     * @param mountpoint mount point name
     * @return null or NE specific data
     */
    public @Nullable NetworkElement getNeByMountpoint(String mountpoint) {

        return networkElementRepresentations.get(mountpoint);

    }

    @Override
    public void writeToEventLog(String objectId, String msg, String value) {
        this.odlEventListenerHandler.writeEventLog(objectId, msg, value);
    }

    /*---------------------------------------------------------------------
     * Private funtions
     */


    /* -- LOG related functions -- */


    private boolean isInClusterMode() {
        return this.akkaConfig == null ? false : this.akkaConfig.isCluster();
    }

    private String getClusterNetconfNodeName() {
        return this.akkaConfig == null ? "" : this.akkaConfig.getClusterConfig().getClusterSeedNodeName("abc");
    }

    private boolean isNetconfNodeMaster(NetconfNode nNode) {
        if (isInClusterMode()) {
            LOG.debug("check if me is responsible for node");
            String masterNodeName = null;
            ClusteredConnectionStatus ccst = nNode.getClusteredConnectionStatus();
            if (ccst != null) {
                masterNodeName = ccst.getNetconfMasterNode();
            }
            if (masterNodeName == null) {
                masterNodeName = "null";
            }

            String myNodeName = getClusterNetconfNodeName();
            LOG.debug("sdnMasterNode=" + masterNodeName + " and sdnMyNode=" + myNodeName);
            if (!masterNodeName.equals(myNodeName)) {
                LOG.debug("netconf change but me is not master for this node");
                return false;
            }
        }
        return true;
    }

    private static AkkaConfig loadClusterConfiguration() {
        AkkaConfig akkaConfigRes;
        try {
            akkaConfigRes = AkkaConfig.load();
            LOG.debug("akka.conf loaded: " + akkaConfigRes.toString());
        } catch (Exception e1) {
            akkaConfigRes = null;
            LOG.warn("problem loading akka.conf: " + e1.getMessage());
        }
        @SuppressWarnings("unused")
        GeoConfig geoConfig = null;
        if (akkaConfigRes != null && akkaConfigRes.isCluster()) {
            LOG.info("cluster mode detected");
            if (GeoConfig.fileExists()) {
                try {
                    LOG.debug("try to load geoconfig");
                    geoConfig = GeoConfig.load();
                } catch (Exception err) {
                    LOG.warn("problem loading geoconfig: " + err.getMessage());
                }
            } else {
                LOG.debug("no geoconfig file found");
            }
        } else {
            LOG.info("single node mode detected");
        }
        return akkaConfigRes;
    }

    private void sleepMs(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            LOG.debug("Interrupted sleep");
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
    }
}
