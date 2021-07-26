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

import java.io.FileNotFoundException;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.DataMigrationReport;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.Release;


public interface DataMigrationProviderService {

    /**
     * import data from file and write these to database
     *
     * @param filename source
     * @param dryrun only loading file and check consistency, not pushing into database
     * @return report
     * @throws FileNotFoundException
     * @throws Exception
     */
    DataMigrationReport importData(String filename, boolean dryrun) throws FileNotFoundException, Exception;

    DataMigrationReport importData(String filename, boolean dryrun, Release forRelease) throws Exception;
    /**
     * export current data to file
     *
     * @param filename
     */
    DataMigrationReport exportData(String filename);

    /**
     *
     * @return
     */
    Release getCurrentVersion();

    Release autoDetectRelease();

    /**
     * @param release
     * @param numShards
     * @param numReplicas
     * @param dbPrefix
     * @param forceRecreate
     * @param timeoutms
     * @return
     */
    boolean initDatabase(Release release, int numShards, int numReplicas, String dbPrefix, boolean forceRecreate,
            long timeoutms);

    /**
     * clean up the database all data will be removed complete structure will be destroyed
     *
     * @return
     */
    boolean clearDatabase(Release release, String dbPrefix, long timeoutms);

}
