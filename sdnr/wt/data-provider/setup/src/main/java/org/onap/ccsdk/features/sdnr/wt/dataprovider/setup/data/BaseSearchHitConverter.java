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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data;

import org.json.JSONObject;
import org.onap.ccsdk.features.sdnr.wt.common.database.SearchHit;

public abstract class BaseSearchHitConverter implements SearchHitConverter {

    private ComponentName name;

    public BaseSearchHitConverter(ComponentName name) {
        this.name = name;
    }

    protected SearchHit getSearchHit(String index, String type, String id, JSONObject data) {
        JSONObject o = new JSONObject();
        o.put("_index", index);
        o.put("_type", type);
        o.put("_id", id);
        o.put("_source", data);
        return new SearchHit(o);
    }

    @Override
    public ComponentData convert(DataContainer container) {
        ComponentData srcData = container.getComponents().get(this.name);
        ComponentData dstData = new ComponentData(srcData.getName());
        for (SearchHit sh : srcData) {
            dstData.add(this.convert(sh));
        }
        return dstData;
    }
}
