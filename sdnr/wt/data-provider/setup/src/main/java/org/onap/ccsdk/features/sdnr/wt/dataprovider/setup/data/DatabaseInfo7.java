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

/**
 * @author Michael Dürre
 *
 */
public class DatabaseInfo7 extends DatabaseInfo {

    private boolean disableMapping;

    public DatabaseInfo7(String alias, String doctype, String mapping) {
        super(alias, alias, doctype, mapping);
        this.disableMapping = false;
    }
    public DatabaseInfo7(String alias, String doctype, String mapping, String settingsformat) {
        super(alias, alias, doctype, mapping, settingsformat);
        this.disableMapping = false;
    }
    public DatabaseInfo7(String index, String alias, String doctype, String mapping, String settingsformat) {
        super(index, alias, doctype, mapping, settingsformat);
        this.disableMapping = false;
    }

    @Override
    public String getMapping(boolean useStrict) {
        if(this.disableMapping){
            return "{\"enabled\": false}";
        }
        return this.mapping == null ? null
                : String.format("{%s\"properties\":%s}", useStrict ? "\"dynamic\": false," : "\"dynamic\": true,",
                        this.mapping);
    }

    public DatabaseInfo disableMapping() {
        this.disableMapping = true;
        return this;
    }
}
