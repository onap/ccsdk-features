/* ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.dblib.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.query.CountQuery;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.query.SelectQuery;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.YangToolsMapperHelper;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Entity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EntityInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadEventlogListInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadGuiCutThroughEntryInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.Filter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.FilterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.FilterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.PaginationBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;

public class TestQuerySyntax {


    private static final String TABLENAME1 = "table1";
    private static final String CONTROLLERID = "controllerid1";

    @Test
    public void testTimestampFilter() {
        Map<FilterKey, Filter> filterMap = new HashMap<>();
        Filter filter = new FilterBuilder().setProperty("timestamp").setFiltervalue("2021*").build();
        filterMap.put(filter.key(), filter);
        EntityInput input = new ReadEventlogListInputBuilder().setFilter(filterMap).build();
        SelectQuery query = new SelectQuery(TABLENAME1, input, CONTROLLERID);
        final String sql = query.toSql();
        assertTrue(sql.contains("`timestamp`>='2021-01-01 00:00:00.000'"));
        assertTrue(sql.contains("`timestamp`<'2022-01-01 00:00:00.0'"));
    }

    @Test
    public void testTimestampFilter2() {
        Map<FilterKey, Filter> filterMap = new HashMap<>();
        Filter filter = new FilterBuilder().setProperty("timestamp").setFiltervalue(">2021").build();
        filterMap.put(filter.key(), filter);
        EntityInput input = new ReadEventlogListInputBuilder().setFilter(filterMap).build();
        SelectQuery query = new SelectQuery(TABLENAME1, input, CONTROLLERID);
        final String sql = query.toSql();
        assertTrue(sql.contains("`timestamp`>='2022-01-01 00:00:00.0'"));
    }

    @Test
    public void testTimestampFilter3() {
        Map<FilterKey, Filter> filterMap = new HashMap<>();
        Filter filter = new FilterBuilder().setProperty("timestamp").setFiltervalue(">=2021").build();
        filterMap.put(filter.key(), filter);
        EntityInput input = new ReadEventlogListInputBuilder().setFilter(filterMap).build();
        SelectQuery query = new SelectQuery(TABLENAME1, input, CONTROLLERID);
        final String sql = query.toSql();
        assertTrue(sql.contains("`timestamp`>='2021-01-01 00:00:00.000'"));

    }

    @Test
    public void testTimestampFilter4() {
        Map<FilterKey, Filter> filterMap = new HashMap<>();
        Filter filter = new FilterBuilder().setProperty("timestamp").setFiltervalue("<2021").build();
        filterMap.put(filter.key(), filter);
        EntityInput input = new ReadEventlogListInputBuilder().setFilter(filterMap).build();
        SelectQuery query = new SelectQuery(TABLENAME1, input, CONTROLLERID);
        final String sql = query.toSql();
        assertTrue(sql.contains("`timestamp`<'2021-01-01 00:00:00.000'"));
    }

    @Test
    public void testTimestampFilter5() {
        Map<FilterKey, Filter> filterMap = new HashMap<>();
        Filter filter = new FilterBuilder().setProperty("timestamp").setFiltervalue("<=2021").build();
        filterMap.put(filter.key(), filter);
        EntityInput input = new ReadEventlogListInputBuilder().setFilter(filterMap).build();
        SelectQuery query = new SelectQuery(TABLENAME1, input, CONTROLLERID);
        final String sql = query.toSql();
        assertTrue(sql.contains("`timestamp`<'2022-01-01 00:00:00.0'"));
    }

    @Test
    public void testTimestampFilter6() {
        Map<FilterKey, Filter> filterMap = new HashMap<>();
        Filter filter =
                new FilterBuilder().setProperty("timestamp").setFiltervalue(">=2022-01-01T00:00:00.000Z").build();
        filterMap.put(filter.key(), filter);
        EntityInput input = new ReadEventlogListInputBuilder().setFilter(filterMap).build();
        SelectQuery query = new SelectQuery(TABLENAME1, input, CONTROLLERID);
        final String sql = query.toSql();
        assertTrue(sql.contains("`timestamp`>='2022-01-01 00:00:00.000'"));
    }

    @Test
    public void testTimestampFilter8() {
        Map<FilterKey, Filter> filterMap = new HashMap<>();
        Filter filter =
                new FilterBuilder().setProperty("timestamp").setFiltervalue(">2022-01-01T00:00:00.000Z").build();
        filterMap.put(filter.key(), filter);
        EntityInput input = new ReadEventlogListInputBuilder().setFilter(filterMap).build();
        SelectQuery query = new SelectQuery(TABLENAME1, input, CONTROLLERID);
        final String sql = query.toSql();
        assertTrue(sql.contains("`timestamp`>'2022-01-01 00:00:00.000'"));
    }

    @Test
    public void testTimestampFilter9() {
        Map<FilterKey, Filter> filterMap = new HashMap<>();
        Filter filter =
                new FilterBuilder().setProperty("timestamp").setFiltervalue("<2022-01-01T00:00:00.000Z").build();
        filterMap.put(filter.key(), filter);
        EntityInput input = new ReadEventlogListInputBuilder().setFilter(filterMap).build();
        SelectQuery query = new SelectQuery(TABLENAME1, input, CONTROLLERID);
        final String sql = query.toSql();
        assertTrue(sql.contains("`timestamp`<'2022-01-01 00:00:00.000'"));
    }

    @Test
    public void testTimestampFilter10() {
        Map<FilterKey, Filter> filterMap = new HashMap<>();
        Filter filter =
                new FilterBuilder().setProperty("timestamp").setFiltervalue("<=2022-01-01T00:00:00.000Z").build();
        filterMap.put(filter.key(), filter);
        EntityInput input = new ReadEventlogListInputBuilder().setFilter(filterMap).build();
        SelectQuery query = new SelectQuery(TABLENAME1, input, CONTROLLERID);
        final String sql = query.toSql();
        assertTrue(sql.contains("`timestamp`<='2022-01-01 00:00:00.000'"));
    }

    @Test
    public void testTimestampFilter11() {
        Map<FilterKey, Filter> filterMap = new HashMap<>();
        Filter filter =
                new FilterBuilder().setProperty("timestamp").setFiltervalue("<=2022-01-01T00:00:00.000222Z").build();
        filterMap.put(filter.key(), filter);
        EntityInput input = new ReadEventlogListInputBuilder().setFilter(filterMap).build();
        SelectQuery query = new SelectQuery(TABLENAME1, input, CONTROLLERID);
        final String sql = query.toSql();
        assertTrue(sql.contains("`timestamp`<='2022-01-01 00:00:00.000'"));
    }

    @Test
    public void testSelectForFilterValues() {
        EntityInput input = new ReadGuiCutThroughEntryInputBuilder()
                .setFilter(YangToolsMapperHelper.toMap(Arrays.asList(
                        new FilterBuilder().setProperty("id").setFiltervalues(Set.of("das", "das2")).build())))
                .setPagination(new PaginationBuilder().setSize(Uint32.valueOf(20)).setPage(Uint64.valueOf(1)).build())
                .build();
        SelectQuery query = new SelectQuery(TABLENAME1, input, CONTROLLERID);
        assertEquals("SELECT * FROM `table1` WHERE `id`='das2' OR `id`='das' AND `controller-id`='controllerid1' LIMIT 0,20;", query.toSql());
    }
    @Test
    public void testSelectForFilterValues2() {
        EntityInput input = new ReadGuiCutThroughEntryInputBuilder()
                .setFilter(YangToolsMapperHelper.toMap(Arrays.asList(
                        new FilterBuilder().setProperty("id").setFiltervalue("*").build())))
                .setPagination(new PaginationBuilder().setSize(Uint32.valueOf(20)).setPage(Uint64.valueOf(1)).build())
                .build();
        SelectQuery query = new SelectQuery(TABLENAME1, input, CONTROLLERID);
        System.out.println(query.toSql());
        assertFalse(query.toSql().contains("RLIKE"));
    }
    @Test
    public void testSelectForFilterValues3() {
        EntityInput input = new ReadGuiCutThroughEntryInputBuilder()
                .setFilter(YangToolsMapperHelper.toMap(Arrays.asList(
                        new FilterBuilder().setProperty("id").setFiltervalues(Set.of("*","abc")).build())))
                .setPagination(new PaginationBuilder().setSize(Uint32.valueOf(20)).setPage(Uint64.valueOf(1)).build())
                .build();
        SelectQuery query = new SelectQuery(TABLENAME1, input, CONTROLLERID);
        System.out.println(query.toSql());
        assertFalse(query.toSql().contains("RLIKE"));
    }
    @Test
    public void testSelectForFilterValues4() {
        EntityInput input = new ReadGuiCutThroughEntryInputBuilder()
                .setFilter(YangToolsMapperHelper.toMap(Arrays.asList(
                        new FilterBuilder().setProperty("id").setFiltervalues(Set.of("abc")).build(),
                        new FilterBuilder().setProperty("node-id").setFiltervalues(Set.of("*")).build())))
                .setPagination(new PaginationBuilder().setSize(Uint32.valueOf(20)).setPage(Uint64.valueOf(1)).build())
                .build();
        SelectQuery query = new SelectQuery(TABLENAME1, input, CONTROLLERID);
        System.out.println(query.toSql());
        assertFalse(query.toSql().contains("RLIKE"));
    }
    @Test
    public void testSelectForFilterValues5() {
        EntityInput input = new ReadGuiCutThroughEntryInputBuilder()
                .setFilter(YangToolsMapperHelper.toMap(Arrays.asList(
                        new FilterBuilder().setProperty("id").setFiltervalues(Set.of("abc")).build(),
                        new FilterBuilder().setProperty("node-id").setFiltervalues(Set.of("*ran*")).build())))
                .setPagination(new PaginationBuilder().setSize(Uint32.valueOf(20)).setPage(Uint64.valueOf(1)).build())
                .build();
        SelectQuery query = new SelectQuery(TABLENAME1, input, CONTROLLERID);
        System.out.println(query.toSql());
        assertTrue(query.toSql().contains("RLIKE"));
        assertTrue(query.toSql().contains(".*"));

    }
    @Test
    public void testSelectForFilterValues6() {
        EntityInput input = new ReadGuiCutThroughEntryInputBuilder()
                .setFilter(YangToolsMapperHelper.toMap(Arrays.asList(
                        new FilterBuilder().setProperty("id").setFiltervalue("*ran*").build())))
                .setPagination(new PaginationBuilder().setSize(Uint32.valueOf(20)).setPage(Uint64.valueOf(1)).build())
                .build();
        SelectQuery query = new SelectQuery(TABLENAME1, input, CONTROLLERID);
        System.out.println(query.toSql());
        assertTrue(query.toSql().contains("RLIKE"));
        assertTrue(query.toSql().contains(".*"));

    }

    @Test
    public void testCount() {
        CountQuery query = new CountQuery(Entity.Eventlog, TestMariaDataProvider.createInput(1, 20));
        String sQuery = query.toSql();
        assertNotNull(sQuery);
        assertTrue(sQuery.contains("*") && sQuery.contains("COUNT") && sQuery.contains(Entity.Eventlog.getName()));

        query = new CountQuery(Entity.Eventlog, TestMariaDataProvider.createInput("node-id","abc",1, 20));
        sQuery = query.toSql();
        assertNotNull(sQuery);
        assertTrue(sQuery.contains("node-id") && sQuery.contains("COUNT") && sQuery.contains(Entity.Eventlog.getName()));


    }
}
