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
import { TlsKeys } from '../models/networkElementConnection';
import { convertPropertyNames, replaceUpperCase } from '../../../../framework/src/utilities/yangHelper';
import { Result } from '../../../../framework/src/models/elasticSearch';

import { FeatureTopology, Topology, TopologyNode, Module } from '../models/topologyNetconf';
import { guiCutThrough } from '../models/guiCutTrough';

/**
* Represents a web api accessor service for all network element/node actions.
*/
class ConnectService {
  public getNetworkElementUri = (nodeId: string) => '/rests/data/network-topology:network-topology/topology=topology-netconf/node=' + nodeId;

  public getNetworkElementConnectDataProviderUri = (operation: 'create' | 'update' | 'delete') => `/rests/operations/data-provider:${operation}-network-element-connection`;

  public getAllWebUriExtensionsForNetworkElementListUri = (nodeId: string) => this.getNetworkElementUri(nodeId) + '/yang-ext:mount/core-model:network-element';

  public getNetworkElementYangLibraryFeature = (nodeId: string) => '/rests/data/network-topology:network-topology/topology=topology-netconf/node=' + nodeId + '/yang-ext:mount/ietf-yang-library:yang-library?content=nonconfig';

  /**
   * Inserts a network element/node.
   */
  public async createNetworkElement(element: NetworkElementConnection): Promise<NetworkElementConnection | null> {
    const path = this.getNetworkElementConnectDataProviderUri('create');
    const result = await requestRest<NetworkElementConnection>(path, {
      method: 'POST', body: JSON.stringify(convertPropertyNames({ 'data-provider:input': element }, replaceUpperCase)),
    });
    return result || null;
  }

  /**
  * Updates a network element/node.
  */
  public async updateNetworkElement(element: UpdateNetworkElement): Promise<NetworkElementConnection | null> {
    const path = this.getNetworkElementConnectDataProviderUri('update');
    const result = await requestRest<NetworkElementConnection>(path, {
      method: 'POST', body: JSON.stringify(convertPropertyNames({ 'data-provider:input': element }, replaceUpperCase)),
    });
    return result || null;
  }

  /**
    * Deletes a network element/node.
    */
  public async deleteNetworkElement(element: UpdateNetworkElement): Promise<NetworkElementConnection | null> {
    const query = {
      'id': element.id,
    };
    const path = this.getNetworkElementConnectDataProviderUri('delete');
    const result = await requestRest<NetworkElementConnection>(path, {
      method: 'POST', body: JSON.stringify(convertPropertyNames({ 'data-provider:input': query }, replaceUpperCase)),
    });
    return result || null;
  }

  /** Mounts network element/node */
  public async mountNetworkElement(networkElement: NetworkElementConnection): Promise<boolean> {
    const path = this.getNetworkElementUri(networkElement.nodeId);
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

    const tlsXml = [
      '<node xmlns="urn:TBD:params:xml:ns:yang:network-topology">',
      `<node-id>${networkElement.nodeId}</node-id>`,
      '<key-based xmlns="urn:opendaylight:netconf-node-topology">',
      `<key-id xmlns="urn:opendaylight:netconf-node-topology">${networkElement.tlsKey}</key-id>`,
      `<username xmlns="urn:opendaylight:netconf-node-topology">${networkElement.username}</username>`,
      '</key-based>',
      `<host xmlns="urn:opendaylight:netconf-node-topology">${networkElement.host}</host>`,
      `<port xmlns="urn:opendaylight:netconf-node-topology">${networkElement.port}</port>`,
      '<tcp-only xmlns="urn:opendaylight:netconf-node-topology">false</tcp-only>',
      '<protocol xmlns="urn:opendaylight:netconf-node-topology">',
      '<name xmlns="urn:opendaylight:netconf-node-topology">TLS</name>',
      ' </protocol>',
      '<max-connection-attempts xmlns="urn:opendaylight:netconf-node-topology">2</max-connection-attempts>',
      '</node>'].join('');
    let bodyXml;
    if (networkElement.password) {
      bodyXml = mountXml;
    } else {
      bodyXml = tlsXml;
    }

    try {
      const result = await requestRest<string>(path, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/xml',
          'Accept': 'application/xml',
        },
        body: bodyXml,
      });
      // expect an empty answer
      return result !== null;
    } catch {
      return false;
    }
  }

  /** Unmounts a network element by its id. */
  public async unmountNetworkElement(nodeId: string): Promise<boolean> {
    const path = this.getNetworkElementUri(nodeId);

    try {
      const result = await requestRest<string>(path, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/xml',
          'Accept': 'application/xml',
        },
      });
      // expect an empty answer
      return result !== null;

    } catch {
      return false;
    }
  }

  /** Yang capabilities of the selected network element/node */
  public async infoNetworkElement(nodeId: string): Promise<TopologyNode | null> {
    const path = this.getNetworkElementUri(nodeId);
    const topologyRequestPomise = requestRest<Topology>(path, { method: 'GET' });

    return topologyRequestPomise && topologyRequestPomise.then(result => {
      return result && result['network-topology:node'] && result['network-topology:node'][0] || null;
    });
  }


  /** Yang features of the selected network element/node module */
  public async infoNetworkElementFeatures(nodeId: string): Promise<Module[] | null | undefined> {
    const path = this.getNetworkElementYangLibraryFeature(nodeId);
    const topologyRequestPomise = requestRest<FeatureTopology>(path, { method: 'GET' });

    return topologyRequestPomise && topologyRequestPomise.then(result => {
      const resultFinal = result && result['ietf-yang-library:yang-library']
        && result['ietf-yang-library:yang-library']['module-set'] &&
        result['ietf-yang-library:yang-library']['module-set'][0] &&
        result['ietf-yang-library:yang-library']['module-set'][0].module || null;
      return resultFinal;
    });
  }



  /**
   * Get the connection state of the network element/ node
   */
  public async getNetworkElementConnectionStatus(element: string): Promise<(ConnectionStatus)[] | null> {
    const path = '/rests/operations/data-provider:read-network-element-connection-list';
    const query = {
      'data-provider:input': {
        'filter': [{
          'property': 'node-id',
          'filtervalue': element,
        }],
        'pagination': {
          'size': 20,
          'page': 1,
        },
      },
    };
    const result = await requestRest<Result<ConnectionStatus>>(path, { method: 'POST', body: JSON.stringify(query) });
    return result && result['data-provider:output'] && result['data-provider:output'].data && result['data-provider:output'].data.map(ne => ({
      status: ne.status,
    })) || null;
  }

  /**
  * Gets all available tlsKeys.
  */

  public async getTlsKeys(): Promise<(TlsKeys)[] | null> {
    const path = '/rests/operations/data-provider:read-tls-key-entry';
    const query = {
      'data-provider:input': {
        'filter': [],
        'sortorder': [],
        'pagination': {
          'size': 20,
          'page': 1,
        },
      },
    };

    const result = await requestRest<Result<string>>(path, { method: 'POST', body: JSON.stringify(query) });
    return result && result['data-provider:output'] && result['data-provider:output'].data && result['data-provider:output'].data.map(ne => ({
      key: ne,
    })) || null;
  }

  public async getAllWebUriExtensionsForNetworkElementListAsync(neList: string[]): Promise<(guiCutThrough)[]> {
    const path = '/rests/operations/data-provider:read-gui-cut-through-entry';
    let webUriList: guiCutThrough[] = [];
    const query = {
      'data-provider:input': {
        'filter': [{
          'property': 'id',
          'filtervalues': neList,
        }],
        'pagination': {
          'size': 20,
          'page': 1,
        },
      },
    };

    const result = await requestRest<Result<guiCutThrough>>(path, { method: 'POST', body: JSON.stringify(query) });
    const resultData = result && result['data-provider:output'] && result['data-provider:output'].data;
    neList.forEach(nodeId => {
      let entryNotFound = true;
      if (resultData) {
        try {
          resultData.forEach(entry => {
            if (entry.id == nodeId) {
              entryNotFound = false;
              if (entry.weburi) {
                webUriList.push({ id: nodeId, weburi: entry.weburi });
              } else {
                webUriList.push({ id: nodeId, weburi: undefined });
              }
              throw new Error();
            }
          });
        } catch (e) { }
      }
      if (entryNotFound)
        webUriList.push({ id: nodeId, weburi: undefined });
    });
    return webUriList;
  }

  //  public async getAllWebUriExtensionsForNetworkElementListAsync(ne: string[]): Promise<(guiCutThrough)[] | null> {

  //   let promises: any[] = [];
  //   let webUris: guiCutThrough[] = []

  //   ne.forEach(nodeId => {
  //     const path = this.getAllWebUriExtensionsForNetworkElementListUri(nodeId);

  // // add search request to array
  //     promises.push(requestRest<any>(path, { method: "GET" })
  //       .then(result => {
  //         if (result != null && result['core-model:network-element'] && result['core-model:network-element'].extension) {
  //           const webUri = result['core-model:network-element'].extension.find((item: any) => item['value-name'] === "webUri")
  //           if (webUri) {
  //             webUris.push({ weburi: webUri.value, id: nodeId });
  //           } else {
  //             webUris.push({ weburi: undefined, id: nodeId });
  //           }
  //         } else {
  //           webUris.push({ weburi: undefined, id: nodeId });
  //         }
  //       })
  //       .catch(error => {
  //         webUris.push({ weburi: undefined, id: nodeId });
  //       }))
  //   })
  //   // wait until all promises are done and return weburis
  //   return Promise.all(promises).then(result => { return webUris });
  // }

}



export const connectService = new ConnectService();
