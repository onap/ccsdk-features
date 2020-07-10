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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.elalto;

import java.util.Map;

import org.json.JSONObject;
import org.onap.ccsdk.features.sdnr.wt.common.database.SearchHit;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.BaseSearchHitConverter;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.ComponentData;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.ComponentName;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.DataContainer;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.frankfurt.data.ConnectionLogStatus;

/**
 * 
 * @author Michael Dürre
 * 
 *         Convert data from el alto to frankfurt
 * 
 *         src: eventlog dst: connectionlog
 * 
 * 
 *         { "event": { "nodeName": "SDN-Controller-5a150173d678", "counter": "48", "timeStamp":
 *         "2019-10-07T09:57:08.2Z", "objectId": "Sim2230", "attributeName": "ConnectionStatus", "newValue":
 *         "connecting", "type": "AttributeValueChangedNotificationXml" } }
 * 
 *         =>
 * 
 *         { "timestamp": "2020-01-28T12:00:10.2Z", "status": "Connected", "node-id": "sim1" }
 * 
 */
public class FrankfurtConnectionlogConverter extends BaseSearchHitConverter {

    public FrankfurtConnectionlogConverter() {
        super(ComponentName.CONNECTIONLOG);
    }

    /**
     * @source eventlog searchhit converted to connectionlog entry
     */
    @Override
    public SearchHit convert(SearchHit source) {

        JSONObject data = new JSONObject();
        JSONObject inner = source.getSource().getJSONObject("event");
        String eventType = inner.getString("type");
        String eventSource = inner.getString("nodeName");
        if (!eventSource.startsWith("SDN-Controller")) {
            return null;
        }
        data.put("node-id", inner.getString("objectId"));
        data.put("timestamp", inner.getString("timeStamp"));
        if (eventType.equals("AttributeValueChangedNotificationXml")) {
            String event = inner.getString("newValue").toLowerCase();
            if (event.equals("connected")) {
                data.put("status", ConnectionLogStatus.Connected.getName());
            } else if (event.equals("connecting")) {
                data.put("status", ConnectionLogStatus.Connecting.getName());
            } else {
                data.put("status", ConnectionLogStatus.UnableToConnect.getName());
            }

        } else if (eventType.equals("ObjectCreationNotificationXml")) {
            data.put("status", ConnectionLogStatus.Mounted.getName());

        } else if (eventType.equals("ObjectDeletionNotificationXml")) {
            data.put("status", ConnectionLogStatus.Unmounted.getName());
        }

        return this.getSearchHit(source.getIndex(), source.getType(), source.getId(), data);
    }

    @Override
    public ComponentData convert(DataContainer container) {
        Map<ComponentName, ComponentData> src = container.getComponents();
        if (!src.containsKey(ComponentName.EVENTLOG)) {
            return null;
        }
        ComponentData eventData = src.get(ComponentName.EVENTLOG);
        ComponentData dstData = new ComponentData(ComponentName.CONNECTIONLOG);
        for (SearchHit sh : eventData) {
            dstData.add(this.convert(sh));
        }
        return dstData;
    }

}
