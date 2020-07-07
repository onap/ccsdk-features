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
package org.onap.ccsdk.features.sdnr.wt.common.database.data;

import org.onap.ccsdk.features.sdnr.wt.common.database.queries.RangeQueryBuilder;

public class DbFilter {

    public static String createDatabaseRegex(String restFilterValue) {
        return restFilterValue == null ? null : restFilterValue.replace("?", ".{1,1}").replace("*", ".*");
    }

    public static boolean hasSearchParams(String restFilterValue) {
        return restFilterValue == null ? false : restFilterValue.contains("*") || restFilterValue.contains("?");
    }

    public static boolean isComparisonValid(String restFilterValue) {
        return restFilterValue == null ? false : restFilterValue.contains(">") || restFilterValue.contains("<");
    }

    public static RangeQueryBuilder getRangeQuery(String key, String restFilterValue) {
        RangeQueryBuilder query = new RangeQueryBuilder(key);
        restFilterValue = restFilterValue.trim();
        if (restFilterValue.startsWith(">=")) {
            query.gte(getObjectFromString(restFilterValue.substring(2).trim()));
        } else if (restFilterValue.startsWith(">")) {
            query.gt(getObjectFromString(restFilterValue.substring(1).trim()));
        } else if (restFilterValue.startsWith("<=")) {
            query.lte(getObjectFromString(restFilterValue.substring(2).trim()));
        } else if (restFilterValue.startsWith("<")) {
            query.lt(getObjectFromString(restFilterValue.substring(1).trim()));
        } else {
            return null;
        }

        return query;
    }

    private static Object getObjectFromString(String str) {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException | NullPointerException nfe) {
            return str;
        }

    }
}
