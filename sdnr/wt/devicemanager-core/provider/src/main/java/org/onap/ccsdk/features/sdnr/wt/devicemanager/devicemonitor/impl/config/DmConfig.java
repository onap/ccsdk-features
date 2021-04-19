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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.devicemonitor.impl.config;

import org.onap.ccsdk.features.sdnr.wt.common.configuration.Configuration;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.devicemonitor.impl.DeviceMonitorProblems;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.util.InternalSeverity;

/**
 * Configuration of devicemonitor, section [devicemonitor] SeverityConnectionlossNeOAM=minor
 * SeverityConnectionlossOAM=major SeverityConnectionlossMediator=critical
 */
public class DmConfig implements Configuration {

    private static final String SECTION_MARKER_TA = "devicemonitor";

    private static final String PROPERTY_KEY_PREFIX_Severity = "Severity";

    private final ConfigurationFileRepresentation configuration;

    public DmConfig(ConfigurationFileRepresentation configuration) {
        this.configuration = configuration;
        this.configuration.addSection(SECTION_MARKER_TA);
        defaults();
    }

    public InternalSeverity getSeverity(DeviceMonitorProblems problem) {
        String severityString = configuration.getProperty(SECTION_MARKER_TA, getPropertyName(problem));
        InternalSeverity result = InternalSeverity.valueOfString(severityString);
        return result != null ? result : InternalSeverity.Major;
    }

    @Override
    public String getSectionName() {
        return SECTION_MARKER_TA;
    }

    @Override
    public void defaults() {
        for (DeviceMonitorProblems problem : DeviceMonitorProblems.values()) {
            configuration.setPropertyIfNotAvailable(SECTION_MARKER_TA, getPropertyName(problem),
                    problem.getSeverity().name());
        }
    }

    private String getPropertyName(DeviceMonitorProblems problem) {
        return PROPERTY_KEY_PREFIX_Severity + problem.name();
    }

}
