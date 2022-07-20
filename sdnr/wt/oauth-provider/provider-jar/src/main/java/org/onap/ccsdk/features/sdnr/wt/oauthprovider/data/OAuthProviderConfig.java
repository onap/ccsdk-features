/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2021 highstreet technologies GmbH Intellectual Property.
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.Map;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.providers.OAuthProviderFactory.OAuthProvider;

public class OAuthProviderConfig {

    private String url;
    private String internalUrl;
    private String clientId;
    private String secret;
    private String id;
    private String title;
    private String scope;
    private String realmName;
    private String openIdConfigUrl;

    private boolean trustAll;
    private OAuthProvider type;
    private Map<String, String> roleMapping;

    public OAuthProvider getType() {
        return type;
    }

    public OAuthProviderConfig(String id, String url, String internalUrl, String clientId, String secret, String scope,
            String title, String realmName, String openIdConfigUrl, boolean trustAll) {
        this.id = id;
        this.url = url;
        this.internalUrl = internalUrl;
        this.clientId = clientId;
        this.secret = secret;
        this.scope = scope;
        this.title = title;
        this.realmName = realmName;
        this.trustAll = trustAll;
        this.openIdConfigUrl = openIdConfigUrl;
        this.roleMapping = new HashMap<>();
    }

    @Override
    public String toString() {
        return "OAuthProviderConfig [url=" + url + ", clientId=" + clientId + ", secret=" + secret + ", id=" + id
                + ", title=" + title + ", scope=" + scope + ", realmName=" + realmName + ", trustAll=" + trustAll
                + ", type=" + type + ", roleMapping=" + roleMapping + "]";
    }

    public void setType(OAuthProvider type) {
        this.type = type;
    }

    public OAuthProviderConfig() {
        this(null, null, null, null, null, null, null, null, null, false);
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getId() {
        return this.id;
    }

    public String getUrl() {
        return this.url;
    }

    public String getClientId() {
        return this.clientId;
    }

    public String getSecret() {
        return this.secret;
    }

    public String getTitle() {
        return this.title;
    }

    public String getScope() {
        return this.scope;
    }

    public String getRealmName() {
        return realmName;
    }

    public void setRealmName(String realmName) {
        this.realmName = realmName;
    }

    public boolean trustAll() {
        return trustAll;
    }

    public void setTrustAll(boolean trustAll) {
        this.trustAll = trustAll;
    }

    public Map<String, String> getRoleMapping() {
        return roleMapping;
    }

    public void setRoleMapping(Map<String, String> roleMapping) {
        this.roleMapping = roleMapping;
    }

    public String getInternalUrl() {
        return internalUrl;
    }

    public void setInternalUrl(String internalUrl) {
        this.internalUrl = internalUrl;
    }

    public void setOpenIdConfigUrl(String openIdConfigUrl){ this.openIdConfigUrl = openIdConfigUrl;}

    public String getOpenIdConfigUrl() { return this.openIdConfigUrl;}
    @JsonIgnore
    public void handleEnvironmentVars() {
        if (Config.isEnvExpression(this.id)) {
            this.id = Config.getProperty(this.id, null);
        }
        if (Config.isEnvExpression(this.url)) {
            this.url = Config.getProperty(this.url, null);
        }
        if (Config.isEnvExpression(this.internalUrl)) {
            this.internalUrl = Config.getProperty(this.internalUrl, null);
        }
        if (Config.isEnvExpression(this.clientId)) {
            this.clientId = Config.getProperty(this.clientId, null);
        }
        if (Config.isEnvExpression(this.secret)) {
            this.secret = Config.getProperty(this.secret, null);
        }
        if (Config.isEnvExpression(this.scope)) {
            this.scope = Config.getProperty(this.scope, null);
        }
        if (Config.isEnvExpression(this.title)) {
            this.title = Config.getProperty(this.title, null);
        }
        if (Config.isEnvExpression(this.realmName)) {
            this.realmName = Config.getProperty(this.realmName, null);
        }
        if (Config.isEnvExpression(this.openIdConfigUrl)) {
            this.openIdConfigUrl = Config.getProperty(this.openIdConfigUrl, null);
        }
    }

    @JsonIgnore
    public String getUrlOrInternal() {
        return this.internalUrl != null && this.internalUrl.length() > 0 ? this.internalUrl : this.url;
    }

    @JsonIgnore
    public boolean hasToBeConfigured(){
        return this.openIdConfigUrl!=null && this.openIdConfigUrl.length()>0;
    }
}
