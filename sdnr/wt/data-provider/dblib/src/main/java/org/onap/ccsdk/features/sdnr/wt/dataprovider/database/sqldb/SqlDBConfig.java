/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2021 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb;

import org.onap.ccsdk.features.sdnr.wt.common.configuration.Configuration;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data.entity.DatabaseIdGenerator;

public class SqlDBConfig implements Configuration {

    private static final String SECTION_MARKER_MARIADB = "mariadb";

    private static final String PROPERTY_KEY_DBURL = "url";
    private static final String PROPERTY_KEY_USERNAME = "username";
    private static final String PROPERTY_KEY_PASSWORD = "password";
    private static final String PROPERTY_KEY_CONTROLLERID = "controllerId";
    private static final String PROPERTY_KEY_DBSUFFIX = "suffix";


    private static final String DEFAULT_VALUE_DBURL = "${SDNRDBURL}";
    private static final String DEFAULT_VALUE_DBUSERNAME = "${SDNRDBUSERNAME}";
    private static final String DEFAULT_VALUE_DBPASSWORD = "${SDNRDBPASSWORD}";
    private static final String DEFAULT_VALUE_CONTROLLERID = "${SDNRCONTROLLERID}";
    private static final String DEFAULT_VALUE_DBSUFFIX = "-v7";

    private final ConfigurationFileRepresentation configuration;

    public SqlDBConfig(ConfigurationFileRepresentation configuration) {

        this.configuration = configuration;
        this.configuration.addSection(SECTION_MARKER_MARIADB);
        defaults();
    }


    /*
     * Getter
     */

    public String getUrl() {
        return configuration.getProperty(SECTION_MARKER_MARIADB, PROPERTY_KEY_DBURL);
    }

    public void setUrl(String url) {
        configuration.setProperty(SECTION_MARKER_MARIADB, PROPERTY_KEY_DBURL, url);

    }

    public String getUsername() {
        return this.configuration.getProperty(SECTION_MARKER_MARIADB, PROPERTY_KEY_USERNAME);
    }

    public String getPassword() {
        return this.configuration.getProperty(SECTION_MARKER_MARIADB, PROPERTY_KEY_PASSWORD);
    }

    public String getControllerId() {
        String v = this.configuration.getProperty(SECTION_MARKER_MARIADB, PROPERTY_KEY_CONTROLLERID);
        return (v == null || v.equals("null") || v.isEmpty()) ? null : v;
    }

    public String getDbSuffix() {
        return this.configuration.getProperty(SECTION_MARKER_MARIADB, PROPERTY_KEY_DBSUFFIX);
    }

    @Override
    public String getSectionName() {
        return SECTION_MARKER_MARIADB;
    }

    @Override
    public synchronized void defaults() {
        // Add default if not available
        configuration.setPropertyIfNotAvailable(SECTION_MARKER_MARIADB, PROPERTY_KEY_DBURL, DEFAULT_VALUE_DBURL);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER_MARIADB, PROPERTY_KEY_USERNAME,
                DEFAULT_VALUE_DBUSERNAME);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER_MARIADB, PROPERTY_KEY_PASSWORD,
                DEFAULT_VALUE_DBPASSWORD);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER_MARIADB, PROPERTY_KEY_CONTROLLERID,
                DEFAULT_VALUE_CONTROLLERID);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER_MARIADB, PROPERTY_KEY_DBSUFFIX, DEFAULT_VALUE_DBSUFFIX);

        String v = this.configuration.getProperty(SECTION_MARKER_MARIADB, PROPERTY_KEY_CONTROLLERID);
        // if is set to "null" then it is valid, otherwise if not set or env is empty generate one
        if (!"null".equals(v) && (v == null || v.isEmpty())) {
            this.setControllerId(DatabaseIdGenerator.getControllerId());
        }

    }


    public void setControllerId(String id) {
        configuration.setProperty(SECTION_MARKER_MARIADB, PROPERTY_KEY_CONTROLLERID, id);
    }

    public void setDbSuffix(String suffix) {
        configuration.setProperty(SECTION_MARKER_MARIADB, PROPERTY_KEY_DBSUFFIX, suffix);
    }
}
