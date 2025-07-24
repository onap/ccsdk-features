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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.types;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

@JsonPropertyOrder({"domain", "eventId", "eventName", "eventType", "lastEpochMicrosec", "nfcNamingCode", "nfNamingCode",
        "nfVendorName", "priority", "reportingEntityId", "reportingEntityName", "sequence", "sourceId", "sourceName",
        "startEpochMicrosec", "timeZoneOffset", "version", "vesEventListenerVersion"})

@Getter
@Setter
public class VESCommonEventHeaderPOJO {

    private String domain = "";
    private String eventId = "";
    private String eventName = "";
    private String eventType = "";
    private long sequence = 0L;
    private String priority = "";
    private String reportingEntityId = "";
    private String reportingEntityName = "";
    private String sourceId = "";
    private String sourceName = "";
    private long startEpochMicrosec = 0L;
    private long lastEpochMicrosec = 0L;
    private String nfcNamingCode = "";
    private String nfNamingCode = "";
    private String nfVendorName = "";
    private String timeZoneOffset = "+00:00";
    private String version = "4.1";
    private String vesEventListenerVersion = "7.2.1";
}
