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
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElementService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementDeviceType;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author herbert
 *
 */
public class NtsNetworkElement implements NetworkElement {

    private static final Logger LOG = LoggerFactory.getLogger(NtsNetworkElement.class);

    private final NetconfBindingAccessor netconfAccessor;

    @SuppressWarnings("unused")
    private final DataProvider databaseService;
    private final NotificationListenerImpl notificationListener;

    private @NonNull ListenerRegistration<NotificationListener> listenerRegistrationresult;

    NtsNetworkElement(NetconfBindingAccessor netconfAccess, DeviceManagerServiceProvider serviceProvider) {
        LOG.debug("Create {}", NtsNetworkElement.class.getSimpleName());
        this.netconfAccessor = netconfAccess;
        this.databaseService = serviceProvider.getDataProvider();
        this.notificationListener = new NotificationListenerImpl(netconfAccess,serviceProvider);
        this.listenerRegistrationresult = null;
    }

    @Override
    public void deregister() {
        if(this.listenerRegistrationresult!=null) {
            this.listenerRegistrationresult.close();
        }
    }

    @Override
    public NodeId getNodeId() {
        return netconfAccessor.getNodeId();
    }

    @Override
    public <L extends NetworkElementService> Optional<L> getService(Class<L> clazz) {
        return Optional.empty();
    }

    @Override
    public void warmstart() {
    }

    @Override
    public void register() {
        if (netconfAccessor.isNotificationsRFC5277Supported()) {
            listenerRegistrationresult = netconfAccessor.doRegisterNotificationListener(this.notificationListener);
            // Register default (NETCONF) stream
            netconfAccessor.registerNotificationsStream();
            LOG.debug("registered for notifications");
        } else {
            LOG.warn("unable to register for notifications. RFC5277 not supported");
        }
    }

    @Override
    public NetworkElementDeviceType getDeviceType() {
        return NetworkElementDeviceType.NtsManager;
    }

    @Override
    public Optional<NetconfAccessor> getAcessor() {
        return Optional.of(this.netconfAccessor);
    }
}
