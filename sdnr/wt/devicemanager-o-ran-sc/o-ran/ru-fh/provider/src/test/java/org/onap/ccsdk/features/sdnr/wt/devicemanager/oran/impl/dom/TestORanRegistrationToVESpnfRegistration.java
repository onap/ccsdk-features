/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2021 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.impl.dom;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.vesmapper.ORanRegistrationToVESpnfRegistrationMapper;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorCfgService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESCommonEventHeaderPOJO;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESPNFRegistrationFieldsPOJO;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Host;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev221225.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;

@RunWith(MockitoJUnitRunner.class)
public class TestORanRegistrationToVESpnfRegistration {

    @Mock
    NetconfAccessor netconfAccessor;
    @Mock
    VESCollectorService vesCollectorService;
    @Mock
    VESCollectorCfgService vesCfgService;

    private static final QNameModule IETF_HARDWARE_MODULE =
            QNameModule.create(XMLNamespace.of("urn:ietf:params:xml:ns:yang:ietf-hardware"), Revision.of("2018-03-13"));
    private static final QName HW_COMPONENT_LIST = QName.create(IETF_HARDWARE_MODULE, "component");
    private static final QName HW_COMPONENT_LIST_KEY = QName.create(IETF_HARDWARE_MODULE, "name");
    private static final QName HW_COMPONENT_LIST_CLASS = QName.create(IETF_HARDWARE_MODULE, "class");
    private static final QName HW_COMPONENT_LIST_PHYSICAL_INDEX = QName.create(IETF_HARDWARE_MODULE, "physical-index"); // leaf:int32
    private static final QName HW_COMPONENT_LIST_DESC = QName.create(IETF_HARDWARE_MODULE, "description"); // leaf:String
    private static final QName HW_COMPONENT_LIST_SW_REV = QName.create(IETF_HARDWARE_MODULE, "software-rev"); // leaf:String
    private static final QName HW_COMPONENT_LIST_SER_NUM = QName.create(IETF_HARDWARE_MODULE, "serial-num"); // leaf:String
    private static final QName HW_COMPONENT_LIST_MFG_NAME = QName.create(IETF_HARDWARE_MODULE, "mfg-name"); // leaf:String
    private static final QName HW_COMPONENT_LIST_MODEL_NAME = QName.create(IETF_HARDWARE_MODULE, "model-name"); // leaf:String
    private static final QName HW_COMPONENT_LIST_ALIAS = QName.create(IETF_HARDWARE_MODULE, "alias"); // leaf:String

    @Test
    public void test() {
        NetconfNode testNetconfNode = mock(NetconfNode.class);
        when(testNetconfNode.getHost()).thenReturn(new Host(new IpAddress(new Ipv4Address("10.10.10.10"))));

        when(netconfAccessor.getNodeId()).thenReturn(new NodeId("nSky"));
        when(netconfAccessor.getNetconfNode()).thenReturn(testNetconfNode);
        when(vesCollectorService.getConfig()).thenReturn(vesCfgService);
        when(vesCfgService.getReportingEntityName()).thenReturn("SDN-R");

        ORanRegistrationToVESpnfRegistrationMapper mapper =
                new ORanRegistrationToVESpnfRegistrationMapper(netconfAccessor, vesCollectorService);
        VESCommonEventHeaderPOJO commonHeader = mapper.mapCommonEventHeader(buildComponentEntry());
        VESPNFRegistrationFieldsPOJO pnfFields = mapper.mapPNFRegistrationFields(buildComponentEntry());

        assertEquals(commonHeader.getNfVendorName(), "ISCO");
        assertEquals(pnfFields.getUnitType(), "chassis");
        assertEquals(pnfFields.getSerialNumber(), "10283");
        assertEquals(pnfFields.getSoftwareVersion(), "3.8.1 (2020-10-30 11:47:59)");
    }

    public MapEntryNode buildComponentEntry() {
        return Builders.mapEntryBuilder()
                .withNodeIdentifier(
                        NodeIdentifierWithPredicates.of(HW_COMPONENT_LIST, HW_COMPONENT_LIST_KEY, "chassis"))
                .withChild(Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(HW_COMPONENT_LIST_CLASS))
                        .withValue("ianahw:chassis").build())
                .withChild(Builders.leafBuilder()
                        .withNodeIdentifier(new NodeIdentifier(HW_COMPONENT_LIST_PHYSICAL_INDEX)).withValue(1).build())
                .withChild(Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(HW_COMPONENT_LIST_MFG_NAME))
                        .withValue("ISCO").build())
                .withChild(Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(HW_COMPONENT_LIST_MODEL_NAME))
                        .withValue("ProteusCPRI Compact").build())
                .withChild(Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(HW_COMPONENT_LIST_ALIAS))
                        .withValue("chassis").build())
                .withChild(Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(HW_COMPONENT_LIST_SER_NUM))
                        .withValue("10283").build())
                .withChild(Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(HW_COMPONENT_LIST_DESC))
                        .withValue("HighStreet-ONAP40").build())
                .withChild(Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(HW_COMPONENT_LIST_SW_REV))
                        .withValue("3.8.1 (2020-10-30 11:47:59)").build())
                .build();

    }

}
