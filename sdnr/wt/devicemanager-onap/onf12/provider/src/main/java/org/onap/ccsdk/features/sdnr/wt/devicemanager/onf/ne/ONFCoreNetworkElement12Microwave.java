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

import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.common.YangHelper;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.microwave.Helper;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.microwave.OnfMicrowaveModel;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.impl.DeviceManagerOnfConfiguration;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.notifications.NotificationActor;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.notifications.NotificationWorker;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.util.ONFLayerProtocolName;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.EquipmentService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.FaultService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.FaultData;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.PerformanceDataLtp;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.util.InconsistentPMDataException;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.opendaylight.mdsal.binding.api.MountPoint;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.UniversalId;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.extension.g.Extension;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.logical.termination.point.g.Lp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EventlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementDeviceType;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
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
public class ONFCoreNetworkElement12Microwave extends ONFCoreNetworkElement12Basic
        implements NotificationActor<EventlogEntity> {

    private static final Logger LOG = LoggerFactory.getLogger(ONFCoreNetworkElement12Microwave.class);

    private final @NonNull FaultService microwaveEventListener;
    private final @NonNull EquipmentService equipmentService;
    private final @NonNull OnfMicrowaveModel microwaveModel;
    private final NotificationWorker<EventlogEntity> notificationQueue;

    private ListenerRegistration<NotificationListener> listenerRegistrationresult = null;

    /*-----------------------------------------------------------------------------
     * Construction
     */

    /**
     * Constructor
     *
     * @param acessor for device
     * @param serviceProvider to get services
     * @param onfMicrowaveModel handling ofmicrosoft model data
     */
    public ONFCoreNetworkElement12Microwave(@NonNull NetconfBindingAccessor acessor,
            @NonNull DeviceManagerServiceProvider serviceProvider, DeviceManagerOnfConfiguration configuration,
            OnfMicrowaveModel onfMicrowaveModel) {

        super(acessor, serviceProvider, configuration);
        this.microwaveModel = onfMicrowaveModel;

        this.microwaveEventListener = serviceProvider.getFaultService();
        this.equipmentService = serviceProvider.getEquipmentService();

        this.notificationQueue = new NotificationWorker<>(1, 100, this);
        this.microwaveModel.setNotificationQueue(notificationQueue);

    }

    /*-----------------------------------------------------------------------------
     * Functions
     */

    /**
     * @param acessor
     * @param serviceProvider
     * @param configuration
     * @param onfMicrowaveModel
     */
    /**
     * DeviceMonitor Prepare check by updating NE state and reading all interfaces.
     */
    @Override
    public void prepareCheck() {
        synchronized (dmLock) {
            boolean change = readNetworkElementAndInterfaces();
            if (change) {
                int problems = microwaveEventListener.removeAllCurrentProblemsOfNode(nodeId);
                FaultData resultList = readAllCurrentProblemsOfNode();
                microwaveEventListener.initCurrentProblemStatus(nodeId, resultList);
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
     * Services for NE/Device synchronization
     */

    /**
     * Handling of specific Notifications from NE, indicating changes and need for synchronization.
     *
     * <attribute-value-changed-notification xmlns="urn:onf:params:xml:ns:yang:microwave-model">
     * <attribute-name>/equipment-pac/equipment-current-problems</attribute-name>
     * <object-id-ref>CARD-1.1.1.0</object-id-ref> <new-value></new-value> </attribute-value-changed-notification>
     * <attribute-value-changed-notification xmlns="urn:onf:params:xml:ns:yang:microwave-model">
     * <attribute-name>/network-element/extension[value-name="top-level-equipment"]/value</attribute-name>
     * <object-id-ref>Hybrid-Z</object-id-ref>
     * <new-value>SHELF-1.1.0.0,IDU-1.55.0.0,ODU-1.56.0.0,IDU-1.65.0.0</new-value>
     * </attribute-value-changed-notification>
     */
    @Override
    public void notificationActor(@NonNull EventlogEntity notification) {

        LOG.debug("Enter change notification listener");
        if (LOG.isTraceEnabled()) {
            LOG.trace("Notification: {}", notification);
        }
        String attributeName = notification.getAttributeName();
        if (attributeName != null) {
            if (attributeName.equals("/equipment-pac/equipment-current-problems")) {
                syncEquipmentPac(notification.getObjectId());
            } else if (attributeName.equals("/network-element/extension[value-name=\"top-level-equipment\"]/value")) {
                initialReadFromNetworkElement();
            }
        }
        LOG.debug("Leave change notification listener");
    }

    /**
     * Synchronize problems for a specific equipment-pac
     *
     * @param uuidString of the equipment-pac
     */
    private void syncEquipmentPac(String uuidString) {

        int problems = microwaveEventListener.removeObjectsCurrentProblemsOfNode(nodeId, uuidString);
        LOG.debug("Removed {} problems for uuid {}", problems, uuidString);

        FaultData resultList = equipment.addProblemsofNodeObject(uuidString);
        microwaveEventListener.initCurrentProblemStatus(nodeId, resultList);
        LOG.debug("Added {} problems for uuid {}", resultList.size(), uuidString);

    }


    /*-----------------------------------------------------------------------------
     * Problem/Fault related functions
     */

    /**
     * Read during startup all relevant structure and status parameters from device
     */
    @Override
    public void initialReadFromNetworkElement() {
        LOG.debug("Get info about {}", getMountpoint());

        int problems = microwaveEventListener.removeAllCurrentProblemsOfNode(nodeId);
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

        microwaveEventListener.initCurrentProblemStatus(nodeId, resultList);
        LOG.debug("DB write current problems completed");
        equipmentService.writeEquipment(nodeId, equipment.getEquipmentData());

        LOG.debug("Found info at {} for device {} number of problems: {}", getMountpoint(), getUuId(),
                resultList.size());
    }

    /**
     * LOG the newly added problems of the interface pac
     *
     * @param idxStart
     * @param uuid
     * @param resultList
     */
    private void debugResultList(String uuid, FaultData resultList, int idxStart) {
        if (LOG.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            int idx = 0;
            for (int t = idxStart; t < resultList.size(); t++) {
                sb.append(idx++);
                sb.append(":{");
                sb.append(resultList.get(t));
                sb.append('}');
            }
            LOG.debug("Found problems {} {}", uuid, sb);
        }
    }

    /**
     * Read current problems of AirInterfaces and EthernetContainer according to NE status into DB
     *
     * @return List with all problems
     */
    @Override
    protected FaultData readAllCurrentProblemsOfNode() {

        // Step 2.3: read the existing faults and add to DB
        FaultData resultList = new FaultData();
        int idxStart; // Start index for debug messages
        @NonNull
        UniversalId uuid;

        synchronized (getPmLock()) {
            for (Lp lp : getInterfaceList()) {

                idxStart = resultList.size();
                uuid = Helper.nnGetUniversalId(lp.getUuid());
                @Nullable
                Class<?> lpClass = getLpExtension(lp);

                ONFLayerProtocolName lpName = ONFLayerProtocolName.valueOf(lp.getLayerProtocolName());
                microwaveModel.readTheFaultsOfMicrowaveModel(lpName, lpClass, uuid, resultList);
                debugResultList(uuid.getValue(), resultList, idxStart);
            }
        }

        // Step 2.4: Read other problems from mountpoint
        if (isNetworkElementCurrentProblemsSupporting12) {
            idxStart = resultList.size();
            readNetworkElementCurrentProblems12(resultList);
            debugResultList("CurrentProblems12", resultList, idxStart);
        }

        return resultList;

    }

    /**
     * Get from LayerProtocolExtensions the related generated ONF Interface PAC class which represents it.
     *
     * @param lp logical termination point
     * @return Class of InterfacePac
     */
    @Nullable
    private Class<?> getLpExtension(@Nullable Lp lp) {

        String capability = EMPTY;
        String revision = EMPTY;
        String conditionalPackage = EMPTY;
        Class<?> res = null;

        if (lp != null) {
            for (Extension e : getExtensionList(lp)) {
                String valueName = e.getValueName();
                if (valueName != null) {
                    if (valueName.contentEquals("capability")) {
                        capability = e.getValue();
                        if (capability != null) {
                            int idx = capability.indexOf('?');
                            if (idx != -1) {
                                capability = capability.substring(0, idx);
                            }
                        }
                    }
                    if (valueName.contentEquals("revision")) {
                        revision = e.getValue();
                    }
                    if (valueName.contentEquals("conditional-package")) {
                        conditionalPackage = e.getValue();
                    }
                }
            }
        }
        // QName qName =
        // org.opendaylight.yangtools.yang.common.QName.create("urn:onf:params:xml:ns:yang:microwave-model",
        // "2017-03-24", "mw-air-interface-pac").intern();
        LOG.debug("LpExtension capability={} revision={} conditionalPackage={}", capability, revision,
                conditionalPackage);
        if (capability != null && !capability.isEmpty() && !revision.isEmpty() && !conditionalPackage.isEmpty()) {
            try {
                QName qName = QName.create(capability, revision, conditionalPackage);
                res = this.microwaveModel.getClassForLtpExtension(qName);
            } catch (IllegalArgumentException e) {
                LOG.warn("Can not create QName from ({}{}{}): {}", capability, revision, conditionalPackage,
                        e.getMessage());
            }
        }
        return res;
    }

    /**
     * Read element from class that could be not available
     *
     * @param ltp layer termination point
     * @return List with extension parameters or empty list
     */
    @NonNull
    private static List<Extension> getExtensionList(@Nullable Lp ltp) {
        if (ltp != null) {
            return YangHelper.getList(ltp.nonnullExtension());
        } else {
            return EMPTYLTPEXTENSIONLIST;
        }
    }

    @Override
    public Optional<PerformanceDataLtp> getLtpHistoricalPerformanceData() throws InconsistentPMDataException {
        synchronized (getPmLock()) {
            if (pmLp != null) {
                LOG.debug("Enter query PM");
                @NonNull
                Lp lp = pmLp;
                ONFLayerProtocolName lpName = ONFLayerProtocolName.valueOf(lp.getLayerProtocolName());
                return Optional.of(this.microwaveModel.getLtpHistoricalPerformanceData(lpName, lp));
            }
            return Optional.empty();
        }
    }

    /**
     * Remove all entries from list
     */
    @Override
    public int removeAllCurrentProblemsOfNode() {
        return microwaveEventListener.removeAllCurrentProblemsOfNode(nodeId);
    }

    /**
     * Register the listener
     */
    @Override
    public void doRegisterEventListener(MountPoint mountPoint) {
        LOG.debug("Begin registration listener for Mountpoint");
        final Optional<NotificationService> optionalNotificationService =
                mountPoint.getService(NotificationService.class);
        if (optionalNotificationService.isPresent()) {
            final NotificationService notificationService = optionalNotificationService.get();
            // notificationService.registerNotificationListener(microwaveEventListener);
            listenerRegistrationresult =
                    notificationService.registerNotificationListener(microwaveModel.getNotificationListener());
            LOG.debug("End registration listener for Mountpoint Result: {}", listenerRegistrationresult);
        } else {
            LOG.error("Could not get NotificationService, hence microwave notification listener not registered");
        }
    }

    /*------------------------------------------------------------
     * private function to access database
     */

    /*-----------------------------------------------------------------------------
     * Reading problems for the networkElement V1.2
     */

    @Override
    public void close() throws Exception {
        if (listenerRegistrationresult != null) {
            listenerRegistrationresult.close();
        }
    }

    @Override
    public NetworkElementDeviceType getDeviceType() {
        return NetworkElementDeviceType.Wireless;
    }

}
