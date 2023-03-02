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

package org.onap.ccsdk.features.sdnr.wt.devicemanager.openroadm.impl;

import java.util.Optional;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.factory.NetworkElementFactory;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.opendaylight.yang.gen.v1.http.org.openroadm.device.rev191129.OrgOpenroadmDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Shabnam Sultana
 *
 *         Register the openroadm device as an optical network element
 *
 **/

public class OpenroadmNetworkElementFactory implements NetworkElementFactory {

    // variables
    private static final Logger log = LoggerFactory.getLogger(OpenroadmNetworkElementFactory.class);
    // end of variables

    // public methods
    @Override
    public Optional<NetworkElement> create(NetconfAccessor accessor, DeviceManagerServiceProvider serviceProvider) {

        if (accessor.getCapabilites().isSupportingNamespaceAndRevision(OrgOpenroadmDevice.QNAME)) {
            log.info("Create OpenRoadm device {} ", OpenroadmNetworkElement.class.getName());
            log.debug("Node Id read by Acessor {}:", accessor.getNodeId().getValue());
            Optional<NetconfBindingAccessor> bindingAccessor = accessor.getNetconfBindingAccessor();
            if (bindingAccessor.isPresent()) {
                return Optional.of(new OpenroadmNetworkElement(bindingAccessor.get(), serviceProvider));
            }
        } else if (accessor.getCapabilites().isSupportingNamespaceAndRevision("http://org/openroadm/device","2018-10-19")) {
            log.info("Create OpenRoadm base device {} ", OpenroadmNetworkElementBase.class.getName());
            log.debug("Node Id read by Acessor {}:", accessor.getNodeId().getValue());
            Optional<NetconfBindingAccessor> bindingAccessor = accessor.getNetconfBindingAccessor();
            if (bindingAccessor.isPresent()) {
                return Optional.of(new OpenroadmNetworkElementBase(bindingAccessor.get(), serviceProvider));
            }
        }
        return Optional.empty();
    }
    // end of public methods

}
