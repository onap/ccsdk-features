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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.impl.Onf14NetworkElementFactory;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.FaultService;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.TransactionUtils;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.onf.yang.air._interface._2._0.rev200121.LAYERPROTOCOLNAMETYPEAIRLAYER;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.ControlConstruct;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.UniversalId;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.control.construct.Equipment;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.control.construct.EquipmentKey;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.control.construct.LogicalTerminationPoint;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.control.construct.LogicalTerminationPointBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.control.construct.LogicalTerminationPointKey;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.logical.termination.point.LayerProtocol;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.logical.termination.point.LayerProtocolBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.logical.termination.point.LayerProtocolKey;
import org.opendaylight.yang.gen.v1.urn.onf.yang.ethernet.container._2._0.rev200121.LAYERPROTOCOLNAMETYPEETHERNETCONTAINERLAYER;
import org.opendaylight.yang.gen.v1.urn.onf.yang.wire._interface._2._0.rev200123.LAYERPROTOCOLNAMETYPEWIRELAYER;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class TestOnf14NetworkElement extends Mockito {

    private static String NODEIDSTRING = "nSky";

    static NetconfBindingAccessor bindingAccessor;
    static DeviceManagerServiceProvider serviceProvider;
    static Capabilities capabilities;
    static DataProvider dataProvider;
    static FaultService faultService;
    static DataBroker dataBroker;
    static TransactionUtils transactionUtils;
    static ControlConstruct controlConstruct;
    static Equipment equipment;

    @Before
    public void init() {
        capabilities = mock(Capabilities.class);
        bindingAccessor = mock(NetconfBindingAccessor.class);
        serviceProvider = mock(DeviceManagerServiceProvider.class);

        NodeId nNodeId = new NodeId("nSky");
        when(bindingAccessor.getCapabilites()).thenReturn(capabilities);
        when(bindingAccessor.getNodeId()).thenReturn(nNodeId);

        dataProvider = mock(DataProvider.class);
        when(serviceProvider.getDataProvider()).thenReturn(dataProvider);

        faultService = mock(FaultService.class);
        when(serviceProvider.getFaultService()).thenReturn(faultService);

        dataBroker = mock(DataBroker.class);
        when(bindingAccessor.getDataBroker()).thenReturn(dataBroker);

        controlConstruct = mock(ControlConstruct.class);

        InstanceIdentifier<ControlConstruct> CONTROLCONSTRUCT_IID = InstanceIdentifier.builder(ControlConstruct.class).build();
        when(bindingAccessor.getTransactionUtils()).thenReturn(mock(TransactionUtils.class));
        when(bindingAccessor.getTransactionUtils().readData(bindingAccessor.getDataBroker(), LogicalDatastoreType.CONFIGURATION, CONTROLCONSTRUCT_IID)).thenReturn(controlConstruct);

        List<UniversalId> topLevelEqList = null;
        UniversalId uuid = new UniversalId("0Aabcdef-0abc-0cfD-0abC-0123456789AB");
        topLevelEqList = Arrays.asList(uuid);
        when(Optional.of(controlConstruct).get().getTopLevelEquipment()).thenReturn(topLevelEqList);

        InstanceIdentifier<Equipment> equipmentIID = InstanceIdentifier.builder(ControlConstruct.class)
                .child(Equipment.class, new EquipmentKey(uuid)).build();

        equipment = mock(Equipment.class);
        when(bindingAccessor.getTransactionUtils().readData(bindingAccessor.getDataBroker(), LogicalDatastoreType.CONFIGURATION,
                equipmentIID)).thenReturn(equipment);
        UniversalId eqpUid = new UniversalId("1Aabcdef-1abc-1cfD-1abc-0123456789AB");
        when(equipment.getUuid()).thenReturn(eqpUid);

        @NonNull Map<LogicalTerminationPointKey, LogicalTerminationPoint> lptMap = new HashMap<LogicalTerminationPointKey, LogicalTerminationPoint>();
        var lpMap = new HashMap<LayerProtocolKey, LayerProtocol>();

        LayerProtocol lp = new LayerProtocolBuilder().setLayerProtocolName(LAYERPROTOCOLNAMETYPEAIRLAYER.class).setLocalId("TESTAIRLAYER").build();
        LayerProtocolKey lpKey = new LayerProtocolKey("AIRPROTOCOL");
        lpMap.put(lpKey, lp);
        LogicalTerminationPoint ltp = new LogicalTerminationPointBuilder().setLayerProtocol(lpMap).setUuid(uuid).build();
        UniversalId ltpUuid = new UniversalId("1Aabcdef-1abc-1cfD-1abc-0123456789AB");
        LogicalTerminationPointKey ltpKey = new LogicalTerminationPointKey(ltpUuid);

        lptMap.put(ltpKey, ltp);

        lp = new LayerProtocolBuilder().setLayerProtocolName(LAYERPROTOCOLNAMETYPEETHERNETCONTAINERLAYER.class).setLocalId("TESTETHCONTAINERLAYER").build();
        lpKey = new LayerProtocolKey("ETHERNETCONTAINERPROTOCOL");
        lpMap = new HashMap<LayerProtocolKey, LayerProtocol>();
        lpMap.put(lpKey, lp);
        ltp = new LogicalTerminationPointBuilder().setLayerProtocol(lpMap).setUuid(uuid).build();
        ltpUuid = new UniversalId("1Aabcdef-1abc-1cfD-1abc-0123456789BC");
        ltpKey = new LogicalTerminationPointKey(ltpUuid);

        lptMap.put(ltpKey, ltp);

        lp = new LayerProtocolBuilder().setLayerProtocolName(LAYERPROTOCOLNAMETYPEWIRELAYER.class).setLocalId("TESTWIRELAYER").build();
        lpKey = new LayerProtocolKey("WIREPROTOCOL");
        lpMap = new HashMap<LayerProtocolKey, LayerProtocol>();
        lpMap.put(lpKey, lp);
        ltp = new LogicalTerminationPointBuilder().setLayerProtocol(lpMap).setUuid(uuid).build();
        ltpUuid = new UniversalId("1Aabcdef-1abc-1cfD-1abc-0123456789CD");
        ltpKey = new LogicalTerminationPointKey(ltpUuid);

        lptMap.put(ltpKey, ltp);

        when(Optional.of(controlConstruct).get().nonnullLogicalTerminationPoint()).thenReturn(lptMap);

    }

    @Test
    public void testGeneric() {
        Optional<NetworkElement> onfNe;
        NodeId nodeId = new NodeId(NODEIDSTRING);

        when(bindingAccessor.getNodeId()).thenReturn(nodeId);

        NetconfDomAccessor domAccessor = mock(NetconfDomAccessor.class);
        when(domAccessor.getNodeId()).thenReturn(nodeId);

        when(bindingAccessor.getCapabilites().isSupportingNamespace(ControlConstruct.QNAME)).thenReturn(true);
        when(bindingAccessor.getNetconfBindingAccessor()).thenReturn(Optional.of(bindingAccessor));
        when(bindingAccessor.getNetconfDomAccessor()).thenReturn(Optional.of(domAccessor));

        ConfigurationFileRepresentation configurationRepresentation = mock(ConfigurationFileRepresentation.class);
        when(serviceProvider.getConfigurationFileRepresentation()).thenReturn(configurationRepresentation);

        Onf14NetworkElementFactory factory = new Onf14NetworkElementFactory();
        onfNe = factory.create(bindingAccessor, serviceProvider);
        assertTrue(onfNe.isPresent());

        onfNe.get().register();
        onfNe.get().deregister();
        onfNe.get().getAcessor();
        onfNe.get().getDeviceType();
        onfNe.get().warmstart();
        onfNe.get().getService(null);
        assertEquals(onfNe.get().getNodeId().getValue(), "nSky");
    }
}
