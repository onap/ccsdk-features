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
package org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.data;

import java.util.List;

public class ScopeRegistrationResponse {

    private Status status;
    private String message;
    private List<Scope> scopes;

    public ScopeRegistrationResponse() {

    }

    private ScopeRegistrationResponse(Status status, List<Scope> scopes, String message) {
        this.status = status;
        this.scopes = scopes;
        this.message = message;
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Object getScopes() {
        return scopes;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setScopes(List<Scope> scopes) {
        this.scopes = scopes;
    }


    public static ScopeRegistrationResponse error(String message) {
        return new ScopeRegistrationResponse(Status.error, null, message);
    }

    public static ScopeRegistrationResponse success(List<Scope> scopes) {
        return new ScopeRegistrationResponse(Status.success, scopes, null);
    }

    public enum Status {
        success, error
    }

}
