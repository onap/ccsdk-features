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

import org.json.JSONArray;
import org.json.JSONObject;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilder;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilders;

@Deprecated
public class UpdateByQueryRequest extends BaseRequest {

    private JSONObject params;
    private final String alias;

    public UpdateByQueryRequest(String alias, String dataType) {
        this(alias, dataType, false);
    }

    public UpdateByQueryRequest(String alias, String dataType, boolean refresh) {
        super("POST", String.format("/%s/%s/_update_by_query", alias, dataType), refresh);
        this.alias = alias;
        this.params = null;
    }

    public void source(String esId, JSONObject map) {
        this.source(map, QueryBuilders.matchQuery("_id", esId));
    }

    public void source(JSONObject map, QueryBuilder query) {
        JSONObject outer = new JSONObject();
        outer.put("query", query.getInner());
        JSONObject script = new JSONObject();
        script.put("lang", "painless");
        script.put("inline", this.createInline(map));
        if (this.params != null) {
            script.put("params", this.params);
        }
        outer.put("script", script);
        super.setQuery(outer.toString());
    }

    private String createInline(JSONObject map) {
        String s = "", k, pkey;
        int i = 1;
        Object value;
        for (Object key : map.keySet()) {
            k = String.valueOf(key);
            value = map.get(k);
            if (value instanceof JSONObject || value instanceof JSONArray) {
                pkey = String.format("p%d", i++);
                if (value instanceof JSONObject) {
                    this.withParam(pkey, (JSONObject) value);
                } else {
                    this.withParam(pkey, (JSONArray) value);
                }

                s += String.format("ctx._source['%s']=%s;", key, "params." + pkey);
            } else {
                s += String.format("ctx._source['%s']=%s;", key, escpaped(value));
            }
        }
        return s;
    }

    private UpdateByQueryRequest withParam(String key, JSONArray p) {
        if (this.params == null) {
            this.params = new JSONObject();
        }
        this.params.put(key, p);
        return this;
    }

    private UpdateByQueryRequest withParam(String key, JSONObject p) {
        if (this.params == null) {
            this.params = new JSONObject();
        }
        this.params.put(key, p);
        return this;
    }

    private String escpaped(Object value) {
        String s = "";
        if (value instanceof Boolean || value instanceof Integer || value instanceof Long || value instanceof Float
                || value instanceof Double) {
            s = String.valueOf(value);
        } else {
            s = "\"" + String.valueOf(value) + "\"";
        }
        return s;

    }

    protected String getAlias() {
        return this.alias;
    }

}
