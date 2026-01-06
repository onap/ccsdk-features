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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.database;

import java.io.File;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.onap.ccsdk.features.sdnr.wt.common.database.data.DatabaseVersion;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.SqlDBClient;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.SdnrDbType;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.DataMigrationProviderService;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.ReleaseInformation;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.ComponentName;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.DataMigrationReport;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.Release;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.ReleaseGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MariaDbDataMigrationProvider implements DataMigrationProviderService {


    private static final Logger LOG = LoggerFactory.getLogger(MariaDbDataMigrationProvider.class);
    private static final SdnrDbType DBTYPE = SdnrDbType.MARIADB;
    private static final String LOG_DELETING_INDEX = "deleting index {}";
    private final SqlDBClient dbClient;

    public MariaDbDataMigrationProvider(String url, String username, String password, boolean trustAll,
            long timeoutms) throws Exception {
        dbClient = new SqlDBClient(url, username, password);
    }

    @Override
    public DataMigrationReport importData(String filename, boolean dryrun) throws Exception {
        return this.importData(filename, dryrun, Release.CURRENT_RELEASE);
    }

    @Override
    public DataMigrationReport importData(String filename, boolean dryrun, Release forRelease) throws Exception {
        throw new RuntimeException("not supported anymore");
    }


    /**
     * export data if file exists .1 (.n) will be created
     */
    @Override
    public DataMigrationReport exportData(String filename) {
        throw new RuntimeException("not supported anymore");
    }

    private String checkFilenameForWrite(String filename) {
        File f = new File(filename);
        if (!f.exists()) {
            return filename;
        }
        return this.checkFilenameForWrite(filename, 0);
    }

    private String checkFilenameForWrite(String filename, int apdx) {
        File f = new File(String.format("$s.$d", filename, apdx));
        if (!f.exists()) {
            return filename;
        }
        return this.checkFilenameForWrite(filename, apdx + 1);
    }

    @Override
    public Release getCurrentVersion() {
        return Release.CURRENT_RELEASE;
    }


    @Override
    public Release autoDetectRelease() {
        DatabaseVersion dbVersion;
        try {
            dbVersion = this.dbClient.readActualVersion();
        } catch (SQLException | ParseException e) {
            LOG.error("unable to detect db version", e);
            return null;
        }
        var views = this.dbClient.readViews();
        var tables = this.dbClient.readTables();
        if (tables == null) {
            return null;
        }
        List<Release> foundReleases = new ArrayList<>();
        //if there are active aliases reduce indices to the active ones
        if (views != null && !views.isEmpty()) {
            tables = tables.stream()
                    .filter(e -> views.stream().anyMatch(v -> v.getTableReference().equals(e.getName())))
                    .collect(Collectors.toUnmodifiableList());
        }
        for (Release r : Release.values()) {
            if (r.isDbInRange(dbVersion)) {
                ReleaseInformation ri = ReleaseInformation.getInstance(r);
                if (ri != null && ri.containsIndices(tables)) {
                    foundReleases.add(r);
                }
            }
        }
        if (foundReleases.size() == 1) {
            return foundReleases.get(0);
        }
        LOG.error("detect {} releases: {}. unable to detect for which one to do sth.", foundReleases.size(),
                foundReleases);
        return null;
    }

    @Override
    public boolean initDatabase(Release release, int numShards, int numReplicas, String dbPrefix, boolean forceRecreate,
            long timeoutms) {
        if (timeoutms > 0) {
            this.dbClient.waitForYellowStatus(timeoutms);
        }
        DatabaseVersion dbVersion;
        try {
            dbVersion = this.dbClient.readActualVersion();
        } catch (SQLException | ParseException e1) {
            LOG.error("unable to detect db version", e1);
            return false;
        }
        if (dbVersion == null) {
            return false;
        }
        LOG.info("detected database version {}", dbVersion);
        if (release == null) {
            release = Release.CURRENT_RELEASE;
        }
        if (!release.isDbInRange(dbVersion)) {
            LOG.warn("db version {} maybe not compatible with release {}", dbVersion, release);
        }
        if (forceRecreate) {
            this.clearDatabase(release, dbPrefix, 0);
        }
        ReleaseInformation ri = ReleaseInformation.getInstance(release);
        var views = this.dbClient.readViews();
        var tables = this.dbClient.readTables();
        if (views == null || tables == null) {
            return false;
        }
        boolean response = false;
        if (!ri.runPreInitCommands(this.dbClient)) {
            return false;
        }
        for (ComponentName component : ri.getComponents()) {
            try {
                if (ri.hasOwnDbIndex(component)) {
                    //check if index already exists
                    String tableName = ri.getIndex(component, dbPrefix);
                    String viewName = ri.getAlias(component, dbPrefix);
                    if (!tables.stream().anyMatch(e->e.getName().equals(tableName))) {
                        LOG.info("creating index for {}", component);
                        response = this.dbClient.createTable(ri.getIndex(component, dbPrefix),
                                ri.getDatabaseMapping(component, DBTYPE));
                        LOG.info(response ? "succeeded" : "failed");
                    } else {
                        LOG.info("index {} for {} already exists", tableName, component);
                    }
                    //check if alias already exists
                    if (!views.stream().anyMatch(e->e.getName().equals(viewName))) {
                        LOG.info("creating alias for {}", component);
                        response = this.dbClient.createView(tableName, viewName);
                        LOG.info(response ? "succeeded" : "failed");
                    } else {
                        LOG.info("view {} for table {} for {} already exists", viewName, tableName, component);
                    }
                }
            } catch (SQLException e) {
                LOG.error(e.getMessage());
                return false;
            }
        }
        if (!ri.runPostInitCommands(this.dbClient)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean clearDatabase(Release release, String dbPrefix, long timeoutms) {

        if (timeoutms > 0) {
            this.dbClient.waitForYellowStatus(timeoutms);
        }
        //check aliases
        var entries = this.dbClient.readViews();
        var entries2 = this.dbClient.readTables();
        if (entries == null) {
            return false;
        }
        if (release == null) {
            DatabaseVersion dbVersion;
            try {
                dbVersion = this.dbClient.readActualVersion();
            } catch (SQLException | ParseException e) {
                LOG.error("unable to detect db version", e);
                return false;
            }
            LOG.info("detected database version {}", dbVersion);
            release = ReleaseGroup.CURRENT_RELEASE.getLatestCompatibleRelease(dbVersion);
            if (release == null) {
                LOG.warn("unable to autodetect release for this database version for release {}",
                        ReleaseGroup.CURRENT_RELEASE.name());
                return false;
            }
            LOG.info("autodetect release {}", release);
        }
        ReleaseInformation ri = ReleaseInformation.getInstance(release);
        boolean response;
        if (entries.isEmpty()) {
            LOG.info("no aliases to clear");
        } else {
            //check for every component of release if alias exists
            for (ComponentName component : ri.getComponents()) {
                String aliasToDelete = ri.getAlias(component, dbPrefix);
                var entryToDelete = entries.stream().filter(e->e.getName().equals(aliasToDelete)).findFirst().orElse(null);
                if (entryToDelete != null) {
                    try {
                        LOG.info("deleting alias {} for index {}", entryToDelete.getName(), entryToDelete.getTableReference());
                        response = this.dbClient.deleteView(entryToDelete.getName());
                        LOG.info(response ? "succeeded" : "failed");
                    } catch (SQLException e) {
                        LOG.error(e.getMessage());
                        return false;
                    }
                } else {
                    //try to find malformed typed index with alias name
                    var entry2ToDelete = entries2.stream().filter(e->e.getName().equals(aliasToDelete)).findFirst().orElse(null);
                    if (entry2ToDelete != null) {
                        try {
                            LOG.info(LOG_DELETING_INDEX, entry2ToDelete.getName());
                            response = this.dbClient.deleteTable(entry2ToDelete.getName());
                            LOG.info(response ? "succeeded" : "failed");
                        } catch (SQLException e) {
                            LOG.error(e.getMessage());
                            return false;
                        }
                    }
                }
            }
        }
        if (entries2 == null) {
            return false;
        }
        if (entries2.isEmpty()) {
            LOG.info("no indices to clear");
        } else {
            //check for every component of release if index exists
            for (ComponentName component : ri.getComponents()) {
                String indexToDelete = ri.getIndex(component, dbPrefix);
                var entryToDelete = entries2.stream().filter(e->e.getName().equals(indexToDelete)).findFirst().orElse(null);
                if (entryToDelete != null) {
                    try {
                        LOG.info(LOG_DELETING_INDEX, entryToDelete.getName());
                        response = this.dbClient.deleteTable(entryToDelete.getName());
                        LOG.info(response ? "succeeded" : "failed");
                    } catch (SQLException e) {
                        LOG.error(e.getMessage());
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * @param timeoutms
     * @return
     */
    public boolean clearCompleteDatabase(long timeoutms) {
        if (timeoutms > 0) {
            this.dbClient.waitForYellowStatus(timeoutms);
        }
        //check aliases and indices
        var aliases = this.dbClient.readViews();
        var indices = this.dbClient.readTables();
        if (aliases == null || indices == null) {
            return false;
        }
        for (var alias : aliases) {
            try {
                LOG.info("deleting alias {} for index {}", alias.getName(), alias.getTableReference());
                this.dbClient.deleteView(alias.getName());
            } catch (SQLException e) {
                LOG.error("problem deleting alias {}: {}", alias.getName(), e);
                return false;
            }
        }
        for (var index : indices) {
            try {
                LOG.info(LOG_DELETING_INDEX, index.getName());
                this.dbClient.deleteTable(index.getName());
            } catch (SQLException e) {
                LOG.error("problem deleting index {}: {}", index.getName(), e);
                return false;
            }
        }
        return true;
    }

}
