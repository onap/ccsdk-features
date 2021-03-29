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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"domain", "eventId", "eventName", "eventType", "lastEpochMicrosec", "nfcNamingCode", "nfNamingCode",
        "nfVendorName", "priority", "reportingEntityId", "reportingEntityName", "sequence", "sourceId", "sourceName",
        "startEpochMicrosec", "timeZoneOffset", "version", "vesEventListenerVersion"})

public class VESCommonEventHeaderPOJO {

    private String domain = "";
    private String eventId = "";
    private String eventName = "";
    private String eventType = "";
    private long sequence = 0L;
    private String priority = "";
    @JsonIgnore
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
    private String vesEventListenerVersion = "7.1.1";

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getSequence() {
        return sequence;
    }

    public void setSequence(long sequenceNo) {
        this.sequence = sequenceNo;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getReportingEntityId() {
        return reportingEntityId;
    }

    public void setReportingEntityId(String reportingEntityId) {
        this.reportingEntityId = reportingEntityId;
    }

    public String getReportingEntityName() {
        return reportingEntityName;
    }

    public void setReportingEntityName(String reportingEntityName) {
        this.reportingEntityName = reportingEntityName;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public long getStartEpochMicrosec() {
        return startEpochMicrosec;
    }

    public void setStartEpochMicrosec(long startEpochMicrosec) {
        this.startEpochMicrosec = startEpochMicrosec;
    }

    public long getLastEpochMicrosec() {
        return lastEpochMicrosec;
    }

    public void setLastEpochMicrosec(long lastEpochMicrosec) {
        this.lastEpochMicrosec = lastEpochMicrosec;
    }

    public String getNfcNamingCode() {
        return nfcNamingCode;
    }

    public void setNfcNamingCode(String nfcNamingCode) {
        this.nfcNamingCode = nfcNamingCode;
    }

    public String getNfNamingCode() {
        return nfNamingCode;
    }

    public void setNfNamingCode(String nfNamingCode) {
        this.nfNamingCode = nfNamingCode;
    }

    public String getNfVendorName() {
        return nfVendorName;
    }

    public void setNfVendorName(String nfVendorName) {
        this.nfVendorName = nfVendorName;
    }

    public String getTimeZoneOffset() {
        return timeZoneOffset;
    }

    public void setTimeZoneOffset(String timeZoneOffset) {
        this.timeZoneOffset = timeZoneOffset;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVesEventListenerVersion() {
        return vesEventListenerVersion;
    }

    public void setVesEventListenerVersion(String vesEventListenerVersion) {
        this.vesEventListenerVersion = vesEventListenerVersion;
    }
}
