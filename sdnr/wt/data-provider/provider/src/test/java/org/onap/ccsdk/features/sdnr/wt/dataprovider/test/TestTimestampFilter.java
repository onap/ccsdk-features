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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilder;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.data.rpctypehelper.QueryByFilter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.EntityInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.entity.input.Filter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.entity.input.FilterBuilder;

public class TestTimestampFilter extends Mockito {

    @Test
    public void testTimestampRange() {
        final String PROPERTY_TIMESTAMP = "timestamp";
        List<Filter> filters =
                Arrays.asList(new FilterBuilder().setProperty(PROPERTY_TIMESTAMP).setFiltervalue("2017*").build());
        EntityInput input = mock(EntityInput.class);
        when(input.getFilter()).thenReturn(filters);
        QueryBuilder query = new QueryByFilter(input).getQueryBuilderByFilter();
        assertRange(query.getInner(), PROPERTY_TIMESTAMP, "2017-01-01T00:00:00.0Z", "2018-01-01T00:00:00.0Z");

        filters = Arrays.asList(new FilterBuilder().setProperty(PROPERTY_TIMESTAMP).setFiltervalue("2*").build());
        when(input.getFilter()).thenReturn(filters);
        query = new QueryByFilter(input).getQueryBuilderByFilter();
        assertRange(query.getInner(), PROPERTY_TIMESTAMP, "2000-01-01T00:00:00.0Z", "3000-01-01T00:00:00.0Z");

        filters = Arrays.asList(new FilterBuilder().setProperty(PROPERTY_TIMESTAMP).setFiltervalue("20*").build());
        when(input.getFilter()).thenReturn(filters);
        query = new QueryByFilter(input).getQueryBuilderByFilter();
        assertRange(query.getInner(), PROPERTY_TIMESTAMP, "2000-01-01T00:00:00.0Z", "2100-01-01T00:00:00.0Z");

        filters = Arrays.asList(new FilterBuilder().setProperty(PROPERTY_TIMESTAMP).setFiltervalue("205*").build());
        when(input.getFilter()).thenReturn(filters);
        query = new QueryByFilter(input).getQueryBuilderByFilter();
        assertRange(query.getInner(), PROPERTY_TIMESTAMP, "2050-01-01T00:00:00.0Z", "2060-01-01T00:00:00.0Z");

        filters = Arrays.asList(new FilterBuilder().setProperty(PROPERTY_TIMESTAMP).setFiltervalue("2050*").build());
        when(input.getFilter()).thenReturn(filters);
        query = new QueryByFilter(input).getQueryBuilderByFilter();
        assertRange(query.getInner(), PROPERTY_TIMESTAMP, "2050-01-01T00:00:00.0Z", "2051-01-01T00:00:00.0Z");

        filters = Arrays.asList(new FilterBuilder().setProperty(PROPERTY_TIMESTAMP).setFiltervalue("2050-*").build());
        when(input.getFilter()).thenReturn(filters);
        query = new QueryByFilter(input).getQueryBuilderByFilter();
        assertRange(query.getInner(), PROPERTY_TIMESTAMP, "2050-01-01T00:00:00.0Z", "2051-01-01T00:00:00.0Z");

        filters = Arrays.asList(new FilterBuilder().setProperty(PROPERTY_TIMESTAMP).setFiltervalue("2050-1*").build());
        when(input.getFilter()).thenReturn(filters);
        query = new QueryByFilter(input).getQueryBuilderByFilter();
        assertRange(query.getInner(), PROPERTY_TIMESTAMP, "2050-10-01T00:00:00.0Z", "2051-01-01T00:00:00.0Z");

        filters = Arrays.asList(new FilterBuilder().setProperty(PROPERTY_TIMESTAMP).setFiltervalue("2050-10*").build());
        when(input.getFilter()).thenReturn(filters);
        query = new QueryByFilter(input).getQueryBuilderByFilter();
        assertRange(query.getInner(), PROPERTY_TIMESTAMP, "2050-10-01T00:00:00.0Z", "2050-11-01T00:00:00.0Z");

        filters =
                Arrays.asList(new FilterBuilder().setProperty(PROPERTY_TIMESTAMP).setFiltervalue("2050-10-*").build());
        when(input.getFilter()).thenReturn(filters);
        query = new QueryByFilter(input).getQueryBuilderByFilter();
        assertRange(query.getInner(), PROPERTY_TIMESTAMP, "2050-10-01T00:00:00.0Z", "2050-11-01T00:00:00.0Z");

        filters =
                Arrays.asList(new FilterBuilder().setProperty(PROPERTY_TIMESTAMP).setFiltervalue("2050-10-0*").build());
        when(input.getFilter()).thenReturn(filters);
        query = new QueryByFilter(input).getQueryBuilderByFilter();
        assertRange(query.getInner(), PROPERTY_TIMESTAMP, "2050-10-01T00:00:00.0Z", "2050-10-10T00:00:00.0Z");

        filters = Arrays
                .asList(new FilterBuilder().setProperty(PROPERTY_TIMESTAMP).setFiltervalue("2050-10-02*").build());
        when(input.getFilter()).thenReturn(filters);
        query = new QueryByFilter(input).getQueryBuilderByFilter();
        assertRange(query.getInner(), PROPERTY_TIMESTAMP, "2050-10-02T00:00:00.0Z", "2050-10-03T00:00:00.0Z");

        filters = Arrays
                .asList(new FilterBuilder().setProperty(PROPERTY_TIMESTAMP).setFiltervalue("2050-10-14*").build());
        when(input.getFilter()).thenReturn(filters);
        query = new QueryByFilter(input).getQueryBuilderByFilter();
        assertRange(query.getInner(), PROPERTY_TIMESTAMP, "2050-10-14T00:00:00.0Z", "2050-10-15T00:00:00.0Z");

        filters = Arrays
                .asList(new FilterBuilder().setProperty(PROPERTY_TIMESTAMP).setFiltervalue("2050-10-14T*").build());
        when(input.getFilter()).thenReturn(filters);
        query = new QueryByFilter(input).getQueryBuilderByFilter();
        assertRange(query.getInner(), PROPERTY_TIMESTAMP, "2050-10-14T00:00:00.0Z", "2050-10-15T00:00:00.0Z");

        filters = Arrays
                .asList(new FilterBuilder().setProperty(PROPERTY_TIMESTAMP).setFiltervalue("2050-10-14T1*").build());
        when(input.getFilter()).thenReturn(filters);
        query = new QueryByFilter(input).getQueryBuilderByFilter();
        assertRange(query.getInner(), PROPERTY_TIMESTAMP, "2050-10-14T10:00:00.0Z", "2050-10-14T20:00:00.0Z");

        filters = Arrays
                .asList(new FilterBuilder().setProperty(PROPERTY_TIMESTAMP).setFiltervalue("2050-10-14T12*").build());
        when(input.getFilter()).thenReturn(filters);
        query = new QueryByFilter(input).getQueryBuilderByFilter();
        assertRange(query.getInner(), PROPERTY_TIMESTAMP, "2050-10-14T12:00:00.0Z", "2050-10-14T13:00:00.0Z");

        filters = Arrays
                .asList(new FilterBuilder().setProperty(PROPERTY_TIMESTAMP).setFiltervalue("2050-10-14T12:*").build());
        when(input.getFilter()).thenReturn(filters);
        query = new QueryByFilter(input).getQueryBuilderByFilter();
        assertRange(query.getInner(), PROPERTY_TIMESTAMP, "2050-10-14T12:00:00.0Z", "2050-10-14T13:00:00.0Z");

        filters = Arrays
                .asList(new FilterBuilder().setProperty(PROPERTY_TIMESTAMP).setFiltervalue("2050-10-14T12:4*").build());
        when(input.getFilter()).thenReturn(filters);
        query = new QueryByFilter(input).getQueryBuilderByFilter();
        assertRange(query.getInner(), PROPERTY_TIMESTAMP, "2050-10-14T12:40:00.0Z", "2050-10-14T12:50:00.0Z");

        filters = Arrays.asList(
                new FilterBuilder().setProperty(PROPERTY_TIMESTAMP).setFiltervalue("2050-10-14T12:42*").build());
        when(input.getFilter()).thenReturn(filters);
        query = new QueryByFilter(input).getQueryBuilderByFilter();
        assertRange(query.getInner(), PROPERTY_TIMESTAMP, "2050-10-14T12:42:00.0Z", "2050-10-14T12:43:00.0Z");

        filters = Arrays.asList(
                new FilterBuilder().setProperty(PROPERTY_TIMESTAMP).setFiltervalue("2050-10-14T12:42:*").build());
        when(input.getFilter()).thenReturn(filters);
        query = new QueryByFilter(input).getQueryBuilderByFilter();
        assertRange(query.getInner(), PROPERTY_TIMESTAMP, "2050-10-14T12:42:00.0Z", "2050-10-14T12:43:00.0Z");

        filters = Arrays.asList(
                new FilterBuilder().setProperty(PROPERTY_TIMESTAMP).setFiltervalue("2050-10-14T12:42:5*").build());
        when(input.getFilter()).thenReturn(filters);
        query = new QueryByFilter(input).getQueryBuilderByFilter();
        assertRange(query.getInner(), PROPERTY_TIMESTAMP, "2050-10-14T12:42:50.0Z", "2050-10-14T12:43:00.0Z");

        filters = Arrays.asList(
                new FilterBuilder().setProperty(PROPERTY_TIMESTAMP).setFiltervalue("2050-10-14T12:42:56*").build());
        when(input.getFilter()).thenReturn(filters);
        query = new QueryByFilter(input).getQueryBuilderByFilter();
        assertRange(query.getInner(), PROPERTY_TIMESTAMP, "2050-10-14T12:42:56.0Z", "2050-10-14T12:42:57.0Z");
    }

    @Test
    public void testExtra() {
        final String PROPERTY_TIMESTAMP = "end";
        EntityInput input = mock(EntityInput.class);
        List<Filter> filters;

        filters = Arrays
                .asList(new FilterBuilder().setProperty(PROPERTY_TIMESTAMP).setFiltervalue("2020-02-19T*").build());
        when(input.getFilter()).thenReturn(filters);
        QueryBuilder query = new QueryByFilter(input).getQueryBuilderByFilter();
        assertRange(query.getInner(), PROPERTY_TIMESTAMP, "2020-02-19T00:00:00.0Z", "2020-02-20T00:00:00.0Z");

        filters = Arrays
                .asList(new FilterBuilder().setProperty(PROPERTY_TIMESTAMP).setFiltervalue("2020-02-19*").build());
        when(input.getFilter()).thenReturn(filters);
        query = new QueryByFilter(input).getQueryBuilderByFilter();
        assertRange(query.getInner(), PROPERTY_TIMESTAMP, "2020-02-19T00:00:00.0Z", "2020-02-20T00:00:00.0Z");

        filters = Arrays.asList(new FilterBuilder().setProperty(PROPERTY_TIMESTAMP).setFiltervalue("2020*").build());
        when(input.getFilter()).thenReturn(filters);
        query = new QueryByFilter(input).getQueryBuilderByFilter();
        assertRange(query.getInner(), PROPERTY_TIMESTAMP, "2020-01-01T00:00:00.0Z", "2021-01-01T00:00:00.0Z");

    }


    private void assertRange(JSONObject rangeQuery, String property, String lower, String upper) {
        System.out.println("==test for " + rangeQuery.toString());
        assertTrue(rangeQuery.has("range"));
        assertTrue(rangeQuery.getJSONObject("range").has(property));
        JSONObject o = rangeQuery.getJSONObject("range").getJSONObject(property);
        assertNotNull(o);
        assertTrue(o.has("lt"));
        assertEquals(upper, o.getString("lt"));
        assertTrue(o.has("gte"));
        assertEquals(lower, o.getString("gte"));
        System.out.println("succeeded");
    }
}
