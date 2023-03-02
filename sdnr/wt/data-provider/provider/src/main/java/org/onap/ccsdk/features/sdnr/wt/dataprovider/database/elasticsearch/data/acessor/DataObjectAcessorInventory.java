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
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.SearchRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.AggregationEntries;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.SearchResponse;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.elasticsearch.data.rpctypehelper.QueryByFilter;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.elasticsearch.data.rpctypehelper.QueryResult;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Entity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EntityInput;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataObjectAcessorInventory<T extends DataObject> extends DataObjectAcessorWithId<T> {

    private final Logger LOG = LoggerFactory.getLogger(DataObjectAcessorInventory.class);

    private static final String KEY = "node-id";

    private ExtRestClient dbClient;

    public DataObjectAcessorInventory(HtDatabaseClient dbClient, Entity entity, Class<T> clazz,
                                      boolean doFullsizeRequest) throws ClassNotFoundException {
        super(dbClient, entity, clazz, doFullsizeRequest);
        LOG.info("Create DataObjectAcessorInventory");
        this.dbClient = dbClient;
    }


    /**
     * get aggregated devices list
     *
     * @param input filter should be empty/no filter handled, only sortorder for KEY ('node-name')
     * @return
     * @throws IOException
     */
    public QueryResult<String> getDataDeviceList(EntityInput input) {

        QueryByFilter queryByFilter = new QueryByFilter(input);
        SearchRequest request =
                queryByFilter.getSearchRequestAggregated(KEY,this.getDataTypeName(), this.getDataTypeName(), this.doFullsizeRequest);
        try {
            SearchResponse response = this.dbClient.search(request);
            AggregationEntries aggs = response.getAggregations(KEY);
            String[] uuids =
                    aggs.getKeysAsPagedStringList(queryByFilter.getPageSize(), queryByFilter.getPageStartIndex());
            long totalSize = aggs.size();
            return new QueryResult<String>(queryByFilter.getPage(), queryByFilter.getPageSize(),
                    new SearchResult<String>(uuids, totalSize));
        } catch (IOException e) {
            throw new RuntimeException("problem reading nodes for req=" + request, e);
        }

    }

}
