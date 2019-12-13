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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.base.onfcore;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.dcaeconnector.impl.DcaeForwarderInternal;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.handler.NetconfEventListenerHandler12;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.AttributeValueChangedNotificationXml;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.ProblemNotificationXml;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.WebSocketServiceClientInternal;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.toggleAlarmFilter.NotificationDelayService;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.INetconfAcessor;
import org.opendaylight.mdsal.binding.api.DataBroker;
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
public class ONFCoreNetworkElement12Basic extends ONFCoreNetworkElement12Base
        implements ONFCoreNetworkElementCallback, NotificationActor<AttributeValueChangedNotificationXml> {

    private static final Logger LOG = LoggerFactory.getLogger(ONFCoreNetworkElement12Basic.class);

    /*-----------------------------------------------------------------------------
     * Class members
     */
    private final @NonNull NetconfEventListenerHandler12 netconfEventListener;
    private final NotificationWorker<AttributeValueChangedNotificationXml> notificationQueue;

    /*-----------------------------------------------------------------------------
     * Construction
     */

    /**
     * Constructor
     *
     * @param mountPointNodeName as String
     * @param capabilities of the specific network element
     * @param netconfNodeDataBroker for the network element specific data
     * @param webSocketService to forward event notifications
     * @param databaseService to access the database
     * @param dcaeProvider to forward problem / change notifications
     */
    ONFCoreNetworkElement12Basic(INetconfAcessor acessor, String mountPointNodeName, Capabilities capabilities,
            DataBroker netconfNodeDataBroker, WebSocketServiceClientInternal webSocketService,
            DataProvider databaseService, DcaeForwarderInternal aotsDcaeForwarder,
            NotificationDelayService<ProblemNotificationXml> notificationDelayService ) {

        super(acessor, mountPointNodeName, netconfNodeDataBroker, capabilities);

        this.netconfEventListener = new NetconfEventListenerHandler12(mountPointNodeName, webSocketService,
                databaseService, aotsDcaeForwarder, notificationDelayService, this);
        this.notificationQueue = new NotificationWorker<>(1, 100, this);

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
                int problems = netconfEventListener.removeAllCurrentProblemsOfNode();
                List<ProblemNotificationXml> resultList = readAllCurrentProblemsOfNode();
                netconfEventListener.initCurrentProblemStatus(resultList);
                LOG.info("Resync mountpoint {} for device {}. Removed {}. Current problems: {}", getMountPointNodeName(),
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
     * Services for NE/Device synchronization
     */

    /**
     * Handling of specific Notifications from NE, indicating changes and need for synchronization.
     *
     * <attribute-value-changed-notification xmlns="urn:onf:params:xml:ns:yang:microwave-model">
     * <attribute-name>/equipment-pac/equipment-current-problems</attribute-name>
     * <object-id-ref>CARD-1.1.1.0</object-id-ref> <new-value></new-value>
     * </attribute-value-changed-notification>
     * <attribute-value-changed-notification xmlns="urn:onf:params:xml:ns:yang:microwave-model">
     * <attribute-name>/network-element/extension[value-name="top-level-equipment"]/value</attribute-name>
     * <object-id-ref>Hybrid-Z</object-id-ref>
     * <new-value>SHELF-1.1.0.0,IDU-1.55.0.0,ODU-1.56.0.0,IDU-1.65.0.0</new-value>
     * </attribute-value-changed-notification>
     */


    @Override
    public void notificationFromNeListener(AttributeValueChangedNotificationXml notificationXml) {
        notificationQueue.put(notificationXml);
    }

    @Override
    public void notificationActor(AttributeValueChangedNotificationXml notificationXml) {

        LOG.debug("Enter change notification listener");
        if (LOG.isTraceEnabled()) {
            LOG.trace("Notification: {}", notificationXml);
        }
        if (notificationXml.getAttributeName().equals("/equipment-pac/equipment-current-problems")) {
            syncEquipmentPac(notificationXml.getObjectId());
        } else if (notificationXml.getAttributeName()
                .equals("/network-element/extension[value-name=\"top-level-equipment\"]/value")) {
            initialReadFromNetworkElement();
        }
        LOG.debug("Leave change notification listener");
    }

    /**
     * Synchronize problems for a specific equipment-pac
     *
     * @param uuidString of the equipment-pac
     */
    private synchronized void syncEquipmentPac(String uuidString) {

        int problems = netconfEventListener.removeObjectsCurrentProblemsOfNode(uuidString);
        LOG.debug("Removed {} problems for uuid {}", problems, uuidString);

        List<ProblemNotificationXml> resultList = equipment.addProblemsofNodeObject(uuidString);
        netconfEventListener.initCurrentProblemStatus(resultList);
        LOG.debug("Added {} problems for uuid {}", resultList.size(), uuidString);

    }


    /*-----------------------------------------------------------------------------
     * Problem/Fault related functions
     */

    /**
     * Read during startup all relevant structure and status parameters from device
     */
    @Override
    public synchronized void initialReadFromNetworkElement() {
        // optionalNe.getLtp().get(0).getLp();
        LOG.debug("Get info about {}", getMountPointNodeName());

        int problems = netconfEventListener.removeAllCurrentProblemsOfNode();
        LOG.debug("Removed all {} problems from database at registration", problems);

        // Step 2.1: access data broker within this mount point
        LOG.debug("DBRead start");

        // Step 2.2: read ne from data store
        readNetworkElementAndInterfaces();
        equipment.readNetworkElementEquipment();

        // Step 2.3: read the existing faults and add to DB
        List<ProblemNotificationXml> resultList = readAllCurrentProblemsOfNode();
        equipment.addProblemsofNode(resultList);

        netconfEventListener.initCurrentProblemStatus(resultList);

        netconfEventListener.writeEquipment(equipment);

        LOG.info("Found info at {} for device {} number of problems: {}", getMountPointNodeName(), getUuId(),
                resultList.size());
    }

    /**
     * Remove all entries from list
     */
    @Override
    public int removeAllCurrentProblemsOfNode() {
        return netconfEventListener.removeAllCurrentProblemsOfNode();
    }


    @Override
    public void close() throws Exception {
    }


    @Override
    public NetworkElementDeviceType getDeviceType() {
        return NetworkElementDeviceType.Optical;
    }

}
