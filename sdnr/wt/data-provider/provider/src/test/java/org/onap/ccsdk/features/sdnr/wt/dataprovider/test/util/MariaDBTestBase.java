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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.test.util;

import static org.junit.Assert.fail;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.subtypes.Section;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.subtypes.Section.EnvGetter;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.SqlDBClient;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.SqlDBConfig;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data.SqlDBDataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.database.SqlDBMapper;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.database.SqlDBMapper.UnableToMapClassException;
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
    private final DB db;
    private static final Map<String, String> envDefaultValues = initEnvDefaultValues();

    public MariaDBTestBase() throws ManagedProcessException {
        this(new Random().nextInt(1000) + 50000);
    }

    private static Map<String, String> initEnvDefaultValues() {
        Map<String, String> defaults = new HashMap<>();
        defaults.put("SDNRDBHOST", "localhost");
        defaults.put("SDNRDBDATABASE", "test");

        return defaults;
    }

    public MariaDBTestBase(String host, int port) {
        EnvGetter env = Section.getEnvGetter();
        envDefaultValues.put("SDNRDBPORT", String.valueOf(port));
        envDefaultValues.put("SDNRDBHOST", host);
        envDefaultValues.put("SDNRDBDATABASE", "sdnrdb");
        envDefaultValues.put("SDNRDBUSERNAME", "sdnrdb");
        envDefaultValues.put("SDNRDBPASSWORD", "sdnrdb");
        Section.setEnvGetter((envname) -> {
            return envDefaultValues.getOrDefault(envname, env.getenv(envname));
        });
        SqlDBConfig config = new SqlDBConfig(new ConfigurationFileRepresentation("test.properties"));
        this.db = null;
        this.dbProvider = new SqlDBDataProvider(config);
        testCreateTable(this.dbProvider.getDBService());
    }

    public MariaDBTestBase(int port) throws ManagedProcessException {
        EnvGetter env = Section.getEnvGetter();
        envDefaultValues.put("SDNRDBPORT", String.valueOf(port));
        Section.setEnvGetter((envname) -> {
            return envDefaultValues.getOrDefault(envname, env.getenv(envname));
        });
        SqlDBConfig config = new SqlDBConfig(new ConfigurationFileRepresentation("test.properties"));
        //start db server
        this.db = startDatabase(port);
        //create db with name sdnrdb
        this.dbProvider = new SqlDBDataProvider(config);
        testCreateTable(this.dbProvider.getDBService());
    }

    public void close() throws ManagedProcessException {
        if (db != null) {
            this.db.stop();
        }

    }

    public SqlDBDataProvider getDbProvider() {
        return dbProvider;
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
    public static void testCreateTable(SqlDBClient dbService) {
        createTableOdl(dbService);
        createTable(dbService, ConnectionlogEntity.class, Entity.Connectionlog, true);
        createTable(dbService, EventlogEntity.class, Entity.Eventlog, true);
        createTable(dbService, FaultcurrentEntity.class, Entity.Faultcurrent, false);
        createTable(dbService, FaultlogEntity.class, Entity.Faultlog, true);
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
            // TODO Auto-generated catch block
            e.printStackTrace();

        }
        return false;
    }

    public static boolean createTable(SqlDBClient dbService, Class<?> cls, Entity entity, boolean autoIndex) {
        String createStatement = null;
        try {
            createStatement = SqlDBMapper.createTable(cls, entity, "", autoIndex);
        } catch (UnableToMapClassException e) {
            fail(e.getMessage());
        }
        System.out.println(createStatement);
        try {
            return dbService.write(createStatement);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }
        return false;
    }
}
