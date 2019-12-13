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
package org.onap.ccsdk.features.sdnr.wt.apigateway.database;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.onap.ccsdk.features.sdnr.wt.apigateway.database.http.BaseHTTPClient;
import org.onap.ccsdk.features.sdnr.wt.apigateway.database.http.BaseHTTPResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseHttpClient extends BaseHTTPClient {

	private static Logger LOG = LoggerFactory.getLogger(DatabaseHttpClient.class);
	private static final String URI = "/mediator-server/mediator-server/_search";
	private final Map<String, String> headers;

	public DatabaseHttpClient(String base, boolean trustAllCerts) {
		super(base, trustAllCerts);
		this.headers = this.getHeaders();
	}

	private Map<String, String> getHeaders() {
		Map<String, String> h = new HashMap<String, String>();
		h.put("Content-Type", "application/json");
		return h;
	}

	public Map<String, MediatorServerInfo> requestEntries() {
		Map<String, MediatorServerInfo> entries = new HashMap<String, MediatorServerInfo>();
		BaseHTTPResponse response = null;
		try {
			response = this.sendRequest(URI, "GET", (String) null, this.headers);
		} catch (IOException e) {
			LOG.warn("problem reading db entries of mediator server: {}", e.getMessage());
		}
		if (response != null && response.code == BaseHTTPResponse.CODE200) {
			try {
				JSONObject o = new JSONObject(response.body);
				if (o.has("hits")) {
					JSONObject hits = o.getJSONObject("hits");
					if (hits.has("hits")) {
						JSONArray hitsarray = hits.getJSONArray("hits");
						if (hitsarray.length() > 0) {
							for (int i = 0; i < hitsarray.length(); i++) {
								JSONObject entry = hitsarray.getJSONObject(i);
								entries.put(entry.getString("_id"),
										new MediatorServerInfo(entry.getJSONObject("_source").getString("name"),
												entry.getJSONObject("_source").getString("url")));
							}
						}

					}
				}
			} catch (Exception e) {
				LOG.warn("problem parsing response: {} | e={}", response, e.getMessage());
			}
		}
		return entries;
	}

	
}
