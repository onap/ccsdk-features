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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.guilin;

import java.util.HashMap;
import java.util.Map;

import org.onap.ccsdk.features.sdnr.wt.common.database.HtDatabaseClient;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.ReleaseInformation;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.ComponentName;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.DatabaseInfo;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.KeepDataSearchHitConverter;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.Release;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.SearchHitConverter;

public class GuilinReleaseInformationR2 extends ReleaseInformation {

    /**
     * @param r
     * @param dbMap
     */
    public GuilinReleaseInformationR2() {
        super(Release.GUILIN_R2, createDBMap());

    }

    private static Map<ComponentName, DatabaseInfo> createDBMap() {
        Map<ComponentName, DatabaseInfo> map = new HashMap<>();
        map.put(ComponentName.EVENTLOG, new DatabaseInfo("eventlog", "eventlog", ""));
        map.put(ComponentName.FAULTCURRENT, new DatabaseInfo("faultcurrent", "faultcurrent", ""));
        map.put(ComponentName.FAULTLOG, new DatabaseInfo("faultlog", "faultlog", ""));
        map.put(ComponentName.INVENTORY, new DatabaseInfo("inventoryequipment", "inventoryequipment", ""));
        map.put(ComponentName.HISTORICAL_PERFORMANCE_15M,
                new DatabaseInfo("historicalperformance15min", "historicalperformance15min", ""));
        map.put(ComponentName.HISTORICAL_PERFORMANCE_24H,
                new DatabaseInfo("historicalperformance24h", "historicalperformance24h", ""));
        map.put(ComponentName.REQUIRED_NETWORKELEMENT,
                new DatabaseInfo("networkelement-connection", "networkelement-connection", ""));
        map.put(ComponentName.MEDIATOR_SERVER, new DatabaseInfo("mediator-server", "mediator-server", ""));
        map.put(ComponentName.MAINTENANCE, new DatabaseInfo("maintenancemode", "maintenancemode", ""));
        return map;
    }

    @Override
    public SearchHitConverter getConverter(Release dst, ComponentName comp) {
        if (dst == Release.GUILIN_R2) {
            return new KeepDataSearchHitConverter(comp);
        }
        return null;
    }

    @Override
    protected boolean runPreInitCommands(HtDatabaseClient dbClient) {
        return true;
    }

    @Override
    protected boolean runPostInitCommands(HtDatabaseClient dbClient) {
        return true;
    }

}
