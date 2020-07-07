/*******************************************************************************
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
 ******************************************************************************/
package org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl;

import org.onap.ccsdk.features.sdnr.wt.common.configuration.Configuration;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;

/**
 * Configuration of mountpoint-registrar, general section<br>
 * - dmaapEnabled : Boolean disable/enable service depending on whether DMaaP is running or not Generates default
 * Configuration properties if none exist or exist partially Generates Consumer properties only for
 * TransportType=HTTPNOAUTH. Other TransportTypes like HTTP, AUTH_KEY and DME2 have additional properties and are not
 * generated by default. For a list of applicable properties for the different TranportType values, please see -
 * https://wiki.onap.org/display/DW/Feature+configuration+requirements
 */
public class GeneralConfig implements Configuration {

    private static final String SECTION_MARKER = "general";

    private static final String PROPERTY_KEY_ENABLED = "dmaapEnabled"; //"enabled";

    private static final String PROPERTY_KEY_USER = "sdnrUser";
    private static final String DEFAULT_VALUE_USER = "admin";

    private static final String PROPERTY_KEY_USERPASSWD = "sdnrPasswd";
    private static final String DEFAULT_VALUE_USERPASSWD = "admin";

    private static final String PROPERTY_KEY_BASEURL = "baseUrl";
    private static final String DEFAULT_VALUE_BASEURL = "http://localhost:8181";


    private static ConfigurationFileRepresentation configuration;

    public GeneralConfig(ConfigurationFileRepresentation configuration) {
        GeneralConfig.configuration = configuration;
        GeneralConfig.configuration.addSection(SECTION_MARKER);
        defaults();
    }

    public Boolean getEnabled() {
        Boolean enabled = configuration.getPropertyBoolean(SECTION_MARKER, PROPERTY_KEY_ENABLED);
        return enabled;
    }

    public static String getBaseUrl() {
        return configuration.getProperty(SECTION_MARKER, PROPERTY_KEY_BASEURL);
    }

    public static String getSDNRUser() {
        return configuration.getProperty(SECTION_MARKER, PROPERTY_KEY_USER);
    }

    public static String getSDNRPasswd() {
        return configuration.getProperty(SECTION_MARKER, PROPERTY_KEY_USERPASSWD);
    }

    @Override
    public String getSectionName() {
        return SECTION_MARKER;
    }

    @Override
    public void defaults() {
        // The default value should be "false" given that SDNR can be run in environments where DMaaP is not used
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_ENABLED, Boolean.FALSE);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_BASEURL, DEFAULT_VALUE_BASEURL);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_USER, DEFAULT_VALUE_USER);
        configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_USERPASSWD, DEFAULT_VALUE_USERPASSWD);
    }



}
