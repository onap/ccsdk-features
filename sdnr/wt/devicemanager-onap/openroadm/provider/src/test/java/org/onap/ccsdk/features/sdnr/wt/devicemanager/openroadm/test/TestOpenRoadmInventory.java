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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.openroadm.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.openroadm.impl.OpenroadmInventoryInput;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.opendaylight.yang.gen.v1.http.org.openroadm.common.node.types.rev191129.NodeIdType;
import org.opendaylight.yang.gen.v1.http.org.openroadm.common.state.types.rev191129.LifecycleState;
import org.opendaylight.yang.gen.v1.http.org.openroadm.common.state.types.rev191129.State;
import org.opendaylight.yang.gen.v1.http.org.openroadm.common.types.rev191129.OpenroadmVersionType;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.OrgOpenroadmDevice;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.circuit.packs.CircuitPacks;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.interfaces.grp.Interface;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.org.openroadm.device.Info;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.org.openroadm.device.InfoBuilder;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.org.openroadm.device.Xponder;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.shelves.Shelves;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.types.rev191129.NodeTypes;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.types.rev191129.XpdrNodeTypes;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestOpenRoadmInventory {
    private static final Logger LOG = LoggerFactory.getLogger(OpenroadmInventoryInput.class);
    private NetconfAccessor accessor = mock(NetconfAccessor.class);
    private long value1 = 1;
    private IpAddress ipAddress = new IpAddress(new Ipv4Address("127.0.0.11"));
    private NodeId nodeId = new NodeId("RoadmA2");
    private Info info = new InfoBuilder().setNodeId(NodeIdType.getDefaultInstance("zNhe2i5")).setClli("NodeB")
            .setSerialId("0002").setModel("model2").setVendor("VendorA").setCurrentIpAddress(ipAddress)
            .setCurrentIpAddress(ipAddress).setCurrentDefaultGateway(new IpAddress(new Ipv4Address("127.0.0.20")))
            .setCurrentDefaultGateway(new IpAddress(new Ipv4Address("127.0.0.20"))).setNodeType(NodeTypes.Rdm)
            .setCurrentDatetime(new DateAndTime("2017-10-22T15:23:43Z")).setSoftwareVersion("swversion1234")
            .setPrefixLength(Uint8.valueOf(28)).setMaxDegrees(Uint16.valueOf(2)).setMaxSrgs(Uint16.valueOf(3))
            .setMaxNumBin15minHistoricalPm(Uint16.valueOf(32)).setMaxNumBin24hourHistoricalPm(Uint16.valueOf(7))
            .setOpenroadmVersion(OpenroadmVersionType._20).build();

    private OrgOpenroadmDevice device = mock(OrgOpenroadmDevice.class);
    private Shelves shelf = mock(Shelves.class);
    private Interface interfaces = mock(Interface.class);
    private CircuitPacks cp = mock(CircuitPacks.class);
    private Xponder xpdr = mock(Xponder.class);
    OpenroadmInventoryInput roadmInventory = new OpenroadmInventoryInput(accessor, device);

    @Test
    public void TestDevice() {
        when(accessor.getNodeId()).thenReturn(nodeId);
        when(device.getInfo()).thenReturn(info);

        roadmInventory.getInventoryData(Uint32.valueOf(value1));
        assertEquals(info, device.getInfo());

    }

    @Test
    public void TestShelves() {
        when(accessor.getNodeId()).thenReturn(nodeId);
        when(device.getInfo()).thenReturn(info);
        when(shelf.getShelfPosition()).thenReturn("10");
        when(shelf.getOperationalState()).thenReturn(State.InService);
        when(shelf.getSerialId()).thenReturn("nodeid-1");
        when(shelf.getShelfName()).thenReturn("Shelf1");
        when(shelf.getShelfType()).thenReturn("Shelf");
        when(shelf.getClei()).thenReturn("1234567890");
        when(shelf.getVendor()).thenReturn("vendorA");
        when(shelf.getModel()).thenReturn("1");
        when(shelf.getHardwareVersion()).thenReturn("0.1");
        when(shelf.getManufactureDate()).thenReturn(new DateAndTime("2017-10-22T15:23:43Z"));
        assertNotNull(roadmInventory.getShelvesInventory(shelf, Uint32.valueOf(value1 + 1)));

        LOG.info("Shelves test completed");

    }

    @Test
    public void TestCircuitPacks() {
        when(accessor.getNodeId()).thenReturn(nodeId);
        when(cp.getCircuitPackName()).thenReturn("1/0");
        when(cp.getVendor()).thenReturn("VendorA");
        when(cp.getModel()).thenReturn("Model1");
        when(cp.getSerialId()).thenReturn("46277sgh6");
        when(cp.getClei()).thenReturn("136268785");
        when(cp.getHardwareVersion()).thenReturn("0.1");
        when(cp.getType()).thenReturn("WSS");
        when(cp.getProductCode()).thenReturn("oooooo");
        when(cp.getCircuitPackMode()).thenReturn("inServiceMode");
        when(device.getInfo()).thenReturn(info);
        assertNotNull(roadmInventory.getCircuitPackInventory(cp, Uint32.valueOf(value1 + 1)));

    }

    @Test
    public void TestInterfaces() {
        when(accessor.getNodeId()).thenReturn(nodeId);
        when(interfaces.getName()).thenReturn("1GE-interface-1");
        when(interfaces.getDescription()).thenReturn("Ethernet Interface");
        when(interfaces.getSupportingCircuitPackName()).thenReturn("1/0");
        when(device.getInfo()).thenReturn(info);
        assertNotNull(roadmInventory.getInterfacesInventory(interfaces, Uint32.valueOf(value1 + 2)));
    }

    @Test
    public void TestXponder() {
        when(xpdr.getXpdrNumber()).thenReturn(Uint16.valueOf(1));
        when(xpdr.getXpdrType()).thenReturn(XpdrNodeTypes.Mpdr);
        when(xpdr.getLifecycleState()).thenReturn(LifecycleState.Deployed);
        when(accessor.getNodeId()).thenReturn(nodeId);
        when(device.getInfo()).thenReturn(info);
        assertNotNull(roadmInventory.getXponderInventory(xpdr, Uint32.valueOf(value1 + 1)));

    }

}
