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

/**
 * convert maintenance object from el alto version to frankfurt
 * 
 * @author jack
 *
 *         { "node": "ddd", "filter": [ { "definition": { "object-id-ref": "", "problem": "" }, "description": "",
 *         "start": "2019-11-26T15:37+00:00", "end": "2019-11-26T23:37+00:00" }], "active": false } => { "id": "sim1"
 *         "node-id": "sim1", "description": "", "start": "2020-01-28T12:00:17.6Z", "end": "2020-01-28T12:00:17.6Z",
 *         "active": false, "object-id-ref": "", "problem": ""
 *
 */
public class FrankfurtMaintenanceConverter extends BaseSearchHitConverter {

    public FrankfurtMaintenanceConverter() {
        super(ComponentName.MAINTENANCE);
    }

    @Override
    public SearchHit convert(SearchHit source) {

        JSONObject src = source.getSource();
        JSONObject data = new JSONObject();
        data.put("id", src.getString("node"));
        data.put("node-id", src.getString("node"));
        data.put("active", src.getBoolean("active"));
        JSONObject filter = null;
        if (src.has("filter")) {
            filter = src.getJSONArray("filter").length() > 0 ? src.getJSONArray("filter").getJSONObject(0) : null;
        }
        data.put("start", filter != null ? filter.getString("start") : "");
        data.put("end", filter != null ? filter.getString("end") : "");
        data.put("description", filter != null ? filter.getString("description") : "");
        JSONObject definition =
                filter != null ? filter.has("definition") ? filter.getJSONObject("definition") : null : null;
        data.put("problem", definition != null ? definition.getString("problem") : "");
        data.put("object-id-ref", definition != null ? definition.getString("object-id-ref") : "");
        return this.getSearchHit(source.getIndex(), source.getType(), source.getId(), data);
    }
}
