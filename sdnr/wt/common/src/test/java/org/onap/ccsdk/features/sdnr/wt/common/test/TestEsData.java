/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.common.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.ParseException;

import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.database.data.AliasesEntry;
import org.onap.ccsdk.features.sdnr.wt.common.database.data.AliasesEntryList;
import org.onap.ccsdk.features.sdnr.wt.common.database.data.EsVersion;
import org.onap.ccsdk.features.sdnr.wt.common.database.data.IndicesEntry;
import org.onap.ccsdk.features.sdnr.wt.common.database.data.IndicesEntryList;

/**
 * @author Michael Dürre
 *
 */
public class TestEsData {


    @Test
    public void testVersion() {
        EsVersion version = null;
        try {
            version = new EsVersion("2.3.4");
        } catch (ParseException e) {
            fail(e.getMessage());
        }
        assertNotNull(version);
        assertEquals(2, version.getMajor());
        assertEquals(3, version.getMinor());
        assertEquals(4, version.getRevision());

        EsVersion versionNewer = new EsVersion(5, 0, 0);
        EsVersion versionOlder = new EsVersion(2, 2, 0);

        assertTrue(version.isOlderOrEqualThan(versionNewer));
        assertTrue(version.isNewerOrEqualThan(versionOlder));

    }

    @Test
    public void testIndices() {
        IndicesEntryList list = new IndicesEntryList();
        IndicesEntry entry = null;
        try {
            entry = new IndicesEntry(
                    "yellow open inventoryequipment-v1         5nNPRbJ3T9arMxqxBbJKyQ 5 1 2 3 1.2kb 2.4kb");
            list.add(entry);
            list.add(new IndicesEntry(
                    "yellow open networkelement-connection-v1         5nNPRbJ3T9arMxqxBbJKyQ 5 1 0 0 1.2kb 1.2kb"));
            list.add(new IndicesEntry("yellow open faultlog-v1         5nNPRbJ3T9arMxqxBbJKyQ 5 1 0 0 1.2kb 1.2kb"));
            list.add(new IndicesEntry("yellow open eventlog-v1         5nNPRbJ3T9arMxqxBbJKyQ 5 1 0 0 1.2kb 1.2kb"));
        } catch (ParseException e) {
            fail(e.getMessage());
        }
        assertEquals(4, list.size());
        assertNotNull(list.findByIndex("eventlog-v1"));
        assertNull(list.findByIndex("faultcurrent"));
        assertNotNull(entry);
        assertEquals("yellow", entry.getStatus());
        assertEquals("open", entry.getStatus2());
        assertEquals("inventoryequipment-v1", entry.getName());
        assertEquals("5nNPRbJ3T9arMxqxBbJKyQ", entry.getHash());
        assertEquals(5, entry.getShards());
        assertEquals(1, entry.getReplicas());
        assertEquals(2, entry.getC1());
        assertEquals(3, entry.getC2());
        assertEquals("1.2kb", entry.getSize1());
        assertEquals("2.4kb", entry.getSize2());

    }

    @Test
    public void testAliases() {
        AliasesEntryList list = new AliasesEntryList();
        AliasesEntry entry = null;
        try {
            entry = new AliasesEntry("networkelement-connection  networkelement-connection-v1  - - -");
            list.add(entry);
            list.add(new AliasesEntry("faultcurrent               faultcurrent-v1               - - -"));
            list.add(new AliasesEntry("faultlog                   faultlog-v1                   - - -"));
            list.add(new AliasesEntry("maintenancemode            maintenancemode-v1            - - -"));
        } catch (ParseException e) {
            fail(e.getMessage());
        }
        assertEquals(4, list.size());
        assertNotNull(list.findByAlias("faultcurrent"));
        assertNull(list.findByAlias("eventlog"));
        assertNotNull(entry);
        assertEquals("networkelement-connection", entry.getAlias());
        assertEquals("networkelement-connection-v1", entry.getIndex());
    }
}
