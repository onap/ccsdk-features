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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * {
  "exp": 1610362593,
  "iat": 1610361393,
  "jti": "09bd6f2c-5dba-44a0-bd76-cd0d440137d0",
  "iss": "http://10.20.11.160:8080/auth/realms/onap",
  "aud": "account",
  "sub": "446a24bc-d8a0-43dd-afa5-e56eed75deb8",
  "typ": "Bearer",
  "azp": "admin-cli",
  "session_state": "db2c96f4-cc9b-47e8-a83f-a01c50d656f2",
  "acr": "1",
  "realm_access": {
    "roles": [
      "provision",
      "offline_access",
      "uma_authorization"
    ]
  },
  "resource_access": {
    "account": {
      "roles": [
        "manage-account",
        "manage-account-links",
        "view-profile"
      ]
    }
  },
  "scope": "profile email",
  "email_verified": false,
  "name": "Luke Skywalker",
  "preferred_username": "luke.skywalker",
  "given_name": "Luke",
  "family_name": "Skywalker",
  "email": "luke.skywalker@sdnr.onap.org"
}
 * @author jack
 *
 */
public class KeycloakUserTokenPayload {

    private long exp;
    private long iat;
    private String jti;
    private String iss;
    private String aud;
    private String sub;
    private String typ;
    private String azp;
    @JsonProperty("session_state")
    private String sessionState;
    private String acr;
    @JsonProperty("realm_access")
    private RealmAccessData realmAccess;
    @JsonProperty("resource_access")
    private ResourceAccessData resourceAccess;
    private String scope;
    @JsonProperty("email_verified")
    private String emailVerified;
    private String name;
    @JsonProperty("preferred_username")
    private String preferredUsername;
    @JsonProperty("given_name")
    private String givenName;
    @JsonProperty("family_name")
    private String familyName;
    private String email;

    public long getExp() {
        return exp;
    }
    public void setExp(long exp) {
        this.exp = exp;
    }
    public long getIat() {
        return iat;
    }
    public void setIat(long iat) {
        this.iat = iat;
    }
    public String getJti() {
        return jti;
    }
    public void setJti(String jti) {
        this.jti = jti;
    }
    public String getIss() {
        return iss;
    }
    public void setIss(String iss) {
        this.iss = iss;
    }
    public String getAud() {
        return aud;
    }
    public void setAud(String aud) {
        this.aud = aud;
    }
    public String getSub() {
        return sub;
    }
    public void setSub(String sub) {
        this.sub = sub;
    }
    public String getTyp() {
        return typ;
    }
    public void setTyp(String typ) {
        this.typ = typ;
    }
    public String getAzp() {
        return azp;
    }
    public void setAzp(String azp) {
        this.azp = azp;
    }
    public String getSessionState() {
        return sessionState;
    }
    public void setSessionState(String sessionState) {
        this.sessionState = sessionState;
    }
    public String getAcr() {
        return acr;
    }
    public void setAcr(String acr) {
        this.acr = acr;
    }
    public RealmAccessData getRealmAccess() {
        return realmAccess;
    }
    public void setRealmAccess(RealmAccessData realmAccess) {
        this.realmAccess = realmAccess;
    }
    public ResourceAccessData getResourceAccess() {
        return resourceAccess;
    }
    public void setResourceAccess(ResourceAccessData resourceAccess) {
        this.resourceAccess = resourceAccess;
    }
    public String getScope() {
        return scope;
    }
    public void setScope(String scope) {
        this.scope = scope;
    }
    public String getEmailVerified() {
        return emailVerified;
    }
    public void setEmailVerified(String emailVerified) {
        this.emailVerified = emailVerified;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPreferredUsername() {
        return preferredUsername;
    }
    public void setPreferredUsername(String preferredUsername) {
        this.preferredUsername = preferredUsername;
    }
    public String getGivenName() {
        return givenName;
    }
    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }
    public String getFamilyName() {
        return familyName;
    }
    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }


    public static class RealmAccessData {
        private List<String> roles;

        public List<String> getRoles(){
            return this.roles;
        }
        public void setRoles(List<String> roles) {
            this.roles = roles;
        }
    }
    public static class ResourceAccessData {
        private RealmAccessData account;

        public RealmAccessData getAccount() {
            return this.account;
        }
        public void setAccount(RealmAccessData account) {
            this.account = account;
        }
    }
}
