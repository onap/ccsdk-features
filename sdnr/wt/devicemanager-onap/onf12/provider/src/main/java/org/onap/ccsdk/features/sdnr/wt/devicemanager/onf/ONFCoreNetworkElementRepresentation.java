/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
 * =================================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * ============LICENSE_END==========================================================================
 */
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf;

import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.DeviceMonitoredNe;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.InventoryProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.PerformanceDataProvider;
import org.opendaylight.mdsal.binding.api.MountPoint;

public interface ONFCoreNetworkElementRepresentation
        extends DeviceMonitoredNe, PerformanceDataProvider, NetworkElementCoreData, InventoryProvider, NetworkElement {

    /**
     * Read during startup all relevant structure and status parameters from device. Remove all currentAlarms, read
     * structure from networkElement with all interfacePacs, read current alarm status
     */
    public void initialReadFromNetworkElement();

    public String getMountPointNodeName();

    public int removeAllCurrentProblemsOfNode();

    public void doRegisterEventListener(MountPoint mountPoint);
}
