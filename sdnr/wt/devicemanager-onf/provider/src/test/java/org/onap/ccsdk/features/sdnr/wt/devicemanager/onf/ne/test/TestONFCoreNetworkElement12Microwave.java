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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ne.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.microwave.OnfMicrowaveModel;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.impl.DeviceManagerOnfConfiguration;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ne.ONFCoreNetworkElement12Microwave;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.EquipmentService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.FaultService;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.TransactionUtils;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.AdministrativeControl;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.AdministrativeState;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.LifecycleState;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.NetworkElement;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.OperationalState;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.UniversalId;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.extension.g.Extension;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.global._class.g.LocalId;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.label.g.Label;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.name.g.Name;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.network.element.Fd;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.network.element.Ltp;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.onf.core.model.conditional.packages.rev170402.NetworkElementPac;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EventlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SourceType;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class TestONFCoreNetworkElement12Microwave {

    NetconfAccessor accessor;
    DeviceManagerServiceProvider serviceProvider;
    Capabilities capabilities;
    TransactionUtils transactionUtils;
    NetworkElement optionalNe;
    OnfMicrowaveModel onfMicrowaveModel;
    FaultService faultService;
    EquipmentService equipmentService;
    DeviceManagerOnfConfiguration configuration;

    protected static final InstanceIdentifier<NetworkElement> NETWORKELEMENT_IID =
            InstanceIdentifier.builder(NetworkElement.class).build();

    @Before
    public void init() {
        accessor = mock(NetconfAccessor.class);
        serviceProvider = mock(DeviceManagerServiceProvider.class);
        capabilities = mock(Capabilities.class);
        transactionUtils = mock(TransactionUtils.class);
        onfMicrowaveModel = mock(OnfMicrowaveModel.class);
        faultService = mock(FaultService.class);
        equipmentService = mock(EquipmentService.class);
        configuration = mock(DeviceManagerOnfConfiguration.class);

        when(accessor.getCapabilites()).thenReturn(capabilities);
        when(serviceProvider.getFaultService()).thenReturn(faultService);
        when(serviceProvider.getEquipmentService()).thenReturn(equipmentService);

        NodeId nNodeId = new NodeId("nSky");
        when(accessor.getNodeId()).thenReturn(nNodeId);
        when(accessor.getCapabilites().isSupportingNamespaceAndRevision(NetworkElementPac.QNAME)).thenReturn(true);
        when(accessor.getTransactionUtils()).thenReturn(transactionUtils);

    }

    @Test
    public void test() {
    	optionalNe = mock(NetworkElement.class);

        when(accessor.getTransactionUtils().readData(accessor.getDataBroker(), LogicalDatastoreType.OPERATIONAL,
                NETWORKELEMENT_IID)).thenReturn(optionalNe);

        ONFCoreNetworkElement12Microwave onfCoreNetworkElement12MW =
                new ONFCoreNetworkElement12Microwave(accessor, serviceProvider, configuration, onfMicrowaveModel);
        onfCoreNetworkElement12MW.prepareCheck();

        EventlogEntity eventlogEntity = mock(EventlogEntity.class);
        when(eventlogEntity.getObjectId()).thenReturn("ABCD");
        when(eventlogEntity.getAttributeName()).thenReturn("/network-element/extension[value-name=\"top-level-equipment\"]/value");

        onfCoreNetworkElement12MW.notificationActor(eventlogEntity);

    }


    @Test
    public void test1() {
        when(accessor.getTransactionUtils().readData(accessor.getDataBroker(), LogicalDatastoreType.OPERATIONAL,
                NETWORKELEMENT_IID)).thenReturn(null);

        ONFCoreNetworkElement12Microwave onfCoreNetworkElement12MW =
                new ONFCoreNetworkElement12Microwave(accessor, serviceProvider, configuration, onfMicrowaveModel);
        onfCoreNetworkElement12MW.prepareCheck();

        EventlogEntity eventlogEntity = mock(EventlogEntity.class);
        when(eventlogEntity.getObjectId()).thenReturn("ABCD");
        when(eventlogEntity.getAttributeName()).thenReturn("/equipment-pac/equipment-current-problems");

        onfCoreNetworkElement12MW.notificationActor(eventlogEntity);
    }


}
