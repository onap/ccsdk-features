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
package org.onap.ccsdk.features.sdnr.wt.oauthprovider.data;

public class OAuthResponseData {

    private String access_token;
    private double expires_in;
    private double refresh_expires_in;
    private String refresh_token;
    private String token_type;
    private String id_token;

    public OAuthResponseData() {
    }

    public OAuthResponseData(String token) {
        this.access_token = token;
    }

    public String getAccess_token() {
        return access_token;
    }

    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public double getRefresh_expires_in() {
        return refresh_expires_in;
    }

    public void setRefresh_expires_in(double refresh_expires_in) {
        this.refresh_expires_in = refresh_expires_in;
    }

    public double getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(double expires_in) {
        this.expires_in = expires_in;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public void setId_token(String id_token){ this.id_token = id_token;}
    public String getId_token(){ return this.id_token;}
    @Override
    public String toString() {
        return "OAuthResponseData [access_token=" + access_token + ", expires_in=" + expires_in
                + ", refresh_expires_in=" + refresh_expires_in + ", refresh_token=" + refresh_token + ", token_type="
                + token_type + "]";
    }
}
