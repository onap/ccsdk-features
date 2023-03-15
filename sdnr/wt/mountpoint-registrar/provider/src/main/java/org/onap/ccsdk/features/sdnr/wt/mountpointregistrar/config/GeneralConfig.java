/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
 * =================================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * ============LICENSE_END==========================================================================
 */
package org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.config;

import org.onap.ccsdk.features.sdnr.wt.common.configuration.Configuration;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;

/**
 * Configuration of mountpoint-registrar, general section<br>
 */
public class GeneralConfig implements Configuration {

    private static final String SECTION_MARKER = "general";

    private static final String PROPERTY_KEY_USER = "sdnrUser";
    private static final String DEFAULT_VALUE_USER = "${SDNRUSERNAME}";

    private static final String PROPERTY_KEY_USERPASSWD = "sdnrPasswd";
    private static final String DEFAULT_VALUE_USERPASSWD = "${SDNRPASSWORD}";

    private static final String PROPERTY_KEY_BASEURL = "baseUrl";
    private static final String DEFAULT_VALUE_BASEURL = "http://localhost:8181";


    private ConfigurationFileRepresentation configuration;

    public GeneralConfig(ConfigurationFileRepresentation configuration) {
        this.configuration = configuration;
        configuration.addSection(SECTION_MARKER);
        defaults();
    }

    public String getBaseUrl() {
        return configuration.getProperty(SECTION_MARKER, PROPERTY_KEY_BASEURL);
    }

    public String getSDNRUser() {
        return configuration.getProperty(SECTION_MARKER, PROPERTY_KEY_USER);
    }

    public String getSDNRPasswd() {
        return configuration.getProperty(SECTION_MARKER, PROPERTY_KEY_USERPASSWD);
    }

    @Override
    public String getSectionName() {
        return SECTION_MARKER;
    }

    @Override
    public void defaults() {
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_BASEURL, DEFAULT_VALUE_BASEURL);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_USER, DEFAULT_VALUE_USER);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_USERPASSWD, DEFAULT_VALUE_USERPASSWD);
    }



}
