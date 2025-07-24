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
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.SqlDBConfig;

public class DataProviderConfig implements Configuration {

    private static final String PROPERTY_KEY_DBENABLED = "enabled";
    private static final String DEFAULT_ISENABLED = "${SDNRDBENABLED}";
    private static final boolean DEFAULT_ISENABLED_IFNOTSET = true;
    private static final String PROPERTY_KEY_GUICUTTHROUGH_OVERWRITE = "GUICUTTHROUGH_PROXY_OVERWRITE";
    private static final String DEFAULT_GUICUTTHROUGH_OVERWRITE = "${GUICUTTHROUGH_PROXY_OVERWRITE}";
    private final SqlDBConfig maridadbConfig;
    private final ConfigurationFileRepresentation configuration;

    public DataProviderConfig(ConfigurationFileRepresentation configuration) {
        this.configuration = configuration;
        defaults();
        this.maridadbConfig = new SqlDBConfig(configuration);
    }


    public SqlDBConfig getMariadbConfig() {
        return this.maridadbConfig;
    }

    public boolean isEnabled() {
        final String s = this.configuration.getProperty(ConfigurationFileRepresentation.SECTIONNAME_ROOT,
                PROPERTY_KEY_DBENABLED);
        if (s != null && !s.isBlank()) {
            return Boolean.getBoolean(s);
        }
        return DEFAULT_ISENABLED_IFNOTSET;
    }

    @Override
    public void defaults() {

        configuration.setPropertyIfNotAvailable(this.getSectionName(), PROPERTY_KEY_DBENABLED, DEFAULT_ISENABLED);
        configuration.setPropertyIfNotAvailable(this.getSectionName(), PROPERTY_KEY_GUICUTTHROUGH_OVERWRITE,
                DEFAULT_GUICUTTHROUGH_OVERWRITE);
    }

    @Override
    public String getSectionName() {
        return ConfigurationFileRepresentation.SECTIONNAME_ROOT;
    }

    public String getGuicutthroughOverride() {
        return this.configuration.getProperty(this.getSectionName(), PROPERTY_KEY_GUICUTTHROUGH_OVERWRITE);
    }

}
