/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.query.filters;

import org.eclipse.jdt.annotation.Nullable;

public class RangeSqlDBFilter extends DBKeyValuePair<Object> implements SqlDBFilter {

    private final String comparator;

    public RangeSqlDBFilter(String property, Object value, String comparator) {
        super(property, value);
        this.comparator = comparator;
    }

    @Override
    public String getFilterExpression() {
        if (isNumericValue(this.getValue())) {
            return String.format("`%s`%s%d", this.getKey(), this.comparator, this.getValue());
        } else {
            return String.format("`%s`%s'%s'", this.getKey(), this.comparator, this.getValue());
        }
    }

    public static @Nullable String between(String property, String lowerEnd, boolean incLowerEnd, String upperEnd,
            boolean incUpperEnd) {
        return String.format("(%s AND %s)",
                new RangeSqlDBFilter(property, lowerEnd, incLowerEnd ? ">=" : ">").getFilterExpression(),
                new RangeSqlDBFilter(property, upperEnd, incUpperEnd ? "<=" : "<").getFilterExpression());
    }

}
