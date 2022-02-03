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
package org.onap.ccsdk.features.sdnr.wt.oauthprovider.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.http.BaseHTTPResponse;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.KeycloakUserTokenPayload;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.OAuthResponseData;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.http.client.MappedBaseHttpResponse;

public class TestDeserializer {

    @Test
    public void test1() throws IOException {
        final String token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ1OHNXaTF4QWxjT1pyelY4X0l2VjliMlJTaFdZUWV4aXZYUXNYLTFTME"
                + "RNIn0.eyJleHAiOjE2MTAzNjE2OTQsImlhdCI6MTYxMDM2MTM5NCwianRpIjoiOWRhOThmMTYtOTEyOS00N2NmLTgzOGQtNWQzYmVkYzYyZTJjIiwiaXNzIjoiaHR0cDovLzEwLjIwLjExLjE2MDo4MDgwL2F1dGgvcmVhbG1zL21hc3RlciIsInN1YiI6IjE4MzhjNGYyLTVmZTMtNGYwYy1iMmQyLWQzNjRiMjdhNDk5NyIsInR5cCI6IkJlYXJlciIsImF6cCI6ImFkbWluLWNsaSIsInNlc3Npb25fc3RhdGUiOiJjYzcxZmMxZi1hZGQ0LTRhODYtYWU1ZS1jMzRkZjQwM2M3NzIiLCJhY3IiOiIxIiwic2NvcGUiOiJlbWFpbCBwcm9maWxlIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhZG1pbiJ9.PUT4NzCM1ej3sNMMCkQa1NuQQwDgn19G-OnWL4NgLvZ3ocJUZ1Yfr9KAPkrJHaiK_HXQqwTA-Ma6Qn7BBMoXNdFjwu0k_HpqyUbBDilGN4wpkGiUeS1p5SW4T_hnWJtwCJ5BYkEvF6WaEbi7MFCbEVO9LVcUvsa-7St1WZ8V8RVfbWgjAu7ejlxe6RYUDMYzIKDj5F5y1-qCyoKzGIjt5ajcA9FWrexHifLJECKO8ZG08Wp7xQld1sYPOdde6XHMwiyNelTwd_EzCBgUw_8664rETGDVtyfuYchowo5Z6fmn4U87L6EGjEuxiAE8f3USy_jh6UF0LnvyTyq_9I"
                + "M1VA";
        final String response =
                "{\"access_token\":\"eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ1OHNXaTF4QWxjT1pyelY4X0l2VjliMlJTaFdZUWV4aXZYUXNYLTFTME"
                        + "RNIn0.eyJleHAiOjE2MTAzNjE2OTQsImlhdCI6MTYxMDM2MTM5NCwianRpIjoiOWRhOThmMTYtOTEyOS00N2NmLTgzOGQtNWQzYmVkYzYyZTJjIiwiaXNzIjoiaHR0cDovLzEwLjIwLjExLjE2MDo4MDgwL2F1dGgvcmVhbG1zL21hc3RlciIsInN1YiI6IjE4MzhjNGYyLTVmZTMtNGYwYy1iMmQyLWQzNjRiMjdhNDk5NyIsInR5cCI6IkJlYXJlciIsImF6cCI6I"
                        + "mFkbWluLWNsaSIsInNlc3Npb25fc3RhdGUiOiJjYzcxZmMxZi1hZGQ0LTRhODYtYWU1ZS1jMzRkZjQwM2M3NzIiLCJhY3IiOiIxIiwic2NvcGUiOiJlbWFpbCBwcm9maWxlIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJhZG1pbiJ9.PUT4NzCM1ej3sNMMCkQa1NuQQwDgn19G-OnWL4NgLvZ3ocJUZ1Yfr9KAPkrJHaiK_HX"
                        + "QqwTA-Ma6Qn7BBMoXNdFjwu0k_HpqyUbBDilGN4wpkGiUeS1p5SW4T_hnWJtwCJ5BYkEvF6WaEbi7MFCbEVO9LVcUvsa-7St1WZ8V8RVfbWgjAu7ejlxe6RYUDMYzIKDj5F5y1-qCyoKzGIjt5ajcA9FWrexHifLJECKO8ZG08Wp7xQld1sYPOdde6XHMwiyNelTwd_EzCBgUw_8664rETGDVtyfuYchowo5Z6fmn4U87L6EGjEuxiAE8f3USy_jh6UF0LnvyTyq_9I"
                        + "M1VA\",\"expires_in\":300,\"refresh_expires_in\":1800,\"refresh_token\":\"eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI1NzdiN2Q3MC00NzMwLTQ0MDMtODk4My04ZjJmYTg4M2U2M2EifQ.eyJleHAiOjE2MTAzNjMxOTQsImlhdCI6MTYxMDM2MTM5NCwianRpIjoiMmNjMGY4YWYtNWY2OC00YmFhLWEyOTctNjMxMjk2YzhmY2"
                        + "U5IiwiaXNzIjoiaHR0cDovLzEwLjIwLjExLjE2MDo4MDgwL2F1dGgvcmVhbG1zL21hc3RlciIsImF1ZCI6Imh0dHA6Ly8xMC4yMC4xMS4xNjA6ODA4MC9hdXRoL3JlYWxtcy9tYXN0ZXIiLCJzdWIiOiIxODM4YzRmMi01ZmUzLTRmMGMtYjJkMi1kMzY0YjI3YTQ5OTciLCJ0eXAiOiJSZWZyZXNoIiwiYXpwIjoiYWRtaW4tY2xpIiwic2Vzc2lvbl9zdGF0ZSI6I"
                        + "mNjNzFmYzFmLWFkZDQtNGE4Ni1hZTVlLWMzNGRmNDAzYzc3MiIsInNjb3BlIjoiZW1haWwgcHJvZmlsZSJ9.qutqcFuQW-GzaCVNMfiYrbmHYD34GYwBqIbaQbJSY-g\",\"token_type\":\"bearer\",\"not-before-policy\":0,\"session_state\":\"cc71fc1f-add4-4a86-ae5e-c34df403c772\",\"scope\":\"email profile\"} ";

        BaseHTTPResponse res = new BaseHTTPResponse(200, response);
        OAuthResponseData data = new MappedBaseHttpResponse<>(res,OAuthResponseData.class).body;
        assertEquals(token,data.getAccess_token());

    }

    @Test
    public void testUserPayloadDeser() throws JsonMappingException, JsonProcessingException {
        final String payload = "{\n"
                + "  \"exp\": 1610362593,\n"
                + "  \"iat\": 1610361393,\n"
                + "  \"jti\": \"09bd6f2c-5dba-44a0-bd76-cd0d440137d0\",\n"
                + "  \"iss\": \"http://10.20.11.160:8080/auth/realms/onap\",\n"
                + "  \"aud\": \"account\",\n"
                + "  \"sub\": \"446a24bc-d8a0-43dd-afa5-e56eed75deb8\",\n"
                + "  \"typ\": \"Bearer\",\n"
                + "  \"azp\": \"admin-cli\",\n"
                + "  \"session_state\": \"db2c96f4-cc9b-47e8-a83f-a01c50d656f2\",\n"
                + "  \"acr\": \"1\",\n"
                + "  \"realm_access\": {\n"
                + "    \"roles\": [\n"
                + "      \"provision\",\n"
                + "      \"offline_access\",\n"
                + "      \"uma_authorization\"\n"
                + "    ]\n"
                + "  },\n"
                + "  \"resource_access\": {\n"
                + "    \"account\": {\n"
                + "      \"roles\": [\n"
                + "        \"manage-account\",\n"
                + "        \"manage-account-links\",\n"
                + "        \"view-profile\"\n"
                + "      ]\n"
                + "    }\n"
                + "  },\n"
                + "  \"scope\": \"profile email\",\n"
                + "  \"email_verified\": false,\n"
                + "  \"name\": \"Luke Skywalker\",\n"
                + "  \"preferred_username\": \"luke.skywalker\",\n"
                + "  \"given_name\": \"Luke\",\n"
                + "  \"family_name\": \"Skywalker\",\n"
                + "  \"email\": \"luke.skywalker@sdnr.onap.org\"\n"
                + "}";

        ObjectMapper mapper = new ObjectMapper();
        KeycloakUserTokenPayload data = mapper.readValue(payload,KeycloakUserTokenPayload.class);
        assertNotNull(data.getRealmAccess());
        assertEquals(3, data.getRealmAccess().getRoles().size());
    }
}
