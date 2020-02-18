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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ne;

import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.AaiService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.EquipmentService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.EventHandlingService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.FaultService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.MaintenanceService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.PerformanceManager;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.FaultData;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.NetworkElementDeviceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get information over NETCONF device according to ONF Coremodel. Read networkelement and
 * conditional packages.
 *
 * Get conditional packages from Networkelement Possible interfaces are: MWPS, LTP(MWPS-TTP),
 * MWAirInterfacePac, MicrowaveModel-ObjectClasses-AirInterface ETH-CTP,LTP(Client),
 * MW_EthernetContainer_Pac MWS, LTP(MWS-CTP-xD), MWAirInterfaceDiversityPac,
 * MicrowaveModel-ObjectClasses-AirInterfaceDiversity MWS, LTP(MWS-TTP),
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


    private final @NonNull String mountPointNodeName;
    private final @NonNull NetconfAccessor acessor;

    /*-----------------------------------------------------------------------------
     * Construction
     */

    /**
     * Basic element for netconf device with ONF Core model V1.2
     * @param acessor to manage device connection
     * @param serviceProvider to get devicemanager services
     */
    public ONFCoreNetworkElement12Basic(@NonNull NetconfAccessor acessor,
            @NonNull DeviceManagerServiceProvider serviceProvider) {

        super(acessor);
        this.mountPointNodeName = acessor.getNodeId().getValue();
        this.acessor = acessor;

        this.faultService = serviceProvider.getFaultService();
        this.equipmentService = serviceProvider.getEquipmentService();
        this.maintenanceService = serviceProvider.getMaintenanceService();
        this.aaiProviderClient = serviceProvider.getAaiService();
        this.performanceManager = serviceProvider.getPerformanceManagerService();
        this.eventListenerHandler = serviceProvider.getEventHandlingService();


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
            if (change) {
                int problems = faultService.removeAllCurrentProblemsOfNode(nodeId);
                FaultData resultList = readAllCurrentProblemsOfNode();
                faultService.initCurrentProblemStatus(nodeId, resultList);
                LOG.info("Resync mountpoint {} for device {}. Removed {}. Current problems: {}", getMountpoint(),
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
    public synchronized void initialReadFromNetworkElement() {

        LOG.debug("Get info about {}", getMountpoint());

        int problems = faultService.removeAllCurrentProblemsOfNode(nodeId);
        LOG.debug("Removed all {} problems from database at registration", problems);

        // Step 2.1: access data broker within this mount point
        LOG.debug("DBRead start");

        // Step 2.2: read ne from data store
        readNetworkElementAndInterfaces();
        equipment.readNetworkElementEquipment();

        // Step 2.3: read the existing faults and add to DB
        FaultData resultList = readAllCurrentProblemsOfNode();
        equipment.addProblemsofNode(resultList);

        faultService.initCurrentProblemStatus(nodeId, resultList);
        equipmentService.writeEquipment(equipment.getEquipmentData());

        LOG.info("Found info at {} for device {} number of problems: {}", getMountpoint(), getUuId(),
                resultList.size());
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

        // Register netconf stream
        acessor.registerNotificationsStream(NetconfAccessor.DefaultNotificationsStream);

        // -- Read data from NE
        initialReadFromNetworkElement();

        // create automatic empty maintenance entry into db before reading and listening
        // for problems
        maintenanceService.createIfNotExists(mountPointNodeName);

        aaiProviderClient.onDeviceRegistered(mountPointNodeName);
        // -- Register NE to performance manager
        performanceManager.registration(mountPointNodeName, this);

        eventListenerHandler.registration(mountPointNodeName, acessor.getNetconfNode());
        eventListenerHandler.connectIndication(mountPointNodeName, getDeviceType());
        LOG.info("Starting Event listener finished. Added Netconf device:{} type:{}", mountPointNodeName, getDeviceType());

    }


    @Override
    public void deregister() {
        maintenanceService.deleteIfNotRequired(mountPointNodeName);
        performanceManager.deRegistration(mountPointNodeName);
        aaiProviderClient.onDeviceUnregistered(mountPointNodeName);
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
