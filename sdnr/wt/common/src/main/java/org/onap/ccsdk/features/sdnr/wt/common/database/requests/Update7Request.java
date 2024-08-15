/*
 * ============LICENSE_START=======================================================
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
package org.onap.ccsdk.features.sdnr.wt.common.database.requests;

/**
 * @author Michael Dürre
 *
 *         https://github.com/elastic/elasticsearch/blob/7.1/rest-api-spec/src/main/resources/rest-api-spec/api/update.json
 */
@Deprecated
public class Update7Request extends UpdateRequest {

    public Update7Request(String alias, String esId, int retries, boolean refresh) {
        super(String.format("/%s/_update/%s", alias, BaseRequest.urlEncodeValue(esId), retries), refresh);
    }

    public Update7Request(UpdateRequest request) {
        this(request.getAlias(), request.getEsId(), request.getRetries(), request.doRefresh());
        this.setQuery(request.getQuery());
    }

}
