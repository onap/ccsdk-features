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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.base.toggleAlarmFilter;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationDelayFilter<T> implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationDelayFilter.class);

    private final ConcurrentHashMap <String, NotificationWithServerTimeStamp<T>> problemItems;
//    private final HashMap<String, NotificationWithServerTimeStamp<T>> nonProblemItems;
    private final NotificationDelayedListener<T> timeoutListener;

    private static long delay;
    private static boolean enabled;

    public static void setDelay(long l) {
        NotificationDelayFilter.delay = l;
    }

    public static long getDelay() {
        return NotificationDelayFilter.delay;
    }

    public static boolean isEnabled() {
        return NotificationDelayFilter.enabled;
    }

    public static void setEnabled(boolean enabled) {
        NotificationDelayFilter.enabled = enabled;
    }

    private final ScheduledExecutorService scheduler;
    private final Runnable timerRunner = () -> onTick();

    private final String nodeName;

    public NotificationDelayFilter(String nodeName, NotificationDelayedListener<T> timeoutListener) {
        this.nodeName = nodeName;
        this.timeoutListener = timeoutListener;
        this.problemItems = new ConcurrentHashMap <>();
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.startTimer();
    }

    /**
     * Push notification with a specific severity (everything except non-alarmed)
     * @param problemName key
     * @param notification related notification
     */
    public void pushAlarmNotification(String problemName, T notification) {
        synchronized (problemItems) {

            boolean cp = this.problemItems.containsKey(problemName);
            if (!cp) {
                // no alarm in entries => create entry and push the alarm currently
                NotificationWithServerTimeStamp<T> item = new NotificationWithServerTimeStamp<>(
                        notification);
                LOG.debug("add event into list for node " + this.nodeName + " for alarm " + problemName + ": "
                        + item.toString());
                this.problemItems.put(problemName, item);
                if (this.timeoutListener != null) {
                    this.timeoutListener.onNotificationDelay(notification);
                }
            } else {
                LOG.debug("clear contra event for node " + this.nodeName + " for alarm " + problemName);
                this.problemItems.get(problemName).clrContraEvent();
            }

        }
    }

    /**
     * Push notification with severity non-alarmed
     * @param problemName key
     * @param notification related notification
     */
    public void clearAlarmNotification(String problemName, T notification) {
        synchronized (problemItems) {

            boolean cp = this.problemItems.containsKey(problemName);
            if (cp) {
                LOG.debug("set contra event for alarm " + problemName);
                this.problemItems.get(problemName).setContraEvent(notification);
            } else {
                // not in list => push directly through
                if (this.timeoutListener != null) {
                    this.timeoutListener.onNotificationDelay(notification);
                }
            }
        }
    }

    private void startTimer() {
        scheduler.scheduleAtFixedRate(timerRunner, 0, 1, TimeUnit.SECONDS);
    }

    private void stopTimer() {
        scheduler.shutdown();
    }

    /**
     * check for clearing item out of the list
     */
    private void onTick() {
        long now = System.currentTimeMillis();
        try {

            synchronized (problemItems) {

                for (Entry<String, NotificationWithServerTimeStamp<T>> entry : problemItems
                        .entrySet()) {
                    NotificationWithServerTimeStamp<T> value = entry.getValue();
                    if (value.isStable(now)) {
                        // send contra Alarm if exists
                        if (value.getContraAlarmNotification() != null) {
                            if (this.timeoutListener != null) {
                                this.timeoutListener.onNotificationDelay(value.getContraAlarmNotification());
                            }
                        }
                        problemItems.remove(entry.getKey());
                        LOG.debug("removing entry for "+this.nodeName+" for alarm " + entry.getKey());
                    } else {
                        LOG.trace("currently state is still unstable for alarm " + entry.getKey());
                    }
                }

            }
        } catch (Exception e) {
            //Prevent stopping the task
            LOG.warn("Exception during NotificationDelayFilter Task", e);
        }
    }

    @Override
    public void close() throws Exception {
        this.stopTimer();
    }

}
