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
import org.onap.ccsdk.features.sdnr.wt.common.database.data.EsVersion;

public enum Release {

    EL_ALTO("el alto", "_v1", new EsVersion(2, 2, 0), new EsVersion(2, 2, 0)),
    FRANKFURT_R1("frankfurt-R1", "-v2", new EsVersion(6, 4, 3), new EsVersion(6, 8, 6)),
    FRANKFURT_R2("frankfurt-R2", "-v3", new EsVersion(7, 0, 1), new EsVersion(7, 6, 1)),
    GUILIN_R1("guilin-R1", "-v4", new EsVersion(7,1,1), new EsVersion(7,6,1)),
	HONOLULU_R1("honolulu-R1", "-v5", new EsVersion(7,1,1), new EsVersion(8,0,0), false),
	ISTANBUL_R1("istanbul-R1", "-v6", new EsVersion(7,1,1), new EsVersion(8,0,0), false);

    public static final Release CURRENT_RELEASE = Release.ISTANBUL_R1;

    private final String value;
    private final String dbSuffix;
    private final EsVersion minDbVersion;
    private final EsVersion maxDbVersion;
    private final boolean includeEndVersion;

	private Release(String s, String dbsuffix, EsVersion minDbVersion, EsVersion maxDbVersion) {
		this(s, dbsuffix, minDbVersion, maxDbVersion, true);
	}
    private Release(String s, String dbsuffix, EsVersion minDbVersion, EsVersion maxDbVersion, boolean includeEnd) {
        this.value = s;
        this.dbSuffix = dbsuffix;
        this.minDbVersion = minDbVersion;
        this.maxDbVersion = maxDbVersion;
        this.includeEndVersion = includeEnd;
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

    public EsVersion getDBVersion() {
        return this.minDbVersion;
    }

    public boolean isDbInRange(EsVersion dbVersion) {
        if(this.includeEndVersion) {
            return dbVersion.isNewerOrEqualThan(minDbVersion) && dbVersion.isOlderOrEqualThan(maxDbVersion);
        }
        else {
            return dbVersion.isNewerOrEqualThan(minDbVersion) && dbVersion.isOlderThan(maxDbVersion);
        }
    }
}
