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

import org.json.JSONObject;

@Deprecated
public class RangeQueryBuilder extends QueryBuilder {

    private Object gtValue = null;
    private Object gteValue = null;
    private Float boost = 2.0f;
    private Object ltValue = null;
    private Object lteValue = null;
    private String key;

    public RangeQueryBuilder(String key) {
        super();
        this.key = key;

    }

    private void setQuery() {
        JSONObject r = new JSONObject();
        JSONObject k = new JSONObject();
        if (this.gteValue != null) {
            k.put("gte", this.gteValue);
        } else if (this.gtValue != null) {
            k.put("gt", this.gtValue);
        }
        if (this.lteValue != null) {
            k.put("lte", this.lteValue);
        } else if (this.ltValue != null) {
            k.put("lt", this.ltValue);
        }
        if (this.boost != null) {
            k.put("boost", this.boost);
        }
        r.put(this.key, k);

        this.setQuery("range", r);
    }


    public RangeQueryBuilder lte(Object compare) {
        this.lteValue = compare;
        this.ltValue = null;
        this.setQuery();
        return this;
    }

    public RangeQueryBuilder lt(Object compare) {
        this.lteValue = null;
        this.ltValue = compare;
        this.setQuery();
        return this;
    }

    public RangeQueryBuilder gte(Object compare) {
        this.gteValue = compare;
        this.gtValue = null;
        this.setQuery();
        return this;
    }

    public RangeQueryBuilder gt(Object compare) {
        this.gteValue = null;
        this.gtValue = compare;
        this.setQuery();
        return this;
    }

}
