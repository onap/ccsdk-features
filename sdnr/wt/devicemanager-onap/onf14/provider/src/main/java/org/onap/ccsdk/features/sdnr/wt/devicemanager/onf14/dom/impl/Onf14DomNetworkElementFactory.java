/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2022 highstreet technologies GmbH Intellectual Property. All rights reserved.
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

package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl;

import java.util.Optional;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.factory.NetworkElementFactory;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.yangspecs.CoreModel14;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Onf14DomNetworkElementFactory implements NetworkElementFactory {

    private static final Logger LOG = LoggerFactory.getLogger(Onf14DomNetworkElementFactory.class);

    @Override
    public Optional<NetworkElement> create(NetconfAccessor accessor, DeviceManagerServiceProvider serviceProvider) {

        Optional<NetworkElement> ne = Optional.empty();
        Optional<NetconfDomAccessor> domAccessor = accessor.getNetconfDomAccessor();
        if (domAccessor.isPresent()) {
            Optional<CoreModel14> onf14CoreModelQnames = CoreModel14.getModule(domAccessor.get());
            if (onf14CoreModelQnames.isPresent()) {
                ne = Optional
                        .of(new Onf14DomNetworkElement(domAccessor.get(), serviceProvider, onf14CoreModelQnames.get()));
            }
       }
        LOG.info("Create device:{}", ne.isPresent() ? ne.get().getClass().getSimpleName() : "not");
        return ne;
    }

}
