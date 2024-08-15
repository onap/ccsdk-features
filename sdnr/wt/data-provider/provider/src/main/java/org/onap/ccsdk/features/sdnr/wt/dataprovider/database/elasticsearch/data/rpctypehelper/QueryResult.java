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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.database.elasticsearch.data.rpctypehelper;

import java.math.BigInteger;
import org.onap.ccsdk.features.sdnr.wt.common.database.SearchResult;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.YangHelper2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.PaginationOutputG;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.connectionlog.list.output.PaginationBuilder;

@Deprecated
public class QueryResult<T> {

    private SearchResult<T> result;
    private PaginationOutputG pagination;

    public QueryResult(long page, long pageSize, SearchResult<T> result) {
        this.result = result;

        PaginationBuilder x = new PaginationBuilder();
        x.setPage(YangHelper2.getBigIntegerOrUint64(BigInteger.valueOf(page)));
        x.setSize(YangHelper2.getLongOrUint32(pageSize));
        x.setTotal(YangHelper2.getBigIntegerOrUint64(BigInteger.valueOf(result.getTotal())));
        pagination = x.build();
    }

    public QueryResult(QueryByFilter queryByFilter, SearchResult<T> result) {
        this.result = result;

        PaginationBuilder x = new PaginationBuilder();
        x.setPage(YangHelper2.getBigIntegerOrUint64(BigInteger.valueOf(queryByFilter.getPage())));
        x.setSize(YangHelper2.getLongOrUint32(queryByFilter.getPageSize()));
        x.setTotal(YangHelper2.getBigIntegerOrUint64(BigInteger.valueOf(result.getTotal())));
        pagination = x.build();
    }


    public SearchResult<T> getResult() {
        return result;
    }

    public PaginationOutputG getPagination() {
        return pagination;
    }

    @Override
    public String toString() {
        return "QueryResult [result=" + result + ", pagination=" + pagination + "]";
    }



}
