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

package org.onap.ccsdk.features.sdnr.wt.devicemanager.adaptermanager.impl;

import java.util.Optional;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.factory.NetworkElementFactory;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.opendaylight.yang.gen.v1.urn.o.ran.sc.params.xml.ns.yang.nts.manager.rev210608.simulation.NetworkFunctions;
import org.opendaylight.yangtools.yang.common.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * YANG Specs:
 *    urn:o-ran-sc:params:xml:ns:yang:nts:manager?revision=2021-06-08)nts-manager
 *
 */
public class AdapterManagerNetworkElementFactory implements NetworkElementFactory {

    private static final Logger log = LoggerFactory.getLogger(AdapterManagerNetworkElementFactory.class);

    private static QName ROOTKEY=NetworkFunctions.QNAME;

    @Override
    public Optional<NetworkElement> create(NetconfAccessor acessor, DeviceManagerServiceProvider serviceProvider) {
        showLogInfo();
        Capabilities capabilities = acessor.getCapabilites();
        if (capabilities.isSupportingNamespaceAndRevision(ROOTKEY)) {
            Optional<NetconfBindingAccessor> bindingAccessor = acessor.getNetconfBindingAccessor();
            if (bindingAccessor.isPresent()) {
                log.info("Create device {} ", NtsNetworkElement.class.getName());
                return Optional.of(new NtsNetworkElement(bindingAccessor.get(), serviceProvider));
            }
        }
        log.debug("No accessor for mountpoint {} {}", acessor.getNodeId(), capabilities);
        return Optional.empty();
    }

    private void showLogInfo() {
        log.debug("{} searching for {}", AdapterManagerNetworkElementFactory.class.getSimpleName(),
                Capabilities.getNamespaceAndRevisionAsString(ROOTKEY));
    }
}
