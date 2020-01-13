/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 			reserved.
 * Modifications Copyright (C) 2020 Nordix Foundation.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.features.sdnr.northbound.a1Adapter;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.onap.ccsdk.sli.core.sli.provider.MdsalHelper;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.A1ADAPTERAPIService;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.CreatePolicyInstanceInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.CreatePolicyInstanceInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.CreatePolicyInstanceOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.CreatePolicyInstanceOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.CreatePolicyTypeInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.CreatePolicyTypeInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.CreatePolicyTypeOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.CreatePolicyTypeOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.DeletePolicyInstanceInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.DeletePolicyInstanceInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.DeletePolicyInstanceOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.DeletePolicyInstanceOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.DeletePolicyTypeInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.DeletePolicyTypeInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.DeletePolicyTypeOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.DeletePolicyTypeOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetHealthCheckInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetHealthCheckInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetHealthCheckOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetHealthCheckOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetNearRTRICsInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetNearRTRICsInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetNearRTRICsOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetNearRTRICsOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyInstanceInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyInstanceInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyInstanceOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyInstanceOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyInstancesInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyInstancesInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyInstancesOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyInstancesOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyTypeInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyTypeInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyTypeOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyTypeOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyTypesInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyTypesInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyTypesOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetPolicyTypesOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetStatusInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetStatusInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetStatusOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.GetStatusOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.NotifyPolicyEnforcementUpdateInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.NotifyPolicyEnforcementUpdateInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.NotifyPolicyEnforcementUpdateOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev191212.NotifyPolicyEnforcementUpdateOutputBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines a base implementation for your provider. This class extends from a helper class which
 * provides storage for the most commonly used components of the MD-SAL. Additionally the base class
 * provides some basic logging and initialization / clean up methods.
 *
 */
public class A1AdapterProvider implements AutoCloseable, A1ADAPTERAPIService {

    private static final Logger LOG = LoggerFactory.getLogger(A1AdapterProvider.class);

    private static final String APPLICATION_NAME = "a1Adapter-api";
    private static final String GET_NEARRT_RICS = "getNearRT-RICs";
    private static final String GET_HEALTH_CHECK = "getHealthCheck";
    private static final String GET_POLICY_TYPES = "getPolicyTypes";
    private static final String CREATE_POLICY_TYPE = "createPolicyType";
    private static final String GET_POLICY_TYPE = "getPolicyType";
    private static final String DELETE_POLICY_TYPE = "deletePolicyType";
    private static final String GET_POLICY_INSTANCES = "getPolicyInstances";
    private static final String CREATE_POLICY_INSTANCES = "createPolicyInstance";
    private static final String GET_POLICY_INSTANCE = "getPolicyInstance";
    private static final String DELETE_POLICY_INSTANCE = "deletePolicyInstance";
    private static final String GET_STATUS = "getStatus";
    private static final String NOTIFICATION_ENFORECEMENT = "notifyPolicyEnforcementUpdate";

    private final ExecutorService executor;
    protected DataBroker dataBroker;
    protected NotificationPublishService notificationService;
    protected RpcProviderRegistry rpcRegistry;
    protected BindingAwareBroker.RpcRegistration<A1ADAPTERAPIService> rpcRegistration;
    private final A1AdapterClient A1AdapterClient;

    public A1AdapterProvider(final DataBroker dataBroker, final NotificationPublishService notificationPublishService,
            final RpcProviderRegistry rpcProviderRegistry, final A1AdapterClient A1AdapterClient) {

        LOG.info("Creating provider for {}", APPLICATION_NAME);
        executor = Executors.newFixedThreadPool(1);
        this.dataBroker = dataBroker;
        this.notificationService = notificationPublishService;
        this.rpcRegistry = rpcProviderRegistry;
        this.A1AdapterClient = A1AdapterClient;
        initialize();
    }

    public void initialize() {
        LOG.info("Initializing provider for {}", APPLICATION_NAME);
        rpcRegistration = rpcRegistry.addRpcImplementation(A1ADAPTERAPIService.class, this);
        LOG.info("Initialization complete for {}", APPLICATION_NAME);
    }

    protected void initializeChild() {
        // Override if you have custom initialization intelligence
    }

    @Override
    public void close() throws Exception {
        LOG.info("Closing provider for {}", APPLICATION_NAME);
        executor.shutdown();
        rpcRegistration.close();
        LOG.info("Successfully closed provider for {}", APPLICATION_NAME);
    }

    // RPC getNearRT-RICs

    @Override
    public ListenableFuture<RpcResult<GetNearRTRICsOutput>> getNearRTRICs(GetNearRTRICsInput input) {
        final String svcOperation = "getNearRT-RICs";

        Properties parms = new Properties();
        GetNearRTRICsOutputBuilder serviceDataBuilder = (GetNearRTRICsOutputBuilder) getServiceData(GET_NEARRT_RICS);

        LOG.info("Reached RPC getNearRT-RICs");

        LOG.info(svcOperation + " called.");

        if (input == null) {
            LOG.debug("exiting " + svcOperation + " because of invalid input");
            serviceDataBuilder.setResponseCode("Input is null");
            RpcResult<GetNearRTRICsOutput> rpcResult =
                    RpcResultBuilder.<GetNearRTRICsOutput>status(true).withResult(serviceDataBuilder.build()).build();
            return Futures.immediateFuture(rpcResult);
        }

        // add input to parms
        LOG.info("Adding INPUT data for " + svcOperation + " input: " + input);
        GetNearRTRICsInputBuilder inputBuilder = new GetNearRTRICsInputBuilder(input);
        MdsalHelper.toProperties(parms, inputBuilder.build());

        LOG.info("Printing SLI parameters to be passed");

        // iterate properties file to get key-value pairs
        for (String key : parms.stringPropertyNames()) {
            String value = parms.getProperty(key);
            LOG.info("The SLI parameter in " + key + " is: " + value);
        }

        // Call SLI sync method
        try {
            if (A1AdapterClient.hasGraph("A1-ADAPTER-API", svcOperation, null, "sync")) {
                LOG.info("A1AdapterClient has a Directed Graph for '" + svcOperation + "'");
                try {
                    A1AdapterClient.execute("A1-ADAPTER-API", svcOperation, null, "sync", serviceDataBuilder, parms);
                } catch (Exception e) {
                    LOG.error("Caught exception executing service logic for " + svcOperation, e);
                    serviceDataBuilder.setResponseCode("500");
                }
            } else {
                LOG.error("No service logic active for A1Adapter: '" + svcOperation + "'");
                serviceDataBuilder.setResponseCode("503");
            }
        } catch (Exception e) {
            LOG.error("Caught exception looking for service logic", e);
            serviceDataBuilder.setResponseCode("500");
        }

        String errorCode = serviceDataBuilder.getResponseCode();

        if (!("0".equals(errorCode) || "200".equals(errorCode))) {
            LOG.error("Returned FAILED for " + svcOperation + " error code: '" + errorCode + "'");
        } else {
            LOG.info("Returned SUCCESS for " + svcOperation + " ");
            serviceDataBuilder.setResponseCode("A1 Adapter Executed for GetNearRTRICs ");
        }

        RpcResult<GetNearRTRICsOutput> rpcResult =
                RpcResultBuilder.<GetNearRTRICsOutput>status(true).withResult(serviceDataBuilder.build()).build();

        LOG.info("Successful exit from getNearRT-RICs ");

        return Futures.immediateFuture(rpcResult);
    }

    // RPC getHealthCheck

    @Override
    public ListenableFuture<RpcResult<GetHealthCheckOutput>> getHealthCheck(GetHealthCheckInput input) {
        final String svcOperation = "getHealthCheck";

        Properties parms = new Properties();
        GetHealthCheckOutputBuilder serviceDataBuilder = (GetHealthCheckOutputBuilder) getServiceData(GET_HEALTH_CHECK);

        LOG.info("Reached RPC getHealthCheck");

        LOG.info(svcOperation + " called.");

        if (input == null) {
            LOG.debug("exiting " + svcOperation + " because of invalid input");
            serviceDataBuilder.setResponseCode("Input is null");
            RpcResult<GetHealthCheckOutput> rpcResult =
                    RpcResultBuilder.<GetHealthCheckOutput>status(true).withResult(serviceDataBuilder.build()).build();
            return Futures.immediateFuture(rpcResult);
        }

        // add input to parms
        LOG.info("Adding INPUT data for " + svcOperation + " input: " + input);
        GetHealthCheckInputBuilder inputBuilder = new GetHealthCheckInputBuilder(input);
        MdsalHelper.toProperties(parms, inputBuilder.build());

        LOG.info("Printing SLI parameters to be passed");

        // iterate properties file to get key-value pairs
        for (String key : parms.stringPropertyNames()) {
            String value = parms.getProperty(key);
            LOG.info("The SLI parameter in " + key + " is: " + value);
        }

        // Call SLI sync method
        try {
            if (A1AdapterClient.hasGraph("A1-ADAPTER-API", svcOperation, null, "sync")) {
                LOG.info("A1AdapterClient has a Directed Graph for '" + svcOperation + "'");
                try {
                    A1AdapterClient.execute("A1-ADAPTER-API", svcOperation, null, "sync", serviceDataBuilder, parms);
                } catch (Exception e) {
                    LOG.error("Caught exception executing service logic for " + svcOperation, e);
                    serviceDataBuilder.setResponseCode("500");
                }
            } else {
                LOG.error("No service logic active for A1Adapter: '" + svcOperation + "'");
                serviceDataBuilder.setResponseCode("503");
            }
        } catch (Exception e) {
            LOG.error("Caught exception looking for service logic", e);
            serviceDataBuilder.setResponseCode("500");
        }

        String errorCode = serviceDataBuilder.getResponseCode();

        if (!("0".equals(errorCode) || "200".equals(errorCode))) {
            LOG.error("Returned FAILED for " + svcOperation + " error code: '" + errorCode + "'");
        } else {
            LOG.info("Returned SUCCESS for " + svcOperation + " ");
            serviceDataBuilder.setResponseCode("A1 Adapter Executed for GetHealthCheck. ");
        }

        RpcResult<GetHealthCheckOutput> rpcResult =
                RpcResultBuilder.<GetHealthCheckOutput>status(true).withResult(serviceDataBuilder.build()).build();

        LOG.info("Successful exit from getHealthCheck ");

        return Futures.immediateFuture(rpcResult);
    }

    // RPC getPolicyTypes

    @Override
    public ListenableFuture<RpcResult<GetPolicyTypesOutput>> getPolicyTypes(GetPolicyTypesInput input) {
        final String svcOperation = "getPolicyTypes";

        Properties parms = new Properties();
        GetPolicyTypesOutputBuilder serviceDataBuilder = (GetPolicyTypesOutputBuilder) getServiceData(GET_POLICY_TYPES);

        LOG.info("Reached RPC getPolicyTypes");

        LOG.info(svcOperation + " called.");

        if (input == null) {
            LOG.debug("exiting " + svcOperation + " because of invalid input");
            serviceDataBuilder.setResponseCode("Input is null");
            RpcResult<GetPolicyTypesOutput> rpcResult =
                    RpcResultBuilder.<GetPolicyTypesOutput>status(true).withResult(serviceDataBuilder.build()).build();
            return Futures.immediateFuture(rpcResult);
        }

        // add input to parms
        LOG.info("Adding INPUT data for " + svcOperation + " input: " + input);
        GetPolicyTypesInputBuilder inputBuilder = new GetPolicyTypesInputBuilder(input);
        MdsalHelper.toProperties(parms, inputBuilder.build());

        LOG.info("Printing SLI parameters to be passed");

        // iterate properties file to get key-value pairs
        for (String key : parms.stringPropertyNames()) {
            String value = parms.getProperty(key);
            LOG.info("The SLI parameter in " + key + " is: " + value);
        }

        // Call SLI sync method
        try {
            if (A1AdapterClient.hasGraph("A1-ADAPTER-API", svcOperation, null, "sync")) {
                LOG.info("A1AdapterClient has a Directed Graph for '" + svcOperation + "'");
                try {
                    A1AdapterClient.execute("A1-ADAPTER-API", svcOperation, null, "sync", serviceDataBuilder, parms);
                } catch (Exception e) {
                    LOG.error("Caught exception executing service logic for " + svcOperation, e);
                    serviceDataBuilder.setResponseCode("500");
                }
            } else {
                LOG.error("No service logic active for A1Adapter: '" + svcOperation + "'");
                serviceDataBuilder.setResponseCode("503");
            }
        } catch (Exception e) {
            LOG.error("Caught exception looking for service logic", e);
            serviceDataBuilder.setResponseCode("500");
        }

        String errorCode = serviceDataBuilder.getResponseCode();

        if (!("0".equals(errorCode) || "200".equals(errorCode))) {
            LOG.error("Returned FAILED for " + svcOperation + " error code: '" + errorCode + "'");
        } else {
            LOG.info("Returned SUCCESS for " + svcOperation + " ");
            serviceDataBuilder.setResponseCode("A1 Adapter Executed for GetPolicyTypes ");
        }

        RpcResult<GetPolicyTypesOutput> rpcResult =
                RpcResultBuilder.<GetPolicyTypesOutput>status(true).withResult(serviceDataBuilder.build()).build();

        LOG.info("Successful exit from getPolicyTypes ");

        return Futures.immediateFuture(rpcResult);
    }

    // RPC createPolicyType

    @Override
    public ListenableFuture<RpcResult<CreatePolicyTypeOutput>> createPolicyType(CreatePolicyTypeInput input) {
        final String svcOperation = "createPolicyType";

        Properties parms = new Properties();
        CreatePolicyTypeOutputBuilder serviceDataBuilder =
                (CreatePolicyTypeOutputBuilder) getServiceData(CREATE_POLICY_TYPE);

        LOG.info("Reached RPC createPolicyType");

        LOG.info(svcOperation + " called.");

        if (input == null) {
            LOG.debug("exiting " + svcOperation + " because of invalid input");
            serviceDataBuilder.setResponseCode("Input is null");
            RpcResult<CreatePolicyTypeOutput> rpcResult = RpcResultBuilder.<CreatePolicyTypeOutput>status(true)
                    .withResult(serviceDataBuilder.build()).build();
            return Futures.immediateFuture(rpcResult);
        }

        // add input to parms
        LOG.info("Adding INPUT data for " + svcOperation + " input: " + input);
        CreatePolicyTypeInputBuilder inputBuilder = new CreatePolicyTypeInputBuilder(input);
        MdsalHelper.toProperties(parms, inputBuilder.build());

        LOG.info("Printing SLI parameters to be passed");

        // iterate properties file to get key-value pairs
        for (String key : parms.stringPropertyNames()) {
            String value = parms.getProperty(key);
            LOG.info("The SLI parameter in " + key + " is: " + value);
        }

        // Call SLI sync method
        try {
            if (A1AdapterClient.hasGraph("A1-ADAPTER-API", svcOperation, null, "sync")) {
                LOG.info("A1AdapterClient has a Directed Graph for '" + svcOperation + "'");
                try {
                    A1AdapterClient.execute("A1-ADAPTER-API", svcOperation, null, "sync", serviceDataBuilder, parms);
                } catch (Exception e) {
                    LOG.error("Caught exception executing service logic for " + svcOperation, e);
                    serviceDataBuilder.setResponseCode("500");
                }
            } else {
                LOG.error("No service logic active for A1Adapter: '" + svcOperation + "'");
                serviceDataBuilder.setResponseCode("503");
            }
        } catch (Exception e) {
            LOG.error("Caught exception looking for service logic", e);
            serviceDataBuilder.setResponseCode("500");
        }

        String errorCode = serviceDataBuilder.getResponseCode();

        if (!("0".equals(errorCode) || "200".equals(errorCode))) {
            LOG.error("Returned FAILED for " + svcOperation + " error code: '" + errorCode + "'");
        } else {
            LOG.info("Returned SUCCESS for " + svcOperation + " ");
            serviceDataBuilder.setResponseCode("A1 Adapter Executed for CreatePolicyType");
        }

        RpcResult<CreatePolicyTypeOutput> rpcResult =
                RpcResultBuilder.<CreatePolicyTypeOutput>status(true).withResult(serviceDataBuilder.build()).build();

        LOG.info("Successful exit from createPolicyType ");

        return Futures.immediateFuture(rpcResult);
    }

    // RPC getPolicyType

    @Override
    public ListenableFuture<RpcResult<GetPolicyTypeOutput>> getPolicyType(GetPolicyTypeInput input) {
        final String svcOperation = "getPolicyType";

        Properties parms = new Properties();
        GetPolicyTypeOutputBuilder serviceDataBuilder = (GetPolicyTypeOutputBuilder) getServiceData(GET_POLICY_TYPE);

        LOG.info("Reached RPC getPolicyType");

        LOG.info(svcOperation + " called.");

        if (input == null) {
            LOG.debug("exiting " + svcOperation + " because of invalid input");
            serviceDataBuilder.setResponseCode("Input is null");
            RpcResult<GetPolicyTypeOutput> rpcResult =
                    RpcResultBuilder.<GetPolicyTypeOutput>status(true).withResult(serviceDataBuilder.build()).build();
            return Futures.immediateFuture(rpcResult);
        }

        // add input to parms
        LOG.info("Adding INPUT data for " + svcOperation + " input: " + input);
        GetPolicyTypeInputBuilder inputBuilder = new GetPolicyTypeInputBuilder(input);
        MdsalHelper.toProperties(parms, inputBuilder.build());

        LOG.info("Printing SLI parameters to be passed");

        // iterate properties file to get key-value pairs
        for (String key : parms.stringPropertyNames()) {
            String value = parms.getProperty(key);
            LOG.info("The SLI parameter in " + key + " is: " + value);
        }

        // Call SLI sync method
        try {
            if (A1AdapterClient.hasGraph("A1-ADAPTER-API", svcOperation, null, "sync")) {
                LOG.info("A1AdapterClient has a Directed Graph for '" + svcOperation + "'");
                try {
                    A1AdapterClient.execute("A1-ADAPTER-API", svcOperation, null, "sync", serviceDataBuilder, parms);
                } catch (Exception e) {
                    LOG.error("Caught exception executing service logic for " + svcOperation, e);
                    serviceDataBuilder.setResponseCode("500");
                }
            } else {
                LOG.error("No service logic active for A1Adapter: '" + svcOperation + "'");
                serviceDataBuilder.setResponseCode("503");
            }
        } catch (Exception e) {
            LOG.error("Caught exception looking for service logic", e);
            serviceDataBuilder.setResponseCode("500");
        }

        String errorCode = serviceDataBuilder.getResponseCode();

        if (!("0".equals(errorCode) || "200".equals(errorCode))) {
            LOG.error("Returned FAILED for " + svcOperation + " error code: '" + errorCode + "'");
        } else {
            LOG.info("Returned SUCCESS for " + svcOperation + " ");
            serviceDataBuilder.setResponseCode("A1 Adapter Executed for GetPolicyType. ");
        }

        RpcResult<GetPolicyTypeOutput> rpcResult =
                RpcResultBuilder.<GetPolicyTypeOutput>status(true).withResult(serviceDataBuilder.build()).build();

        LOG.info("Successful exit from getPolicyType ");

        return Futures.immediateFuture(rpcResult);
    }

    // RPC deletePolicyType

    @Override
    public ListenableFuture<RpcResult<DeletePolicyTypeOutput>> deletePolicyType(DeletePolicyTypeInput input) {
        final String svcOperation = "deletePolicyType";

        Properties parms = new Properties();
        DeletePolicyTypeOutputBuilder serviceDataBuilder =
                (DeletePolicyTypeOutputBuilder) getServiceData(DELETE_POLICY_TYPE);

        LOG.info("Reached RPC deletePolicyType");

        LOG.info(svcOperation + " called.");

        if (input == null) {
            LOG.debug("exiting " + svcOperation + " because of invalid input");
            serviceDataBuilder.setResponseCode("Input is null");
            RpcResult<DeletePolicyTypeOutput> rpcResult = RpcResultBuilder.<DeletePolicyTypeOutput>status(true)
                    .withResult(serviceDataBuilder.build()).build();
            return Futures.immediateFuture(rpcResult);
        }

        // add input to parms
        LOG.info("Adding INPUT data for " + svcOperation + " input: " + input);
        DeletePolicyTypeInputBuilder inputBuilder = new DeletePolicyTypeInputBuilder(input);
        MdsalHelper.toProperties(parms, inputBuilder.build());

        LOG.info("Printing SLI parameters to be passed");

        // iterate properties file to get key-value pairs
        for (String key : parms.stringPropertyNames()) {
            String value = parms.getProperty(key);
            LOG.info("The SLI parameter in " + key + " is: " + value);
        }

        // Call SLI sync method
        try {
            if (A1AdapterClient.hasGraph("A1-ADAPTER-API", svcOperation, null, "sync")) {
                LOG.info("A1AdapterClient has a Directed Graph for '" + svcOperation + "'");
                try {
                    A1AdapterClient.execute("A1-ADAPTER-API", svcOperation, null, "sync", serviceDataBuilder, parms);
                } catch (Exception e) {
                    LOG.error("Caught exception executing service logic for " + svcOperation, e);
                    serviceDataBuilder.setResponseCode("500");
                }
            } else {
                LOG.error("No service logic active for A1Adapter: '" + svcOperation + "'");
                serviceDataBuilder.setResponseCode("503");
            }
        } catch (Exception e) {
            LOG.error("Caught exception looking for service logic", e);
            serviceDataBuilder.setResponseCode("500");
        }

        String errorCode = serviceDataBuilder.getResponseCode();

        if (!("0".equals(errorCode) || "200".equals(errorCode))) {
            LOG.error("Returned FAILED for " + svcOperation + " error code: '" + errorCode + "'");
        } else {
            LOG.info("Returned SUCCESS for " + svcOperation + " ");
            serviceDataBuilder.setResponseCode("A1 Adapter Executed for DeletePolicyType ");
        }

        RpcResult<DeletePolicyTypeOutput> rpcResult =
                RpcResultBuilder.<DeletePolicyTypeOutput>status(true).withResult(serviceDataBuilder.build()).build();

        LOG.info("Successful exit from deletePolicyType ");

        return Futures.immediateFuture(rpcResult);
    }

    // RPC getPolicyInstances

    @Override
    public ListenableFuture<RpcResult<GetPolicyInstancesOutput>> getPolicyInstances(GetPolicyInstancesInput input) {
        final String svcOperation = "getPolicyInstances";

        Properties parms = new Properties();
        GetPolicyInstancesOutputBuilder serviceDataBuilder =
                (GetPolicyInstancesOutputBuilder) getServiceData(GET_POLICY_INSTANCES);

        LOG.info("Reached RPC getPolicyInstances");

        LOG.info(svcOperation + " called.");

        if (input == null) {
            LOG.debug("exiting " + svcOperation + " because of invalid input");
            serviceDataBuilder.setResponseCode("Input is null");
            RpcResult<GetPolicyInstancesOutput> rpcResult = RpcResultBuilder.<GetPolicyInstancesOutput>status(true)
                    .withResult(serviceDataBuilder.build()).build();
            return Futures.immediateFuture(rpcResult);
        }

        // add input to parms
        LOG.info("Adding INPUT data for " + svcOperation + " input: " + input);
        GetPolicyInstancesInputBuilder inputBuilder = new GetPolicyInstancesInputBuilder(input);
        MdsalHelper.toProperties(parms, inputBuilder.build());

        LOG.info("Printing SLI parameters to be passed");

        // iterate properties file to get key-value pairs
        for (String key : parms.stringPropertyNames()) {
            String value = parms.getProperty(key);
            LOG.info("The SLI parameter in " + key + " is: " + value);
        }

        // Call SLI sync method
        try {
            if (A1AdapterClient.hasGraph("A1-ADAPTER-API", svcOperation, null, "sync")) {
                LOG.info("A1AdapterClient has a Directed Graph for '" + svcOperation + "'");
                try {
                    A1AdapterClient.execute("A1-ADAPTER-API", svcOperation, null, "sync", serviceDataBuilder, parms);
                } catch (Exception e) {
                    LOG.error("Caught exception executing service logic for " + svcOperation, e);
                    serviceDataBuilder.setResponseCode("500");
                }
            } else {
                LOG.error("No service logic active for A1Adapter: '" + svcOperation + "'");
                serviceDataBuilder.setResponseCode("503");
            }
        } catch (Exception e) {
            LOG.error("Caught exception looking for service logic", e);
            serviceDataBuilder.setResponseCode("500");
        }

        String errorCode = serviceDataBuilder.getResponseCode();

        if (!("0".equals(errorCode) || "200".equals(errorCode))) {
            LOG.error("Returned FAILED for " + svcOperation + " error code: '" + errorCode + "'");
        } else {
            LOG.info("Returned SUCCESS for " + svcOperation + " ");
            serviceDataBuilder.setResponseCode("A1 Adapter Executed for GetPolicyInstances ");
        }

        RpcResult<GetPolicyInstancesOutput> rpcResult =
                RpcResultBuilder.<GetPolicyInstancesOutput>status(true).withResult(serviceDataBuilder.build()).build();

        LOG.info("Successful exit from getPolicyInstances ");

        return Futures.immediateFuture(rpcResult);
    }


    // RPC createPolicyInstance

    @Override
    public ListenableFuture<RpcResult<CreatePolicyInstanceOutput>> createPolicyInstance(
            CreatePolicyInstanceInput input) {
        final String svcOperation = "createPolicyInstance";

        Properties parms = new Properties();
        CreatePolicyInstanceOutputBuilder serviceDataBuilder =
                (CreatePolicyInstanceOutputBuilder) getServiceData(CREATE_POLICY_INSTANCES);

        LOG.info("Reached RPC createPolicyInstance");

        LOG.info(svcOperation + " called.");

        if (input == null) {
            LOG.debug("exiting " + svcOperation + " because of invalid input");
            serviceDataBuilder.setResponseCode("Input is null");
            RpcResult<CreatePolicyInstanceOutput> rpcResult = RpcResultBuilder.<CreatePolicyInstanceOutput>status(true)
                    .withResult(serviceDataBuilder.build()).build();
            return Futures.immediateFuture(rpcResult);
        }

        // add input to parms
        LOG.info("Adding INPUT data for " + svcOperation + " input: " + input);
        CreatePolicyInstanceInputBuilder inputBuilder = new CreatePolicyInstanceInputBuilder(input);
        MdsalHelper.toProperties(parms, inputBuilder.build());

        LOG.info("Printing SLI parameters to be passed");

        // iterate properties file to get key-value pairs
        for (String key : parms.stringPropertyNames()) {
            String value = parms.getProperty(key);
            LOG.info("The SLI parameter in " + key + " is: " + value);
        }

        // Call SLI sync method
        try {
            if (A1AdapterClient.hasGraph("A1-ADAPTER-API", svcOperation, null, "sync")) {
                LOG.info("A1AdapterClient has a Directed Graph for '" + svcOperation + "'");
                try {
                    A1AdapterClient.execute("A1-ADAPTER-API", svcOperation, null, "sync", serviceDataBuilder, parms);
                } catch (Exception e) {
                    LOG.error("Caught exception executing service logic for " + svcOperation, e);
                    serviceDataBuilder.setResponseCode("500");
                }
            } else {
                LOG.error("No service logic active for A1Adapter: '" + svcOperation + "'");
                serviceDataBuilder.setResponseCode("503");
            }
        } catch (Exception e) {
            LOG.error("Caught exception looking for service logic", e);
            serviceDataBuilder.setResponseCode("500");
        }

        String errorCode = serviceDataBuilder.getResponseCode();

        if (!("0".equals(errorCode) || "200".equals(errorCode))) {
            LOG.error("Returned FAILED for " + svcOperation + " error code: '" + errorCode + "'");
        } else {
            LOG.info("Returned SUCCESS for " + svcOperation + " ");
            serviceDataBuilder.setResponseCode("A1 Adapter Executed for CreatePolicyInstance. ");
        }

        RpcResult<CreatePolicyInstanceOutput> rpcResult = RpcResultBuilder.<CreatePolicyInstanceOutput>status(true)
                .withResult(serviceDataBuilder.build()).build();

        LOG.info("Successful exit from createPolicyInstance ");

        return Futures.immediateFuture(rpcResult);
    }



    // RPC getPolicyInstance

    @Override
    public ListenableFuture<RpcResult<GetPolicyInstanceOutput>> getPolicyInstance(GetPolicyInstanceInput input) {
        final String svcOperation = "getPolicyInstance";

        Properties parms = new Properties();
        GetPolicyInstanceOutputBuilder serviceDataBuilder =
                (GetPolicyInstanceOutputBuilder) getServiceData(GET_POLICY_INSTANCE);

        LOG.info("Reached RPC getPolicyInstance");

        LOG.info(svcOperation + " called.");

        if (input == null) {
            LOG.debug("exiting " + svcOperation + " because of invalid input");
            serviceDataBuilder.setResponseCode("Input is null");
            RpcResult<GetPolicyInstanceOutput> rpcResult = RpcResultBuilder.<GetPolicyInstanceOutput>status(true)
                    .withResult(serviceDataBuilder.build()).build();
            return Futures.immediateFuture(rpcResult);
        }

        // add input to parms
        LOG.info("Adding INPUT data for " + svcOperation + " input: " + input);
        GetPolicyInstanceInputBuilder inputBuilder = new GetPolicyInstanceInputBuilder(input);
        MdsalHelper.toProperties(parms, inputBuilder.build());

        LOG.info("Printing SLI parameters to be passed");

        // iterate properties file to get key-value pairs
        for (String key : parms.stringPropertyNames()) {
            String value = parms.getProperty(key);
            LOG.info("The SLI parameter in " + key + " is: " + value);
        }

        // Call SLI sync method
        try {
            if (A1AdapterClient.hasGraph("A1-ADAPTER-API", svcOperation, null, "sync")) {
                LOG.info("A1AdapterClient has a Directed Graph for '" + svcOperation + "'");
                try {
                    A1AdapterClient.execute("A1-ADAPTER-API", svcOperation, null, "sync", serviceDataBuilder, parms);
                } catch (Exception e) {
                    LOG.error("Caught exception executing service logic for " + svcOperation, e);
                    serviceDataBuilder.setResponseCode("500");
                }
            } else {
                LOG.error("No service logic active for A1Adapter: '" + svcOperation + "'");
                serviceDataBuilder.setResponseCode("503");
            }
        } catch (Exception e) {
            LOG.error("Caught exception looking for service logic", e);
            serviceDataBuilder.setResponseCode("500");
        }

        String errorCode = serviceDataBuilder.getResponseCode();

        if (!("0".equals(errorCode) || "200".equals(errorCode))) {
            LOG.error("Returned FAILED for " + svcOperation + " error code: '" + errorCode + "'");
        } else {
            LOG.info("Returned SUCCESS for " + svcOperation + " ");
            serviceDataBuilder.setResponseCode("A1 Adapter Executed for GetPolicyInstance. ");
        }

        RpcResult<GetPolicyInstanceOutput> rpcResult =
                RpcResultBuilder.<GetPolicyInstanceOutput>status(true).withResult(serviceDataBuilder.build()).build();

        LOG.info("Successful exit from getPolicyInstance ");

        return Futures.immediateFuture(rpcResult);
    }



    // RPC deletePolicyInstance

    @Override
    public ListenableFuture<RpcResult<DeletePolicyInstanceOutput>> deletePolicyInstance(
            DeletePolicyInstanceInput input) {
        final String svcOperation = "deletePolicyInstance";

        Properties parms = new Properties();
        DeletePolicyInstanceOutputBuilder serviceDataBuilder =
                (DeletePolicyInstanceOutputBuilder) getServiceData(DELETE_POLICY_INSTANCE);

        LOG.info("Reached RPC deletePolicyInstance");

        LOG.info(svcOperation + " called.");

        if (input == null) {
            LOG.debug("exiting " + svcOperation + " because of invalid input");
            serviceDataBuilder.setResponseCode("Input is null");
            RpcResult<DeletePolicyInstanceOutput> rpcResult = RpcResultBuilder.<DeletePolicyInstanceOutput>status(true)
                    .withResult(serviceDataBuilder.build()).build();
            return Futures.immediateFuture(rpcResult);
        }

        // add input to parms
        LOG.info("Adding INPUT data for " + svcOperation + " input: " + input);
        DeletePolicyInstanceInputBuilder inputBuilder = new DeletePolicyInstanceInputBuilder(input);
        MdsalHelper.toProperties(parms, inputBuilder.build());

        LOG.info("Printing SLI parameters to be passed");

        // iterate properties file to get key-value pairs
        for (String key : parms.stringPropertyNames()) {
            String value = parms.getProperty(key);
            LOG.info("The SLI parameter in " + key + " is: " + value);
        }

        // Call SLI sync method
        try {
            if (A1AdapterClient.hasGraph("A1-ADAPTER-API", svcOperation, null, "sync")) {
                LOG.info("A1AdapterClient has a Directed Graph for '" + svcOperation + "'");
                try {
                    A1AdapterClient.execute("A1-ADAPTER-API", svcOperation, null, "sync", serviceDataBuilder, parms);
                } catch (Exception e) {
                    LOG.error("Caught exception executing service logic for " + svcOperation, e);
                    serviceDataBuilder.setResponseCode("500");
                }
            } else {
                LOG.error("No service logic active for A1Adapter: '" + svcOperation + "'");
                serviceDataBuilder.setResponseCode("503");
            }
        } catch (Exception e) {
            LOG.error("Caught exception looking for service logic", e);
            serviceDataBuilder.setResponseCode("500");
        }

        String errorCode = serviceDataBuilder.getResponseCode();

        if (!("0".equals(errorCode) || "200".equals(errorCode))) {
            LOG.error("Returned FAILED for " + svcOperation + " error code: '" + errorCode + "'");
        } else {
            LOG.info("Returned SUCCESS for " + svcOperation + " ");
            serviceDataBuilder.setResponseCode("A1 Adapter Executed for DeletePolicyInstance. ");
        }

        RpcResult<DeletePolicyInstanceOutput> rpcResult = RpcResultBuilder.<DeletePolicyInstanceOutput>status(true)
                .withResult(serviceDataBuilder.build()).build();

        LOG.info("Successful exit from deletePolicyInstance ");

        return Futures.immediateFuture(rpcResult);
    }



    // RPC getStatus

    @Override
    public ListenableFuture<RpcResult<GetStatusOutput>> getStatus(GetStatusInput input) {
        final String svcOperation = "getStatus";

        Properties parms = new Properties();
        GetStatusOutputBuilder serviceDataBuilder = (GetStatusOutputBuilder) getServiceData(GET_STATUS);

        LOG.info("Reached RPC getStatus");

        LOG.info(svcOperation + " called.");

        if (input == null) {
            LOG.debug("exiting " + svcOperation + " because of invalid input");
            serviceDataBuilder.setResponseCode("Input is null");
            RpcResult<GetStatusOutput> rpcResult =
                    RpcResultBuilder.<GetStatusOutput>status(true).withResult(serviceDataBuilder.build()).build();
            return Futures.immediateFuture(rpcResult);
        }

        // add input to parms
        LOG.info("Adding INPUT data for " + svcOperation + " input: " + input);
        GetStatusInputBuilder inputBuilder = new GetStatusInputBuilder(input);
        MdsalHelper.toProperties(parms, inputBuilder.build());

        LOG.info("Printing SLI parameters to be passed");

        // iterate properties file to get key-value pairs
        for (String key : parms.stringPropertyNames()) {
            String value = parms.getProperty(key);
            LOG.info("The SLI parameter in " + key + " is: " + value);
        }

        // Call SLI sync method
        try {
            if (A1AdapterClient.hasGraph("A1-ADAPTER-API", svcOperation, null, "sync")) {
                LOG.info("A1AdapterClient has a Directed Graph for '" + svcOperation + "'");
                try {
                    A1AdapterClient.execute("A1-ADAPTER-API", svcOperation, null, "sync", serviceDataBuilder, parms);
                } catch (Exception e) {
                    LOG.error("Caught exception executing service logic for " + svcOperation, e);
                    serviceDataBuilder.setResponseCode("500");
                }
            } else {
                LOG.error("No service logic active for A1Adapter: '" + svcOperation + "'");
                serviceDataBuilder.setResponseCode("503");
            }
        } catch (Exception e) {
            LOG.error("Caught exception looking for service logic", e);
            serviceDataBuilder.setResponseCode("500");
        }

        String errorCode = serviceDataBuilder.getResponseCode();

        if (!("0".equals(errorCode) || "200".equals(errorCode))) {
            LOG.error("Returned FAILED for " + svcOperation + " error code: '" + errorCode + "'");
        } else {
            LOG.info("Returned SUCCESS for " + svcOperation + " ");
            serviceDataBuilder.setResponseCode("A1 Adapter Executed for GetStatus. ");
        }

        RpcResult<GetStatusOutput> rpcResult =
                RpcResultBuilder.<GetStatusOutput>status(true).withResult(serviceDataBuilder.build()).build();

        LOG.info("Successful exit from getStatus ");

        return Futures.immediateFuture(rpcResult);
    }


    // RPC notifyPolicyEnforcementUpdate

    @Override
    public ListenableFuture<RpcResult<NotifyPolicyEnforcementUpdateOutput>> notifyPolicyEnforcementUpdate(
            NotifyPolicyEnforcementUpdateInput input) {
        final String svcOperation = "notifyPolicyEnforcementUpdate";

        Properties parms = new Properties();
        NotifyPolicyEnforcementUpdateOutputBuilder serviceDataBuilder =
                (NotifyPolicyEnforcementUpdateOutputBuilder) getServiceData(NOTIFICATION_ENFORECEMENT);

        LOG.info("Reached RPC notifyPolicyEnforcementUpdate");

        LOG.info(svcOperation + " called.");

        if (input == null) {
            LOG.debug("exiting " + svcOperation + " because of invalid input");
            serviceDataBuilder.setResponseCode("Input is null");
            RpcResult<NotifyPolicyEnforcementUpdateOutput> rpcResult = RpcResultBuilder
                    .<NotifyPolicyEnforcementUpdateOutput>status(true).withResult(serviceDataBuilder.build()).build();
            return Futures.immediateFuture(rpcResult);
        }

        // add input to parms
        LOG.info("Adding INPUT data for " + svcOperation + " input: " + input);
        NotifyPolicyEnforcementUpdateInputBuilder inputBuilder = new NotifyPolicyEnforcementUpdateInputBuilder(input);
        MdsalHelper.toProperties(parms, inputBuilder.build());

        LOG.info("Printing SLI parameters to be passed");

        // iterate properties file to get key-value pairs
        for (String key : parms.stringPropertyNames()) {
            String value = parms.getProperty(key);
            LOG.info("The SLI parameter in " + key + " is: " + value);
        }

        // Call SLI sync method
        try {
            if (A1AdapterClient.hasGraph("A1-ADAPTER-API", svcOperation, null, "sync")) {
                LOG.info("A1AdapterClient has a Directed Graph for '" + svcOperation + "'");
                try {
                    A1AdapterClient.execute("A1-ADAPTER-API", svcOperation, null, "sync", serviceDataBuilder, parms);
                } catch (Exception e) {
                    LOG.error("Caught exception executing service logic for " + svcOperation, e);
                    serviceDataBuilder.setResponseCode("500");
                }
            } else {
                LOG.error("No service logic active for A1Adapter: '" + svcOperation + "'");
                serviceDataBuilder.setResponseCode("503");
            }
        } catch (Exception e) {
            LOG.error("Caught exception looking for service logic", e);
            serviceDataBuilder.setResponseCode("500");
        }

        String errorCode = serviceDataBuilder.getResponseCode();

        if (!("0".equals(errorCode) || "200".equals(errorCode))) {
            LOG.error("Returned FAILED for " + svcOperation + " error code: '" + errorCode + "'");
        } else {
            LOG.info("Returned SUCCESS for " + svcOperation + " ");
            serviceDataBuilder.setResponseCode("A1 Adapter Executed for notifyPolicyEnforcementUpdate. ");
        }

        RpcResult<NotifyPolicyEnforcementUpdateOutput> rpcResult = RpcResultBuilder
                .<NotifyPolicyEnforcementUpdateOutput>status(true).withResult(serviceDataBuilder.build()).build();

        LOG.info("Successful exit from notifyPolicyEnforcementUpdate ");

        return Futures.immediateFuture(rpcResult);
    }

    protected Builder<?> getServiceData(String svcOperation) {
        switch (svcOperation) {
            case GET_NEARRT_RICS:
                return new GetNearRTRICsOutputBuilder();
            case GET_HEALTH_CHECK:
                return new GetHealthCheckOutputBuilder();
            case GET_POLICY_TYPES:
                return new GetPolicyTypesOutputBuilder();
            case CREATE_POLICY_TYPE:
                return new CreatePolicyTypeOutputBuilder();
            case GET_POLICY_TYPE:
                return new GetPolicyTypeOutputBuilder();
            case DELETE_POLICY_TYPE:
                return new DeletePolicyTypeOutputBuilder();
            case GET_POLICY_INSTANCES:
                return new GetPolicyInstancesOutputBuilder();
            case CREATE_POLICY_INSTANCES:
                return new CreatePolicyInstanceOutputBuilder();
            case GET_POLICY_INSTANCE:
                return new GetPolicyInstanceOutputBuilder();
            case DELETE_POLICY_INSTANCE:
                return new DeletePolicyInstanceOutputBuilder();
            case GET_STATUS:
                return new GetStatusOutputBuilder();
            case NOTIFICATION_ENFORECEMENT:
                return new NotifyPolicyEnforcementUpdateOutputBuilder();
        }
        return null;
    }
}
