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

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class UpdateRequest extends BaseRequest {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateRequest.class);
    private JSONObject params;
    private String alias;
    private String esId;
    private int retries;

    public UpdateRequest(String alias, String dataType, String esId) {
        this(alias, dataType, esId, BaseRequest.DEFAULT_RETRIES);
    }

    public UpdateRequest(String alias, String dataType, String esId, boolean refresh) {
        this(alias, dataType, esId, BaseRequest.DEFAULT_RETRIES, refresh);
    }

    public UpdateRequest(String alias, String dataType, String esId, int retries) {
        this(alias, dataType, esId, retries, false);
    }

    public UpdateRequest(String alias, String dataType, String esId, int retries, boolean refresh) {
        this(String.format("/%s/%s/%s/_update", alias, dataType, BaseRequest.urlEncodeValue(esId)), refresh);
        this.alias = alias;
        this.esId = esId;
        this.retries = retries;
    }

    public UpdateRequest(String uri, boolean refresh) {
        super("POST", uri, refresh, BaseRequest.DEFAULT_RETRIES);
        this.params = null;
        this.retries = 1;

    }



    private UpdateRequest withParam(String key, JSONObject p) {
        if (this.params == null) {
            this.params = new JSONObject();
        }
        this.params.put(key, p);
        return this;
    }

    private UpdateRequest withParam(String key, JSONArray p) {
        if (this.params == null) {
            this.params = new JSONObject();
        }
        this.params.put(key, p);
        return this;
    }

    public void source(JSONObject map) {
        this.source(map, null);
    }

    public void source(JSONObject map, List<String> onlyForInsert) {
        JSONObject outer = new JSONObject();
        JSONObject script = new JSONObject();
        script.put("lang", "painless");
        script.put("source", this.createInline(map, onlyForInsert));
        if (this.params != null) {
            script.put("params", this.params);
        }
        outer.put("script", script);
        outer.put("upsert", map);
        LOG.debug("update payload: " + outer.toString());
        super.setQuery(outer.toString());
    }

    private String createInline(JSONObject map, List<String> onlyForInsert) {
        if (onlyForInsert == null) {
            onlyForInsert = new ArrayList<String>();
        }
        String s = "", k = "";
        Object value;
        String pkey;
        int i = 0;
        for (Object key : map.keySet()) {
            k = String.valueOf(key);
            if (onlyForInsert.contains(k)) {
                continue;
            }
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

    protected String getEsId() {
        return this.esId;
    }

    protected int getRetries() {
        return this.retries;
    }
}
