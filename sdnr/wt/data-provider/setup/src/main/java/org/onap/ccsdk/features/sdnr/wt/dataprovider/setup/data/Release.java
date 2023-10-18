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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data;

import org.onap.ccsdk.features.sdnr.wt.common.database.data.AliasesEntry;
import org.onap.ccsdk.features.sdnr.wt.common.database.data.DatabaseVersion;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.SdnrDbType;


public enum Release {

    EL_ALTO("el alto", "_v1", new DatabaseVersion(2, 2, 0), new DatabaseVersion(2, 2, 0)),
    FRANKFURT_R1("frankfurt-R1", "-v2", new DatabaseVersion(6, 4, 3), new DatabaseVersion(6, 8, 6)),
    FRANKFURT_R2("frankfurt-R2", "-v3", new DatabaseVersion(7, 0, 1), new DatabaseVersion(7, 6, 1)),
    GUILIN_R1("guilin-R1", "-v4", new DatabaseVersion(7,1,1), new DatabaseVersion(7,6,1)),
	HONOLULU_R1("honolulu-R1", "-v5", new DatabaseVersion(7,1,1), new DatabaseVersion(8,0,0), false),
	ISTANBUL_R1("istanbul-R1", "-v6", new DatabaseVersion(7,1,1), new DatabaseVersion(8,0,0), false,
            new DatabaseVersion(10,2,7), new DatabaseVersion(10,6,0), false),
    JAKARTA_R1("jakarta-R1", "-v7", new DatabaseVersion(7,1,1), new DatabaseVersion(8,0,0), false,
            new DatabaseVersion(10,2,7), new DatabaseVersion(11,1,5), false);


    public static final Release CURRENT_RELEASE = Release.JAKARTA_R1;

    private final String value;
    private final String dbSuffix;
    private final DatabaseVersion minDbVersion;
    private final DatabaseVersion maxDbVersion;
    private final DatabaseVersion minMariaDbVersion;
    private final DatabaseVersion maxMariaDbVersion;
    private final boolean includeEndVersion;
    private final boolean mariaDbIncludeEndVersion;

    private Release(String s, String dbsuffix, DatabaseVersion minDbVersion, DatabaseVersion maxDbVersion) {
        this(s, dbsuffix, minDbVersion, maxDbVersion, true, null, null, false);
    }

    private Release(String s, String dbsuffix, DatabaseVersion minDbVersion, DatabaseVersion maxDbVersion,
            boolean includeEnd) {
        this(s, dbsuffix, minDbVersion, maxDbVersion, includeEnd, null, null, false);
    }

    private Release(String s, String dbsuffix, DatabaseVersion minDbVersion, DatabaseVersion maxDbVersion,
            boolean includeEnd, DatabaseVersion minMariaDbVersion, DatabaseVersion maxMariaDbVersion,
            boolean mariaDbIncludeEnd) {
        this.value = s;
        this.dbSuffix = dbsuffix;
        this.minDbVersion = minDbVersion;
        this.maxDbVersion = maxDbVersion;
        this.includeEndVersion = includeEnd;
        this.minMariaDbVersion = minMariaDbVersion;
        this.maxMariaDbVersion = maxMariaDbVersion;
        this.mariaDbIncludeEndVersion = mariaDbIncludeEnd;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public String getValue() {
        return value;
    }

    public static Release getValueOf(String s) throws Exception {
        //s = s.toLowerCase();
        for (Release p : Release.values()) {
            if (p.value.equals(s)) {
                return p;
            }
        }
        throw new Exception("value not found");
    }

    public static Release getValueBySuffix(String suffix) {
        if (!suffix.startsWith("-")) {
            suffix = "-" + suffix;
        }
        for (Release r : Release.values()) {
            if (r.dbSuffix.equals(suffix))
                return r;
        }
        return null;
    }

    public static String getDbSuffix(AliasesEntry entry) throws Exception {
        ComponentName comp = ComponentName.getValueOf(entry.getAlias());
        if (comp != null) {
            return entry.getIndex().substring(entry.getAlias().length());
        }
        return null;
    }

    public String getDBSuffix() {
        return this.dbSuffix;
    }

    public DatabaseVersion getDBVersion() {
        return this.minDbVersion;
    }

    public boolean isDbInRange(DatabaseVersion dbVersion, SdnrDbType type) {
        if (type == SdnrDbType.ELASTICSEARCH) {
            if (this.includeEndVersion) {
                return dbVersion.isNewerOrEqualThan(minDbVersion) && dbVersion.isOlderOrEqualThan(maxDbVersion);
            } else {
                return dbVersion.isNewerOrEqualThan(minDbVersion) && dbVersion.isOlderThan(maxDbVersion);
            }
        } else {
            if (this.mariaDbIncludeEndVersion) {
                return dbVersion.isNewerOrEqualThan(minMariaDbVersion)
                        && dbVersion.isOlderOrEqualThan(maxMariaDbVersion);
            } else {
                return dbVersion.isNewerOrEqualThan(minMariaDbVersion) && dbVersion.isOlderThan(maxMariaDbVersion);
            }
        }
    }
}
