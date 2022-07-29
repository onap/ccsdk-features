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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.KeycloakUserTokenPayload;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.OAuthProviderConfig;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.UnableToConfigureOAuthService;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.UserTokenPayload;

public class KeycloakProviderService extends AuthService {

    public static final String ID = "keycloak";
    private Map<String, String> additionalTokenVerifierParams;

    public KeycloakProviderService(OAuthProviderConfig config, String redirectUri, TokenCreator tokenCreator) throws UnableToConfigureOAuthService {
        super(config, redirectUri, tokenCreator);
        this.additionalTokenVerifierParams = new HashMap<>();
        this.additionalTokenVerifierParams.put("grant_type", "authorization_code");
    }

    @Override
    protected String getTokenVerifierUri() {
        return String.format("/auth/realms/%s/protocol/openid-connect/token", urlEncode(this.config.getRealmName()));
    }

    @Override
    protected String getLoginUrl(String callbackUrl) {
        return String.format(
                "%s/auth/realms/%s/protocol/openid-connect/auth?client_id=%s&response_type=code&scope=%s&redirect_uri=%s",
                this.config.getUrl(), urlEncode(this.config.getRealmName()), urlEncode(this.config.getClientId()),
                this.config.getScope(), urlEncode(callbackUrl));
    }

    @Override
    protected String getLogoutUrl() {
        return String.format("%s/auth/realms/%s/protocol/openid-connect/logout", this.config.getUrl(),
                urlEncode(this.config.getRealmName()));
    }

    @Override
    protected List<String> mapRoles(List<String> data) {
        final Map<String, String> map = this.config.getRoleMapping();
        List<String> filteredRoles =
                data.stream().filter(role -> !role.equals("uma_authorization") && !role.equals("offline_access"))
                        .map(r -> map.getOrDefault(r, r)).collect(Collectors.toList());
        return filteredRoles;
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
        return false;
    }

    @Override
    protected UserTokenPayload mapAccessToken(String spayload) throws JsonMappingException, JsonProcessingException {
        KeycloakUserTokenPayload payload = mapper.readValue(spayload, KeycloakUserTokenPayload.class);
        UserTokenPayload data = new UserTokenPayload();
        data.setIat(payload.getIat() * 1000L);
        data.setExp(payload.getExp() * 1000L);
        data.setFamilyName(payload.getFamilyName());
        data.setGivenName(payload.getGivenName());
        data.setProviderId(this.config.getId());
        data.setPreferredUsername(payload.getPreferredUsername());
        data.setRoles(this.mapRoles(payload.getRealmAccess().getRoles()));
        return data;
    }

    @Override
    protected UserTokenPayload requestUserRoles(String access_token, long issued_at, long expires_at) {
        return null;
    }

    @Override
    protected boolean verifyState(String state) {
        return true;
    }


}
