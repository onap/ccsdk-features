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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.data.rpctypehelper;

import java.math.BigInteger;

import org.onap.ccsdk.features.sdnr.wt.common.database.SearchResult;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.PaginationOutputG;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.connectionlog.list.output.PaginationBuilder;

public class QueryResult<T> {

    private SearchResult<T> result;
    private PaginationOutputG pagination;

    public QueryResult(long page, long pageSize, SearchResult<T> result) {
        this.result = result;

        PaginationBuilder x = new PaginationBuilder();
        x.setPage(BigInteger.valueOf(page));
        x.setSize(pageSize);
        x.setTotal(BigInteger.valueOf(result.getTotal()));
        pagination = x.build();
    }

    public QueryResult(QueryByFilter queryByFilter, SearchResult<T> result) {
        this.result = result;

        PaginationBuilder x = new PaginationBuilder();
        x.setPage(BigInteger.valueOf(queryByFilter.getPage()));
        x.setSize(queryByFilter.getPageSize());
        x.setTotal(BigInteger.valueOf(result.getTotal()));
        pagination = x.build();
    }


    public SearchResult<T> getResult() {
        return result;
    }

    public PaginationOutputG getPagination() {
        return pagination;
    }



}
