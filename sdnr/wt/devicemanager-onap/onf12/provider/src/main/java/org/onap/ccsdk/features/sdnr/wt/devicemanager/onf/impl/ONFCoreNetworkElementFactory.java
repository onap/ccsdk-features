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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.impl;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.factory.NetworkElementFactory;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.microwave.OnfMicrowaveModel;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.microwave.WrapperMicrowaveModelRev170324;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.microwave.WrapperMicrowaveModelRev180907;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.microwave.WrapperMicrowaveModelRev181010;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ne.ONFCoreNetworkElement12Basic;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ne.ONFCoreNetworkElement12Microwave;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.NetworkElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a Network Element representation according to the capability information. The capabilities are more than an
 * ODL-QName. After the ? other terms than "revision" are provided.
 *
 */
public class ONFCoreNetworkElementFactory implements NetworkElementFactory {

    private static final Logger log = LoggerFactory.getLogger(ONFCoreNetworkElementFactory.class);

    private final @NonNull DeviceManagerOnfConfiguration configuration;

    public ONFCoreNetworkElementFactory(@NonNull DeviceManagerOnfConfiguration configuration) {
        this.configuration = configuration;
    }


    @Override
    public Optional<org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElement> create(
            @NonNull NetconfAccessor accessor, @NonNull DeviceManagerServiceProvider serviceProvider) {

        log.info("Enter factory {}", ONFCoreNetworkElementFactory.class.getName(), accessor.getNodeId());

        Capabilities capabilities = accessor.getCapabilites();

        if (capabilities.isSupportingNamespaceAndRevision(NetworkElement.QNAME)) {
            OnfMicrowaveModel onfMicrowaveModel = null;
            Optional<NetconfBindingAccessor> bindingAccessor = accessor.getNetconfBindingAccessor();
            if (capabilities.isSupportingNamespaceAndRevision(WrapperMicrowaveModelRev170324.QNAME)) {
                onfMicrowaveModel = new WrapperMicrowaveModelRev170324(bindingAccessor.get(), serviceProvider);
            } else if (capabilities.isSupportingNamespaceAndRevision(WrapperMicrowaveModelRev180907.QNAME)) {
                onfMicrowaveModel = new WrapperMicrowaveModelRev180907(bindingAccessor.get(), serviceProvider);
            } else if (capabilities.isSupportingNamespaceAndRevision(WrapperMicrowaveModelRev181010.QNAME)) {
                onfMicrowaveModel = new WrapperMicrowaveModelRev181010(bindingAccessor.get(), serviceProvider);
            }

            if (onfMicrowaveModel != null) {
                return Optional.of(new ONFCoreNetworkElement12Microwave(bindingAccessor.get(), serviceProvider, configuration,
                        onfMicrowaveModel));
            } else {
                return Optional.of(new ONFCoreNetworkElement12Basic(bindingAccessor.get(), serviceProvider, configuration));
            }
        }

        return Optional.empty();
    }

}
