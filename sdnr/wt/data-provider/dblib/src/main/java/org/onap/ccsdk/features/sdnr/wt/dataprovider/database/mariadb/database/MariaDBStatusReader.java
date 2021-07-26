/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2021 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.database.mariadb.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.mariadb.MariaDBClient;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.mariadb.data.rpctypehelper.QueryResult;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ConnectionLogStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Entity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SeverityType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.status.output.Data;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.status.output.DataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.status.entity.FaultsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.status.entity.NetworkElementConnectionsBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MariaDBStatusReader {

    private final Logger LOG = LoggerFactory.getLogger(MariaDBStatusReader.class);

    private final MariaDBClient dbClient;
    private final String controllerId;

    public MariaDBStatusReader(MariaDBClient dbClient, String controllerId) {
        this.dbClient = dbClient;
        this.controllerId = controllerId;
    }

    public QueryResult<Data> getDataStatus() {
        String selectQuery = createCountQuery("severity", Entity.Faultcurrent, this.controllerId);
        long criticalCount = 0;
        long majorCount = 0;
        long minorCount = 0;
        long warningCount = 0;
        ResultSet data;
        try {
            data = this.dbClient.read(selectQuery);
            String severity;
            while (data.next()) {
                severity = data.getString(1);
                if (severity != null) {
                    if (severity.equals(SeverityType.Critical.getName())) {
                        criticalCount = data.getLong(2);
                    } else if (severity.equals(SeverityType.Major.getName())) {
                        majorCount = data.getLong(2);
                    } else if (severity.equals(SeverityType.Minor.getName())) {
                        minorCount = data.getLong(2);
                    } else if (severity.equals(SeverityType.Warning.getName())) {
                        warningCount = data.getLong(2);
                    }

                }
            }
        } catch (SQLException e) {
            LOG.warn("problem reading status:", e);
        }
        DataBuilder builder = new DataBuilder().setFaults(
                new FaultsBuilder().setCriticals(Uint32.valueOf(criticalCount)).setMajors(Uint32.valueOf(majorCount))
                        .setMinors(Uint32.valueOf(minorCount)).setWarnings(Uint32.valueOf(warningCount)).build());
        selectQuery = createCountQuery("status", Entity.NetworkelementConnection, this.controllerId);
        NetworkElementConnectionsBuilder neBuilder = new NetworkElementConnectionsBuilder();
        String state;
        long connectedCount = 0, connectingCount = 0, disconnectedCount = 0, mountedCount = 0, unableToConnectCount = 0,
                undefinedCount = 0, unmountedCount = 0;
        long cnt;
        try {
            data = this.dbClient.read(selectQuery);
            while (data.next()) {
                state = data.getString(1);
                cnt = data.getLong(2);
                if (state != null) {
                    if (state.equals(ConnectionLogStatus.Connected.getName())) {
                        connectedCount = cnt;
                    } else if (state.equals(ConnectionLogStatus.Connecting.getName())) {
                        connectingCount = cnt;
                    } else if (state.equals(ConnectionLogStatus.Disconnected.getName())) {
                        disconnectedCount = cnt;
                    } else if (state.equals(ConnectionLogStatus.Mounted.getName())) {
                        mountedCount = cnt;
                    } else if (state.equals(ConnectionLogStatus.UnableToConnect.getName())) {
                        unableToConnectCount = cnt;
                    } else if (state.equals(ConnectionLogStatus.Undefined.getName())) {
                        undefinedCount = cnt;
                    } else if (state.equals(ConnectionLogStatus.Unmounted.getName())) {
                        unmountedCount = cnt;
                    }
                }
            }
        } catch (SQLException e) {
            LOG.warn("problem reading status:", e);
        }
        neBuilder.setConnected(Uint32.valueOf(connectedCount)).setConnecting(Uint32.valueOf(connectingCount))
                .setDisconnected(Uint32.valueOf(disconnectedCount)).setMounted(Uint32.valueOf(mountedCount))
                .setTotal(Uint32.valueOf(connectedCount + connectingCount + disconnectedCount + mountedCount
                        + unableToConnectCount + undefinedCount + unmountedCount))
                .setUnableToConnect(Uint32.valueOf(unableToConnectCount)).setUndefined(Uint32.valueOf(undefinedCount))
                .setUnmounted(Uint32.valueOf(unmountedCount));
        builder.setNetworkElementConnections(neBuilder.build());
        return new QueryResult<Data>(Arrays.asList(builder.build()), 1, 1, 1);
    }

    private static String createCountQuery(String key, Entity e, String controllerId) {
        return String.format("SELECT `%s`, COUNT(`%s`) " + "FROM `%s` " + "%s " + "GROUP BY `%s`;", key, key,
                e.getName(),
                controllerId != null ? String.format("WHERE `%s`='%s'", MariaDBMapper.ODLID_DBCOL, controllerId) : "",
                key);
    }

}
