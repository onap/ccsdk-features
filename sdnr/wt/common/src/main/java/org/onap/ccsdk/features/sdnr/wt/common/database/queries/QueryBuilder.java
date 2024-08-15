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
package org.onap.ccsdk.features.sdnr.wt.common.database.queries;

import org.json.JSONArray;
import org.json.JSONObject;

@Deprecated
public class QueryBuilder {

    private JSONObject innerQuery;
    private final JSONObject outerQuery;
    private final JSONObject queryObj;

    public QueryBuilder() {
        this.outerQuery = new JSONObject();
        this.queryObj = new JSONObject();
        this.outerQuery.put("query", this.queryObj);

    }

    public QueryBuilder from(long from) {
        this.outerQuery.put("from", from);
        return this;
    }

    public QueryBuilder size(long size) {
        this.outerQuery.put("size", size);
        return this;
    }

    public QueryBuilder sort(String prop, SortOrder order) {
        JSONArray a;
        if (this.outerQuery.has("sort")) {
            a = this.outerQuery.getJSONArray("sort");
        } else {
            a = new JSONArray();
        }
        JSONObject sortObj = new JSONObject();
        JSONObject orderObj = new JSONObject();
        orderObj.put("order", order.getValue());
        sortObj.put(prop, orderObj);
        a.put(sortObj);
        this.outerQuery.put("sort", a);
        return this;
    }

    public QueryBuilder aggregations(String key, SortOrder sortOrder) {
        JSONObject keyquery = new JSONObject();
        JSONObject terms = new JSONObject();
        JSONObject field = new JSONObject();
        field.put("field", key);
        terms.put("terms", field);
        if (sortOrder != null) {
            JSONObject so = new JSONObject();
            so.put("_key", sortOrder.getValue());
            terms.put("order", so);
        }
        keyquery.put(key, terms);
        this.outerQuery.put("aggs", keyquery);
        return this;
    }

    protected QueryBuilder setQuery(String key, JSONObject query) {
        this.innerQuery = query;
        this.queryObj.put(key, this.innerQuery);
        return this;
    }

    public JSONObject getInner() {
        return this.queryObj;
    }

    public boolean contains(String match) {
        return this.toJSON().contains(match);
    }

    public String toJSON() {
        return this.outerQuery.toString();
    }

    public QueryBuilder aggregations(String key) {
        return this.aggregations(key, null);
    }

    public void doFullsizeRequest() {
        this.setFullsizeRequest(true);
    }

    public QueryBuilder setFullsizeRequest(boolean doFullsizeRequest) {
        if (doFullsizeRequest) {
            this.outerQuery.put("track_total_hits", doFullsizeRequest);
        } else {
            this.outerQuery.remove("track_total_hits");
        }
        return this;
    }
}
