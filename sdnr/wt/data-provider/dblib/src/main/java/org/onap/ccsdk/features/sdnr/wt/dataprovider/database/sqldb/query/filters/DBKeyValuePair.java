/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2021 highstreet technologies GmbH Intellectual Property.
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

import java.math.BigInteger;

public class DBKeyValuePair<T> implements SqlDBFilter {

    private final String key;
    private final T value;

    public DBKeyValuePair(String key, T value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "DBKeyValuePair [key=" + key + ", value=" + value + "]";
    }

    protected boolean isNumericValue(T v) {
        return ((v instanceof Long) || (v instanceof Integer) || (v instanceof Byte) || (v instanceof BigInteger));
    }

    @Override
    public String getFilterExpression() {
        if (isNumericValue(this.value)) {
            return String.format("`%s`=%d", this.key, this.value);
        } else {
            return String.format("`%s`='%s'", this.key, this.value);
        }
    }
}
