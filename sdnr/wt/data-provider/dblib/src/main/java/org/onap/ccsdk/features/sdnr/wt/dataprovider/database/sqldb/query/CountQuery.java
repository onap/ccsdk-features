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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.database.SqlDBMapper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Entity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EntityInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.Filter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.FilterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.FilterKey;

public class CountQuery implements SqlQuery {

    private final Entity entity;
    private final List<Filter> filters;
    private final String countField;
    public CountQuery(Entity e) {
        this(e, "*", null);
    }
    public CountQuery(Entity e, String controllerId) {
        this(e, "*", controllerId);
    }
    public CountQuery(Entity e, String countField, String controllerId) {
        this.entity = e;
        this.countField = countField;
        this.filters = new ArrayList<>();
        if (controllerId != null) {
            this.addFilter(SqlDBMapper.ODLID_DBCOL, controllerId);
        }
    }

    public CountQuery(Entity e, EntityInput input) {
        this(e, input, null);
    }

    public CountQuery(Entity e, EntityInput input, String controllerId) {
        this(e);
        Map<FilterKey, Filter> filter = input != null ? input.getFilter() : null;
        if (filter != null && filter.size() > 0) {
           this.filters.addAll(filter.values());
        }
        if (controllerId != null) {
            this.addFilter(SqlDBMapper.ODLID_DBCOL, controllerId);
        }
    }

    public void addFilter(String property, String filtervalue) {
        this.filters.add(new FilterBuilder().setProperty(property).setFiltervalue(filtervalue).build());

    }

    @Override
    public String toSql() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SELECT COUNT(`%s`) FROM `%s`", this.countField, this.entity.getName()));
        sb.append(SqlQuery.getWhereExpression(this.filters));
        return sb.toString();
    }

}
