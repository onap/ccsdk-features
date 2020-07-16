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
import { requestRest, formEncode } from "./restService";
import { AuthToken } from "../models/authentication";

type AuthTokenResponse = {
  access_token: string;
  token_type: string;
  expires_in: number;
}


class AuthenticationService {
  public async authenticateUserOAuth(email: string, password: string, scope: string): Promise<AuthToken | null> {
    const result = await requestRest<string>(`oauth2/token`, {
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
    const resultObj: AuthTokenResponse| null = result && JSON.parse(result);
    return resultObj && {
      username: email,
      access_token: resultObj.access_token,
      token_type: resultObj.token_type,
      expires: (new Date().valueOf()) + (resultObj.expires_in * 1000)
    } || null;
  }

   public async authenticateUserBasicAuth(email: string, password: string, scope: string): Promise<AuthToken | null> {
    const result = await requestRest<string>(`restconf/modules`, {
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
          expires: (new Date()).valueOf() + 2678400000 // 31 days
      }
    }
    return null;
  }
}

export const authenticationService = new AuthenticationService();
export default authenticationService;