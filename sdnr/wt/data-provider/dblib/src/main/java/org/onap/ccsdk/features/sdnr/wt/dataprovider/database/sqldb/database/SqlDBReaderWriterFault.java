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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.database;

import java.util.List;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.SqlDBClient;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Entity;
import org.opendaylight.yangtools.binding.DataContainer;

public class SqlDBReaderWriterFault<T extends DataContainer> extends SqlDBReaderWriter<T> {

    private static final String NODE_KEY = "node-id";

    public SqlDBReaderWriterFault(SqlDBClient dbService, Entity e, String dbSuffix, Class<T> clazz,
            String controllerId) {
        super(dbService, e, dbSuffix, clazz, controllerId);
    }

    public List<String> getAllNodes() {

        return this.readAll(NODE_KEY);
    }

}
