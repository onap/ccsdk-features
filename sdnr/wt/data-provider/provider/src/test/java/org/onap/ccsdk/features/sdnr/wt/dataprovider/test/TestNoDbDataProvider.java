/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.nodb.NoDbDatabaseDataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.test.util.DataBrokerHelper;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Host;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.device.rev241009.ConnectionOper.ConnectionStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev240911.NetconfNodeAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev240911.netconf.node.augment.NetconfNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EntityInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadNetworkElementConnectionListInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadNetworkElementConnectionListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SortOrder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.Filter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.FilterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.PaginationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.Sortorder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.SortorderBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;

public class TestNoDbDataProvider {


    private static final HelperNoDbDataProvider dataProvider = new HelperNoDbDataProvider();

    @Test
    public void testMatcher() {
        assertTrue(dataProvider.filterMatches("test123", "test123"));
        assertTrue(dataProvider.filterMatches("test123", "test*"));
        assertTrue(dataProvider.filterMatches("test123", "*123"));
        assertTrue(dataProvider.filterMatches("test123", "test?23"));
        assertTrue(dataProvider.filterMatches("test123", "test12?"));
        assertFalse(dataProvider.filterMatches("test123", "test124"));
        assertFalse(dataProvider.filterMatches("test123", "test12"));
        assertFalse(dataProvider.filterMatches("test123", "est123"));
        assertFalse(dataProvider.filterMatches("test123", "test123?"));
        assertFalse(dataProvider.filterMatches("test123", "test??124"));
        assertFalse(dataProvider.filterMatches("test123", "test_1*"));
        assertTrue(dataProvider.filterMatches(Uint16.valueOf(25), "25"));
        assertTrue(dataProvider.filterMatches(Uint16.valueOf(25), ">=25"));
        assertTrue(dataProvider.filterMatches(Uint16.valueOf(25), ">11"));
        assertTrue(dataProvider.filterMatches(Uint16.valueOf(25), "<=44"));
        assertTrue(dataProvider.filterMatches(Uint16.valueOf(25), "<35"));
        assertFalse(dataProvider.filterMatches(Uint16.valueOf(25), ">35"));
        assertFalse(dataProvider.filterMatches(Uint16.valueOf(25), "<11"));

    }

    @Test
    public void testFiltering() {
        ReadNetworkElementConnectionListOutputBuilder output;
        output = dataProvider.readNetworkElementConnectionList(
                createInput(List.of(new FilterBuilder().setProperty("node-id").setFiltervalue("node1").build()),
                        List.of()));
        assertEquals(1, output.getData().size());
        assertEquals("node1", output.getData().get(0).getNodeId());
        output = dataProvider.readNetworkElementConnectionList(
                createInput(List.of(new FilterBuilder().setProperty("status").setFiltervalue("Connecting").build()),
                        List.of()));
        assertEquals(5, output.getData().size());
        assertEquals(List.of("node12", "node15", "node3", "node6", "node9"),
                output.getData().stream().map(e -> e.getNodeId()).sorted().collect(Collectors.toList()));
    }

    @Test
    public void testSortOrder() {
        ReadNetworkElementConnectionListOutputBuilder output;
        output = dataProvider.readNetworkElementConnectionList(
                createInput(List.of(),
                        List.of(new SortorderBuilder().setProperty("node-id").setSortorder(SortOrder.Ascending)
                                .build())));
        assertEquals(16, output.getData().size());
        assertEquals("node1", output.getData().get(0).getNodeId());
        assertEquals("node10", output.getData().get(1).getNodeId());
        assertEquals("node11", output.getData().get(2).getNodeId());
        output = dataProvider.readNetworkElementConnectionList(
                createInput(List.of(),
                        List.of(new SortorderBuilder().setProperty("status").setSortorder(SortOrder.Descending)
                                        .build(),
                                new SortorderBuilder().setProperty("node-id").setSortorder(SortOrder.Ascending)
                                        .build())));
        assertEquals(16, output.getData().size());
        assertEquals("node1", output.getData().get(0).getNodeId());
        assertEquals("node10", output.getData().get(1).getNodeId());
        assertEquals("node11", output.getData().get(2).getNodeId());
    }

    @Test
    public void testPagination() {
        ReadNetworkElementConnectionListOutputBuilder output;
        output = dataProvider.readNetworkElementConnectionList(
                createInput(List.of(), List.of(), 1, 10));
        assertEquals(10, output.getData().size());
        assertEquals(16, output.getPagination().getTotal().intValue());
        assertEquals(1, output.getPagination().getPage().intValue());
        assertEquals(10, output.getPagination().getSize().intValue());

        output = dataProvider.readNetworkElementConnectionList(
                createInput(List.of(), List.of(), 2, 10));
        assertEquals(6, output.getData().size());
        assertEquals(16, output.getPagination().getTotal().intValue());
        assertEquals(2, output.getPagination().getPage().intValue());
        assertEquals(10, output.getPagination().getSize().intValue());


    }


    private EntityInput createInput(List<Filter> filter, List<Sortorder> sortorder) {
        return createInput(filter, sortorder, 1, 20);
    }

    private EntityInput createInput(List<Filter> filter, List<Sortorder> sortorder, int page, int size) {
        return new ReadNetworkElementConnectionListInputBuilder()
                .setFilter(filter.stream().collect(Collectors.toMap(
                        e -> e.key(),
                        e -> e
                )))
                .setSortorder(sortorder.stream().collect(Collectors.toMap(
                        e -> e.key(),
                        e -> e
                )))
                .setPagination(
                        new PaginationBuilder().setPage(Uint64.valueOf(page)).setSize(Uint32.valueOf(size)).build())
                .build();
    }

    public static class HelperNoDbDataProvider extends NoDbDatabaseDataProvider {

        public HelperNoDbDataProvider() {
            super(new DataBrokerHelper<>(

                    new TopologyBuilder().setTopologyId(new TopologyId("topology-netconf")).setNode(generateNodes())
                            .build()));
        }

        private static Map<NodeKey, Node> generateNodes() {
            final var nodes = new HashMap<NodeKey, Node>();
            List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16).stream()
                    .map(e -> new NodeBuilder().setNodeId(new NodeId(String.format("node%d", e)))
                            .addAugmentation(
                                    ((NetconfNodeAugment) () -> new NetconfNodeBuilder()
                                            .setHost(new Host(
                                                    new IpAddress(new Ipv4Address(String.format("1.2.3.%d", e)))))
                                            .setPort(new PortNumber(Uint16.valueOf(20000)))
                                            .setConnectionStatus(e % 3 == 0 ? ConnectionStatus.Connecting
                                                    : e % 3 == 1 ? ConnectionStatus.Connected
                                                            : ConnectionStatus.UnableToConnect)
                                            .build()))
                            .build()).forEach(e -> nodes.put(e.key(), e));

            return nodes;
        }

        @Override
        public boolean filterMatches(String value, Filter filter) {
            return super.filterMatches(value, filter);
        }

        @Override
        public boolean filterMatches(Uint16 value, Filter filter) {
            return super.filterMatches(value, filter);
        }

        @Override
        public boolean filterMatches(String value, String filtervalue) {
            return super.filterMatches(value, filtervalue);
        }

        @Override
        public boolean filterMatches(Uint16 value, String filtervalue) {
            return super.filterMatches(value, filtervalue);
        }
    }
}
