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

import java.util.List;
import org.json.JSONObject;

@Deprecated
public class QueryBuilders {

    public static QueryBuilder matchAllQuery() {
        return new QueryBuilder().setQuery("match_all", new JSONObject());
    }

    public static QueryBuilder termQuery(String key, String value) {
        JSONObject o = new JSONObject();
        o.put(key, value);
        return new QueryBuilder().setQuery("term", o);
    }
    public static QueryBuilder multiTermQuery(String key, List<String> value) {
        JSONObject o = new JSONObject();
        o.put(key, value);
        return new QueryBuilder().setQuery("terms", o);
    }

    public static QueryBuilder matchQuery(String key, Object value) {
        JSONObject o = new JSONObject();
        o.put(key, value);
        return new QueryBuilder().setQuery("match", o);
    }

    public static QueryBuilder matchQuery(String key, List<?> values) {
        BoolQueryBuilder query = boolQuery();
        for (Object value : values) {
            query.should(matchQuery(key, value));
        }
        return query;
    }

    public static BoolQueryBuilder boolQuery() {
        return new BoolQueryBuilder();
    }

    public static RangeQueryBuilder rangeQuery(String key) {
        return new RangeQueryBuilder(key);
    }

    public static RegexQueryBuilder regex(String propertyName, String re) {
        return new RegexQueryBuilder().add(propertyName, re);
    }

    /**
     * @param object
     * @return
     */
    public static QueryBuilder searchAllFieldsQuery(String filter) {
        JSONObject inner = new JSONObject();
        inner.put("default_field", "*");
        inner.put("query", filter == null ? "" : filter);
        return new QueryBuilder().setQuery("query_string", inner);
    }
}
