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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.base.onfcore;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.NetworkElementFactory;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.UnkownDevicemanagerServiceException;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.onfcore.wrapperc.OnfMicrowaveModel;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.onfcore.wrapperc.WrapperMicrowaveModelRev170324;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.onfcore.wrapperc.WrapperMicrowaveModelRev180907;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.onfcore.wrapperc.WrapperMicrowaveModelRev181010;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.dcaeconnector.impl.DcaeForwarderInternal;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.DevicemanagerNotificationDelayService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.WebSocketServiceClientInternal;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.Capabilities;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.INetconfAcessor;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.NetworkElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a Network Element representation according to the capability
 * information. The capabilities are more than an ODL-QName. After the ? other
 * terms than "revision" are provided.
 *
 */
public class ONFCoreNetworkElementFactory implements NetworkElementFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ONFCoreNetworkElementFactory.class);

    private static final ONFCoreNetworkElementRepresentation ONFCORE_NETWORKELEMENT_LOCK = new ONFCoreNetworkElementEmpty("NE-LOCK");

    /**
     * Used as Lock by devicemanager
     * @return ONFCoreNetworkElementRepresentation for lock purpose
     */
    public @NonNull ONFCoreNetworkElementRepresentation getLock() {
        return ONFCORE_NETWORKELEMENT_LOCK;
    }

    @Override
    public Optional<org.onap.ccsdk.features.sdnr.wt.devicemanager.NetworkElement> create(INetconfAcessor acessor,
            DeviceManagerServiceProvider serviceProvider) {
        try {
            DataProvider dataProvider = serviceProvider.getDataProvider();
            WebSocketServiceClientInternal webSocketService = serviceProvider.getService(WebSocketServiceClientInternal.class);
            DcaeForwarderInternal aotsDcaeForwarder = serviceProvider.getService(DcaeForwarderInternal.class);
            DevicemanagerNotificationDelayService notificationDelayService = serviceProvider
                    .getService(DevicemanagerNotificationDelayService.class);

            Capabilities capabilities = acessor.getCapabilites();

            if (capabilities.isSupportingNamespaceAndRevision(NetworkElement.QNAME)) {
                OnfMicrowaveModel onfMicrowaveModel = null;

                if (capabilities.isSupportingNamespaceAndRevision(WrapperMicrowaveModelRev170324.QNAME)) {
                    onfMicrowaveModel = new WrapperMicrowaveModelRev170324(acessor);
                } else if (capabilities.isSupportingNamespaceAndRevision(WrapperMicrowaveModelRev180907.QNAME)) {
                    onfMicrowaveModel = new WrapperMicrowaveModelRev180907(acessor);
                } else if (capabilities.isSupportingNamespaceAndRevision(WrapperMicrowaveModelRev181010.QNAME)) {
                    onfMicrowaveModel = new WrapperMicrowaveModelRev181010(acessor);
                }

                String mountPointNodeName = acessor.getNodeId().getValue();
                DataBroker netconfNodeDataBroker = acessor.getDataBroker();

                if (onfMicrowaveModel != null) {
                    return Optional.of(new ONFCoreNetworkElement12Microwave(acessor, mountPointNodeName, capabilities, netconfNodeDataBroker,
                            webSocketService, dataProvider, aotsDcaeForwarder,
                            notificationDelayService, onfMicrowaveModel));
                } else {
                    return Optional.of(new ONFCoreNetworkElement12Basic(acessor, mountPointNodeName, capabilities, netconfNodeDataBroker,
                            webSocketService, dataProvider, aotsDcaeForwarder,
                            notificationDelayService));
                }
            }

        } catch (UnkownDevicemanagerServiceException e) {
            LOG.warn("Service missing", e);
        }
        return Optional.empty();
    }

}
