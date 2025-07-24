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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.query;

import java.util.List;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.query.filters.DBKeyValuePair;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Entity;
import org.opendaylight.yangtools.binding.DataContainer;

public class UpsertQuery<T extends DataContainer> extends InsertQuery<T> {

    public UpsertQuery(Entity e, T object, String controllerId) {
        super(e, object, controllerId);

    }
    public UpsertQuery(Entity e, T object, String controllerId, boolean ignoreControllerId, boolean ignoreIdField) {
        super(e, object, controllerId, ignoreControllerId, ignoreIdField);

    }

    @Override
    protected void appendAdditionalToQuery(StringBuilder sb, List<DBKeyValuePair<String>> keyValues) {
        sb.append(" ON DUPLICATE KEY UPDATE ");
        boolean comma = false;
        for (DBKeyValuePair<String> kvp : keyValues) {
            if(kvp.getKey().equals("`id`")) {
                continue;
            }
            //do not update is-required if entry already exists
            if (this.entity == Entity.NetworkelementConnection && kvp.getKey().equals("`is-required`")
                    && (kvp.getValue().equals("false") || kvp.getValue().equals("0"))) {
                continue;
            }
            if (comma) {
                sb.append(",");
            }
            sb.append(String.format("%s=%s", kvp.getKey(), kvp.getValue()));
            comma = true;
        }
    }
}
