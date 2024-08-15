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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.database.elasticsearch.impl;

import org.onap.ccsdk.features.sdnr.wt.common.database.HtDatabaseClient;
import org.onap.ccsdk.features.sdnr.wt.common.database.SearchHit;
import org.onap.ccsdk.features.sdnr.wt.common.database.SearchResult;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilders;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data.HtUserdataManagerBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Entity;

@Deprecated
public class HtUserdataManagerImpl extends HtUserdataManagerBase {

    private final HtDatabaseClient dbClient;

    public HtUserdataManagerImpl(HtDatabaseClient rawClient) {
        this.dbClient = rawClient;
    }

    @Override
    protected String readUserdata(String username, String defaultValue) {

        SearchResult<SearchHit> result = this.dbClient.doReadByQueryJsonData(Entity.Userdata.getName(),
                QueryBuilders.matchQuery("_id", username));
        return result.getHits().size() > 0 ? result.getHits().get(0).getSourceAsString() : defaultValue;
    }

    @Override
    public boolean setUserdata(String username, String data) {
        return this.dbClient.doWriteRaw(Entity.Userdata.getName(), username, data) != null;
    }

    @Override
    public boolean removeUserdata(String username) {
        return this.dbClient.doRemove(Entity.Userdata.getName(), username);
    }

}
