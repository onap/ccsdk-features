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
package org.onap.ccsdk.features.sdnr.wt.common.database.requests;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.elasticsearch.client.Request;
import org.json.JSONObject;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseRequest {

	private static final Logger LOG = LoggerFactory.getLogger(BaseRequest.class);

	protected final Request request;
	private String query;
	public BaseRequest(String method, String endpoint) {
		LOG.debug("create request {} {}", method, endpoint);
		this.request = new Request(method, endpoint);
		query=null;
	}

	public Request getInner() {

		return this.request;
	}

	public static String urlEncodeValue(String value) {
		if (value == null)
			return null;
		try {
			return URLEncoder.encode(value, StandardCharsets.UTF_8.toString()).replace("+", "%20");
		} catch (UnsupportedEncodingException ex) {
			LOG.warn("encoding problem: {}", ex.getMessage());
		}
		return value;
	}
	@Override
	public String toString() {
		return this.request.getMethod() + " "+this.request.getEndpoint()+ " : "+(this.query!=null?this.query:"no query");
	}

	protected void setQuery(QueryBuilder query) {
		this.setQuery(query.toJSON());
	}

	public void setQuery(JSONObject o) {
		this.setQuery(o.toString());
	}

	public void setQuery(String content) {
		this.query=content;
		LOG.trace("query={}",content);
		this.request.setJsonEntity(this.query);
	}
}
