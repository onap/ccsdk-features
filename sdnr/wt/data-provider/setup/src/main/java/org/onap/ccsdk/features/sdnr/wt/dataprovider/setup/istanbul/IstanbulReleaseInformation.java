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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.istanbul;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.onap.ccsdk.features.sdnr.wt.common.database.HtDatabaseClient;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.ClusterSettingsRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.ClusterSettingsResponse;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.SqlDBClient;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.ReleaseInformation;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.ComponentName;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.DatabaseInfo;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.DatabaseInfo7;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.KeepDataSearchHitConverter;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.MariaDBTableInfo;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.Release;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.SearchHitConverter;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.honolulu.HonoluluReleaseInformation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IstanbulReleaseInformation extends ReleaseInformation {

    private final Logger LOG = LoggerFactory.getLogger(IstanbulReleaseInformation.class);
    private static String NORMAL_STRING_MAX_LEN = "1024";
    public static final String TIMEZONE_TYPE_FORMAT =
            "CHAR(6) DEFAULT NULL CHECK (`%s` regexp '^[+-]\\\\d\\\\d:\\\\d\\\\d$')";
    public static final String TABLENAME_CONTROLLER_FORMAT = "controller%s";
    public static final String TABLEMAPPING_CONTROLLER =
            "`id` VARCHAR(40) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,`desc` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,primary key(id)";
    private static final String TABLEMAPPING_CONNECTIONLOG_FORMAT = "`id` int(11) NOT NULL AUTO_INCREMENT,\n"
            + "`controller-id` VARCHAR(40) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,\n" + "`timestamp` DATETIME(3) ,\n"
            + "`status` VARCHAR(100) CHARACTER SET utf8 COLLATE utf8_bin ,\n" + "`node-id` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n"
            + "primary key(id),foreign key(`controller-id`) references `controller%s`(id)";
    private static final String TABLEMAPPING_EVENTLOG_FORMAT = "`id` int(11) NOT NULL AUTO_INCREMENT,\n"
            + "`controller-id` VARCHAR(40) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,\n"
            + "`source-type` VARCHAR(100) CHARACTER SET utf8 COLLATE utf8_bin ,\n" + "`object-id` VARCHAR(" + NORMAL_STRING_MAX_LEN
            + ") CHARACTER SET utf8 COLLATE utf8_bin ,\n" + "`timestamp` DATETIME(3) ,\n" + "`timestamp-tz` "
            + String.format(TIMEZONE_TYPE_FORMAT, "timestamp-tz") + " ,\n"
            + "`node-id` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n" + "`counter` INTEGER ,\n" + "`attribute-name` VARCHAR("
            + NORMAL_STRING_MAX_LEN + ") CHARACTER SET utf8 COLLATE utf8_bin ,\n" + "`new-value` VARCHAR(" + NORMAL_STRING_MAX_LEN
            + ") CHARACTER SET utf8 COLLATE utf8_bin ,\n" + "primary key(id),foreign key(`controller-id`) references `controller%s`(id)";
    private static final String TABLEMAPPING_FAULTCURRENT_FORMAT = "`id` VARCHAR(" + NORMAL_STRING_MAX_LEN
            + ") CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,\n" + "`controller-id` VARCHAR(40) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,\n"
            + "`object-id` VARCHAR(" + NORMAL_STRING_MAX_LEN + ") CHARACTER SET utf8 COLLATE utf8_bin ,\n"
            + "`timestamp` DATETIME(3) ,\n" + "`timestamp-tz` " + String.format(TIMEZONE_TYPE_FORMAT, "timestamp-tz")
            + " ,\n" + "`node-id` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n" + "`counter` INTEGER ,\n"
            + "`severity` VARCHAR(100) CHARACTER SET utf8 COLLATE utf8_bin ,\n" + "`problem` VARCHAR(" + NORMAL_STRING_MAX_LEN
            + ") CHARACTER SET utf8 COLLATE utf8_bin ,\n" + "primary key(id),foreign key(`controller-id`) references `controller%s`(id)";
    private static final String TABLEMAPPING_FAULTLOG_FORMAT = "`id` int(11) NOT NULL AUTO_INCREMENT,\n"
            + "`controller-id` VARCHAR(40) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,\n" + "`source-type` VARCHAR("
            + NORMAL_STRING_MAX_LEN + ") CHARACTER SET utf8 COLLATE utf8_bin ,\n" + "`object-id` VARCHAR(" + NORMAL_STRING_MAX_LEN
            + ") CHARACTER SET utf8 COLLATE utf8_bin ,\n" + "`timestamp` DATETIME(3) ,\n" + "`timestamp-tz` "
            + String.format(TIMEZONE_TYPE_FORMAT, "timestamp-tz") + " ,\n"
            + "`node-id` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n" + "`counter` INTEGER ,\n"
            + "`severity` VARCHAR(100) CHARACTER SET utf8 COLLATE utf8_bin ,\n" + "`problem` VARCHAR(" + NORMAL_STRING_MAX_LEN
            + ") CHARACTER SET utf8 COLLATE utf8_bin ,\n" + "primary key(id),foreign key(`controller-id`) references `controller%s`(id)";
    private static final String TABLEMAPPING_GUICUTTHROUGH_FORMAT = "`id` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,\n"
            + "`controller-id` VARCHAR(40) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,\n"
            + "`name` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n" + "`weburi` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n"
            + "primary key(id),foreign key(`controller-id`) references `controller%s`(id)";
    private static final String TABLEMAPPING_HISTORICALPM15M_FORMAT = "`id` VARCHAR(" + NORMAL_STRING_MAX_LEN
            + ") CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,\n" + "`controller-id` VARCHAR(40) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,\n"
            + "`performance-data` JSON ,\n" + "`granularity-period` VARCHAR(100) CHARACTER SET utf8 COLLATE utf8_bin ,\n"
            + "`time-stamp` DATETIME(3) ,\n" + "`time-stamp-tz` " + String.format(TIMEZONE_TYPE_FORMAT, "time-stamp-tz")
            + " ,\n" + "`node-name` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n"
            + "`uuid-interface` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n"
            + "`layer-protocol-name` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n" + "`radio-signal-id` VARCHAR("
            + NORMAL_STRING_MAX_LEN + ") CHARACTER SET utf8 COLLATE utf8_bin ,\n" + "`suspect-interval-flag` BOOLEAN ,\n"
            + "`scanner-id` VARCHAR(" + NORMAL_STRING_MAX_LEN + ") CHARACTER SET utf8 COLLATE utf8_bin ,\n"
            + "primary key(id),foreign key(`controller-id`) references `controller%s`(id)";
    private static final String TABLEMAPPING_HISTORICALPM24H_FORMAT = "`id` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,\n"
            + "`controller-id` VARCHAR(40) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,\n" + "`performance-data` JSON ,\n"
            + "`granularity-period` VARCHAR(100) CHARACTER SET utf8 COLLATE utf8_bin ,\n" + "`time-stamp` DATETIME(3) ,\n"
            + "`time-stamp-tz` " + String.format(TIMEZONE_TYPE_FORMAT, "time-stamp-tz") + " ,\n"
            + "`node-name` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n" + "`uuid-interface` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n"
            + "`layer-protocol-name` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n"
            + "`radio-signal-id` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n" + "`suspect-interval-flag` BOOLEAN ,\n"
            + "`scanner-id` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n"
            + "primary key(id),foreign key(`controller-id`) references `controller%s`(id)";
    private static final String TABLEMAPPING_INVENTORY_FORMAT = "`id` VARCHAR(" + NORMAL_STRING_MAX_LEN
            + ") CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,\n" + "`controller-id` VARCHAR(40) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,\n"
            + "`version` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n" + "`type-name` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n"
            + "`date` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n" + "`description` VARCHAR(1024) CHARACTER SET utf8 COLLATE utf8_bin ,\n"
            + "`node-id` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n" + "`uuid` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n"
            + "`part-type-id` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n"
            + "`model-identifier` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n"
            + "`manufacturer-identifier` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n" + "`tree-level` BIGINT ,\n"
            + "`parent-uuid` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n" + "`contained-holder` JSON ,\n"
            + "`serial` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n" + "`manufacturer-name` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n"
            + "primary key(id),foreign key(`controller-id`) references `controller%s`(id)";
    private static final String TABLEMAPPING_MAINTENANCE_FORMAT = "`id` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,\n"
            + "`controller-id` VARCHAR(40) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,\n" + "`active` BOOLEAN ,\n"
            + "`node-id` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n" + "`description` VARCHAR(1024) CHARACTER SET utf8 COLLATE utf8_bin ,\n"
            + "`problem` VARCHAR("+NORMAL_STRING_MAX_LEN+") CHARACTER SET utf8 COLLATE utf8_bin ,\n" + "`start` DATETIME(3) ,\n" + "`start-tz` "
            + String.format(TIMEZONE_TYPE_FORMAT, "start-tz") + " ,\n" + "`end` DATETIME(3) ,\n" + "`end-tz` "
            + String.format(TIMEZONE_TYPE_FORMAT, "end-tz") + " ,\n"
            + "`object-id-ref` VARCHAR("+NORMAL_STRING_MAX_LEN+") CHARACTER SET utf8 COLLATE utf8_bin ,\n"
            + "primary key(id),foreign key(`controller-id`) references `controller%s`(id)";
    private static final String TABLEMAPPING_MEDIATORSERVER_FORMAT =
            "`id` int(11) NOT NULL AUTO_INCREMENT,\n" + "`controller-id` VARCHAR(40) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,\n"
                    + "`name` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n" + "`url` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n"
                    + "primary key(id),foreign key(`controller-id`) references `controller%s`(id)";
    private static final String TABLEMAPPING_NETWORKELEMENT_FORMAT = "`id` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,\n"
            + "`controller-id` VARCHAR(40) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,\n"
            + "`password` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n" + "`host` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n"
            + "`port` BIGINT ,\n" + "`status` VARCHAR(100) CHARACTER SET utf8 COLLATE utf8_bin ,\n"
            + "`node-id` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n" + "`username` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n"
            + "`is-required` BOOLEAN ,\n" + "`core-model-capability` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n"
            + "`device-type` VARCHAR(100) CHARACTER SET utf8 COLLATE utf8_bin ,\n"
            + "`device-function` VARCHAR(512) CHARACTER SET utf8 COLLATE utf8_bin ,\n" + "`node-details` JSON ,\n"
            + "`tls-key` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n" + "`mount-method` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n"
            + "primary key(id),foreign key(`controller-id`) references `controller%s`(id)";
    private static final String TABLEMAPPING_USERDATA_FORMAT = "`id` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,\n"
            + "`value` JSON ,\n"
            + "primary key(id)";


    public IstanbulReleaseInformation() {
        super(Release.ISTANBUL_R1, createDBMap(), createMariaDBMap(Release.ISTANBUL_R1.getDBSuffix()));
    }

    public static Map<ComponentName, MariaDBTableInfo> createMariaDBMap(String suffix) {
        Map<ComponentName, MariaDBTableInfo> map = new HashMap<>();
        map.put(ComponentName.CONNECTIONLOG,
                new MariaDBTableInfo(Entity.Connectionlog.getName(), TABLEMAPPING_CONNECTIONLOG_FORMAT));
        map.put(ComponentName.EVENTLOG, new MariaDBTableInfo(Entity.Eventlog.getName(), TABLEMAPPING_EVENTLOG_FORMAT));
        map.put(ComponentName.FAULTCURRENT,
                new MariaDBTableInfo(Entity.Faultcurrent.getName(), TABLEMAPPING_FAULTCURRENT_FORMAT));
        map.put(ComponentName.FAULTLOG, new MariaDBTableInfo(Entity.Faultlog.getName(), TABLEMAPPING_FAULTLOG_FORMAT));
        map.put(ComponentName.GUICUTTHROUGH,
                new MariaDBTableInfo(Entity.Guicutthrough.getName(), TABLEMAPPING_GUICUTTHROUGH_FORMAT));
        map.put(ComponentName.HISTORICAL_PERFORMANCE_15M,
                new MariaDBTableInfo(Entity.Historicalperformance15min.getName(), TABLEMAPPING_HISTORICALPM15M_FORMAT));
        map.put(ComponentName.HISTORICAL_PERFORMANCE_24H,
                new MariaDBTableInfo(Entity.Historicalperformance24h.getName(), TABLEMAPPING_HISTORICALPM24H_FORMAT));
        map.put(ComponentName.INVENTORY,
                new MariaDBTableInfo(Entity.Inventoryequipment.getName(), TABLEMAPPING_INVENTORY_FORMAT));
        map.put(ComponentName.MAINTENANCE,
                new MariaDBTableInfo(Entity.Maintenancemode.getName(), TABLEMAPPING_MAINTENANCE_FORMAT));
        map.put(ComponentName.MEDIATOR_SERVER,
                new MariaDBTableInfo(Entity.MediatorServer.getName(), TABLEMAPPING_MEDIATORSERVER_FORMAT));
        map.put(ComponentName.REQUIRED_NETWORKELEMENT,
                new MariaDBTableInfo(Entity.NetworkelementConnection.getName(), TABLEMAPPING_NETWORKELEMENT_FORMAT));
        map.put(ComponentName.USERDATA, new MariaDBTableInfo(Entity.Userdata.getName(), TABLEMAPPING_USERDATA_FORMAT));
        return map;
    }

    public static Map<ComponentName, DatabaseInfo> createDBMap() {
        Map<ComponentName, DatabaseInfo> map = HonoluluReleaseInformation.createDBMap();
        map.put(ComponentName.USERDATA, new DatabaseInfo7("userdata", "userdata", "").disableMapping());
        map.put(ComponentName.REQUIRED_NETWORKELEMENT, new DatabaseInfo7("networkelement-connection",
                "networkelement-connection",
                "{\"node-id\": {\"type\": \"keyword\"},\"host\": {\"type\": \"keyword\"},\"port\": "
                        + "{\"type\": \"long\"},\"username\": {\"type\": \"keyword\"},\"password\": {\"type\": \"keyword\"},"
                        + "\"core-model-capability\": {\"type\": \"keyword\"},\"device-type\": {\"type\": \"keyword\"},"
                        + "\"device-function\": {\"type\": \"keyword\"},\"is-required\": {\"type\": \"boolean\"},"
                        + "\"status\": {\"type\": \"keyword\"},\"tls-key\": {\"type\": \"keyword\"},"
                        + "\"mount-method\": {\"type\":\"keyword\"}}",
                "{\"index\":{\"max_result_window\": 20000,\"number_of_shards\":%d,\"number_of_replicas\":%d},"
                        + "\"analysis\":{\"analyzer\":{\"content\":{\"type\":\"custom\",\"tokenizer\":\"whitespace\"}}}}"));
        return map;
    }

    @Override
    public SearchHitConverter getConverter(Release dst, ComponentName comp) {
        if (dst == Release.ISTANBUL_R1) {
            return new KeepDataSearchHitConverter(comp);
        }
        return null;
    }

    @Override
    public boolean runPreInitCommands(HtDatabaseClient dbClient) {
        ClusterSettingsResponse response = null;
        try {
            response = dbClient.setupClusterSettings(new ClusterSettingsRequest(false).maxCompilationsPerMinute(400));
        } catch (IOException e) {
            LOG.warn("problem setting up cluster: {}", e);
        }
        return response == null ? false : response.isAcknowledged();
    }

    @Override
    public boolean runPostInitCommands(HtDatabaseClient dbClient) {
        return true;
    }

    @Override
    public boolean runPreInitCommands(SqlDBClient dbClient) {
        boolean success = dbClient.createTable(
                String.format(TABLENAME_CONTROLLER_FORMAT, this.getReleas().getDBSuffix()), TABLEMAPPING_CONTROLLER);
        return success;
    }

    @Override
    public boolean runPostInitCommands(SqlDBClient dbClient) {
        return true;
    }

}
