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
/**
 * (c) 2017 highstreet technologies GmbH
 */

package org.onap.ccsdk.features.sdnr.wt.devicemanager.devicemonitor.impl;

import java.util.Enumeration;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.filechange.IConfigChangedListener;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.devicemonitor.impl.config.DmConfig;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.eventdatahandler.ODLEventListenerHandler;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.DeviceMonitoredNe;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.ne.service.NetworkElement;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of concept "Active monitoring" of a device.<br>
 * <br>
 * For each existing mountpoint a task runs with 120s cycle time. Every 120 seconds the check actions are performed. The
 * request is handled by the NETCONF layer with a (default)configured time-out of 60 seconds.<br>
 * Generated alarms, by the object/node "SDN-Controller" are (enum DeviceMonitorProblems):<br>
 * - notConnected(InternalSeverity.Warning)<br>
 * - noConnectionMediator(InternalSeverity.Minor)<br>
 * - noConnectionNe(InternalSeverity.Critical)<br>
 * <br>
 * 1. Mountpoint does not exist<br>
 * If the mountpoint does not exists there are no related current alarms in the database.<br>
 * <br>
 * 2. Created mountpoint with state "Connecting" or "UnableToConnect"<br>
 * If the Mountpoint is created and connection status is "Connecting" or "UnableToConnect".<br>
 * - After about 2..4 Minutes ... raise alarm "notConnected" with severity warning<br>
 * <br>
 * 3. Created mountpoint with state "Connection"<br>
 * There are two monitor activities.<br>
 * 3a. Check of Mediator connection by requesting (typical) cached data.<br>
 * - After about 60 seconds raise alarm: connection-loss-mediator with severity minor<br>
 * - Request from Mediator: network-element<br>
 * <br>
 * 3b. Check connection to NEby requesting (typical) non-cached data.<br>
 * - Only if AirInterface available. The first one is used.<br>
 * - Requested are the currentAlarms<br>
 * - After about 60 seconds raise alarm: connection-loss-network-element with severity critical<br>
 * <br>
 * 
 * @author herbert
 */

public class DeviceMonitorImpl implements DeviceMonitor, IConfigChangedListener {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceMonitorImpl.class);

    private final ConcurrentHashMap<String, DeviceMonitorTask> queue;
    private final ScheduledExecutorService scheduler;
    private final ODLEventListenerHandler odlEventListener;
    @SuppressWarnings("unused")
    private final DataBroker dataBroker; //Future usage
    private final DmConfig dmConfig;
    private final DeviceMonitoredNe dummyNe;

    /*-------------------------------------------------------------
     * Construction/ destruction of service
     */

    /**
     * Basic implementation of devicemonitoring
     * 
     * @param odlEventListener as destination for problems
     */
    public DeviceMonitorImpl(DataBroker dataBroker, ODLEventListenerHandler odlEventListener,
            ConfigurationFileRepresentation htconfig) {
        LOG.info("Construct {}", this.getClass().getSimpleName());

        this.odlEventListener = odlEventListener;
        this.dataBroker = dataBroker;
        this.dummyNe = getDummyNe();

        htconfig.registerConfigChangedListener(this);
        this.dmConfig = new DmConfig(htconfig);
        setDmConfig(dmConfig);

        this.queue = new ConcurrentHashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(10);
    }

    /**
     * Stop the service. Stop all running monitoring tasks.
     */
    @Override
    synchronized public void close() {
        LOG.info("Close {}", this.getClass().getSimpleName());

        Enumeration<String> e = queue.keys();
        while (e.hasMoreElements()) {
            deviceDisconnectIndication(e.nextElement());
        }

        scheduler.shutdown();
    }

    @Override
    public void onConfigChanged() {
        setDmConfig(dmConfig);
    }

    private void setDmConfig(DmConfig dmConfig) {
        for (DeviceMonitorProblems problem : DeviceMonitorProblems.values()) {
            problem.setSeverity(dmConfig.getSeverity(problem));
        }
    }

    /*-------------------------------------------------------------
     * Start/ stop/ update service for Mountpoint
     */

    /**
     * Notify of device state changes to "connected" for slave nodes
     * 
     * @param mountPointNodeName name of mount point
     */
    @Override
    synchronized public void deviceConnectSlaveIndication(String mountPointNodeName) {
        deviceConnectMasterIndication(mountPointNodeName, (DeviceMonitoredNe) null);
    }

    @Override
    public void deviceConnectMasterIndication(String mountPointNodeName, NetworkElement networkElement) {
        Optional<DeviceMonitoredNe> monitoredNe = networkElement.getService(DeviceMonitoredNe.class);
        deviceConnectMasterIndication(mountPointNodeName, monitoredNe.isPresent() ? monitoredNe.get() : dummyNe);
    }

    /**
     * Notify of device state changes to "connected"
     * 
     * @param mountPointNodeName name of mount point
     * @param ne to monitor
     */
    synchronized private void deviceConnectMasterIndication(String mountPointNodeName, @Nullable DeviceMonitoredNe ne) {

        LOG.debug("ne changes to connected state {}", mountPointNodeName);
        createMonitoringTask(mountPointNodeName);
        if (queue.containsKey(mountPointNodeName)) {
            DeviceMonitorTask task = queue.get(mountPointNodeName);
            task.deviceConnectIndication(ne);
        } else {
            LOG.warn("Monitoring task not in queue: {} {} {}", mountPointNodeName, mountPointNodeName.hashCode(),
                    queue.size());
        }
    }

    /**
     * Notify of device state change to "disconnected" Mount point supervision
     * 
     * @param mountPointNodeName to deregister
     */
    @Override
    synchronized public void deviceDisconnectIndication(String mountPointNodeName) {

        LOG.debug("State changes to not connected state {}", mountPointNodeName);
        createMonitoringTask(mountPointNodeName);
        if (queue.containsKey(mountPointNodeName)) {
            DeviceMonitorTask task = queue.get(mountPointNodeName);
            task.deviceDisconnectIndication();
        } else {
            LOG.warn("Monitoring task not in queue: {} {} {}", mountPointNodeName, mountPointNodeName.hashCode(),
                    queue.size());
        }
    }

    /**
     * removeMountpointIndication deregisters a mountpoint for registration services
     * 
     * @param mountPointNodeName to deregister
     */
    @Override
    synchronized public void removeMountpointIndication(String mountPointNodeName) {

        if (queue.containsKey(mountPointNodeName)) {
            DeviceMonitorTask task = queue.get(mountPointNodeName);
            //Remove from here
            queue.remove(mountPointNodeName);
            //Clear all problems
            task.removeMountpointIndication();
            LOG.debug("Task stopped: {}", mountPointNodeName);
        } else {
            LOG.warn("Task not in queue: {}", mountPointNodeName);
        }
    }

    /**
     * Referesh database by raising all alarms again.
     */
    @Override
    public void refreshAlarmsInDb() {
        synchronized (queue) {
            for (DeviceMonitorTask task : queue.values()) {
                task.refreshAlarms();
            }
        }
    }

    /**
     * For test run the tasks
     */
    public void taskTestRun() {
        synchronized (queue) {
            for (DeviceMonitorTask task : queue.values()) {
                task.run();
            }
        }
    }

    /*-------------------------------------------------------------
     * Private functions
     */

    /**
     * createMountpoint registers a new mountpoint monitoring service
     * 
     * @param mountPointNodeName name of mountpoint
     */
    synchronized private DeviceMonitorTask createMonitoringTask(String mountPointNodeName) {

        DeviceMonitorTask task;
        LOG.debug("Register for monitoring {} {}", mountPointNodeName, mountPointNodeName.hashCode());

        if (queue.containsKey(mountPointNodeName)) {
            LOG.debug("Monitoring task exists");
            task = queue.get(mountPointNodeName);
        } else {
            LOG.debug("Do start of DeviceMonitor task");
            //Runnable task = new PerformanceManagerTask(queue, databaseService);
            task = new DeviceMonitorTask(mountPointNodeName, this.odlEventListener);
            queue.put(mountPointNodeName, task);
            task.start(scheduler);
        }
        return task;
    }


    private static DeviceMonitoredNe getDummyNe() {
        return new DeviceMonitoredNe() {

            @Override
            public void prepareCheck() {
                // Do nothing
            }

            @Override
            public boolean checkIfConnectionToMediatorIsOk() {
                return true;
            }

            @Override
            public boolean checkIfConnectionToNeIsOk() {
                return true;
            }

            @Override
            public Optional<NetconfAccessor> getAcessor() {
                return Optional.empty();
            }
        };
    }
}
