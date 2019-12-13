/*******************************************************************************
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
 ******************************************************************************/

package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.impl;

import java.util.Optional;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.NetworkElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.NetworkElementFactory;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.INetconfAcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnfNetworkElementFactory implements NetworkElementFactory {

    private static final Logger log = LoggerFactory.getLogger(OnfNetworkElementFactory.class);

    @Override
    public Optional<NetworkElement> create(INetconfAcessor acessor, DeviceManagerServiceProvider serviceProvider) {
        if (acessor.getCapabilites().isSupportingNamespace(org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.NetworkElement.QNAME)) {
            log.info("Create device {} ",OnfNetworkElement.class.getName());
            return Optional.of(new OnfNetworkElement(acessor, serviceProvider.getDataProvider()));
        } else {
            return Optional.empty();
        }
    }
}
