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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.WrapperPTPModelRev170208;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.TransactionUtils;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.YangToolsMapperHelper;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ptp.dataset.rev170208.InstanceList;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ptp.dataset.rev170208.InstanceListBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ptp.dataset.rev170208.InstanceListKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ptp.dataset.rev170208.instance.list.PortDsList;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ptp.dataset.rev170208.instance.list.PortDsListBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ptp.dataset.rev170208.port.ds.entry.PortIdentity;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ptp.dataset.rev170208.port.ds.entry.PortIdentityBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Uint16;

public class TestWrapperPTPModelRev170208 {

    private static final String NODEID = "node1";
    NetconfBindingAccessor netconfAccessor;
    Capabilities capabilities;
    TransactionUtils transactionUtils;
    DataBroker dataBroker;

    @Before
    public void init() {
        netconfAccessor = mock(NetconfBindingAccessor.class);
        capabilities = mock(Capabilities.class);
        dataBroker = mock(DataBroker.class);
        transactionUtils = mock(TransactionUtils.class);

        when(netconfAccessor.getNodeId()).thenReturn(new NodeId(NODEID));
        when(netconfAccessor.getCapabilites()).thenReturn(capabilities);
        when(netconfAccessor.getTransactionUtils()).thenReturn(transactionUtils);
        when(netconfAccessor.getDataBroker()).thenReturn(dataBroker);

        Uint16 portNumber = Uint16.valueOf(10);
        PortIdentity portIdentity = new PortIdentityBuilder().setPortNumber(portNumber).build();
        List<PortDsList> portDsList =
                Arrays.asList(new PortDsListBuilder().setPortNumber(portNumber).setPortIdentity(portIdentity).build());
        InstanceList instanceList =
                new InstanceListBuilder().setInstanceNumber(Uint16.valueOf(1)).setPortDsList(YangToolsMapperHelper.toMap(portDsList)).build();
        InstanceIdentifier<InstanceList> PTPINSTANCES_IID =
                InstanceIdentifier.builder(InstanceList.class, new InstanceListKey(Uint16.valueOf(1))).build();
        when(netconfAccessor.getTransactionUtils().readData(netconfAccessor.getDataBroker(),
                LogicalDatastoreType.OPERATIONAL, PTPINSTANCES_IID)).thenReturn(instanceList);
    }

    @Test
    public void test() {

        when(capabilities.isSupportingNamespaceAndRevision(InstanceList.QNAME)).thenReturn(false);

        WrapperPTPModelRev170208.initSynchronizationExtension(netconfAccessor);
    }

    @Test
    public void test1() {

        when(capabilities.isSupportingNamespaceAndRevision(InstanceList.QNAME)).thenReturn(true);

        WrapperPTPModelRev170208.initSynchronizationExtension(netconfAccessor);
    }

}
