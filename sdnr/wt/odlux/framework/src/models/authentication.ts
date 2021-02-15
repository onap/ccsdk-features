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

export type AuthToken = {
  username: string;
  access_token: string;
  token_type: string;
  expires: number;
}

export type AuthPolicy = {
  path: string;
  methods: {
    get?: boolean;
    post?: boolean;
    put?: boolean;
    patch?: boolean;
    delete?: boolean;
  }
}

export class User {

  constructor (private _bearerToken: AuthToken) {

  }

  public get user(): string | null {
    return this._bearerToken && this._bearerToken.username;
  };

  public get token(): string | null {
    return this._bearerToken && this._bearerToken.access_token;
  }

  public get tokenType(): string | null {
    return this._bearerToken && this._bearerToken.token_type;
  }

  public get isValid(): boolean {
    return (this._bearerToken && (new Date().valueOf()) < this._bearerToken.expires) || false;
  }

  public toString() {
    return JSON.stringify(this._bearerToken);
  }

  public static fromString(data: string) {
    return new User(JSON.parse(data));
  }


}
