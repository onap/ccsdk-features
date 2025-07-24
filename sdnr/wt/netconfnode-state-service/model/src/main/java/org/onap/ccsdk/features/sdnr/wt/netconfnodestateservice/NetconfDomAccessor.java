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
package org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice;

import com.google.common.util.concurrent.ListenableFuture;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMMountPoint;
import org.opendaylight.mdsal.dom.api.DOMNotificationListener;
import org.opendaylight.mdsal.dom.api.DOMRpcResult;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf.notification._1._0.rev080714.CreateSubscriptionInput;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf.notification._1._0.rev080714.create.subscription.input.Filter;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netmod.notification.rev080714.netconf.streams.Stream;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netmod.notification.rev080714.netconf.streams.StreamKey;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

/**
 * Interface handling netconf connection.
 */
public interface NetconfDomAccessor extends NetconfAccessor {

    /**
     * @return the dataBroker
     */
    DOMDataBroker getDataBroker();

    /**
     * @return the MDSAL Mountpoint service
     */
    DOMMountPoint getMountpoint();

    /**
     * @return DOMRpcService
     */
    DOMRpcService getRpcService();

    /**
     * @return DomContext
     */
    DomContext getDomContext();

    /**
     * Deliver the data into a class
     *
     * @param <T> DataObject type
     * @param dataStoreType config or operational database
     * @param path data path
     * @return Optional<T> with object
     */
    <T extends DataObject> Optional<T> readData(LogicalDatastoreType dataStoreType, YangInstanceIdentifier path,
            Class<T> clazz);

    /**
     * Read data from device
     *
     * @param dataStoreType
     * @param path
     * @return NormalizedNode<?, ?> with data
     */
    Optional<NormalizedNode> readDataNode(LogicalDatastoreType dataStoreType, YangInstanceIdentifier path);

    /**
     * Read data from Controller node - controller-config
     *
     * @param dataStoreType
     * @param path
     * @return NormalizedNode<?, ?> with data
     */
    Optional<NormalizedNode> readControllerDataNode(LogicalDatastoreType dataStoreType,
            YangInstanceIdentifier path);


    /**
     * Register netconf notification listener for related mountpoint
     *
     * @param <T> specific child class of DOMNotificationListener
     * @param listener listener to be called
     * @param types as list of SchemaPath
     * @return handler to manager registration
     */
    <T extends DOMNotificationListener> @NonNull Registration doRegisterNotificationListener(
            @NonNull T listener, Collection<Absolute> types);

    /**
     * Register netconf notification listener for related mountpoint
     *
     * @See <a href="https://tools.ietf.org/html/rfc5277">https://tools.ietf.org/html/rfc5277</a>
     * @param <T>
     * @param listener to be registers
     * @param types as array of SchemaPath
     * @return Object to close and access
     */
    <T extends DOMNotificationListener> @NonNull Registration doRegisterNotificationListener(
            @NonNull T listener, Absolute[] types);

    /**
     * Register netconf notification listener for related mountpoint
     *
     * @param <T>
     * @param listener to be registers
     * @param types as array of QName
     * @return Object to close and access
     */
    <T extends DOMNotificationListener> @NonNull Registration doRegisterNotificationListener(
            @NonNull T listener, QName[] types);

    /**
     * Request notification streams from device.
     *
     * @return provided streams.
     */
    @NonNull
    Map<StreamKey, Stream> getNotificationStreamsAsMap();

    /**
     * Send out a NETCONF create-subscription for one notification stream.
     *
     * @See <a href="https://tools.ietf.org/html/rfc5277">https://tools.ietf.org/html/rfc5277</a>
     * @param input with CreateSubscriptionInput
     * @return RpcMessage for the RPC call.
     */
    ListenableFuture<? extends DOMRpcResult> invokeCreateSubscription(CreateSubscriptionInput input);

    /**
     * Send out a NETCONF create-subscription for one notification stream, using parameters.
     *
     * @See <a href="https://tools.ietf.org/html/rfc5277">https://tools.ietf.org/html/rfc5277</a>
     * @param oStream Optional Stream
     * @param filter Optional Filter
     * @param startTime startTime according the RFC
     * @param stopTime stopTime according the RFC
     * @return
     */
    ListenableFuture<? extends DOMRpcResult> invokeCreateSubscription(Optional<Stream> oStream, Optional<Filter> filter,
            Optional<Instant> startTime, Optional<Instant> stopTime);

    /**
     * Send out a NETCONF create-subscription for a list of streams, not offering replay options.
     *
     * @param streams is a list of stream with 0..n elements.
     * @return if ok last rpc call result, if notok the result provided by rpc call providing error response.
     */
    ListenableFuture<? extends DOMRpcResult> invokeCreateSubscription(Stream... streams);

    /**
     * Get NETCONF object to serialize between GenericNodes and Java classes
     *
     * @return serialization object.
     */
    //BindingNormalizedNodeSerializer getBindingNormalizedNodeSerializer();
}
