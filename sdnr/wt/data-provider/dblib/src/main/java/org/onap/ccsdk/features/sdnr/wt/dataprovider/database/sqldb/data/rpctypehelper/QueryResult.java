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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data.rpctypehelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.PaginationOutputG;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.connectionlog.list.output.PaginationBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;

public class QueryResult<T> {

    private List<T> result;
    private PaginationOutputG pagination;

    public QueryResult( List<T> result,long page, long pageSize,long total) {
        this.result = result;

        PaginationBuilder x = new PaginationBuilder();
        x.setPage(Uint64.valueOf(page));
        x.setSize(Uint32.valueOf(pageSize));
        x.setTotal(Uint64.valueOf(total));
        pagination = x.build();
    }

    public Set<T> getResultSet() {
        return new HashSet<>(this.result);
    }
    public List<T> getResult() {
        return this.result;
    }

    public PaginationOutputG getPagination() {
        return pagination;
    }

    @Override
    public String toString() {
        return "QueryResult [result=" + result + ", pagination=" + pagination + "]";
    }

    public static <X> QueryResult<X> createEmpty() {
        return new QueryResult<X>(new ArrayList<>(), 1, 0, 0);
    }



}
