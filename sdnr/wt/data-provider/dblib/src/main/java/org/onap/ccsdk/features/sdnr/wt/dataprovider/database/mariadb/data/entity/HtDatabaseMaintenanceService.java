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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.database.mariadb.data.entity;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.mariadb.data.MariaDBDataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.mariadb.database.MariaDBReaderWriter;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.HtDatabaseMaintenance;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CreateMaintenanceInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.CreateMaintenanceOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.DeleteMaintenanceInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.MaintenanceEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadMaintenanceListInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadMaintenanceListOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadNetworkElementConnectionListInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadNetworkElementConnectionListOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.Filter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.FilterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.FilterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.maintenance.list.output.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HtDatabaseMaintenanceService implements HtDatabaseMaintenance {

    private static final Logger LOG = LoggerFactory.getLogger(HtDatabaseMaintenanceService.class);
    private final MariaDBReaderWriter<Data> maintenanceRw;
    private final MariaDBDataProvider dbProvider;

    public HtDatabaseMaintenanceService(MariaDBDataProvider dbProvider) {
        this.dbProvider = dbProvider;
        this.maintenanceRw = dbProvider.getMaintenanceReaderWriter();
    }


    @Override
    public MaintenanceEntity createIfNotExists(String nodeId) {

        MaintenanceEntity e = this.getMaintenance(nodeId);
        if (e == null) {
            try {
                CreateMaintenanceOutputBuilder createResult = this.dbProvider
                        .createMaintenance(new CreateMaintenanceInputBuilder().setNodeId(nodeId).build());
                e = createResult.build();
            } catch (IOException e1) {
                LOG.warn("problem writing initial maintenance entry for {} : ", nodeId, e);
            }
        }
        return e;
    }

    private static Map<FilterKey, Filter> getFilterInput(String key, String value) {
        Map<FilterKey, Filter> filterMap = new HashMap<>();
        Filter f = new FilterBuilder().setProperty(key).setFiltervalue(value).build();
        filterMap.put(f.key(), f);
        return filterMap;
    }

    @Override
    public void deleteIfNotRequired(String nodeId) {
        ReadNetworkElementConnectionListOutput result = this.dbProvider.readNetworkElementConnectionList(
                new ReadNetworkElementConnectionListInputBuilder().setFilter(getFilterInput("node-id", nodeId)).build())
                .build();
        if (result.getData() != null && result.getData().size() > 0) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.network.element.connection.list.output.Data entry =
                    result.getData().get(0);
            if (entry.isIsRequired()) {
                return;

            }
        }
        try {
            this.dbProvider.deleteMaintenance(
                    new DeleteMaintenanceInputBuilder().setId(DatabaseIdGenerator.getMaintenanceId(nodeId)).build());
        } catch (IOException e) {
            LOG.warn("problem deleting maintenance entry for node {}: ", nodeId, e);
        }
    }

    @Override
    public List<MaintenanceEntity> getAll() {
        return this.maintenanceRw.readAll(MaintenanceEntity.class);
    }

    @Override
    public MaintenanceEntity getMaintenance(@Nullable String nodeId) {
        ReadMaintenanceListOutput result = this.dbProvider
                .readMaintenanceList(
                        new ReadMaintenanceListInputBuilder().setFilter(getFilterInput("node-id", nodeId)).build())
                .build();

        return result.getData() != null ? result.getData().size() > 0 ? result.getData().get(0) : null : null;
    }

    @Override
    public MaintenanceEntity setMaintenance(MaintenanceEntity m) {

        this.maintenanceRw.updateOrInsert(m, DatabaseIdGenerator.getMaintenanceId(m));
        return this.getMaintenance(m.getNodeId());
    }


}
