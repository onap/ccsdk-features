/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2025 highstreet technologies GmbH Intellectual Property.
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
 */
package org.onap.ccsdk.features.sdnr.wt.common.test;

import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.database.data.DatabaseVersion;

public class TestDatabaseVersion {

    @Test
    public void test() throws ParseException {
        var v1 = new DatabaseVersion("1.0.0");
        assertOlder(1, 0, 0, 1, 0, 1);
        assertOlder(1, 0, 0, 1, 1, 1);
        assertOlder(1, 0, 0, 2, 0, 1);
        assertNewer(1, 0, 0, 0, 0, 1);
        assertNewer(1, 0, 0, 0, 1, 1);
        assertNewer(1, 0, 2, 1, 0, 1);
        assertNewerOrEqual(1, 0, 0, 1, 0, 0);
    }

    static void assertOlder(int major1, int minor1, int rev1, int major2, int minor2, int rev2) {
        assertTrue(new DatabaseVersion(major1, minor1, rev1).isOlderThan(
                new DatabaseVersion(major2, minor2, rev2)));
    }

    static void assertNewer(int major1, int minor1, int rev1, int major2, int minor2, int rev2) {
        assertTrue(new DatabaseVersion(major1, minor1, rev1).isNewerThan(
                new DatabaseVersion(major2, minor2, rev2)));
    }

    static void assertOlderOrEqual(int major1, int minor1, int rev1, int major2, int minor2, int rev2) {
        assertTrue(new DatabaseVersion(major1, minor1, rev1).isOlderOrEqualThan(
                new DatabaseVersion(major2, minor2, rev2)));
    }

    static void assertNewerOrEqual(int major1, int minor1, int rev1, int major2, int minor2, int rev2) {
        assertTrue(new DatabaseVersion(major1, minor1, rev1).isNewerOrEqualThan(
                new DatabaseVersion(major2, minor2, rev2)));
    }
}
