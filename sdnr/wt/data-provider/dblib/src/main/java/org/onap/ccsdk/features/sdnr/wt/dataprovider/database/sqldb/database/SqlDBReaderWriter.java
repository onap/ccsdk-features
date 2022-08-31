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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.SqlDBClient;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data.rpctypehelper.QueryResult;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.query.DeleteQuery;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.query.InsertQuery;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.query.SelectQuery;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.query.SqlQuery;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.query.UpdateQuery;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.query.UpsertQuery;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Entity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EntityInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.Filter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.FilterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.FilterKey;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlDBReaderWriter<T extends DataObject> {

    private static final Logger LOG = LoggerFactory.getLogger(SqlDBReaderWriter.class);

    protected final Entity entity;
    private final Class<T> clazz;
    protected final SqlDBClient dbService;
    protected final String controllerId;
    protected final String tableName;
    private final boolean ignoreControllerId;

    public SqlDBReaderWriter(SqlDBClient dbService, Entity e, String dbSuffix, Class<T> clazz,
            String controllerId) {
        this(dbService, e, dbSuffix, clazz, controllerId, false);
    }

    public SqlDBReaderWriter(SqlDBClient dbService, Entity e, String dbSuffix, Class<T> clazz,
            String controllerId, boolean ignoreControllerId) {
        this.dbService = dbService;
        this.entity = e;
        this.clazz = clazz;
        this.tableName = this.entity.getName() + dbSuffix;
        this.controllerId = controllerId;
        this.ignoreControllerId = ignoreControllerId;
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
        if(data==null) {
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
                if(data!=null) {
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



    public <S extends DataObject> String write(S object, String id) {
        if (id == null) {
            return this.writeWithoutId(object);
        }
        InsertQuery<S> query = new InsertQuery<S>(this.entity, object, this.controllerId, this.ignoreControllerId);
        query.setId(id);
        if (LOG.isTraceEnabled()) {
            LOG.trace("query={}", query.toSql());
        }
        boolean success = false;
        try {
            success = this.dbService.write(query.toSql());
        } catch (SQLException e) {
            LOG.warn("problem writing data into db: ", e);
        }

        return success ? id : null;
    }

    private <S extends DataObject> String writeWithoutId(S object) {

        InsertQuery<S> query =
                new InsertQuery<S>(this.entity, object, this.controllerId, this.ignoreControllerId, true);
        if (LOG.isTraceEnabled()) {
            LOG.trace("query={}", query.toSql());
        }
        try {
            return this.dbService.writeAndReturnId(query.toSql());
        } catch (SQLException e) {
            LOG.warn("problem writing data into db: ", e);
        }
        return null;
    }

    public <S extends DataObject> String update(S object, String id) {
        UpdateQuery<S> query = new UpdateQuery<S>(this.entity, object, this.controllerId, this.ignoreControllerId, true);
        query.setId(id);
        if (LOG.isTraceEnabled()) {
            LOG.trace("query={}", query.toSql());
        }
        String insertedId = null;
        PreparedStatement stmt = null;
        Connection connection = null;
        try {
            connection = this.dbService.getConnection();
            stmt = connection.prepareStatement(query.toSql());
            stmt.execute();

            int affectedRows = stmt.getUpdateCount();
            connection.close();
            if (affectedRows > 0) {
                insertedId = id;
            }
            if (LOG.isTraceEnabled()) {
                LOG.trace("insertedid={}", insertedId);
            }
        } catch (SQLException e) {
            LOG.warn("problem writing data into db: ", e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    LOG.warn("problem closing sql statement: ", e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOG.warn("problem closing sql connection: ", e);
                }
            }
        }

        return insertedId;
    }

    public <S extends DataObject> String updateOrInsert(S object, String id) {
        UpsertQuery<S> query = new UpsertQuery<S>(this.entity, object, this.controllerId, this.ignoreControllerId, true);
        query.setId(id);
        String insertedId = null;
        if (LOG.isTraceEnabled()) {
            LOG.trace("query={}", query.toSql());
        }
        PreparedStatement stmt = null;
        Connection connection = null;
        try {
            connection = this.dbService.getConnection();
            stmt = connection.prepareStatement(query.toSql());
            stmt.execute();

            int affectedRows = stmt.getUpdateCount();
            connection.close();
            if (affectedRows > 0) {
                insertedId = id;
            }
        } catch (SQLException e) {
            LOG.warn("problem writing data into db: ", e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    LOG.warn("problem closing sql statement: ", e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOG.warn("problem closing sql connection: ", e);
                }
            }
        }
        return insertedId;
    }

    public SqlDBReaderWriter<T> setWriteInterface(Class<? extends DataObject> writeInterfaceClazz) {
        LOG.debug("Set write interface to {}", writeInterfaceClazz);
        if (writeInterfaceClazz == null) {
            throw new IllegalArgumentException("Null not allowed here.");
        }

        //      this.writeInterfaceClazz = writeInterfaceClazz;
        return this;
    }

    public int remove(List<Filter> filters) {
        DeleteQuery query = new DeleteQuery(this.entity, filters);
        if (LOG.isTraceEnabled()) {
            LOG.trace("query={}", query.toSql());
        }
        int affectedRows = 0;
        PreparedStatement stmt = null;
        Connection connection = null;
        try {
            connection = this.dbService.getConnection();
            stmt = connection.prepareStatement(query.toSql());
            stmt.execute();
            affectedRows = stmt.getUpdateCount();
            connection.close();
        } catch (SQLException e) {
            LOG.warn("problem execute delete query: ", e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    LOG.warn("problem closing sql statement: ", e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOG.warn("problem closing sql connection: ", e);
                }
            }
        }
        return affectedRows;
    }

    public int remove(@Nullable String id) {
        return this.remove(Arrays.asList(new FilterBuilder().setProperty("id").setFiltervalue(id).build()));
    }

    public <S extends DataObject> List<S> readAll(Class<S> clazz) {
        SelectQuery query = new SelectQuery(this.tableName);
        if (LOG.isTraceEnabled()) {
            LOG.trace("query={}", query.toSql());
        }
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
            item = mappedData.size()>0? mappedData.get(0): null;
        } catch (SQLException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | InstantiationException | SecurityException | NoSuchMethodException | JsonProcessingException e) {
            LOG.warn("problem reading data {}: ", this.entity, e);
        }
        return item;
    }
}
