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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.common.database.data.DbFilter;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.query.filters.DBFilterKeyValuePair;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.query.filters.RangeSqlDBFilter;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.query.filters.RegexSqlDBFilter;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.NetconfTimeStamp;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.NetconfTimeStampImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.Filter;

public interface SqlQuery {

    String toSql();

    static final List<String> TIMESTAMPPROPERTYNAMES = Arrays.asList("timestamp", "time-stamp", "start", "end");
    static final String MARIADB_TIMESTAMP_REPLACER = "0000-00-00 00:00:00.000";
    static final String NETCONF_TIMESTAMP_REPLACER = "0000-00-00T00:00:00.000Z";
    static final String MARIADB_TIMESTAMP_REPLACER_MIN = "0000-00-00 00:00:00";
    static final int MARIADB_TIMESTAMP_REPLACER_MIN_LENGTH = MARIADB_TIMESTAMP_REPLACER_MIN.length();
    static final int MARIADB_TIMESTAMP_REPLACER_MAX_LENGTH = MARIADB_TIMESTAMP_REPLACER.length();

    public static String getWhereExpression(List<Filter> filters) {
        if (filters == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (filters.size() > 0) {

            sb.append(" WHERE (" + getFilterExpression(filters.get(0)) + ")");
            for (int i = 1; i < filters.size(); i++) {
                sb.append(" AND (" + getFilterExpression(filters.get(i)) + ")");
            }
        }
        return sb.toString();
    }

    public static String getFilterExpression(Filter filter) {
        String property = filter.getProperty();
        List<String> values = collectValues(filter.getFiltervalue(), filter.getFiltervalues());
        if (values.size() == 1) {
            return getFilterExpression(property, values.get(0));
        } else if (values.size() > 1) {
            StringBuilder sb = new StringBuilder();
            sb.append(getFilterExpression(property, values.get(0)));
            for (int i = 1; i < values.size(); i++) {
                sb.append(" OR ");
                sb.append(getFilterExpression(property, values.get(i)));
            }
            return sb.toString();
        }
        return null;
    }

    public static String getFilterExpression(String property, String value) {
        String filter = null;;
        if (DbFilter.hasSearchParams(value)) {
            if (TIMESTAMPPROPERTYNAMES.contains(property.toLowerCase())) {
                if (DbFilter.isComparisonValid(value)) {
                    filter = getComparisonFilter(property, value, true);
                } else {
                    filter = fromTimestampSearchFilter(property, value);
                }
                if (filter != null) {
                    return filter;
                }
            }
            return new RegexSqlDBFilter(property, value).getFilterExpression();
        } else if (DbFilter.isComparisonValid(value)) {
            filter = getComparisonFilter(property, value, TIMESTAMPPROPERTYNAMES.contains(property.toLowerCase()));
            if (filter != null) {
                return filter;
            }
        }
        return new DBFilterKeyValuePair(property, value).getFilterExpression();
    }

    static List<String> collectValues(String filtervalue, List<String> filtervalues) {
        if (filtervalues == null) {
            return Arrays.asList(filtervalue);
        }
        List<String> values = new ArrayList<>();
        if (filtervalue != null) {
            values.add(filtervalue);
        }
        values.addAll(filtervalues);
        return values;
    }

    private static String getComparisonFilter(String property, String filtervalue, boolean asTimeStamp) {
        filtervalue = filtervalue.trim();
        String comparator = null;
        Object value;
        if (filtervalue.startsWith(">=")) {
            comparator = ">=";
            filtervalue = filtervalue.substring(2).trim();
            if (asTimeStamp) {
                filtervalue = netconfToMariaDBTimestamp(fillTimeStamp(
                        filtervalue.endsWith("*") ? filtervalue : (filtervalue + "*"), MARIADB_TIMESTAMP_REPLACER));
            }
        } else if (filtervalue.startsWith(">")) {
            comparator = ">";
            filtervalue = filtervalue.substring(1).trim();
            if (asTimeStamp) {
                if (isFullTimestamp(filtervalue)) {
                    filtervalue = netconfToMariaDBTimestamp(filtervalue);
                } else {
                    comparator = ">=";
                    filtervalue = netconfToMariaDBTimestamp(
                            fillTimeStamp(filtervalue.endsWith("*") ? filtervalue : (filtervalue + "*"),
                                    NETCONF_TIMESTAMP_REPLACER, true));
                }
            }
        } else if (filtervalue.startsWith("<=")) {
            comparator = "<=";
            filtervalue = filtervalue.substring(2).trim();
            if (asTimeStamp) {
                if (isFullTimestamp(filtervalue)) {
                    filtervalue = netconfToMariaDBTimestamp(filtervalue);
                } else {
                    comparator = "<";
                    filtervalue = netconfToMariaDBTimestamp(
                            fillTimeStamp(filtervalue.endsWith("*") ? filtervalue : (filtervalue + "*"),
                                    NETCONF_TIMESTAMP_REPLACER, true));
                }
            }
        } else if (filtervalue.startsWith("<")) {
            comparator = "<";
            filtervalue = filtervalue.substring(1).trim();
            if (asTimeStamp) {
                filtervalue = netconfToMariaDBTimestamp(fillTimeStamp(
                        filtervalue.endsWith("*") ? filtervalue : (filtervalue + "*"), MARIADB_TIMESTAMP_REPLACER));
            }
        } else {
            return null;
        }
        value = filtervalue;
        return new RangeSqlDBFilter(property, value, comparator).getFilterExpression();
    }

    static boolean isFullTimestamp(String v) {
        return v.length() >= MARIADB_TIMESTAMP_REPLACER_MIN_LENGTH;
    }

    /**
     * Convert timestamp beginning filter expression like 2017* to a full qualified timestamp like '2017-01-01
     * 00:00:00'.
     *
     * @param value filter input value
     * @return fully qualified timestamp
     */
    private static String fillTimeStamp(String value) {
        return fillTimeStamp(value, NETCONF_TIMESTAMP_REPLACER);
    }

    private static String fillTimeStamp(String value, String replacer) {
        return fillTimeStamp(value, replacer, false);
    }

    private static String fillTimeStamp(String value, String replacer, boolean useUpperEnd) {
        int idx = value.lastIndexOf("*");
        String s = null;
        if (idx > replacer.length()) {
            s = value.substring(0, replacer.length());
        } else {
            s = value.substring(0, idx) + replacer.substring(idx);
        }
        //if month is zero => set to 1
        if (Integer.parseInt(s.substring(5, 7)) == 0) {
            s = s.substring(0, 5) + "01-" + s.substring(8);
        }
        //if day is zero => set to 1
        if (Integer.parseInt(s.substring(8, 10)) == 0) {
            s = s.substring(0, 8) + "01" + s.substring(10);
        }
        if (useUpperEnd) {
            s = getTimestampUpperLimit(s, idx);
        }
        return s;
    }

    /**
     * convert timestamp with ending placeholder in filter to elasticsearch filter e.g. 2017* => gte: 2017-01-01
     * 00:00:00, lt:2018-01-01 00:00:00Z
     *
     * 201* => 2010-01... 2020 .. 2018-* => 2018-01... <=> 2019-01
     *
     */
    private static @Nullable String fromTimestampSearchFilter(String property, String value) {
        if (!value.endsWith("*")) {
            return null;
        }
        int idx = value.lastIndexOf("*");
        String lowerEnd = fillTimeStamp(value);
        String upperEnd = getTimestampUpperLimit(fillTimeStamp(value, "0000-00-00T00:00:00.0Z"), idx);
        return RangeSqlDBFilter.between(property, netconfToMariaDBTimestamp(lowerEnd), true,
                netconfToMariaDBTimestamp(upperEnd), false);
    }

        private static String netconfToMariaDBTimestamp(String ts) {
            String v = ts.replace("T", " ").replace("Z", "");
            return v.length() > MARIADB_TIMESTAMP_REPLACER_MAX_LENGTH
                    ? v.substring(0, MARIADB_TIMESTAMP_REPLACER_MAX_LENGTH)
                    : v;
        }

    private static String getTimestampUpperLimit(String lowerEnd, int idx) {

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
        return upperEnd;
    }

}
