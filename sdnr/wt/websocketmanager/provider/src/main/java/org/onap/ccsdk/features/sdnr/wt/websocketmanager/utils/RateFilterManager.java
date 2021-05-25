/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.websocketmanager.utils;

import java.io.Closeable;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Problems of to many notifications during mount of thousand of devices:
 * <ul>
 * <li>Overload ODLUX with notification flood -> ODLUX App can not control notifications rate
 * <li>Notification processing blocks user -> App design with notifications popups
 * </ul>
 * Rate filter requirements
 * <ul>
 * <li>Use a single thread
 * <li>Parameter1 integrationTime : Measurement or integration time for period
 * <li>Parameter2 readMaxCount : Specifies event number per interval indicating overload
 * <li>Start measurement on event received that comes later then
 * </ul>
 *
 * <pre>
 *  Example for behavior (e: Event received, rateMaxCount=3)
 *         eee                           e  e e e e e  e  e e e e    e         e                e
 *  ---//--|--------------|-----//-------|--------------|--------------|--------------|---//----|--------------|
 *         P1             P2             P1             P2             P3             P7        P1
 *Overload              no             no            yes            yes             no              no
 * </pre>
 *
 * Interface to use:
 * <ul>
 * <li>construct RateFilterManager. Parameters are integration time and function to get the actual time
 * <li>RateFilterManager.getRateFilter() provides rateFilter object for a stream to count events and provide overload
 * status.
 * <li>rateFilter.event() count the events during measurement period
 * <li>rateFilter.getOverloadStatus() indicates status
 * <li>rateFilter.close() to release this object
 * </ul>
 */

public class RateFilterManager implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(RateFilterManager.class.getName());
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.mm.yy hh:mm:ss_SSS")
            .withLocale(Locale.GERMAN).withZone(ZoneId.systemDefault());
    private static final long CLIENTS_NUMBER_WARNLEVEL = 1000;

    //Configuration
    private final Duration integrationTime; // Integration time to measure event rate
    private GetNow get; //Provides actual system time
    private final Map<Long, RateFilter> rateFilterList;
    @SuppressWarnings("unused")
    private final Timer timerTask;

    /**
     * Allow testing with own timestamp provider Provide actual system time.
     */
    public interface GetNow {
        Instant now();
    }

    /**
     * Constructor with all parameters, intended to be used for unit test
     *
     * @param integrationTime is the interval length for counting events.
     * @param rateMaxCountDefault if event count exceed this limit, status changes to overload.
     * @param startTimer true start time with intervall time
     * @param get function to provide actual system time.
     */
    public RateFilterManager(Duration integrationTime, boolean startTimer, GetNow get) {
        this.integrationTime = integrationTime;
        this.get = get;

        this.rateFilterList = Collections.synchronizedMap(new HashMap<Long, RateFilter>());
        this.timerTask = startTimer ? startTimerTask(integrationTime) : null;
    }

    /**
     * Get RateFilter manager
     *
     * @param integrationTime is the time to measure events
     * @param rateMaxCountDefault if exceeded state overload is true
     */
    public RateFilterManager(Duration integrationTime) {
        this(integrationTime, true, () -> Instant.now());
    }

    /**
     */
    /**
     * Get a specific rate filter for one stream. Use close() to release.
     *
     * @param ratePerMinute Rate per Minute for this filter. If 0 never overloaded.
     * @return RateFilter object for each event stream.
     * @throws IllegalArgumentException on negative rate
     */
    public synchronized RateFilter getRateFilter(long maxRatePerMinute) throws IllegalArgumentException {
        long maxEventsPerIntegration = convertRPMToMaxCount(maxRatePerMinute);
        if (maxEventsPerIntegration < 0)
            throw new IllegalArgumentException(
                    "Resulting in illegal maxEventsPerIntegration=" + maxEventsPerIntegration);
        return getRateFilterInstance(maxEventsPerIntegration);
    }

    @Override
    public void close() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask.purge();
        }
        rateFilterList.clear();
    }

    /**
     * Function to get a new Ratefilter for a connection
     *
     * @param maxEventsPerIntegration
     * @return reference to object with filter status
     */
    private RateFilter getRateFilterInstance(long maxEventsPerIntegration) {
        RateFilter rateFilter;
        synchronized (rateFilterList) {
            rateFilter = rateFilterList.get(maxEventsPerIntegration);
            if (rateFilter == null) {
                rateFilter = new RateFilter(maxEventsPerIntegration);
                synchronized (rateFilterList) {
                    rateFilterList.put(maxEventsPerIntegration, rateFilter);
                }
            } else {
                if (rateFilter.addClient() > CLIENTS_NUMBER_WARNLEVEL)
                    LOG.warn("Warnlevel {} exceeded for client connections", CLIENTS_NUMBER_WARNLEVEL);
            }
        }
        return rateFilter;
    }

    private Timer startTimerTask(Duration integrationTime) {
        long milliseconds = integrationTime.toMillis();
        LOG.debug("Start startTimerTask with {} ms", milliseconds);
        Timer time = new Timer();
        time.scheduleAtFixedRate(new TimeoutHandler(), 0L, milliseconds);
        return time;
    }

    private class TimeoutHandler extends TimerTask {
        @Override
        public void run() {
            LOG.debug("Run timeout task at {}", f(get.now()));
            synchronized (rateFilterList) {
                rateFilterList.forEach((k, f) -> f.timer());
            }
        }
    }

    /**
     * Provide nice debug output for Instant and Duration
     *
     * @param i with instant
     * @return output string
     */
    private static String f(Instant i) {
        return i != null ? FORMATTER.format(i) : "null";
    }

    /**
     * Convert a rate per minute into events per integration time.
     *
     * @param ratePerMinute
     * @return events per integration time.
     */
    private long convertRPMToMaxCount(long ratePerMinute) {
        return ratePerMinute * integrationTime.toSeconds() / TimeUnit.MINUTES.toSeconds(1);
    }

    /**
     * Ratefilter class contains status informaton for one event stream.
     */
    public class RateFilter implements Closeable {
        private final long maxEventsPerIntegration; //uuid and maximum of events without overload
        private Long clients; // Number of clients for this filter.
        private long rateCount; // number of events during integration period
        private boolean overload; //true means in overload status. Change at end of period only.

        /**
         * Create a new Filter
         *
         * @param maxEventsPerIntegration >= 1 characteristics and uuid of this filter. < 1 switched off
         * @see {@link #close}
         */
        private RateFilter(long maxEventsPerIntegration) {
            synchronized (this) {
                this.clients = 1L;
                this.maxEventsPerIntegration = maxEventsPerIntegration;
                this.rateCount = 0;
            }
        }

        /**
         * Add a client to this filter
         *
         * @return number of clients, handled by this filter
         * @see {@link #close}
         */
        private synchronized long addClient() {
            if (clients >= 1) {
                ++clients;
            } else {
                LOG.warn("Misalligned open/close for {} with number {}", maxEventsPerIntegration, clients);
            }
            return clients;
        }

        /**
         * Provide actual overload status
         *
         * @return status true means overloaded false not overloaded
         */
        public synchronized boolean getOverloadStatus() {
            return overload;
        }

        /**
         * Handle filter on event received
         */
        public synchronized void event() {
            rateCount++;
            LOG.debug("event rc:{}", rateCount);
        }

        /**
         * Called if measurement period ends. Device if overload and reset counter.
         */
        public synchronized void timer() {
            //Change overload only at end of period
            //Always inactive if maxEventsPerIntegration== 0
            if (maxEventsPerIntegration > 0) {
                overload = rateCount > maxEventsPerIntegration;
            }
            rateCount = 0;
            LOG.debug("Timer ol:{} rc:{}", overload, rateCount);
        }

        /**
         * Get maximum events allowed per integration period
         *
         * @return 1 ...
         */
        public synchronized long getMaxEventsPerIntegration() {
            return maxEventsPerIntegration;
        }

        /**
         * Get number of client streams.
         *
         * @return 1 ...
         */
        public synchronized long getClients() {
            return clients;
        }

        @Override
        public void close() {
            synchronized (rateFilterList) {
                if (clients == 1) {
                    LOG.debug("Close and remove last client {}", maxEventsPerIntegration);
                    rateFilterList.remove(this.maxEventsPerIntegration);
                    clients--;
                } else if (clients > 1) {
                    LOG.debug("Close one client of {} for events {}", clients, maxEventsPerIntegration);
                    clients--;
                } else {
                    LOG.warn("Misaligned new/close for events {}", maxEventsPerIntegration);
                }
            }
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("RateFilter [maxEventsPerIntegration=");
            builder.append(maxEventsPerIntegration);
            builder.append(", clients=");
            builder.append(clients);
            builder.append(", rateCount=");
            builder.append(rateCount);
            builder.append(", overload=");
            builder.append(overload);
            builder.append("]");
            return builder.toString();
        }
    }
}
