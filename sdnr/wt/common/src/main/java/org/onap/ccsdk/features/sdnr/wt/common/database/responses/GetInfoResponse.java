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

import org.elasticsearch.client.Response;
import org.json.JSONObject;
import org.onap.ccsdk.features.sdnr.wt.common.database.data.DatabaseVersion;

@Deprecated
public class GetInfoResponse extends BaseResponse {

    /**
     * { "name" : "kpOdXt-", "cluster_name" : "docker-cluster", "cluster_uuid" : "qags6CGGTrS75iBhrAdsgg", "version" : {
     * "number" : "6.4.3", "build_flavor" : "default", "build_type" : "tar", "build_hash" : "fe40335", "build_date" :
     * "2018-10-30T23:17:19.084789Z", "build_snapshot" : false, "lucene_version" : "7.4.0",
     * "minimum_wire_compatibility_version" : "5.6.0", "minimum_index_compatibility_version" : "5.0.0" }, "tagline" :
     * "You Know, for Search" }
     */
    private final String clusterName;
    private final String name;

    private final DatabaseVersion version;

    public GetInfoResponse(Response response) throws Exception {
        super(response);
        JSONObject o = this.getJson(response);
        if (o == null) {
            throw new Exception("unable to read response");
        }
        this.name = o.getString("name");
        this.clusterName = o.getString("cluster_name");
        this.version = new DatabaseVersion(o.getJSONObject("version").getString("number"));
    }

    public String getClusterName() {
        return clusterName;
    }

    public String getName() {
        return name;
    }

    public DatabaseVersion getVersion() {
        return version;
    }



}
