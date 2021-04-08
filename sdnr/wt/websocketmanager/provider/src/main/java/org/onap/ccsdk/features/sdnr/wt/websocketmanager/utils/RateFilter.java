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

import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Problems of to many notifications during mount of thousand of devices:
 * <ul>
 * <li>Overload ODLUX with notification flood -> ODLUX App can not control notifications rate
 * <li>Notification processing blocks user -> App design with notifications popups
 * </ul>
 * Rate filter
 * <ul>
 * <li>Do not use a thread -> Do nothing if there are no notifications
 * <li>Parameter1 integrationTime : Measurement or integration time for period
 * <li>Parameter2 readMaxCount : Specifies event number per interval indicating overload
 * <li>Start measurement on event received that comes later then
 * </ul>
 *
 * <pre>
 *  Example (e: Event received, rateMaxCount=3)
 *         eee                           e  e e e e e  e  e e e e    e         e                e
 *  ---//--|--------------|-----//-------|--------------|--------------|--------------|---//----|--------------|
 *         P1             P2             P1             P2             P3             P7        P1
 *Overload              no             no            yes            yes             no              no
 *
 *
 *Intention to use:
 *   1. Construct with parameters for WS stream to handle
 *   2.
 * </pre>
 */

public class RateFilter {

    private static final Logger LOG = LoggerFactory.getLogger(RateFilter.class.getName());

    private final Duration integrationTime; // Integration time to measure event rate
    private final long rateMaxCount; //Rate for dropping packets
    private Instant timeStampPeriodStart; //Time stamp period beginn
    private Instant timeStampLastEvent; //Measurement interval
    private long rateCount; // >0: integration running 0: no integration running
    private boolean overload; //true means in overload status. Change at end of period only.
    private GetNow get;

    /**
     * Allow testing with own timestamp provider
     */
    public interface GetNow {
        Instant now();
    }

    public RateFilter(Duration integrationTime, long rateMaxCount, GetNow getNowMethod) {
        this.integrationTime = integrationTime;
        this.rateMaxCount = rateMaxCount;
        this.get = getNowMethod;
        this.timeStampLastEvent = Instant.MIN;
    }

    public RateFilter(Duration integrationTime, long rateMaxCount) {
        this(integrationTime, rateMaxCount, () -> Instant.now());
    }

    public synchronized boolean getOverloadStatus() {
        return overload;
    }

    /**
     * Handle filter on event received
     */
    public synchronized void filterEvent() {
        final Instant now = get.now();
        final Duration durationSinceLastEvent = Duration.between(timeStampLastEvent, now);
        this.timeStampLastEvent = now;

        if (durationSinceLastEvent.compareTo(integrationTime) >= 0) {
            //No measurement. Sync and start with period
            LOG.debug("Sync");
            timeStampPeriodStart = now;
            rateCount = 1; //Reset event count .. is part of the
        } else {
            //Within period
            Duration durationPeriod = Duration.between(timeStampPeriodStart, now);
            rateCount++;
            boolean endOfPeriod = durationPeriod.compareTo(integrationTime) >= 0;
            LOG.debug("Period start{}: now:{} end:{} dur:{} int:{}", timeStampPeriodStart, now, endOfPeriod, durationPeriod, integrationTime);
            if (endOfPeriod) {
                //Only if end of Period
                overload = rateCount > rateMaxCount;
                LOG.debug("Reset overload {}", overload);
                timeStampPeriodStart = timeStampPeriodStart.plus(integrationTime);
                rateCount = 0;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RateFilter [integrationTime=");
        builder.append(integrationTime);
        builder.append(", rateMaxCount=");
        builder.append(rateMaxCount);
        builder.append(", timeStampPeriodStart=");
        builder.append(timeStampPeriodStart);
        builder.append(", timeStampLastEvent=");
        builder.append(timeStampLastEvent);
        builder.append(", rateCount=");
        builder.append(rateCount);
        builder.append(", overload=");
        builder.append(overload);
        builder.append("]");
        return builder.toString();
    }
}
