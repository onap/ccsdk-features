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

import java.util.ArrayList;
import org.json.JSONArray;
import org.onap.ccsdk.features.sdnr.wt.common.database.SearchHit;

public class ComponentData extends ArrayList<SearchHit> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final ComponentName name;

    public ComponentData(ComponentName name) {
        this(name, null);
    }

    public ComponentData(ComponentName name, JSONArray a) {
        this.name = name;
        if (a != null) {
            for (int i = 0; i < a.length(); i++) {
                this.add(new SearchHit(a.getJSONObject(i)));
            }
        }

    }

    public ComponentName getName() {
        return this.name;
    }

    public JSONArray toJsonArray() {
        JSONArray a = new JSONArray();
        for (SearchHit h : this) {
            a.put(h.getRaw());
        }
        return a;
    }

}
