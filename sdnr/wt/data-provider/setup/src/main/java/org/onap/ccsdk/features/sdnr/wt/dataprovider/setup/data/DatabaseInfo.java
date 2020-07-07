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
public class DatabaseInfo {
    public final String doctype;
    public final String alias;
    protected final String mapping;
    private final String settingsFormat;
    private final String index;

    public String getIndex(String version) {
        return this.index + version;
    }

    public DatabaseInfo(String alias, String doctype, String mapping) {
        this(alias, alias, doctype, mapping);
    }

    public DatabaseInfo(String index, String alias, String doctype, String mapping) {
        this(index, alias, doctype, mapping,
                "{\"index\":{\"number_of_shards\":%d,\"number_of_replicas\":%d},\"analysis\":{\"analyzer\":{\"content\":"
                        + "{\"type\":\"custom\",\"tokenizer\":\"whitespace\"}}}}");
    }

    public DatabaseInfo(String index, String alias, String doctype, String mapping, String settingsformat) {
        this.index = index;
        this.alias = alias;
        this.doctype = doctype;
        this.mapping = mapping;
        this.settingsFormat = settingsformat;
    }

    public String getMapping() {
        return this.getMapping(false);
    }

    public String getMapping(boolean useStrict) {
        return this.mapping == null ? null
                : String.format("{\"%s\":{%s\"properties\":%s}}", this.doctype,
                        useStrict ? "\"dynamic\": \"strict\"," : "", this.mapping);
    }

    public String getSettings(int shards, int replicas) {
        return String.format(this.settingsFormat, shards, replicas);
    }
}
