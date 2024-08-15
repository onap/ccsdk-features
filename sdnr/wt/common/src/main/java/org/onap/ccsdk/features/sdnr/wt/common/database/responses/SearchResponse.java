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
import org.json.JSONArray;
import org.json.JSONObject;
import org.onap.ccsdk.features.sdnr.wt.common.database.SearchHit;

@Deprecated
public class SearchResponse extends BaseResponse {

    private long total;
    private SearchHit[] searchHits;
    private JSONObject aggregations;

    public SearchResponse(Response response) {
        super(response);
        this.handleResult(this.getJson(response));
    }

    public SearchResponse(String json) {
        super(null);
        this.handleResult(this.getJson(json));
    }

    private void handleResult(JSONObject result) {
        if (result != null && this.isResponseSucceeded()) {
            JSONObject hitsouter = result.getJSONObject("hits");
            this.total = this.getTotalFromHits(hitsouter);
            JSONArray a = hitsouter.getJSONArray("hits");
            SearchHit[] hits = new SearchHit[a.length()];
            for (int i = 0; i < a.length(); i++) {
                hits[i] = new SearchHit(a.getJSONObject(i));
            }
            this.searchHits = hits;
            if (result.has("aggregations")) {
                this.aggregations = result.getJSONObject("aggregations");
            } else {
                this.aggregations = null;
            }
        } else {
            this.searchHits = new SearchHit[0];
        }
    }

    public SearchHit[] getHits() {
        return this.searchHits;
    }

    public long getTotal() {
        return this.total;
    }

    public boolean hasAggregations() {
        return this.aggregations != null;
    }

    public AggregationEntries getAggregations(String property) {
        AggregationEntries entries = new AggregationEntries();
        if (this.aggregations != null && this.aggregations.has(property)) {
            JSONArray a = this.aggregations.getJSONObject(property).getJSONArray("buckets");
            for (int i = 0; i < a.length(); i++) {
                entries.put(a.getJSONObject(i).getString("key"), a.getJSONObject(i).getLong("doc_count"));
            }
        }
        return entries;
    }

    /**
     * @param hits
     * @return
     */
    private long getTotalFromHits(JSONObject hits) {
        Object o = hits.get("total");
        if (o instanceof Long || o instanceof Integer) {
            return hits.getLong("total");
        } else if (o instanceof JSONObject) {
            return hits.getJSONObject("total").getLong("value");
        }
        return 0;
    }
}
