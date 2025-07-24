/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2023 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.SqlDBClient;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.database.SqlDBReaderWriter;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.BaseInventoryTreeProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.YangHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Entity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EntityInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Inventory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.InventoryEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadInventoryListInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SortOrder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.Filter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.FilterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.FilterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.Sortorder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.SortorderBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.SortorderKey;

public class SqlDbInventoryTreeProvider extends BaseInventoryTreeProvider {
    private final SqlDBReaderWriter<Inventory> dbReader;

    public SqlDbInventoryTreeProvider(SqlDBClient dbClient, String controllerId) {
        this.dbReader = new SqlDBReaderWriter<>(dbClient, Entity.Inventoryequipment, "", Inventory.class, controllerId);
    }

    @Override
    protected List<String> getAllNodeIds() {
        return this.dbReader.readAll(INVENTORY_PROPERTY_NODEID);
    }

    @Override
    protected List<InventoryEntity> search(String filter, String sortOrderProperty, SortOrder sortOrder) {
        return new ArrayList<>(this.dbReader.searchAll(Inventory.class, null, filter));
    }

    @Override
    protected List<InventoryEntity> search(String filter, String nodeId, String parentUuid, String uuid,
            String sortOrderProperty, SortOrder sortOrder) {
        List<Filter> filters = new ArrayList<>();
        if (nodeId != null) {
            filters.add(new FilterBuilder().setProperty(INVENTORY_PROPERTY_NODEID).setFiltervalue(nodeId).build());
        }
        EntityInput input = new ReadInventoryListInputBuilder()
                .setFilter(YangHelper.getListOrMap(FilterKey.class, filters)).build();
        return new ArrayList<>(this.dbReader.searchAll(Inventory.class, input, filter));
    }

    @Override
    protected List<InventoryEntity> getItemsForNodes(List<String> nodeIds, String sortOrderProperty,
            SortOrder sortOrder) {
        Map<FilterKey, Filter> nodeFilter = new HashMap<>();
        Filter filter = new FilterBuilder().setProperty(INVENTORY_PROPERTY_NODEID)
                .setFiltervalues(nodeIds.stream().collect(Collectors.toSet())).build();
        nodeFilter.put(filter.key(), filter);
        Map<SortorderKey, Sortorder> so = new HashMap<>();
        Sortorder soItem = new SortorderBuilder().setProperty(sortOrderProperty)
                .setSortorder(sortOrder).build();
        so.put(soItem.key(), soItem);
        EntityInput input = new ReadInventoryListInputBuilder().setFilter(nodeFilter).setSortorder(so).build();
        return new ArrayList<>(this.dbReader.readAll(Inventory.class, input));
    }
}
