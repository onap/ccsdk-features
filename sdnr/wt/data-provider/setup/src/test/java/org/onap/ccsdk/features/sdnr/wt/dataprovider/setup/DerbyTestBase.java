/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2021 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Update Copyright (C) 2021 Samsung Electronics Intellectual Property. All rights reserved.
 * ================================================================================================
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

import static org.junit.Assert.fail;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.subtypes.Section;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.subtypes.Section.EnvGetter;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.SqlDBClient;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.SqlDBConfig;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data.SqlDBDataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.database.SqlDBMapper;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.database.SqlDBMapper.UnableToMapClassException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CmlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Entity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ConnectionlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EventlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultcurrentEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.GuicutthroughEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.InventoryEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.MaintenanceEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.MediatorServerEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementConnectionEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Pmdata15mEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Pmdata24hEntity;

public class DerbyTestBase {

    private final SqlDBDataProvider dbProvider;
    private static final Map<String, String> envDefaultValues = initEnvDefaultValues();
    private static final String SDNRDBDATABASETEST = "test";
    private static final String TESTPROPFILE = "test.properties";
    private static final String SUFFIX = "";
    private static int dbCounter = 0;

    public DerbyTestBase() throws SQLException {
        this("testdb" + (dbCounter++));
    }

    private static String dbUrl(String dbName) {
        return String.format("jdbc:derby:memory:%s;create=true", dbName);
    }

    private static Map<String, String> initEnvDefaultValues() {
        Map<String, String> defaults = new HashMap<>();
        defaults.put("SDNRDBURL", dbUrl(SDNRDBDATABASETEST));
        defaults.put("SDNRDBDATABASE", "test");
        return defaults;
    }

    public DerbyTestBase(String dbName) throws SQLException {
        EnvGetter env = Section.getEnvGetter();
        envDefaultValues.put("SDNRDBURL", dbUrl(dbName));
        envDefaultValues.put("SDNRDBUSERNAME", "");
        envDefaultValues.put("SDNRDBPASSWORD", "");
        Section.setEnvGetter((envname) -> {
            return envDefaultValues.getOrDefault(envname, env.getenv(envname));
        });
        SqlDBConfig config = new SqlDBConfig(new ConfigurationFileRepresentation(TESTPROPFILE));
        config.setControllerId("test123");
        this.dbProvider = new SqlDBDataProvider(config, false, "");
    }

    public void close() {
        File f = new File(TESTPROPFILE);
        if (f.exists()) {
            f.delete();
        }
    }

    public SqlDBDataProvider getDbProvider() {
        return dbProvider;
    }

    public String getDBUrl() {
        return envDefaultValues.get("SDNRDBURL");
    }

    public String getDBUsername() {
        return envDefaultValues.getOrDefault("SDNRDBUSERNAME", "");
    }

    public String getDBPassword() {
        return envDefaultValues.getOrDefault("SDNRDBPASSWORD", "");
    }

    public static void testCreateTable(SqlDBClient dbService) {
        createTableOdl(dbService);
        createTable(dbService, ConnectionlogEntity.class, Entity.Connectionlog, true);
        createTable(dbService, EventlogEntity.class, Entity.Eventlog, true);
        createTable(dbService, FaultcurrentEntity.class, Entity.Faultcurrent, false);
        createTable(dbService, FaultlogEntity.class, Entity.Faultlog, true);
        createTable(dbService, CmlogEntity.class, Entity.Cmlog, true);
        createTable(dbService, GuicutthroughEntity.class, Entity.Guicutthrough, true);
        createTable(dbService, Pmdata15mEntity.class, Entity.Historicalperformance15min, false);
        createTable(dbService, Pmdata24hEntity.class, Entity.Historicalperformance24h, false);
        createTable(dbService, InventoryEntity.class, Entity.Inventoryequipment, false);
        createTable(dbService, MaintenanceEntity.class, Entity.Maintenancemode, false);
        createTable(dbService, MediatorServerEntity.class, Entity.MediatorServer, true);
        createTable(dbService, NetworkElementConnectionEntity.class, Entity.NetworkelementConnection, false);
    }

    public static boolean createTableOdl(SqlDBClient dbService) {
        String createStatement = null;
        createStatement = SqlDBMapper.createTableOdl();
        System.out.println(createStatement);
        try {
            return dbService.write(createStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean createTable(SqlDBClient dbService, Class<?> cls, Entity entity, boolean autoIndex) {
        return createTable(dbService, cls, entity, autoIndex, true);
    }

    public static boolean createTable(SqlDBClient dbService, Class<?> cls, Entity entity, boolean autoIndex,
            boolean withControllerId) {
        String createStatement = null;
        try {
            createStatement = SqlDBMapper.createTable(cls, entity, SUFFIX, autoIndex, withControllerId);
        } catch (UnableToMapClassException e) {
            fail(e.getMessage());
        }
        System.out.println(createStatement);
        return dbService.createTable(createStatement);
    }
}
