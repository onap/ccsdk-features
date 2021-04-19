/*
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
 */
package org.onap.ccsdk.features.sdnr.wt.devicemanager.archiveservice;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.filechange.IConfigChangedListener;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.ArchiveCleanProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.IEsConfig;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceRegistration;
import org.opendaylight.mdsal.singleton.common.api.ServiceGroupIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArchiveCleanService implements AutoCloseable, IConfigChangedListener, Runnable, ClusterSingletonService {

    private static final Logger LOG = LoggerFactory.getLogger(ArchiveCleanService.class);
    private static final ServiceGroupIdentifier IDENT =
            ServiceGroupIdentifier.create("ElasticSearchArchiveCleanService");

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ArchiveCleanProvider[] indexCleanList;
    private final Runnable doClean;

    private final IEsConfig esConfig;
    private Future<?> taskReference;
    private boolean isMaster;
    private final ClusterSingletonServiceRegistration cssRegistration;

    public ArchiveCleanService(IEsConfig config, ClusterSingletonServiceProvider clusterSingletonServiceProvider,
            ArchiveCleanProvider... indexCleanList) {

        this.esConfig = config;
        this.esConfig.registerConfigChangedListener(this);

        this.indexCleanList = indexCleanList;
        this.doClean = this;
        this.taskReference = null;

        this.reinit();

        this.cssRegistration = clusterSingletonServiceProvider.registerClusterSingletonService(this);

    }

    private void reinit() {

        if (taskReference != null) {
            taskReference.cancel(false);
        }
        if (this.isMaster) {
            if (this.esConfig.getArchiveCheckIntervalSeconds() > 0) {
                LOG.info("DBCleanService is turned on for entries older than {} seconds",
                        this.esConfig.getArchiveLifetimeSeconds());
                taskReference = this.scheduler.scheduleAtFixedRate(doClean, 0,
                        this.esConfig.getArchiveCheckIntervalSeconds(), TimeUnit.SECONDS);
            } else {
                LOG.info("DBCleanService is turned off");
            }
        } else {
            LOG.info("service is inactive on this node. active on another node.");
        }
    }

    public Date getDateForOldElements() {
        return new Date(System.currentTimeMillis() - esConfig.getArchiveLifetimeSeconds() * 1000);
    }

    public int countOldEntries() {
        int cnt = 0;
        Date olderAreOutdated = getDateForOldElements();
        for (ArchiveCleanProvider indexCleanElement : indexCleanList) {
            if (indexCleanElement != null) {
                indexCleanElement.getNumberOfOldObjects(olderAreOutdated);
            }
        }
        return cnt;
    }

    @Override
    public void run() {

        try {
            int removed = 0;
            Date olderElementToBeRemoved = getDateForOldElements();
            LOG.trace("cleaning logs from entries older than {}", olderElementToBeRemoved);

            for (ArchiveCleanProvider indexCleanElement : indexCleanList) {
                if (indexCleanElement != null) {
                    removed += indexCleanElement.doIndexClean(olderElementToBeRemoved);
                }
            }
            if (removed > 0) {
                LOG.trace("Removed elements: {}", removed);
            }
        } catch (Exception e) {
            LOG.warn("problem executing dbclean", e);
        }
    }

    @Override
    public void onConfigChanged() {
        LOG.info("Not yet implemented. config changed. reninit timer");
    };

    @Override
    public void close() throws Exception {
        this.esConfig.unregisterConfigChangedListener(this);
        this.scheduler.shutdown();
        if (this.cssRegistration != null)
            this.cssRegistration.close();
    }

    @Override
    public String toString() {
        return "ArchivCleanService [ArchiveCheckIntervalSeconds=" + esConfig.getArchiveCheckIntervalSeconds()
                + "ArchiveLifetimeSeconds=" + esConfig.getArchiveLifetimeSeconds() + "]";
    }

    @Override
    public @NonNull ServiceGroupIdentifier getIdentifier() {
        return IDENT;
    }

    @Override
    public void instantiateServiceInstance() {
        LOG.info("We take Leadership");
        this.isMaster = true;
        this.reinit();
    }

    @Override
    public ListenableFuture<? extends Object> closeServiceInstance() {
        LOG.info("We lost Leadership");
        this.isMaster = false;
        this.reinit();
        return Futures.immediateFuture(null);
    }
}
