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

import org.onap.ccsdk.features.lib.npm.utils.NpmUtils;

import java.util.UUID;

/**
 * The type Npm Ack.
 *
 * @author Kapil Singal
 */
public class NpmAck {
    private UUID npmTransactionId;
    private String requestId; // multiple transactions can have the same requestId
    private NpmStatusEnum status;
    private String message;

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

    @Override
    public String toString() {
        return NpmUtils.getJson(this);
    }

}
