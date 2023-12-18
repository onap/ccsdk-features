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
import { getAccessPolicyByUrl } from '../../../../framework/src/services/restService';
import { createSearchDataHandler } from '../../../../framework/src/utilities/elasticSearch';

import { NetworkElementConnection } from '../models/networkElementConnection';
import { connectService } from '../services/connectService';

export interface INetworkElementsState extends IExternalTableState<NetworkElementConnection> { }

// create eleactic search material data fetch handler
const networkElementsSearchHandler = createSearchDataHandler<NetworkElementConnection>('network-element-connection');

export const {
  actionHandler: networkElementsActionHandler,
  createActions: createNetworkElementsActions,
  createProperties: createNetworkElementsProperties,
  reloadAction: networkElementsReloadAction,

  // set value action, to change a value
} = createExternal<NetworkElementConnection>(networkElementsSearchHandler, appState => {

  const webUris = appState.connect.guiCutThrough.searchedElements;
  // add weburi links, if element is connected & weburi available
  if (appState.connect.networkElements.rows && webUris.length > 0) {

    appState.connect.networkElements.rows.forEach(element => {

      if (element.status) {
        const webUri = webUris.find(item => item.id === element.id as string);
        if (webUri) {
          element.weburi = webUri.weburi;
          element.isWebUriUnreachable = false;
        } else {
          element.isWebUriUnreachable = true;
        }
      }
    });
  }

  return appState.connect.networkElements;
}, (ne) => {
  if (!ne || !ne.id) return true;
  const neUrl = connectService.getNetworkElementUri(ne.id);
  const policy = getAccessPolicyByUrl(neUrl);
  return !(policy.GET || policy.POST);
});

