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
 * { "_index": "networkelement-connection-v1", "_type": "networkelement-connection", "_id": "sim2", "_version": 2,
 * "result": "updated", "_shards": { "total": 2, "successful": 1, "failed": 0 }, "_seq_no": 5, "_primary_term": 1 }
 * 
 * @author jack
 *
 */
@Deprecated
public class UpdateResponse extends BaseResponse {

    private String result;

    public UpdateResponse(Response response) {
        super(response);
        JSONObject o = this.getJson(response);

        this.result = o == null ? null : o.getString("result");
    }

    public boolean succeeded() {
        if (this.result == null) {
            return false;
        }
        String s = this.result.toLowerCase();
        return s.equals("updated") || s.equals("created");
    }

    @Override
    public String toString() {
        return "UpdateResponse [result=" + result + "]";
    }

}
