/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2022 highstreet technologies GmbH Intellectual Property. All rights reserved.
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

package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.pm;

import java.util.Map;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.GranularityPeriodType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Helper {

    private static Logger log = LoggerFactory.getLogger(Helper.class);
    private static final Map<String, GranularityPeriodType> GRANULARITYMAP =
            Map.of("GRANULARITY_PERIOD_TYPE_PERIOD-15-MIN", GranularityPeriodType.Period15Min,
                    "GRANULARITY_PERIOD_TYPE_PERIOD-24-HOURS", GranularityPeriodType.Period24Hours);

    public static GranularityPeriodType mapGranularityPeriod(String gp) {
        log.debug("Granularity Period is - {}", gp);
        GranularityPeriodType res = null;
        if (gp != null) {
            res = GRANULARITYMAP.get(gp);
        }
        return res == null ? GranularityPeriodType.Unknown : res;
    }

}
