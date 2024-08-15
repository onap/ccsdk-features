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

//https://www.elastic.co/guide/en/elasticsearch/reference/6.8/docs-index_.html
//https://github.com/elastic/elasticsearch/blob/6.8/rest-api-spec/src/main/resources/rest-api-spec/api/cluster.put_settings.json
@Deprecated
public class ClusterSettingsRequest extends BaseRequest {

    private static final boolean DEFAULT_ALLOW_AUTOCREATEINDEX = true;
    private final JSONObject persistent;
    private final JSONObject data;

    public ClusterSettingsRequest() {
        this(DEFAULT_ALLOW_AUTOCREATEINDEX);
    }

    public ClusterSettingsRequest(boolean autoCreateIndex) {
        super("PUT", "/_cluster/settings");
        this.data = new JSONObject();
        this.persistent = new JSONObject();
        this.data.put("persistent", this.persistent);
        this.allowAutoCreateIndex(autoCreateIndex);

    }

    public ClusterSettingsRequest allowAutoCreateIndex(boolean allow) {
        this.persistent.put("action.auto_create_index", String.valueOf(allow));
        this.setQuery(this.data);
        return this;
    }

    public ClusterSettingsRequest maxCompilationsPerMinute(long executions) {
        //this.persistent.put("script.max_compilations_per_minute" ,executions);
        this.persistent.put("script.max_compilations_rate", String.format("%d/1m", executions));
        this.setQuery(this.data);
        return this;
    }
}
