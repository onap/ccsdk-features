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

 package org.onap.ccsdk.features.sdnr.northbound.cmnotify;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.provider.MdsalHelper;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicService;

import org.onap.ccsdk.features.sdnr.northbound.cmnotify.CMNotifyClient;

import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev200224.NbrlistChangeNotificationInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev200224.NbrlistChangeNotificationInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev200224.NbrlistChangeNotificationOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev200224.NbrlistChangeNotificationOutputBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CMNotifyClientTest {

 	SvcLogicService mockSvcLogicService;
 	String module = "test-module";
 	String rpc = "test-rpc";
 	String version = "test-version";
 	String mode = "test-mode";
 	Properties localProp = new Properties();

 	@Before
 	public void setUp() throws Exception {
 		mockSvcLogicService = mock(SvcLogicService.class);
 		when(mockSvcLogicService.hasGraph(module, rpc, version, mode)).thenReturn(true);
 	}

 	@Test
 	public void testCMNotifyClientConstructor() {
 		CMNotifyClient CMNotifyClient = new CMNotifyClient(mockSvcLogicService);
 		assertNotNull(CMNotifyClient);
 	}

 	@Test
 	public void testHasGraph() throws SvcLogicException {
 		CMNotifyClient CMNotifyClient = new CMNotifyClient(mockSvcLogicService);
 		boolean result = CMNotifyClient.hasGraph(module, rpc, version, mode);
 		assertTrue(result);
 	}

 	@Test
 	public void testExecuteSvcLogicStatusFailure() throws SvcLogicException {
 		NbrlistChangeNotificationOutputBuilder serviceData = mock(NbrlistChangeNotificationOutputBuilder.class);
 		Properties parms = mock(Properties.class);
 		SvcLogicService svcLogicService = mock(SvcLogicService.class);
 		Properties properties = new Properties();
 		properties.setProperty("SvcLogic.status", "failure");
 		when(svcLogicService.execute(module, rpc, version, mode, properties)).thenReturn(properties);
 		CMNotifyClient sliClient = new CMNotifyClient(svcLogicService);
 		Properties prop = sliClient.execute(module, rpc, version, mode, serviceData, properties);
 		assertTrue(prop != null);
 	}
}
