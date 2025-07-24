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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.SqlDBClient;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data.entity.DatabaseIdGenerator;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data.rpctypehelper.QueryResult;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.query.SelectQuery;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Entity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EntityInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.PmdataEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.Filter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.FilterKey;
import org.opendaylight.yangtools.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlDBReaderWriterPm<T extends DataObject> extends SqlDBReaderWriter<T> {

    private final Logger LOG = LoggerFactory.getLogger(SqlDBReaderWriterPm.class);

    private static final String UUID_KEY = "uuid-interface";
    private static final String NODE_KEY = "node-name";
    private static final String KEY = "node-name";

    private static final FilterKey FILTERKEY = new FilterKey(KEY);

    public SqlDBReaderWriterPm(SqlDBClient dbService, Entity e, String dbSuffix, Class<T> clazz,
            String controllerId) {
        super(dbService, e, dbSuffix, clazz, controllerId);
    }

    /**
     * get aggregated list of ltps for filter NODE_KEY
     *
     * @param input
     * @return
     * @throws IOException
     */
    public QueryResult<String> getDataLtpList(EntityInput input) throws IOException {

        SelectQuery query = new SelectQuery(this.tableName, UUID_KEY, this.controllerId).groupBy(UUID_KEY);
        query.setPagination(input.getPagination());
        Map<FilterKey, Filter> filter = input.nonnullFilter();
        if (!filter.containsKey(FILTERKEY)) {
            String msg = "no node-name in filter found ";
            LOG.debug(msg);
            throw new IllegalArgumentException(msg);
        }
        for (Filter f : filter.values()) {
            query.addFilter(f.getProperty(), f.getFiltervalue());
        }


        try {
            ResultSet data = this.dbService.read(query.toSql());
            List<String> mappedData = SqlDBMapper.read(data, String.class, UUID_KEY);
            try { data.close(); } catch (SQLException ignore) { }
            Map<FilterKey, Filter> inpFilter = input.getFilter();
            long total = this.count(inpFilter != null ? new ArrayList<>(inpFilter.values()) : null, this.controllerId);
            return new QueryResult<>(mappedData, query.getPage(), query.getPageSize(), total);
        } catch (SQLException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | InstantiationException | SecurityException | NoSuchMethodException | JsonProcessingException e) {
            LOG.warn("problem reading ltp list: ", e);
        }
        return null;
    }

    /**
     * get aggregated devices list
     *
     * @param input filter should be empty/no filter handled, only sortorder for KEY ('node-name')
     * @return
     * @throws IOException
     */
    public QueryResult<String> getDataDeviceList(EntityInput input) throws IOException {

        SelectQuery query = new SelectQuery(this.tableName, NODE_KEY, this.controllerId).groupBy(NODE_KEY);
        query.setPagination(input.getPagination());
        Map<FilterKey, Filter> filter = input.getFilter();
        if (filter != null) {
            for (Filter f : filter.values()) {
                query.addFilter(f.getProperty(), f.getFiltervalue());
            }
        }

        try {
            ResultSet data = this.dbService.read(query.toSql());
            List<String> mappedData = SqlDBMapper.read(data, String.class, NODE_KEY);
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

    public void write(PmdataEntity pmData) {
        DateAndTime date = pmData.getTimeStamp();
        final String id = DatabaseIdGenerator.getPmData15mId(pmData.getNodeName(), pmData.getUuidInterface(),
                date != null ? date.getValue() : "null");
        this.updateOrInsert(pmData, id);
    }

}
