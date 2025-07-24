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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.service;

import java.time.Instant;
import java.util.HashMap;
import org.opendaylight.yangtools.binding.Notification;

public interface NotificationProxyParser {

    /**
     * parses the Notification proxy object created by ODL
     * Returns a Map with class members as keys and the member values as values.
     * The keys are in xpath notation.
     * Ex: key = /notification/VALUECHANGE[@xmlns=\"urn:org:onap:ccsdk:features:sdnr:northbound:onecell-notification\"]/device/device-info/serial-number"
     *     value = "0005B94238A0"
     * References: https://stackoverflow.com/questions/19633534/what-is-com-sun-proxy-proxy
     */
    public HashMap<String, String> parseNotificationProxy(Notification notification);

    /**
     * Gets the time at which the Event occurred if the notification is an instance of EventInstantAware. If not, then returns the current time
     * Read notification time via {@link #EventInstantAware } interface.
     *
     * @param notification
     * @return
     */
    public Instant getTime(Notification notification);

}
