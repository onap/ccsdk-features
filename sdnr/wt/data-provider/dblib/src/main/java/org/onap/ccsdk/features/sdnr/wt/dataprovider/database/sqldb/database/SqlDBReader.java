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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.database;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.SqlDBClient;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data.PropertyList;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data.rpctypehelper.QueryResult;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.query.SelectQuery;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.query.SqlQuery;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Entity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EntityInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.Filter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.FilterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.FilterKey;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SqlDBReader<T extends DataObject> {
    private static final Logger LOG = LoggerFactory.getLogger(SqlDBReader.class);

    protected final Entity entity;
    private final Class<T> clazz;
    protected final SqlDBClient dbService;
    protected final String controllerId;
    protected final String tableName;
    protected final boolean ignoreControllerId;
    protected final PropertyList propertyList;
    public SqlDBReader(SqlDBClient dbService, Entity e, String dbSuffix, Class<T> clazz,
                       String controllerId) {
        this(dbService, e, dbSuffix, clazz, controllerId, false);
    }

    public SqlDBReader(SqlDBClient dbService, Entity e, String dbSuffix, Class<T> clazz,
                       String controllerId, boolean ignoreControllerId) {
        this.dbService = dbService;
        this.entity = e;
        this.clazz = clazz;
        this.tableName = this.entity.getName() + dbSuffix;
        this.controllerId = controllerId;
        this.ignoreControllerId = ignoreControllerId;
        this.propertyList = new PropertyList(clazz);
    }

    public long count(List<Filter> filter) throws SQLException {
        String query;
        if (filter == null || filter.isEmpty()) {
            //            query = String.format("SELECT table_rows FROM `information_schema`.`tables` "
            //                    + "WHERE `table_schema` = '%s' AND `table_name` = '%s'", this.dbName, this.tableName);
            query = String.format("SELECT COUNT(`id`) FROM `%s`", this.tableName);
        } else {
            query = String.format("SELECT COUNT(`id`) FROM `%s` %s", this.tableName,
                    SqlQuery.getWhereExpression(filter));
        }
        ResultSet data = this.dbService.read(query);
        if (data == null) {
            return 0;
        }
        long cnt = 0;
        if (data.next()) {
            cnt = data.getLong(1);
        }
        try {
            data.close();
        } catch (SQLException ignore) {
        }
        return cnt;
    }

    public long count(List<Filter> list, String controllerId) throws SQLException {
        if (list == null) {
            list = new ArrayList<>();
        }
        Optional<Filter> cFilter =
                list.stream().filter(e -> SqlDBMapper.ODLID_DBCOL.equals(e.getProperty())).findFirst();
        if (!cFilter.isEmpty()) {
            list.remove(cFilter.get());
        }
        if (controllerId != null) {
            list.add(
                    new FilterBuilder().setProperty(SqlDBMapper.ODLID_DBCOL).setFiltervalue(this.controllerId).build());
        }
        return this.count(list);
    }

    public QueryResult<T> getData(EntityInput input) {
        SelectQuery query = new SelectQuery(this.tableName, input, this.controllerId);
        if (LOG.isTraceEnabled()) {
            LOG.trace("query={}", query.toSql());
        }
        try {
            ResultSet data = this.dbService.read(query.toSql());
            List<T> mappedData = SqlDBMapper.read(data, clazz);
            final Map<FilterKey, Filter> filter = input.getFilter();
            try {
                if (data != null) {
                    data.close();
                }
            } catch (SQLException ignore) {
            }
            long total = this.count(filter != null ? new ArrayList<>(filter.values()) : null, this.controllerId);
            return new QueryResult<T>(mappedData, query.getPage(), query.getPageSize(), total);
        } catch (SQLException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                 | InstantiationException | SecurityException | NoSuchMethodException | JsonProcessingException e) {
            LOG.warn("problem reading data {}: ", this.entity, e);
        }
        return QueryResult.createEmpty();
    }

    public <S extends DataObject> List<S> readAll(Class<S> clazz) {
        SelectQuery query = new SelectQuery(this.tableName, this.controllerId);
        if (LOG.isTraceEnabled()) {
            LOG.trace("query={}", query.toSql());
        }
        return this.readAll(clazz, query);
    }

    public <S extends DataObject> List<S> readAll(Class<S> clazz, EntityInput input) {
        SelectQuery query = new SelectQuery(this.tableName, input, this.controllerId);
        return this.readAll(clazz, query);
    }
    public  <S extends DataObject> List<S> searchAll(Class<S> clazz, EntityInput input, String searchTerm) {
        SelectQuery query = new SelectQuery(this.tableName, input, this.controllerId);
        if(searchTerm!=null && !searchTerm.isEmpty()) {
            query.setAllPropertyFilter(searchTerm, this.propertyList);
        }
        return this.readAll(clazz, query);
    }
    public <S extends DataObject> List<S> readAll(Class<S> clazz, SelectQuery query) {
        try {
            ResultSet data = this.dbService.read(query.toSql());
            List<S> mappedData = SqlDBMapper.read(data, clazz);
            try {
                data.close();
            } catch (SQLException ignore) {
            }
            return mappedData;
        } catch (SQLException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                 | InstantiationException | SecurityException | NoSuchMethodException | JsonProcessingException e) {
            LOG.warn("problem reading all data{}: ", this.entity, e);
        }
        return null;
    }

    public List<String> readAll(String key) {
        SelectQuery query = new SelectQuery(this.tableName, key, this.controllerId).groupBy(key);
        if (LOG.isTraceEnabled()) {
            LOG.trace("query={}", query.toSql());
        }
        try {
            ResultSet data = this.dbService.read(query.toSql());
            List<String> mappedData = SqlDBMapper.read(data, String.class, key);
            try {
                data.close();
            } catch (SQLException ignore) {
            }
            return mappedData;
        } catch (SQLException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                 | InstantiationException | SecurityException | NoSuchMethodException | JsonProcessingException e) {
            LOG.warn("problem reading all data {} for key: ", this.entity, key, e);
        }
        return null;
    }

    public T read(String id) {
        SelectQuery query =
                new SelectQuery(this.tableName, this.controllerId).addFilter(SqlDBMapper.ID_DBCOL, id);
        if (LOG.isTraceEnabled()) {
            LOG.trace("query={}", query.toSql());
        }
        T item = null;
        try {
            ResultSet data = this.dbService.read(query.toSql());
            List<T> mappedData = SqlDBMapper.read(data, clazz);
            item = mappedData.size() > 0 ? mappedData.get(0) : null;
        } catch (SQLException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                 | InstantiationException | SecurityException | NoSuchMethodException | JsonProcessingException e) {
            LOG.warn("problem reading data {}: ", this.entity, e);
        }
        return item;
    }
}
