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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilder;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.data.QueryByFilter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.SortOrder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.entity.input.Filter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.entity.input.FilterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.entity.input.Sortorder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.entity.input.SortorderBuilder;

public class TestFilterConversion {

    private static final String PROPERTY = "node-id";
    private static final String PROPERTY2 = "_id";
    private static final String PROPERTY3 = "timestamp";

    @Test
    public void testQuestionMark() {
        List<Filter> filters = Arrays.asList(new FilterBuilder().setProperty(PROPERTY).setFiltervalue("si?ba").build());
        QueryBuilder query = QueryByFilter.fromFilter(filters);
        System.out.println(query.toJSON());
        assertTrue(query.toJSON().contains("{1,1}"));
        assertNotNull(QueryByFilter.getFilter(filters, PROPERTY));
        assertNull(QueryByFilter.getFilter(filters, PROPERTY2));
        filters = Arrays.asList(new FilterBuilder().setProperty(PROPERTY).setFiltervalue("si?ba").build(),
                new FilterBuilder().setProperty(PROPERTY2).setFiltervalue("abc").build());
        query = QueryByFilter.fromFilter(filters);
        System.out.println(query.toJSON());
        assertNotNull(QueryByFilter.getFilter(filters, PROPERTY2));
        filters = Arrays.asList(new FilterBuilder().setProperty(PROPERTY).setFiltervalue("si?ba").build(),
                new FilterBuilder().setProperty(PROPERTY3).setFiltervalue("<2019-06-13T15:00:12.0Z").build());
        query = QueryByFilter.fromFilter(filters);
        List<Sortorder> sortorder =
                Arrays.asList(new SortorderBuilder().setProperty(PROPERTY).setSortorder(SortOrder.Ascending).build());
        QueryByFilter.setSortOrder(query, sortorder);
        assertNotNull(QueryByFilter.getSortOrder(sortorder, PROPERTY));
    }

    @Test
    public void testSortorder() {
        String f =
                "{\"input\":{\"filter\":[],\"sortorder\":[{\"property\":\"source-type\",\"sortorder\":\"ascending\"}],\"pagination\":{\"size\":10,\"page\":1}}}";

        QueryBuilder query = QueryByFilter.setSortOrder(QueryByFilter.fromFilter(null), Arrays
                .asList(new SortorderBuilder().setProperty("source-type").setSortorder(SortOrder.Ascending).build()));
        System.out.println(query.toJSON());
    }
}
