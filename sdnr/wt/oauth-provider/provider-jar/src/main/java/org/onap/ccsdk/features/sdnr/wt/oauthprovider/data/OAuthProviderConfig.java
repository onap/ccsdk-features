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
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.providers.OAuthProviderFactory.OAuthProvider;

public class OAuthProviderConfig {

    private String host;
    private String clientId;
    private String secret;
    private String id;
    private String title;
    private String scope;
    private OAuthProvider type;

    public OAuthProvider getType() {
        return type;
    }

    public OAuthProviderConfig(String id, String host, String clientId, String secret, String scope,
            String title) {
        this.id = id;
        this.host = host;
        this.clientId = clientId;
        this.secret = secret;
        this.scope = scope;
        this.title = title;
    }

    @Override
    public String toString() {
        return "OAuthProviderConfig [host=" + host + ", clientId=" + clientId + ", secret=" + secret + ", id=" + id
                + ", title=" + title + ", scope=" + scope + ", type=" + type + "]";
    }

    public void setType(OAuthProvider type) {
        this.type = type;
    }

    public OAuthProviderConfig() {
        this(null, null, null, null, null, null);
    }

    public void setHost(String host) {
        this.host = host;
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

    public String getHost() {
        return this.host;
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

    @JsonIgnore
    public void handleEnvironmentVars() {
        if (Config.isEnvExpression(id)) {
            this.id = Config.getProperty(id, null);
        }
        if (Config.isEnvExpression(host)) {
            this.host = Config.getProperty(host, null);
        }
        if (Config.isEnvExpression(clientId)) {
            this.clientId = Config.getProperty(clientId, null);
        }
        if (Config.isEnvExpression(secret)) {
            this.secret = Config.getProperty(secret, null);
        }
        if (Config.isEnvExpression(scope)) {
            this.scope = Config.getProperty(scope, null);
        }
        if (Config.isEnvExpression(title)) {
            this.title = Config.getProperty(title, null);
        }
    }

}
