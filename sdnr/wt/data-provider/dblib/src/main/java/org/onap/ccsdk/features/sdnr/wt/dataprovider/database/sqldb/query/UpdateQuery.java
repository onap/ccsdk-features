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
import org.opendaylight.yangtools.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateQuery<T extends DataObject> implements SqlQuery {

    private final Logger LOG = LoggerFactory.getLogger(UpdateQuery.class);

    private final Entity entity;
    private final String controllerId;
    private final T object;
    private final boolean ignoreNull;
    private String id;

    public UpdateQuery(Entity e, T object) {
        this(e, object, null, SqlQuery.DEFAULT_IGNORE_CONTROLLERID, SqlQuery.DEFAULT_IGNORE_ID_FIELD);
    }

    public UpdateQuery(Entity e, T object, String controllerId, boolean ignoreControllerId, boolean ignoreIdField) {
        this.entity = e;
        this.controllerId = controllerId;
        this.object = object;
        this.ignoreNull = true;
        this.id = null;
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

    private String toSqlWithError() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            JsonProcessingException {
        Class<?> cls = this.object.getClass();
        Object value;
        String col;
        StringBuilder sb = new StringBuilder();
        List<String> args = new ArrayList<>();
        sb.append("UPDATE `" + entity.getName() + "` SET ");
        List<Method> methods = SqlDBMapper.getFilteredMethods(cls, true);
        Method m;
        for (int i = 0; i < methods.size(); i++) {
            m = methods.get(i);
            m.setAccessible(true);
            value = m.invoke(this.object);
            col = SqlDBMapper.getColumnName(m);
            if (col.equals("id")) {
                if (this.id == null) {
                    this.id = String.valueOf(value);
                }
                continue;
            }
            if (ignoreNull && value == null) {
                continue;
            }
            DBKeyValuePair<String> kvp = SqlDBMapper.getEscapedKeyValue(m, col, value);
            args.add(String.format("%s=%s", kvp.getKey(), kvp.getValue()));
        }
        sb.append(String.join(",", args));
        sb.append(String.format(" WHERE `id`='%s'", this.id));
        if (this.controllerId != null) {
            sb.append(String.format(" AND `%s`='%s'", SqlDBMapper.ODLID_DBCOL, this.controllerId));
        }

        return sb.toString();
    }

    public void setId(String id) {
        this.id = id;
    }



}
