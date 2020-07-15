/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 *
 */
package org.onap.ccsdk.features.sdnr.wt.dataprovider.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.onap.ccsdk.features.sdnr.wt.common.database.HtDatabaseClient;
import org.onap.ccsdk.features.sdnr.wt.common.database.SearchResult;
import org.onap.ccsdk.features.sdnr.wt.common.database.config.HostInfo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.Entity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.read.mediator.server.list.output.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MediatorServerDataProvider implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(MediatorServerDataProvider.class);

    private final HtDatabaseClient dbClient;
    private final DataObjectAcessorWithId<Data> mediatorserverRW;
    private final int REFRESH_INTERVAL = 60;
    private final Map<String, Data> entries;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private boolean isRunning;

    public MediatorServerDataProvider(HostInfo[] hosts) throws Exception {
        this(hosts, null, null);
    }

    public MediatorServerDataProvider(HostInfo[] hosts, String authUsername, String authPassword) throws Exception {
        super();
        LOG.info("Start {}", this.getClass().getName());
        this.entries = new HashMap<>();
        this.dbClient = HtDatabaseClient.getClient(hosts, authUsername, authPassword);
        this.mediatorserverRW = new DataObjectAcessorWithId<>(dbClient, Entity.MediatorServer, Data.class);
        this.scheduler.scheduleAtFixedRate(onTick, this.REFRESH_INTERVAL, this.REFRESH_INTERVAL, TimeUnit.SECONDS);
    }

    private final Runnable onTick = new Runnable() {

        @Override
        public void run() {
            isRunning = true;
            runIt();
            isRunning = false;
        }

    };

    private void runIt() {
        SearchResult<Data> result = MediatorServerDataProvider.this.mediatorserverRW.doReadAll();
        List<Data> data = result.getHits();
        for (Data item : data) {
            MediatorServerDataProvider.this.entries.put(item.getId(), item);
        }
    }

    /**
     *
     * @param dbServerId
     * @return url or null if not exists
     */
    public String getHostUrl(String dbServerId) {
        Data info = this.entries.getOrDefault(dbServerId, null);
        return info == null ? null : info.getUrl();
    }

    public boolean triggerReloadSync() {
        if (!isRunning) {
            runIt();
        }
        return true;
    }

    @Override
    public void close() throws Exception {
        this.scheduler.shutdown();
    }
}
