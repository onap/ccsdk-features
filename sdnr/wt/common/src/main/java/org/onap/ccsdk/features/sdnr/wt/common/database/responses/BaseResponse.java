/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.common.database.responses;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class BaseResponse {

    private static final Logger LOG = LoggerFactory.getLogger(BaseResponse.class);

    private final int responseCode;

    BaseResponse(Response response) {
        this.responseCode = response != null ? response.getStatusLine().getStatusCode() : 0;
    }

    int getResponseCode() {
        return this.responseCode;
    }

    public boolean isResponseSucceeded() {
        return this.responseCode < 300 && this.responseCode >= 200;
    }

    JSONObject getJson(Response response) {
        try {
            String sresponse = EntityUtils.toString(response.getEntity());
            LOG.debug("parsing response={}", sresponse);
            return new JSONObject(sresponse);
        } catch (UnsupportedOperationException | IOException e) {
            LOG.warn("error parsing es response: {}", e.getMessage());
            return null;
        }

    }

    JSONObject getJson(String json) {
        return new JSONObject(json);
    }

    /**
     * @param response
     * @return
     */
    List<String> getLines(Response response) {
        return this.getLines(response, true);
    }

    List<String> getLines(Response response, boolean ignoreEmpty) {
        try {
            String sresponse = EntityUtils.toString(response.getEntity());
            LOG.debug("parsing response={}", sresponse);
            String[] hlp = sresponse.split("\n");
            List<String> lines = new ArrayList<String>();
            for (String h : hlp) {
                if (ignoreEmpty && h.trim().length() == 0) {
                    continue;
                }
                lines.add(h);
            }
            return lines;
        } catch (UnsupportedOperationException | IOException e) {
            LOG.warn("error parsing es response: {}", e.getMessage());
            return null;
        }

    }


}
