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

package org.onap.ccsdk.features.sdnr.northbound.cmnotify;

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
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev200224.NbrlistChangeNotificationInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev200224.NbrlistChangeNotificationInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev200224.NbrlistChangeNotificationOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev200224.NbrlistChangeNotificationOutputBuilder;
import org.opendaylight.yangtools.binding.Rpc;
import org.opendaylight.yangtools.binding.RpcInput;
import org.opendaylight.yangtools.binding.RpcOutput;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Defines a base implementation for your provider. This class extends from a
 * helper class which provides storage for the most commonly used components of
 * the MD-SAL. Additionally the base class provides some basic logging and
 * initialization / clean up methods.
 *
 */
public class CMNotifyProvider implements AutoCloseable {

	private static final Logger LOG = LoggerFactory.getLogger(CMNotifyProvider.class);

	private static final String APPLICATION_NAME = "CMNotify-api";
	private static final String NBRLIST_CHANGE_NOTIFICATION = "nbrlist-change-notification";

	private final ExecutorService executor;
	protected DataBroker dataBroker;
	protected RpcProviderService rpcProviderService;
    private Registration rpcRegistration;
	protected NotificationPublishService notificationService;
	private CMNotifyClient CMNotifyClient;

	public CMNotifyProvider() {

		LOG.info("Creating provider for {}", APPLICATION_NAME);
		executor = Executors.newFixedThreadPool(1);
		
		this.dataBroker = null; 
		this.notificationService = null; 
		this.rpcProviderService = null;
		this.CMNotifyClient = null;
	}

	public void setDataBroker(DataBroker dataBroker) {
		this.dataBroker = dataBroker;
	}

	public void setRpcProviderService(RpcProviderService rpcProviderService) {
		this.rpcProviderService = rpcProviderService;
	}

	public void setNotificationPublishService(NotificationPublishService notificationPublishService) {
		this.notificationService = notificationPublishService;
	}

	public void setClient(CMNotifyClient client) {
		this.CMNotifyClient = client;
	}

	public void init() {
		LOG.info("Initializing provider for {}", APPLICATION_NAME);
        rpcRegistration = rpcProviderService.registerRpcImplementations(
                List.of(new RpcHelper<>(
                        null, //NbrlistChangeNotification.class,
                        CMNotifyProvider.this::nbrlistChangeNotification)
                ));
        LOG.info("Initialization complete for {}", APPLICATION_NAME);
	}

	@Override
	public void close() throws Exception {
		LOG.info("Closing provider for {}", APPLICATION_NAME);
		executor.shutdown();
    	rpcRegistration.close();
		LOG.info("Successfully closed provider for {}", APPLICATION_NAME);
	}

	// RPC nbrlist-change-notification

	public ListenableFuture<RpcResult<NbrlistChangeNotificationOutput>> nbrlistChangeNotification(
			NbrlistChangeNotificationInput input) {
		final String svcOperation = "nbrlist-change-notification";

		Properties parms = new Properties();
		NbrlistChangeNotificationOutputBuilder serviceDataBuilder = getServiceData(
				NBRLIST_CHANGE_NOTIFICATION);

		LOG.info("Reached RPC nbrlist-change-notification");

		LOG.info(svcOperation + " called.");

		if (input == null) {
			LOG.debug("exiting " + svcOperation + " because of invalid input");
            NbrlistChangeNotificationOutput serviceData = serviceDataBuilder.setResponseCode("Input is null").build();
			RpcResult<NbrlistChangeNotificationOutput> rpcResult = RpcResultBuilder
					.<NbrlistChangeNotificationOutput>status(true).withResult(serviceData).build();
			return Futures.immediateFuture(rpcResult);
		}

		// add input to parms
		LOG.info("Adding INPUT data for " + svcOperation + " input: " + input);
		NbrlistChangeNotificationInputBuilder inputBuilder = new NbrlistChangeNotificationInputBuilder(input);
		MdsalHelper.toProperties(parms, inputBuilder.build());

		LOG.info("Printing SLI parameters to be passed");

		// iterate properties file to get key-value pairs
		for (String key : parms.stringPropertyNames()) {
			String value = parms.getProperty(key);
			LOG.info("The SLI parameter in " + key + " is: " + value);
		}

		// Call SLI sync method
		try {
			if (CMNotifyClient.hasGraph("CM-NOTIFY-API", svcOperation, null, "sync")) {
				LOG.info("CMNotifyClient has a Directed Graph for '" + svcOperation + "'");
				try {
					CMNotifyClient.execute("CM-NOTIFY-API", svcOperation, null, "sync", serviceDataBuilder, parms);
				} catch (Exception e) {
					LOG.error("Caught exception executing service logic for " + svcOperation, e);
					serviceDataBuilder.setResponseCode("500");
				}
			} else {
				LOG.error("No service logic active for CMNotify: '" + svcOperation + "'");
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
			serviceDataBuilder.setResponseMessage("CM Notification Executed and RuntimeDB Updated. ");
		}

		RpcResult<NbrlistChangeNotificationOutput> rpcResult = RpcResultBuilder
				.<NbrlistChangeNotificationOutput>status(true).withResult(serviceDataBuilder.build()).build();

		LOG.info("Successful exit from nbrlist-change-notification ");

		return Futures.immediateFuture(rpcResult);
	}

	protected NbrlistChangeNotificationOutputBuilder getServiceData(String svcOperation) {
		switch (svcOperation) {
		case NBRLIST_CHANGE_NOTIFICATION:
			return new NbrlistChangeNotificationOutputBuilder();
		}
		return null;
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
