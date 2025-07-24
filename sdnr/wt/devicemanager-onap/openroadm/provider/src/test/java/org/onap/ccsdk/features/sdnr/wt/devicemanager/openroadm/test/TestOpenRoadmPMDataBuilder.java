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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.openroadm.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.openroadm.impl.PmDataBuilderOpenRoadm;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.TransactionUtils;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.http.org.openroadm.common.alarm.pm.types.rev191129.Direction;
import org.opendaylight.yang.gen.v1.http.org.openroadm.common.alarm.pm.types.rev191129.Location;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.circuit.pack.features.circuit.pack.components.Component;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.rev191129.HistoricalPmList;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.rev191129.historical.pm.group.HistoricalPm;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.rev191129.historical.pm.group.HistoricalPmBuilder;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.rev191129.historical.pm.group.HistoricalPmKey;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.rev191129.historical.pm.list.HistoricalPmEntry;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.rev191129.historical.pm.list.HistoricalPmEntryBuilder;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.rev191129.historical.pm.list.HistoricalPmEntryKey;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.rev191129.historical.pm.val.group.Measurement;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.rev191129.historical.pm.val.group.MeasurementBuilder;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.rev191129.historical.pm.val.group.MeasurementKey;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.types.rev191129.PmDataType;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.types.rev191129.PmGranularity;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.types.rev191129.PmNamesEnum;
import org.opendaylight.yang.gen.v1.http.org.openroadm.pm.types.rev191129.Validity;
import org.opendaylight.yang.gen.v1.http.org.openroadm.resource.types.rev191129.ResourceTypeEnum;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.units.rev200413.Celsius;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.units.rev200413.PerformanceMeasurementUnitId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint64;


public class TestOpenRoadmPMDataBuilder {

    // variables
    // end of variables
    private NetconfBindingAccessor acessor = mock(NetconfBindingAccessor.class);
    private DataBroker dataBroker = mock(DataBroker.class);
    private TransactionUtils transactionUtils = mock(TransactionUtils.class);
    //    String nodeId = "RdmA";
    private PmDataBuilderOpenRoadm pmDataBuilderORoadm;
    private NodeId nodeId = new NodeId("RoadmA");
    private HistoricalPmList historicalPmDatalist = mock(HistoricalPmList.class);
    private HistoricalPm historicalPm = mock(HistoricalPm.class);
    private PmDataType pmDataType = new PmDataType(Uint64.valueOf(67508));
    private MeasurementBuilder measurementBuilder = new MeasurementBuilder();
    private HistoricalPmBuilder historicalPmBuilder = new HistoricalPmBuilder();
    private HistoricalPmEntryBuilder historicalPmEntryBuiler = new HistoricalPmEntryBuilder();
    private List<Class<? extends PerformanceMeasurementUnitId>> performanceMeasUnitList =
            new ArrayList<Class<? extends PerformanceMeasurementUnitId>>();
    private Map<MeasurementKey, Measurement> measurementData = new HashMap<MeasurementKey, Measurement>();
    private Map<HistoricalPmKey, HistoricalPm> historicalPMCollection = new HashMap<HistoricalPmKey, HistoricalPm>();
    private Map<HistoricalPmEntryKey, HistoricalPmEntry> historicalPmEntryCollection =
            new HashMap<HistoricalPmEntryKey, HistoricalPmEntry>();


    // public methods
    @Before
    public void init() {
        when(acessor.getDataBroker()).thenReturn(dataBroker);
        when(acessor.getTransactionUtils()).thenReturn(transactionUtils);


    }

    @Test
    public void testGetPmData() {
        when(acessor.getNodeId()).thenReturn(nodeId);
        pmDataBuilderORoadm = new PmDataBuilderOpenRoadm(acessor);

        final Class<HistoricalPmList> pmDataClass = HistoricalPmList.class;
        InstanceIdentifier<HistoricalPmList> pmDataListId = InstanceIdentifier.builder(pmDataClass).build();
        when(acessor.getTransactionUtils().readData(acessor.getDataBroker(), LogicalDatastoreType.OPERATIONAL,
                pmDataListId)).thenReturn(historicalPmDatalist);

        assertEquals(historicalPmDatalist, pmDataBuilderORoadm.getPmData(acessor));

    }

    @Test
    public void testBuildPmDataEntity() {
        when(acessor.getNodeId()).thenReturn(nodeId);
        pmDataBuilderORoadm = new PmDataBuilderOpenRoadm(acessor);

        performanceMeasUnitList.add(Celsius.class);
        measurementBuilder.setBinNumber(Uint16.valueOf(24657))
                .setCompletionTime(new DateAndTime("2020-10-22T15:23:43Z")).setGranularity(PmGranularity._24Hour)
                .setPmParameterUnit("dBm").setPmParameterValue(pmDataType).setValidity(Validity.Suspect);

        measurementData.put(measurementBuilder.key(), measurementBuilder.build());
        historicalPmBuilder.setType(PmNamesEnum.SeverelyErroredSeconds).setDirection(Direction.Bidirectional)
                .setExtension("sajhsiwiduwugdhegdeuz").setLocation(Location.NearEnd).setMeasurement(measurementData);
        when(historicalPm.getMeasurement()).thenReturn(measurementData);

        historicalPMCollection.put(historicalPmBuilder.key(), historicalPmBuilder.build());

        historicalPmEntryBuiler.setPmResourceInstance(InstanceIdentifier.unsafeOf(List.of(DataObjectStep.of(
                        Component.class))).toIdentifier()).setPmResourceTypeExtension("dshjdekjdewkk")
                .setPmResourceType(ResourceTypeEnum.CircuitPack).setHistoricalPm(historicalPMCollection);

        historicalPmEntryCollection.put(historicalPmEntryBuiler.key(), historicalPmEntryBuiler.build());
        when(historicalPmDatalist.getHistoricalPmEntry()).thenReturn(historicalPmEntryCollection);

        assertNotNull(pmDataBuilderORoadm.buildPmDataEntity(historicalPmDatalist));
    }

    @Test
    public void testBuildPmDataEntity1() {
        when(acessor.getNodeId()).thenReturn(nodeId);
        pmDataBuilderORoadm = new PmDataBuilderOpenRoadm(acessor);

        performanceMeasUnitList.add(Celsius.class);
        measurementBuilder.setBinNumber(Uint16.valueOf(24657))
                .setCompletionTime(new DateAndTime("2020-10-22T15:23:43Z")).setGranularity(PmGranularity._15min)
                .setPmParameterUnit("celsius").setPmParameterValue(pmDataType).setValidity(Validity.Suspect);

        measurementData.put(measurementBuilder.key(), measurementBuilder.build());
        historicalPmBuilder.setType(PmNamesEnum.ErroredSeconds).setDirection(Direction.Rx)
                .setExtension("sajhsiwiduwugdhegdeuz").setLocation(Location.FarEnd).setMeasurement(measurementData);
        when(historicalPm.getMeasurement()).thenReturn(measurementData);

        historicalPMCollection.put(historicalPmBuilder.key(), historicalPmBuilder.build());
        historicalPmEntryBuiler.setPmResourceInstance(
                        InstanceIdentifier.unsafeOf(Arrays.asList(DataObjectStep.of(
                                Component.class))).toIdentifier()).setPmResourceTypeExtension("dshjdekjdewkk")
                .setPmResourceType(ResourceTypeEnum.Device).setHistoricalPm(historicalPMCollection);

        historicalPmEntryCollection.put(historicalPmEntryBuiler.key(), historicalPmEntryBuiler.build());
        when(historicalPmDatalist.getHistoricalPmEntry()).thenReturn(historicalPmEntryCollection);

        assertNotNull(pmDataBuilderORoadm.buildPmDataEntity(historicalPmDatalist));
    }
    // end of public methods

    // constants
    // end of constants

    // variables
    // end of variables

    // constructors
    // end of constructors

    // getters and setters
    // end of getters and setters

    // private methods
    // end of private methods

    // end of public methods

    // static methods
    // end of static methods

    // private classes
    // end of private classes
}
