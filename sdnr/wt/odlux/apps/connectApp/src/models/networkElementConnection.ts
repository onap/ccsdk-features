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

export type NetworkElementConnection = {
  id?: string;
  nodeId: string;
  isRequired: boolean;
  host: string;
  port: number;
  username?: string;
  password?: string;
  tlsKey?: string;
  weburi?: string;
  isWebUriUnreachable?: boolean;
  status?: 'Connected' | 'mounted' | 'unmounted' | 'Connecting' | 'Disconnected' | 'idle';
  coreModelCapability?: string;
  deviceType?: string;
  deviceFunction?: string;
  nodeDetails?: {
    availableCapabilites: {
      capabilityOrigin: string;
      capability: string;
    }[];
    unavailableCapabilities: {
      failureReason: string;
      capability: string;
    }[];
  };
};


export type UpdateNetworkElement = {
  id: string;
  isRequired?: boolean;
  username?: string;
  password?: string;
  tlsKey?: string;
};

export type ConnectionStatus = {
  status: string;
};

export type TlsKeys = {
  key: string;
};


/**
 * Checks if a object has a given propertyname, if yes, the name is returned as string.
 * @throws at compile time if property is not available
 * @param name propertyname
 */
export const propertyOf = <TObj>(name: keyof TObj) => name;