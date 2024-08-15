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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.json.JSONObject;
import org.onap.ccsdk.features.sdnr.wt.common.database.HtDatabaseClient;
import org.onap.ccsdk.features.sdnr.wt.common.database.SearchHit;
import org.onap.ccsdk.features.sdnr.wt.common.database.SearchResult;
import org.onap.ccsdk.features.sdnr.wt.common.database.config.HostInfo;
import org.onap.ccsdk.features.sdnr.wt.common.database.data.AliasesEntry;
import org.onap.ccsdk.features.sdnr.wt.common.database.data.AliasesEntryList;
import org.onap.ccsdk.features.sdnr.wt.common.database.data.DatabaseVersion;
import org.onap.ccsdk.features.sdnr.wt.common.database.data.IndicesEntry;
import org.onap.ccsdk.features.sdnr.wt.common.database.data.IndicesEntryList;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.CreateAliasRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.CreateIndexRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.DeleteAliasRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.requests.DeleteIndexRequest;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.AcknowledgedResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.GetInfoResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.ListAliasesResponse;
import org.onap.ccsdk.features.sdnr.wt.common.database.responses.ListIndicesResponse;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.SdnrDbType;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.DataMigrationProviderService;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.ReleaseInformation;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.ComponentData;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.ComponentName;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.DataContainer;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.DataMigrationReport;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.Release;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.ReleaseGroup;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.SearchHitConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class ElasticsearchDataMigrationProvider implements DataMigrationProviderService {


    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchDataMigrationProvider.class);
    private static final String LOG_DELETING_INDEX = "deleting index {}";
    private final HtDatabaseClient dbClient;

    public ElasticsearchDataMigrationProvider(String url, String username, String password, boolean trustAll,
            long timeoutms) throws Exception {
        dbClient = HtDatabaseClient.getClient(new HostInfo[] {HostInfo.parse(url)}, username, password, trustAll, true,
                timeoutms);
    }

    @Override
    public DataMigrationReport importData(String filename, boolean dryrun) throws Exception {
        return this.importData(filename, dryrun, Release.CURRENT_RELEASE);
    }

    @Override
    public DataMigrationReport importData(String filename, boolean dryrun, Release forRelease) throws Exception {
        DataMigrationReport report = new DataMigrationReport();
        File file = new File(filename);
        if (!file.exists()) {
            if (dryrun) {
                report.error("file %s not found", filename);
                return report;
            }
            throw new FileNotFoundException(filename);
        }
        DataContainer container = null;
        try {
            container = DataContainer.load(file);
        } catch (Exception e) {
            if (dryrun) {
                report.error("problem loading file %s: %s", filename, e.getMessage());
                return report;
            }
            throw new Exception("problem loading file " + filename, e);
        }
        ReleaseInformation ri = ReleaseInformation.getInstance(forRelease);
        SearchHitConverter converter;
        Set<ComponentName> components = ri.getComponents();
        //for all db components of dest architecture
        for (ComponentName component : components) {
            //convert to ComponentData for current release with existing ComponentData of the container
            converter = SearchHitConverter.Factory.getInstance(container.getRelease(), forRelease, component);
            if (converter == null) {
                continue;
            }
            ComponentData data = converter.convert(container);
            if (data != null) {
                String indexName = ri.getAlias(component);
                String dataTypeName = ri.getDataType(component);
                if (dryrun) {
                    report.log("write %d entries into %s/%s", data.size(), indexName, dataTypeName);
                } else {
                    LOG.debug("write {} entries into {}/{}", data.size(), indexName, dataTypeName);
                }
                for (SearchHit item : data) {
                    if (!dryrun) {
                        String id = this.dbClient.doWriteRaw(indexName, dataTypeName, item.getId(),
                                item.getSourceAsString(), true);
                        if (!item.getId().equals(id)) {
                            LOG.warn("entry for {} with original id {} was written with another id {}",
                                    component.getValue(), item.getId(), id);
                        }
                    }
                }
            } else {
                if (dryrun) {
                    report.error("unable to convert data for " + component.getValue() + " from version "
                            + container.getRelease().getValue() + " to " + forRelease.getValue() + "\n");
                } else {
                    LOG.warn("unable to convert data for {} from version {} to {}", component.getValue(),
                            container.getRelease().getValue(), forRelease.getValue());
                }
            }
        }
        LOG.info("import of {} completed", filename);
        if (dryrun) {
            report.log("import of %s completed", filename);
        }
        report.setCompleted(true);
        return report;
    }


    /**
     * export data if file exists .1 (.n) will be created
     *
     */
    @Override
    public DataMigrationReport exportData(String filename) {
        DataMigrationReport report = new DataMigrationReport();

        DataContainer container = new DataContainer();

        filename = this.checkFilenameForWrite(filename);
        LOG.info("output will be written to {}", filename);
        //autodetect version
        Release dbRelease = this.autoDetectRelease();
        if (dbRelease == null) {
            report.error("unbable to detect db release. is database initialized?");
            return report;
        }
        ReleaseInformation ri = ReleaseInformation.getInstance(dbRelease);
        boolean componentsSucceeded = true;
        for (ComponentName c : ri.getComponents()) {
            ComponentData data = new ComponentData(c);
            SearchResult<SearchHit> result = this.dbClient.doReadAllJsonData(ri.getAlias(c), ri.getDataType(c), false);
            data.addAll(result.getHits());
            container.addComponent(c, data);
        }
        try {
            Files.write(new File(filename).toPath(), Arrays.asList(container.toJSON()), StandardCharsets.UTF_8);
            report.setCompleted(componentsSucceeded);
        } catch (IOException e) {
            LOG.warn("problem writing data to {}: {}", filename, e);
        }
        return report;
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
        DatabaseVersion dbVersion = this.readActualVersion();
        AliasesEntryList aliases = this.readAliases();
        IndicesEntryList indices = this.readIndices();
        if (indices == null) {
            return null;
        }
        List<Release> foundReleases = new ArrayList<>();
        //if there are active aliases reduce indices to the active ones
        if (aliases != null && !aliases.isEmpty()) {
            indices = indices.subList(aliases.getLinkedIndices());
        }
        for (Release r : Release.values()) {
            if (r.isDbInRange(dbVersion, SdnrDbType.ELASTICSEARCH)) {
                ReleaseInformation ri = ReleaseInformation.getInstance(r);
                if (ri != null && ri.containsIndices(indices)) {
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

    private DatabaseVersion readActualVersion() {
        try {
            GetInfoResponse response = this.dbClient.getInfo();
            return response.getVersion();
        } catch (Exception e) {
            LOG.warn(e.getMessage());
        }
        return null;
    }

    private AliasesEntryList readAliases() {
        AliasesEntryList entries = null;
        try {
            ListAliasesResponse response = this.dbClient.getAliases();
            entries = response.getEntries();
        } catch (ParseException | IOException e) {
            LOG.error(e.getMessage());
        }
        return entries;
    }

    private IndicesEntryList readIndices() {
        IndicesEntryList entries = null;
        try {
            ListIndicesResponse response = this.dbClient.getIndices();
            entries = response.getEntries();
        } catch (ParseException | IOException e) {
            LOG.error(e.getMessage());
        }
        return entries;
    }


    @Override
    public boolean initDatabase(Release release, int numShards, int numReplicas, String dbPrefix, boolean forceRecreate,
            long timeoutms) {
        if (timeoutms > 0) {
            this.dbClient.waitForYellowStatus(timeoutms);
        }
        DatabaseVersion dbVersion = this.readActualVersion();
        if (dbVersion == null) {
            return false;
        }
        LOG.info("detected database version {}", dbVersion);
        if (release == null) {
            release = ReleaseGroup.CURRENT_RELEASE.getLatestCompatibleRelease(dbVersion, SdnrDbType.ELASTICSEARCH);
            if (release == null) {
                LOG.warn("unable to autodetect release for this database version for release {}",
                        ReleaseGroup.CURRENT_RELEASE.name());
                return false;
            }
            LOG.info("autodetect release {}", release);
        }
        if (!release.isDbInRange(dbVersion, SdnrDbType.ELASTICSEARCH)) {
            LOG.warn("db version {} maybe not compatible with release {}", dbVersion, release);
            return false;
        }
        if (forceRecreate) {
            this.clearDatabase(release, dbPrefix, 0);
        }
        ReleaseInformation ri = ReleaseInformation.getInstance(release);
        AliasesEntryList aliases = this.readAliases();
        IndicesEntryList indices = this.readIndices();
        if (aliases == null || indices == null) {
            return false;
        }
        AcknowledgedResponse response = null;
        if (!ri.runPreInitCommands(this.dbClient)) {
            return false;
        }
        for (ComponentName component : ri.getComponents()) {
            try {
                if (ri.hasOwnDbIndex(component)) {
                    //check if index already exists
                    String indexName = ri.getIndex(component, dbPrefix);
                    String aliasName = ri.getAlias(component, dbPrefix);
                    if (indices.findByIndex(indexName) == null) {
                        LOG.info("creating index for {}", component);
                        CreateIndexRequest request = new CreateIndexRequest(ri.getIndex(component, dbPrefix));
                        request.mappings(new JSONObject(ri.getDatabaseMapping(component)));
                        request.settings(new JSONObject(ri.getDatabaseSettings(component, numShards, numReplicas)));
                        response = this.dbClient.createIndex(request);
                        LOG.info(response.isAcknowledged() ? "succeeded" : "failed");
                    } else {
                        LOG.info("index {} for {} already exists", indexName, component);
                    }
                    //check if alias already exists
                    if (aliases.findByAlias(aliasName) == null) {
                        LOG.info("creating alias for {}", component);
                        response = this.dbClient.createAlias(new CreateAliasRequest(indexName, aliasName));
                        LOG.info(response.isAcknowledged() ? "succeeded" : "failed");
                    } else {
                        LOG.info("alias {} for index {} for {} already exists", aliasName, indexName, component);
                    }
                }
            } catch (IOException e) {
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
        AliasesEntryList entries = this.readAliases();
        IndicesEntryList entries2 = this.readIndices();
        if (entries == null) {
            return false;
        }
        if (release == null) {
            DatabaseVersion dbVersion = this.readActualVersion();
            if (dbVersion == null) {
                return false;
            }
            LOG.info("detected database version {}", dbVersion);
            release = ReleaseGroup.CURRENT_RELEASE.getLatestCompatibleRelease(dbVersion, SdnrDbType.ELASTICSEARCH);
            if (release == null) {
                LOG.warn("unable to autodetect release for this database version for release {}",
                        ReleaseGroup.CURRENT_RELEASE.name());
                return false;
            }
            LOG.info("autodetect release {}", release);
        }
        ReleaseInformation ri = ReleaseInformation.getInstance(release);
        AcknowledgedResponse response;
        if (entries.isEmpty()) {
            LOG.info("no aliases to clear");
        } else {
            //check for every component of release if alias exists
            for (ComponentName component : ri.getComponents()) {
                String aliasToDelete = ri.getAlias(component, dbPrefix);
                AliasesEntry entryToDelete = entries.findByAlias(aliasToDelete);
                if (entryToDelete != null) {
                    try {
                        LOG.info("deleting alias {} for index {}", entryToDelete.getAlias(), entryToDelete.getIndex());
                        response = this.dbClient.deleteAlias(
                                new DeleteAliasRequest(entryToDelete.getIndex(), entryToDelete.getAlias()));
                        LOG.info(response.isResponseSucceeded() ? "succeeded" : "failed");
                    } catch (IOException e) {
                        LOG.error(e.getMessage());
                        return false;
                    }
                } else {
                    //try to find malformed typed index with alias name
                    IndicesEntry entry2ToDelete = entries2 == null ? null : entries2.findByIndex(aliasToDelete);
                    if (entry2ToDelete != null) {
                        try {
                            LOG.info(LOG_DELETING_INDEX, entry2ToDelete.getName());
                            response = this.dbClient.deleteIndex(new DeleteIndexRequest(entry2ToDelete.getName()));
                            LOG.info(response.isResponseSucceeded() ? "succeeded" : "failed");
                        } catch (IOException e) {
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
                IndicesEntry entryToDelete = entries2.findByIndex(indexToDelete);
                if (entryToDelete != null) {
                    try {
                        LOG.info(LOG_DELETING_INDEX, entryToDelete.getName());
                        response = this.dbClient.deleteIndex(new DeleteIndexRequest(entryToDelete.getName()));
                        LOG.info(response.isResponseSucceeded() ? "succeeded" : "failed");
                    } catch (IOException e) {
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
        AliasesEntryList aliases = this.readAliases();
        IndicesEntryList indices = this.readIndices();
        if (aliases == null || indices == null) {
            return false;
        }
        for (AliasesEntry alias : aliases) {
            try {
                LOG.info("deleting alias {} for index {}", alias.getAlias(), alias.getIndex());
                this.dbClient.deleteAlias(new DeleteAliasRequest(alias.getIndex(), alias.getAlias()));
            } catch (IOException e) {
                LOG.error("problem deleting alias {}: {}", alias.getAlias(), e);
                return false;
            }
        }
        for (IndicesEntry index : indices) {
            try {
                LOG.info(LOG_DELETING_INDEX, index.getName());
                this.dbClient.deleteIndex(new DeleteIndexRequest(index.getName()));
            } catch (IOException e) {
                LOG.error("problem deleting index {}: {}", index.getName(), e);
                return false;
            }
        }
        return true;
    }

}
