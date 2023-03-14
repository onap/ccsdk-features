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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.impl;

import java.util.List;
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
import org.onap.ccsdk.features.sdnr.wt.devicemanager.dcaeconnector.impl.DcaeProviderClient;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.devicemonitor.impl.DeviceMonitor;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.devicemonitor.impl.DeviceMonitorImpl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.eventdatahandler.DeviceManagerDatabaseNotificationService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.eventdatahandler.ODLEventListenerHandler;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.eventdatahandler.RpcPushNotificationsHandler;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.housekeeping.ConnectionStatusHousekeepingService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.housekeeping.ResyncNetworkElementHouskeepingService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.WebSocketServiceClientImpl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.WebSocketServiceClientInternal;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.maintenance.impl.MaintenanceServiceImpl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.factory.FactoryRegistration;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.factory.NetworkElementFactory;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.performancemanager.impl.PerformanceManagerImpl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.AaiService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.EquipmentService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.EventHandlingService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.FaultService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.MaintenanceService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.NetconfNetworkElementService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.NotificationService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.PerformanceManager;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.toggleAlarmFilter.DevicemanagerNotificationDelayService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.vescollectorconnector.impl.VESCollectorServiceImpl;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNodeStateService;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.WebsocketManagerService;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.MountPointService;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Devicemanager - Handles startup and closedown of network element handlers for netconf session - Provide common
 * services for network element specific components
 */
public class DeviceManagerImpl implements NetconfNetworkElementService, DeviceManagerServiceProvider, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceManagerImpl.class);
    private static final String APPLICATION_NAME = "DeviceManager";
    private static final String MYDBKEYNAMEBASE = "SDN-Controller";
    private static final String CONFIGURATIONFILE = "etc/devicemanager.properties";
    public static final long DATABASE_TIMEOUT_MS = 120 * 1000L;

    @SuppressWarnings("unused")
    private static final String STARTUPLOG_FILENAME = "etc/devicemanager.startup.log";

    // MDSAL Services
    private DataBroker dataBroker;
    private MountPointService mountPointService;
    private RpcProviderService rpcProviderRegistry;
    private ClusterSingletonServiceProvider clusterSingletonServiceProvider;
    private WebsocketManagerService websocketmanagerService;
    private IEntityDataProvider iEntityDataProvider;

    // Devicemanager common services for network element handler
    private WebSocketServiceClientInternal webSocketService;
    private ODLEventListenerHandler odlEventListenerHandler; //EventHandlingService
    private DeviceManagerApiServiceImpl rpcApiService;
    private PerformanceManagerImpl performanceManager;
    private DcaeProviderClient dcaeProviderClient;
    private AaiProviderClient aaiProviderClient;
    private DcaeForwarderImpl aotsDcaeForwarder;
    private DeviceMonitor deviceMonitor;
    private MaintenanceServiceImpl maintenanceService;
    private DevicemanagerNotificationDelayService notificationDelayService;
    private ResyncNetworkElementHouskeepingService resyncNetworkElementHouskeepingService;
    private ArchiveCleanService archiveCleanService;
    private ConnectionStatusHousekeepingService housekeepingService;
    private NetconfNodeStateService netconfNodeStateService;
    private DataProvider dataProvider;
    private VESCollectorServiceImpl vesCollectorServiceImpl;

    // Handler
    private DeviceManagerNetconfConnectHandler deviceManagerNetconfConnectHandler;

    // Attributes
    private final List<NetworkElementFactory> factoryList;

    private DeviceManagerDatabaseNotificationService deviceManagerDatabaseAndNotificationService;

    ConfigurationFileRepresentation config;
    private Boolean devicemanagerInitializationOk;

    // Blueprint 1
    public DeviceManagerImpl() {
        LOG.info("Creating provider for {}", APPLICATION_NAME);
        this.devicemanagerInitializationOk = false;
        this.factoryList = new CopyOnWriteArrayList<>();

        this.dataBroker = null;
        this.mountPointService = null;
        this.rpcProviderRegistry = null;
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

    public void setNotificationPublishService(NotificationPublishService notificationPublishService) {}

    public void setMountPointService(MountPointService mountPointService) {
        this.mountPointService = mountPointService;
    }

    public void setClusterSingletonService(ClusterSingletonServiceProvider clusterSingletonService) {
        this.clusterSingletonServiceProvider = clusterSingletonService;
    }

    public void setNetconfNodeStateService(NetconfNodeStateService netconfNodeStateService) {
        this.netconfNodeStateService = netconfNodeStateService;
    }

    public void setWebsocketmanagerService(WebsocketManagerService websocketmanagerService) {
        this.websocketmanagerService = websocketmanagerService;
    }

    public void setEntityDataProvider(IEntityDataProvider iEntityDataProvider) {
        this.iEntityDataProvider = iEntityDataProvider;
    }

    public void init() {

        LOG.info("Session Initiated start {}", APPLICATION_NAME);

        this.dataProvider = iEntityDataProvider.getDataProvider();

        // Get configuration
        this.config = new ConfigurationFileRepresentation(CONFIGURATIONFILE);

        this.notificationDelayService = new DevicemanagerNotificationDelayService(config);

        // start service for device maintenance service
        this.maintenanceService = new MaintenanceServiceImpl(iEntityDataProvider.getHtDatabaseMaintenance());

        // Websockets
        this.webSocketService = new WebSocketServiceClientImpl(websocketmanagerService);

        IEsConfig esConfig = iEntityDataProvider.getEsConfig();
        // DCAE
        this.dcaeProviderClient = new DcaeProviderClient(config, esConfig.getCluster(), this);

        this.aaiProviderClient = new AaiProviderClient(config, this);

        this.vesCollectorServiceImpl = new VESCollectorServiceImpl(config);
        // EM
        String myDbKeyNameExtended = MYDBKEYNAMEBASE + "-" + esConfig.getCluster();

        this.aotsDcaeForwarder = new DcaeForwarderImpl(null, dcaeProviderClient, maintenanceService);

        this.deviceManagerDatabaseAndNotificationService = new DeviceManagerDatabaseNotificationService(dataProvider,
                maintenanceService, webSocketService, notificationDelayService, aotsDcaeForwarder);

        RpcPushNotificationsHandler rpcPushNotificationsHandler =
                new RpcPushNotificationsHandler(webSocketService, dataProvider, aotsDcaeForwarder);
        this.odlEventListenerHandler = new ODLEventListenerHandler(myDbKeyNameExtended, webSocketService, dataProvider,
                aotsDcaeForwarder, dataBroker);
        this.archiveCleanService = new ArchiveCleanService(iEntityDataProvider.getEsConfig(),
                clusterSingletonServiceProvider, dataProvider);
        this.housekeepingService = new ConnectionStatusHousekeepingService(config, clusterSingletonServiceProvider,
                this.dataBroker, dataProvider);
        // PM
        this.performanceManager = new PerformanceManagerImpl(60, this, dataProvider, config);
        // DM
        // DeviceMonitor has to be available before netconfSubscriptionManager is
        // configured
        LOG.debug("start DeviceMonitor Service");
        this.deviceMonitor = new DeviceMonitorImpl(dataBroker, odlEventListenerHandler, config);

        // ResyncNetworkElementHouskeepingService
        this.resyncNetworkElementHouskeepingService = new ResyncNetworkElementHouskeepingService(this,
                mountPointService, odlEventListenerHandler, dataProvider, deviceMonitor);

        // RPC Service for specific services
        // Start RPC Service
        LOG.debug("start rpc service");
        this.rpcApiService = new DeviceManagerApiServiceImpl(rpcProviderRegistry, maintenanceService,
                resyncNetworkElementHouskeepingService, rpcPushNotificationsHandler);

        // netconfSubscriptionManager should be the last one because this is a callback

        // service
        LOG.debug("start NetconfSubscriptionManager Service");
        this.deviceManagerNetconfConnectHandler = new DeviceManagerNetconfConnectHandler(netconfNodeStateService,
                clusterSingletonServiceProvider, odlEventListenerHandler, deviceMonitor, this, factoryList);

        writeToEventLog(APPLICATION_NAME, "startup", "done");
        this.devicemanagerInitializationOk = true;

        LOG.info("Session Initiated end. Initialization done {}", devicemanagerInitializationOk);
    }

    @Override
    public void close() {
        LOG.info("DeviceManagerImpl closing ...");
        close(performanceManager);
        close(dcaeProviderClient);
        close(aotsDcaeForwarder);
        close(aaiProviderClient);
        close(deviceMonitor);
        close(maintenanceService);
        close(rpcApiService);
        close(notificationDelayService);
        close(archiveCleanService);
        close(housekeepingService);
        close(deviceManagerNetconfConnectHandler);
        close(vesCollectorServiceImpl);
        LOG.info("DeviceManagerImpl closing done");
    }

    @Override
    public @NonNull <L extends NetworkElementFactory> FactoryRegistration<L> registerBindingNetworkElementFactory(
            @NonNull final L factory) {
        LOG.debug("Factory registration {}", factory.getClass().getName());

        factoryList.add(factory);
        factory.init(getServiceProvider());
        return new FactoryRegistration<L>() {

            @Override
            public @NonNull L getInstance() {
                return factory;
            }

            @Override
            public void close() {
                factoryList.remove(factory);
            }

        };
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

    @SuppressWarnings("null")
    @Override
    public @NonNull ConfigurationFileRepresentation getConfigurationFileRepresentation() {
        return this.config;
    }

    // Deviceinitialization

    /**
     * Used to close all Services, that should support AutoCloseable Pattern
     *
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
     *
     * @return true if init() was sucessfull. False if not done or not successfull.
     */
    public boolean isDevicemanagerInitializationOk() {
        return this.devicemanagerInitializationOk;
    }

    /**
     * Get NE object. Used by DCAE Service
     *
     * @param mountpoint mount point name
     * @return null or NE specific data
     */
    public @Nullable NetworkElement getConnectedNeByMountpoint(String mountpoint) {

        return this.deviceManagerNetconfConnectHandler.getConnectedNeByMountpoint(mountpoint);

    }

    @Override
    public void writeToEventLog(String objectId, String msg, String value) {
        this.odlEventListenerHandler.writeEventLog(objectId, msg, value);
    }

    @Override
    public @NonNull VESCollectorService getVESCollectorService() {
        return this.vesCollectorServiceImpl;
    }

    @Override
    public WebsocketManagerService getWebsocketService() {
        return this.websocketmanagerService;
    }


}
