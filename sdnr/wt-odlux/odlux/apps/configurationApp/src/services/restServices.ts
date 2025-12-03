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

import { requestRest, requestRestExt } from '../../../../framework/src/services/restService';
import { convertPropertyNames, replaceHyphen } from '../../../../framework/src/utilities/yangHelper';

import { NetworkElementConnection } from '../models/networkElementConnection';

type ImportOnlyResponse = {
  'ietf-yang-library:yang-library': {
    'module-set': {
      'import-only-module': {
        'name': string;
        'revision': string;
      }[];
    }[];
  };
};


type CapabilityResponse = {
  'network-topology:node': {
    'node-id': string;
    'netconf-node-topology:netconf-node':{
      'available-capabilities': {
        'available-capability': {
          'capability-origin': string;
          'capability': string;
        }[];
      };
      'unavailable-capabilities': {
        'unavailable-capability': {
          'capability': string;
          'failure-reason': string;
        }[];
      };
    };
  }[];
};

type CapabilityAnswer = {
  availableCapabilities: {
    capabilityOrigin: string;
    capability: string;
    version: string;
  }[] | null;
  unavailableCapabilities: {
    failureReason: string;
    capability: string;
    version: string;
  }[] | null;
  importOnlyModules: {
    name: string;
    revision: string;
  }[] | null;
};

const capParser = /^\(.*\?revision=(\d{4}-\d{2}-\d{2})\)(\S+)$/i;

class RestService {
  public getNetworkElementUri = (nodeId: string) => '/rests/data/network-topology:network-topology/topology=topology-netconf/node=' + nodeId;

  public async getImportOnlyModules(nodeId: string): Promise<{ name: string; revision: string }[]> {
    const path = `${this.getNetworkElementUri(nodeId)}/yang-ext:mount/ietf-yang-library:yang-library?content=nonconfig&fields=module-set(import-only-module(name;revision))`;
    const importOnlyResult = await requestRest<ImportOnlyResponse>(path, { method: 'GET' });
    const importOnlyModules = importOnlyResult
      ? importOnlyResult['ietf-yang-library:yang-library']['module-set'][0]['import-only-module']
      : [];
    return importOnlyModules;
  }

  public async getCapabilitiesByMountId(nodeId: string): Promise<CapabilityAnswer> {
    const path = this.getNetworkElementUri(nodeId);
    const capabilitiesResult = await requestRest<CapabilityResponse>(path, { method: 'GET' });
    const availableCapabilities = capabilitiesResult && capabilitiesResult['network-topology:node'] && capabilitiesResult['network-topology:node'].length > 0 &&
      (capabilitiesResult['network-topology:node'][0]['netconf-node-topology:netconf-node']['available-capabilities'] &&
        capabilitiesResult['network-topology:node'][0]['netconf-node-topology:netconf-node']['available-capabilities']['available-capability'] &&
        capabilitiesResult['network-topology:node'][0]['netconf-node-topology:netconf-node']['available-capabilities']['available-capability'].map<any>(obj => convertPropertyNames(obj, replaceHyphen)) || [])
        .map(cap => {
          const capMatch = cap && capParser.exec(cap.capability);
          return capMatch ? {
            capabilityOrigin: cap.capabilityOrigin,
            capability: capMatch && capMatch[2] || '',
            version: capMatch && capMatch[1] || '',
          } : null ;
        }).filter((cap) => cap != null) || [] as any;

    const unavailableCapabilities = capabilitiesResult && capabilitiesResult['network-topology:node'] && capabilitiesResult['network-topology:node'].length > 0 &&
      (capabilitiesResult['network-topology:node'][0]['netconf-node-topology:netconf-node']['unavailable-capabilities'] &&
      capabilitiesResult['network-topology:node'][0]['netconf-node-topology:netconf-node']['unavailable-capabilities']['unavailable-capability'] &&
      capabilitiesResult['network-topology:node'][0]['netconf-node-topology:netconf-node']['unavailable-capabilities']['unavailable-capability'].map<any>(obj => convertPropertyNames(obj, replaceHyphen)) || [])
        .map(cap => {
          const capMatch = cap && capParser.exec(cap.capability);
          return capMatch ? {
            failureReason: cap.failureReason,
            capability: capMatch && capMatch[2] || '',
            version: capMatch && capMatch[1] || '',
          } : null ;
        }).filter((cap) => cap != null) || [] as any;

    const importOnlyModules = availableCapabilities && availableCapabilities.findIndex((ac: { capability: string }) => ac.capability && ac.capability.toLowerCase() === 'ietf-yang-library') > -1
      ? await this.getImportOnlyModules(nodeId)
      : null;

    return { availableCapabilities, unavailableCapabilities, importOnlyModules };
  }

  public async getMountedNetworkElementByMountId(nodeId: string): Promise<NetworkElementConnection | null> {
    // const path = 'restconf/operational/network-topology:network-topology/topology/topology-netconf/node/' + nodeId;
    // const connectedNetworkElement = await requestRest<NetworkElementConnection>(path, { method: "GET" });
    // return connectedNetworkElement || null;

    const path = '/rests/operations/data-provider:read-network-element-connection-list';
    const body = { 'data-provider:input': { 'filter': [{ 'property': 'node-id', 'filtervalue': nodeId }], 'sortorder': [], 'pagination': { 'size': 1, 'page': 1 } } };
    const networkElementResult = await requestRest<{ 'data-provider:output': { data: NetworkElementConnection[] } }>(path, { method: 'POST', body: JSON.stringify(body) });
    return networkElementResult && networkElementResult['data-provider:output'] && networkElementResult['data-provider:output'].data &&
      networkElementResult['data-provider:output'].data.map(obj => convertPropertyNames(obj, replaceHyphen))[0] || null;
  }

  /** Reads the config data by restconf path.
  * @param path The restconf path to be used for read.
  * @returns The data.
  */
  public getConfigData(path: string) {
    return requestRestExt<{ [key: string]: any }>(path, { method: 'GET' });
  }

  /** Updates or creates the config data by restconf path using data.
   * @param path The restconf path to identify the note to update.
   * @param data The data to be updated.
   * @returns The written data.
   */
  public setConfigData(path: string, data: any, method: 'PUT' | 'POST' = 'PUT') {
    return requestRestExt<{ [key: string]: any }>(path, { method, body: JSON.stringify(data) });
  }

  public executeRpc(path: string, data: any) {
    return requestRestExt<{ [key: string]: any }>(path, { method: 'POST', body: JSON.stringify(data) });
  }

  /** Removes the element by restconf path.
  * @param path The restconf path to identify the note to update.
  * @returns The restconf result.
  */
  public removeConfigElement(path: string) {
    return requestRestExt<{ [key: string]: any }>(path, { method: 'DELETE' });
  }
}

export const restService = new RestService();
export default restService;