/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=======================================================
 *
 */

package org.onap.ccsdk.features.lib.npm.models;

import org.onap.ccsdk.features.lib.npm.NpmException;
import org.onap.ccsdk.features.lib.npm.utils.NpmUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * The type Npm Transaction.
 *
 * @author Kapil Singal
 */
public class NpmTransaction {

    private UUID npmTransactionId;
    private String requestId; // multiple transactions can have the same requestId

    private String sbEndpoint;
    private String sbType;

    private int priority = -1;
    private int connectionCount = 1;

    private Instant timestamp = Instant.now();
    private Instant timeToLive = Instant.MAX;

    private NpmStatusEnum status = NpmStatusEnum.RECEIVED;
    private String message;

    private String serviceKey;
    private Object serviceRequest;

    public UUID getNpmTransactionId() {
        return npmTransactionId;
    }

    public void setNpmTransactionId(UUID npmTransactionId) {
        this.npmTransactionId = npmTransactionId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getSbEndpoint() {
        return sbEndpoint;
    }

    public void setSbEndpoint(String sbEndpoint) {
        this.sbEndpoint = sbEndpoint;
    }

    public String getSbType() {
        return sbType;
    }

    public void setSbType(String sbType) {
        this.sbType = sbType;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getConnectionCount() {
        return connectionCount;
    }

    public void setConnectionCount(int connectionCount) {
        this.connectionCount = connectionCount;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Instant getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToLive(Instant timeToLive) {
        this.timeToLive = timeToLive;
    }

    public NpmStatusEnum getStatus() {
        return status;
    }

    public void setStatus(NpmStatusEnum status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getServiceKey() {
        return serviceKey;
    }

    public void setServiceKey(String serviceKey) {
        this.serviceKey = serviceKey;
    }

    public Object getServiceRequest() {
        return serviceRequest;
    }

    public void setServiceRequest(Object serviceRequest) {
        this.serviceRequest = serviceRequest;
    }

    /**
     * Validate boolean.
     *
     * @throws NpmException the validator exception
     */
    public void validate() throws NpmException {
        if (npmTransactionId == null) {
            throw new NpmException("Transaction is not valid: npmTransactionId is required.");
        }
        if (StringUtils.isBlank(sbEndpoint)) {
            throw new NpmException("Transaction is not valid: sbEndpoint is required.");
        }
        if (StringUtils.isBlank(sbType)) {
            throw new NpmException("Transaction is not valid: sbType is required.");
        }
        if (timestamp == null) {
            throw new NpmException("Transaction is not valid: txTimestamp is required.");
        }
        if (timeToLive == null) {
            throw new NpmException("Transaction is not valid: timeToLive is required.");
        }
        if (StringUtils.isBlank(serviceKey)) {
            throw new NpmException("Transaction is not valid: serviceKey is required.");
        }
        if (serviceRequest == null) {
            throw new NpmException("Transaction is not valid: serviceRequest is required.");
        }
    }

    @Override
    public String toString() {
        return NpmUtils.getJson(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NpmTransaction)) {
            return false;
        }
        NpmTransaction that = (NpmTransaction) o;
        return npmTransactionId.equals(that.npmTransactionId);
    }

    @Override
    public int hashCode() {
        return Math.abs(Objects.hash(npmTransactionId));
    }

}
