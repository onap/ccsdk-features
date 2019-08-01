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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EsServlet extends BaseServlet {

	public interface IRequestCallback{
		
		void onRequest(String uri,String method);
	}
	/**
	 *
	 */
	private static final long serialVersionUID = -3996363343749995011L;
	private static final String OFFLINE_RESPONSE_MESSAGE = "Database interface is offline";
	private static Logger LOG = LoggerFactory.getLogger(EsServlet.class);
	
	private static final Map<String,List<IRequestCallback>> requestCallbacks=new HashMap<String,List<IRequestCallback>>();
	
	public static void registerRequestCallback(String uri,IRequestCallback callback) {
		List<IRequestCallback> list=requestCallbacks.getOrDefault(uri, new ArrayList<IRequestCallback>());
		if(!list.contains(callback)) {
			list.add(callback);
		}
		requestCallbacks.put(uri, list);
	}
	public static void unregisterRequestCallback(String uri,IRequestCallback callback) {
		List<IRequestCallback> list=requestCallbacks.getOrDefault(uri, new ArrayList<IRequestCallback>());
		if(list.contains(callback)) {
			list.remove(callback);
		}
	}
	
	
	public EsServlet() {
		super();
	}

	private void handleCallbacks(String uri,String method) {
	
		LOG.debug("try to find callbacks for uri {}",uri);
		for(Entry<String,List<IRequestCallback>> entry:requestCallbacks.entrySet()) {
			if(uri.contains(entry.getKey())) {
				List<IRequestCallback> cblist = entry.getValue();
				if(cblist!=null && cblist.size()>0) {
					LOG.debug("found at least one");
					for(IRequestCallback cb :cblist) {
						cb.onRequest(uri, method);
					}
				}		
			}
		
		}
	}
	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (MyProperties.getInstance().corsEnabled()) {
			resp.addHeader("Access-Control-Allow-Origin", "*");
			resp.addHeader("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE");
			resp.addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
		}
		resp.setStatus(200);
	}

	@Override
	protected String getOfflineResponse() {
		return OFFLINE_RESPONSE_MESSAGE;
	}

	@Override
	protected boolean isOff() {
		return MyProperties.getInstance().isEsOff();
	}

	@Override
	protected String getRemoteUrl(String uri) {
		if (uri != null && uri.length() > 0) {
			uri = uri.substring("/database".length());
		}
		return MyProperties.getInstance().getEsBaseUrl() + uri;
	}
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doGet(req, resp);
		this.handleCallbacks(req.getRequestURI(),"GET");
	}
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doPost(req, resp);
		this.handleCallbacks(req.getRequestURI(),"POST");
	}
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doPut(req, resp);
		this.handleCallbacks(req.getRequestURI(),"PUT");
	}
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doDelete(req, resp);
		this.handleCallbacks(req.getRequestURI(),"DELETE");
	}
}	
