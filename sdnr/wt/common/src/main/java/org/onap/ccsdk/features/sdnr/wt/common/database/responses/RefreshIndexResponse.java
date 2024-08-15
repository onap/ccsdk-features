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
 * { "_shards": { "total": 10, "successful": 5, "failed": 0 } }
 * 
 * @author jack
 *
 */
@Deprecated
public class RefreshIndexResponse extends BaseResponse {

    private boolean succeeded;

    public RefreshIndexResponse(Response response) {
        super(response);
        JSONObject o = this.getJson(response);
        this.succeeded = o.getJSONObject("_shards").getInt("failed") == 0;
        // {"_index":"historicalperformance24h","_type":"historicalperformance24h","_id":"CbZxvWwB4xjGPydc9ida","_version":1,"result":"created","_shards":{"total":4,"successful":1,"failed":0},"_seq_no":1,"_primary_term":1}
    }

    public boolean succeeded() {
        return this.succeeded;
    }

}
