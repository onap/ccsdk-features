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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.releases;

import java.util.HashMap;
import java.util.Map;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.SqlDBClient;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.ReleaseInformation;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.ComponentName;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.DatabaseInfo;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.Release;

public class ElAltoReleaseInformation extends ReleaseInformation {

    public ElAltoReleaseInformation() {
        super(Release.EL_ALTO, createDbInfos());
    }

    private static Map<ComponentName, DatabaseInfo> createDbInfos() {
        Map<ComponentName, DatabaseInfo> map = new HashMap<>();
        map.put(ComponentName.EVENTLOG, new DatabaseInfo("sdnevents", "eventlog", ""));
        map.put(ComponentName.FAULTCURRENT, new DatabaseInfo("sdnevents", "faultcurrent", ""));
        map.put(ComponentName.FAULTLOG, new DatabaseInfo("sdnevents", "faultlog", ""));
        map.put(ComponentName.INVENTORY, new DatabaseInfo("sdnevents", "inventoryequipment", ""));
        map.put(ComponentName.INVENTORYTOPLEVEL, new DatabaseInfo("sdnevents", "inventorytoplevel", ""));
        map.put(ComponentName.HISTORICAL_PERFORMANCE_15M,
                new DatabaseInfo("sdnperformance", "historicalperformance15min", ""));
        map.put(ComponentName.HISTORICAL_PERFORMANCE_24H,
                new DatabaseInfo("sdnperformance", "historicalperformance24h", ""));
        map.put(ComponentName.REQUIRED_NETWORKELEMENT, new DatabaseInfo("mwtn", "required-networkelement",
                "{\"required-networkelement\": {\"date_detection\": false }}"));
        map.put(ComponentName.MEDIATOR_SERVER, new DatabaseInfo("mwtn", "mediator-server", ""));
        map.put(ComponentName.MAINTENANCE, new DatabaseInfo("mwtn", "maintenancemode", ""));
        return map;
    }

    /**
     * @return components used in el alto
     */

    @Override
    public boolean runPreInitCommands(SqlDBClient dbClient) {
        return false;
    }

    @Override
    public boolean runPostInitCommands(SqlDBClient dbClient) {
        return false;
    }
}
