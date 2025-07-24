/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property. All rights reserved.
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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.microwave.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.microwave.WrapperMicrowaveModelRev181010;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.util.ONFLayerProtocolName;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.FaultData;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.util.InconsistentPMDataException;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.TransactionUtils;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.YangToolsMapperHelper;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.UniversalId;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.logical.termination.point.g.Lp;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.logical.termination.point.g.LpBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.g._874._1.model.rev170320.GranularityPeriodType;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.MwAirInterfaceDiversityPac;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.MwAirInterfaceDiversityPacKey;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.MwAirInterfacePac;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.MwAirInterfacePacKey;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.MwEthernetContainerPac;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.MwEthernetContainerPacKey;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.MwHybridMwStructurePac;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.MwHybridMwStructurePacKey;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.MwPureEthernetStructurePac;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.MwPureEthernetStructurePacKey;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.MwTdmContainerPac;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.MwTdmContainerPacKey;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.SeverityType;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.air._interface.current.problems.g.CurrentProblemList;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.air._interface.current.problems.g.CurrentProblemListBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.air._interface.historical.performance.type.g.PerformanceData;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.air._interface.historical.performance.type.g.PerformanceDataBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.mw.air._interface.diversity.pac.AirInterfaceDiversityCurrentProblems;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.mw.air._interface.diversity.pac.AirInterfaceDiversityCurrentProblemsBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.mw.air._interface.pac.AirInterfaceConfiguration;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.mw.air._interface.pac.AirInterfaceConfigurationBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.mw.air._interface.pac.AirInterfaceCurrentProblems;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.mw.air._interface.pac.AirInterfaceCurrentProblemsBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.mw.air._interface.pac.AirInterfaceHistoricalPerformances;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.mw.air._interface.pac.AirInterfaceHistoricalPerformancesBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.mw.ethernet.container.pac.EthernetContainerCurrentProblems;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.mw.ethernet.container.pac.EthernetContainerCurrentProblemsBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.mw.ethernet.container.pac.EthernetContainerHistoricalPerformances;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.mw.ethernet.container.pac.EthernetContainerHistoricalPerformancesBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.mw.hybrid.mw.structure.pac.HybridMwStructureCurrentProblems;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.mw.hybrid.mw.structure.pac.HybridMwStructureCurrentProblemsBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.mw.pure.ethernet.structure.pac.PureEthernetStructureCurrentProblems;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.mw.pure.ethernet.structure.pac.PureEthernetStructureCurrentProblemsBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.mw.tdm.container.pac.TdmContainerCurrentProblems;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.mw.tdm.container.pac.TdmContainerCurrentProblemsBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

@SuppressWarnings("deprecation")
public class TestWrapperMicrowaveModelRev181010 {

    NetconfBindingAccessor accessor;
    DeviceManagerServiceProvider serviceProvider;
    FaultData resultList;
    UniversalId uid;
    TransactionUtils transactionUtils;
    DataBroker dataBroker;

    InstanceIdentifier<AirInterfaceCurrentProblems> mwAirInterfaceIID;
    @NonNull
    AirInterfaceCurrentProblems airInterfaceCurrentProblems;

    InstanceIdentifier<EthernetContainerCurrentProblems> mwEthInterfaceIID;
    @NonNull
    EthernetContainerCurrentProblems ethernetContainerCurrentProblems;

    @Before
    public void init() {
        accessor = mock(NetconfBindingAccessor.class);
        serviceProvider = mock(DeviceManagerServiceProvider.class);
        resultList = mock(FaultData.class);
        transactionUtils = mock(TransactionUtils.class);
        dataBroker = mock(DataBroker.class);

        uid = new UniversalId("ABCD");

        mwAirInterfaceIID = InstanceIdentifier.builder(MwAirInterfacePac.class, new MwAirInterfacePacKey(uid))
                .child(AirInterfaceCurrentProblems.class).build();
        List<CurrentProblemList> currentProblemList =
                Arrays.asList(new CurrentProblemListBuilder().setProblemName("Loss of Signal")
                        .setProblemSeverity(SeverityType.Critical).setSequenceNumber(1).setTimeStamp(null).build());
        airInterfaceCurrentProblems =
                new AirInterfaceCurrentProblemsBuilder().setCurrentProblemList(YangToolsMapperHelper.toMap(currentProblemList)).build();

        mwEthInterfaceIID = InstanceIdentifier.builder(MwEthernetContainerPac.class, new MwEthernetContainerPacKey(uid))
                .child(EthernetContainerCurrentProblems.class).build();
        List<org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.ethernet.container.current.problems.g.CurrentProblemList> ethCurrentProblemsList =
                Arrays.asList(
                        new org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.ethernet.container.current.problems.g.CurrentProblemListBuilder()
                                .setProblemName("Link Negotiation Unsuccessful")
                                .setProblemSeverity(SeverityType.Critical).setSequenceNumber(1).setTimeStamp(null)
                                .build());
        ethernetContainerCurrentProblems =
                new EthernetContainerCurrentProblemsBuilder().setCurrentProblemList(YangToolsMapperHelper.toMap(ethCurrentProblemsList)).build();

        NodeId nNodeId = new NodeId("nSky");
        when(accessor.getNodeId()).thenReturn(nNodeId);
        when(accessor.getTransactionUtils()).thenReturn(transactionUtils);
        when(accessor.getDataBroker()).thenReturn(dataBroker);

    }

    @Test
    public void testMWAirInterfaceWithProblems() {
        when(accessor.getTransactionUtils().readData(accessor.getDataBroker(), LogicalDatastoreType.OPERATIONAL,
                mwAirInterfaceIID)).thenReturn(airInterfaceCurrentProblems);

        WrapperMicrowaveModelRev181010 wrapperMicrowaveModelRev181010 =
                new WrapperMicrowaveModelRev181010(accessor, serviceProvider);
        wrapperMicrowaveModelRev181010.readTheFaultsOfMicrowaveModel(ONFLayerProtocolName.MWAIRINTERFACE, null, uid,
                resultList);
    }

    @Test
    public void testMWAirInterfaceWithoProblems() {
        when(accessor.getTransactionUtils().readData(accessor.getDataBroker(), LogicalDatastoreType.OPERATIONAL,
                mwAirInterfaceIID)).thenReturn(null);

        WrapperMicrowaveModelRev181010 wrapperMicrowaveModelRev181010 =
                new WrapperMicrowaveModelRev181010(accessor, serviceProvider);
        wrapperMicrowaveModelRev181010.readTheFaultsOfMicrowaveModel(ONFLayerProtocolName.MWAIRINTERFACE, null, uid,
                resultList);
    }

    @Test
    public void testEthernetContainer12WithProblems() {
        when(accessor.getTransactionUtils().readData(accessor.getDataBroker(), LogicalDatastoreType.OPERATIONAL,
                mwEthInterfaceIID)).thenReturn(ethernetContainerCurrentProblems);

        WrapperMicrowaveModelRev181010 wrapperMicrowaveModelRev181010 =
                new WrapperMicrowaveModelRev181010(accessor, serviceProvider);
        wrapperMicrowaveModelRev181010.readTheFaultsOfMicrowaveModel(ONFLayerProtocolName.ETHERNETCONTAINER12, null,
                uid, resultList);
    }

    @Test
    public void testEthernetContainer12WithNoProblems() {
        when(accessor.getTransactionUtils().readData(accessor.getDataBroker(), LogicalDatastoreType.OPERATIONAL,
                mwEthInterfaceIID)).thenReturn(null);

        WrapperMicrowaveModelRev181010 wrapperMicrowaveModelRev181010 =
                new WrapperMicrowaveModelRev181010(accessor, serviceProvider);
        wrapperMicrowaveModelRev181010.readTheFaultsOfMicrowaveModel(ONFLayerProtocolName.ETHERNETCONTAINER12, null,
                uid, resultList);
    }

    @Test
    public void testTdmContainer12WithProblems() throws Exception {
        final Class<MwTdmContainerPac> clazzPac = MwTdmContainerPac.class;
        final Class<MwTdmContainerPacKey> clazzPacKey = MwTdmContainerPacKey.class;
        final Class<TdmContainerCurrentProblems> clazzProblems = TdmContainerCurrentProblems.class;

        Constructor<MwTdmContainerPacKey> cons = clazzPacKey.getConstructor(UniversalId.class); // Avoid new()
        InstanceIdentifier<TdmContainerCurrentProblems> mwEthInterfaceIID =
                InstanceIdentifier.builder(clazzPac, cons.newInstance(uid)).child(clazzProblems).build();

        List<org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.tdm.container.current.problems.g.CurrentProblemList> currentProblemList =
                Arrays.asList(
                        new org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.tdm.container.current.problems.g.CurrentProblemListBuilder()
                                .setProblemName("Loss of Payload").setProblemSeverity(SeverityType.Major)
                                .setSequenceNumber(2).setTimeStamp(null).build());
        TdmContainerCurrentProblems tdmInterfaceCurrentProblems =
                new TdmContainerCurrentProblemsBuilder().setCurrentProblemList(YangToolsMapperHelper.toMap(currentProblemList)).build();
        when(accessor.getTransactionUtils().readData(accessor.getDataBroker(), LogicalDatastoreType.OPERATIONAL,
                mwEthInterfaceIID)).thenReturn(tdmInterfaceCurrentProblems);

        WrapperMicrowaveModelRev181010 wrapperMicrowaveModelRev181010 =
                new WrapperMicrowaveModelRev181010(accessor, serviceProvider);
        wrapperMicrowaveModelRev181010.readTheFaultsOfMicrowaveModel(ONFLayerProtocolName.TDMCONTAINER, null, uid,
                resultList);

    }

    @Test
    public void testMwHybridMwStructureWithProblems() throws Exception {
        final Class<MwHybridMwStructurePac> clazzPac = MwHybridMwStructurePac.class;
        final Class<HybridMwStructureCurrentProblems> clazzProblems = HybridMwStructureCurrentProblems.class;

        InstanceIdentifier<HybridMwStructureCurrentProblems> mwEthInterfaceIID =
                InstanceIdentifier.builder(clazzPac, new MwHybridMwStructurePacKey(uid)).child(clazzProblems).build();

        List<org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.hybrid.mw.structure.current.problems.g.CurrentProblemList> currentProblemList =
                Arrays.asList(
                        new org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.hybrid.mw.structure.current.problems.g.CurrentProblemListBuilder()
                                .setProblemName("Loss of Payload").setProblemSeverity(SeverityType.Major)
                                .setSequenceNumber(2).setTimeStamp(null).build());
        HybridMwStructureCurrentProblems hybridMwStructureCurrentProblems =
                new HybridMwStructureCurrentProblemsBuilder().setCurrentProblemList(YangToolsMapperHelper.toMap(currentProblemList)).build();
        when(accessor.getTransactionUtils().readData(accessor.getDataBroker(), LogicalDatastoreType.OPERATIONAL,
                mwEthInterfaceIID)).thenReturn(hybridMwStructureCurrentProblems);

        WrapperMicrowaveModelRev181010 wrapperMicrowaveModelRev181010 =
                new WrapperMicrowaveModelRev181010(accessor, serviceProvider);
        wrapperMicrowaveModelRev181010.readTheFaultsOfMicrowaveModel(ONFLayerProtocolName.STRUCTURE,
                MwHybridMwStructurePac.class, uid, resultList);

    }

    @Test
    public void testMwAirInterfaceDiversityStructureWithProblems() throws Exception {
        final Class<MwAirInterfaceDiversityPac> clazzPac = MwAirInterfaceDiversityPac.class;
        final Class<AirInterfaceDiversityCurrentProblems> clazzProblems = AirInterfaceDiversityCurrentProblems.class;

        InstanceIdentifier<AirInterfaceDiversityCurrentProblems> mwEthInterfaceIID = InstanceIdentifier
                .builder(clazzPac, new MwAirInterfaceDiversityPacKey(uid)).child(clazzProblems).build();

        List<org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.air._interface.diversity.current.problems.g.CurrentProblemList> currentProblemList =
                Arrays.asList(
                        new org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.air._interface.diversity.current.problems.g.CurrentProblemListBuilder()
                                .setProblemName("Loss of Payload").setProblemSeverity(SeverityType.Major)
                                .setSequenceNumber(2).setTimeStamp(null).build());
        AirInterfaceDiversityCurrentProblems hybridMwStructureCurrentProblems =
                new AirInterfaceDiversityCurrentProblemsBuilder().setCurrentProblemList(YangToolsMapperHelper.toMap(currentProblemList)).build();
        when(accessor.getTransactionUtils().readData(accessor.getDataBroker(), LogicalDatastoreType.OPERATIONAL,
                mwEthInterfaceIID)).thenReturn(hybridMwStructureCurrentProblems);

        WrapperMicrowaveModelRev181010 wrapperMicrowaveModelRev181010 =
                new WrapperMicrowaveModelRev181010(accessor, serviceProvider);
        wrapperMicrowaveModelRev181010.readTheFaultsOfMicrowaveModel(ONFLayerProtocolName.STRUCTURE,
                MwAirInterfaceDiversityPac.class, uid, resultList);

    }

    @Test
    public void testMwPureEthernetStructureWithProblems() throws Exception {
        final Class<MwPureEthernetStructurePac> clazzPac = MwPureEthernetStructurePac.class;
        final Class<PureEthernetStructureCurrentProblems> clazzProblems = PureEthernetStructureCurrentProblems.class;

        InstanceIdentifier<PureEthernetStructureCurrentProblems> mwEthInterfaceIID = InstanceIdentifier
                .builder(clazzPac, new MwPureEthernetStructurePacKey(uid)).child(clazzProblems).build();

        List<org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.pure.ethernet.structure.current.problems.g.CurrentProblemList> currentProblemList =
                Arrays.asList(
                        new org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.pure.ethernet.structure.current.problems.g.CurrentProblemListBuilder()
                                .setProblemName("Loss of Payload").setProblemSeverity(SeverityType.Major)
                                .setSequenceNumber(2).setTimeStamp(null).build());
        PureEthernetStructureCurrentProblems hybridMwStructureCurrentProblems =
                new PureEthernetStructureCurrentProblemsBuilder().setCurrentProblemList(YangToolsMapperHelper.toMap(currentProblemList)).build();
        when(accessor.getTransactionUtils().readData(accessor.getDataBroker(), LogicalDatastoreType.OPERATIONAL,
                mwEthInterfaceIID)).thenReturn(hybridMwStructureCurrentProblems);

        WrapperMicrowaveModelRev181010 wrapperMicrowaveModelRev181010 =
                new WrapperMicrowaveModelRev181010(accessor, serviceProvider);
        wrapperMicrowaveModelRev181010.readTheFaultsOfMicrowaveModel(ONFLayerProtocolName.STRUCTURE,
                MwPureEthernetStructurePac.class, uid, resultList);

    }

    @Test
    public void testNullStructureWithProblems() throws Exception {

        WrapperMicrowaveModelRev181010 wrapperMicrowaveModelRev181010 =
                new WrapperMicrowaveModelRev181010(accessor, serviceProvider);
        wrapperMicrowaveModelRev181010.readTheFaultsOfMicrowaveModel(ONFLayerProtocolName.STRUCTURE, null, uid,
                resultList);

    }

    @Test
    public void testgetLtpHistoricalPerformanceData() throws InconsistentPMDataException {
        InstanceIdentifier<AirInterfaceConfiguration> mwAirInterfaceConfigurationIID =
                InstanceIdentifier.builder(MwAirInterfacePac.class, new MwAirInterfacePacKey(uid))
                        .child(AirInterfaceConfiguration.class).build();

        AirInterfaceConfiguration airConfiguration = new AirInterfaceConfigurationBuilder().build();
        when(accessor.getTransactionUtils().readData(dataBroker, LogicalDatastoreType.OPERATIONAL,
                mwAirInterfaceConfigurationIID)).thenReturn(airConfiguration);

        InstanceIdentifier<AirInterfaceHistoricalPerformances> mwAirInterfaceHistoricalPerformanceIID =
                InstanceIdentifier.builder(MwAirInterfacePac.class, new MwAirInterfacePacKey(uid))
                        .child(AirInterfaceHistoricalPerformances.class).build();

        PerformanceData performanceData = new PerformanceDataBuilder().build();
        List<org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.air._interface.historical.performances.g.HistoricalPerformanceDataList> airHistPMList =
                Arrays.asList(
                        new org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.air._interface.historical.performances.g.HistoricalPerformanceDataListBuilder()
                        .setHistoryDataId("123")
                        .setGranularityPeriod(GranularityPeriodType.Period15Min)
                        .setSuspectIntervalFlag(true)
                                .setPerformanceData(performanceData).build());
        AirInterfaceHistoricalPerformances airHistoricalPerformanceData =
                new AirInterfaceHistoricalPerformancesBuilder().setHistoricalPerformanceDataList(YangToolsMapperHelper.toMap(airHistPMList)).build();
        when(accessor.getTransactionUtils().readData(accessor.getDataBroker(), LogicalDatastoreType.OPERATIONAL,
                mwAirInterfaceHistoricalPerformanceIID)).thenReturn(airHistoricalPerformanceData);

        InstanceIdentifier<EthernetContainerHistoricalPerformances> ethContainerIID =
                InstanceIdentifier.builder(MwEthernetContainerPac.class, new MwEthernetContainerPacKey(uid))
                        .child(EthernetContainerHistoricalPerformances.class).build();

        org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.container.historical.performance.type.g.PerformanceData ethPerformanceData =
                new org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.container.historical.performance.type.g.PerformanceDataBuilder()
                        .build();
        List<org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.ethernet.container.historical.performances.g.HistoricalPerformanceDataList> ethHistPMList =
                Arrays.asList(
                        new org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev181010.ethernet.container.historical.performances.g.HistoricalPerformanceDataListBuilder()
                        .setHistoryDataId("123")
                        .setGranularityPeriod(GranularityPeriodType.Period24Hours)
                        .setSuspectIntervalFlag(true)
                                .setPerformanceData(ethPerformanceData).build());
        EthernetContainerHistoricalPerformances ethContainerHistoricalPerformanceData =
                new EthernetContainerHistoricalPerformancesBuilder().setHistoricalPerformanceDataList(YangToolsMapperHelper.toMap(ethHistPMList))
                        .build();
        when(accessor.getTransactionUtils().readData(accessor.getDataBroker(), LogicalDatastoreType.OPERATIONAL,
                ethContainerIID)).thenReturn(ethContainerHistoricalPerformanceData);

        Lp lp = new LpBuilder().setUuid(uid).build();
        WrapperMicrowaveModelRev181010 wrapperMicrowaveModelRev181010 =
                new WrapperMicrowaveModelRev181010(accessor, serviceProvider);

        wrapperMicrowaveModelRev181010.getLtpHistoricalPerformanceData(ONFLayerProtocolName.MWAIRINTERFACE, lp);
    }
}
