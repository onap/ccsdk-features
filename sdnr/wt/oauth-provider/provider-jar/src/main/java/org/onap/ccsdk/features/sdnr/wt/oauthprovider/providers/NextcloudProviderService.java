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
import java.util.Map;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.OAuthProviderConfig;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.UnableToConfigureOAuthService;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.UserTokenPayload;

public class NextcloudProviderService extends AuthService {

    public NextcloudProviderService(OAuthProviderConfig config, String redirectUri, TokenCreator tokenCreator) throws UnableToConfigureOAuthService {
        super(config, redirectUri, tokenCreator);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected String getTokenVerifierUri() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Map<String, String> getAdditionalTokenVerifierParams() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected ResponseType getResponseType() {
        // TODO Auto-generated method stub
        return ResponseType.TOKEN;
    }

    @Override
    protected boolean doSeperateRolesRequest() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected UserTokenPayload mapAccessToken(String spayload) throws JsonMappingException, JsonProcessingException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected String getLoginUrl(String callbackUrl) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected String getLogoutUrl() {
        return null;
    }

    @Override
    protected UserTokenPayload requestUserRoles(String access_token, long issued_at, long expires_at) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected boolean verifyState(String state) {
        // TODO Auto-generated method stub
        return false;
    }

}