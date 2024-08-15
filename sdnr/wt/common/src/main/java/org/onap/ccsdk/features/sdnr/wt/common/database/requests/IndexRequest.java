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

import javax.annotation.Nullable;

@Deprecated
public class IndexRequest extends BaseRequest {

    private final String alias;
    private final String esId;

    public IndexRequest(String alias, String dataType) {
        this(alias, dataType, null);
    }

    public IndexRequest(String alias, String dataType, @Nullable String esId) {
        super("POST", esId != null ? String.format("/%s/%s/%s", alias, dataType, BaseRequest.urlEncodeValue(esId))
                : String.format("/%s/%s", alias, dataType));
        this.alias = alias;
        this.esId = esId;
    }

    public IndexRequest(String alias, String dataType, @Nullable String esId, boolean refresh) {
        super("POST", esId != null ? String.format("/%s/%s/%s", alias, dataType, BaseRequest.urlEncodeValue(esId))
                : String.format("/%s/%s", alias, dataType), refresh);
        this.alias = alias;
        this.esId = esId;
    }

    public void source(String content) {
        super.setQuery(content);
    }

    protected String getAlias() {
        return this.alias;
    }

    protected String getEsId() {
        return this.esId;
    }

}
