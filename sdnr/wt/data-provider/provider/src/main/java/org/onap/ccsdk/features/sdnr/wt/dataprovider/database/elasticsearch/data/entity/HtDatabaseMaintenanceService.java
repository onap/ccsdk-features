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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.database.elasticsearch.data.entity;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.common.HtAssert;
import org.onap.ccsdk.features.sdnr.wt.common.database.HtDatabaseClient;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.elasticsearch.EsDataObjectReaderWriter2;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.HtDatabaseMaintenance;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.NetconfTimeStampImpl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Entity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.MaintenanceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.MaintenanceEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementConnectionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementConnectionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HtDatabaseMaintenanceService implements HtDatabaseMaintenance {

    private static final Logger LOG = LoggerFactory.getLogger(HtDatabaseMaintenanceService.class);

    private final EsDataObjectReaderWriter2<MaintenanceEntity> maintenanceRW;
    private final EsDataObjectReaderWriter2<NetworkElementConnectionEntity> requiredNeRW;

    public HtDatabaseMaintenanceService(@NonNull HtDatabaseClient client) throws ClassNotFoundException {
        HtAssert.nonnull(client);

        // Create control structure
        maintenanceRW = new EsDataObjectReaderWriter2<>(client, Entity.Maintenancemode, MaintenanceEntity.class,
                MaintenanceBuilder.class, true).setEsIdAttributeName("_id");

        requiredNeRW = new EsDataObjectReaderWriter2<>(client, Entity.NetworkelementConnection,
                NetworkElementConnectionEntity.class, NetworkElementConnectionBuilder.class, true)
                        .setEsIdAttributeName("_id");

    }

    /**
     * Get existing object for mountpoint to manage maintenance mode
     *
     * @return Object with configuration
     */
    @Override
    @Nullable
    public MaintenanceEntity getMaintenance(@Nullable String mountpointId) {
        MaintenanceEntity deviceMaintenanceMode = null;
        if (maintenanceRW != null && mountpointId != null) {
            deviceMaintenanceMode = maintenanceRW.read(mountpointId);
        }
        return deviceMaintenanceMode;
    }

    @Override
    public MaintenanceEntity setMaintenance(MaintenanceEntity m) {
        if (maintenanceRW != null) {
            if (maintenanceRW.write(m, m.getNodeId()) == null) {
                throw new IllegalArgumentException("Problem writing to database: " + m.getId());
            }
            LOG.debug("Wrote maintenance object {}", m.toString());
        }
        return m;
    }

    @Override
    public List<MaintenanceEntity> getAll() {
        return maintenanceRW != null ? maintenanceRW.doReadAll().getHits() : new ArrayList<>();
    }

    @Override
    public MaintenanceEntity createIfNotExists(String mountpointId) {
        MaintenanceEntity deviceMaintenanceMode = null;
        if (maintenanceRW != null) {
            deviceMaintenanceMode = maintenanceRW.read(mountpointId);
            if (deviceMaintenanceMode == null) {
                LOG.debug("creating empty maintenance object in database");
                deviceMaintenanceMode = getDefaultMaintenance(mountpointId);
                maintenanceRW.write(deviceMaintenanceMode, mountpointId);
            } else {
                LOG.debug("maintenance object already exists in database");
            }
        } else {
            LOG.warn("cannot create maintenance obj. db reader/writer is null");
        }
        return deviceMaintenanceMode;
    }

    @Override
    public void deleteIfNotRequired(String mountPointNodeName) {

        if (!this.isRequireNe(mountPointNodeName)) {
            if (maintenanceRW != null) {
                LOG.debug("removing maintenance object in database for " + mountPointNodeName);
                maintenanceRW.remove(mountPointNodeName);
            } else {
                LOG.warn("cannot create maintenance obj. db reader/writer is null");
            }
        }


    }

    /**
     * Provide default maintenanceinformation for a device
     *
     * @param mountpointId nodeId of device
     * @return default data
     */
    static private MaintenanceEntity getDefaultMaintenance(String mountpointId) {

        DateAndTime now = NetconfTimeStampImpl.getConverter().getTimeStamp();

        MaintenanceBuilder deviceMaintenanceModeBuilder = new MaintenanceBuilder();
        deviceMaintenanceModeBuilder.setNodeId(mountpointId).setId(mountpointId);
        // Use time from mountpoint creation
        deviceMaintenanceModeBuilder.setDescription("");
        // Use time from mountpoint creation
        deviceMaintenanceModeBuilder.setStart(now);
        deviceMaintenanceModeBuilder.setEnd(now);
        deviceMaintenanceModeBuilder.setActive(false);

        // Reference to all
        //consistent to UI input to null/not empty string
        //deviceMaintenanceModeBuilder.setObjectIdRef("");
        //deviceMaintenanceModeBuilder.setProblem("");

        return deviceMaintenanceModeBuilder.build();
    }

    // -- Private
    /**
     * Check in required ne if entry exists for mountpointNodeName
     *
     * @param mountPointNodeName
     * @return
     */
    @SuppressWarnings("null")
    private boolean isRequireNe(String mountPointNodeName) {
        NetworkElementConnectionEntity ne = null;
        if (requiredNeRW != null) {
            LOG.debug("searching for entry in required-networkelement for " + mountPointNodeName);
            ne = requiredNeRW.read(mountPointNodeName);
        } else {
            LOG.warn("cannot read db. no db reader writer initialized");
        }
        if (ne != null && ne.requireIsRequired() != null) {
            return ne.requireIsRequired();
        } else {
            return false;
        }
    }

}
