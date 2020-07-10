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

import org.json.JSONObject;
import org.onap.ccsdk.features.sdnr.wt.common.database.SearchHit;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.BaseSearchHitConverter;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.ComponentName;

/**
 * 
 * @author Michael Dürre { "fault": { "nodeName": "SDN-Controller-5a150173d678", "counter": "380", "timeStamp":
 *         "2019-10-18T18:40:25.0Z", "objectId": "ADVA-DTAG-Nuernberg-3", "problem": "connectionLossOAM", "severity":
 *         "Major", "type": "ProblemNotificationXml" } }
 *
 *         => { "timestamp": "2017-01-01T00:00:00.0Z", "object-id": "LP-MWPS-RADIO", "severity": "Critical", "counter":
 *         -1, "node-id": "sim1", "source-type": "Netconf", "problem": "signalIsLost" }
 *
 */
public class FrankfurtFaultlogConverter extends BaseSearchHitConverter {

    public FrankfurtFaultlogConverter() {
        super(ComponentName.FAULTLOG);
    }

    @Override
    public SearchHit convert(SearchHit source) {

        JSONObject data = new JSONObject();
        JSONObject src = source.getSource();
        JSONObject srcInner = src.getJSONObject("fault");
        data.put("node-id", srcInner.getString("nodeName"));
        data.put("severity", srcInner.getString("severity"));
        data.put("counter", Long.parseLong(srcInner.getString("counter")));
        data.put("timestamp", srcInner.getString("timeStamp"));
        data.put("object-id", srcInner.getString("objectId"));
        data.put("problem", srcInner.getString("problem"));
        data.put("type", "Netconf");
        return this.getSearchHit(source.getIndex(), source.getType(), source.getId(), data);
    }

}
