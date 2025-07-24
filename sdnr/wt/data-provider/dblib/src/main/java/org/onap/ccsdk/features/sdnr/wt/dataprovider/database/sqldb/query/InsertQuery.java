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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.query;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.database.SqlDBMapper;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.query.filters.DBKeyValuePair;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Entity;
import org.opendaylight.yangtools.binding.DataContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsertQuery<T extends DataContainer> implements SqlQuery {

    private final Logger LOG = LoggerFactory.getLogger(InsertQuery.class);

    protected final Entity entity;
    private final String controllerId;
    private final boolean ignoreControllerId;
    private final T object;
    private final boolean ignoreNull;
    private String id;
    private final boolean ignoreIdField;

    public InsertQuery(Entity e, T object, String controllerId) {
        this(e, object, controllerId, SqlQuery.DEFAULT_IGNORE_CONTROLLERID);
    }

    public InsertQuery(Entity e, T object, String controllerId, boolean ignoreControllerId) {
        this(e, object, controllerId, ignoreControllerId, SqlQuery.DEFAULT_IGNORE_ID_FIELD);
    }

    public InsertQuery(Entity e, T object, String controllerId, boolean ignoreControllerId, boolean ignoreIdField) {
        this.entity = e;
        this.controllerId = controllerId;
        this.object = object;
        this.ignoreNull = true;
        this.id = null;
        this.ignoreControllerId = ignoreControllerId;
        this.ignoreIdField = ignoreIdField;
    }

    @Override
    public String toSql() {
        try {
            return this.toSqlWithError();
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | JsonProcessingException e) {
            LOG.warn("unable to create insert statement for table {} from object {}: ", this.entity, this.object, e);
        }
        return null;
    }

    protected String toSqlWithError() throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, JsonProcessingException {
        Class<?> cls = this.object.getClass();
        List<DBKeyValuePair<String>> kvps = new ArrayList<>();
        List<String> cols = new ArrayList<>();
        List<String> args = new ArrayList<>();
        Object value;
        String col;
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO `" + entity.getName() + "` (");
        for (Method m : SqlDBMapper.getFilteredMethods(cls, true)) {

            m.setAccessible(true);
            value = m.invoke(this.object);
            col = SqlDBMapper.getColumnName(m);
            if (col.equals("id")) {
                if (this.ignoreIdField) {
                    continue;
                }
                if (this.id != null) {
                    value = this.id;
                }
            }
            if (ignoreNull && value == null) {
                continue;
            }
            DBKeyValuePair<String> kvp = SqlDBMapper.getEscapedKeyValue(m, col, value);
            cols.add(kvp.getKey());
            args.add(kvp.getValue());
            kvps.add(kvp);
        }
        if (this.id != null && !cols.contains("`id`")) {
            cols.add("`id`");
            args.add("'" + SqlDBMapper.escape(this.id) + "'");
        }
        if (!this.ignoreControllerId) {
            args.add("'" + this.controllerId + "'");
        }
        sb.append(String.join(",", cols));
        if (!this.ignoreControllerId) {
            sb.append(",`" + SqlDBMapper.ODLID_DBCOL + "`) VALUES (");
        } else {
            sb.append(") VALUES (");
        }
        sb.append(String.join(",", args) + " )");
        this.appendAdditionalToQuery(sb, kvps);
        return sb.toString();
    }

    protected void appendAdditionalToQuery(StringBuilder sb, List<DBKeyValuePair<String>> keyValues) {

    }

    public void setId(String id) {
        this.id = id;
    }
}
