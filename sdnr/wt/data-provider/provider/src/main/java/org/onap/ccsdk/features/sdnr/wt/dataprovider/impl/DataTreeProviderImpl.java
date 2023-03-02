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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.impl;

import org.onap.ccsdk.features.sdnr.wt.common.database.HtDatabaseClient;
import org.onap.ccsdk.features.sdnr.wt.common.database.SearchResult;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.BoolQueryBuilder;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilder;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilders;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.SortOrder;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.SearchRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.AggregationEntries;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.SearchResponse;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.elasticsearch.data.acessor.DataObjectAcessorWithId;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.BaseInventoryTreeProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Entity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Inventory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.InventoryEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Dürre
 */
public class DataTreeProviderImpl extends BaseInventoryTreeProvider {

    private static final Logger LOG = LoggerFactory.getLogger(DataTreeProviderImpl.class);
    private static final long MAXSIZE_PERSEARCH = 10;
    private final HtDatabaseClient dbClient;
    private final DataObjectAcessorWithId<Inventory> dbReader;

    public DataTreeProviderImpl(HtDatabaseClient dbClient) {
        this.dbClient = dbClient;
        try {
            this.dbReader = new DataObjectAcessorWithId<>(dbClient,Entity.Inventoryequipment,Inventory.class,true);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private List<InventoryEntity> search(QueryBuilder query)  {

        query.size(MAXSIZE_PERSEARCH);
        SearchResult<Inventory> response = null;
        response = this.dbReader.doReadAll(query);
        List<Inventory> matches = response.getHits();
        List<InventoryEntity> list = new ArrayList<>();
       list.addAll(matches);
        if (response.getTotal() > MAXSIZE_PERSEARCH) {
            long todo = response.getTotal();
            long from = MAXSIZE_PERSEARCH;
            while (todo > from) {
                query.from(from);
                from += MAXSIZE_PERSEARCH;
                //merge into list
                response = this.dbReader.doReadAll(query);
                matches = response.getHits();
                list.addAll(matches);
            }
        }
        return list;
    }


    @Override
    protected List<String> getAllNodeIds() {
        QueryBuilder query = QueryBuilders.matchAllQuery().setFullsizeRequest(true).aggregations(INVENTORY_PROPERTY_NODEID).size(0);
        SearchRequest request = new SearchRequest(Entity.Inventoryequipment.getName(),Entity.Inventoryequipment.getName());
        request.setQuery(query);
        try {
            SearchResponse response = this.dbClient.search(request);
            AggregationEntries aggs = response.getAggregations(INVENTORY_PROPERTY_NODEID);
            return new ArrayList<>(aggs.keySet());
        } catch (IOException e) {
            LOG.warn("problem reading nodes of inventory: ", e);
        }
        return List.of();
    }

    @Override
    protected List<InventoryEntity> search(String filter, String sortOrderProperty, SortOrder sortOrder) {
        return this.search(filter, null, null, null, sortOrderProperty, sortOrder);
    }

    @Override
    protected List<InventoryEntity> search(String filter, String nodeId, String parentUuid, String uuid, String sortOrderProperty, SortOrder sortOrder) {
        QueryBuilder query =
                filter == null ? QueryBuilders.matchAllQuery() : QueryBuilders.searchAllFieldsQuery(filter);
        if ((nodeId != null) || (parentUuid != null)) {
            BoolQueryBuilder bquery = new BoolQueryBuilder();
            if (filter != null) {
                bquery.must(query);
            }
            if (nodeId != null) {
                bquery.must(QueryBuilders.matchQuery(INVENTORY_PROPERTY_NODEID, nodeId));
            }
            query = bquery;

        }
        query.sort(sortOrderProperty, sortOrder);
        return this.search(query);
    }

    @Override
    protected List<InventoryEntity> getItemsForNodes(List<String> nodeIds, String sortOrderProperty, SortOrder sortOrder) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        nodeIds.forEach(e->query.should(QueryBuilders.matchQuery(INVENTORY_PROPERTY_NODEID,e)));
        return this.search(query);
    }

}
