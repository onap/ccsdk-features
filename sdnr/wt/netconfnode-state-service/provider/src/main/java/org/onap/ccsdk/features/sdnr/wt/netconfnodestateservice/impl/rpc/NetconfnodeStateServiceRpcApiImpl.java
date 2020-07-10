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
package org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.rpc;

import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.VesNotificationListener;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netconfnode.state.rev191011.AttributeChangeNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netconfnode.state.rev191011.FaultNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netconfnode.state.rev191011.GetStatusInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netconfnode.state.rev191011.GetStatusOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netconfnode.state.rev191011.GetStatusOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netconfnode.state.rev191011.NetconfnodeStateService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netconfnode.state.rev191011.PushAttributeChangeNotificationInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netconfnode.state.rev191011.PushAttributeChangeNotificationOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netconfnode.state.rev191011.PushFaultNotificationInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netconfnode.state.rev191011.PushFaultNotificationOutput;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;

public class NetconfnodeStateServiceRpcApiImpl implements NetconfnodeStateService, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(NetconfnodeStateServiceRpcApiImpl.class);

    private final ObjectRegistration<NetconfnodeStateServiceRpcApiImpl> rpcReg;
    private RpcApigetStateCallback getStatusCallback;
    private final List<VesNotificationListener> vesNotificationListenerList;

    public NetconfnodeStateServiceRpcApiImpl(final RpcProviderService rpcProviderRegistry,
            List<VesNotificationListener> vesNotificationListenerList) {

        this.vesNotificationListenerList = vesNotificationListenerList;

        // Register ourselves as the REST API RPC implementation
        LOG.info("Register RPC Service " + NetconfnodeStateService.class.getSimpleName());
        rpcReg = rpcProviderRegistry.registerRpcImplementation(NetconfnodeStateService.class, this);
        this.getStatusCallback = null;
    }

    public NetconfnodeStateServiceRpcApiImpl setStatusCallback(RpcApigetStateCallback getStatusCallback) {
        this.getStatusCallback = getStatusCallback;
        return this;
    }

    @Override
    public void close() throws Exception {
        LOG.info("Close RPC Service");
        if (rpcReg != null) {
            rpcReg.close();
        }
    }

    /*-------------------------------
     * Interfaces for getting information
     */
    @Override
    public ListenableFuture<RpcResult<GetStatusOutput>> getStatus(GetStatusInput input) {

        LOG.info("RPC Request: getStatus input: {}", input);
        RpcResultBuilder<GetStatusOutput> result;

        try {
            GetStatusOutputBuilder outputBuilder = new GetStatusOutputBuilder();
            getStatusCallback.getStatus(input);
            result = RpcResultBuilder.success(outputBuilder);
        } catch (Exception e) {
            result = RpcResultBuilder.failed();
            result.withError(ErrorType.APPLICATION, "Exception", e);
        }
        return result.buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<PushFaultNotificationOutput>> pushFaultNotification(
            PushFaultNotificationInput input) {

        RpcResultBuilder<PushFaultNotificationOutput> result;
        try {
            FaultNotificationBuilder faultNotificationBuilder = new FaultNotificationBuilder();
            faultNotificationBuilder.fieldsFrom(input);
            vesNotificationListenerList.forEach(item -> item.onNotification(faultNotificationBuilder.build()));
            result = RpcResultBuilder.success();
        } catch (Exception e) {
            result = RpcResultBuilder.failed();
            result.withError(ErrorType.APPLICATION, "Exception", e);
        }
        return result.buildFuture();
    }

    @Override
    public ListenableFuture<RpcResult<PushAttributeChangeNotificationOutput>> pushAttributeChangeNotification(
            PushAttributeChangeNotificationInput input) {
        RpcResultBuilder<PushAttributeChangeNotificationOutput> result;
        try {
            AttributeChangeNotificationBuilder attributeChangeNotificationBuilder =
                    new AttributeChangeNotificationBuilder();
            attributeChangeNotificationBuilder.fieldsFrom(input);
            vesNotificationListenerList
                    .forEach(item -> item.onNotification(attributeChangeNotificationBuilder.build()));
            result = RpcResultBuilder.success();
        } catch (Exception e) {
            result = RpcResultBuilder.failed();
            result.withError(ErrorType.APPLICATION, "Exception", e);
        }
        return result.buildFuture();
    }
}
