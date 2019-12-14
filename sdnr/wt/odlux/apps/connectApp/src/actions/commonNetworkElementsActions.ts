// update action erstellen, die unterscheiden kann, ob die eine oder die andere Ansicht gerade aktive ist und diese katualisiert.
// Diese action wird dann bei jeder aktualisierung in den anderen Actions und bei eintreffen von notifikationen verwendet.

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

import { Action } from '../../../../framework/src/flux/action';
import { Dispatch } from '../../../../framework/src/flux/store';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';

import { networkElementsReloadAction } from '../handlers/networkElementsHandler';
import { connectionStatusLogReloadAction } from '../handlers/connectionStatusLogHandler';

import { PanelId } from '../models/panelId';
import { guiCutThrough } from '../models/guiCutTrough';
import connectService from '../services/connectService';
import { NetworkElementConnection } from '../models/networkElementConnection';

export class SetPanelAction extends Action {
  constructor(public panelId: PanelId) {
    super();
  }
}

export class AddWebUriList extends Action {
  constructor(public element: guiCutThrough[], public knownElements: string[]) {
    super();
  }
}

export class RemoveWebUri extends Action {
  constructor(public element: string) {
    super();
  }
}

export const removeWebUriAction = (nodeId: string) => {
  return new RemoveWebUri(nodeId);
}

let isBusy = false;
export const findWebUrisForGuiCutThroughAsyncAction = (dispatcher: Dispatch) => (networkElements: NetworkElementConnection[], knownElements: string[]) => {

  // keep method from executing simultanously; state not used because change of iu isn't needed
  if (isBusy)
    return;
  isBusy = true;

  const nodeIds = networkElements.map(element => { return element.id as string });

  if (knownElements.length > 0) {

    let elementsToSearch: string[] = [];

    nodeIds.forEach(element => {
      // find index of nodeId
      const index = knownElements.indexOf(element);

      // if element dosen't exist, add it to list
      if (index === -1) {
        elementsToSearch.push(element)
      }
    });

    // if new elements were found, search for weburi
    if (elementsToSearch.length > 0) {
      const foundWebUris = connectService.getAllWebUriExtensionsForNetworkElementListAsync(elementsToSearch);
      foundWebUris.then(result => {
        dispatcher(new AddWebUriList(result, elementsToSearch));
        isBusy = false;
      })

    } else {
      isBusy = false;
    }

  } else {
    connectService.getAllWebUriExtensionsForNetworkElementListAsync(nodeIds).then(result => {
      dispatcher(new AddWebUriList(result, nodeIds));
      isBusy = false;
    })
  }
}

export const setPanelAction = (panelId: PanelId) => {
  return new SetPanelAction(panelId);
}

export const updateCurrentViewAsyncAction = () => (dispatch: Dispatch, getState: () => IApplicationStoreState) => {
  const { connect: { currentOpenPanel } } = getState();
  if (currentOpenPanel === "NetworkElements") {
    return dispatch(networkElementsReloadAction);
  }
  else {
    return dispatch(connectionStatusLogReloadAction);
  }
};

