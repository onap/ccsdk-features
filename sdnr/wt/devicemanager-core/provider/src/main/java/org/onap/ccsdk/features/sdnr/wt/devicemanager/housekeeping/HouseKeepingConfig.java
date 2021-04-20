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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.housekeeping;

import org.onap.ccsdk.features.sdnr.wt.common.configuration.Configuration;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;

/**
 * @author Michael Dürre
 *
 */
public class HouseKeepingConfig implements Configuration {

    private static final String SECTION_MARKER_HK = "housekeeping";

    private static final String PROPERTY_KEY_ENABLED = "hkEnabled";

    private static final boolean DEFAULT_VALUE_ENABLED = false;

    private final ConfigurationFileRepresentation configuration;

    public HouseKeepingConfig(ConfigurationFileRepresentation configuration) {
        this.configuration = configuration;
        this.configuration.addSection(SECTION_MARKER_HK);
        defaults();
    }

    public boolean isEnabled() {
        return configuration.getPropertyBoolean(SECTION_MARKER_HK, PROPERTY_KEY_ENABLED);
    }


    @Override
    public String getSectionName() {
        return SECTION_MARKER_HK;
    }

    @Override
    public void defaults() {
        //Add default if not available
        configuration.setPropertyIfNotAvailable(SECTION_MARKER_HK, PROPERTY_KEY_ENABLED, DEFAULT_VALUE_ENABLED);
    }

}
