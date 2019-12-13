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
package org.onap.ccsdk.features.sdnr.wt.apigateway.test;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.apigateway.database.http.BaseHTTPClient;
import org.onap.ccsdk.features.sdnr.wt.apigateway.test.helper.HelpServletBase;

public class TestHttpClient {

	private static final int PORT =40010;
	private static final String BASEURI = "/test";
	
	private class TestBaseHTTPClient extends BaseHTTPClient {
		public TestBaseHTTPClient() {
			super(String.format("http://localhost:%s%s",PORT,BASEURI));
		}
		public void doTest() {
			String[] methods=new String[] {"GET","POST","PUT","DELETE"};
			Map<String, String> headers=new HashMap<String,String>();
			headers.put("Content-Type","application/json");
			headers.put("Authorization",BaseHTTPClient.getAuthorizationHeaderValue("admin","admin"));
			for(String method:methods) {
				try {
					this.sendRequest(String.format("%s%s", BASEURI,"/abc"), method, "abddef", headers);
				} catch (IOException e) {
					e.printStackTrace();
					fail(String.format("problem with method %s: %s",method,e.getMessage()));
				}
			}
		}
	}
	@Test
	public void test() {
		TestBaseHTTPClient client = new TestBaseHTTPClient();
		client.doTest();
	}	
	
	
	@Before
	public void init() throws IOException{	
		HelpServletBase.initEsTestWebserver(PORT);
	}
	@After
	public void deinit() {
		HelpServletBase.stopTestWebserver();
	}
}
