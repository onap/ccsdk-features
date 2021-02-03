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

import com.google.common.util.concurrent.FluentFuture;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.access.NetconfAccessorImpl;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
//import org.opendaylight.mdsal.binding.dom.codec.impl.BindingCodecContext;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMDataTreeReadTransaction;
import org.opendaylight.mdsal.dom.api.DOMMountPoint;
import org.opendaylight.mdsal.dom.api.DOMNotificationListener;
import org.opendaylight.mdsal.dom.api.DOMNotificationService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetconfDomAccessorImpl extends NetconfAccessorImpl implements NetconfDomAccessor {

    private static final Logger LOG = LoggerFactory.getLogger(NetconfDomAccessorImpl.class);

    private final DOMDataBroker dataBroker;
    private final DOMMountPoint mountpoint;
    private final DomContext domContext;

    public NetconfDomAccessorImpl(NetconfAccessorImpl accessor, DOMDataBroker domDataBroker, DOMMountPoint mountPoint,
            DomContext domContext) {
        super(accessor);
        this.dataBroker = Objects.requireNonNull(domDataBroker);
        this.mountpoint = Objects.requireNonNull(mountPoint);
        this.domContext = Objects.requireNonNull(domContext);
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
    public @NonNull <T extends DOMNotificationListener> ListenerRegistration<DOMNotificationListener> doRegisterNotificationListener(
            @NonNull T listener, Collection<SchemaPath> types) {
        LOG.info("Begin register listener for Mountpoint {}", mountpoint.getIdentifier().toString());
        final Optional<DOMNotificationService> optionalNotificationService =
                mountpoint.getService(DOMNotificationService.class);
        if (optionalNotificationService.isPresent()) {
            final ListenerRegistration<DOMNotificationListener> ranListenerRegistration =
                    optionalNotificationService.get().registerNotificationListener(listener, types);
            LOG.info("End registration listener for Mountpoint {} Listener: {} Result: {}",
                    mountpoint.getIdentifier().toString(), optionalNotificationService, ranListenerRegistration);
            return ranListenerRegistration;
        }
        throw new IllegalArgumentException("Can not get notification service");
    }

    @Override
    public @NonNull <T extends DOMNotificationListener> ListenerRegistration<DOMNotificationListener> doRegisterNotificationListener(
            @NonNull T listener, SchemaPath[] types) {
        return doRegisterNotificationListener(listener, Arrays.asList(types));
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
    public Optional<NormalizedNode<?, ?>> readDataNode(LogicalDatastoreType dataStoreType,
            YangInstanceIdentifier path) {
        LOG.debug("Read to node datastore:{} path:{}", dataStoreType, path);

        try (DOMDataTreeReadTransaction readOnlyTransaction = dataBroker.newReadOnlyTransaction()) {
            FluentFuture<Optional<NormalizedNode<?, ?>>> foData = readOnlyTransaction.read(dataStoreType, path);
            // RAVI - Add a few debug here, like what ? Speak to Micha....
            
            Optional<NormalizedNode<?, ?>> data = foData.get(120, TimeUnit.SECONDS);
            LOG.info("read is done - {} ", foData.isDone());
            return data;
            
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOG.info("Incomplete read to node transaction {} {}", dataStoreType, path, e);
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends DataObject> Optional<T> convertNormalizedNode(BindingNormalizedNodeSerializer serializer,
            Optional<NormalizedNode<?, ?>> oData, YangInstanceIdentifier path, Class<T> clazz)
            throws CanNotConvertException {
        if (oData.isPresent()) {
            NormalizedNode<?, ?> data = oData.get();
            LOG.debug("data identifier: {}", data.getIdentifier());
            @Nullable
            Entry<InstanceIdentifier<?>, DataObject> entry = serializer.fromNormalizedNode(path, data);
            if (entry != null) {
                LOG.debug("object identifier: {}", entry.getKey());
                DataObject value = entry.getValue();
                if (clazz.isInstance(value)) {
                    return Optional.of((T) value);
                } else {
                    throw new CanNotConvertException("Unexpected class. Expected:" + clazz.getName() + " provided:"
                            + value.getClass().getName() + " Nodetype:" + data.getNodeType());
                }
            } else {
                throw new CanNotConvertException(
                        "No object created for path:" + path + " Nodetype:" + data.getNodeType());
            }
        } else {
            throw new CanNotConvertException("No data received for path:" + path);
        }
    }
}
