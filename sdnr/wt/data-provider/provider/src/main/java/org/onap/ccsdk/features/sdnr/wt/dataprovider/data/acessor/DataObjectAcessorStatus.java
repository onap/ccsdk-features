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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.data.acessor;

import java.io.IOException;

import org.onap.ccsdk.features.sdnr.wt.common.database.ExtRestClient;
import org.onap.ccsdk.features.sdnr.wt.common.database.HtDatabaseClient;
import org.onap.ccsdk.features.sdnr.wt.common.database.SearchResult;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilders;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.SearchRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.AggregationEntries;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.SearchResponse;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.data.rpctypehelper.QueryResult;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.Entity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.status.output.Data;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.status.output.DataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.status.entity.FaultsBuilder;

public class DataObjectAcessorStatus extends DataObjectAcessor<Data> {

    final String ESDATATYPE_FAULTCURRENT_SEVERITY_KEY = "severity";

    private final ExtRestClient dbClient;
    private final Entity entity;

    public DataObjectAcessorStatus(HtDatabaseClient dbClient, Entity entity) throws ClassNotFoundException {
        super(dbClient, entity, Data.class);
        this.dbClient = dbClient;
        this.entity = entity;
    }

    public QueryResult<Data> getDataStatus() throws IOException {
        SearchRequest request = getNewInstanceOfSearchRequest(entity);
        request.setQuery(QueryBuilders.matchAllQuery().aggregations(ESDATATYPE_FAULTCURRENT_SEVERITY_KEY).size(0));
        SearchResponse response = this.dbClient.search(request);
        AggregationEntries aggs = response.getAggregations(ESDATATYPE_FAULTCURRENT_SEVERITY_KEY);

        Data[] data = {new DataBuilder().setFaults(new FaultsBuilder().setCriticals(aggs.getOrDefault("Critical", 0L))
                .setMajors(aggs.getOrDefault("Major", 0L)).setMinors(aggs.getOrDefault("Minor", 0L))
                .setWarnings(aggs.getOrDefault("Warning", 0L)).build()).build()};
        long toalsize = data.length;
        return new QueryResult<Data>(1L, 1L, new SearchResult<Data>(data, toalsize));

    }


    private static SearchRequest getNewInstanceOfSearchRequest(Entity entity) {
        return new SearchRequest(entity.getName(), entity.getName());
    }


}
