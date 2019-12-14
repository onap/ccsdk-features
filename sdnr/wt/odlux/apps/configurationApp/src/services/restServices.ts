import { requestRest, requestRestExt } from "../../../../framework/src/services/restService";
import { convertPropertyNames, replaceHyphen } from "../../../../framework/src/utilities/yangHelper";

import { NetworkElementConnection } from "../models/networkElementConnection";

class RestService {
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