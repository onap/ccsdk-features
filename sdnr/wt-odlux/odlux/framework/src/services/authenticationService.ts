/**
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt odlux
 * =================================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
 * =================================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * ============LICENSE_END==========================================================================
 */
import { AuthPolicy, AuthToken } from "../models/authentication";
import { ExternalLoginProvider } from "../models/externalLoginProvider";

import { requestRest, formEncode, requestRestExt } from "./restService";

type AuthTokenResponse = {
  access_token: string;
  token_type: string;
  expires_at: number;
  issued_at: number;
}

class AuthenticationService {
  public async getAvaliableExteralProvider() {
    const result = await requestRest<ExternalLoginProvider[]>(`oauth/providers`, {
      method: "GET",
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
    }, false);
    return result;
  }

  public async authenticateUserOAuth(email: string, password: string, scope: string): Promise<AuthToken | null> {
    const result = await requestRest<AuthTokenResponse>(`oauth/login`, {
      method: "POST",
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      body: formEncode({
        grant_type: "password",
        username: email,
        password: password,
        scope: scope
      })
    }, false);

   
    return result && {
      username: email,
      access_token: result.access_token,
      token_type: result.token_type,
      expires: result.expires_at,
      issued: result.issued_at
    } || null;
  }

   public async authenticateUserBasicAuth(email: string, password: string, scope: string): Promise<AuthToken | null> {
    const result = await requestRest<string>(`rests/data/network-topology:network-topology/topology=topology-netconf?fields=node(node-id)`, {
      method: "GET",
      headers: {
        'Authorization':  "Basic " + btoa(email + ":" + password)
      },
    }, false);

    if (result) {
      return {
          username: email,
          access_token:  btoa(email + ":" + password),
          token_type: "Basic",
          expires: (new Date()).valueOf() / 1000 + 86400, // 1 day
          issued: (new Date()).valueOf() / 1000
      }
    }
    return null;
  }

  public async getAccessPolicies(){
    return await requestRest<AuthPolicy[]>(`oauth/policies`, { method: "GET" }, true);
  }

  public async getServerReadyState(){
    const result = await requestRestExt(`/ready`, { method: "GET" }, false);
    return result.status == (200 || 304) ? true : false;
  }
}

export const authenticationService = new AuthenticationService();
export default authenticationService;