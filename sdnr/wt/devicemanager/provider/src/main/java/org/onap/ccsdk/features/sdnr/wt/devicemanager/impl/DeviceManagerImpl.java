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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.IEntityDataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.IEsConfig;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.aaiconnector.impl.AaiProviderClient;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.archiveservice.ArchiveCleanService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.dcaeconnector.impl.DcaeForwarderImpl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.dcaeconnector.impl.DcaeForwarderInternal;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.dcaeconnector.impl.DcaeProviderClient;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.devicemonitor.impl.DeviceMonitor;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.devicemonitor.impl.DeviceMonitorImpl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.eventdatahandler.DeviceManagerDatabaseNotificationService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.eventdatahandler.ODLEventListenerHandler;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.eventdatahandler.RpcPushNotificationsHandler;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.housekeeping.ConnectionStatusHousekeepingService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.housekeeping.ResyncNetworkElementHouskeepingService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.WebSocketServiceClientImpl2;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.WebSocketServiceClientInternal;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.maintenance.impl.MaintenanceServiceImpl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.factory.FactoryRegistration;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.factory.NetworkElementFactory;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.performancemanager.impl.PerformanceManagerImpl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.performancemanager.impl.database.service.MicrowaveHistoricalPerformanceWriterService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.AaiService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.EquipmentService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.EventHandlingService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.FaultService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.MaintenanceService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.NetconfNetworkElementService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.NotificationService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.PerformanceManager;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.toggleAlarmFilter.DevicemanagerNotificationDelayService;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNodeStateService;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.MountPoint;
import org.opendaylight.mdsal.binding.api.MountPointService;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceRegistration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.websocketmanager.rev150105.WebsocketmanagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Devicemanager
 * - Handles startup and closedown of network element handlers for netconf session
 * - Provide common services for network element specific components
 */
public class DeviceManagerImpl implements NetconfNetworkElementService, DeviceManagerServiceProvider, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceManagerImpl.class);
    private static final String APPLICATION_NAME = "DeviceManager";
    private static final String MYDBKEYNAMEBASE = "SDN-Controller";
    private static final String CONFIGURATIONFILE = "etc/devicemanager.properties";
    public static final long DATABASE_TIMEOUT_MS = 120*1000L;

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
    private ODLEventListenerHandler odlEventListenerHandler; //EventHandlingService
    //private NetconfChangeListener netconfChangeListener;
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
    //private HtDatabaseClient htDatabaseClient;
    // Handler
    private RpcPushNotificationsHandler rpcPushNotificationsHandler;
    private DeviceManagerNetconfConnectHandler forTest;
    // Attributes
    private final Object networkelementLock;
    private final ConcurrentHashMap<String, NetworkElement> networkElementRepresentations;
    private final List<MyNetworkElementFactory<? extends NetworkElementFactory>> factoryList;

    private DeviceManagerDatabaseNotificationService deviceManagerDatabaseAndNotificationService;
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

    @SuppressWarnings({ "deprecation", "null" })
    public void init() throws Exception {

        LOG.info("Session Initiated start {}", APPLICATION_NAME);
        this.iEntityDataProvider.setReadyStatus(false);

        this.dataProvider = iEntityDataProvider.getDataProvider();        // Get configuration

        ConfigurationFileRepresentation config = new ConfigurationFileRepresentation(CONFIGURATIONFILE);

        this.notificationDelayService = new DevicemanagerNotificationDelayService(config);

        //EsConfig dbConfig = new EsConfig(config);
        //LOG.debug("esConfig=" + dbConfig.toString());
        // Start database
        // TODO Remove this database client
        //this.htDatabaseClient = new HtDatabaseClient(dbConfig.getHosts());
        //this.htDatabaseClient.waitForYellowStatus(DATABASE_TIMEOUT_MS);

        // start service for device maintenance service
        this.maintenanceService = new MaintenanceServiceImpl(iEntityDataProvider.getHtDatabaseMaintenance());

        // Websockets
        this.webSocketService = new WebSocketServiceClientImpl2(websocketmanagerService);

        IEsConfig esConfig = iEntityDataProvider.getEsConfig();
        // DCAE
        this.dcaeProviderClient = new DcaeProviderClient(config, esConfig.getCluster(), this);

        this.aaiProviderClient = new AaiProviderClient(config, this);
        // EM
        String myDbKeyNameExtended = MYDBKEYNAMEBASE + "-" + esConfig.getCluster();

        this.aotsDcaeForwarder = new DcaeForwarderImpl(null, dcaeProviderClient, maintenanceService);

        this.deviceManagerDatabaseAndNotificationService = new DeviceManagerDatabaseNotificationService(dataProvider, maintenanceService,webSocketService, notificationDelayService, aotsDcaeForwarder);

        this.rpcPushNotificationsHandler = new RpcPushNotificationsHandler(webSocketService,
                dataProvider, aotsDcaeForwarder);
        this.odlEventListenerHandler = new ODLEventListenerHandler(myDbKeyNameExtended, webSocketService,
                dataProvider, aotsDcaeForwarder);
        this.archiveCleanService = new ArchiveCleanService(iEntityDataProvider.getEsConfig(), dataProvider);
        this.housekeepingService = new ConnectionStatusHousekeepingService(this.dataBroker,
                dataProvider);
        this.cssRegistration = this.clusterSingletonServiceProvider
                .registerClusterSingletonService(this.archiveCleanService);
        this.cssRegistration2 = this.clusterSingletonServiceProvider
                .registerClusterSingletonService(this.housekeepingService);
        // PM
        this.performanceManager = new PerformanceManagerImpl(60, this, new MicrowaveHistoricalPerformanceWriterService(dataProvider), config);
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

        //---->>>>>>> OLD OLD OLD
        //this.netconfChangeListener = new NetconfChangeListener(this, dataBroker);
        //this.netconfChangeListener.register();

        //---->>>>>>> NEW NEW NEW
        this.forTest = new DeviceManagerNetconfConnectHandler(netconfNodeStateService, odlEventListenerHandler,
                deviceMonitor, this, factoryList);

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
        //close(htDatabaseClient);
        //close(netconfChangeListener);
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

    /**
     * Execute register command, for network element
     * @param mountPointNodeName  of new network element
     * @param mountPoint of new network element
     * @param inNe that needs to register
     */
    private void initDefault(String mountPointNodeName, MountPoint mountPoint, NetworkElement inNe) {
        // sendUpdateNotification(mountPointNodeName, nNode.getConnectionStatus(), nNode);

        NetworkElement result;
        synchronized (networkelementLock) {
            result = networkElementRepresentations.put(mountPointNodeName, inNe);
        }
        if (result != null) {
            LOG.warn("NE list was not empty as expected, but contained {} ", result.getNodeId());
        } else {
            deviceMonitor.deviceConnectMasterIndication(mountPointNodeName, inNe);
            inNe.register(); // Call NE specific initialization
            odlEventListenerHandler.connectIndication(mountPointNodeName, inNe.getDeviceType());
        }
    }

    @SuppressWarnings("null")
    @Override
    public @NonNull DataProvider getDataProvider() {
        return this.dataProvider;
    }

    @SuppressWarnings("null")
    @Override
    public @NonNull NotificationService getNotificationService() {
        return this.deviceManagerDatabaseAndNotificationService;
    }

    @SuppressWarnings("null")
    @Override
    public @NonNull FaultService getFaultService() {
        return this.deviceManagerDatabaseAndNotificationService;
    }

    @SuppressWarnings("null")
    @Override
    public @NonNull EquipmentService getEquipmentService() {
        return this.deviceManagerDatabaseAndNotificationService;
    }

    @SuppressWarnings("null")
    @Override
    public @NonNull AaiService getAaiService() {
        return this.aaiProviderClient;
    }

    @SuppressWarnings("null")
    @Override
    public @NonNull MaintenanceService getMaintenanceService() {
        return this.maintenanceService;
    }

    @SuppressWarnings("null")
    @Override
    public @NonNull PerformanceManager getPerformanceManagerService() {
        return this.performanceManager;
    }

    @SuppressWarnings("null")
    @Override
    public @NonNull EventHandlingService getEventHandlingService() {
        return this.odlEventListenerHandler;
    }
    // Deviceinitialization

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

}
