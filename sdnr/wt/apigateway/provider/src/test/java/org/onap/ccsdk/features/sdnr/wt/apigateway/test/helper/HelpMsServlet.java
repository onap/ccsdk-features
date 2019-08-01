/*
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK.apps.sdnr.wt.apigateway
 * ================================================================================
 * Copyright (C) 2018 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.apigateway.test.helper;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.ccsdk.features.sdnr.wt.apigateway.MsServlet;
import org.onap.ccsdk.features.sdnr.wt.apigateway.database.MediatorServerInfo;

public class HelpMsServlet extends MsServlet implements IPublicServlet {

	public static final String RESPONSE_GET = "This is the response get";
	public static final String RESPONSE_POST = "This is the response post";
	public static final String RESPONSE_PUT = "This is the response put";
	public static final String RESPONSE_DELETE = "This is the response delete";
	public static final String RESPONSE_OPTIONS = "This is the response options";
	private boolean offline = true;
	private String baseurl;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doGet(req, resp);
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doPost(req, resp);
	}

	@Override
	public void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doPut(req, resp);
	}

	@Override
	public void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doOptions(req, resp);
	}

	@Override
	public void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doDelete(req, resp);
	}

	public void setOfflineStatus(boolean offline) {
		this.offline = offline;
	}

	public void setBaseUrl(String url) {
		this.baseurl = url;
	}

	@Override
	protected boolean isOff() {
		return this.offline;
	}

	@Override
	protected String getBaseUrl(String dbServerId) {
		return this.baseurl;
	}
	public void setMediatorEntries(Map<String, MediatorServerInfo> entries) {
		this.getEntryProvider().setEntries(entries);
	}
}
