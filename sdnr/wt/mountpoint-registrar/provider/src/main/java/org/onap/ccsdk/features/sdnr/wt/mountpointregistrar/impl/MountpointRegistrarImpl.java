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

package org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onap.ccsdk.features.sdnr.wt.common.configuration.Configuration;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.filechange.IConfigChangedListener;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.MountPointService;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("deprecation")
public class MountpointRegistrarImpl implements AutoCloseable, IConfigChangedListener {

	private static final Logger LOG = LoggerFactory.getLogger(MountpointRegistrarImpl.class);
	private static final String APPLICATION_NAME = "mountpoint-registrar";
	private static final String CONFIGURATIONFILE = "etc/mountpoint-registrar.properties";

	private Thread dmaapVESMsgConsumerMain = null;
	@SuppressWarnings("unused")
	private DataBroker dataBroker = null;
	@SuppressWarnings("unused")
	private MountPointService mountPointService = null;
	@SuppressWarnings("unused")
	private RpcProviderRegistry rpcProviderRegistry = null;
	@SuppressWarnings("unused")
	private NotificationPublishService notificationPublishService = null;
	@SuppressWarnings("unused")
	private ClusterSingletonServiceProvider clusterSingletonServiceProvider;

	private GeneralConfig generalConfig;
	private boolean dmaapEnabled = false;
	Map<String, Configuration> configMap = new HashMap<>();

	// Blueprint 1
	public MountpointRegistrarImpl() {
		LOG.info("Creating provider class for {}", APPLICATION_NAME);
	}

	public void setDataBroker(DataBroker dataBroker) {
		this.dataBroker = dataBroker;
	}

	public void setRpcProviderRegistry(RpcProviderRegistry rpcProviderRegistry) {
		this.rpcProviderRegistry = rpcProviderRegistry;
	}

	public void setNotificationPublishService(NotificationPublishService notificationPublishService) {
		this.notificationPublishService = notificationPublishService;
	}

	public void setMountPointService(MountPointService mountPointService) {
		this.mountPointService = mountPointService;
	}

	public void setClusterSingletonService(ClusterSingletonServiceProvider clusterSingletonService) {
		this.clusterSingletonServiceProvider = clusterSingletonService;
	}

	public void init() {
		LOG.info("Init call for {}", APPLICATION_NAME);

		ConfigurationFileRepresentation configFileRepresentation = new ConfigurationFileRepresentation(CONFIGURATIONFILE);
		configFileRepresentation.registerConfigChangedListener(this);

		generalConfig = new GeneralConfig(configFileRepresentation);
		PNFRegistrationConfig pnfRegConfig = new PNFRegistrationConfig(configFileRepresentation);
		FaultConfig faultConfig = new FaultConfig(configFileRepresentation);

		configMap.put("pnfRegistration", pnfRegConfig);
		configMap.put("fault", faultConfig);

		dmaapEnabled = generalConfig.getEnabled();
		if (dmaapEnabled) { // start dmaap consumer thread only if dmaapEnabled=true
			LOG.info("DMaaP seems to be enabled, starting consumer(s)");
			dmaapVESMsgConsumerMain = new Thread(new DMaaPVESMsgConsumerMain(configMap));
			dmaapVESMsgConsumerMain.start();
		} else {
			LOG.info("DMaaP seems to be disabled, not starting any consumer(s)");
		}
	}

	/**
	 * Reflect status for Unit Tests
	 * @return Text with status
	 */
	public String isInitializationOk() {
		return "No implemented";
	}

	@Override
	public void onConfigChanged() {
		LOG.info("Service configuration state changed. Enabled: {}", generalConfig.getEnabled());
		boolean dmaapEnabledNewVal = generalConfig.getEnabled();
		if (!dmaapEnabled && dmaapEnabledNewVal) { // Dmaap disabled earlier (or during bundle startup) but enabled later, start Consumer(s)
			LOG.info("DMaaP is enabled, starting consumer(s)");
			dmaapVESMsgConsumerMain = new Thread(new DMaaPVESMsgConsumerMain(configMap));
			dmaapVESMsgConsumerMain.start();
		} else if (dmaapEnabled && !dmaapEnabledNewVal) { // Dmaap enabled earlier (or during bundle startup) but disabled later, stop consumer(s)
			LOG.info("DMaaP is disabled, stopping consumer(s)");
			List<DMaaPVESMsgConsumer> consumers = DMaaPVESMsgConsumerMain.getConsumers();
			for (DMaaPVESMsgConsumer consumer : consumers) {
				// stop all consumers
				consumer.stopConsumer();
			}
		}
		dmaapEnabled = dmaapEnabledNewVal;
	}

	@Override
	public void close() throws Exception {
		LOG.info("{} closing ...", this.getClass().getName());
		//close(updateService, configService, mwtnService); issue#1
		//close(updateService, mwtnService);
		LOG.info("{} closing done",APPLICATION_NAME);
	}

	/**
	 * Used to close all Services, that should support AutoCloseable Pattern
	 *
	 * @param toClose
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	private void close(AutoCloseable... toCloseList) throws Exception {
		for (AutoCloseable element : toCloseList) {
			if (element != null) {
				element.close();
			}
		}
	}


}
