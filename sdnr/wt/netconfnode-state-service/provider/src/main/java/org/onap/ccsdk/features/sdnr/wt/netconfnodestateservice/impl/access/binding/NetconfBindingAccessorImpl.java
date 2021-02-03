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
package org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.access.binding;

import java.util.Objects;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.TransactionUtils;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.access.NetconfAccessorImpl;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.MountPoint;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class NetconfBindingAccessorImpl extends NetconfAccessorImpl implements NetconfBindingAccessor {

    private static final Logger log = LoggerFactory.getLogger(NetconfBindingAccessorImpl.class);

    private final static GenericTransactionUtils GENERICTRANSACTIONUTILS = new GenericTransactionUtils();

    private final DataBroker dataBroker;
    private final MountPoint mountpoint;

    /**
     * Contains all data to access and manage netconf device
     *
     * @param nodeId of managed netconf node
     * @param netconfNode information
     * @param dataBroker to access node
     * @param mountpoint of netconfNode
     */
    public NetconfBindingAccessorImpl(NetconfAccessorImpl accessor, DataBroker dataBroker, MountPoint mountpoint) {
        super(accessor);
        this.dataBroker = Objects.requireNonNull(dataBroker);
        this.mountpoint = Objects.requireNonNull(mountpoint);
    }

    @Override
    public DataBroker getDataBroker() {
        return dataBroker;
    }

    @Override
    public MountPoint getMountpoint() {
        return mountpoint;
    }

    @Override
    public TransactionUtils getTransactionUtils() {
        return GENERICTRANSACTIONUTILS;
    }

    @Override
    public @NonNull <T extends NotificationListener> ListenerRegistration<NotificationListener> doRegisterNotificationListener(
            @NonNull T listener) {
        log.info("Begin register listener for Mountpoint {}", mountpoint.getIdentifier().toString());
        final Optional<NotificationService> optionalNotificationService =
                mountpoint.getService(NotificationService.class);
        final NotificationService notificationService = optionalNotificationService.get();
        final ListenerRegistration<NotificationListener> ranListenerRegistration =
                notificationService.registerNotificationListener(listener);
        log.info("End registration listener for Mountpoint {} Listener: {} Result: {}",
                mountpoint.getIdentifier().toString(), optionalNotificationService, ranListenerRegistration);
        return ranListenerRegistration;
    }
}
