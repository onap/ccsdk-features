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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.SqlDBClient;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data.SqlTable;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.SdnrDbType;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.ComponentName;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.DatabaseInfo;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.MariaDBTableInfo;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.Release;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.releases.ElAltoReleaseInformation;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.releases.FrankfurtReleaseInformation;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.releases.FrankfurtReleaseInformationR2;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.releases.GuilinReleaseInformation;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.releases.HonoluluReleaseInformation;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.releases.IstanbulReleaseInformation;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.releases.JakartaReleaseInformation;

public abstract class ReleaseInformation {

    // variables
    private final Release release;
    private final Map<ComponentName, DatabaseInfo> dbMap;
    private Map<ComponentName, MariaDBTableInfo> mariadbMap;
    // end of variables

    // constructors
    public ReleaseInformation(Release r, Map<ComponentName, DatabaseInfo> dbMap) {
        this(r, dbMap, new HashMap<>());
    }

    public ReleaseInformation(Release r, Map<ComponentName, DatabaseInfo> dbMap,
            Map<ComponentName, MariaDBTableInfo> mariadbMap) {
        this.release = r;
        this.dbMap = dbMap;
        this.mariadbMap = mariadbMap;
    }
    // end of constructors

    protected Release getReleas() {
        return this.release;
    }

    /**
     * get database alias for component
     *
     * @param name
     * @return alias or null if not exists
     */
    public String getAlias(ComponentName name) {
        return this.getAlias(name, "");
    }

    public String getAlias(ComponentName name, String prefix) {
        return dbMap.get(name) == null ? null : prefix + dbMap.get(name).alias;
    }

    /**
     * get index name for component
     *
     * @param comp
     * @return null if component does not exists in this release, otherwise index name
     */
    public String getIndex(ComponentName comp) {
        return this.getIndex(comp, "");
    }

    /**
     * get index name for component with prefix
     *
     * @param comp
     * @param prefix
     * @return null if component does not exists in this release, otherwise index name
     */
    public String getIndex(ComponentName comp, String prefix) {
        return dbMap.get(comp) == null ? null : (prefix + dbMap.get(comp).getIndex(this.release.getDbSuffix()));
    }

    /**
     * get database datatype (doctype) for component
     *
     * @param name
     * @return datatype or null if not exists
     */
    public String getDataType(ComponentName name) {
        return dbMap.get(name) == null ? null : dbMap.get(name).doctype;
    }

    public String getDatabaseMapping(ComponentName name) {
        return dbMap.get(name) == null ? null : dbMap.get(name).getMapping();
    }

    /**
     * get database doctype definition for component
     *
     * @param name
     * @return mappings or null if not exists
     */
    public String getDatabaseMapping(ComponentName name, boolean useStrict) {
        return dbMap.get(name) == null ? null : dbMap.get(name).getMapping(useStrict);
    }

    /**
     * get database settings definition for component
     *
     * @param name
     * @return settings or null if not exists
     */
    public String getDatabaseSettings(ComponentName name, int shards, int replicas) {
        return dbMap.get(name) == null ? null : dbMap.get(name).getSettings(shards, replicas);
    }

    public String getDatabaseMapping(ComponentName name, SdnrDbType dbType) {
        switch (dbType) {
            case ELASTICSEARCH:
                return this.getDatabaseMapping(name);
            case MARIADB:
                return mariadbMap.get(name) == null ? null
                        : mariadbMap.get(name).getMapping(this.release.getDbSuffix());
            default:
                return null;
        }
    }

    public static ReleaseInformation getInstance(Release r) {
        switch (r) {
            case EL_ALTO:
                return new ElAltoReleaseInformation();
            case FRANKFURT_R1:
                return new FrankfurtReleaseInformation();
            case FRANKFURT_R2:
                return new FrankfurtReleaseInformationR2();
            case GUILIN_R1:
                return new GuilinReleaseInformation();
            case HONOLULU_R1:
                return new HonoluluReleaseInformation();
            case ISTANBUL_R1:
                return new IstanbulReleaseInformation();
            case JAKARTA_R1:
                return new JakartaReleaseInformation();
            default:
                return null;
        }
    }

    /**
     * @return
     */
    public Set<ComponentName> getComponents() {
        return dbMap.keySet();
    }

    /**
     * @param component
     * @return
     */
    public boolean hasOwnDbIndex(ComponentName component) {
        return this.getDatabaseMapping(component) != null;
    }

    /**
     * @param views
     * @return true if components of this release are covered by the given indices
     */
    public boolean containsIndices(List<SqlTable> views) {

        if (this.dbMap.size() <= 0) {
            return false;
        }
        for (DatabaseInfo entry : this.dbMap.values()) {
            String dbIndexName = entry.getIndex(this.release.getDbSuffix());
            if (!views.stream().anyMatch(e->e.getName().equals(dbIndexName))) {
                return false;
            }
        }
        return true;

    }

    /**
     * @param dbClient
     * @return if succeeded or not
     */
    public abstract boolean runPreInitCommands(SqlDBClient dbClient);

    /**
     *
     * @param dbClient
     * @return if succeeded or not
     */
    public abstract boolean runPostInitCommands(SqlDBClient dbClient);



}
