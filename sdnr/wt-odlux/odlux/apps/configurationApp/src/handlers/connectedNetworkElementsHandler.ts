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

import { createExternal, IExternalTableState } from '../../../../framework/src/components/material-table/utilities';
import { createSearchDataHandler } from '../../../../framework/src/utilities/elasticSearch';
import { getAccessPolicyByUrl } from '../../../../framework/src/services/restService';

import { NetworkElementConnection } from '../models/networkElementConnection';
import { restService } from '../services/restServices';

export interface IConnectedNetworkElementsState extends IExternalTableState<NetworkElementConnection> { }

// create elastic search material data fetch handler
const connectedNetworkElementsSearchHandler = createSearchDataHandler<NetworkElementConnection>('network-element-connection', false, { status: 'Connected' });

export const {
  actionHandler: connectedNetworkElementsActionHandler,
  createActions: createConnectedNetworkElementsActions,
  createProperties: createConnectedNetworkElementsProperties,
  reloadAction: connectedNetworkElementsReloadAction,

  // set value action, to change a value
} = createExternal<NetworkElementConnection>(connectedNetworkElementsSearchHandler, appState => appState.configuration.connectedNetworkElements, 
  (ne) => {
    if (!ne || !ne.id) return true;
    const neUrl = restService.getNetworkElementUri(ne.id);
    const policy = getAccessPolicyByUrl(neUrl);
    return !(policy.GET && policy.POST);
  },
);
