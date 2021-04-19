/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property. All rights reserved.
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

package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.impl;

import java.util.Optional;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.factory.NetworkElementFactory;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.ControlConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Onf14NetworkElementFactory implements NetworkElementFactory {

    private static final Logger log = LoggerFactory.getLogger(Onf14NetworkElementFactory.class);
    private Optional<Onf14Configuration> configuration = Optional.empty();

    @Override
    public Optional<NetworkElement> create(NetconfAccessor accessor, DeviceManagerServiceProvider serviceProvider) {

        Optional<NetworkElement> ne = Optional.empty();
        Capabilities capabilities = accessor.getCapabilites();
        if (capabilities.isSupportingNamespace(ControlConstruct.QNAME)) {
            String namespaceRevision = capabilities.getRevisionForNamespace(ControlConstruct.QNAME);

            if (configuration.isPresent() && configuration.get().isUseDomApiEnabled()) {
                Optional<NetconfDomAccessor> domAccessor = accessor.getNetconfDomAccessor();
                if (domAccessor.isPresent()) {
                    ne = Optional.of(new Onf14DomNetworkElement(domAccessor.get(), serviceProvider, namespaceRevision));
                }
            } else {
                Optional<NetconfBindingAccessor> bindingAccessor = accessor.getNetconfBindingAccessor();
                if (bindingAccessor.isPresent()) {
                    ne = Optional.of(new Onf14NetworkElement(bindingAccessor.get(), serviceProvider));
                }
            }
            log.info("Create device:{}", ne.isPresent() ? ne.get().getClass().getSimpleName() : "not");
        }
        return ne;
    }

    @Override
    public void init(DeviceManagerServiceProvider serviceProvider) {
        configuration = Optional.of(new Onf14Configuration(serviceProvider.getConfigurationFileRepresentation()));
    }
}
