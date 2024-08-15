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
public class BoolQueryBuilder extends QueryBuilder {

    private JSONObject inner;

    public BoolQueryBuilder() {
        super();
        this.inner = new JSONObject();
        this.setQuery("bool", this.inner);
    }

    @Override
    public String toString() {
        return "BoolQueryBuilder [inner=" + inner + "]";
    }

    public static boolean isEmpty(JSONObject o) {
        return o.keySet().isEmpty();
    }

    public BoolQueryBuilder must(QueryBuilder query) {

        if (!isEmpty(this.inner)) {
            Object x = this.inner.has("must") ? this.inner.get("must") : this.inner;
            if (x instanceof JSONArray) {
                ((JSONArray) x).put(query.getInner());
            } else {
                this.inner = new JSONObject();
                this.inner.put("must", new JSONObject());
                JSONArray a = new JSONArray();
                a.put(x);
                a.put(query.getInner());
                this.inner.put("must", a);
            }
        } else {
            this.inner.put("must", query.getInner());
        }
        this.setQuery("bool", this.inner);
        return this;
    }

    /**
     * @param query
     */
    public BoolQueryBuilder should(QueryBuilder query) {
        if (!isEmpty(this.inner)) {
            Object x = this.inner.has("should") ? this.inner.get("should") : this.inner;
            if (x instanceof JSONArray) {
                ((JSONArray) x).put(query.getInner());
            } else {
                this.inner = new JSONObject();
                //				this.inner.put("should", new JSONObject());
                JSONArray a = new JSONArray();
                if (!x.toString().equals("{}")) {
                    a.put(x);
                }
                a.put(query.getInner());
                this.inner.put("should", a);
            }
        } else {
            this.inner.put("should", query.getInner());
        }
        this.setQuery("bool", this.inner);
        return this;
    }

}
