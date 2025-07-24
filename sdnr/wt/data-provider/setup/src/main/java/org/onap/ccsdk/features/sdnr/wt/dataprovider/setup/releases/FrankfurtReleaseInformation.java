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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FrankfurtReleaseInformation extends ReleaseInformation {

    private final Logger LOG = LoggerFactory.getLogger(FrankfurtReleaseInformation.class);

    public FrankfurtReleaseInformation() {
        super(Release.FRANKFURT_R1, createDBMap());
    }

    private static Map<ComponentName, DatabaseInfo> createDBMap() {
        Map<ComponentName, DatabaseInfo> map = new HashMap<>();
        map.put(ComponentName.CONNECTIONLOG, new DatabaseInfo("connectionlog", "connectionlog",
                "{\"node-id\": {\"type\": \"keyword\"},\"timestamp\": {\"type\": \"date\"},\"status\": {\"type\": \"keyword\"}}"));
        map.put(ComponentName.EVENTLOG, new DatabaseInfo("eventlog", "eventlog",
                "{\"node-id\": {\"type\": \"keyword\"},\"source-type\": {\"type\": \"keyword\"},\"timestamp\": {\"type\": \"date\"},\"new-value\": {\"type\": \"keyword\"},\"attribute-name\": {\"type\": \"keyword\"},\"counter\": {\"type\": \"long\"},\"object-id\": {\"type\": \"keyword\"}}"));
        map.put(ComponentName.FAULTCURRENT, new DatabaseInfo("faultcurrent", "faultcurrent",
                "{\"node-id\": {\"type\": \"keyword\"},\"severity\": {\"type\": \"keyword\"},\"timestamp\": {\"type\": \"date\"},\"problem\": {\"type\": \"keyword\"},\"counter\": {\"type\": \"long\"},\"object-id\":{\"type\": \"keyword\"}}"));
        map.put(ComponentName.FAULTLOG, new DatabaseInfo("faultlog", "faultlog",
                "{\"node-id\": {\"type\": \"keyword\"},\"severity\": {\"type\": \"keyword\"},\"timestamp\": {\"type\": \"date\"},\"problem\": {\"type\": \"keyword\"},\"counter\": {\"type\": \"long\"},\"object-id\":{\"type\": \"keyword\"},\"source-type\":{\"type\": \"keyword\"}}"));
        map.put(ComponentName.INVENTORY, new DatabaseInfo("inventoryequipment", "inventoryequipment",
                "{\"date\": {\"type\": \"keyword\"},\"model-identifier\": {\"type\": \"keyword\"},\"manufacturer-identifier\": {\"type\": \"keyword\"},\"type-name\": {\"type\": \"keyword\"},\"description\": {\"type\": \"keyword\"},\"uuid\": {\"type\": \"keyword\"},\"version\": {\"type\": \"keyword\"},\"parent-uuid\": {\"type\": \"keyword\"},\"contained-holder\": {\"type\": \"keyword\"},\"node-id\": {\"type\": \"keyword\"},\"tree-level\": {\"type\": \"long\"},\"part-type-id\": {\"type\": \"keyword\"},\"serial\": {\"type\": \"keyword\"}}"));
        map.put(ComponentName.HISTORICAL_PERFORMANCE_15M, new DatabaseInfo("historicalperformance15min",
                "historicalperformance15min",
                "{\"node-name\":{\"type\": \"keyword\"},\"timestamp\":{\"type\": \"date\"},\"suspect-interval-flag\":{\"type\":\"boolean\"},\"scanner-id\":{\"type\": \"keyword\"},\"uuid-interface\":{\"type\": \"keyword\"},\"layer-protocol-name\":{\"type\": \"keyword\"},\"granularity-period\":{\"type\": \"keyword\"},\"radio-signal-id\":{\"type\": \"keyword\"}}"));
        map.put(ComponentName.HISTORICAL_PERFORMANCE_24H, new DatabaseInfo("historicalperformance24h",
                "historicalperformance24h",
                "{\"node-name\":{\"type\": \"keyword\"},\"timestamp\":{\"type\": \"date\"},\"suspect-interval-flag\":{\"type\":\"boolean\"},\"scanner-id\":{\"type\": \"keyword\"},\"uuid-interface\":{\"type\": \"keyword\"},\"layer-protocol-name\":{\"type\": \"keyword\"},\"granularity-period\":{\"type\": \"keyword\"},\"radio-signal-id\":{\"type\": \"keyword\"}}"));
        map.put(ComponentName.REQUIRED_NETWORKELEMENT, new DatabaseInfo("networkelement-connection",
                "networkelement-connection",
                "{\"node-id\": {\"type\": \"keyword\"},\"host\": {\"type\": \"keyword\"},\"port\": {\"type\": \"long\"},\"username\": {\"type\": \"keyword\"},\"password\": {\"type\": \"keyword\"},\"core-model-capability\": {\"type\": \"keyword\"},\"device-type\": {\"type\": \"keyword\"},\"is-required\": {\"type\": \"boolean\"},\"status\": {\"type\": \"keyword\"}}"));
        map.put(ComponentName.MEDIATOR_SERVER, new DatabaseInfo("mediator-server", "mediator-server",
                "{\"url\":{\"type\": \"keyword\"},\"name\":{\"type\": \"keyword\"}}"));
        map.put(ComponentName.MAINTENANCE, new DatabaseInfo("maintenancemode", "maintenancemode",
                "{\"node-id\": {\"type\": \"keyword\"},\"start\": {\"type\": \"date\"},\"end\": {\"type\": \"date\"},\"description\": {\"type\": \"keyword\"},\"active\": {\"type\": \"boolean\"}},\"date_detection\":false}}"));
        return map;
    }

    @Override
    public boolean runPreInitCommands(SqlDBClient dbClient) {
        return false;
    }

    @Override
    public boolean runPostInitCommands(SqlDBClient dbClient) {
        return false;
    }
}
