/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 			reserved.
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

package org.onap.ccsdk.features.sdnr.northbound.oofpcipoc;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.sli.core.sli.provider.MdsalHelper;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev190308.AddNeighbor;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev190308.AddNeighborInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev190308.AddNeighborInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev190308.AddNeighborOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev190308.AddNeighborOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev190308.ConfigurationPhyCellId;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev190308.ConfigurationPhyCellIdInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev190308.ConfigurationPhyCellIdInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev190308.ConfigurationPhyCellIdOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev190308.ConfigurationPhyCellIdOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev190308.DeleteNeighbor;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev190308.DeleteNeighborInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev190308.DeleteNeighborInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev190308.DeleteNeighborOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev190308.DeleteNeighborOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev190308.GenericNeighborConfiguration;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev190308.GenericNeighborConfigurationInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev190308.GenericNeighborConfigurationInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev190308.GenericNeighborConfigurationOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev190308.GenericNeighborConfigurationOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev190308.Greeting;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev190308.GreetingInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev190308.GreetingInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev190308.GreetingOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev190308.GreetingOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev190308.HandleNbrlistChangeNotif;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev190308.HandleNbrlistChangeNotifInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev190308.HandleNbrlistChangeNotifInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev190308.HandleNbrlistChangeNotifOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev190308.HandleNbrlistChangeNotifOutputBuilder;
import org.opendaylight.yangtools.binding.Rpc;
import org.opendaylight.yangtools.binding.RpcInput;
import org.opendaylight.yangtools.binding.RpcOutput;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines a base implementation for your provider. This class extends from a helper class which provides storage for
 * the most commonly used components of the MD-SAL. Additionally the base class provides some basic logging and
 * initialization / clean up methods.
 *
 */
public class OofpcipocProvider implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(OofpcipocProvider.class);

    private static final String APPLICATION_NAME = "oofpcipoc-api";

    private final ExecutorService executor;

    private DataBroker dataBroker;
    private NotificationPublishService notificationService;
    private RpcProviderService rpcProviderService;
    private Registration rpcRegistration;
    private OofpcipocClient OofpcipocClient;

    public OofpcipocProvider() {

        LOG.info("Creating provider for {}", APPLICATION_NAME);
        executor = Executors.newFixedThreadPool(1);
        this.dataBroker = null;
        this.notificationService = null;
        this.rpcProviderService = null;
        this.OofpcipocClient = null;
    }

    public void setDataBroker(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public void setRpcProviderService(RpcProviderService rpcProviderRegistry) {
        this.rpcProviderService = rpcProviderRegistry;
    }

    public void setNotificationPublishService(NotificationPublishService notificationPublishService) {
        this.notificationService = notificationPublishService;
    }

    public void setClient(OofpcipocClient client) {
        this.OofpcipocClient = client;
    }

    public void init() {
        LOG.info("Initializing provider for {}", APPLICATION_NAME);
        //rpcRegistration = rpcProviderService.registerRpcImplementation(OofpcipocApiService.class, this);
        rpcRegistration = rpcProviderService.registerRpcImplementations(
                List.of(new RpcHelper<>(Greeting.class, OofpcipocProvider.this::greeting),
                        new RpcHelper<>(ConfigurationPhyCellId.class, OofpcipocProvider.this::configurationPhyCellId),
                        new RpcHelper<>(AddNeighbor.class, OofpcipocProvider.this::addNeighbor),
                        new RpcHelper<>(DeleteNeighbor.class, OofpcipocProvider.this::deleteNeighbor),
                        new RpcHelper<>(GenericNeighborConfiguration.class,
                                OofpcipocProvider.this::genericNeighborConfiguration),
                        new RpcHelper<>(HandleNbrlistChangeNotif.class,
                                OofpcipocProvider.this::handleNbrlistChangeNotif)));
        LOG.info("Initialization complete for {}", APPLICATION_NAME);
    }

    @Override
    public void close() throws Exception {
        LOG.info("Closing provider for {}", APPLICATION_NAME);
        executor.shutdown();
        if (rpcRegistration != null) {
            rpcRegistration.close();
        }
        LOG.info("Successfully closed provider for {}", APPLICATION_NAME);
    }

    public ListenableFuture<RpcResult<GreetingOutput>> greeting(GreetingInput input) {
        final String svcOperation = "greeting";

        Properties parms = new Properties();
        GreetingOutputBuilder serviceDataBuilder = new GreetingOutputBuilder();

        LOG.info("Reached RPC greeting");

        LOG.info(svcOperation + " called.");

        if (input == null) {
            LOG.debug("exiting " + svcOperation + " because of invalid input");
            serviceDataBuilder.setResponse("Input is null");
            RpcResult<GreetingOutput> rpcResult = RpcResultBuilder.<GreetingOutput>status(true)
                    .withResult(serviceDataBuilder.build()).build();
            return Futures.immediateFuture(rpcResult);
        }

        // add input to parms
        LOG.info("Adding INPUT data for " + svcOperation + " input: " + input);
        GreetingInputBuilder inputBuilder = new GreetingInputBuilder(input);
        MdsalHelper.toProperties(parms, inputBuilder.build());

        // Call SLI sync method
        try {
            if (OofpcipocClient.hasGraph("oofpcipoc-api", svcOperation, null, "sync")) {
                LOG.info("OofpcipocClient has a Directed Graph for '" + svcOperation + "'");
                try {
                    OofpcipocClient.execute("oofpcipoc-api", svcOperation, null, "sync", serviceDataBuilder, parms);
                } catch (Exception e) {
                    LOG.error("Caught exception executing service logic for " + svcOperation, e);
                    serviceDataBuilder.setResponse("500");
                }
            } else {
                LOG.error("No service logic active for Oofpcipoc: '" + svcOperation + "'");
                serviceDataBuilder.setResponse("503");
            }
        } catch (Exception e) {
            LOG.error("Caught exception looking for service logic", e);
            serviceDataBuilder.setResponse("500");
        }

        String errorCode = serviceDataBuilder.getResponse();

        if (!("0".equals(errorCode) || "200".equals(errorCode))) {
            LOG.error("Returned FAILED for " + svcOperation + " error code: '" + errorCode + "'");
        } else {
            LOG.info("Returned SUCCESS for " + svcOperation + " ");
            serviceDataBuilder.setResponse("Welcome OOF POC " + input.getSalutation());
        }

        RpcResult<GreetingOutput> rpcResult = RpcResultBuilder.<GreetingOutput>status(true)
                .withResult(serviceDataBuilder.build()).build();

        LOG.info("Successful exit from greeting ");

        return Futures.immediateFuture(rpcResult);
    }

    // RPC configuration-phy-cell-id
    public ListenableFuture<RpcResult<ConfigurationPhyCellIdOutput>> configurationPhyCellId(
            ConfigurationPhyCellIdInput input) {
        final String svcOperation = "configuration-phy-cell-id";

        Properties parms = new Properties();
        ConfigurationPhyCellIdOutputBuilder serviceDataBuilder = new ConfigurationPhyCellIdOutputBuilder();

        LOG.info("Reached RPC configurationPhyCellId");

        LOG.info(svcOperation + " called.");

        if (input == null) {
            LOG.debug("exiting " + svcOperation + " because of invalid input");
            serviceDataBuilder.setResponseCode("Input is null");
            RpcResult<ConfigurationPhyCellIdOutput> rpcResult = RpcResultBuilder
                    .<ConfigurationPhyCellIdOutput>status(true).withResult(serviceDataBuilder.build()).build();
            return Futures.immediateFuture(rpcResult);
        }

        // add input to parms
        LOG.info("Adding INPUT data for " + svcOperation + " input: " + input);
        ConfigurationPhyCellIdInputBuilder inputBuilder = new ConfigurationPhyCellIdInputBuilder(input);
        MdsalHelper.toProperties(parms, inputBuilder.build());

        // Call SLI sync method
        try {

            if (OofpcipocClient.hasGraph("oofpcipoc-api", svcOperation, null, "sync")) {
                LOG.info("OofpcipocClient has a Directed Graph for '" + svcOperation + "'");

                try {
                    OofpcipocClient.execute("oofpcipoc-api", svcOperation, null, "sync", serviceDataBuilder, parms);
                } catch (Exception e) {
                    LOG.error("Caught exception executing service logic for " + svcOperation, e);
                    serviceDataBuilder.setResponseCode("500");
                }
            } else {
                LOG.error("No service logic active for Oofpcipoc: '" + svcOperation + "'");
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
            serviceDataBuilder
                    .setResponseCode("Welcome OOF POC. Number of FAP entries " + input.getFapServiceNumberOfEntries());
        }

        RpcResult<ConfigurationPhyCellIdOutput> rpcResult = RpcResultBuilder.<ConfigurationPhyCellIdOutput>status(true)
                .withResult(serviceDataBuilder.build()).build();

        return Futures.immediateFuture(rpcResult);
    }

    // RPC add-neighbor
    public ListenableFuture<RpcResult<AddNeighborOutput>> addNeighbor(AddNeighborInput input) {
        final String svcOperation = "add-neighbor";

        Properties parms = new Properties();
        AddNeighborOutputBuilder serviceDataBuilder = new AddNeighborOutputBuilder();

        LOG.info("Reached RPC addNeighbor");

        LOG.info(svcOperation + " called.");

        if (input == null) {
            LOG.debug("exiting " + svcOperation + " because of invalid input");
            serviceDataBuilder.setResponseCode("Input is null");
            RpcResult<AddNeighborOutput> rpcResult = RpcResultBuilder.<AddNeighborOutput>status(true)
                    .withResult(serviceDataBuilder.build()).build();
            return Futures.immediateFuture(rpcResult);
        }

        // add input to parms
        LOG.info("Adding INPUT data for " + svcOperation + " input: " + input);
        AddNeighborInputBuilder inputBuilder = new AddNeighborInputBuilder(input);
        MdsalHelper.toProperties(parms, inputBuilder.build());

        // Call SLI sync method
        try {

            if (OofpcipocClient.hasGraph("oofpcipoc-api", svcOperation, null, "sync")) {
                LOG.info("OofpcipocClient has a Directed Graph for '" + svcOperation + "'");

                try {
                    OofpcipocClient.execute("oofpcipoc-api", svcOperation, null, "sync", serviceDataBuilder, parms);
                } catch (Exception e) {
                    LOG.error("Caught exception executing service logic for " + svcOperation, e);
                    serviceDataBuilder.setResponseCode("500");
                }
            } else {
                LOG.error("No service logic active for Oofpcipoc: '" + svcOperation + "'");
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
            serviceDataBuilder.setResponseCode(
                    "Welcome OOF POC. Number of Neighbor entries to be added " + input.getLteCellNumberOfEntries());
        }

        RpcResult<AddNeighborOutput> rpcResult = RpcResultBuilder.<AddNeighborOutput>status(true)
                .withResult(serviceDataBuilder.build()).build();

        return Futures.immediateFuture(rpcResult);
    }

    // RPC delete-neighbor
    public ListenableFuture<RpcResult<DeleteNeighborOutput>> deleteNeighbor(DeleteNeighborInput input) {
        final String svcOperation = "delete-neighbor";

        Properties parms = new Properties();
        DeleteNeighborOutputBuilder serviceDataBuilder = new DeleteNeighborOutputBuilder();

        LOG.info("Reached RPC deleteNeighbor");

        LOG.info(svcOperation + " called.");

        if (input == null) {
            LOG.debug("exiting " + svcOperation + " because of invalid input");
            serviceDataBuilder.setResponseCode("Input is null");
            RpcResult<DeleteNeighborOutput> rpcResult = RpcResultBuilder.<DeleteNeighborOutput>status(true)
                    .withResult(serviceDataBuilder.build()).build();
            return Futures.immediateFuture(rpcResult);
        }

        // add input to parms
        LOG.info("Adding INPUT data for " + svcOperation + " input: " + input);
        DeleteNeighborInputBuilder inputBuilder = new DeleteNeighborInputBuilder(input);
        MdsalHelper.toProperties(parms, inputBuilder.build());

        // Call SLI sync method
        try {

            if (OofpcipocClient.hasGraph("oofpcipoc-api", svcOperation, null, "sync")) {
                LOG.info("OofpcipocClient has a Directed Graph for '" + svcOperation + "'");

                try {
                    OofpcipocClient.execute("oofpcipoc-api", svcOperation, null, "sync", serviceDataBuilder, parms);
                } catch (Exception e) {
                    LOG.error("Caught exception executing service logic for " + svcOperation, e);
                    serviceDataBuilder.setResponseCode("500");
                }
            } else {
                LOG.error("No service logic active for Oofpcipoc: '" + svcOperation + "'");
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
            serviceDataBuilder.setResponseCode(
                    "Welcome OOF POC. Number of Neighbor entries to be deleted " + input.getLteCellNumberOfEntries());
        }

        RpcResult<DeleteNeighborOutput> rpcResult = RpcResultBuilder.<DeleteNeighborOutput>status(true)
                .withResult(serviceDataBuilder.build()).build();

        return Futures.immediateFuture(rpcResult);
    }

    // RPC generic-neighbor-configuration
    public ListenableFuture<RpcResult<GenericNeighborConfigurationOutput>> genericNeighborConfiguration(
            GenericNeighborConfigurationInput input) {
        final String svcOperation = "generic-neighbor-configuration";

        Properties parms = new Properties();
        GenericNeighborConfigurationOutputBuilder serviceDataBuilder = new GenericNeighborConfigurationOutputBuilder();

        LOG.info("Reached RPC genericNeighborConfiguration");

        LOG.info(svcOperation + " called.");

        if (input == null) {
            LOG.debug("exiting " + svcOperation + " because of invalid input");
            serviceDataBuilder.setResponseCode("Input is null");
            RpcResult<GenericNeighborConfigurationOutput> rpcResult = RpcResultBuilder
                    .<GenericNeighborConfigurationOutput>status(true).withResult(serviceDataBuilder.build()).build();
            return Futures.immediateFuture(rpcResult);
        }

        // add input to parms
        LOG.info("Adding INPUT data for " + svcOperation + " input: " + input);
        GenericNeighborConfigurationInputBuilder inputBuilder = new GenericNeighborConfigurationInputBuilder(input);
        MdsalHelper.toProperties(parms, inputBuilder.build());

        // Call SLI sync method
        try {

            if (OofpcipocClient.hasGraph("oofpcipoc-api", svcOperation, null, "sync")) {
                LOG.info("OofpcipocClient has a Directed Graph for '" + svcOperation + "'");

                try {
                    OofpcipocClient.execute("oofpcipoc-api", svcOperation, null, "sync", serviceDataBuilder, parms);
                } catch (Exception e) {
                    LOG.error("Caught exception executing service logic for " + svcOperation, e);
                    serviceDataBuilder.setResponseCode("500");
                }
            } else {
                LOG.error("No service logic active for Oofpcipoc: '" + svcOperation + "'");
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
            serviceDataBuilder.setResponseCode("Welcome OOF POC. Number of Neighbor entries to be configured "
                    + input.getLteCellNumberOfEntries());
        }

        RpcResult<GenericNeighborConfigurationOutput> rpcResult = RpcResultBuilder
                .<GenericNeighborConfigurationOutput>status(true).withResult(serviceDataBuilder.build()).build();

        return Futures.immediateFuture(rpcResult);
    }

    // RPC handle-nbrlist-change-notif
    public ListenableFuture<RpcResult<HandleNbrlistChangeNotifOutput>> handleNbrlistChangeNotif(
            HandleNbrlistChangeNotifInput input) {
        final String svcOperation = "handle-nbrlist-change-notif";

        Properties parms = new Properties();
        HandleNbrlistChangeNotifOutputBuilder serviceDataBuilder = new HandleNbrlistChangeNotifOutputBuilder();

        LOG.info("Reached RPC handle-nbrlist-change-notif");

        LOG.info(svcOperation + " called.");

        if (input == null) {
            LOG.debug("exiting " + svcOperation + " because of invalid input");
            serviceDataBuilder.setResponseCode("Input is null");
            RpcResult<HandleNbrlistChangeNotifOutput> rpcResult = RpcResultBuilder
                    .<HandleNbrlistChangeNotifOutput>status(true).withResult(serviceDataBuilder.build()).build();
            return Futures.immediateFuture(rpcResult);
        }

        // add input to parms
        LOG.info("Adding INPUT data for " + svcOperation + " input: " + input);
        HandleNbrlistChangeNotifInputBuilder inputBuilder = new HandleNbrlistChangeNotifInputBuilder(input);
        MdsalHelper.toProperties(parms, inputBuilder.build());

        // Call SLI sync method
        try {
            if (OofpcipocClient.hasGraph("oofpcipoc-api", svcOperation, null, "sync")) {
                LOG.info("OofpcipocClient has a Directed Graph for '" + svcOperation + "'");
                try {
                    OofpcipocClient.execute("oofpcipoc-api", svcOperation, null, "sync", serviceDataBuilder, parms);
                } catch (Exception e) {
                    LOG.error("Caught exception executing service logic for " + svcOperation, e);
                    serviceDataBuilder.setResponseCode("500");
                }
            } else {
                LOG.error("No service logic active for Oofpcipoc: '" + svcOperation + "'");
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
            serviceDataBuilder.setResponseCode(
                    "Welcome OOF POC. Number of FAP services changed = " + input.getFapServiceNumberOfEntriesChanged());
        }

        RpcResult<HandleNbrlistChangeNotifOutput> rpcResult = RpcResultBuilder
                .<HandleNbrlistChangeNotifOutput>status(true).withResult(serviceDataBuilder.build()).build();

        LOG.info("Successful exit from handle-nbrlist-change-notif ");

        return Futures.immediateFuture(rpcResult);
    }

    private interface RpcExecutionWrapper<I extends RpcInput, O extends RpcOutput> {

        ListenableFuture<@NonNull RpcResult<@NonNull O>> execute(@NonNull I input);
    }

    private static class RpcHelper<I extends RpcInput, O extends RpcOutput> implements Rpc<I, O> {

        private final RpcExecutionWrapper<I, O> executor;
        private final Class<? extends Rpc<I, O>> implementedInterface;

        RpcHelper(Class<? extends Rpc<I, O>> implementedInterface, RpcExecutionWrapper<I, O> executor) {
            this.implementedInterface = implementedInterface;
            this.executor = executor;
        }

        @Override
        public @NonNull ListenableFuture<@NonNull RpcResult<@NonNull O>> invoke(@NonNull I input) {
            return this.executor.execute(input);
        }

        @Override
        public @NonNull Class<? extends Rpc<I, O>> implementedInterface() {
            return this.implementedInterface;
        }
    }

}
