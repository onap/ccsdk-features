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

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.shiro.authc.BearerToken;

public class OAuthToken {
    private final String access_token;
    private final String token_type;
    private final long expires_at;
    private final long issued_at;

    public OAuthToken(BearerToken btoken) {
        this.access_token = btoken.getToken();
        this.token_type = "Bearer";
        DecodedJWT token = JWT.decode(this.access_token);
        this.expires_at = token.getExpiresAt().getTime() / 1000L;
        this.issued_at = token.getIssuedAt().getTime() / 1000L;
    }

    public String getAccess_token() {
        return access_token;
    }

    public String getToken_type() {
        return token_type;
    }

    public long getExpires_at() {
        return expires_at;
    }
    public long getIssued_at() {
        return issued_at;
    }

}
