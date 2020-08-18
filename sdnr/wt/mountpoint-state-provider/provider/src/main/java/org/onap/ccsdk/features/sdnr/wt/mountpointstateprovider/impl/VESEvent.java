/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property. All rights reserved.
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
package org.onap.ccsdk.features.sdnr.wt.mountpointstateprovider.impl;

import java.util.HashMap;
import java.util.Map;

public class VESEvent {
    public Map<String, Object> event = new HashMap<String, Object>();

    public void addEventObjects(Object eventObject) {
        if (eventObject instanceof VESCommonEventHeaderPOJO)
            event.put("commonEventHeader", eventObject);
        else if (eventObject instanceof VESNotificationFieldsPOJO)
            event.put("notificationFields", eventObject);

    }

    public Map<String, Object> getEvent() {
        return event;
    }
}
