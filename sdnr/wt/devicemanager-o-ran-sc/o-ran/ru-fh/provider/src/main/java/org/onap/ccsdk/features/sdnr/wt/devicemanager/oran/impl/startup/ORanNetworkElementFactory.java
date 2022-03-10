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
 */
package org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.impl.startup;

import java.util.Optional;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.factory.NetworkElementFactory;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.impl.dom.ORanDOMNetworkElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.impl.dom.ORanDeviceManagerQNames;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.opendaylight.yangtools.yang.common.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ORanNetworkElementFactory implements NetworkElementFactory {

    private static final Logger log = LoggerFactory.getLogger(ORanNetworkElementFactory.class);
    //Workaround
    private static final QName OneCell =
            QName.create("urn:onf:otcc:wireless:yang:radio-access:commscope-onecell", "2020-06-22", "onecell").intern();

    @Override
    public Optional<NetworkElement> create(NetconfAccessor accessor, DeviceManagerServiceProvider serviceProvider) {
        Capabilities capabilites = accessor.getCapabilites();
        if (!capabilites.isSupportingNamespace(OneCell)) {
            if (capabilites.isSupportingNamespace(ORanDeviceManagerQNames.ORAN_HW_COMPONENT)) {
                log.info("Create device {} ", ORanDOMNetworkElement.class.getName());
                //Optional<NetconfBindingAccessor> bindingAccessor = accessor.getNetconfBindingAccessor();
                Optional<NetconfDomAccessor> domAccessor = accessor.getNetconfDomAccessor();
                if (domAccessor.isPresent()) {
                    //return Optional.of(new ORanNetworkElement(bindingAccessor.get(), serviceProvider));
                    return Optional.of(new ORanDOMNetworkElement(domAccessor.get(), serviceProvider));
                }
            }
        }
        return Optional.empty();
    }
}
