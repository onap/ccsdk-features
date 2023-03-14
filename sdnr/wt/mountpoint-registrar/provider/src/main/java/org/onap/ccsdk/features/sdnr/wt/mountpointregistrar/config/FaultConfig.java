/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
 * Copyright (C) 2021 Samsung Electronics Intellectual Property. All rights reserved.
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

import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;

public class FaultConfig extends MessageConfig {

    private static final String SECTION_MARKER = "fault";
    private static final String DEFAULT_VALUE_CONSUMER_TOPIC = "unauthenticated.SEC_FAULT_OUTPUT";


    public FaultConfig(ConfigurationFileRepresentation configuration) {
        super(configuration);
        sectionMarker = SECTION_MARKER;
        super.configuration.addSection(SECTION_MARKER);
        super.configuration.setPropertyIfNotAvailable(SECTION_MARKER, PROPERTY_KEY_CONSUMER_TOPIC,
                DEFAULT_VALUE_CONSUMER_TOPIC);
        defaults();
    }

}
