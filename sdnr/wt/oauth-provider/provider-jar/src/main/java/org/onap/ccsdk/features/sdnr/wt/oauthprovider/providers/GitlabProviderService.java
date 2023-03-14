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
package org.onap.ccsdk.features.sdnr.wt.oauthprovider.providers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.Config;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.OAuthProviderConfig;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.UnableToConfigureOAuthService;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.UserTokenPayload;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.http.client.MappedBaseHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitlabProviderService extends AuthService {

    private static final Logger LOG = LoggerFactory.getLogger(GitlabProviderService.class);
    private Map<String, String> additionalTokenVerifierParams;
    protected final List<String> randomIds;
    private static final String API_USER_URI = "/api/v4/user";
    private static final String API_GROUP_URI = "/api/v4/groups?min_access_level=10";

    public GitlabProviderService(OAuthProviderConfig config, String redirectUri, TokenCreator tokenCreator) throws UnableToConfigureOAuthService {
        super(config, redirectUri, tokenCreator);
        this.additionalTokenVerifierParams = new HashMap<>();
        this.additionalTokenVerifierParams.put("grant_type", "authorization_code");
        this.randomIds = new ArrayList<>();
    }

    @Override
    protected String getTokenVerifierUri() {
        return "/oauth/token";
    }

    @Override
    protected String getLoginUrl(String callbackUrl) {
        return String.format("%s/oauth/authorize?client_id=%s&response_type=code&state=%s&redirect_uri=%s",
                this.config.getUrl(), urlEncode(this.config.getClientId()), this.createRandomId(), callbackUrl);
    }

    @Override
    protected String getLogoutUrl() {
        return String.format("%s/oauth/logout", this.config.getUrl());
    }

    private String createRandomId() {
        String rnd = null;
        while(true) {
            rnd=Config.generateSecret(20);
            if(!this.randomIds.contains(rnd)) {
                break;
            }
        }
        this.randomIds.add(rnd);
        return rnd;
    }

    @Override
    protected ResponseType getResponseType() {
        return ResponseType.CODE;
    }

    @Override
    protected Map<String, String> getAdditionalTokenVerifierParams() {
        return this.additionalTokenVerifierParams;

    }

    @Override
    protected boolean doSeperateRolesRequest() {
        return true;
    }

    @Override
    protected UserTokenPayload mapAccessToken(String spayload) throws JsonMappingException, JsonProcessingException {
        return null;
    }

    @Override
    protected UserTokenPayload requestUserRoles(String access_token, long issued_at, long expires_at) {
        LOG.debug("reqesting user roles with token={}", access_token);
        Map<String, String> authHeaders = new HashMap<>();
        authHeaders.put("Authorization", String.format("Bearer %s", access_token));
        Optional<MappedBaseHttpResponse<GitlabUserInfo>> userInfo =
                this.getHttpClient().sendMappedRequest(API_USER_URI, "GET", null, authHeaders, GitlabUserInfo.class);
        if (userInfo.isEmpty()) {
            LOG.warn("unable to read user data");
            return null;
        }
        Optional<MappedBaseHttpResponse<GitlabGroupInfo[]>> groupInfos = this.getHttpClient()
                .sendMappedRequest(API_GROUP_URI, "GET", null, authHeaders, GitlabGroupInfo[].class);
        if (groupInfos.isEmpty()) {
            LOG.warn("unable to read group information for user");
            return null;
        }
        UserTokenPayload data = new UserTokenPayload();
        GitlabUserInfo uInfo = userInfo.get().body;
        data.setPreferredUsername(uInfo.getUsername());
        data.setGivenName(uInfo.getName());
        data.setFamilyName(uInfo.getName());
        data.setIat(issued_at);
        data.setExp(expires_at);
        List<String> roles = new ArrayList<>();
        GitlabGroupInfo[] uRoles = groupInfos.get().body;
        for (GitlabGroupInfo uRole : uRoles) {
            roles.add(uRole.getName());
        }
        data.setRoles(this.mapRoles(roles));
        return data;
    }



    @SuppressWarnings("unused")
    private static class GitlabUserInfo {

        private String username;
        private String name;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
    @SuppressWarnings("unused")
    private static class GitlabGroupInfo {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
    @Override
    protected boolean verifyState(String state) {
        if(this.randomIds.contains(state)) {
            this.randomIds.remove(state);
            return true;
        }
        return false;
    }
}
