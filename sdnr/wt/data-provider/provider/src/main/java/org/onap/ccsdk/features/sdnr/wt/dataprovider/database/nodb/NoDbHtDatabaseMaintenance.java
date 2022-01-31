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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.database.nodb;

import java.util.Arrays;
import java.util.List;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.HtDatabaseMaintenance;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.MaintenanceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.MaintenanceEntity;

public class NoDbHtDatabaseMaintenance implements HtDatabaseMaintenance {

    @Override
    public MaintenanceEntity createIfNotExists(String mountPointNodeName) {
        return new MaintenanceBuilder().build();
    }

    @Override
    public void deleteIfNotRequired(String mountPointNodeName) {
        
    }

    @Override
    public List<MaintenanceEntity> getAll() {
        return Arrays.asList();
    }

    @Override
    public MaintenanceEntity getMaintenance(@Nullable String mountpointName) {
        return new MaintenanceBuilder().build();
    }

    @Override
    public MaintenanceEntity setMaintenance(MaintenanceEntity m) {
        return m;
    }
}
