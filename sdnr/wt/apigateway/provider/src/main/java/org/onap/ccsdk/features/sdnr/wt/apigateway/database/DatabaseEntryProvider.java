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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class DatabaseEntryProvider implements AutoCloseable {

	private final DatabaseHttpClient httpClient;
	private int refreshInterval;
	private final Map<String, MediatorServerInfo> entries;
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private boolean isRunning;

	protected DatabaseEntryProvider (DatabaseHttpClient httpClient,int refreshInterval) {
		this.httpClient = httpClient;
		this.refreshInterval = refreshInterval;
		this.entries = new HashMap<String, MediatorServerInfo>();
		this.isRunning = false;
		this.scheduler.scheduleAtFixedRate(onTick, this.refreshInterval, this.refreshInterval, TimeUnit.SECONDS);
	}
	public DatabaseEntryProvider(String dbBaseUri, int refreshInterval) {

		this.httpClient = new DatabaseHttpClient(dbBaseUri, false);
		this.refreshInterval = refreshInterval;
		this.entries = new HashMap<String, MediatorServerInfo>();
		this.isRunning = false;
		this.scheduler.scheduleAtFixedRate(onTick, this.refreshInterval, this.refreshInterval, TimeUnit.SECONDS);
	}

	private final Runnable onTick = new Runnable() {

		@Override
		public void run() {
			isRunning = true;
			Map<String, MediatorServerInfo> map = DatabaseEntryProvider.this.httpClient.requestEntries();
			DatabaseEntryProvider.this.entries.putAll(map);
			isRunning = false;
		}

	};

	public String getHostUrl(String dbServerId) {
		MediatorServerInfo info = this.entries.getOrDefault(dbServerId, null);
		return info == null ? null : info.getHost();
	}

	@Override
	public void close() throws Exception {
		this.scheduler.shutdown();
	}

	public boolean triggerReloadSync() {
		new Thread(onTick).start();
		int i=20;
		while(isRunning && i-->0) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				Thread.interrupted();
			}			
		}
		return i>0;
	}

	public void setEntries(Map<String, MediatorServerInfo> e) {
	
		this.entries.clear();
		this.entries.putAll(e);	
	}
	public String printEntries() {
		String s="";
		if(this.entries==null || this.entries.size()<=0) {
			return "empty";
		}
		for(Entry<String, MediatorServerInfo> entry:this.entries.entrySet()) {
			s+=String.format("%s:%s", entry.getKey(),entry.getValue().toString()+"\n");
		}
		return s;
	}

}
