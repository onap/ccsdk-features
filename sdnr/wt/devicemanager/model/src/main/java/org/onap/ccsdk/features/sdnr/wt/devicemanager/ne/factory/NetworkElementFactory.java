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

package org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.factory;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;

public interface NetworkElementFactory {

    /**
     * Called after connect of device. Decide if devicemanger should be started to manage device.
     * @param accessor function to access device and get information from mountpoint
     * @param serviceProvider provides base device manager services.
     * @return Empty or NetworkElement object
     */
    Optional<NetworkElement> create(@NonNull NetconfAccessor accessor,
            @NonNull DeviceManagerServiceProvider serviceProvider);

    /**
     * Called directly after factory registration to allow initialization
     * @param serviceProvider provides base device manager services.
     */
    default void init(DeviceManagerServiceProvider serviceProvider) {
    }


}
