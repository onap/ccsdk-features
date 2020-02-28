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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.impl;

import org.onap.ccsdk.features.sdnr.wt.common.configuration.Configuration;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;

public class DeviceManagerOnfConfiguration implements Configuration {

    private static final String SECTION_MARKER_DMONF = "dmonf";

    private static final String DEFAULT_VALUE_ENABLED = String.valueOf(false);
    private static final String PROPERTY_KEY_POLLALARMS = "pollAlarms";

    private final ConfigurationFileRepresentation configuration;

    public DeviceManagerOnfConfiguration(ConfigurationFileRepresentation configuration) {
        this.configuration = configuration;
        this.configuration.addSection(SECTION_MARKER_DMONF);
        defaults();
    }

    public boolean isPollAlarmsEnabled() {
        return configuration.getPropertyBoolean(SECTION_MARKER_DMONF, PROPERTY_KEY_POLLALARMS);
    }

    @Override
    public String getSectionName() {
        return SECTION_MARKER_DMONF;
    }

    @Override
    public void defaults() {
        //Add default if not available
        configuration.setPropertyIfNotAvailable(SECTION_MARKER_DMONF, PROPERTY_KEY_POLLALARMS, DEFAULT_VALUE_ENABLED);
    }

}
