/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2022 highstreet technologies GmbH Intellectual Property.
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

package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.dataprovider;

import java.util.Map;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SeverityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InternalDataModelSeverity {

    private static Logger log = LoggerFactory.getLogger(InternalDataModelSeverity.class);
    // @formatter:off
    private static final Map<String, SeverityType> SEVERITYMAP = Map.of(
            "SEVERITY_TYPE_MAJOR", SeverityType.Major,
            "SEVERITY_AND_CLEARED_TYPE_MAJOR", SeverityType.Major,
            "SEVERITY_TYPE_CRITICAL", SeverityType.Critical,
            "SEVERITY_AND_CLEARED_TYPE_CRITICAL", SeverityType.Critical,
            "SEVERITY_TYPE_MINOR", SeverityType.Minor,
            "SEVERITY_AND_CLEARED_TYPE_MINOR", SeverityType.Minor,
            "SEVERITY_TYPE_WARNING", SeverityType.Warning,
            "SEVERITY_AND_CLEARED_TYPE_WARNING", SeverityType.Warning,
            "SEVERITY_AND_CLEARED_TYPE_INDETERMINATE", SeverityType.NonAlarmed,
            "SEVERITY_AND_CLEARED_TYPE_CLEARED", SeverityType.NonAlarmed);
    // @formatter:on
    public static SeverityType mapSeverity(String severity) {
        log.debug("Severity is - {}", severity);
        SeverityType res = null;
        if (severity != null) {
            res = SEVERITYMAP.get(severity);
        }
        return res == null ? SeverityType.NonAlarmed : res;
    }

}
