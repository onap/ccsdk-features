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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.setup;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.mariadb.data.MariaDBDataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.mariadb.database.MariaDBMapper;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.mariadb.query.InsertQuery;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.mariadb.query.SelectQuery;
import org.onap.ccsdk.sli.core.dblib.DbLibService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ConnectionLogStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Entity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementConnection;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementConnectionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementDeviceType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.network.element.connection.entity.NodeDetails;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.network.element.connection.entity.NodeDetailsBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.Uint32;
import ch.vorburger.exec.ManagedProcessException;

public class TestMariaDBCreate {

    private static final String MARIADB_USERNAME = "sdnrdb";
    private static final String MARIADB_PASSWORD = "sdnrdb";
    //    private static final String MARIADB_HOST = "10.20.11.159";
    private static final String MARIADB_HOST = "localhost";
    private static final int MARIADB_PORT = 3306;
    private static final String MARIADB_DATABASENAME = "sdnrdb";
    private static String CONTROLLERID = null;

    private static DbLibService dbService;

    private static MariaDBTestBase testBase;
    private static MariaDBDataProvider dbProvider;

    @BeforeClass
    public static void init() throws Exception {

        testBase = new MariaDBTestBase();
        dbProvider = testBase.getDbProvider();
        dbProvider.waitForDatabaseReady(30, TimeUnit.SECONDS);
        dbProvider.setControllerId();
        CONTROLLERID = dbProvider.getControllerId();

    }

    @AfterClass
    public static void close() {
        try {
            testBase.close();
        } catch (ManagedProcessException e) {
            e.printStackTrace();
        }
    }

    //@Test
    public void testInsert() {
        NetworkElementConnectionBuilder builder = new NetworkElementConnectionBuilder();
        builder.setId("ROADM-A");
        builder.setNodeId("ROADM-A");
        builder.setCoreModelCapability("2017-03-12");
        builder.setDeviceType(NetworkElementDeviceType.OROADM);
        builder.setHost("10.20.30.40");
        builder.setIsRequired(true);
        NodeDetails nodeDetails = new NodeDetailsBuilder().setAvailableCapabilities(Arrays.asList(
                "urn:ietf:params:netconf:capability:with-defaults:1.0?basic-mode=explicit&also-supported=report-all,"
                        + "report-all-tagged,trim,explicit",
                "urn:ietf:params:netconf:capability:validate:1.1", "urn:ietf:params:netconf:capability:url:1.0"))
                .build();
        builder.setNodeDetails(nodeDetails);
        builder.setPassword("password");
        builder.setPort(Uint32.valueOf(50000));
        builder.setStatus(ConnectionLogStatus.Connected);
        builder.setUsername("admin");
        try {
            writeEntry(builder.build(), Entity.NetworkelementConnection);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //@Test
    public void testRead() {
        List<NetworkElementConnection> con =
                readEntry(Entity.NetworkelementConnection, NetworkElementConnection.class, "ROADM-A");
        System.out.println(con);
    }

    private <T extends DataObject> List<T> readEntry(Entity entity, Class<T> clazz, String id) {
        final SelectQuery selectStatement = new SelectQuery(entity.getName());
        System.out.println(selectStatement);
        try {
            return MariaDBMapper.read(dbService.getData(selectStatement.toSql(), null, null), clazz);

        } catch (SQLException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | InstantiationException | SecurityException | NoSuchMethodException | JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }
        return null;
    }

    private <T extends DataObject> boolean writeEntry(T data, Entity entity) throws IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, JsonProcessingException {

        final InsertQuery<T> insertStatement = new InsertQuery<T>(entity, data, CONTROLLERID);
        System.out.println(insertStatement);
        try {
            return dbService.writeData(insertStatement.toSql(), null, null);

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }

        return false;
    }

    private static Properties getConfig() {
        Properties config = new Properties();
        config.setProperty("org.onap.ccsdk.sli.dbtype", "jdbc");
        config.setProperty("org.onap.ccsdk.sli.jdbc.hosts", MARIADB_HOST);
        config.setProperty("org.onap.ccsdk.sli.jdbc.url",
                String.format("jdbc:mysql://dbhost:%d/%s", MARIADB_PORT, MARIADB_DATABASENAME));
        config.setProperty("org.onap.ccsdk.sli.jdbc.driver", "org.mariadb.jdbc.Driver");
        config.setProperty("org.onap.ccsdk.sli.jdbc.database", MARIADB_DATABASENAME);
        config.setProperty("org.onap.ccsdk.sli.jdbc.user", MARIADB_USERNAME);
        config.setProperty("org.onap.ccsdk.sli.jdbc.password", MARIADB_PASSWORD);
        return config;
    }
}
