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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.database.config.HostInfo;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.DataMigrationProviderImpl;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.DataMigrationReport;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.Release;

/**
 * @author Michael Dürre
 *
 */
public class TestMigrationProvider {

    private static final String FRANKFURT_BACKUP_FILE = "src/test/resources/test2.bak.json";
    public static HostInfo[] hosts = new HostInfo[] {new HostInfo("localhost", Integer
            .valueOf(System.getProperty("databaseport") != null ? System.getProperty("databaseport") : "49200"))};

    @Test
    public void testCreateImport() {
        DataMigrationProviderImpl provider = new DataMigrationProviderImpl(hosts, null, null, true, 5000);

        try {
            //create el alto db infrastructure
            provider.initDatabase(Release.FRANKFURT_R1, 5, 1, "", true, 10000);
            //import data into database
            DataMigrationReport report = provider.importData(FRANKFURT_BACKUP_FILE, false, Release.FRANKFURT_R1);
            assertTrue(report.completed());
            assertEquals(Release.FRANKFURT_R1, provider.autoDetectRelease());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
