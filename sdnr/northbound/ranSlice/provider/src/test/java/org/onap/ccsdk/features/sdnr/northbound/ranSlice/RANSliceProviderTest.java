/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.features.sdnr.northbound.ranSlice;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.SvcLogicLoader;
import org.onap.ccsdk.sli.core.sli.SvcLogicStore;
import org.onap.ccsdk.sli.core.sli.SvcLogicStoreFactory;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicClassResolver;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicPropertiesProviderImpl;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicServiceImpl;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
/*import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
*/import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev200806.*;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev200806.common.header.CommonHeaderBuilder;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.concepts.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RANSliceProviderTest {

  	Logger LOG = LoggerFactory.getLogger(RANSliceProvider.class);
  	private RANSliceProvider provider;

      /**
       * @throws java.lang.Exception
       */
      @Before
      public void setUp() throws Exception {
          DataBroker dataBroker = mock(DataBroker.class);
          DOMDataBroker domDataBroker = mock(DOMDataBroker.class);
          NotificationPublishService notifyService = mock(NotificationPublishService.class);
          //RpcProviderRegistry rpcRegistry = mock(RpcProviderRegistry.class);
          RpcProviderService rpcRegistry = mock(RpcProviderService.class);
          Registration rpcRegistration = mock(Registration.class);
          //when(rpcRegistry.registerRpcImplementation(any(Class.class), any(RanSliceApiService.class))).thenReturn(rpcRegistration);


          // Load svclogic.properties and get a SvcLogicStore
          InputStream propStr = RANSliceProviderTest.class.getResourceAsStream("/svclogic.properties");
          Properties svcprops = new Properties();
          svcprops.load(propStr);

          SvcLogicStore store = SvcLogicStoreFactory.getSvcLogicStore(svcprops);

          assertNotNull(store);

          URL graphUrl = RANSliceProviderTest.class.getClassLoader().getResource("graphs");
          LOG.info("Graph URL:" + graphUrl);


          if (graphUrl == null) {
              fail("Cannot find graphs directory");
          }

          SvcLogicLoader loader = new SvcLogicLoader(graphUrl.getPath(), store);
          loader.loadAndActivate();

          // Create a ServiceLogicService
          SvcLogicServiceImpl svc = new SvcLogicServiceImpl(new SvcLogicPropertiesProviderImpl(),
  				new SvcLogicClassResolver());

          // Finally ready to create sliapiProvider
          RANSliceClient client = new RANSliceClient(svc);
			provider = new RANSliceProvider();
			provider.setDataBroker(dataBroker);
			provider.setDomDataBroker(domDataBroker);
			provider.setNotificationPublishService(notifyService);
			provider.setClient(client);
			provider.init();
			

      }

      /**
       * @throws java.lang.Exception
       */
      @After
      public void tearDown() throws Exception {
          //provider.close();
      }


  	@Test
  	public void testConfigureNearRTRIC() {
  		ConfigureNearRTRICInputBuilder builder = new ConfigureNearRTRICInputBuilder();

  		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
  		hdrBuilder.setApiVer("1");
  		hdrBuilder.setFlags(null);
  		hdrBuilder.setOriginatorId("jUnit");
  		hdrBuilder.setRequestId("123");
  		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
  		builder.setCommonHeader(hdrBuilder.build());

  		builder.setAction(Action.Reconfigure);

  		try {
  			ConfigureNearRTRICOutput results = provider.configureNearRTRIC(builder.build()).get().getResult();
  			LOG.info("configureNearRTRIC returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
  			assert(results.getStatus().getCode().intValue() == 400);
  		} catch (InterruptedException | ExecutionException e) {
  			LOG.error("Caught exception", e);
  			fail("configureNearRTRIC threw exception");
  		}

  	}


    @Test
  	public void testInstantiateRANSlice() {
  		InstantiateRANSliceInputBuilder builder = new InstantiateRANSliceInputBuilder();

  		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
  		hdrBuilder.setApiVer("1");
  		hdrBuilder.setFlags(null);
  		hdrBuilder.setOriginatorId("jUnit");
  		hdrBuilder.setRequestId("123");
  		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
  		builder.setCommonHeader(hdrBuilder.build());

  		builder.setAction(Action.Reconfigure);

  		try {
  			InstantiateRANSliceOutput results = provider.instantiateRANSlice(builder.build()).get().getResult();
  			LOG.info("instantiateRANSlice returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
  			assert(results.getStatus().getCode().intValue() == 400);
  		} catch (InterruptedException | ExecutionException e) {
  			LOG.error("Caught exception", e);
  			fail("instantiateRANSlice threw exception");
  		}

  	}


    @Test
  	public void testConfigureRANSliceInstance() {
  		ConfigureRANSliceInstanceInputBuilder builder = new ConfigureRANSliceInstanceInputBuilder();

  		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
  		hdrBuilder.setApiVer("1");
  		hdrBuilder.setFlags(null);
  		hdrBuilder.setOriginatorId("jUnit");
  		hdrBuilder.setRequestId("123");
  		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
  		builder.setCommonHeader(hdrBuilder.build());

  		builder.setAction(Action.Reconfigure);

  		try {
  			ConfigureRANSliceInstanceOutput results = provider.configureRANSliceInstance(builder.build()).get().getResult();
  			LOG.info("configureRANSliceInstance returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
  			assert(results.getStatus().getCode().intValue() == 400);
  		} catch (InterruptedException | ExecutionException e) {
  			LOG.error("Caught exception", e);
  			fail("configureRANSliceInstance threw exception");
  		}

  	}

    @Test
  	public void testConfigureCU() {
  		ConfigureCUInputBuilder builder = new ConfigureCUInputBuilder();

  		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
  		hdrBuilder.setApiVer("1");
  		hdrBuilder.setFlags(null);
  		hdrBuilder.setOriginatorId("jUnit");
  		hdrBuilder.setRequestId("123");
  		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
  		builder.setCommonHeader(hdrBuilder.build());

  		builder.setAction(Action.Reconfigure);

  		try {
  			ConfigureCUOutput results = provider.configureCU(builder.build()).get().getResult();
  			LOG.info("configureCU returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
  			assert(results.getStatus().getCode().intValue() == 400);
  		} catch (InterruptedException | ExecutionException e) {
  			LOG.error("Caught exception", e);
  			fail("configureCU threw exception");
  		}

  	}


    @Test
  	public void testConfigureDU() {
  		ConfigureDUInputBuilder builder = new ConfigureDUInputBuilder();

  		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
  		hdrBuilder.setApiVer("1");
  		hdrBuilder.setFlags(null);
  		hdrBuilder.setOriginatorId("jUnit");
  		hdrBuilder.setRequestId("123");
  		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
  		builder.setCommonHeader(hdrBuilder.build());

  		builder.setAction(Action.Reconfigure);

  		try {
  			ConfigureDUOutput results = provider.configureDU(builder.build()).get().getResult();
  			LOG.info("configureDU returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
  			assert(results.getStatus().getCode().intValue() == 400);
  		} catch (InterruptedException | ExecutionException e) {
  			LOG.error("Caught exception", e);
  			fail("configureDU threw exception");
  		}

  	}

    @Test
  	public void testActivateRANSliceInstance() {
  		ActivateRANSliceInstanceInputBuilder builder = new ActivateRANSliceInstanceInputBuilder();

  		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
  		hdrBuilder.setApiVer("1");
  		hdrBuilder.setFlags(null);
  		hdrBuilder.setOriginatorId("jUnit");
  		hdrBuilder.setRequestId("123");
  		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
  		builder.setCommonHeader(hdrBuilder.build());

  		builder.setAction(Action.Reconfigure);

  		try {
  			ActivateRANSliceInstanceOutput results = provider.activateRANSliceInstance(builder.build()).get().getResult();
  			LOG.info("activateRANSliceInstance returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
  			assert(results.getStatus().getCode().intValue() == 400);
  		} catch (InterruptedException | ExecutionException e) {
  			LOG.error("Caught exception", e);
  			fail("activateRANSliceInstance threw exception");
  		}

  	}


    @Test
  	public void testDeactivateRANSliceInstance() {
  		DeactivateRANSliceInstanceInputBuilder builder = new DeactivateRANSliceInstanceInputBuilder();

  		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
  		hdrBuilder.setApiVer("1");
  		hdrBuilder.setFlags(null);
  		hdrBuilder.setOriginatorId("jUnit");
  		hdrBuilder.setRequestId("123");
  		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
  		builder.setCommonHeader(hdrBuilder.build());

  		builder.setAction(Action.Reconfigure);

  		try {
  			DeactivateRANSliceInstanceOutput results = provider.deactivateRANSliceInstance(builder.build()).get().getResult();
  			LOG.info("deactivateRANSliceInstance returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
  			assert(results.getStatus().getCode().intValue() == 400);
  		} catch (InterruptedException | ExecutionException e) {
  			LOG.error("Caught exception", e);
  			fail("deactivateRANSliceInstance threw exception");
  		}

  	}

    @Test
  	public void testTerminateRANSliceInstance() {
  		TerminateRANSliceInstanceInputBuilder builder = new TerminateRANSliceInstanceInputBuilder();

  		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
  		hdrBuilder.setApiVer("1");
  		hdrBuilder.setFlags(null);
  		hdrBuilder.setOriginatorId("jUnit");
  		hdrBuilder.setRequestId("123");
  		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
  		builder.setCommonHeader(hdrBuilder.build());

  		builder.setAction(Action.Reconfigure);

  		try {
  			TerminateRANSliceInstanceOutput results = provider.terminateRANSliceInstance(builder.build()).get().getResult();
  			LOG.info("terminateRANSliceInstance returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
  			assert(results.getStatus().getCode().intValue() == 400);
  		} catch (InterruptedException | ExecutionException e) {
  			LOG.error("Caught exception", e);
  			fail("terminateRANSliceInstance threw exception");
  		}

  	}

    @Test
  	public void testDetermineRANSliceResources() {
  		DetermineRANSliceResourcesInputBuilder builder = new DetermineRANSliceResourcesInputBuilder();

  		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
  		hdrBuilder.setApiVer("1");
  		hdrBuilder.setFlags(null);
  		hdrBuilder.setOriginatorId("jUnit");
  		hdrBuilder.setRequestId("123");
  		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
  		builder.setCommonHeader(hdrBuilder.build());

  		builder.setAction(Action.Reconfigure);

  		try {
  			DetermineRANSliceResourcesOutput results = provider.determineRANSliceResources(builder.build()).get().getResult();
  			LOG.info("determineRANSliceResources returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
  			assert(results.getStatus().getCode().intValue() == 400);
  		} catch (InterruptedException | ExecutionException e) {
  			LOG.error("Caught exception", e);
  			fail("determineRANSliceResources threw exception");
  		}

  	}


    @Test
  	public void testConfigNotification() {
  		ConfigNotificationInputBuilder builder = new ConfigNotificationInputBuilder();

  		CommonHeaderBuilder hdrBuilder = new CommonHeaderBuilder();
  		hdrBuilder.setApiVer("1");
  		hdrBuilder.setFlags(null);
  		hdrBuilder.setOriginatorId("jUnit");
  		hdrBuilder.setRequestId("123");
  		hdrBuilder.setTimestamp(new ZULU(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date())));
  		builder.setCommonHeader(hdrBuilder.build());

  		builder.setAction(Action.Reconfigure);

  		try {
  			ConfigNotificationOutput results = provider.configNotification(builder.build()).get().getResult();
  			LOG.info("configNotification returned status {} : {}", results.getStatus().getCode(), results.getStatus().getMessage());
  			assert(results.getStatus().getCode().intValue() == 400);
  		} catch (InterruptedException | ExecutionException e) {
  			LOG.error("Caught exception", e);
  			fail("configNotification threw exception");
  		}

  	}


  }
