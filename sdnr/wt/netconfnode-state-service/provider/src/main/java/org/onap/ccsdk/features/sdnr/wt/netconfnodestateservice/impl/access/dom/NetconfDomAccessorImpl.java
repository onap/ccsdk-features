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
 *
 */
package org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.access.dom;

import static java.util.stream.Collectors.toList;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.ListenableFuture;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.DomContext;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.access.NetconfAccessorImpl;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.mdsal.MdsalApi;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
//import org.opendaylight.mdsal.binding.dom.codec.impl.BindingCodecContext;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;
import org.opendaylight.mdsal.dom.api.DOMMountPoint;
import org.opendaylight.mdsal.dom.api.DOMNotificationListener;
import org.opendaylight.mdsal.dom.api.DOMNotificationService;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf.notification._1._0.rev080714.CreateSubscriptionInput;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf.notification._1._0.rev080714.CreateSubscriptionInputBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf.notification._1._0.rev080714.create.subscription.input.Filter;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netmod.notification.rev080714.Netconf;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netmod.notification.rev080714.netconf.Streams;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netmod.notification.rev080714.netconf.streams.Stream;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netmod.notification.rev080714.netconf.streams.StreamKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetconfDomAccessorImpl extends NetconfAccessorImpl implements NetconfDomAccessor {

    private static final Logger LOG = LoggerFactory.getLogger(NetconfDomAccessorImpl.class);

    private static final QName CREATE_SUBSCRIPTION = QName.create(CreateSubscriptionInput.QNAME, "create-subscription");
    private static final YangInstanceIdentifier STREAMS_PATH =
            YangInstanceIdentifier.builder().node(Netconf.QNAME).node(Streams.QNAME).build();

    protected final DOMDataBroker dataBroker;
    protected final DOMMountPoint mountpoint;
    protected final DomContext domContext;
    private final DOMNotificationService notificationService;
    private final BindingNormalizedNodeSerializer serializer;
    private final DOMRpcService rpcService;


    public NetconfDomAccessorImpl(NetconfAccessorImpl accessor, DOMDataBroker domDataBroker,
            DOMMountPoint mountpoint, DomContext domContext) {
        super(accessor);
        this.dataBroker = Objects.requireNonNull(domDataBroker);
        this.mountpoint = Objects.requireNonNull(mountpoint);
        this.domContext = Objects.requireNonNull(domContext);
        this.serializer = domContext.getBindingNormalizedNodeSerializer();
        this.rpcService = MdsalApi.getMountpointService(mountpoint, DOMRpcService.class);
        this.notificationService = MdsalApi.getMountpointService(mountpoint, DOMNotificationService.class);
    }

    @Override
    public DOMDataBroker getDataBroker() {
        return dataBroker;
    }

    @Override
    public DOMMountPoint getMountpoint() {
        return mountpoint;
    }

    @Override
    public DOMRpcService getRpcService() {
        return rpcService;
    }

    @Override
    public DomContext getDomContext() {
        return domContext;
    }

    @Override
    public <T extends DataObject> Optional<T> readData(LogicalDatastoreType dataStoreType, YangInstanceIdentifier path,
            Class<T> clazz) {
        LOG.debug("Read to object datastore:{} path:{}", dataStoreType, path);

        try {
            return convertNormalizedNode(domContext.getBindingNormalizedNodeSerializer(),
                    readDataNode(dataStoreType, path), path, clazz);
        } catch (CanNotConvertException e) {
            LOG.info("Incomplete read to class transaction {} {}", dataStoreType, path, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<NormalizedNode> readDataNode(LogicalDatastoreType dataStoreType,
            YangInstanceIdentifier path) {
        LOG.debug("Read to node datastore:{} path:{}", dataStoreType, path);

        // Don't use try with resource because the implicit close of this construct is not handled
        // correctly by underlying opendaylight NETCONF service
        DOMDataTreeReadTransaction readOnlyTransaction = dataBroker.newReadOnlyTransaction();
        try {
            FluentFuture<Optional<NormalizedNode>> foData = readOnlyTransaction.read(dataStoreType, path);

            Optional<NormalizedNode> data = foData.get(120, TimeUnit.SECONDS);
            LOG.trace("read is done - {} ", foData.isDone());
            return data;
        } catch (InterruptedException e) {
            LOG.debug("Incomplete read to node transaction {} {}", dataStoreType, path, e);
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (ExecutionException | TimeoutException e) {
            LOG.debug("Incomplete read to node transaction {} {}", dataStoreType, path, e);
            return Optional.empty();
        } catch (IllegalArgumentException e) {
        	LOG.debug("IllegalArgumentException occurred, Incomplete read to node transaction {} {}", dataStoreType, path, e);
        	return Optional.empty();
        }
    }

    @Override
    public Optional<NormalizedNode> readControllerDataNode(LogicalDatastoreType dataStoreType,
            YangInstanceIdentifier path) {
        LOG.debug("Read to controller node datastore:{} path:{}", dataStoreType, path);

        DOMDataTreeReadTransaction readOnlyTransaction = this.getControllerDOMDataBroker().newReadOnlyTransaction();
        try {
            FluentFuture<Optional<NormalizedNode>> foData = readOnlyTransaction.read(dataStoreType, path);

            Optional<NormalizedNode> data = foData.get(120, TimeUnit.SECONDS);
            LOG.trace("read is done - {} ", foData.isDone());
            return data;
        } catch (InterruptedException e) {
            LOG.debug("Incomplete read to node transaction {} {}", dataStoreType, path, e);
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (ExecutionException | TimeoutException e) {
            LOG.debug("Incomplete read to node transaction {} {}", dataStoreType, path, e);
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends DataObject> Optional<T> convertNormalizedNode(BindingNormalizedNodeSerializer serializer,
            Optional<NormalizedNode> oData, YangInstanceIdentifier path, Class<T> clazz)
            throws CanNotConvertException {
        if (oData.isPresent()) {
            NormalizedNode data = oData.get();
            LOG.debug("convertNormalizedNode data identifier: {} data nodetype: {}", data.getIdentifier(),
                    data.getIdentifier().getNodeType());
            @Nullable
            Entry<InstanceIdentifier<?>, DataObject> entry = serializer.fromNormalizedNode(path, data);
            if (entry != null) {
                LOG.debug("object identifier: {}", entry.getKey());
                DataObject value = entry.getValue();
                if (clazz.isInstance(value)) {
                    return Optional.of((T) value);
                } else {
                    throw new CanNotConvertException("Unexpected class. Expected:" + clazz.getName() + " provided:"
                            + value.getClass().getName() + " Nodetype:" + data.getIdentifier().getNodeType());
                }
            } else {
                throw new CanNotConvertException(
                        "No object created for path:" + path + " Nodetype:" + data.getIdentifier().getNodeType());
            }
        } else {
            throw new CanNotConvertException("No data received for path:" + path);
        }
    }

    @Override
    public @NonNull <T extends DOMNotificationListener> ListenerRegistration<DOMNotificationListener> doRegisterNotificationListener(
            @NonNull T listener, Collection<Absolute> types) {
        LOG.debug("Begin register listener for Mountpoint {}", mountpoint.getIdentifier().toString());

        final ListenerRegistration<DOMNotificationListener> ranListenerRegistration =
                notificationService.registerNotificationListener(listener, types);

        LOG.debug("End registration listener for Mountpoint {} Listener: {} Result: {}",
                mountpoint.getIdentifier().toString(), notificationService, ranListenerRegistration);

        return ranListenerRegistration;
    }

    @Override
    public @NonNull <T extends DOMNotificationListener> ListenerRegistration<DOMNotificationListener> doRegisterNotificationListener(
            @NonNull T listener, Absolute[] types) {
        return doRegisterNotificationListener(listener, Arrays.asList(types));
    }

    @Override
    public @NonNull <T extends DOMNotificationListener> ListenerRegistration<DOMNotificationListener> doRegisterNotificationListener(
            @NonNull T listener, QName[] types) {
        List<Absolute> schemaPathList = Arrays.stream(types).map(qname -> Absolute.of(qname)).collect(toList());
        return doRegisterNotificationListener(listener, schemaPathList);
    }


    @Override
    public ListenableFuture<? extends DOMRpcResult> invokeCreateSubscription(CreateSubscriptionInput input) {
        final ContainerNode nnInput = serializer.toNormalizedNodeRpcData(input);
        return rpcService.invokeRpc(CREATE_SUBSCRIPTION, nnInput);
    }

    @Override
    public ListenableFuture<? extends DOMRpcResult> invokeCreateSubscription(Optional<Stream> oStream,
            Optional<Filter> filter, Optional<Instant> startTime, Optional<Instant> stopTime) {

        CreateSubscriptionInputBuilder inputBuilder = new CreateSubscriptionInputBuilder();
        boolean replayIsSupported = false;
        if (oStream.isPresent()) {
            Stream stream = oStream.get();
            if (stream.getName() != null) {
                inputBuilder.setStream(stream.getName());
            }
            replayIsSupported = Boolean.TRUE.equals(stream.requireReplaySupport());

        }
        filter.ifPresent(inputBuilder::setFilter);
        if (startTime.isPresent()) {
            if (replayIsSupported) {
                inputBuilder.setStartTime(getDateAndTime(startTime.get()));
                if (stopTime.isPresent()) {
                    if (startTime.get().isBefore(stopTime.get())) {
                        inputBuilder.setStopTime(getDateAndTime(stopTime.get()));
                    } else {
                        throw new IllegalArgumentException("stopTime must be later than startTime");
                    }
                }
            } else {
                throw new IllegalArgumentException("Replay not supported by this stream.");
            }
        }
        return invokeCreateSubscription(inputBuilder.build());
    }

    @Override
    public ListenableFuture<? extends DOMRpcResult> invokeCreateSubscription(Stream... streams) {
        ListenableFuture<? extends DOMRpcResult> res;
        if (streams.length == 0) {
            return invokeCreateSubscription(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        } else if (streams.length == 1) {
            return invokeCreateSubscription(Optional.of(streams[0]), Optional.empty(), Optional.empty(),
                    Optional.empty());
        } else {
            for (Stream stream : streams) {
                res = invokeCreateSubscription(Optional.of(stream), Optional.empty(), Optional.empty(),
                        Optional.empty());
                try {
                    if (!res.get().getErrors().isEmpty()) {
                        return res;
                    }
                } catch (InterruptedException e) {
                    LOG.warn("InterruptedException during rpc call", e);
                    Thread.currentThread().interrupt();
                    return res;
                } catch (ExecutionException e) {
                    LOG.warn("ExecutionException during rpc call", e);
                    return res;
                }
            }
        }
        throw new IllegalStateException("Could never be reached"); //avoid eclipse error
    }

    @Override
    public @NonNull Map<StreamKey, Stream> getNotificationStreamsAsMap() {
        Optional<Streams> oStreams = readData(LogicalDatastoreType.OPERATIONAL, STREAMS_PATH, Streams.class);
        return oStreams.map(Streams::nonnullStream).orElse(Collections.emptyMap());
    }

	/*
	 * @Override public BindingNormalizedNodeSerializer
	 * getBindingNormalizedNodeSerializer() { return serializer; }
	 */
    private DateAndTime getDateAndTime(Instant dateTime) {
        final String formattedDate = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dateTime);
        return new DateAndTime(formattedDate);
    }
}
