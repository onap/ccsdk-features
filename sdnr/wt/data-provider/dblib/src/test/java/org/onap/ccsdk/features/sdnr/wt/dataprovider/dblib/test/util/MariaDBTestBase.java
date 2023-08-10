/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2021 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Update Copyright (C) 2021 Samsung Electronics Intellectual Property. All rights reserved.
 * =================================================================================================
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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.dblib.test.util;

import static org.junit.Assert.fail;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.subtypes.Section;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.subtypes.Section.EnvGetter;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.SqlDBClient;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.SqlDBConfig;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data.SqlDBDataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data.Userdata;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.database.SqlDBMapper;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.database.SqlDBMapper.UnableToMapClassException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CmlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ConnectionlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Entity;
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
import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;

public class MariaDBTestBase {

    private final SqlDBDataProvider dbProvider;
    private final SqlDBDataProvider dbProviderOverall;
    private final DB db;
    private SqlDBConfig config;
    private static final Map<String, String> envDefaultValues = initEnvDefaultValues();
    private static final String SDNRDBDATABASETEST = "test";
    public static final String SUFFIX = "";

    public MariaDBTestBase() throws ManagedProcessException {
        this(new Random().nextInt(1000) + 50000);
    }

    private static String dbUrl(String host, int port, String dbName) {
        return String.format("jdbc:mysql://%s:%d/%s", host, port, dbName);
    }

    private static Map<String, String> initEnvDefaultValues() {
        Map<String, String> defaults = new HashMap<>();
        defaults.put("SDNRDBURL", dbUrl("localhost", 3306, SDNRDBDATABASETEST));
        defaults.put("SDNRDBDATABASE", "test");

        return defaults;
    }

    public MariaDBTestBase(String host, int port) {
        this(host, port, SDNRDBDATABASETEST);
    }

    public MariaDBTestBase(String host, int port, String dbName) {
        EnvGetter env = Section.getEnvGetter();
        envDefaultValues.put("SDNRDBURL", dbUrl(host, port, dbName));
        envDefaultValues.put("SDNRDBUSERNAME", "sdnrdb");
        envDefaultValues.put("SDNRDBPASSWORD", "sdnrdb");
        Section.setEnvGetter((envname) -> {
            return envDefaultValues.getOrDefault(envname, env.getenv(envname));
        });
        this.config = new SqlDBConfig(new ConfigurationFileRepresentation("test.properties"));
        this.config.setDbSuffix("");
        this.config.setControllerId("test123");
        this.db = null;
        this.dbProvider = new SqlDBDataProvider(config, false, "");

        SqlDBConfig config2 = new SqlDBConfig(new ConfigurationFileRepresentation("test2.properties"));
        config2.setDbSuffix("");
        config2.setControllerId(null);
        this.dbProviderOverall = new SqlDBDataProvider(config2, false, "");
    }

    public MariaDBTestBase(int port) throws ManagedProcessException {
        EnvGetter env = Section.getEnvGetter();
        envDefaultValues.put("SDNRDBURL", dbUrl("localhost", port, SDNRDBDATABASETEST));
        Section.setEnvGetter((envname) -> {
            return envDefaultValues.getOrDefault(envname, env.getenv(envname));
        });
        this.config = new SqlDBConfig(new ConfigurationFileRepresentation("test.properties"));
        this.config.setDbSuffix("");
        this.config.setControllerId("test123");
        //start db server
        this.db = startDatabase(port);
        //create db with name sdnrdb
        this.dbProvider = new SqlDBDataProvider(config, false, "");
        SqlDBConfig config2 = new SqlDBConfig(new ConfigurationFileRepresentation("test2.properties"));
        config2.setDbSuffix("");
        config2.setControllerId(null);
        this.dbProviderOverall = new SqlDBDataProvider(config2, false, "");
    }

    public void close() throws ManagedProcessException {
        if (db != null) {
            this.db.stop();
        }
        File f = new File("test.properties");
        if(f.exists()) {
            f.delete();
        }
    }

    public SqlDBDataProvider getDbProvider() {
        return dbProvider;
    }
    public SqlDBDataProvider getOverallDbProvider() {
        return dbProviderOverall;
    }

    public DB getDb() {
        return db;
    }

    private static DB startDatabase(int port) throws ManagedProcessException {
        // Start MariaDB4j database
        DBConfigurationBuilder dbconfig = DBConfigurationBuilder.newBuilder();
        dbconfig.setPort(port); // 0 => autom. detect free port
        DB db = DB.newEmbeddedDB(dbconfig.build());
        db.start();
        return db;
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

    public SqlDBClient createRawClient() {
        return new SqlDBClient(this.config.getUrl(), this.config.getUsername(), this.config.getPassword());
    }

    public String getDBName() {
        String url = this.getDBUrl();
        return url.substring(url.lastIndexOf("/") + 1);
    }

    public static void testCreateTableStructure(SqlDBClient dbService) {
        createTableOdl(dbService);
        createTable(dbService, ConnectionlogEntity.class, Entity.Connectionlog, true);
        createTable(dbService, EventlogEntity.class, Entity.Eventlog, true);
        createTable(dbService, FaultcurrentEntity.class, Entity.Faultcurrent, false);
        createTable(dbService, FaultlogEntity.class, Entity.Faultlog, true);
        createTable(dbService, GuicutthroughEntity.class, Entity.Guicutthrough, false);
        createTable(dbService, CmlogEntity.class, Entity.Cmlog, true);
        createTable(dbService, Pmdata15mEntity.class, Entity.Historicalperformance15min, false);
        createTable(dbService, Pmdata24hEntity.class, Entity.Historicalperformance24h, false);
        createTable(dbService, InventoryEntity.class, Entity.Inventoryequipment, false);
        createTable(dbService, MaintenanceEntity.class, Entity.Maintenancemode, false);
        createTable(dbService, MediatorServerEntity.class, Entity.MediatorServer, true);
        createTable(dbService, NetworkElementConnectionEntity.class, Entity.NetworkelementConnection, false);
        createTable(dbService, Userdata.class, Entity.Userdata, false, false);
    }

    public static boolean createTableOdl(SqlDBClient dbService) {
        String createStatement = null;
        createStatement = SqlDBMapper.createTableOdl();
        System.out.println(createStatement);
        return dbService.createTable(createStatement);
    }
    public static boolean createTable(SqlDBClient dbService, Class<?> cls, Entity entity, boolean autoIndex) {
        return createTable(dbService, cls, entity, autoIndex, true);
    }
    public static boolean createTable(SqlDBClient dbService, Class<?> cls, Entity entity, boolean autoIndex,
            boolean withControllerId) {
        String createStatement = null;
        try {
            createStatement = SqlDBMapper.createTable(cls, entity, SUFFIX, autoIndex,withControllerId);
        } catch (UnableToMapClassException e) {
            fail(e.getMessage());
        }
        System.out.println(createStatement);
        return dbService.createTable(createStatement);
    }
}

