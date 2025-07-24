/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2021 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.database;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.SqlDBClient;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data.rpctypehelper.QueryResult;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.query.SelectQuery;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Entity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EntityInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.Filter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.FilterKey;
import org.opendaylight.yangtools.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlDBReaderWriterInventory<T extends DataObject> extends SqlDBReaderWriter<T> {

    private final Logger LOG = LoggerFactory.getLogger(SqlDBReaderWriterInventory.class);

   private static final String KEY = "node-id";

    private static final FilterKey FILTERKEY = new FilterKey(KEY);

    public SqlDBReaderWriterInventory(SqlDBClient dbService, Entity e, String dbSuffix, Class<T> clazz,
                                      String controllerId) {
        super(dbService, e, dbSuffix, clazz, controllerId);
    }

    /**
     * get aggregated devices list
     *
     * @param input filter should be empty/no filter handled, only sortorder for KEY ('node-name')
     * @return
     */
    public QueryResult<String> getDataDeviceList(EntityInput input) {

        SelectQuery query = new SelectQuery(this.tableName, KEY, this.controllerId).groupBy(KEY);
        query.setPagination(input.getPagination());
        Map<FilterKey, Filter> filter = input.getFilter();
        if (filter != null) {
            for (Filter f : filter.values()) {
                query.addFilter(f.getProperty(), f.getFiltervalue());
            }
        }

        try {
            ResultSet data = this.dbService.read(query.toSql());
            List<String> mappedData = SqlDBMapper.read(data, String.class, KEY);
            try { data.close(); } catch (SQLException ignore) { }
            Map<FilterKey, Filter> inpFilter = input.getFilter();
            long total = this.count(inpFilter != null ? new ArrayList<>(inpFilter.values()) : null, this.controllerId);
            return new QueryResult<>(mappedData, query.getPage(), query.getPageSize(), total);
        } catch (SQLException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | InstantiationException | SecurityException | NoSuchMethodException | JsonProcessingException e) {
            LOG.warn("problem reading device list: ", e);
        }
        return null;
    }


}
