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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.elalto;

import org.json.JSONObject;
import org.onap.ccsdk.features.sdnr.wt.common.database.SearchHit;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.BaseSearchHitConverter;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.ComponentName;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.frankfurt.data.ConnectionLogStatus;

/**
 * 
 * @author Michael Dürre
 *
 *         { "mountId":"nts-manager-dev-micha", "host":"10.20.5.2", "port":8300, "username":"netconf",
 *         "password":"netconf" }
 *
 *         =>
 * 
 *         { "node-id": "sim1", "is-required": true, "password": "ads", "port": 12600, "host": "10.20.5.2", "id":
 *         "sim1", "username": "ad", "status": "Connected" }
 * 
 */
public class FrankfurtRequiredNetworkElementConverter extends BaseSearchHitConverter {

    public FrankfurtRequiredNetworkElementConverter() {
        super(ComponentName.REQUIRED_NETWORKELEMENT);
    }

    @Override
    public SearchHit convert(SearchHit source) {

        JSONObject data = new JSONObject();
        JSONObject src = source.getSource();
        data.put("id", src.getString("mountId"));
        data.put("node-id", src.getString("mountId"));
        data.put("username", src.getString("username"));
        data.put("password", src.getString("password"));
        data.put("host", src.getString("host"));
        data.put("port", src.getInt("port"));
        data.put("status", ConnectionLogStatus.Undefined.getName());
        data.put("is-required", true);
        return this.getSearchHit(source.getIndex(), source.getType(), source.getId(), data);
    }

}
