/*
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK.apps.sdnr.wt.apigateway
 * ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
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
package org.onap.ccsdk.features.sdnr.wt.apigateway;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.ccsdk.features.sdnr.wt.apigateway.EsServlet.IRequestCallback;
import org.onap.ccsdk.features.sdnr.wt.apigateway.database.DatabaseEntryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MsServlet extends BaseServlet {

	/**
	 *
	 */
	private static Logger LOG = LoggerFactory.getLogger(MsServlet.class);
	private static final long serialVersionUID = -5361461082028405171L;
	private static final String OFFLINE_RESPONSE_MESSAGE = "MediatorServer interface is offline";
	private static final String DATABASE_REQUEST_URI_REGEX = "/mwtn/mediator-server";
	private final DatabaseEntryProvider entryProvider;
	public MsServlet() {
		super();
		this.entryProvider = new DatabaseEntryProvider("http://localhost:9200/",60);
		EsServlet.registerRequestCallback(DATABASE_REQUEST_URI_REGEX, this.dbRequestCallback);
	}

	private final IRequestCallback dbRequestCallback = new IRequestCallback() {
		
		@Override
		public void onRequest(String uri, String method) {
			if(method=="POST"|| method=="PUT" || method=="DELETE") {
				LOG.debug("found mediator related request. trigger update of local entries");
				MsServlet.this.entryProvider.triggerReloadSync();
			}
			
		}
	};
	protected DatabaseEntryProvider getEntryProvider() {
		return this.entryProvider;
	}
	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setStatus(200);
	}

	@Override
	protected String getOfflineResponse() {
		return OFFLINE_RESPONSE_MESSAGE;
	}

	@Override
	protected boolean isOff() {
		return false;
	}

	@Override
	protected String getRemoteUrl(String uri) {
		String dbServerId = "0";
		if (uri == null)
			uri = "";
		if (uri.length() > 0) {
			uri = uri.substring("/ms/".length());
			int idx= uri.indexOf("/");
			dbServerId = uri.substring(0,idx);
			uri=uri.substring(idx);
		}
		LOG.debug("request for ms server with id={}",dbServerId);
		String url= this.getBaseUrl(dbServerId) + uri;
		LOG.debug("dest-url: {}",url);
		return url;
	}

	protected String getBaseUrl(String dbServerId) {
		return this.entryProvider.getHostUrl(dbServerId);
	}
}
