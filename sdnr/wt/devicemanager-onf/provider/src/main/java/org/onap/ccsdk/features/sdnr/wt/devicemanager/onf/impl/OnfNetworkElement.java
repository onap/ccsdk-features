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

import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElementService;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.opendaylight.mdsal.binding.api.MountPoint;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.netconf.notifications.rev120206.IetfNetconfNotificationsListener;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.netconf.notifications.rev120206.NetconfCapabilityChange;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.netconf.notifications.rev120206.NetconfConfigChange;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.netconf.notifications.rev120206.NetconfConfirmedCommit;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.netconf.notifications.rev120206.NetconfSessionEnd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.netconf.notifications.rev120206.NetconfSessionStart;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.netconf.notifications.rev120206.netconf.config.change.Edit;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.EventlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.NetworkElementDeviceType;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class OnfNetworkElement implements NetworkElement {

    private static final Logger log = LoggerFactory.getLogger(OnfNetworkElement.class);

    private final NetconfAccessor netconfAccessor;

    private final DataProvider databaseService;

    private @NonNull final OnfListener ranListener;

    private ListenerRegistration<NotificationListener> ranListenerRegistrationResult;

    OnfNetworkElement(NetconfAccessor netconfAccess, DataProvider databaseService) {
        log.info("Create {}",OnfNetworkElement.class.getSimpleName());
        this.netconfAccessor = netconfAccess;
        this.databaseService = databaseService;

        this.ranListenerRegistrationResult = null;
        this.ranListener = new OnfListener();

    }

    public void initialReadFromNetworkElement() {
    }

    @Override
    public NetworkElementDeviceType getDeviceType() {
        return NetworkElementDeviceType.ORAN;
    }

    private void doRegisterNotificationListener(MountPoint mountPoint) {
        log.info("Begin register listener for Mountpoint {}", mountPoint.getIdentifier().toString());
        final Optional<NotificationService> optionalNotificationService = mountPoint
                .getService(NotificationService.class);
        final NotificationService notificationService = optionalNotificationService.get();
        // notificationService.registerNotificationListener(microwaveEventListener);
        ranListenerRegistrationResult = notificationService.registerNotificationListener(ranListener);
        log.info("End registration listener for Mountpoint {} Listener: {} Result: {}",
                mountPoint.getIdentifier().toString(), optionalNotificationService, ranListenerRegistrationResult);
    }

    private class OnfListener implements IetfNetconfNotificationsListener {

        @Override
        public void onNetconfConfirmedCommit(NetconfConfirmedCommit notification) {
            log.info("onNetconfConfirmedCommit ", notification);
        }

        @Override
        public void onNetconfSessionStart(NetconfSessionStart notification) {
            log.info("onNetconfSessionStart ", notification);
        }

        @Override
        public void onNetconfSessionEnd(NetconfSessionEnd notification) {
            log.info("onNetconfSessionEnd ", notification);
        }

        @Override
        public void onNetconfCapabilityChange(NetconfCapabilityChange notification) {
            log.info("onNetconfCapabilityChange ", notification);
        }

        @Override
        public void onNetconfConfigChange(NetconfConfigChange notification) {
            log.info("onNetconfConfigChange (1) {}", notification);
            StringBuffer sb = new StringBuffer();
            List<Edit> editList = notification.nonnullEdit();
            for (Edit edit : editList) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(edit);

                EventlogBuilder eventlogBuilder = new EventlogBuilder();

                InstanceIdentifier<?> target = edit.getTarget();
                if (target != null) {
                    eventlogBuilder.setObjectId(target.toString());
                    log.info("TARGET: {} {} {}", target.getClass(), target.getTargetType());
                    for (PathArgument pa : target.getPathArguments()) {
                        log.info("PathArgument {}", pa);
                    }
                }
                eventlogBuilder.setNodeId(netconfAccessor.getNodeId().getValue());
                eventlogBuilder.setNewValue(String.valueOf(edit.getOperation()));
                databaseService.writeEventLog(eventlogBuilder.build());
            }
            log.info("onNetconfConfigChange (2) {}", sb);
        }
    }

    @Override
    public void register() {
    }

    @Override
    public void deregister() {
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
    public Optional<NetconfAccessor> getAcessor() {
        return Optional.of(netconfAccessor);
    }

}
