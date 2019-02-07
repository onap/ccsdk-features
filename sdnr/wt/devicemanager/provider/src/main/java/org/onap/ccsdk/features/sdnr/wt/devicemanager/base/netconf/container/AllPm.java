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
/**
 *
 */
package org.onap.ccsdk.features.sdnr.wt.devicemanager.base.netconf.container;

import java.util.ArrayList;
import java.util.List;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.performancemanager.impl.database.types.EsHistoricalPerformance15Minutes;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.performancemanager.impl.database.types.EsHistoricalPerformance24Hours;

public class AllPm {

    private final static AllPm EMPTY = new AllPm();

    private final List<EsHistoricalPerformance15Minutes> pm15 = new ArrayList<>();
    private final List<EsHistoricalPerformance24Hours> pm24 = new ArrayList<>();

    public void add(EsHistoricalPerformance15Minutes pm) {
        pm15.add(pm);
    }

    public void add(EsHistoricalPerformance24Hours pm) {
        pm24.add(pm);
    }

    public List<EsHistoricalPerformance15Minutes> getPm15() {
        return pm15;
    }

    public List<EsHistoricalPerformance24Hours> getPm24() {
        return pm24;
    }

    public Object size() {
        return pm15.size()+pm24.size();
    }

    public static AllPm getEmpty() {
        return EMPTY;
    }

}
