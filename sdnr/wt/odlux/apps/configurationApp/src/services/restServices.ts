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

import { requestRest, requestRestExt } from "../../../../framework/src/services/restService";
import { convertPropertyNames, replaceHyphen } from "../../../../framework/src/utilities/yangHelper";

import { NetworkElementConnection } from "../models/networkElementConnection";

class RestService {
  public async getCapabilitiesByMoutId(nodeId: string): Promise<{ "capabilityOrigin": string, "capability": string }[] | null> {
    const path = `/restconf/operational/network-topology:network-topology/topology/topology-netconf/node/${nodeId}`;
    const capabilitiesResult = await requestRest<{ node: { "node-id": string, "netconf-node-topology:available-capabilities": { "available-capability": { "capabilityOrigin": string, "capability": string }[] }}[] }>(path, { method: "GET" });
    return capabilitiesResult && capabilitiesResult.node && capabilitiesResult.node.length > 0 &&
      capabilitiesResult.node[0]["netconf-node-topology:available-capabilities"]["available-capability"].map(obj => convertPropertyNames(obj, replaceHyphen)) || null;
  }

  public async getMountedNetworkElementByMountId(nodeId: string): Promise<NetworkElementConnection | null> {
    // const path = 'restconf/operational/network-topology:network-topology/topology/topology-netconf/node/' + nodeId;
    // const connectedNetworkElement = await requestRest<NetworkElementConnection>(path, { method: "GET" });
    // return connectedNetworkElement || null;

    const path = "/restconf/operations/data-provider:read-network-element-connection-list";
    const body = { "input": { "filter": [{ "property": "node-id", "filtervalue": nodeId }], "sortorder": [], "pagination": { "size": 1, "page": 1 } } };
    const networkElementResult = await requestRest<{ output: { data: NetworkElementConnection[] } }>(path, { method: "POST", body: JSON.stringify(body) });
    return networkElementResult && networkElementResult.output && networkElementResult.output.data &&
      networkElementResult.output.data.map(obj => convertPropertyNames(obj, replaceHyphen))[0] || null;
  }

  /** Reads the config data by restconf path.
  * @param path The restconf path to be used for read.
  * @returns The data.
  */
  public getConfigData(path: string) {
    return requestRestExt<{ [key: string]: any }>(path, { method: "GET" });
  }

  /** Updates or creates the config data by restconf path using data.
   * @param path The restconf path to identify the note to update.
   * @param data The data to be updated.
   * @returns The written data.
   */
  public setConfigData(path: string, data: any) {
    return requestRestExt<{ [key: string]: any }>(path, { method: "PUT", body: JSON.stringify(data) });
  }
}

export const restService = new RestService();
export default restService;