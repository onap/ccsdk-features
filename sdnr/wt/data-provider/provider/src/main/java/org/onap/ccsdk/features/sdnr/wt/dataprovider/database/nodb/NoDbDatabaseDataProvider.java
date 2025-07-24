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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.database.nodb;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DatabaseDataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.HtDatabaseMaintenance;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.HtUserdataManager;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.InventoryTreeProvider;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.device.rev241009.ConnectionOper.ConnectionStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.device.rev241009.connection.oper.available.capabilities.AvailableCapability;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev240911.NetconfNodeAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev240911.netconf.node.augment.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev240911.network.topology.topology.topology.types.TopologyNetconf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ConnectionLogStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CreateMaintenanceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CreateMaintenanceOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CreateNetworkElementConnectionOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.DeleteMaintenanceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.DeleteMaintenanceOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.DeleteNetworkElementConnectionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.DeleteNetworkElementConnectionOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EntityInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementConnectionEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadCmlogListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadConnectionlogListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadEventlogListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadFaultcurrentListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadFaultlogListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadGuiCutThroughEntryOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadInventoryDeviceListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadInventoryListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadMaintenanceListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadNetworkElementConnectionListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata15mDeviceListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata15mListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata15mLtpListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata24hDeviceListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata24hListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadPmdata24hLtpListOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadStatusOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.UpdateMaintenanceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.UpdateMaintenanceOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.UpdateNetworkElementConnectionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.UpdateNetworkElementConnectionOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.Filter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.FilterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.Sortorder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.SortorderKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.network.element.connection.entity.NodeDetails;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.network.element.connection.entity.NodeDetailsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.network.element.connection.list.output.Data;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.network.element.connection.list.output.DataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.network.element.connection.list.output.PaginationBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoDbDatabaseDataProvider implements DatabaseDataProvider {

    private static final Logger LOG = LoggerFactory.getLogger(NoDbDatabaseDataProvider.class);
    private final HtUserdataManager usermanger;
    private final HtDatabaseMaintenance maintenance;
    private final DataProvider dataprovider;
    private final InventoryTreeProvider inventoryTreeProvider;
    private final DataBroker dataBroker;

    public NoDbDatabaseDataProvider(DataBroker dataBroker) {
        this.usermanger = new NoDbHtUserdataManager();
        this.maintenance = new NoDbHtDatabaseMaintenance();
        this.dataprovider = new NoDbDataProvider();
        this.inventoryTreeProvider = new NoDbInventoryTreeProvider();
        this.dataBroker = dataBroker;
    }

    @Override
    public ReadFaultcurrentListOutputBuilder readFaultCurrentList(EntityInput input) {
        return new ReadFaultcurrentListOutputBuilder();
    }

    @Override
    public ReadFaultlogListOutputBuilder readFaultLogList(EntityInput input) {
        return new ReadFaultlogListOutputBuilder();
    }

    @Override
    public ReadCmlogListOutputBuilder readCMLogList(EntityInput input) {
        return new ReadCmlogListOutputBuilder();
    }

    @Override
    public ReadMaintenanceListOutputBuilder readMaintenanceList(EntityInput input) {
        return new ReadMaintenanceListOutputBuilder();
    }

    @Override
    public ReadNetworkElementConnectionListOutputBuilder readNetworkElementConnectionList(EntityInput input) {
        DataObjectIdentifier<Topology> iif = DataObjectIdentifier.builder(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId(TopologyNetconf.QNAME.getLocalName()))).build();
        Topology topology = null;
        try (var tx = this.dataBroker.newReadOnlyTransaction()) {
            topology = tx.read(LogicalDatastoreType.OPERATIONAL, iif).get()
                    .orElse(null);
        } catch (InterruptedException | ExecutionException e) {
            LOG.warn("Interrupted!", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
        final var pagination = input != null ? input.getPagination() : null;
        long pageSize = pagination != null ? pagination.getSize().longValue() : 20;
        if (topology == null) {
            LOG.info("topology is null");
            return new ReadNetworkElementConnectionListOutputBuilder().setData(List.of())
                    .setPagination(new PaginationBuilder().setPage(Uint64.valueOf(1)).setSize(Uint32.valueOf(pageSize))
                            .setTotal(Uint64.valueOf(0)).build());
        }
        long offset = pagination != null ? (pagination.getPage().longValue() - 1) * pageSize : 0;
        long page = offset / pageSize + 1;

        final var node = topology.getNode();
        final Collection<Node> nodes = node != null ? node.values() : List.of();
        LOG.info("found {} nodes. filtering...", nodes.size());
        final var result = nodes.stream().filter(e -> matchFilter(e, input != null ? input.getFilter() : null))
                .sorted(new NodeDataProviderComparator(input != null ? input.getSortorder() : null))
                .collect(Collectors.toList());
        long total = result.size();
        LOG.info("found {} nodes for filter {}", total, input != null ? input.getFilter() : null);
        return new ReadNetworkElementConnectionListOutputBuilder().setData(
                result.stream().skip(offset).limit(pageSize).map(this::toNetworkElement).collect(
                        Collectors.toList())).setPagination(new PaginationBuilder().setPage(
                Uint64.valueOf(page)).setSize(Uint32.valueOf(pageSize)).setTotal(Uint64.valueOf(total)).build());
    }

    private static NetconfNode getNetconfNode(Node node) {
        var aug = node.augmentation(NetconfNodeAugment.class);
        return aug != null ? aug.getNetconfNode() : null;
    }

    private Data toNetworkElement(Node node) {
        final var nNode = getNetconfNode(node);
        if (nNode == null) {
            return null;
        }
        ConnectionLogStatus csts =
                nNode.getConnectionStatus() == ConnectionStatus.Connecting ? ConnectionLogStatus.Connecting :
                        nNode.getConnectionStatus() == ConnectionStatus.Connected ? ConnectionLogStatus.Connected :
                                ConnectionLogStatus.UnableToConnect;
        NodeDetails nodeDetails = new NodeDetailsBuilder()
                .setAvailableCapabilities(nNode.getAvailableCapabilities() == null ? null :
                        nNode.getAvailableCapabilities().nonnullAvailableCapability().stream()
                                .map(AvailableCapability::getCapability).collect(Collectors.toSet()))
                .setUnavailableCapabilities(nNode.getNonModuleCapabilities() == null ? null
                        : nNode.getNonModuleCapabilities().getCapability()).build();
        return new DataBuilder()
                .setId(node.getNodeId().getValue())
                .setNodeId(node.getNodeId().getValue())
                .setHost(nNode.getHost().stringValue())
                .setPort(nNode.getPort().getValue().toUint32())
                .setStatus(csts)
                .setNodeDetails(nodeDetails)
                .setIsRequired(false).build();
    }

    private boolean matchFilter(Node node, @Nullable Map<FilterKey, Filter> filter) {
        if (filter == null || filter.size() <= 0) {
            return true;
        }
        NetconfNode nNode = getNetconfNode(node);
        if (nNode == null) {
            //should never happen
            return true;
        }
        for (Filter f : filter.values()) {
            switch (f.getProperty()) {
                case "node-id": {
                    if (!filterMatches(node.getNodeId().getValue(), f)) {
                        return false;
                    }
                    break;
                }
                case "host": {
                    if (!filterMatches(nNode.getHost().stringValue(), f)) {
                        return false;
                    }
                    break;
                }
                case "port": {
                    if (!filterMatches(nNode.getPort().getValue(), f)) {
                        return false;
                    }
                    break;
                }
                case "core-model-capability": {
                    break;
                }
                case "device-type": {
                    break;
                }
                case "device-function": {
                    break;
                }
                case "is-required": {
                    break;
                }
                case "status": {
                    if (!filterMatches(nNode.getConnectionStatus().name(), f)) {
                        return false;
                    }
                    break;
                }
                case "tls-key": {
                    break;
                }
                case "mount-method": {
                    break;
                }
            }
        }
        return true;
    }

    protected boolean filterMatches(Uint16 value, Filter filter) {
        if (value == null) {
            return false;
        }
        // no filter is a match
        if (this.isEmpty(filter)) {
            return true;
        }
        // single filter => matches or not
        if (filter.getFiltervalue() != null) {
            return this.filterMatches(value, filter.getFiltervalue());
        }
        // not empty and did not match
        return false;
    }


    protected boolean filterMatches(String value, Filter filter) {
        if (value == null) {
            return false;
        }
        // no filter is a match
        if (this.isEmpty(filter)) {
            return true;
        }
        // single filter => matches or not
        if (filter.getFiltervalue() != null) {
            return this.filterMatches(value, filter.getFiltervalue());
        }
        final var values = filter.getFiltervalues();
        // multiple filter: check for each => match one of
        if (values != null && !values.isEmpty()) {
            for (String f : filter.getFiltervalues()) {
                if (filterMatches(value, f)) {
                    return true;
                }
            }
        }
        // not empty and did not match
        return false;
    }

    protected boolean isEmpty(Filter filter) {
        if (filter == null) {
            return true;
        }
        Set<String> tmp = new HashSet<>();
        if (filter.getFiltervalue() != null && !filter.getFiltervalue().isBlank()) {
            tmp.add(filter.getFiltervalue());
        }
        final var values = filter.getFiltervalues();
        if (values != null && !values.isEmpty()) {
            tmp.addAll(values);
        }
        return tmp.size() <= 0;
    }

    protected boolean filterMatches(String value, String filtervalue) {
        if (filtervalue.contains("*")) {
            filtervalue = filtervalue.replace("*", ".*");
        }
        if (filtervalue.contains("?")) {
            filtervalue = filtervalue.replace("?", ".");
        }
        /*Pattern pattern = Pattern.compile(filtervalue);
        return pattern.matcher(value).matches();*/
        return value.matches(filtervalue);
    }

    protected boolean filterMatches(Uint16 value, String filtervalue) {
        if (filtervalue.startsWith(">=")) {
            try {
                return value.intValue() >= Integer.parseInt(filtervalue.substring(2));
            } catch (NumberFormatException e) {
                LOG.warn("bad numeric filter value {}", filtervalue);
            }
        } else if (filtervalue.startsWith(">")) {
            try {
                return value.intValue() > Integer.parseInt(filtervalue.substring(1));
            } catch (NumberFormatException e) {
                LOG.warn("bad numeric filter value {}", filtervalue);
            }
        } else if (filtervalue.startsWith("<=")) {
            try {
                return value.intValue() <= Integer.parseInt(filtervalue.substring(2));
            } catch (NumberFormatException e) {
                LOG.warn("bad numeric filter value {}", filtervalue);
            }
        } else if (filtervalue.startsWith("<")) {
            try {
                return value.intValue() < Integer.parseInt(filtervalue.substring(1));
            } catch (NumberFormatException e) {
                LOG.warn("bad numeric filter value {}", filtervalue);
            }
        }
        return filterMatches(String.valueOf(value.intValue()), filtervalue);
    }

    @Override
    public ReadInventoryListOutputBuilder readInventoryList(EntityInput input) {
        return new ReadInventoryListOutputBuilder();
    }

    @Override
    public ReadConnectionlogListOutputBuilder readConnectionlogList(EntityInput input) {
        return new ReadConnectionlogListOutputBuilder();
    }

    @Override
    public ReadEventlogListOutputBuilder readEventlogList(EntityInput input) {
        return new ReadEventlogListOutputBuilder();
    }

    @Override
    public ReadPmdata15mListOutputBuilder readPmdata15mList(EntityInput input) {
        return new ReadPmdata15mListOutputBuilder();
    }

    @Override
    public ReadPmdata24hListOutputBuilder readPmdata24hList(EntityInput input) {
        return new ReadPmdata24hListOutputBuilder();
    }

    @Override
    public ReadPmdata15mLtpListOutputBuilder readPmdata15mLtpList(EntityInput input) {
        return new ReadPmdata15mLtpListOutputBuilder();
    }

    @Override
    public ReadPmdata15mDeviceListOutputBuilder readPmdata15mDeviceList(EntityInput input) {
        return new ReadPmdata15mDeviceListOutputBuilder();
    }

    @Override
    public ReadPmdata24hLtpListOutputBuilder readPmdata24hLtpList(EntityInput input) {
        return new ReadPmdata24hLtpListOutputBuilder();
    }

    @Override
    public ReadPmdata24hDeviceListOutputBuilder readPmdata24hDeviceList(EntityInput input) {
        return new ReadPmdata24hDeviceListOutputBuilder();
    }

    @Override
    public ReadStatusOutputBuilder readStatus(EntityInput input) {
        return new ReadStatusOutputBuilder();
    }

    @Override
    public boolean waitForYellowDatabaseStatus(long timeout, TimeUnit unit) {
        return true;
    }

    @Override
    public CreateNetworkElementConnectionOutputBuilder createNetworkElementConnection(
            NetworkElementConnectionEntity input) {
        return new CreateNetworkElementConnectionOutputBuilder();
    }

    @Override
    public UpdateNetworkElementConnectionOutputBuilder updateNetworkElementConnection(
            UpdateNetworkElementConnectionInput input) {
        return new UpdateNetworkElementConnectionOutputBuilder();
    }

    @Override
    public DeleteNetworkElementConnectionOutputBuilder deleteNetworkElementConnection(
            DeleteNetworkElementConnectionInput input) {
        return new DeleteNetworkElementConnectionOutputBuilder();
    }

    @Override
    public DeleteMaintenanceOutputBuilder deleteMaintenance(DeleteMaintenanceInput input) {
        return new DeleteMaintenanceOutputBuilder();
    }

    @Override
    public UpdateMaintenanceOutputBuilder updateMaintenance(UpdateMaintenanceInput input) {
        return new UpdateMaintenanceOutputBuilder();
    }

    @Override
    public CreateMaintenanceOutputBuilder createMaintenance(CreateMaintenanceInput input) {
        return new CreateMaintenanceOutputBuilder();
    }

    @Override
    public ReadGuiCutThroughEntryOutputBuilder readGuiCutThroughEntry(EntityInput input) {
        return new ReadGuiCutThroughEntryOutputBuilder().setData(List.of())
                .setPagination(
                        new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.gui.cut.through.entry.output.PaginationBuilder().setPage(
                                Uint64.valueOf(1)).setSize(Uint32.valueOf(0)).setTotal(Uint64.valueOf(0)).build());
    }

    @Override
    public DataProvider getDataProvider() {
        return dataprovider;
    }

    @Override
    public HtDatabaseMaintenance getHtDatabaseMaintenance() {
        return this.maintenance;
    }


    @Override
    public HtUserdataManager getUserManager() {
        return this.usermanger;
    }

    @Override
    public InventoryTreeProvider getInventoryTreeProvider() {
        return this.inventoryTreeProvider;
    }

    @Override
    public ReadInventoryDeviceListOutputBuilder readInventoryDeviceList(EntityInput input) {
        return new ReadInventoryDeviceListOutputBuilder();
    }

    private static class NodeDataProviderComparator implements Comparator<Node> {

        private final Collection<Sortorder> sortOrders;

        public NodeDataProviderComparator(@Nullable Map<SortorderKey, Sortorder> sortorder) {
            this.sortOrders = sortorder != null ? sortorder.values() : null;
        }

        @Override
        public int compare(Node n1, Node n2) {
            if (this.sortOrders == null || sortOrders.size() <= 0) {
                return 0;
            }
            final var nn1 = getNetconfNode(n1);
            final var nn2 = getNetconfNode(n2);
            if (nn1 == null && nn2 == null) {
                return 0;
            }
            if (nn1 == null) {
                return 1;
            }
            if (nn2 == null) {
                return -1;
            }

            int tmpResult = 0;
            for (Sortorder o : this.sortOrders) {
                switch (o.getProperty()) {
                    case "node-id": {
                        tmpResult = n1.getNodeId().getValue().compareTo(n2.getNodeId().getValue());
                        break;
                    }
                    case "host": {
                        tmpResult = nn1.getHost().stringValue().compareTo(nn2.getHost().stringValue());
                        break;
                    }
                    case "port": {
                        tmpResult = Integer.compare(nn1.getPort().getValue().intValue(),
                                nn2.getPort().getValue().intValue());
                        break;
                    }
                    case "core-model-capability": {
                        break;
                    }
                    case "device-type": {
                        break;
                    }
                    case "device-function": {
                        break;
                    }
                    case "is-required": {
                        break;
                    }
                    case "status": {
                        tmpResult = nn1.getConnectionStatus().getName().compareTo(nn2.getConnectionStatus().getName());
                        break;
                    }
                    case "tls-key": {
                        break;
                    }
                    case "mount-method": {
                        break;
                    }
                }
                if (tmpResult != 0) {
                    return tmpResult;
                }
            }
            return 0;
        }
    }
}
