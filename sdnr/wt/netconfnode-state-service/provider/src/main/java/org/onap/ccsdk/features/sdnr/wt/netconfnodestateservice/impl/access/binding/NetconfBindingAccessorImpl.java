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

import com.google.common.util.concurrent.ListenableFuture;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.common.YangHelper;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.TransactionUtils;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.access.NetconfAccessorImpl;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.MountPoint;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.binding.api.RpcConsumerRegistry;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf.notification._1._0.rev080714.CreateSubscriptionInputBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf.notification._1._0.rev080714.CreateSubscriptionOutput;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf.notification._1._0.rev080714.NotificationsService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf.notification._1._0.rev080714.StreamNameType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netmod.notification.rev080714.Netconf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netmod.notification.rev080714.netconf.Streams;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netmod.notification.rev080714.netconf.streams.Stream;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetconfBindingAccessorImpl extends NetconfAccessorImpl implements NetconfBindingAccessor {

    private static final Logger log = LoggerFactory.getLogger(NetconfBindingAccessorImpl.class);

    private final static GenericTransactionUtils GENERICTRANSACTIONUTILS = new GenericTransactionUtils();

    private final DataBroker dataBroker;
    private final MountPoint mountpoint;
    final NotificationsService mountpointNotificationService;

    /**
     * Contains all data to access and manage NETCONF device
     *
     * @param accessor with basic mountpoint information
     * @param dataBroker to access node
     * @param mountpoint of netconfNode
     * @throws IllegalArgumentException
     */
    public NetconfBindingAccessorImpl(NetconfAccessorImpl accessor, DataBroker dataBroker, MountPoint mountpoint)
            throws IllegalArgumentException {
        super(accessor);
        this.dataBroker = Objects.requireNonNull(dataBroker);
        this.mountpoint = Objects.requireNonNull(mountpoint);

        final Optional<RpcConsumerRegistry> optionalRpcConsumerService =
                mountpoint.getService(RpcConsumerRegistry.class);
        if (optionalRpcConsumerService.isPresent()) {
            mountpointNotificationService = optionalRpcConsumerService.get().getRpcService(NotificationsService.class);
        } else {
            throw new IllegalArgumentException("Can not process without rpcConsumerService service");
        }
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
        log.debug("Begin register listener for Mountpoint {}", mountpoint.getIdentifier().toString());
        final Optional<NotificationService> optionalNotificationService =
                mountpoint.getService(NotificationService.class);
        final NotificationService notificationService = optionalNotificationService.get();
        final ListenerRegistration<NotificationListener> ranListenerRegistration =
                notificationService.registerNotificationListener(listener);
        log.debug("End registration listener for Mountpoint {} Listener: {} Result: {}",
                mountpoint.getIdentifier().toString(), optionalNotificationService, ranListenerRegistration);
        return ranListenerRegistration;
    }

    @Override
    public ListenableFuture<RpcResult<CreateSubscriptionOutput>> registerNotificationsStream(
            String streamName) {
        final CreateSubscriptionInputBuilder createSubscriptionInputBuilder = new CreateSubscriptionInputBuilder();
        if (streamName != null) {
            createSubscriptionInputBuilder.setStream(new StreamNameType(streamName));
        }
        log.debug("Event listener triggering notification stream '{}' for node {}", streamName, getNodeId());
        return mountpointNotificationService.createSubscription(createSubscriptionInputBuilder.build());
    }

    @Override
    public ListenableFuture<RpcResult<CreateSubscriptionOutput>> registerNotificationsStream() {
        return registerNotificationsStream((String)null);
    }

    @Override
    public void registerNotificationsStream(List<Stream> streamList) {
        for (Stream stream : streamList) {
            @Nullable
            StreamNameType streamName = stream.getName();
            if (streamName != null) {
                String streamNameValue = stream.getName().getValue();
                log.debug("Stream Name = {}, Stream Description = {}", streamNameValue, stream.getDescription());
                if (!(streamNameValue.equals(DefaultNotificationsStream)))
                    // Register any not default stream. Default stream is already registered
                    registerNotificationsStream(streamNameValue);
            } else {
                log.warn("Ignore a stream without name");
            }
        }
    }

    @Override
    public List<Stream> getNotificationStreams() {
        final Class<Netconf> netconfClazz = Netconf.class;
        InstanceIdentifier<Netconf> streamsIID = InstanceIdentifier.builder(netconfClazz).build();

        Netconf res = getTransactionUtils().readData(getDataBroker(), LogicalDatastoreType.OPERATIONAL, streamsIID);
        if (res != null) {
            Streams streams = res.getStreams();
            if (streams != null) {
                return YangHelper.getList(streams.nonnullStream());
            }
        }
        return Collections.emptyList();
    }

}
