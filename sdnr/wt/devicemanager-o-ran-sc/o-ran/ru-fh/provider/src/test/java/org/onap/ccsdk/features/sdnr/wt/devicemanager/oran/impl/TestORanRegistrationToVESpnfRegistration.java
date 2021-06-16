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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.impl.ORanRegistrationToVESpnfRegistrationMapper;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorCfgService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorService;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.hardware.rev180313.hardware.Component;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Host;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

@RunWith(MockitoJUnitRunner.class)
public class TestORanRegistrationToVESpnfRegistration {

    @Mock
    NetconfAccessor netconfAccessor;
    @Mock
    VESCollectorService vesCollectorService;
    @Mock
    VESCollectorCfgService vesCfgService;

    private final int SEQUENCE_NO = 10;

    @Test
    public void test() {
        String dateTimeString = "2020-02-05T12:30:45.283Z";
        String name = "Slot-0";

        NetconfNode testNetconfNode = mock(NetconfNode.class);
        when(testNetconfNode.getHost()).thenReturn(new Host(new IpAddress(new Ipv4Address("10.10.10.10"))));

        when(netconfAccessor.getNodeId()).thenReturn(new NodeId("nSky"));
        when(netconfAccessor.getNetconfNode()).thenReturn(testNetconfNode);
        when(vesCollectorService.getConfig()).thenReturn(vesCfgService);
        when(vesCfgService.getReportingEntityName()).thenReturn("SDN-R");
        Component testComponent = ComponentHelper.get(name, dateTimeString);

        ORanRegistrationToVESpnfRegistrationMapper mapper = new ORanRegistrationToVESpnfRegistrationMapper(netconfAccessor, vesCollectorService);
        mapper.mapCommonEventHeader(testComponent);
        mapper.mapPNFRegistrationFields(testComponent);
    }

}
