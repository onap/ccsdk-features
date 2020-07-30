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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.openroadm.impl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElementService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.OrgOpenroadmDevice;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.circuit.packs.CircuitPacks;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.interfaces.grp.Interface;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.org.openroadm.device.Xponder;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.shelf.Slots;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.shelves.Shelves;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.xponder.XpdrPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.NetworkElementDeviceType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.PmdataEntity;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Shabnam Sultana
 *
 *         Creating the openroadm device as an optical network element and writing inventory, fault, pm data to elastic
 *         search db
 *
 **/
public class OpenroadmNetworkElement implements NetworkElement {

    // variables
    private final long equipmentLevel = 0;
    private static final Logger log = LoggerFactory.getLogger(OpenroadmNetworkElement.class);
    private final NetconfAccessor netconfAccessor;
    private final DataProvider databaseService;
    private Hashtable<String, Long> circuitPacksRecord;
    private Hashtable<String, Long> shelfProvisionedcircuitPacks;
    private ListenerRegistration<NotificationListener> openRdmListenerRegistrationResult;
    private @NonNull final OpenroadmChangeNotificationListener openRdmListener;
    private ListenerRegistration<NotificationListener> opnRdmFaultListenerRegistrationResult;
    private @NonNull OpenroadmFaultNotificationListener opnRdmFaultListener;
    private ListenerRegistration<NotificationListener> opnRdmDeviceListenerRegistrationResult;
    private OpenroadmDeviceChangeNotificationListener opnRdmDeviceListener;
    private OpenroadmInventoryInput opnRdmInventoryInput;
    private PmDataBuilderOpenRoadm openRoadmPmData;
    private InitialDeviceAlarmReader initialAlarmReader;
    private List<PmdataEntity> pmDataEntity = new ArrayList<PmdataEntity>();
    // end of variables

    // constructors
    public OpenroadmNetworkElement(NetconfAccessor netconfAccess, DeviceManagerServiceProvider serviceProvider) {

        log.info("Create {}", OpenroadmNetworkElement.class.getSimpleName());
        this.netconfAccessor = netconfAccess;
        this.databaseService = serviceProvider.getDataProvider();
        this.openRdmListenerRegistrationResult = null;
        this.openRdmListener = new OpenroadmChangeNotificationListener(netconfAccessor, databaseService);
        this.opnRdmFaultListenerRegistrationResult = null;
        this.opnRdmFaultListener = new OpenroadmFaultNotificationListener(netconfAccessor, serviceProvider);
        this.opnRdmDeviceListenerRegistrationResult = null;
        this.opnRdmDeviceListener = new OpenroadmDeviceChangeNotificationListener(netconfAccessor, databaseService);
        this.opnRdmInventoryInput = new OpenroadmInventoryInput(netconfAccess, readDevice(netconfAccess));
        this.circuitPacksRecord = new Hashtable<String, Long>();
        this.shelfProvisionedcircuitPacks = new Hashtable<String, Long>();
        this.openRoadmPmData = new PmDataBuilderOpenRoadm(this.netconfAccessor);
        this.initialAlarmReader = new InitialDeviceAlarmReader(this.netconfAccessor, serviceProvider);
        log.info("NodeId {}", this.netconfAccessor.getNodeId().getValue());
        log.info("oScaMapper details{}", this.opnRdmInventoryInput.getClass().getName());

    }
    // end of constructors

    // public methods
    public void initialReadFromNetworkElement() {
        OrgOpenroadmDevice device = readDevice(this.netconfAccessor);
        databaseService.writeInventory(this.opnRdmInventoryInput.getInventoryData(equipmentLevel));

        readShelvesData(device);
        readXpndrData(device);
        readCircuitPacketData(device);
        readInterfaceData(device);
        // Writing initial alarms at the time of device registration
        initialAlarmReader.faultService();
        // faultEventListener.initCurrentProblemStatus(this.netconfAccessor.getNodeId(),
        // oScaFaultListener.writeFaultData(this.sequenceNumber));
        // oScaFaultListener.writeAlarmLog(oScaFaultListener.writeFaultData(this.sequenceNumber));
        // this.sequenceNumber = this.sequenceNumber + 1;

        pmDataEntity = this.openRoadmPmData.buildPmDataEntity(this.openRoadmPmData.getPmData(this.netconfAccessor));
        if (!pmDataEntity.isEmpty()) {
            this.databaseService.doWritePerformanceData(pmDataEntity);
            log.info("PmDatEntity is written with size {}", pmDataEntity.size());
            for (PmdataEntity ent : pmDataEntity) {
                log.info("GetNode: {}, granPeriod: {}", ent.getNodeName(), ent.getGranularityPeriod().getName());
            }
        } else {
            log.info("PmDatEntity is empty");
        }

    }

    @Override
    public NetworkElementDeviceType getDeviceType() {
        return NetworkElementDeviceType.OROADM;
    }

    @Override
    public void register() {
        initialReadFromNetworkElement();

        this.openRdmListenerRegistrationResult = netconfAccessor.doRegisterNotificationListener(openRdmListener);
        this.opnRdmFaultListenerRegistrationResult =
                netconfAccessor.doRegisterNotificationListener(opnRdmFaultListener);
        this.opnRdmDeviceListenerRegistrationResult =
                netconfAccessor.doRegisterNotificationListener(opnRdmDeviceListener);
        // Register netconf stream
        netconfAccessor.registerNotificationsStream(NetconfAccessor.DefaultNotificationsStream);

    }

    @Override
    public void deregister() {
        if (openRdmListenerRegistrationResult != null) {
            this.openRdmListenerRegistrationResult.close();
        }
        if (opnRdmFaultListenerRegistrationResult != null) {
            this.opnRdmFaultListenerRegistrationResult.close();
        }
        if (opnRdmDeviceListenerRegistrationResult != null) {
            this.opnRdmDeviceListenerRegistrationResult.close();
        }
    }

    @Override
    public NodeId getNodeId() {
        return netconfAccessor.getNodeId();
    }

    @Override
    public <L extends NetworkElementService> Optional<L> getService(Class<L> clazz) {
        return Optional.empty();
    }

    @Override
    public void warmstart() {}

    @Override
    public Optional<NetconfAccessor> getAcessor() {
        return Optional.of(netconfAccessor);
    }
    // end of public methods

    // private methods
    private void readShelvesData(OrgOpenroadmDevice device) {
        List<Shelves> shelves = device.getShelves();
        if (shelves != null) {
            for (Shelves shelf : shelves) {
                log.info(
                        "Shelf Name: {}, \n Serial Id:{}, \n Product Code;{}, \n Position:{}, \n EquipmetState: {}, \n Hardware version: {}"
                                + "\n ShelfType:{}, \n Vendor: {}, \n LifecycleState: {} ",
                        shelf.getShelfName(), shelf.getSerialId(), shelf.getProductCode(), shelf.getShelfPosition(),
                        shelf.getEquipmentState(), shelf.getHardwareVersion(), shelf.getShelfType(), shelf.getVendor(),
                        shelf.getLifecycleState());
                databaseService
                        .writeInventory(this.opnRdmInventoryInput.getShelvesInventory(shelf, equipmentLevel + 1));
                List<Slots> slotList = shelf.getSlots();
                if (slotList != null) {
                    for (Slots slot : slotList) {
                        if (!slot.getProvisionedCircuitPack().isEmpty()) {
                            this.shelfProvisionedcircuitPacks.put(slot.getProvisionedCircuitPack(), equipmentLevel + 2);
                        }
                        log.info("Slots for the shelf: {}", shelf.getShelfName());
                        log.info("\n Slot Name: {}, \n Status: {}, \n Slot label: {} ", slot.getSlotName(),
                                slot.getSlotStatus(), slot.getLabel());
                    }
                }
                log.info("size of shelfProvisionedcircuitPacks: {} ", shelfProvisionedcircuitPacks.size());

            }

        }
    }

    private void readXpndrData(OrgOpenroadmDevice device) {
        List<Xponder> xponderList = device.getXponder();
        if (xponderList != null) {
            for (Xponder xponder : xponderList) {
                databaseService
                        .writeInventory(this.opnRdmInventoryInput.getXponderInventory(xponder, equipmentLevel + 1));
                log.info("Xponders: No.: {} , \n Port: {} ,\n Type: {}", xponder.getXpdrNumber(), xponder.getXpdrPort(),
                        xponder.getXpdrType());
                List<XpdrPort> xpdrportlist = xponder.getXpdrPort();
                if (xpdrportlist != null) {
                    for (XpdrPort xpdrport : xpdrportlist)
                        if (!xpdrport.getCircuitPackName().isEmpty()) {
                            this.shelfProvisionedcircuitPacks.put(xpdrport.getCircuitPackName(), equipmentLevel + 2);
                            log.info("Size of dict{}", this.shelfProvisionedcircuitPacks.size());
                        }


                }
            }

        }
    }

    private void readCircuitPacketData(OrgOpenroadmDevice device) {
        List<CircuitPacks> circuitpacklist = device.getCircuitPacks();

        if (circuitpacklist != null) {
            for (CircuitPacks cp : circuitpacklist) {
                log.info("CP Name:{}", cp.getCircuitPackName());
                if (!this.shelfProvisionedcircuitPacks.isEmpty()
                        && this.shelfProvisionedcircuitPacks.containsKey(cp.getCircuitPackName())) {

                    this.circuitPacksRecord.put(cp.getCircuitPackName(), (equipmentLevel + 2));
                    databaseService.writeInventory(this.opnRdmInventoryInput.getCircuitPackInventory(cp,
                            this.shelfProvisionedcircuitPacks.get(cp.getCircuitPackName())));
                    log.info("shelf has circuit pack");
                } else {
                    if (cp.getParentCircuitPack() == null) {
                        this.circuitPacksRecord.put(cp.getCircuitPackName(), (equipmentLevel + 1));
                        databaseService.writeInventory(
                                this.opnRdmInventoryInput.getCircuitPackInventory(cp, equipmentLevel + 1));
                        log.info("Cp has no parent circuit pack and no shelf");

                    } else {
                        if (this.shelfProvisionedcircuitPacks
                                .containsKey(cp.getParentCircuitPack().getCircuitPackName())) {
                            this.circuitPacksRecord.put(cp.getCircuitPackName(), (equipmentLevel + 3));
                            databaseService.writeInventory(
                                    this.opnRdmInventoryInput.getCircuitPackInventory(cp, equipmentLevel + 3));
                            log.info("Cp {} has parent circuit pack and shelf", cp.getCircuitPackName());
                        } else {
                            this.circuitPacksRecord.put(cp.getCircuitPackName(), (equipmentLevel + 2));
                            databaseService.writeInventory(
                                    this.opnRdmInventoryInput.getCircuitPackInventory(cp, equipmentLevel + 2));
                            log.info("Cp {} has parent circuit pack but no shelf", cp.getCircuitPackName());

                        }


                    }
                }


            }

        }

    }

    private void readInterfaceData(OrgOpenroadmDevice device) {
        List<Interface> interfaceList = device.getInterface();
        if (interfaceList != null) {
            for (Interface deviceInterface : interfaceList) {

                log.info("\n InterfaceName: {}", deviceInterface.getName());
                log.info("Supporting CP {}", this.circuitPacksRecord.size());
                for (String s : this.circuitPacksRecord.keySet()) {
                    log.info("{} value {}", s, this.circuitPacksRecord.get(s));
                }
                log.info("Interface {} and their supporting CP {}", deviceInterface.getName(),
                        deviceInterface.getSupportingCircuitPackName());


                if (deviceInterface.getSupportingCircuitPackName() != null) {
                    if (this.circuitPacksRecord.containsKey(deviceInterface.getSupportingCircuitPackName())) {
                        databaseService.writeInventory(this.opnRdmInventoryInput.getInterfacesInventory(deviceInterface,
                                this.circuitPacksRecord.get(deviceInterface.getSupportingCircuitPackName()) + 1));
                    }

                } else {
                    databaseService.writeInventory(
                            this.opnRdmInventoryInput.getInterfacesInventory(deviceInterface, equipmentLevel + 1));
                }
            }
        }

    }

    private OrgOpenroadmDevice readDevice(NetconfAccessor accessor) {

        final Class<OrgOpenroadmDevice> openRoadmDev = OrgOpenroadmDevice.class;
        InstanceIdentifier<OrgOpenroadmDevice> deviceId = InstanceIdentifier.builder(openRoadmDev).build();

        OrgOpenroadmDevice device = accessor.getTransactionUtils().readData(accessor.getDataBroker(),
                LogicalDatastoreType.OPERATIONAL, deviceId);

        return device;

    }
    // end of private methods


}
