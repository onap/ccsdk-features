/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 			reserved.
 * 	Modifications Copyright © 2018 IBM.
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

package org.onap.ccsdk.features.sdnr.northbound.oranO1;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.onap.ccsdk.sli.core.sli.provider.MdsalHelper;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.binding.impl.AbstractForwardedDataBroker;
import org.opendaylight.controller.md.sal.dom.api.DOMDataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev200806.*;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev200806.common.header.CommonHeaderBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev200806.status.StatusBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.onap.ccsdk.features.sdnr.northbound.oranO1.OranO1ResponseCode.*;

/**
 * Defines a base implementation for your provider. This class extends from a
 * helper class which provides storage for the most commonly used components of
 * the MD-SAL. Additionally the base class provides some basic logging and
 * initialization / clean up methods.
 *
 */
public class OranO1Provider implements AutoCloseable, OranO1ApiService {

	private class CommonOranO1Fields {
		private StatusBuilder statusBuilder;
		private CommonHeaderBuilder commonHeaderBuilder;
		private Payload payload;

		public CommonOranO1Fields(StatusBuilder statusBuilder, CommonHeaderBuilder commonHeaderBuilder) {
			this.statusBuilder = statusBuilder;
			this.commonHeaderBuilder = commonHeaderBuilder;
			this.payload = null;
		}

		public CommonOranO1Fields(StatusBuilder statusBuilder, CommonHeaderBuilder commonHeaderBuilder, Payload payload) {
			this.statusBuilder = statusBuilder;
			this.commonHeaderBuilder = commonHeaderBuilder;
			this.payload = payload;
		}

		public StatusBuilder getStatusBuilder() {
			return statusBuilder;
		}

		public CommonHeaderBuilder getCommonHeaderBuilder() {
			return commonHeaderBuilder;
		}

		public Payload getPayload() {
			return payload;
		}
	}

	private static final Logger LOG = LoggerFactory.getLogger(OranO1Provider.class);

	private static final String exceptionMessage = "Caught exception";

	private static final String APPLICATION_NAME = "OranO1";

	private final ExecutorService executor;
	protected DataBroker dataBroker;
	protected DOMDataBroker domDataBroker;
	protected NotificationPublishService notificationService;
	protected RpcProviderRegistry rpcRegistry;
	private final OranO1Client OranO1Client;

	protected BindingAwareBroker.RpcRegistration<OranO1ApiService> rpcRegistration;

	public OranO1Provider(final DataBroker dataBroker, final NotificationPublishService notificationPublishService,
			final RpcProviderRegistry rpcProviderRegistry, final OranO1Client oranO1Client) {

		LOG.info("Creating provider for {}", APPLICATION_NAME);
		executor = Executors.newFixedThreadPool(1);
		this.dataBroker = dataBroker;
		if (dataBroker instanceof AbstractForwardedDataBroker) {
			domDataBroker = ((AbstractForwardedDataBroker) dataBroker).getDelegate();
		}
		notificationService = notificationPublishService;
		rpcRegistry = rpcProviderRegistry;
		this.OranO1Client = oranO1Client;
		initialize();
	}

	public void initialize() {
		LOG.info("Initializing {} for {}", this.getClass().getName(), APPLICATION_NAME);

		if (rpcRegistration == null) {
			if (rpcRegistry != null) {
				rpcRegistration = rpcRegistry.addRpcImplementation(OranO1ApiService.class, this);
				LOG.info("Initialization complete for {}", APPLICATION_NAME);
			} else {
				LOG.warn("Error initializing {} : rpcRegistry unset", APPLICATION_NAME);
			}
		}
	}

	protected void initializeChild() {
		// Override if you have custom initialization intelligence
	}

	@Override
	public void close() throws Exception {
		LOG.info("Closing provider for " + APPLICATION_NAME);
		executor.shutdown();
		rpcRegistration.close();
		LOG.info("Successfully closed provider for " + APPLICATION_NAME);
	}


//RPC configureNearRTRIC

	@Override
	public ListenableFuture<RpcResult<ConfigureNearRTRICOutput>> configureNearRTRIC(ConfigureNearRTRICInput input) {
		ConfigureNearRTRICInputBuilder iBuilder = new ConfigureNearRTRICInputBuilder(input);
		ConfigureNearRTRICOutputBuilder oBuilder = new ConfigureNearRTRICOutputBuilder();

		try {
			CommonOranO1Fields retval = callDG("configureNearRTRIC", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (OranO1RpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<ConfigureNearRTRICOutput> rpcResult =
				RpcResultBuilder.<ConfigureNearRTRICOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);

	}

	//RPC instantiateRANSlice

	@Override
	public ListenableFuture<RpcResult<InstantiateRANSliceOutput>> instantiateRANSlice(InstantiateRANSliceInput input) {
		InstantiateRANSliceInputBuilder iBuilder = new InstantiateRANSliceInputBuilder(input);
		InstantiateRANSliceOutputBuilder oBuilder = new InstantiateRANSliceOutputBuilder();

		try {
			CommonOranO1Fields retval = callDG("instantiateRANSlice", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (OranO1RpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<InstantiateRANSliceOutput> rpcResult =
				RpcResultBuilder.<InstantiateRANSliceOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);

	}



	//RPC configureRANSliceInstance

	@Override
	public ListenableFuture<RpcResult<ConfigureRANSliceInstanceOutput>> configureRANSliceInstance(ConfigureRANSliceInstanceInput input) {
		ConfigureRANSliceInstanceInputBuilder iBuilder = new ConfigureRANSliceInstanceInputBuilder(input);
		ConfigureRANSliceInstanceOutputBuilder oBuilder = new ConfigureRANSliceInstanceOutputBuilder();

		try {
			CommonOranO1Fields retval = callDG("configureRANSliceInstance", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (OranO1RpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<ConfigureRANSliceInstanceOutput> rpcResult =
				RpcResultBuilder.<ConfigureRANSliceInstanceOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);

	}

	//RPC configureCU

	@Override
	public ListenableFuture<RpcResult<ConfigureCUOutput>> configureCU(ConfigureCUInput input) {
		ConfigureCUInputBuilder iBuilder = new ConfigureCUInputBuilder(input);
		ConfigureCUOutputBuilder oBuilder = new ConfigureCUOutputBuilder();

		try {
			CommonOranO1Fields retval = callDG("configureCU", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (OranO1RpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<ConfigureCUOutput> rpcResult =
				RpcResultBuilder.<ConfigureCUOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);

	}

	//RPC configureDU

	@Override
	public ListenableFuture<RpcResult<ConfigureDUOutput>> configureDU(ConfigureDUInput input) {
		ConfigureDUInputBuilder iBuilder = new ConfigureDUInputBuilder(input);
		ConfigureDUOutputBuilder oBuilder = new ConfigureDUOutputBuilder();

		try {
			CommonOranO1Fields retval = callDG("configureDU", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (OranO1RpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<ConfigureDUOutput> rpcResult =
				RpcResultBuilder.<ConfigureDUOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);

	}

	//RPC activateRANSliceInstance

	@Override
	public ListenableFuture<RpcResult<ActivateRANSliceInstanceOutput>> activateRANSliceInstance(ActivateRANSliceInstanceInput input) {
		ActivateRANSliceInstanceInputBuilder iBuilder = new ActivateRANSliceInstanceInputBuilder(input);
		ActivateRANSliceInstanceOutputBuilder oBuilder = new ActivateRANSliceInstanceOutputBuilder();

		try {
			CommonOranO1Fields retval = callDG("activateRANSliceInstance", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (OranO1RpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<ActivateRANSliceInstanceOutput> rpcResult =
				RpcResultBuilder.<ActivateRANSliceInstanceOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);

	}


	//RPC deactivateRANSliceInstance

	@Override
	public ListenableFuture<RpcResult<DeactivateRANSliceInstanceOutput>> deactivateRANSliceInstance(DeactivateRANSliceInstanceInput input) {
		DeactivateRANSliceInstanceInputBuilder iBuilder = new DeactivateRANSliceInstanceInputBuilder(input);
		DeactivateRANSliceInstanceOutputBuilder oBuilder = new DeactivateRANSliceInstanceOutputBuilder();

		try {
			CommonOranO1Fields retval = callDG("deactivateRANSliceInstance", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (OranO1RpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<DeactivateRANSliceInstanceOutput> rpcResult =
				RpcResultBuilder.<DeactivateRANSliceInstanceOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);

	}

	//RPC terminateRANSliceInstance

	@Override
	public ListenableFuture<RpcResult<TerminateRANSliceInstanceOutput>> terminateRANSliceInstance(TerminateRANSliceInstanceInput input) {
		TerminateRANSliceInstanceInputBuilder iBuilder = new TerminateRANSliceInstanceInputBuilder(input);
		TerminateRANSliceInstanceOutputBuilder oBuilder = new TerminateRANSliceInstanceOutputBuilder();

		try {
			CommonOranO1Fields retval = callDG("terminateRANSliceInstance", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (OranO1RpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<TerminateRANSliceInstanceOutput> rpcResult =
				RpcResultBuilder.<TerminateRANSliceInstanceOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);

	}

	//RPC determineRANSliceResources

	@Override
	public ListenableFuture<RpcResult<DetermineRANSliceResourcesOutput>> determineRANSliceResources(DetermineRANSliceResourcesInput input) {
		DetermineRANSliceResourcesInputBuilder iBuilder = new DetermineRANSliceResourcesInputBuilder(input);
		DetermineRANSliceResourcesOutputBuilder oBuilder = new DetermineRANSliceResourcesOutputBuilder();

		try {
			CommonOranO1Fields retval = callDG("determineRANSliceResources", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (OranO1RpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<DetermineRANSliceResourcesOutput> rpcResult =
				RpcResultBuilder.<DetermineRANSliceResourcesOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);

	}

	//RPC cm-notify

	@Override
	public ListenableFuture<RpcResult<ConfigNotificationOutput>> configNotification(ConfigNotificationInput input) {

		ConfigNotificationInputBuilder iBuilder = new ConfigNotificationInputBuilder(input);
		ConfigNotificationOutputBuilder oBuilder = new ConfigNotificationOutputBuilder();

		try {
			CommonOranO1Fields retval = callDG("configNotification", iBuilder.build());
			oBuilder.setStatus(retval.getStatusBuilder().build());
			oBuilder.setCommonHeader(retval.getCommonHeaderBuilder().build());
		} catch (OranO1RpcInvocationException e) {
			LOG.debug(exceptionMessage, e);
			oBuilder.setCommonHeader(e.getCommonHeader());
			oBuilder.setStatus(e.getStatus());
		}

		RpcResult<ConfigNotificationOutput> rpcResult =
				RpcResultBuilder.<ConfigNotificationOutput> status(true).withResult(oBuilder.build()).build();
		// return error
		return Futures.immediateFuture(rpcResult);

	}


	private CommonOranO1Fields callDG(String rpcName, Object input) throws OranO1RpcInvocationException {

		StatusBuilder statusBuilder = new StatusBuilder();

		if (input == null) {
			LOG.debug("Rejecting " +rpcName+ " because of invalid input");
			statusBuilder.setCode(OranO1ResponseCode.REJECT_INVALID_INPUT.getValue());
			statusBuilder.setMessage("REJECT - INVALID INPUT.  Missing input");
			CommonHeaderBuilder hBuilder = new CommonHeaderBuilder();
			hBuilder.setApiVer("1");
			hBuilder.setOriginatorId("unknown");
			hBuilder.setRequestId("unset");
			hBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
			throw new OranO1RpcInvocationException(statusBuilder.build(), hBuilder.build());
		}

		CommonHeaderBuilder hBuilder = new CommonHeaderBuilder(((CommonHeader)input).getCommonHeader());

		// add input to parms
		LOG.info("Adding INPUT data for "+ rpcName +" input: " + input.toString());
		Properties inputProps = new Properties();
		MdsalHelper.toProperties(inputProps, input);

		LOG.info("Printing SLI parameters to be passed");

	// iterate properties file to get key-value pairs
		for (String key : inputProps.stringPropertyNames()) {
			String value = inputProps.getProperty(key);
			LOG.info("The SLI parameter in " + key + " is: " + value);
		}

		Properties respProps = new Properties();

		// Call SLI sync method
		try
		{
			if (OranO1Client.hasGraph("oranO1-api", rpcName , null, "sync"))
			{
				try
				{
					respProps = OranO1Client.execute("oranO1-api", rpcName, null, "sync", inputProps, domDataBroker);
				}
				catch (Exception e)
				{
					LOG.error("Caught exception executing service logic for "+ rpcName, e);
					statusBuilder.setCode(OranO1ResponseCode.FAILURE_DG_FAILURE.getValue());
					statusBuilder.setMessage("FAILURE - DG FAILURE ("+e.getMessage()+")");
					throw new OranO1RpcInvocationException(statusBuilder.build(), hBuilder.build());
				}
			} else {
				LOG.error("No service logic active for OranO1: '" + rpcName + "'");

				statusBuilder.setCode(OranO1ResponseCode.REJECT_DG_NOT_FOUND.getValue());
				statusBuilder.setMessage("FAILURE - DG not found for action "+rpcName);
				throw new OranO1RpcInvocationException(statusBuilder.build(), hBuilder.build());
			}
		}
		catch (Exception e)
		{
			LOG.error("Caught exception looking for service logic", e);

			statusBuilder.setCode(OranO1ResponseCode.FAILURE_DG_FAILURE.getValue());
			statusBuilder.setMessage("FAILURE - Unexpected error looking for DG ("+e.getMessage()+")");
			throw new OranO1RpcInvocationException(statusBuilder.build(), hBuilder.build());
		}


		StatusBuilder sBuilder = new StatusBuilder();
		MdsalHelper.toBuilder(respProps, sBuilder);
		MdsalHelper.toBuilder(respProps, hBuilder);

		Payload payload = null;
		String payloadValue = respProps.getProperty("payload");
		if (payloadValue != null) {
			payload = new Payload(payloadValue);
		}

		String statusCode = sBuilder.getCode().toString();

		if (!"400".equals(statusCode)) {
			LOG.error("Returned FAILED for "+rpcName+" error code: '" + statusCode + "'");
		} else {
			LOG.info("Returned SUCCESS for "+rpcName+" ");
		}

		return new CommonOranO1Fields(sBuilder, hBuilder, payload);

	}

}
