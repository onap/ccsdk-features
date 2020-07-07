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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.elalto;

import java.util.HashMap;
import java.util.Map;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.ComponentName;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.DatabaseInfo;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.KeepDataSearchHitConverter;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.Release;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.SearchHitConverter;
import org.onap.ccsdk.features.sdnr.wt.common.database.HtDatabaseClient;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.ReleaseInformation;

public class ElAltoReleaseInformation extends ReleaseInformation {


    private Map<Release, Map<ComponentName, SearchHitConverter>> converters;

    public ElAltoReleaseInformation() {
        super(Release.EL_ALTO, createDbInfos());
        this.converters = generateConverters();
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


    private static Map<Release, Map<ComponentName, SearchHitConverter>> generateConverters() {
        Map<Release, Map<ComponentName, SearchHitConverter>> c = new HashMap<>();
        Map<ComponentName, SearchHitConverter> frankfurtConverters = new HashMap<>();
        frankfurtConverters.put(ComponentName.EVENTLOG, new FrankfurtEventlogConverter());
        frankfurtConverters.put(ComponentName.FAULTCURRENT, new FrankfurtFaultcurrentConverter());
        frankfurtConverters.put(ComponentName.FAULTLOG, new FrankfurtFaultlogConverter());
        frankfurtConverters.put(ComponentName.INVENTORY, new KeepDataSearchHitConverter(ComponentName.INVENTORY));
        //obsolete in frankfurt
        //frankfurtConverters.put(ComponentName.INVENTORYTOPLEVEL, new KeepDataSearchHitConverter(ComponentName.INVENTORYTOPLEVEL));
        frankfurtConverters.put(ComponentName.HISTORICAL_PERFORMANCE_15M,
                new KeepDataSearchHitConverter(ComponentName.HISTORICAL_PERFORMANCE_15M));
        frankfurtConverters.put(ComponentName.HISTORICAL_PERFORMANCE_24H,
                new KeepDataSearchHitConverter(ComponentName.HISTORICAL_PERFORMANCE_24H));
        frankfurtConverters.put(ComponentName.MAINTENANCE, new FrankfurtMaintenanceConverter());
        frankfurtConverters.put(ComponentName.MEDIATOR_SERVER,
                new KeepDataSearchHitConverter(ComponentName.MEDIATOR_SERVER));
        frankfurtConverters.put(ComponentName.REQUIRED_NETWORKELEMENT, new FrankfurtRequiredNetworkElementConverter());
        frankfurtConverters.put(ComponentName.CONNECTIONLOG, new FrankfurtConnectionlogConverter());
        c.put(Release.FRANKFURT_R1, frankfurtConverters);
        return c;
    }

    @Override
    public SearchHitConverter getConverter(Release dst, ComponentName comp) {
        SearchHitConverter c = this.converters.containsKey(dst) ? this.converters.get(dst).get(comp) : null;
        if (c == null) {
            c = super.getConverter(dst, comp);
        }
        return c;
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
