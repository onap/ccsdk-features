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

import { CoreModelNetworkElement, NameValue } from '../models/coreModel';
import { ViewSpecification } from '../models/uiModels';

export const getValueByName = (name: string, nameValuePairs: NameValue[], defaultValue: string | null = null): string | null => {
  const entry = nameValuePairs.find(p => p["value-name"] === name);
  return entry && entry.value || defaultValue;
};

class ConfigurationService {

  /** Gets the core model for a network element by its mountId. */
  public async getCoreModelByNodeId(nodeId: string): Promise<CoreModelNetworkElement | null> {
    const path = `restconf/config/network-topology:network-topology/topology/topology-netconf/node/${nodeId}/yang-ext:mount/core-model:network-element`;
    const ne = await requestRest<{ "network-element": CoreModelNetworkElement }>(path, { method: "GET" });
    return ne && ne["network-element"] || null;
  }

  public async getViewData(path: string): Promise<{} | null> {
    const viewData = await requestRest<{}>(path, { method: "GET" });
    return viewData || null;
  }

  /** Gets the UI description object for a capability of a network element. */
  public async getUIDescriptionByCapability(capability: string, revision: string | null): Promise<ViewSpecification[] | null> {
    const capFile = capability && revision && `${capability}@${revision}.json`;
    const coreModelResponse = capFile && await requestRest<{ views: ViewSpecification[] }>(`assets/${capFile}`, { method: "GET" }, false, true);
    return coreModelResponse && coreModelResponse.views || null;
  }

}

export const configurationService = new ConfigurationService();
export default configurationService;