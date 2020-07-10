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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.data;

import java.io.IOException;

import org.onap.ccsdk.features.sdnr.wt.common.database.ExtRestClient;
import org.onap.ccsdk.features.sdnr.wt.common.database.HtDatabaseClient;
import org.onap.ccsdk.features.sdnr.wt.common.database.SearchResult;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilder;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilders;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.SearchRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.AggregationEntries;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.SearchResponse;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.Entity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.EntityInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.SortOrder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.entity.input.Filter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.entity.input.Sortorder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataObjectAcessorPm<T extends DataObject> extends DataObjectAcessor<T> {

    private final Logger LOG = LoggerFactory.getLogger(DataObjectAcessorPm.class);

    private static final String UUID_KEY = "uuid-interface";
    private static final String NODE_KEY = "node-name";
    private static final String KEY = "node-name";


    enum Intervall {
        PMDATA15M("historicalperformance15min", "historicalperformance15min"), PMDATA24H("historicalperformance24h",
                "historicalperformance24h");

        String index;
        String type;

        Intervall(String index, String type) {
            this.index = index;
            this.type = type;
        }

        public String getIndex() {
            return index;
        }

        public String getType() {
            return type;
        }
    }

    private ExtRestClient dbClient;
    private Intervall mode;

    public DataObjectAcessorPm(HtDatabaseClient dbClient, Intervall mode, Entity entity, Class<T> clazz)
            throws ClassNotFoundException {
        super(dbClient, entity, clazz, false);
        this.dbClient = dbClient;
        this.mode = mode;
    }

    /**
     * get aggregated list of ltps for filter NODE_KEY
     * 
     * @param input
     * @return
     * @throws IOException
     */
    QueryResult<String> getDataLtpList(EntityInput input) throws IOException {
        long page = QueryByFilter.getPage(input);
        long pageSize = QueryByFilter.getPageSize(input);
        Filter nodeFilter = QueryByFilter.getFilter(input.getFilter(), NODE_KEY);
        if (nodeFilter != null) {
            SearchRequest request = new SearchRequest(mode.getIndex(), mode.getType());
            request.setQuery(
                    QueryBuilders.matchQuery(NODE_KEY, nodeFilter.getFiltervalue()).aggregations(UUID_KEY).size(0));
            try {
                SearchResponse response = this.dbClient.search(request);
                AggregationEntries aggs = response.getAggregations(UUID_KEY);
                String[] uuids = aggs.getKeysAsPagedStringList(pageSize, pageSize * (page - 1));
                long totalSize = aggs.size();
                return new QueryResult<String>(page, pageSize, new SearchResult<String>(uuids, totalSize));
            } catch (IOException e) {
                throw new IOException("problem reading ltps for req=" + request, e);
            }
        } else {
            String msg = "no nodename in filter found ";
            LOG.debug(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    //	QueryResult<String> getDataDeviceList(EntityInput input) throws IOException {
    //
    //		long page = QueryByFilter.getPage(input);
    //		long pageSize = QueryByFilter.getPageSize(input);
    //
    //		SearchRequest request = new SearchRequest(mode.getIndex(), mode.getType());
    //		request.setQuery(QueryBuilders.matchAllQuery().aggregations(KEY).size(0));
    ////		try {
    //			SearchResponse response = this.dbClient.search(request);
    //			AggregationEntries aggs = response.getAggregations(KEY);
    //			String[] uuids = aggs.getKeysAsPagedStringList(pageSize, pageSize * (page - 1));
    //			long totalSize = aggs.size();
    //			return new QueryResult<String>(page, pageSize, new SearchResult<String>(uuids, totalSize));
    ////		} catch (IOException e) {
    ////			throw new IOException("problem reading nodes for req="+request, e);
    ////		}
    //	}
    /**
     * get aggregated devices list
     * 
     * @param input filter should be empty/no filter handled, only sortorder for KEY ('node-name')
     * @return
     * @throws IOException
     */
    QueryResult<String> getDataDeviceList(EntityInput input) throws IOException {

        long page = QueryByFilter.getPage(input);
        long pageSize = QueryByFilter.getPageSize(input);

        Sortorder soNode = QueryByFilter.getSortOrder(input.getSortorder(), KEY);
        SearchRequest request = new SearchRequest(mode.getIndex(), mode.getType());
        QueryBuilder query = null;
        if (soNode != null) {
            query = QueryBuilders.matchAllQuery()
                    .aggregations(KEY,
                            soNode.getSortorder() == SortOrder.Ascending
                                    ? org.onap.ccsdk.features.sdnr.wt.common.database.queries.SortOrder.ASCENDING
                                    : org.onap.ccsdk.features.sdnr.wt.common.database.queries.SortOrder.DESCENDING)
                    .size(0);
        } else {
            query = QueryBuilders.matchAllQuery().aggregations(KEY).size(0);
        }
        request.setQuery(query);
        try {
            SearchResponse response = this.dbClient.search(request);
            AggregationEntries aggs = response.getAggregations(KEY);
            String[] uuids = aggs.getKeysAsPagedStringList(pageSize, pageSize * (page - 1));
            long totalSize = aggs.size();
            return new QueryResult<String>(page, pageSize, new SearchResult<String>(uuids, totalSize));
        } catch (IOException e) {
            throw new IOException("problem reading nodes for req=" + request, e);
        }

    }

}
