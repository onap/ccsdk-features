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
package org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.test.example.TestNetconfHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev240911.NetconfNodeAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev240911.netconf.node.augment.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.common.QName;

public class TestCapabilites {

    private static final String CAPABILITYSTRING = "network-element";

    private static final QName QNAMECOREMODEL = QName.create("urn:onf:params:xml:ns:yang:core-model", "2017-03-20", "core-model").intern();
    private static final QName QNAMENETWORKELEMENET = QName.create(QNAMECOREMODEL, CAPABILITYSTRING).intern();

    private static Capabilities capabilites;
    private static Capabilities uacapabilites;

    @BeforeClass
    public static void before() {
        String nodeIdString = "Test";
        NodeId nodeId = new NodeId(nodeIdString);
        String capabilityString = "network-element";
        Node node = TestNetconfHelper.getTestNode(nodeId,capabilityString);
        NetconfNode netconfNode = node.augmentation(NetconfNodeAugment.class).getNetconfNode();
        capabilites = Capabilities.getAvailableCapabilities(netconfNode);
        uacapabilites = Capabilities.getUnavailableCapabilities(netconfNode);
    }

    @Test
    public void testAvailableCapabilites() {
        boolean result = capabilites.isSupportingNamespaceAndRevision("network-element", null);
        assertTrue(result);
    }

    @Test
    public void testAvailableCapabilitesNotThere() {
        boolean result = capabilites.isSupportingNamespaceAndRevision(CAPABILITYSTRING+"xy", null);
        assertFalse(result);
    }

    @Test
    public void testUnavailableCapabilites() {
        assertTrue(uacapabilites.getCapabilities().isEmpty());
    }

    @Test
    public void testAvailableCapabilitesQName() {
        boolean result = capabilites.isSupportingNamespace(QNAMENETWORKELEMENET);
        assertFalse(result);
    }
    @Test
    public void testSupportsRevision() {
        boolean result = capabilites.isSupportingNamespaceAndRevision(QNAMENETWORKELEMENET);
        assertFalse(result);
    }
    @Test
    public void testGetRevision() {
        String revisionString = capabilites.getRevisionForNamespace(QNAMENETWORKELEMENET);
        boolean result = Capabilities.isNamespaceSupported(revisionString);
        assertFalse(result);

    }
}
