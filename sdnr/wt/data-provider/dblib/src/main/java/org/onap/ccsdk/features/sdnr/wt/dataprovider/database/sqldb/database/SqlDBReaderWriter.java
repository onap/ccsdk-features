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

import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.SqlDBClient;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.query.DeleteQuery;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.query.InsertQuery;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.query.UpdateQuery;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.query.UpsertQuery;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Entity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.Filter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.FilterBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class SqlDBReaderWriter<T extends DataObject> extends SqlDBReader<T> {

    private static final Logger LOG = LoggerFactory.getLogger(SqlDBReaderWriter.class);

    public SqlDBReaderWriter(SqlDBClient dbService, Entity e, String dbSuffix, Class<T> clazz,
            String controllerId) {
        super(dbService, e, dbSuffix, clazz, controllerId);
    }

    public SqlDBReaderWriter(SqlDBClient dbService, Entity e, String dbSuffix, Class<T> clazz,
            String controllerId, boolean ignoreControllerId) {
        super(dbService, e, dbSuffix, clazz, controllerId, ignoreControllerId);
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
}
