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

import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.SdnrDbType;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.DataMigrationReport;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.Release;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.database.MariaDbDataMigrationProvider;

public class DataMigrationProviderImpl implements DataMigrationProviderService {

    private final DataMigrationProviderService dbProvider;

    public DataMigrationProviderImpl(SdnrDbType type, String url, String username, String password, boolean trustAll,
            long timeoutms) throws Exception {
        if (type == SdnrDbType.ELASTICSEARCH) {
            throw new RuntimeException("Elasticsearch no longer supported");
        } else {
            dbProvider = new MariaDbDataMigrationProvider(url, username, password, trustAll, timeoutms);
        }
    }

    @Override
    public DataMigrationReport importData(String filename, boolean dryrun) throws Exception {
        return this.dbProvider.importData(filename, dryrun, Release.CURRENT_RELEASE);
    }

    @Override
    public DataMigrationReport importData(String filename, boolean dryrun, Release forRelease) throws Exception {
        return this.dbProvider.importData(filename, dryrun, forRelease);
    }


    /**
     * export data if file exists .1 (.n) will be created
     *
     */
    @Override
    public DataMigrationReport exportData(String filename) {
        return this.dbProvider.exportData(filename);
    }

    @Override
    public Release getCurrentVersion() {
        return Release.CURRENT_RELEASE;
    }


    @Override
    public Release autoDetectRelease() {
        return this.dbProvider.autoDetectRelease();
    }

    @Override
    public boolean initDatabase(Release release, int numShards, int numReplicas, String dbPrefix, boolean forceRecreate,
            long timeoutms) {
        return this.dbProvider.initDatabase(release, numShards, numReplicas, dbPrefix, forceRecreate, timeoutms);
    }

    @Override
    public boolean clearDatabase(Release release, String dbPrefix, long timeoutms) {
        return this.dbProvider.clearDatabase(release, dbPrefix, timeoutms);
    }

    /**
     * @param timeoutms
     * @return
     */
    public boolean clearCompleteDatabase(long timeoutms) {
        return this.clearCompleteDatabase(timeoutms);
    }
}
