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
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.common.YangHelper;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.NetconfTimeStamp;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.NetconfTimeStampImpl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNotifications;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.OrgOpenroadmDevice;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.circuit.packs.CircuitPacks;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.interfaces.grp.Interface;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.org.openroadm.device.Xponder;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.shelf.Slots;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.shelves.Shelves;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.xponder.XpdrPort;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EventlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.PmdataEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SourceType;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Shabnam Sultana
 *
 *         Creating the openroadm device as an optical network element and writing inventory, fault, pm data to elastic
 *         search db
 *
 **/
public class OpenroadmNetworkElement extends OpenroadmNetworkElementBase {

    // variables
    private final long equipmentLevel = 1;
    private static final Logger log = LoggerFactory.getLogger(OpenroadmNetworkElement.class);
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

    private Optional<NetconfNotifications> notifications;
    private static final NetconfTimeStamp ncTimeConverter = NetconfTimeStampImpl.getConverter();
    private int counter = 1;
    // end of variables

    // constructors
    public OpenroadmNetworkElement(NetconfBindingAccessor netconfAccess, DeviceManagerServiceProvider serviceProvider) {

        super(netconfAccess, serviceProvider);

        this.notifications = netconfAccess.getNotificationAccessor();

        log.info("Create {}", OpenroadmNetworkElement.class.getSimpleName());
        this.openRdmListenerRegistrationResult = null;
        this.openRdmListener = new OpenroadmChangeNotificationListener(netconfAccessor, databaseService);
        this.opnRdmFaultListenerRegistrationResult = null;
        this.opnRdmFaultListener = new OpenroadmFaultNotificationListener(serviceProvider);
        this.opnRdmDeviceListenerRegistrationResult = null;
        this.opnRdmDeviceListener = new OpenroadmDeviceChangeNotificationListener(netconfAccessor, databaseService);
        this.circuitPacksRecord = new Hashtable<>();
        this.shelfProvisionedcircuitPacks = new Hashtable<>();
        this.openRoadmPmData = new PmDataBuilderOpenRoadm(this.netconfAccessor);
        this.initialAlarmReader = new InitialDeviceAlarmReader(this.netconfAccessor, serviceProvider);
        log.info("NodeId {}", this.netconfAccessor.getNodeId().getValue());


    }
    // end of constructors

    // public methods
    public void initialReadFromNetworkElement() {

        OrgOpenroadmDevice device = readDevice(this.netconfAccessor);
        this.opnRdmInventoryInput = new OpenroadmInventoryInput(this.netconfAccessor, device);
        log.info("openroadmMapper details{}", this.opnRdmInventoryInput.getClass().getName());
        databaseService.writeInventory(this.opnRdmInventoryInput.getInventoryData(Uint32.valueOf(equipmentLevel)));

        readShelvesData(device);
        readXpndrData(device);
        readCircuitPacketData(device);
        readInterfaceData(device);
        // Writing initial alarms at the time of device registration
        initialAlarmReader.faultService();
//        Writing historical PM data at the time of device registration
        List<PmdataEntity> pmDataEntity = new ArrayList<>();
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
    public void register() {
        initialReadFromNetworkElement();

        this.openRdmListenerRegistrationResult = netconfAccessor.doRegisterNotificationListener(openRdmListener);
        this.opnRdmFaultListenerRegistrationResult =
                netconfAccessor.doRegisterNotificationListener(opnRdmFaultListener);
        this.opnRdmDeviceListenerRegistrationResult =
                netconfAccessor.doRegisterNotificationListener(opnRdmDeviceListener);
        // Register netconf stream
        notifications.get().registerNotificationsStream(NetconfAccessor.DefaultNotificationsStream);
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

    // end of public methods

    // private methods
    private void readShelvesData(OrgOpenroadmDevice device) {
        Collection<Shelves> shelves = YangHelper.getCollection(device.getShelves());
        if (shelves != null) {
            for (Shelves shelf : shelves) {
                log.info(
                        "Shelf Name: {}, \n Serial Id:{}, \n Product Code;{}, \n Position:{}, \n EquipmetState: {}, \n Hardware version: {}"
                                + "\n ShelfType:{}, \n Vendor: {}, \n LifecycleState: {} ",
                        shelf.getShelfName(), shelf.getSerialId(), shelf.getProductCode(), shelf.getShelfPosition(),
                        shelf.getEquipmentState(), shelf.getHardwareVersion(), shelf.getShelfType(), shelf.getVendor(),
                        shelf.getLifecycleState());
                databaseService.writeInventory(
                        this.opnRdmInventoryInput.getShelvesInventory(shelf, Uint32.valueOf(equipmentLevel + 1)));
                Collection<Slots> slotList = YangHelper.getCollection(shelf.getSlots());
                if (slotList != null) {
                    for (Slots slot : slotList) {
                        if (slot.getProvisionedCircuitPack() != null) {
                            this.shelfProvisionedcircuitPacks.put(slot.getProvisionedCircuitPack(), equipmentLevel + 2);
                        }
                        log.info("Slots for the shelf: {}", shelf.getShelfName());
                        log.info("\n Slot Name: {}, \n Status: {}, \n Slot label: {} ", slot.getSlotName(),
                                slot.getSlotStatus(), slot.getLabel());
                    }
                }
            }
            log.info("size of shelfProvisionedcircuitPacks: {} ", shelfProvisionedcircuitPacks.size());
        }

    }

    private void readXpndrData(OrgOpenroadmDevice device) {
        Collection<Xponder> xponderList = YangHelper.getCollection(device.getXponder());

        if (xponderList != null) {
            for (Xponder xponder : xponderList) {

                databaseService.writeInventory(
                        this.opnRdmInventoryInput.getXponderInventory(xponder, Uint32.valueOf(equipmentLevel + 1)));
                log.info("Xponders: No.: {} , \n Port: {} ,\n Type: {}", xponder.getXpdrNumber(), xponder.getXpdrPort(),
                        xponder.getXpdrType());
                Collection<XpdrPort> xpdrportlist = YangHelper.getCollection(xponder.getXpdrPort());
                if (xpdrportlist != null) {
                    for (XpdrPort xpdrport : xpdrportlist)
                        if (xpdrport.getCircuitPackName() != null) {
                            this.shelfProvisionedcircuitPacks.put(xpdrport.getCircuitPackName(), equipmentLevel + 2);
                            log.info("Size of dict{}", this.shelfProvisionedcircuitPacks.size());
                        }
                }

            }
        }
    }

    private void readCircuitPacketData(OrgOpenroadmDevice device) {
        Collection<CircuitPacks> circuitpackCollection = YangHelper.getCollection(device.getCircuitPacks());
        List<String> cpNameList = new ArrayList<>();

        if (circuitpackCollection != null) {
//            collect all circuit pack names. Required to check for invalid parents later on
            for (CircuitPacks cp : circuitpackCollection) {
                cpNameList.add(cp.getCircuitPackName());
            }

            for (CircuitPacks cp : circuitpackCollection) {
                log.info("CP Name:{}", cp.getCircuitPackName());

                if (cp.getParentCircuitPack() == null
                        && !this.shelfProvisionedcircuitPacks.containsKey(cp.getCircuitPackName())) {
                    log.info("cp has no parent and no shelf");
                    this.circuitPacksRecord.put(cp.getCircuitPackName(), (equipmentLevel + 1));
                    databaseService.writeInventory(
                            this.opnRdmInventoryInput.getCircuitPackInventory(cp, Uint32.valueOf(equipmentLevel + 1)));
                } else {
                    //                check for missing valid parent circuit name
                    if (cp.getParentCircuitPack().getCpSlotName() != null
                            && cp.getParentCircuitPack().getCircuitPackName() == null) {

                        log.info("Cp {} has slotname of the parent circuit pack  but no parent circuit pack name",
                                cp.getCircuitPackName());
                        this.circuitPacksRecord.put(cp.getCircuitPackName(), (equipmentLevel + 3));
                        databaseService.writeInventory(this.opnRdmInventoryInput.getCircuitPackInventory(cp,
                                Uint32.valueOf(equipmentLevel + 3)));
                        databaseService.writeEventLog(writeIncorrectParentLog(cp.getCircuitPackName(), counter)
                                .setObjectId(device.getInfo().getNodeId().getValue())
                                .setId(cp.getParentCircuitPack().getCpSlotName())
                                .setNewValue("Missing parent circuit pack name").build());
                    } else if (cp.getParentCircuitPack().getCircuitPackName() != null
                            && this.shelfProvisionedcircuitPacks
                                    .containsKey(cp.getParentCircuitPack().getCircuitPackName())) {
                        log.info("Cp {} has parent circuit pack and shelf", cp.getCircuitPackName());
                        this.circuitPacksRecord.put(cp.getCircuitPackName(), (equipmentLevel + 3));
                        databaseService.writeInventory(this.opnRdmInventoryInput.getCircuitPackInventory(cp,
                                Uint32.valueOf(equipmentLevel + 3)));
                    } else {
//                      check for incorrect hierarchy
                        if (cp.getParentCircuitPack().getCircuitPackName() != null
                                && !cpNameList.contains(cp.getParentCircuitPack().getCircuitPackName())) {
                            databaseService.writeEventLog(writeIncorrectParentLog(cp.getCircuitPackName(), counter)
                                    .setObjectId(device.getInfo().getNodeId().getValue())
                                    .setId(cp.getParentCircuitPack().getCpSlotName()).build());
                        }

                        log.info("Cp has parent circuit pack but no shelf or a shelf but no parent circuit pack");
                        this.circuitPacksRecord.put(cp.getCircuitPackName(), (equipmentLevel + 2));
                        databaseService.writeInventory(this.opnRdmInventoryInput.getCircuitPackInventory(cp,
                                Uint32.valueOf(equipmentLevel + 2)));
                    }

                }
            }

        }
    }

    private void readInterfaceData(OrgOpenroadmDevice device) {
        Collection<Interface> interfaceList = YangHelper.getCollection(device.getInterface());
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
                                Uint32.valueOf(
                                        this.circuitPacksRecord.get(deviceInterface.getSupportingCircuitPackName())
                                                + 1)));
                    }
                } else {
                    databaseService.writeInventory(this.opnRdmInventoryInput.getInterfacesInventory(deviceInterface,
                            Uint32.valueOf(equipmentLevel + 1)));
                }
            }
        }
    }

    private OrgOpenroadmDevice readDevice(NetconfBindingAccessor accessor) {
        final Class<OrgOpenroadmDevice> openRoadmDev = OrgOpenroadmDevice.class;
        InstanceIdentifier<OrgOpenroadmDevice> deviceId = InstanceIdentifier.builder(openRoadmDev).build();
        return accessor.getTransactionUtils().readData(accessor.getDataBroker(),
                LogicalDatastoreType.OPERATIONAL, deviceId);
    }

    private EventlogBuilder writeIncorrectParentLog(String attributeName, Integer counter) {
        EventlogBuilder eventlogBuilder = new EventlogBuilder();
        eventlogBuilder.setAttributeName(attributeName).setCounter(counter)
                .setNodeId(this.netconfAccessor.getNodeId().getValue()).setSourceType(SourceType.Netconf)
                .setNewValue("Invalid parent circuit-pack name")
                .setTimestamp(new DateAndTime(ncTimeConverter.getTimeStamp()));

        return eventlogBuilder;

    }
    // end of private methods
}
