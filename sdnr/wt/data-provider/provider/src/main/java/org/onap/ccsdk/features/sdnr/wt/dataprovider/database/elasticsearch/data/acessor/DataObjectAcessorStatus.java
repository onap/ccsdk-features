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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.database.elasticsearch.data.acessor;

import java.io.IOException;
import org.onap.ccsdk.features.sdnr.wt.common.database.ExtRestClient;
import org.onap.ccsdk.features.sdnr.wt.common.database.HtDatabaseClient;
import org.onap.ccsdk.features.sdnr.wt.common.database.SearchResult;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilder;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.SearchRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.AggregationEntries;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.SearchResponse;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.elasticsearch.data.rpctypehelper.QueryByFilter;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.elasticsearch.data.rpctypehelper.QueryResult;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.YangHelper2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ConnectionLogStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Entity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EntityInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.status.output.Data;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.status.output.DataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.status.entity.FaultsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.status.entity.NetworkElementConnectionsBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

@Deprecated
public class DataObjectAcessorStatus extends DataObjectAcessor<Data> {

    final String ESDATATYPE_FAULTCURRENT_SEVERITY_KEY = "severity";
    final String ESDATATYPE_NECON_CONNECTIONSTATE_KEY = "status";

    private final ExtRestClient dbClient;

    public DataObjectAcessorStatus(HtDatabaseClient dbClient, Entity entity, boolean doFullsizeRequests)
            throws ClassNotFoundException {
        super(dbClient, entity, Data.class, doFullsizeRequests);
        this.dbClient = dbClient;

    }

    public QueryResult<Data> getDataStatus(EntityInput input) throws IOException {
        SearchRequest request = getNewInstanceOfSearchRequest(Entity.Faultcurrent);
        QueryByFilter queryByFilter = new QueryByFilter(input);
        QueryBuilder query = queryByFilter.getQueryBuilderByFilter();
        query.aggregations(ESDATATYPE_FAULTCURRENT_SEVERITY_KEY).size(0);
        if(this.doFullsizeRequest) {
            query.doFullsizeRequest();
        }
        request.setQuery(query);
        SearchResponse response = this.dbClient.search(request);
        AggregationEntries aggs = response.getAggregations(ESDATATYPE_FAULTCURRENT_SEVERITY_KEY);

        DataBuilder builder = new DataBuilder().setFaults(
                new FaultsBuilder().setCriticals(YangHelper2.getLongOrUint32(aggs.getOrDefault("Critical", 0L)))
                        .setMajors(YangHelper2.getLongOrUint32(aggs.getOrDefault("Major", 0L)))
                        .setMinors(YangHelper2.getLongOrUint32(aggs.getOrDefault("Minor", 0L)))
                        .setWarnings(YangHelper2.getLongOrUint32(aggs.getOrDefault("Warning", 0L))).build());

        request = getNewInstanceOfSearchRequest(Entity.NetworkelementConnection);
        query.aggregations(ESDATATYPE_NECON_CONNECTIONSTATE_KEY).size(0);
        if(this.doFullsizeRequest) {
            query.doFullsizeRequest();
        }
        request.setQuery(query);
        response = this.dbClient.search(request);
        aggs = response.getAggregations(ESDATATYPE_NECON_CONNECTIONSTATE_KEY);
        builder.setNetworkElementConnections(new NetworkElementConnectionsBuilder()
                .setConnected(Uint32.valueOf(aggs.getOrDefault(ConnectionLogStatus.Connected.getName(), 0L)))
                .setConnecting(Uint32.valueOf(aggs.getOrDefault(ConnectionLogStatus.Connecting.getName(), 0L)))
                .setDisconnected(Uint32.valueOf(aggs.getOrDefault(ConnectionLogStatus.Disconnected.getName(), 0L)))
                .setUnableToConnect(
                        Uint32.valueOf(aggs.getOrDefault(ConnectionLogStatus.UnableToConnect.getName(), 0L)))
                .setMounted(Uint32.valueOf(aggs.getOrDefault(ConnectionLogStatus.Mounted.getName(), 0L)))
                .setUnmounted(Uint32.valueOf(aggs.getOrDefault(ConnectionLogStatus.Unmounted.getName(), 0L)))
                .setUndefined(Uint32.valueOf(aggs.getOrDefault(ConnectionLogStatus.Undefined.getName(), 0L)))
                .setTotal(Uint32.valueOf(response.getTotal())).build());

        long toalsize = 1;
        return new QueryResult<Data>(1L, 1L, new SearchResult<Data>(new Data[] {builder.build()}, toalsize));

    }


    private static SearchRequest getNewInstanceOfSearchRequest(Entity entity) {
        return new SearchRequest(entity.getName(), entity.getName());
    }


}
