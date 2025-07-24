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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.performancemanager.impl;

import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.PerformanceDataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.NetconfNetworkElementService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.PerformanceDataLtp;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerformanceManagerTask implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(PerformanceManagerTask.class);
    private static final String LOGMARKER = "PMTick";

    private int tickCounter = 0;

    private final ConcurrentHashMap<String, PerformanceDataProvider> queue = new ConcurrentHashMap<>();
    private final DataProvider databaseService;
    private final ScheduledExecutorService scheduler;
    private final long seconds;

    private ScheduledFuture<?> taskHandle = null;
    private Iterator<PerformanceDataProvider> neIterator = null;
    private PerformanceDataProvider actualNE = null;
    private final NetconfNetworkElementService netconfNetworkElementService;

    /**
     * Constructor of PM Task
     *
     * @param seconds                                     seconds to call PM Task
     * @param microwaveHistoricalPerformanceWriterService DB Service to load PM data to
     * @param netconfNetworkElementService                to write into log
     */

    public PerformanceManagerTask(long seconds, DataProvider microwaveHistoricalPerformanceWriterService,
            NetconfNetworkElementService netconfNetworkElementService) {

        LOG.info("Init task {} handling time {} seconds", PerformanceManagerTask.class.getSimpleName(), seconds);
        this.seconds = seconds;
        this.databaseService = microwaveHistoricalPerformanceWriterService;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.netconfNetworkElementService = netconfNetworkElementService;

    }

    /**
     * Start PM Task
     */
    public void start() {
        LOG.info("PM task created");
        taskHandle = this.scheduler.scheduleAtFixedRate(this, 0, seconds, TimeUnit.SECONDS);
        LOG.info("PM task scheduled");
    }

    /**
     * Stop everything
     */
    public void stop() {
        LOG.info("Stop {}", PerformanceManagerImpl.class.getSimpleName());
        if (taskHandle != null) {
            taskHandle.cancel(true);
            try {
                scheduler.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                LOG.debug("Scheduler stopped.", e);
                // Restore interrupted state...
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Add NE/Mountpoint to PM Processig
     *
     * @param mountPointNodeName to be added
     * @param ne                 that is connected to the mountpoint
     */
    public void registration(String mountPointNodeName, NetworkElement ne) {

        Optional<PerformanceDataProvider> oPmNe = ne.getService(PerformanceDataProvider.class);
        if (oPmNe.isPresent()) {
            queue.put(mountPointNodeName, oPmNe.get());
        }
    }

    /**
     * Remove mountpoint/NE from PM process
     *
     * @param mountPointNodeName that has to be removed
     */
    public void deRegistration(String mountPointNodeName) {
        LOG.debug("Deregister {}", mountPointNodeName);
        PerformanceDataProvider removedNE = queue.remove(mountPointNodeName);

        if (removedNE == null) {
            LOG.warn("Couldn't delete {}", mountPointNodeName);
        }
    }

    /*--------------------------------------------------------------
     * Task to read PM data from NE
     */

    /**
     * Task runner to read all performance data from Network Elements. Catch exceptions to make sure, that the Task is
     * not stopped.
     */
    @Override
    public void run() {

        String mountpointName = "No NE";
        final Optional<NetconfAccessor> acc = actualNE != null ? actualNE.getAcessor() : Optional.empty();
        if (acc.isPresent()) {
            mountpointName = acc.get().getNodeId().getValue();
        }
        LOG.debug("{} start {} Start with mountpoint {}", LOGMARKER, tickCounter, mountpointName);

        // Proceed to next NE/Interface
        getNextInterface(mountpointName);

        LOG.debug("{} {} Next interface to handle {}", LOGMARKER, tickCounter,
                actualNE == null ? "No NE/IF" : actualNE.pmStatusToString());

        if (actualNE != null) {
            try {
                LOG.debug("{} Start to read PM from NE ({})", LOGMARKER, tickCounter);
                Optional<PerformanceDataLtp> allPm = actualNE.getLtpHistoricalPerformanceData();
                if (allPm.isPresent()) {
                    LOG.debug("{} {} Got PM list. Start write to DB", LOGMARKER, tickCounter);
                    databaseService.doWritePerformanceData(allPm.get().getList());
                }
                LOG.debug("{} {} PM List end.", LOGMARKER, tickCounter);
            } catch (Throwable e) {
                LOG.debug("{} {} PM Exception", LOGMARKER, tickCounter);
                String msg = new StringBuffer().append(e.getMessage()).toString();
                LOG.warn("{} {} PM read/write failed. Write log entry {}", LOGMARKER, tickCounter, msg);
                netconfNetworkElementService.writeToEventLog(mountpointName, "PM Problem", msg);
            }
        }

        LOG.debug("{} end {}", LOGMARKER, tickCounter);
        tickCounter++;
    }

    /**
     * Reset queue to start from beginning
     */
    private void resetQueue() {
        actualNE = null;
        neIterator = null;
    }

    /**
     * Get then next interface in the list. First try to find a next on the actual NE. If not available search next
     * interface at a NE Special Situations to handle: Empty queue, NEs, but no interfaces
     */
    private void getNextInterface(String mountpointName) {
        boolean started = false;
        int loopCounter = 0;

        LOG.debug("{} {} getNextInterface enter. Queue size {} ", LOGMARKER, tickCounter, queue.size());

        if (actualNE != null && !queue.containsValue(actualNE)) {
            LOG.debug("{} {} NE Removed duringprocessing A", LOGMARKER, tickCounter);
            resetQueue();
        }

        while (true) {

            if (loopCounter++ >= 1000) {
                LOG.error("{} {} Problem in PM iteration. endless condition reached", LOGMARKER, tickCounter);
                resetQueue();
                break;
            }

            LOG.debug("{} {} Loop ne {}:neiterator {}:Interfaceiterator:{} Loop:{}", LOGMARKER, tickCounter,
                    actualNE == null ? "null" : mountpointName, neIterator == null ? "null" : neIterator.hasNext(),
                    actualNE == null ? "null" : actualNE.hasNext(), loopCounter);

            if (actualNE != null && actualNE.hasNext()) {
                // Yes, there is an interface, deliver back
                LOG.debug("{} {} getNextInterface yes A", LOGMARKER, tickCounter);
                actualNE.next();
                break;

            } else {
                // No element in neInterfaceInterator .. get next NE and try
                if (neIterator != null && neIterator.hasNext()) {
                    // Set a new NE
                    LOG.debug("{} {} Next NE A", LOGMARKER, tickCounter);
                    actualNE = neIterator.next();
                    actualNE.resetPMIterator();

                } else {
                    // Goto start condition 1) first entry 2) end of queue reached
                    LOG.debug("{} {} Reset", LOGMARKER, tickCounter);
                    resetQueue();

                    if (queue.isEmpty()) {
                        LOG.debug("{} {} no nextInterfac. queue empty", LOGMARKER, tickCounter);
                        break;
                    } else if (!started) {
                        LOG.debug("{} {} getNextInterface start condition. Get interator.", LOGMARKER, tickCounter);
                        neIterator = queue.values().iterator();
                        started = true;
                    } else {
                        LOG.debug("{} {} no nextInterface", LOGMARKER, tickCounter);
                        break;
                    }
                }
            }
        } // while

        if (actualNE != null && !queue.containsValue(actualNE)) {
            LOG.debug("{} {} NE Removed duringprocessing B", LOGMARKER, tickCounter);
            resetQueue();
        }

    }
}
