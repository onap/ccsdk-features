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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.database.mariadb.data.rpctypehelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.common.YangHelper;
import org.onap.ccsdk.features.sdnr.wt.common.database.data.DbFilter;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.BoolQueryBuilder;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilder;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilders;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.RangeQueryBuilder;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.NetconfTimeStamp;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.NetconfTimeStampImpl;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.YangHelper2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EntityInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SortOrder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.Filter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.Pagination;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.Sortorder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MariaDBQueryByFilter {

    private static final Logger LOG = LoggerFactory.getLogger(MariaDBQueryByFilter.class);
    private static final List<String> timestampValueNames = Arrays.asList("timestamp", "time-stamp", "start", "end");

    private static List<Sortorder> emptySortOrderList = new ArrayList<>();
    private static List<Filter> emptyFilterList = new ArrayList<>();

    // Derived from input
    private long page;
    private long pageSize;
    private long fromPage;
    private List<Filter> filterList;
    private List<Sortorder> sortOrder;

    /**
     * Process input from RPC into Queries to database
     *
     * @param input Input from RPC, for test it could be null
     */
    public MariaDBQueryByFilter(EntityInput input) {
        page = -1;
        pageSize = -1;
        if (input != null) {
            @Nullable
            Pagination pagination = input.getPagination();
            if (pagination != null) {
                @Nullable Uint64 pageOrNull = YangHelper2.getUint64(pagination.getPage());
                if (pageOrNull != null) {
                    page = pageOrNull.longValue();
                }
                @Nullable Uint32 pageSizeOrNull = YangHelper2.getUint32(pagination.getSize());
                if (pageSizeOrNull != null) {
                    pageSize = pageSizeOrNull.longValue();
                }
            }
        }
        if (page < 0)
            page = 1;
        if (pageSize < 0)
            pageSize = 1;

        fromPage = (page - 1) * pageSize;
        if (fromPage < 0 || pageSize > 10000)
            throw new IllegalArgumentException("mismatching input parameters. From:" + fromPage + " size:" + pageSize);

        filterList = YangHelper.getList(input.getFilter());
        if (filterList == null)
            filterList = emptyFilterList;
        sortOrder = YangHelper.getList(input.getSortorder());
        if (sortOrder == null)
            sortOrder = emptySortOrderList;

    }

    public QueryBuilder getQueryBuilderByFilter() {
        return getQueryBuilderByFilter("");
    }

    public QueryBuilder getQueryBuilderByFilter(String prefix) {
        QueryBuilder queryBuilder = fromFilter(filterList, prefix).from(fromPage).size(pageSize);
        setSortOrder(queryBuilder, sortOrder, prefix);
        return queryBuilder;
    }

    public long getPage() {
        return page;
    }

    public long getPageSize() {
        return pageSize;
    }

    public long getPageStartIndex() {
        return fromPage;
    }

    @Override
    public String toString() {
        return "QueryByFilter [page=" + page + ", pageSize=" + pageSize + ", fromPage=" + fromPage + ", filterList="
                + filterList + ", sortOrder=" + sortOrder + "]";
    }

    /*
     * Private and static implementations
     */
    private static QueryBuilder setSortOrder(QueryBuilder query, @Nullable List<Sortorder> sortorder, String prefix) {
        if (sortorder != null && sortorder.size() > 0) {
            for (Sortorder so : sortorder) {
                query.sort(handlePrefix(prefix, so.getProperty()), convert(so.getSortorder()));
            }
        }
        return query;
    }

    private static org.onap.ccsdk.features.sdnr.wt.common.database.queries.SortOrder convert(SortOrder sortOrder) {
        return sortOrder == SortOrder.Ascending
                ? org.onap.ccsdk.features.sdnr.wt.common.database.queries.SortOrder.ASCENDING
                : org.onap.ccsdk.features.sdnr.wt.common.database.queries.SortOrder.DESCENDING;
    };

    private static String fillTimeStamp(String value) {
        int idx = value.lastIndexOf("*");
        final String REPLACE = "0000-00-00T00:00:00.0Z";
        String s = value.substring(0, idx) + REPLACE.substring(idx);
        if (Integer.parseInt(s.substring(5, 7)) == 0) {
            s = s.substring(0, 5) + "01-" + s.substring(8);
        }
        if (Integer.parseInt(s.substring(8, 10)) == 0) {
            s = s.substring(0, 8) + "01" + s.substring(10);
        }

        return s;
    }

    /**
     * convert timestamp with ending placeholder in filter to elasticsearch filter e.g. 2017* => gte:
     * 2017-01-01T00:00:00Z, lt:2018-01-01T00:00:00Z
     *
     * 201* => 2010-01... 2020 .. 2018-* => 2018-01... <=> 2019-01
     *
     */
    private static @Nullable QueryBuilder fromTimestampSearchFilter(String property, String value) {
        if (!value.endsWith("*")) {
            return null;
        }
        int idx = value.lastIndexOf("*");
        String lowerEnd = fillTimeStamp(value);
        String upperEnd = null;
        NetconfTimeStamp converter = NetconfTimeStampImpl.getConverter();
        Date dt = null;
        try {
            dt = converter.getDateFromNetconf(lowerEnd);
        } catch (Exception e) {

        }
        if (dt == null) {
            return null;
        }
        //        property.substring(0,idx)+REPLACE.substring(idx+1);
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.setTime(dt);
        int tmpvalue;
        switch (idx) {
            case 1: // (2*)
                c.set(Calendar.YEAR, c.get(Calendar.YEAR) + 1000);
                upperEnd = converter.getTimeStampAsNetconfString(c.getTime());
                break;
            case 2: // (20*)
                c.set(Calendar.YEAR, c.get(Calendar.YEAR) + 100);
                upperEnd = converter.getTimeStampAsNetconfString(c.getTime());
                break;
            case 3: // (200*)
                c.set(Calendar.YEAR, c.get(Calendar.YEAR) + 10);
                upperEnd = converter.getTimeStampAsNetconfString(c.getTime());
                break;
            case 4: // (2000*)
            case 5: // (2000-*)
                c.set(Calendar.YEAR, c.get(Calendar.YEAR) + 1);
                upperEnd = converter.getTimeStampAsNetconfString(c.getTime());
                break;
            case 6: // switch 10 months (2000-0* or 2000-1*)
                tmpvalue = c.get(Calendar.MONTH);
                if (tmpvalue < 9) {
                    c.set(Calendar.MONTH, 9);
                } else {
                    c.set(Calendar.YEAR, c.get(Calendar.YEAR) + 1);
                    c.set(Calendar.MONTH, 0);
                }
                upperEnd = converter.getTimeStampAsNetconfString(c.getTime());

                break;
            case 7: // switch one month (2018-01* or 2018-01-*)
            case 8:
                c.add(Calendar.MONTH, 1);
                upperEnd = converter.getTimeStampAsNetconfString(c.getTime());
                break;
            case 9: // (2018-01-0*)
                tmpvalue = c.get(Calendar.DAY_OF_MONTH);
                if (tmpvalue == 1) {
                    c.set(Calendar.DAY_OF_MONTH, 10);
                } else if (tmpvalue == 10) {
                    c.set(Calendar.DAY_OF_MONTH, 20);
                } else if (tmpvalue == 20) {
                    if (c.getActualMaximum(Calendar.DAY_OF_MONTH) < 30) {
                        c.set(Calendar.DAY_OF_MONTH, 1);
                        c.add(Calendar.MONTH, 1);
                    } else {
                        c.set(Calendar.DAY_OF_MONTH, 30);
                    }
                } else if (tmpvalue == 30) {
                    c.set(Calendar.DAY_OF_MONTH, 1);
                    c.add(Calendar.MONTH, 1);
                } else {
                    break;
                }
                upperEnd = converter.getTimeStampAsNetconfString(c.getTime());
                break;
            case 10: // (2018-01-01*)
            case 11: // (2018-01-01T*)
                c.add(Calendar.DAY_OF_MONTH, 1);
                upperEnd = converter.getTimeStampAsNetconfString(c.getTime());
                break;
            case 12: // (2018-01-01T1*)
                tmpvalue = c.get(Calendar.HOUR_OF_DAY);
                if (tmpvalue == 20) {
                    c.set(Calendar.HOUR_OF_DAY, 0);
                    c.add(Calendar.DAY_OF_MONTH, 1);
                } else {
                    c.add(Calendar.HOUR_OF_DAY, 10);
                }
                upperEnd = converter.getTimeStampAsNetconfString(c.getTime());
                break;
            case 13: // (2018-01-01T11*)
            case 14: // (2018-01-01T11-*)
                c.add(Calendar.HOUR_OF_DAY, 1);
                upperEnd = converter.getTimeStampAsNetconfString(c.getTime());
                break;
            case 15: // (2018-01-01T11-3*)
                c.add(Calendar.MINUTE, 10);
                upperEnd = converter.getTimeStampAsNetconfString(c.getTime());
                break;
            case 16: // (2018-01-01T11-32*)
            case 17: // (2018-01-01T11-32-*)
                c.add(Calendar.MINUTE, 1);
                upperEnd = converter.getTimeStampAsNetconfString(c.getTime());
                break;
            case 18: // (2018-01-01T11-32-1*)
                c.add(Calendar.SECOND, 10);
                upperEnd = converter.getTimeStampAsNetconfString(c.getTime());
                break;
            case 19: // (2018-01-01T11-32-11*)
            case 20: // (2018-01-01T11-32-11.*)
                c.add(Calendar.SECOND, 1);
                upperEnd = converter.getTimeStampAsNetconfString(c.getTime());
                break;

            default:
                break;
        }

        if (upperEnd == null) {
            return null;
        }
        return QueryBuilders.rangeQuery(property).gte(lowerEnd).lt(upperEnd);

    }

    private static QueryBuilder fromFilter(@Nullable List<Filter> filters, String prefix) {
        if (filters == null || filters.size() == 0) {
            return QueryBuilders.matchAllQuery();

        } else if (filters.size() == 1) {
            QueryBuilder query;
            String p = filters.get(0).getProperty();
            String v = filters.get(0).getFiltervalue();
            if ("id".equals(p)) {
                p = "_id";
            } else {
                //    v=v.toLowerCase();
            }
            if (DbFilter.hasSearchParams(v)) {
                if (p != null && timestampValueNames.contains(p.toLowerCase())) {
                    query = fromTimestampSearchFilter(p, v);
                    if (query != null) {
                        return query;
                    }
                }
                return QueryBuilders.regex(p, DbFilter.createDatabaseRegex(v));


            } else if (DbFilter.isComparisonValid(v)) {
                RangeQueryBuilder q = DbFilter.getRangeQuery(handlePrefix(prefix, p), v);
                if (q != null) {
                    return q;
                } else {
                    return QueryBuilders.matchQuery(handlePrefix(prefix, p), v);
                }
            } else {
                return QueryBuilders.matchQuery(handlePrefix(prefix, p), v);
            }
        } else {
            BoolQueryBuilder query = new BoolQueryBuilder();
            QueryBuilder tmpQuery;
            for (Filter fi : filters) {
                String p = fi.getProperty();
                String v = fi.getFiltervalue();
                if ("id".equals(p)) {
                    p = "_id";
                } else {
                    //    v=v.toLowerCase();
                }
                if (DbFilter.hasSearchParams(v)) {
                    if (p != null && timestampValueNames.contains(p.toLowerCase())) {
                        tmpQuery = fromTimestampSearchFilter(p, v);
                        if (tmpQuery != null) {
                            query.must(tmpQuery);
                        } else {
                            query.must(QueryBuilders.regex(handlePrefix(prefix, p), DbFilter.createDatabaseRegex(v)));
                        }
                    } else {
                        query.must(QueryBuilders.regex(handlePrefix(prefix, p), DbFilter.createDatabaseRegex(v)));
                    }
                } else if (DbFilter.isComparisonValid(v)) {
                    RangeQueryBuilder q = DbFilter.getRangeQuery(handlePrefix(prefix, p), v);
                    if (q != null) {
                        query.must(q);
                    } else {
                        query.must(QueryBuilders.matchQuery(handlePrefix(prefix, p), v));
                    }
                } else {
                    query.must(QueryBuilders.matchQuery(handlePrefix(prefix, p), v));
                }
            }
            LOG.trace("Query result. {}", query.toJSON());
            return query;
        }
    }

    private static String handlePrefix(String prefix, String p) {
        return (prefix != null ? prefix : "") + p;
    }

}
