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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.openroadm71.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.openroadm71.impl.OpenroadmInventoryInput;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.openroadm71.impl.OpenroadmNetworkElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.FaultService;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.TransactionUtils;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.http.org.openroadm.alarm.rev200529.ActiveAlarmList;
import org.opendaylight.yang.gen.v1.http.org.openroadm.alarm.rev200529.Severity;
import org.opendaylight.yang.gen.v1.http.org.openroadm.alarm.rev200529.active.alarm.list.ActiveAlarms;
import org.opendaylight.yang.gen.v1.http.org.openroadm.alarm.rev200529.active.alarm.list.ActiveAlarmsKey;
import org.opendaylight.yang.gen.v1.http.org.openroadm.alarm.rev200529.alarm.ProbableCauseBuilder;
import org.opendaylight.yang.gen.v1.http.org.openroadm.alarm.rev200529.alarm.ResourceBuilder;
import org.opendaylight.yang.gen.v1.http.org.openroadm.common.node.types.rev191129.NodeIdType;
import org.opendaylight.yang.gen.v1.http.org.openroadm.common.state.types.rev191129.LifecycleState;
import org.opendaylight.yang.gen.v1.http.org.openroadm.common.state.types.rev191129.State;
import org.opendaylight.yang.gen.v1.http.org.openroadm.common.types.rev200529.OpenroadmVersionType;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev200529.OrgOpenroadmDevice;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev200529.circuit.pack.ParentCircuitPack;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev200529.circuit.packs.CircuitPacks;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev200529.circuit.packs.CircuitPacksKey;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev200529.interfaces.grp.Interface;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev200529.interfaces.grp.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev200529.org.openroadm.device.Info;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev200529.org.openroadm.device.InfoBuilder;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev200529.org.openroadm.device.Xponder;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev200529.org.openroadm.device.XponderKey;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev200529.shelf.Slots;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev200529.shelf.SlotsKey;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev200529.shelves.Shelves;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev200529.shelves.ShelvesKey;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.types.rev191129.NodeTypes;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.types.rev191129.XpdrNodeTypes;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.rev200529.HistoricalPmList;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.rev200529.historical.pm.group.HistoricalPm;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.rev200529.historical.pm.group.HistoricalPmKey;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.rev200529.historical.pm.list.HistoricalPmEntry;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.rev200529.historical.pm.list.HistoricalPmEntryKey;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.rev200529.historical.pm.val.group.Measurement;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.rev200529.historical.pm.val.group.MeasurementKey;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.types.rev200327.PmDataType;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.types.rev200327.PmGranularity;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.types.rev200327.PmNamesEnum;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.types.rev200327.Validity;
import org.opendaylight.yang.gen.v1.http.org.openroadm.probablecause.rev200529.ProbableCauseEnum;
import org.opendaylight.yang.gen.v1.http.org.openroadm.resource.rev200529.resource.DeviceBuilder;
import org.opendaylight.yang.gen.v1.http.org.openroadm.resource.types.rev191129.ResourceTypeEnum;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

public class TestOpenRoadmNetworkElement {

    NetconfBindingAccessor accessor = mock(NetconfBindingAccessor.class);
    DeviceManagerServiceProvider serviceProvider = mock(DeviceManagerServiceProvider.class);
    DataProvider dataprovider = mock(DataProvider.class);
    Capabilities capabilities = mock(Capabilities.class);
    TransactionUtils transactionUtils = mock(TransactionUtils.class);
    DataBroker dataBroker = mock(DataBroker.class);
    FaultService faultService = mock(FaultService.class);
    OrgOpenroadmDevice device;
    OpenroadmInventoryInput inventoryData;
    long level = 1;
    private Shelves shelf = mock(Shelves.class);
    private @Nullable Map<ShelvesKey, Shelves> shelfList;
    private CircuitPacks cp, cp1, cp2, cp3;
    private Interface interfaces = mock(Interface.class);
    private Xponder xpdr = mock(Xponder.class);
    private IpAddress ipAddress = new IpAddress(new Ipv4Address("127.0.0.11"));
    private Info info = new InfoBuilder().setNodeId(NodeIdType.getDefaultInstance("zNhe2i5")).setClli("NodeB")
            .setSerialId("0002").setModel("model2").setVendor("VendorA").setCurrentIpAddress(ipAddress)
            .setCurrentIpAddress(ipAddress).setCurrentDefaultGateway(new IpAddress(new Ipv4Address("127.0.0.20")))
            .setCurrentDefaultGateway(new IpAddress(new Ipv4Address("127.0.0.20"))).setNodeType(NodeTypes.Rdm)
            .setCurrentDatetime(new DateAndTime("2017-10-22T15:23:43Z")).setSoftwareVersion("swversion1234")
            .setCurrentPrefixLength(Uint8.valueOf(45)).setMaxDegrees(Uint16.valueOf(56)).setMaxSrgs(Uint16.valueOf(251))
            .setMaxNumBin15minHistoricalPm(Uint16.valueOf(324)).setMaxNumBin24hourHistoricalPm(Uint16.valueOf(142))
            .setOpenroadmVersion(OpenroadmVersionType._20).build();
    private ActiveAlarmList alarmList = mock(ActiveAlarmList.class);
    private ActiveAlarms activeAlarms = mock(ActiveAlarms.class);
    private HistoricalPmList pmDataList = mock(HistoricalPmList.class);
    private HistoricalPm historicalPm = mock(HistoricalPm.class);

    @Before
    public void init() {
        when(accessor.getCapabilites()).thenReturn(capabilities);
        when(serviceProvider.getFaultService()).thenReturn(faultService);
        when(serviceProvider.getDataProvider()).thenReturn(dataprovider);
        NodeId nNodeId = new NodeId("RoadmA");
        when(accessor.getNodeId()).thenReturn(nNodeId);
        when(accessor.getDataBroker()).thenReturn(dataBroker);
        when(accessor.getTransactionUtils()).thenReturn(transactionUtils);
        when(accessor.getCapabilites().isSupportingNamespaceAndRevision(OrgOpenroadmDevice.QNAME)).thenReturn(true);
        final Class<OrgOpenroadmDevice> openRoadmDev = OrgOpenroadmDevice.class;
        // Reading data from device
        InstanceIdentifier<OrgOpenroadmDevice> deviceId = InstanceIdentifier.create(openRoadmDev);
        device = mock(OrgOpenroadmDevice.class);
        when(accessor.getTransactionUtils().readData(accessor.getDataBroker(), LogicalDatastoreType.OPERATIONAL,
                deviceId)).thenReturn(device);

        when(device.getInfo()).thenReturn(info);
        inventoryData = new OpenroadmInventoryInput(accessor, device);
        // Reading the shelfs data;
        when(shelf.getShelfPosition()).thenReturn("10");
        when(shelf.getOperationalState()).thenReturn(State.InService);
        when(shelf.getSerialId()).thenReturn("nodeid-1");
        when(shelf.getShelfName()).thenReturn("Shelf1");
        when(shelf.getShelfType()).thenReturn("Shelf");
        when(shelf.getClei()).thenReturn("1234567890");
        when(shelf.getVendor()).thenReturn("vendorA");
        when(shelf.getModel()).thenReturn("1");
        when(shelf.getHardwareVersion()).thenReturn("0.1");
        when(shelf.getManufactureDate()).thenReturn(new DateAndTime("2017-10-22T15:23:43Z"));
        @Nullable
        Map<SlotsKey, Slots> slotList = null;
        Slots slots = mock(Slots.class);
        when(slots.getLabel()).thenReturn("Slot56746");
        when(slots.getSlotName()).thenReturn("slotofRoadmA");
        when(slots.getProvisionedCircuitPack()).thenReturn("1/0");
        slotList = new HashMap<>();
        slotList.put(slots.key(), slots);
        when(shelf.getSlots()).thenReturn(slotList);
        shelfList = new HashMap<>();
        shelfList.put(shelf.key(), shelf);
        when(device.getShelves()).thenReturn(shelfList);

        // Reading data from CircuitPacks
        cp = mock(CircuitPacks.class);
        when(cp.getCircuitPackName()).thenReturn("1/0");
        when(cp.getVendor()).thenReturn("VendorA");
        when(cp.getModel()).thenReturn("Model1");
        when(cp.getSerialId()).thenReturn("46277sgh6");
        when(cp.getClei()).thenReturn("136268785");
        when(cp.getHardwareVersion()).thenReturn("0.1");
        when(cp.getType()).thenReturn("WSS");
        when(cp.getProductCode()).thenReturn("oooooo");
        when(cp.getCircuitPackMode()).thenReturn("inServiceMode");

        ParentCircuitPack parentCp = mock(ParentCircuitPack.class);
        when(parentCp.getCircuitPackName()).thenReturn("1/0");
        when(parentCp.getCpSlotName()).thenReturn("Slot1");
        cp1 = mock(CircuitPacks.class);
        when(cp1.getCircuitPackName()).thenReturn("1/0 EThernet");
        when(cp1.getVendor()).thenReturn("VendorA");
        when(cp1.getModel()).thenReturn("Model1678");
        when(cp1.getSerialId()).thenReturn("4627dgs7sgh6");
        when(cp1.getClei()).thenReturn("1362d68785");
        when(cp1.getHardwareVersion()).thenReturn("0.1");
        when(cp1.getType()).thenReturn("EthPlug");
        when(cp1.getProductCode()).thenReturn("oooooo");
        when(cp1.getCircuitPackMode()).thenReturn("inServiceMode");
        when(cp1.getParentCircuitPack()).thenReturn(parentCp);

        cp2 = mock(CircuitPacks.class);
        when(cp2.getCircuitPackName()).thenReturn("2/0");
        when(cp2.getVendor()).thenReturn("VendorA");
        when(cp2.getModel()).thenReturn("Model1678");
        when(cp2.getSerialId()).thenReturn("4sads7sgh6");
        when(cp2.getClei()).thenReturn("1wew362d68785");
        when(cp2.getHardwareVersion()).thenReturn("0.1");
        when(cp2.getType()).thenReturn("WSS");
        when(cp2.getProductCode()).thenReturn("osooooo");
        when(cp2.getCircuitPackMode()).thenReturn("inServiceMode");

        cp3 = mock(CircuitPacks.class);
        when(parentCp.getCircuitPackName()).thenReturn("2/0");
        when(parentCp.getCpSlotName()).thenReturn("Slot1");
        when(cp3.getCircuitPackName()).thenReturn("2/0  OCS");
        when(cp3.getVendor()).thenReturn("VendorA");
        when(cp3.getModel()).thenReturn("Model1678");
        when(cp3.getSerialId()).thenReturn("dswsads7sgh6");
        when(cp3.getClei()).thenReturn("1ew62d68785");
        when(cp3.getHardwareVersion()).thenReturn("0.1");
        when(cp3.getType()).thenReturn("OCS Plug");
        when(cp3.getProductCode()).thenReturn("osooooo");
        when(cp3.getCircuitPackMode()).thenReturn("inServiceMode");
        when(cp3.getParentCircuitPack()).thenReturn(parentCp);
        @Nullable
        Map<CircuitPacksKey, CircuitPacks> cpList = new HashMap<>();
        cpList.put(cp.key(), cp);
        cpList.put(cp1.key(), cp1);
        cpList.put(cp2.key(), cp2);
        cpList.put(cp3.key(), cp3);
        when(device.getCircuitPacks()).thenReturn(cpList);

        // Reading Interface Data
        when(interfaces.getName()).thenReturn("1GE-interface-1");
        when(interfaces.getDescription()).thenReturn("Ethernet Interface");
        when(interfaces.getSupportingCircuitPackName()).thenReturn("1/0 EThernet");
        @Nullable
        Map<InterfaceKey, Interface> interfacesList = new HashMap<>();
        interfacesList.put(interfaces.key(), interfaces);
        when(device.getInterface()).thenReturn(interfacesList);

        // Reading Xponder Data
        when(xpdr.getXpdrNumber()).thenReturn(Uint16.valueOf(1));
        when(xpdr.getXpdrType()).thenReturn(XpdrNodeTypes.Mpdr);
        when(xpdr.getLifecycleState()).thenReturn(LifecycleState.Deployed);
        @Nullable
        Map<XponderKey, Xponder> xpnderList = new HashMap<>();
        xpnderList.put(xpdr.key(), xpdr);
        when(device.getXponder()).thenReturn(xpnderList);

        // Read initial Alarm data
        final Class<ActiveAlarmList> classAlarm = ActiveAlarmList.class;
        InstanceIdentifier<ActiveAlarmList> alarmDataIid = InstanceIdentifier.builder(classAlarm).build();
        when(accessor.getTransactionUtils().readData(accessor.getDataBroker(), LogicalDatastoreType.OPERATIONAL,
                alarmDataIid)).thenReturn(alarmList);
        when(activeAlarms.getId()).thenReturn("Alarm1");
        when(activeAlarms.getCircuitId()).thenReturn("1/0");
        when(activeAlarms.getRaiseTime()).thenReturn(new DateAndTime("2017-10-22T15:23:43Z"));
        when(activeAlarms.getSeverity()).thenReturn(Severity.Critical);
        when(activeAlarms.getProbableCause())
                .thenReturn(new ProbableCauseBuilder().setCause(ProbableCauseEnum.AutomaticLaserShutdown).build());
        when(activeAlarms.getAdditionalDetail()).thenReturn("LaserShutdown");
        when(activeAlarms.getResource()).thenReturn(new ResourceBuilder()
                .setDevice(new DeviceBuilder().setNodeId(NodeIdType.getDefaultInstance("zNhe2i5")).build()).build());
        @Nullable
        Map<ActiveAlarmsKey, ActiveAlarms> activeAlarmlist = new HashMap<>();
        activeAlarmlist.put(activeAlarms.key(), activeAlarms);
        when(alarmList.getActiveAlarms()).thenReturn(activeAlarmlist);

        // Read PM Data
        final Class<HistoricalPmList> pmDataClass = HistoricalPmList.class;
        InstanceIdentifier<HistoricalPmList> pmDataIid = InstanceIdentifier.builder(pmDataClass).build();
        when(accessor.getTransactionUtils().readData(accessor.getDataBroker(), LogicalDatastoreType.OPERATIONAL,
                pmDataIid)).thenReturn(pmDataList);

        Measurement measurement = mock(Measurement.class);
        PmDataType pmDataType = mock(PmDataType.class);
        when(pmDataType.getDecimal64()).thenReturn(Decimal64.valueOf(1,2425425));
        when(measurement.getBinNumber()).thenReturn(Uint16.valueOf(1452));
        when(measurement.getCompletionTime()).thenReturn(new DateAndTime("2018-10-22T15:23:43Z"));
        when(measurement.getGranularity()).thenReturn(PmGranularity._24Hour);
        when(measurement.getPmParameterUnit()).thenReturn("6824545199534863756");
        when(measurement.getPmParameterValue()).thenReturn(pmDataType);
        when(measurement.getValidity()).thenReturn(Validity.Partial);

        @Nullable
        Map<MeasurementKey, Measurement> measurementList = new HashMap<>();
        measurementList.put(measurement.key(), measurement);
        when(historicalPm.getMeasurement()).thenReturn(measurementList);
        when(historicalPm.getType()).thenReturn(PmNamesEnum.ErroredSeconds);
        when(historicalPm.getExtension()).thenReturn("OpticalPowerOutput");
        @Nullable
        Map<HistoricalPmKey, HistoricalPm> historicalPmList = new HashMap<>();
        historicalPmList.put(historicalPm.key(), historicalPm);
        HistoricalPmEntry histPmEntry = mock(HistoricalPmEntry.class);
        when(histPmEntry.getHistoricalPm()).thenReturn(historicalPmList);
        when(histPmEntry.getPmResourceType()).thenReturn(ResourceTypeEnum.CircuitPack);
        @Nullable
        Map<HistoricalPmEntryKey, HistoricalPmEntry> histPmList = new HashMap<>();
        histPmList.put(histPmEntry.key(), histPmEntry);
        when(pmDataList.getHistoricalPmEntry()).thenReturn(histPmList);

    }



    @Test
    public void test() {
        OpenroadmNetworkElement optionalNe = new OpenroadmNetworkElement(accessor, serviceProvider);
        optionalNe.initialReadFromNetworkElement();
        verify(dataprovider).writeInventory(accessor.getNodeId().getValue(),
                Arrays.asList(inventoryData.getInventoryData(Uint32.valueOf(0)),
                        inventoryData.getShelvesInventory(shelf, Uint32.valueOf(1)),
                        inventoryData.getXponderInventory(xpdr, Uint32.valueOf(1)),
                        inventoryData.getCircuitPackInventory(cp3, Uint32.valueOf(2))));
    }

}
