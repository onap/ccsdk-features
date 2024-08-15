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
import org.elasticsearch.client.Response;
import org.json.JSONException;
import org.json.JSONObject;

@Deprecated
public class ClusterHealthResponse extends BaseResponse {

    public static final String HEALTHSTATUS_GREEN = "green";
    public static final String HEALTHSTATUS_YELLOW = "yellow";
    public static final String HEALTSTATUS_RED = "red";

    private String status;
    private boolean timedOut;

    /*
     * "cluster_name": "docker-cluster", "status": "yellow", "timed_out": false,
     * "number_of_nodes": 1, "number_of_data_nodes": 1, "active_primary_shards": 5,
     * "active_shards": 5, "relocating_shards": 0, "initializing_shards": 0,
     * "unassigned_shards": 5, "delayed_unassigned_shards": 0,
     * "number_of_pending_tasks": 0, "number_of_in_flight_fetch": 0,
     * "task_max_waiting_in_queue_millis": 0, "active_shards_percent_as_number": 50
     */
    public ClusterHealthResponse(Response response) throws UnsupportedOperationException, IOException, JSONException {
        super(response);

        JSONObject o = this.getJson(response);
        if (o != null) {
            this.status = o.getString("status");
            this.timedOut = o.getBoolean("timed_out");
        }
    }

    public boolean isTimedOut() {
        return this.timedOut;
    }

    @SuppressWarnings("hiding")
    public boolean isStatusMinimal(String status) {
        if (status == null) {
            return true;
        }
        if (this.status.equals(HEALTHSTATUS_GREEN)) {
            return true;
        }
        if (this.status.equals(HEALTHSTATUS_YELLOW) && !status.equals(HEALTHSTATUS_GREEN)) {
            return true;
        }
        if (this.status.equals(status)) {
            return true;
        }
        return false;

    }

    public String getStatus() {
        return this.status;
    }

}
