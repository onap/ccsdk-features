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

import java.util.List;

public class UserTokenPayload {

    public static final String PROVIDERID_INTERNAL="Internal";

    private List<String> roles;
    private String preferredUsername;
    private String givenName;
    private String familyName;
    private long exp;
    private long iat;

    private String providerId;

    public long getExp() {
        return exp;
    }

    public long getIat() {
        return this.iat;
    }

    public void setPreferredUsername(String preferredUsername) {
        this.preferredUsername = preferredUsername;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public void setExp(long exp) {
        this.exp = exp;
    }

    public void setIat(long iat) {
        this.iat = iat;
    }

    public String getPreferredUsername() {
        return preferredUsername;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public List<String> getRoles() {
        return this.roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public void setProviderId(String providerId){ this.providerId = providerId;}

    public  String getProviderId(){ return this.providerId;}

    public static UserTokenPayload createInternal(String username, List<String> roles) {
        UserTokenPayload data = new UserTokenPayload();
        data.setPreferredUsername(username);
        data.setRoles(roles);
        data.setProviderId(PROVIDERID_INTERNAL);
        return data;
    }


    public boolean isInternal() {
        return PROVIDERID_INTERNAL.equals(this.providerId);
    }
}
