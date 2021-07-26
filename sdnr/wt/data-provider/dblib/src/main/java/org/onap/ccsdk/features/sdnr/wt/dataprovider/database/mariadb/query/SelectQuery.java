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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.database.mariadb.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.mariadb.database.MariaDBMapper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EntityInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SortOrder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.Filter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.FilterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.FilterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.Pagination;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.Sortorder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.SortorderKey;

public class SelectQuery implements SqlQuery {

    private static final long DEFAULT_PAGESIZE = 20;
    private static final long DEFAULT_PAGE = 1;
    private static final List<String> TIMESTAMPPROPERTYNAMES = Arrays.asList("timestamp", "time-stamp", "start", "end");
    private final String tableName;
    private final List<Filter> filters;
    private final List<String> sortExpressions;
    private long page;
    private long pageSize;
    private final List<String> fields;
    private final List<String> groups;

    public SelectQuery(String tableName) {
        this(tableName, Arrays.asList("*"), null);
    }

    public SelectQuery(String tableName, List<String> fields, String controllerId) {
        this.tableName = tableName;
        this.fields = fields;
        this.filters = new ArrayList<>();
        this.sortExpressions = new ArrayList<>();
        this.groups = new ArrayList<>();
        this.page = DEFAULT_PAGE;
        this.pageSize = DEFAULT_PAGESIZE;
        if (controllerId != null) {
            this.addFilter(MariaDBMapper.ODLID_DBCOL, controllerId);
        }
    }

    public SelectQuery(String tableName, String field, String controllerId) {
        this(tableName, Arrays.asList(field), controllerId);
    }

    public SelectQuery(String tableName, EntityInput input) {
        this(tableName, input, null);
    }

    public SelectQuery(String tableName, EntityInput input, String controllerId) {
        this(tableName);
        Map<FilterKey, Filter> filter = input != null ? input.getFilter() : null;
        if (filter != null && filter.size() > 0) {
            for (Filter f : filter.values()) {
                this.addFilter(f.getProperty(), f.getFiltervalue());
            }
        }
        if (controllerId != null) {
            this.addFilter(MariaDBMapper.ODLID_DBCOL, controllerId);
        }

        Map<SortorderKey, Sortorder> so = input.getSortorder();
        if (so != null && so.size() > 0) {
            for (Sortorder s : so.values()) {
                this.addSortOrder(s.getProperty(),
                        s.getSortorder() == SortOrder.Ascending ? "ASC" : "DESC");
            }
        }
        Pagination pagination = input.getPagination();
        if (pagination != null) {
            this.setPagination(pagination.getPage().longValue(), pagination.getSize().longValue());
        } else {
            this.setPagination(1, 30);
        }

    }

    public void addFilter(String property, String filtervalue) {
        this.filters.add(new FilterBuilder().setProperty(property).setFiltervalue(filtervalue).build());

    }

    public void addSortOrder(String col, String order) {
        this.sortExpressions.add(String.format("`%s` %s", col, order));
    }

    public void setPagination(long page, long pageSize) {
        this.page = page;
        this.pageSize = pageSize;
    }

    public void setPagination(@Nullable Pagination pagination) {
        long page = DEFAULT_PAGE;
        long pageSize = DEFAULT_PAGESIZE;
        if(pagination!=null) {
            if(pagination.getPage()!=null) {
                page = pagination.getPage().longValue();
            }
            if(pagination.getSize()!=null) {
                pageSize = pagination.getSize().longValue();
            }
        }
        this.setPagination(page, pageSize);

    }

    @Override
    public String toSql() {
        StringBuilder sb = new StringBuilder();
        if (this.fields.size() == 1 && this.fields.contains("*")) {
            sb.append(String.format("SELECT * FROM `%s`", this.tableName));
        } else {
            sb.append(String.format("SELECT `%s` FROM `%s`", String.join("`,`", this.fields), this.tableName));
        }
        sb.append(SqlQuery.getWhereExpression(this.filters));
        if (this.groups.size() > 0) {
            sb.append(String.format(" GROUP BY `%s`", String.join("`,`", this.groups)));
        }
        if (this.sortExpressions.size() > 0) {
            sb.append(" ORDER BY " + String.join(",", this.sortExpressions));
        }
        sb.append(String.format(" LIMIT %d,%d;", (this.page - 1) * this.pageSize, this.pageSize));
        return sb.toString();
    }

    public long getPage() {
        return this.page;
    }

    public long getPageSize() {
        return this.pageSize;
    }

    public SelectQuery groupBy(String group) {
        this.groups.add(group);
        return this;
    }

}
