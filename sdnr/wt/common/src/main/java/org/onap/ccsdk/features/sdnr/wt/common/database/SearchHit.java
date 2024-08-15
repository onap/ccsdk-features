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
package org.onap.ccsdk.features.sdnr.wt.common.database;

import org.json.JSONObject;

@Deprecated
public class SearchHit {

    private final String index;
    private final String type;
    private final String id;
    private final JSONObject source;
    private final JSONObject raw;

    public SearchHit(JSONObject o) {
        this.raw = o;
        this.index = o.getString("_index");
        this.type = o.getString("_type");
        this.id = o.getString("_id");
        this.source = o.getJSONObject("_source");
    }

    public String getIndex() {
        return this.index;
    }

    public String getType() {
        return this.type;
    }

    public String getId() {
        return this.id;
    }

    public JSONObject getSource() {
        return this.source;
    }

    public String getSourceAsString() {
        return this.source.toString();
    }

    public JSONObject getRaw() {
        return this.raw;
    }

}
