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

import org.onap.ccsdk.features.sdnr.wt.common.database.HtDatabaseClient;
import org.onap.ccsdk.features.sdnr.wt.common.database.SearchResult;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilder;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.EsDataObjectReaderWriter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.Entity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.EntityInput;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataObjectAcessor<T extends DataObject> extends EsDataObjectReaderWriter<T> {

    private static final Logger LOG = LoggerFactory.getLogger(DataObjectAcessor.class);

    public DataObjectAcessor(HtDatabaseClient dbClient, Entity entity, Class<T> clazz) throws ClassNotFoundException {
        this(dbClient, entity, clazz, true);
        LOG.info("Create {}", this.getClass().getName());
    }

    public DataObjectAcessor(HtDatabaseClient dbClient, Entity entity, Class<T> clazz, boolean idSupported)
            throws ClassNotFoundException {
        super(dbClient, entity, clazz);
        if (idSupported) {
            setEsIdAttributeName("_id");
        }
    }

    QueryResult<T> getData(EntityInput input) {
        long page = QueryByFilter.getPage(input);
        long pageSize = QueryByFilter.getPageSize(input);
        LOG.info("Request: {}", this.getDataTypeName());
        QueryBuilder query = QueryByFilter.fromFilter(input.getFilter()).from((page - 1) * pageSize).size(pageSize);
        QueryByFilter.setSortOrder(query, input.getSortorder());
        SearchResult<T> result = doReadAll(query, query.contains("range"));
        return new QueryResult<>(page, pageSize, result);
    }

}
