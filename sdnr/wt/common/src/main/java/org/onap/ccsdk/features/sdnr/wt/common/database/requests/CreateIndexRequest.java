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

import org.json.JSONObject;

//https://github.com/elastic/elasticsearch/blob/6.4/rest-api-spec/src/main/resources/rest-api-spec/api/indices.create.json
//https://github.com/elastic/elasticsearch/blob/6.4/rest-api-spec/src/main/resources/rest-api-spec/api/indices.put_mapping.json
@Deprecated
public class CreateIndexRequest extends BaseRequest {

    private JSONObject settings;
    private JSONObject mappings;

    public CreateIndexRequest(String index) {
        super("PUT", "/" + index);
        this.mappings = new JSONObject();
    }

    private void setRequest() {

        JSONObject o = new JSONObject();
        if (this.mappings != null) {
            o.put("mappings", this.mappings);
        }
        if (this.settings != null) {
            o.put("settings", this.settings);
        }
        super.setQuery(o);
    }

    @SuppressWarnings("hiding")
    public CreateIndexRequest mappings(JSONObject mappings) {
        this.mappings = mappings;
        this.setRequest();
        return this;
    }

    public CreateIndexRequest settings(JSONObject settings) {
        this.settings = settings;
        this.setRequest();
        return this;
    }

    public boolean hasMappings() {
        return this.mappings != null;
    }

    public boolean hasSettings() {
        return this.settings != null;
    }

}
