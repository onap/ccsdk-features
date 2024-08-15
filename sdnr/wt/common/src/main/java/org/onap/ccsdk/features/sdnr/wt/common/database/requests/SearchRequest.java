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
package org.onap.ccsdk.features.sdnr.wt.common.database.requests;

import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilder;

@Deprecated
public class SearchRequest extends BaseRequest {

    private final String alias;

    public SearchRequest(String uri) {
        super("POST", uri);
        this.alias = null;
    }

    public SearchRequest(String alias, String dataType) {
        super("POST", String.format("/%s/%s/_search", alias, dataType));
        this.alias = alias;
    }

    @Override
    public void setQuery(QueryBuilder query) {
        super.setQuery(query);
    }

    public String getAlias() {
        return this.alias;
    }



}
