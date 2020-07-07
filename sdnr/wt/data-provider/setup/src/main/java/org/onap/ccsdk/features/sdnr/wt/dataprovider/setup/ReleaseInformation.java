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

import java.util.Map;
import java.util.Set;

import org.onap.ccsdk.features.sdnr.wt.common.database.HtDatabaseClient;
import org.onap.ccsdk.features.sdnr.wt.common.database.data.IndicesEntryList;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.ComponentName;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.DatabaseInfo;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.KeepDataSearchHitConverter;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.Release;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.SearchHitConverter;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.elalto.ElAltoReleaseInformation;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.frankfurt.FrankfurtReleaseInformation;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.frankfurt.FrankfurtReleaseInformationR2;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.guilin.GuilinReleaseInformation;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.guilin.GuilinReleaseInformationR2;

public abstract class ReleaseInformation {

    // variables
    private final Release release;
    private final Map<ComponentName, DatabaseInfo> dbMap;
    // end of variables

    // constructors
    public ReleaseInformation(Release r, Map<ComponentName, DatabaseInfo> dbMap) {
        this.release = r;
        this.dbMap = dbMap;
    }
    // end of constructors

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
        return dbMap.get(comp) == null ? null : (prefix + dbMap.get(comp).getIndex(this.release.getDBSuffix()));
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

    /**
     * get converter for component data
     * 
     * @param dst destination release
     * @param comp component to convert
     * @return
     */
    public SearchHitConverter getConverter(Release dst, ComponentName comp) {
        if (dst == this.release && this.getComponents().contains(comp)) {
            return new KeepDataSearchHitConverter(comp);
        }
        return null;
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
            case GUILIN_R2:
                return new GuilinReleaseInformationR2();
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
     * @param indices
     * @return true if components of this release are covered by the given indices
     */
    protected boolean containsIndices(IndicesEntryList indices) {

        if (this.dbMap.size() <= 0) {
            return false;
        }
        for (DatabaseInfo entry : this.dbMap.values()) {
            String dbIndexName = entry.getIndex(this.release.getDBSuffix());
            if (indices.findByIndex(dbIndexName) == null) {
                return false;
            }
        }
        return true;

    }

    /**
     * @param dbClient
     * @return if succeeded or not
     */
    protected abstract boolean runPreInitCommands(HtDatabaseClient dbClient);

    /**
     * 
     * @param dbClient
     * @return if succeeded or not
     */
    protected abstract boolean runPostInitCommands(HtDatabaseClient dbClient);

}
