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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ne;

import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.impl.DeviceManagerOnfConfiguration;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.AaiService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.EquipmentService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.EventHandlingService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.FaultService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.MaintenanceService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.PerformanceManager;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.FaultData;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev240911.netconf.node.augment.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementConnectionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementDeviceType;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.common.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get information over NETCONF device according to ONF Coremodel. Read networkelement and conditional packages.
 *
 * Get conditional packages from Networkelement Possible interfaces are: MWPS, LTP(MWPS-TTP), MWAirInterfacePac,
 * MicrowaveModel-ObjectClasses-AirInterface ETH-CTP,LTP(Client), MW_EthernetContainer_Pac MWS, LTP(MWS-CTP-xD),
 * MWAirInterfaceDiversityPac, MicrowaveModel-ObjectClasses-AirInterfaceDiversity MWS, LTP(MWS-TTP),
 * ,MicrowaveModel-ObjectClasses-HybridMwStructure MWS, LTP(MWS-TTP),
 * ,MicrowaveModel-ObjectClasses-PureEthernetStructure
 *
 * @author herbert
 *
 */
public class ONFCoreNetworkElement12Basic extends ONFCoreNetworkElement12Base {

    private static final Logger LOG = LoggerFactory.getLogger(ONFCoreNetworkElement12Basic.class);

    /*-----------------------------------------------------------------------------
     * Class members
     */
    private final @NonNull FaultService faultService;
    private final @NonNull EquipmentService equipmentService;
    private final @NonNull MaintenanceService maintenanceService;
    private final @NonNull AaiService aaiProviderClient;
    private final @NonNull PerformanceManager performanceManager;
    private final @NonNull EventHandlingService eventListenerHandler;
    private final @NonNull DataProvider dataProvider;


    private final @NonNull NodeId mountPointNodeId;
    private final @NonNull NetconfBindingAccessor acessor;
    private final @NonNull DeviceManagerOnfConfiguration pollAlarmConfig;

    /*-----------------------------------------------------------------------------
     * Construction
     */

    /**
     * Basic element for netconf device with ONF Core model V1.2
     *
     * @param acessor to manage device connection
     * @param serviceProvider to get devicemanager services
     */
    public ONFCoreNetworkElement12Basic(@NonNull NetconfBindingAccessor acessor,
            @NonNull DeviceManagerServiceProvider serviceProvider, DeviceManagerOnfConfiguration configuration) {

        super(acessor, serviceProvider);
        this.mountPointNodeId = acessor.getNodeId();
        this.acessor = acessor;
        this.pollAlarmConfig = configuration;

        this.faultService = serviceProvider.getFaultService();
        this.equipmentService = serviceProvider.getEquipmentService();
        this.maintenanceService = serviceProvider.getMaintenanceService();
        this.aaiProviderClient = serviceProvider.getAaiService();
        this.performanceManager = serviceProvider.getPerformanceManagerService();
        this.eventListenerHandler = serviceProvider.getEventHandlingService();
        this.dataProvider = serviceProvider.getDataProvider();
    }

    /*-----------------------------------------------------------------------------
     * Functions
     */

    /**
     * DeviceMonitor Prepare check by updating NE state and reading all interfaces.
     */
    @Override
    public void prepareCheck() {
        synchronized (dmLock) {
            boolean change = readNetworkElementAndInterfaces();
            if (change || pollAlarmConfig.isPollAlarmsEnabled()) {
                int problems = faultService.removeAllCurrentProblemsOfNode(nodeId);
                FaultData resultList = readAllCurrentProblemsOfNode();
                faultService.initCurrentProblemStatus(nodeId, resultList);
                LOG.debug("Resync mountpoint {} for device {}. Removed {}. Current problems: {}", getMountpoint(),
                        getUuId(), problems, resultList.size());
            }
        }
    }

    // public boolean checkIfConnectionToMediatorIsOk() -> Shifted to super class
    // public boolean checkIfConnectionToNeIsOk() -> Shifted to super class

    /*-----------------------------------------------------------------------------
     * Synchronization
     */

    // public void initSynchronizationExtension() -> Shifted to super class
    // private InstanceList readPTPClockInstances() -> Shifted to super class


    /*-----------------------------------------------------------------------------
     * Problem/Fault related functions
     */

    /**
     * Read during startup all relevant structure and status parameters from device
     */
    @Override
    public void initialReadFromNetworkElement() {

        LOG.debug("Get info about {}", getMountpoint());

        int problems = faultService.removeAllCurrentProblemsOfNode(nodeId);
        LOG.debug("Removed all {} problems from database at registration", problems);

        // Step 2.1: access data broker within this mount point
        LOG.debug("DBRead start");

        // Step 2.2: read ne from data store
        readNetworkElementAndInterfaces();
        LOG.debug("NETCONF read network element and interfaces completed");
        equipment.readNetworkElementEquipment();
        LOG.debug("NETCONF read equipment completed");

        // Step 2.3: read the existing faults and add to DB
        FaultData resultList = readAllCurrentProblemsOfNode();
        LOG.debug("NETCONF read current problems completed");
        equipment.addProblemsofNode(resultList);

        faultService.initCurrentProblemStatus(nodeId, resultList);
        LOG.debug("DB write current problems completed");

        equipmentService.writeEquipment(nodeId, equipment.getEquipmentData());

        LOG.debug("Found info at {} for device {} number of problems: {}", getMountpoint(), getUuId(),
                resultList.size());
    }

    /**
     * @param nNode
     * set core-model-capability
     */
    public void setCoreModel(@NonNull NetconfNode nNode) {
        NetworkElementConnectionBuilder eb = new NetworkElementConnectionBuilder();

        String namespaceRevision;
        QName QNAME_COREMODEL = QName.create("urn:onf:params:xml:ns:yang:core-model", "2017-03-20", "core-model").intern();

        Capabilities availableCapabilities = Capabilities.getAvailableCapabilities(nNode);
        namespaceRevision = availableCapabilities.getRevisionForNamespace(QNAME_COREMODEL);
        if (Capabilities.isNamespaceSupported(namespaceRevision)) {
            eb.setCoreModelCapability(namespaceRevision);
        } else {
            eb.setCoreModelCapability("Unsupported");
        }
        dataProvider.updateNetworkConnection22(eb.build(), acessor.getNodeId().getValue());
    }
    /**
     * Remove all entries from list
     */
    @Override
    public int removeAllCurrentProblemsOfNode() {
        return faultService.removeAllCurrentProblemsOfNode(nodeId);
    }

    @Override
    public void register() {

        // Setup microwaveEventListener for notification service
        doRegisterEventListener(acessor.getMountpoint());

		if (acessor.isNotificationsRFC5277Supported()) {
			// Register default (NETCONF) stream
			acessor.registerNotificationsStream();
		}

        // Set core-model revision value in "core-model-capability" field
        setCoreModel(acessor.getNetconfNode());

        // -- Read data from NE
        initialReadFromNetworkElement();

        // create automatic empty maintenance entry into db before reading and listening
        // for problems
        maintenanceService.createIfNotExists(mountPointNodeId);

        aaiProviderClient.onDeviceRegistered(mountPointNodeId);
        // -- Register NE to performance manager
        performanceManager.registration(mountPointNodeId, this);

        //events will be already pushed by base devmgr (needs more clarification SDNC-1123)
        //eventListenerHandler.registration(mountPointNodeName, acessor.getNetconfNode());
        //LOG.debug("refresh necon entry for {} with type {} not",mountPointNodeName,this.getDeviceType());
        //eventListenerHandler.connectIndication(mountPointNodeName, getDeviceType());
        LOG.info("Starting Event listener finished. Added Netconf device:{} type:{}", mountPointNodeId,
                getDeviceType());

    }


    @Override
    public void deregister() {
        maintenanceService.deleteIfNotRequired(mountPointNodeId);
        performanceManager.deRegistration(mountPointNodeId);
        aaiProviderClient.onDeviceUnregistered(mountPointNodeId);
        faultService.removeAllCurrentProblemsOfNode(acessor.getNodeId());
        dataProvider.clearGuiCutThroughEntriesOfNode(acessor.getNodeId().getValue());
    }

    @Override
    public void close() throws Exception {
        // Close to be implemented
    }


    @Override
    public NetworkElementDeviceType getDeviceType() {
        return NetworkElementDeviceType.Optical;
    }

}
