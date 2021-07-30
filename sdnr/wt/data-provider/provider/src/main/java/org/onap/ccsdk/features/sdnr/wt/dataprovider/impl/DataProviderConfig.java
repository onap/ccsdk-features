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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.impl;

import org.onap.ccsdk.features.sdnr.wt.common.configuration.Configuration;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.elasticsearch.EsConfig;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.SqlDBConfig;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.SdnrDbType;

public class DataProviderConfig implements Configuration {

    private static final String PROPERTY_KEY_DBTYPE = "dbType";
    private static final Object DEFAULT_DBTYPE = "${SDNRDBTYPE}";
    private static final SdnrDbType DEFAULT_DBTYPE_VALUE = SdnrDbType.ELASTICSEARCH;
    private final EsConfig esConfig;
    private final SqlDBConfig maridadbConfig;
    private ConfigurationFileRepresentation configuration;

    public DataProviderConfig(ConfigurationFileRepresentation configuration) {
        this.configuration = configuration;
        defaults();
        this.esConfig = new EsConfig(configuration);
        this.maridadbConfig = new SqlDBConfig(configuration);
    }

    public EsConfig getEsConfig() {
        return this.esConfig;
    }

    public SqlDBConfig getMariadbConfig() {
        return this.maridadbConfig;
    }

    @Override
    public void defaults() {

        configuration.setPropertyIfNotAvailable(this.getSectionName(), PROPERTY_KEY_DBTYPE, DEFAULT_DBTYPE);
    }

    @Override
    public String getSectionName() {
        return ConfigurationFileRepresentation.SECTIONNAME_ROOT;
    }

    public SdnrDbType getDbType() {
        String value = this.configuration.getProperty(this.getSectionName(), PROPERTY_KEY_DBTYPE);
        if (value.isEmpty()) {
            return DEFAULT_DBTYPE_VALUE;
        }
        return SdnrDbType.valueOf(value);
    }


}
