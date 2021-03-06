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
import java.util.List;
import java.util.Optional;
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
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.ControlConstruct;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.UniversalId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

public class TestOnf14NetworkElement extends Mockito {

    private static String NODEIDSTRING = "nSky";

    static NetconfDomAccessor accessor;
    static DeviceManagerServiceProvider serviceProvider;
    static Capabilities capabilities;
    static DataProvider dataProvider;
    static FaultService faultService;
    static DOMDataBroker dataBroker;
    static TransactionUtils transactionUtils;
    static ControlConstruct controlConstruct;

    @Before
    public void init() {
        capabilities = mock(Capabilities.class);
        accessor = mock(NetconfDomAccessor.class);
        serviceProvider = mock(DeviceManagerServiceProvider.class);

        NodeId nNodeId = new NodeId("nSky");
        when(accessor.getCapabilites()).thenReturn(capabilities);
        when(accessor.getNodeId()).thenReturn(nNodeId);

        dataProvider = mock(DataProvider.class);
        when(serviceProvider.getDataProvider()).thenReturn(dataProvider);

        faultService = mock(FaultService.class);
        when(serviceProvider.getFaultService()).thenReturn(faultService);

        dataBroker = mock(DOMDataBroker.class);
        when(accessor.getDataBroker()).thenReturn(dataBroker);

        controlConstruct = mock(ControlConstruct.class);

        YangInstanceIdentifier CONTROLCONSTRUCT_IID =
                YangInstanceIdentifier.builder().node(ControlConstruct.QNAME).build();

        when(accessor.readData(LogicalDatastoreType.CONFIGURATION, CONTROLCONSTRUCT_IID, ControlConstruct.class))
                .thenReturn(Optional.of(controlConstruct));

        List<UniversalId> topLevelEqList = null;
        UniversalId uuid = new UniversalId("0Aabcdef-0abc-0cfD-0abC-0123456789AB");
        topLevelEqList = Arrays.asList(uuid);

        when(Optional.of(controlConstruct).get().getTopLevelEquipment()).thenReturn(topLevelEqList);
    }

    @Test
    public void testGeneric() {
        Optional<NetworkElement> onfNe;
        NodeId nodeId = new NodeId(NODEIDSTRING);
        NetconfBindingAccessor bindingAccessor = mock(NetconfBindingAccessor.class);
        when(bindingAccessor.getTransactionUtils()).thenReturn(mock(TransactionUtils.class));
        when(bindingAccessor.getNodeId()).thenReturn(nodeId);

        NetconfDomAccessor domAccessor = mock(NetconfDomAccessor.class);
        when(domAccessor.getNodeId()).thenReturn(nodeId);

        when(accessor.getCapabilites().isSupportingNamespace(ControlConstruct.QNAME)).thenReturn(true);
        when(accessor.getNetconfBindingAccessor()).thenReturn(Optional.of(bindingAccessor));
        when(accessor.getNetconfDomAccessor()).thenReturn(Optional.of(domAccessor));

        ConfigurationFileRepresentation configurationRepresentation = mock(ConfigurationFileRepresentation.class);
        when(serviceProvider.getConfigurationFileRepresentation()).thenReturn(configurationRepresentation);

        Onf14NetworkElementFactory factory = new Onf14NetworkElementFactory();
        onfNe = factory.create(accessor, serviceProvider);
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
