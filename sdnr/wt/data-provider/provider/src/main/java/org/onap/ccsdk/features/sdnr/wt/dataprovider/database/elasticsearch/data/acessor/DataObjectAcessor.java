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

import org.onap.ccsdk.features.sdnr.wt.common.database.HtDatabaseClient;
import org.onap.ccsdk.features.sdnr.wt.common.database.SearchResult;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilder;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.elasticsearch.EsDataObjectReaderWriter2;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.elasticsearch.data.rpctypehelper.QueryByFilter;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.elasticsearch.data.rpctypehelper.QueryResult;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Entity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EntityInput;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class DataObjectAcessor<T extends DataObject> extends EsDataObjectReaderWriter2<T> {

    private static final Logger LOG = LoggerFactory.getLogger(DataObjectAcessor.class);

    DataObjectAcessor(HtDatabaseClient dbClient, Entity entity, Class<T> clazz, boolean doFullsizeRequest)
            throws ClassNotFoundException {
        super(dbClient, entity, clazz);
        this.setFullsizeRequest(doFullsizeRequest);
        LOG.info("Create {}", this.getClass().getName());
    }

    public QueryResult<T> getData(EntityInput input) {

        QueryByFilter queryByFilter = new QueryByFilter(input);
        QueryBuilder queryBuilder = queryByFilter.getQueryBuilderByFilter();
        // Exception handling during user input for invalid filter input.
        // This wrong filter by user is allowed an results into empty data.
        boolean ignoreException = queryBuilder.contains("range");

        LOG.debug("Request: {} filter {} ignoreException{}:", this.getDataTypeName(), queryByFilter, ignoreException);

        SearchResult<T> result = doReadAll(queryBuilder, ignoreException);
        return new QueryResult<>(queryByFilter, result);
    }

}
