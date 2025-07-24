/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2023 highstreet technologies GmbH Intellectual Property.
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

import java.util.List;
import java.util.stream.Collectors;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data.PropertyList;

public class SqlDBSearchFilter implements SqlDBFilter {
    private final PropertyList propertyList;
    private final String filter;

    public SqlDBSearchFilter(PropertyList propertyList, String filter) {
        this.propertyList = propertyList;
        this.filter = filter;
    }

    @Override
    public String getFilterExpression() {
        List<String> tmp = this.propertyList.stream()
                .map(e -> new RegexSqlDBFilter(e.getName(), this.filter).getFilterExpression())
                .collect(Collectors.toList());
        return String.join(" OR ", tmp);
    }
}
