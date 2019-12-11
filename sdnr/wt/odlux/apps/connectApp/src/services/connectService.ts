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

import { requestRest } from '../../../../framework/src/services/restService';
import { NetworkElementConnection, ConnectionStatus, UpdateNetworkElement } from '../models/networkElementConnection';
import { convertPropertyNames, replaceUpperCase } from '../../../../framework/src/utilities/yangHelper';
import { Result } from '../../../../framework/src/models/elasticSearch';

import { Topology, TopologyNode } from '../models/topologyNetconf';

/**
* Represents a web api accessor service for all Network Elements actions.
*/
class ConnectService {

  /**
   * Inserts a network elements.
   */
  public async createNetworkElement(element: NetworkElementConnection): Promise<NetworkElementConnection | null> {
    const path = `/restconf/operations/data-provider:create-network-element-connection`;
    const result = await requestRest<NetworkElementConnection>(path, {
      method: "POST", body: JSON.stringify(convertPropertyNames({ input: element }, replaceUpperCase))
    });
    return result || null;
  }

  /**
  * Updates a network element.
  */
  public async updateNetworkElement(element: UpdateNetworkElement): Promise<NetworkElementConnection | null> {
    const path = `/restconf/operations/data-provider:update-network-element-connection`;
    const result = await requestRest<NetworkElementConnection>(path, {
      method: "POST", body: JSON.stringify(convertPropertyNames({ input: element }, replaceUpperCase))
    });
    return result || null;
  }

  /**
    * Deletes a network element.
    */
  public async deleteNetworkElement(element: UpdateNetworkElement): Promise<NetworkElementConnection | null> {
    const query = {
      "id": element.id
    };
    const path = `/restconf/operations/data-provider:delete-network-element-connection`;
    const result = await requestRest<NetworkElementConnection>(path, {
      method: "POST", body: JSON.stringify(convertPropertyNames({ input: query }, replaceUpperCase))
    });
    return result || null;
  }

  /** Mounts network element. */
  public async mountNetworkElement(networkElement: NetworkElementConnection): Promise<boolean> {
    const path = 'restconf/config/network-topology:network-topology/topology/topology-netconf/node/' + networkElement.nodeId;
    const mountXml = [
      '<node xmlns="urn:TBD:params:xml:ns:yang:network-topology">',
      `<node-id>${networkElement.nodeId}</node-id>`,
      `<host xmlns="urn:opendaylight:netconf-node-topology">${networkElement.host}</host>`,
      `<port xmlns="urn:opendaylight:netconf-node-topology">${networkElement.port}</port>`,
      `<username xmlns="urn:opendaylight:netconf-node-topology">${networkElement.username}</username>`,
      `<password xmlns="urn:opendaylight:netconf-node-topology">${networkElement.password}</password>`,
      '  <tcp-only xmlns="urn:opendaylight:netconf-node-topology">false</tcp-only>',

      '  <!-- non-mandatory fields with default values, you can safely remove these if you do not wish to override any of these values-->',
      '  <reconnect-on-changed-schema xmlns="urn:opendaylight:netconf-node-topology">false</reconnect-on-changed-schema>',
      '  <connection-timeout-millis xmlns="urn:opendaylight:netconf-node-topology">20000</connection-timeout-millis>',
      '  <max-connection-attempts xmlns="urn:opendaylight:netconf-node-topology">100</max-connection-attempts>',
      '  <between-attempts-timeout-millis xmlns="urn:opendaylight:netconf-node-topology">2000</between-attempts-timeout-millis>',
      '  <sleep-factor xmlns="urn:opendaylight:netconf-node-topology">1.5</sleep-factor>',

      '  <!-- keepalive-delay set to 0 turns off keepalives-->',
      '  <keepalive-delay xmlns="urn:opendaylight:netconf-node-topology">120</keepalive-delay>',
      '</node>'].join('');

    try {
      const result = await requestRest<string>(path, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/xml',
          'Accept': 'application/xml'
        },
        body: mountXml
      });
      // expect an empty answer
      return result !== null;
    } catch {
      return false;
    }
  };

  /** Unmounts a network element by its id. */
  public async unmountNetworkElement(nodeId: string): Promise<boolean> {
    const path = 'restconf/config/network-topology:network-topology/topology/topology-netconf/node/' + nodeId;

    try {
      const result = await requestRest<string>(path, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/xml',
          'Accept': 'application/xml'
        },
      });
      // expect an empty answer
      return result !== null;

    } catch {
      return false;
    }
  };

    /** Yang capabilities of the selected network elements. */
  public async infoNetworkElement(nodeId: string): Promise<TopologyNode | null> {
    const path = 'restconf/operational/network-topology:network-topology/topology/topology-netconf/node/' + nodeId;
    const topologyRequestPomise = requestRest<Topology>(path, { method: "GET" });

    return topologyRequestPomise && topologyRequestPomise.then(result => {
      return result && result.node && result.node[0] || null;
    });
  }

  /**
   * Get the connection state of the network element.
   */
  public async getNetworkElementConnectionStatus(element: string): Promise<(ConnectionStatus)[] | null> {
    const path = `/restconf/operations/data-provider:read-network-element-connection-list`;
    const query = {
      "input": {
        "filter": [{
          "property": "node-id",
          "filtervalue": element
        }],
        "pagination": {
          "size": 20,
          "page": 1
        }
      }
    }
    const result = await requestRest<Result<ConnectionStatus>>(path, { method: "POST", body: JSON.stringify(query) });
    return result && result.output && result.output.data && result.output.data.map(ne => ({
      status: ne.status
    })) || null;
  }

  public async getWebUriExtensionForNetworkElementAsync(ne: string) {
    const path = 'restconf/config/network-topology:network-topology/topology/topology-netconf/node/' + ne + '/yang-ext:mount/core-model:network-element';
    try {
      const result = await requestRest<any>(path, { method: "GET" });
      
      if (result['network-element'].extension) {
        const webUri = result['network-element'].extension.find((item: any) => item['value-name'] === "webUri")
        if (webUri) {
          return webUri.value as string;
        }
      }
    } catch (error) {
      console.log(ne + ' unrechable: ' + error)
    }

    return undefined;

  }

}
export const connectService = new ConnectService();
export default connectService;
