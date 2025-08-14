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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.database.nodb;

import java.util.HashMap;
import java.util.Map;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data.HtUserdataManagerBase;

public class NoDbHtUserdataManager extends HtUserdataManagerBase {

    private final Map<String, String> userDataStore = new HashMap<>();

    @Override
    protected String readUserdata(String username, String defaultValue) {
        return this.userDataStore.getOrDefault(username, defaultValue);
    }

    @Override
    public boolean setUserdata(String username, String data) {
        this.userDataStore.put(username, data);
        return true;
    }

    @Override
    public boolean removeUserdata(String username) {
        this.userDataStore.remove(username);
        return true;
    }
}

