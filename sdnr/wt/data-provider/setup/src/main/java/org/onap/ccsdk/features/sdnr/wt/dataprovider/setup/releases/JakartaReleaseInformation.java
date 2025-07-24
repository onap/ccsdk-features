/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Update Copyright (C) 2021 Samsung Electronics Intellectual Property. All rights reserved.
 * =================================================================================================
c * Licensed under the Apache License, Version 2.0 (the "License");
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

import java.util.Map;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.SqlDBClient;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.ReleaseInformation;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.ComponentName;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.DatabaseInfo;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.DatabaseInfo7;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.MariaDBTableInfo;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.Release;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JakartaReleaseInformation extends ReleaseInformation {

    private static final String TABLENAME_CONTROLLER_FORMAT = IstanbulReleaseInformation.TABLENAME_CONTROLLER_FORMAT;
    private static final String TABLEMAPPING_CONTROLLER = IstanbulReleaseInformation.TABLEMAPPING_CONTROLLER;
    private static final String TIMEZONE_TYPE_FORMAT = IstanbulReleaseInformation.TIMEZONE_TYPE_FORMAT;
    private final Logger LOG = LoggerFactory.getLogger(JakartaReleaseInformation.class);

    private static final String TABLEMAPPING_CMLOG_FORMAT = "`id` int(11) NOT NULL AUTO_INCREMENT,\n"
            + "`controller-id` VARCHAR(40) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,\n"
            + "`source-type` VARCHAR(100) CHARACTER SET utf8 COLLATE utf8_bin ,\n"
            + "`object-id` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n" + "`timestamp` DATETIME(3) ,\n" + "`timestamp-tz` "
            + String.format(TIMEZONE_TYPE_FORMAT, "timestamp-tz") + " ,\n"
            + "`node-id` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n" + "`counter` INTEGER ,\n"
            + "`notification-type` VARCHAR(100) CHARACTER SET utf8 COLLATE utf8_bin ,\n"
            + "`notification-id` VARCHAR(40) CHARACTER SET utf8 COLLATE utf8_bin ,\n"
            + "`source-indicator` VARCHAR(100) CHARACTER SET utf8 COLLATE utf8_bin ,\n"
            + "`path` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n" + "`operation` VARCHAR(100) CHARACTER SET utf8 COLLATE utf8_bin ,\n"
            + "`value` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_bin ,\n" + "primary key(id),foreign key(`controller-id`) "
            + "references `controller%s`(id) ";

    public JakartaReleaseInformation() {
        super(Release.JAKARTA_R1, createDBMap(), createMariaDBMap());
    }

    private static Map<ComponentName, DatabaseInfo> createDBMap() {
        Map<ComponentName, DatabaseInfo> map = IstanbulReleaseInformation.createDBMap();
        map.put(ComponentName.CMLOG, new DatabaseInfo7("cmlog", "cmlog",
                "{\"node-id\": {\"type\": \"keyword\"},\"counter\": {\"type\": \"long\"},"
                        + "\"notification-id\": {\"type\": \"date\"},\"notification-type\": {\"type\": \"keyword\"},"
                        + "\"object-id\": {\"type\": \"long\"},\"operation\":{\"type\": \"keyword\"},"
                        + "\"path\": {\"type\": \"long\"},\"source-indicator\":{\"type\": \"keyword\"},"
                        + "\"source-type\": {\"type\": \"long\"},\"timestamp\":{\"type\": \"keyword\"},"
                        + "\"value\":{\"type\": \"keyword\"}}"));
        return map;
    }

    private static Map<ComponentName, MariaDBTableInfo> createMariaDBMap() {
        Map<ComponentName, MariaDBTableInfo> map =
                IstanbulReleaseInformation.createMariaDBMap(Release.JAKARTA_R1.getDbSuffix());
        map.put(ComponentName.CMLOG, new MariaDBTableInfo(Entity.Cmlog.getName(), TABLEMAPPING_CMLOG_FORMAT));
        return map;
    }

    @Override
    public boolean runPreInitCommands(SqlDBClient dbClient) {
        boolean success = dbClient.createTable(
                String.format(TABLENAME_CONTROLLER_FORMAT, this.getReleas().getDbSuffix()), TABLEMAPPING_CONTROLLER);
        return success;
    }

    @Override
    public boolean runPostInitCommands(SqlDBClient dbClient) {
        return true;
    }

}
