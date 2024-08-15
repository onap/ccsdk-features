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
package org.onap.ccsdk.features.sdnr.wt.common.database.responses;

import org.elasticsearch.client.Response;
import org.json.JSONObject;

/**
 * { "took":47, "timed_out":false, "total":1, "updated":1, "deleted":0, "batches":1, "version_conflicts":0, "noops":0,
 * "retries":{ "bulk":0, "search":0 }, "throttled_millis":0, "requests_per_second":-1.0, "throttled_until_millis":0,
 * "failures":[]}
 * 
 * @author jack
 *
 */
@Deprecated
public class UpdateByQueryResponse extends BaseResponse {


    private int isUpdated;
    private int failures;

    public UpdateByQueryResponse(Response response) {
        super(response);
        JSONObject o = this.getJson(response);

        this.isUpdated = o.getInt("updated");
        this.failures = o.getJSONArray("failures").length();
        //{"_index":"historicalperformance24h","_type":"historicalperformance24h","_id":"CbZxvWwB4xjGPydc9ida","_version":1,"result":"created","_shards":{"total":4,"successful":1,"failed":0},"_seq_no":1,"_primary_term":1}
    }


    public boolean isUpdated() {
        return this.isUpdated > 0;
    }

    public boolean hasFailures() {
        return this.failures > 0;
    }

    @Override
    public String toString() {
        return "UpdateByQueryResponse [isUpdated=" + isUpdated + ", failures=" + failures + "]";
    }
}
