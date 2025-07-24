/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2022 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.test;

import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.device.rev241009.connection.oper.AvailableCapabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.device.rev241009.connection.oper.AvailableCapabilitiesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.device.rev241009.connection.oper.UnavailableCapabilitiesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.device.rev241009.connection.oper.available.capabilities.AvailableCapability;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.device.rev241009.connection.oper.available.capabilities.AvailableCapabilityBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.device.rev241009.connection.oper.unavailable.capabilities.UnavailableCapability;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.device.rev241009.connection.oper.unavailable.capabilities.UnavailableCapabilityBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev240911.netconf.node.augment.NetconfNodeBuilder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;

public class TestCapabilities {

    @Test
    public void testavailableCapabilities() {

        NetconfNodeBuilder nnodeBuilder = new NetconfNodeBuilder().setAvailableCapabilities(getAvailableCapabilities());
        Capabilities avcapabilities = Capabilities.getAvailableCapabilities(nnodeBuilder.build());

        assertTrue(avcapabilities != null);

    }

    @Test
    public void testunavailableCapabilities() {

        List<UnavailableCapability> ucapList =
                Arrays.asList(new UnavailableCapabilityBuilder().setCapability("(test-unavailable-capability?revision=2022-11-03)test").build());
        UnavailableCapabilitiesBuilder ucb = new UnavailableCapabilitiesBuilder().setUnavailableCapability(ucapList);

        NetconfNodeBuilder nnodeBuilder = new NetconfNodeBuilder().setUnavailableCapabilities(ucb.build());
        Capabilities uavcapabilities = Capabilities.getUnavailableCapabilities(nnodeBuilder.build());
        assert (uavcapabilities != null);

    }

    @Test
    public void testNameSpaceRevision() {
        QName qname = QName.create("test-capability", "test", Revision.of("2022-11-03"));
        NetconfNodeBuilder nnodeBuilder = new NetconfNodeBuilder().setAvailableCapabilities(getAvailableCapabilities());
        Capabilities avcapabilities = Capabilities.getAvailableCapabilities(nnodeBuilder.build());

        NetconfAccessor accessor = mock(NetconfAccessor.class);
        when(accessor.getCapabilites()).thenReturn(avcapabilities);
        assertEquals("2022-11-03", accessor.getCapabilites().getRevisionForNamespace(qname));
        assertEquals("test-capability?2022-11-03", Capabilities.getNamespaceAndRevisionAsString(qname));
        assertTrue(accessor.getCapabilites().isSupportingNamespace(qname));
        assertTrue(accessor.getCapabilites().isSupportingNamespace("test-capability"));
        assertTrue(accessor.getCapabilites().isSupportingNamespaceAndRevision(qname));

    }

    private AvailableCapabilities getAvailableCapabilities() {

        List<AvailableCapability> acapList =
                Arrays.asList(new AvailableCapabilityBuilder().setCapability("(test-capability?revision=2022-11-03)test").build());
        AvailableCapabilitiesBuilder acb = new AvailableCapabilitiesBuilder().setAvailableCapability(acapList);
        return acb.build();

    }
}
