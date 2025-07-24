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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.equipment.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.NetworkElementCoreData;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.equipment.ONFCoreNetworkElement12Equipment;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.equipment.WrapperEquipmentPacRev170402;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.FaultData;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.TransactionUtils;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.UniversalId;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.onf.core.model.conditional.packages.rev170402.EquipmentPac;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.onf.core.model.conditional.packages.rev170402.EquipmentPacKey;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.onf.core.model.conditional.packages.rev170402.equipment.pac.EquipmentCurrentProblems;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class TestONFCoreNetworkElement12Equipment {

    NetconfBindingAccessor netconfAccessor;
    Capabilities capabilities;
    NetworkElementCoreData coreData;
    FaultData faultData;
    TransactionUtils transactionUtils;
    EquipmentCurrentProblems eqptCurrProblems;
    DataBroker dataBroker;
    UniversalId uid = new UniversalId("ID");


    @Before
    public void init() throws Exception {
        netconfAccessor = mock(NetconfBindingAccessor.class);
        capabilities = mock(Capabilities.class);
        coreData = mock(NetworkElementCoreData.class);
        faultData = mock(FaultData.class);
        eqptCurrProblems = mock(EquipmentCurrentProblems.class);
        dataBroker = mock(DataBroker.class);
        transactionUtils = mock(TransactionUtils.class);

        final Class<EquipmentPac> clazzPac = EquipmentPac.class;
        final Class<EquipmentPacKey> clazzPacKey = EquipmentPacKey.class;
        final Class<EquipmentCurrentProblems> clazzProblems = EquipmentCurrentProblems.class;
        Constructor<EquipmentPacKey> cons = clazzPacKey.getConstructor(UniversalId.class);
        InstanceIdentifier<EquipmentCurrentProblems> interfaceIID =
                InstanceIdentifier.builder(clazzPac, cons.newInstance(uid)).child(clazzProblems).build();
        when(netconfAccessor.getCapabilites()).thenReturn(capabilities);
        when(netconfAccessor.getTransactionUtils()).thenReturn(transactionUtils);
        when(netconfAccessor.getDataBroker()).thenReturn(dataBroker);
        when(netconfAccessor.getTransactionUtils().readData(netconfAccessor.getDataBroker(),
                LogicalDatastoreType.OPERATIONAL, interfaceIID)).thenReturn(eqptCurrProblems);
    }

    @Test
    public void test() {
        when(capabilities.isSupportingNamespaceAndRevision(WrapperEquipmentPacRev170402.QNAME)).thenReturn(true);
        ONFCoreNetworkElement12Equipment onfCoreEqpt = new ONFCoreNetworkElement12Equipment(netconfAccessor, coreData);
        onfCoreEqpt.addProblemsofNode(faultData);
        onfCoreEqpt.addProblemsofNodeObject("ABCD");
        onfCoreEqpt.getInventoryInformation(Arrays.asList("TESTINV"));
        onfCoreEqpt.getEquipmentAll();
        onfCoreEqpt.getEquipmentData();
        onfCoreEqpt.getEquipmentPac();
        onfCoreEqpt.readNetworkElementEquipment();

    }

    @Test
    public void test1() {
        when(capabilities.isSupportingNamespaceAndRevision(WrapperEquipmentPacRev170402.QNAME)).thenReturn(false);
        ONFCoreNetworkElement12Equipment onfCoreEqpt = new ONFCoreNetworkElement12Equipment(netconfAccessor, coreData);
    }

}
