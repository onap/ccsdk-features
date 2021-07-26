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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.HtDatabaseMediatorserver;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.MediatorServerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MediatorServerDataProvider implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(MediatorServerDataProvider.class);

    private final HtDatabaseMediatorserver dbClient;
    private final int REFRESH_INTERVAL = 60;
    private final Map<String, MediatorServerEntity> entries;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private boolean isRunning;

    public MediatorServerDataProvider(HtDatabaseMediatorserver entryProvider) {
        this.entries = new HashMap<>();
        this.dbClient = entryProvider;
        this.scheduler.scheduleAtFixedRate(onTick, this.REFRESH_INTERVAL, this.REFRESH_INTERVAL, TimeUnit.SECONDS);
        LOG.info("Start {}", this.getClass().getName());
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
        List<MediatorServerEntity> result = this.dbClient.getAll();
        for (MediatorServerEntity item : result) {
            MediatorServerDataProvider.this.entries.put(item.getId(), item);
        }
    }

    /**
     *
     * @param dbServerId
     * @return url or null if not exists
     */
    public String getHostUrl(String dbServerId) {
        MediatorServerEntity info = this.entries.getOrDefault(dbServerId, null);
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
