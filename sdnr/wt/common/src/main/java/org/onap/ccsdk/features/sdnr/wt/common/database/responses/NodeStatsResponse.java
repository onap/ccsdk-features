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
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.client.Response;
import org.json.JSONException;
import org.json.JSONObject;

@Deprecated
public class NodeStatsResponse extends BaseResponse {

    private NodesInfo nodesInfo;
    private Map<String, NodeStats> nodeStats;

    public NodesInfo getNodesInfo() {
        return this.nodesInfo;
    }

    public Map<String, NodeStats> getNodeStatistics() {
        return this.nodeStats;
    }

    public NodeStatsResponse(Response response) throws UnsupportedOperationException, IOException, JSONException {
        super(response);

        JSONObject o = this.getJson(response);
        String k;
        if (o != null) {
            this.nodesInfo = new NodesInfo(o.getJSONObject("_nodes"));
            this.nodeStats = new HashMap<>();
            if (this.nodesInfo.successful > 0) {
                JSONObject stats = o.getJSONObject("nodes");
                for (Object key : stats.keySet()) {
                    k = String.valueOf(key);
                    this.nodeStats.put(k, new NodeStats(k, stats.getJSONObject(k)));
                }
            }
        }
    }



    public static class NodesInfo {
        @Override
        public String toString() {
            return "NodesInfo [total=" + total + ", successful=" + successful + ", failed=" + failed + "]";
        }

        public final int total;
        public final int successful;
        public final int failed;

        public NodesInfo(JSONObject o) {
            this.total = o.getInt("total");
            this.successful = o.getInt("successful");
            this.failed = o.getInt("failed");
        }
    }
    public static class NodeStats {
        public final String name;
        public final NodeTotalDiskStats total;

        @Override
        public String toString() {
            return "NodeStats [name=" + name + ", total=" + total + "]";
        }

        public NodeStats(String name, JSONObject o) {
            this.name = name;
            this.total = new NodeTotalDiskStats(o.getJSONObject("fs").getJSONObject("total"));
        }
    }
    public static class NodeTotalDiskStats {
        public final long total;
        public final long available;
        public final long free;

        @Override
        public String toString() {
            return "NodeTotalDiskStats [total=" + total + ", available=" + available + ", free=" + free
                    + ", getUseDiskPercentage()=" + getUseDiskPercentage() + "]";
        }

        public float getUseDiskPercentage() {
            return (total - available) * 100.0f / (float) total;
        }

        public NodeTotalDiskStats(JSONObject o) {
            this.total = o.getLong("total_in_bytes");
            this.available = o.getLong("available_in_bytes");
            this.free = o.getLong("free_in_bytes");
        }
    }
}
