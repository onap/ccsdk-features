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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.impl.dataprovider;

import java.util.Map;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yang.gen.v1.urn.onf.yang.wire._interface._2._0.rev200123.SEVERITYTYPECRITICAL;
import org.opendaylight.yang.gen.v1.urn.onf.yang.wire._interface._2._0.rev200123.SEVERITYTYPEMAJOR;
import org.opendaylight.yang.gen.v1.urn.onf.yang.wire._interface._2._0.rev200123.SEVERITYTYPEMINOR;
import org.opendaylight.yang.gen.v1.urn.onf.yang.wire._interface._2._0.rev200123.SEVERITYTYPEWARNING;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SeverityType;
import org.opendaylight.yangtools.yang.binding.BaseIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InternalDataModelSeverity {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(InternalDataModelSeverity.class);

    private static final Map<String, SeverityType> SEVERITYMAP = Map.of(
            SEVERITYTYPEMAJOR.class.getSimpleName(), SeverityType.Major,
            SEVERITYTYPECRITICAL.class.getSimpleName(), SeverityType.Critical,
            SEVERITYTYPEMINOR.class.getSimpleName(), SeverityType.Minor,
            SEVERITYTYPEWARNING.class.getSimpleName(), SeverityType.Warning);

    public static SeverityType mapSeverity(@Nullable Class<? extends BaseIdentity> severity) {
        SeverityType res = null;
        if (severity != null) {
            String severityName = severity.getSimpleName();
            res = severityName != null ? SEVERITYMAP.get(severity.getSimpleName()) : null;
        }
        return res == null ? SeverityType.NonAlarmed : res;
    }

}
