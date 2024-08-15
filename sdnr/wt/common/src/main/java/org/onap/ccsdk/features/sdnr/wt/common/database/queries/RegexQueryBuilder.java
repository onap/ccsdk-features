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
public class RegexQueryBuilder extends QueryBuilder {

    private JSONObject inner;

    public RegexQueryBuilder() {
        super();
        this.inner = new JSONObject();
        this.setQuery("regexp", this.inner);
    }

    public RegexQueryBuilder add(String propertyName, String filter) {
        JSONObject regexFilter = new JSONObject();
        regexFilter.put("value", filter);
        regexFilter.put("flags", "ALL");
        regexFilter.put("max_determinized_states", 10000);
        this.inner.put(propertyName, regexFilter);
        return this;
    }

    @Override
    public String toString() {
        return "RegexQueryBuilder [inner=" + inner + "]";
    }
}
