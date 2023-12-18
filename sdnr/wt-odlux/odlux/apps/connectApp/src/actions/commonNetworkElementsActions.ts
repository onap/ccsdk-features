<<<<<<< HEAD   (907af9 fix oauth code)
=======

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

/**
 * Create an update action that can distinguish whether one or the other view is currently active and update it.
 * This action is then used for each update in the other actions and when notifications arrive.
 * create an update action that can distinguish whether one or the other view is currently active and update it.
 * This action is then used for each update in the other actions and when notifications arrive.
 */

import { Action } from '../../../../framework/src/flux/action';
import { Dispatch } from '../../../../framework/src/flux/store';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';

import { connectionStatusLogReloadAction } from '../handlers/connectionStatusLogHandler';
import { networkElementsReloadAction } from '../handlers/networkElementsHandler';
import { guiCutThrough } from '../models/guiCutTrough';
import { PanelId } from '../models/panelId';
import { connectService } from '../services/connectService';


export class SetPanelAction extends Action {
  constructor(public panelId: PanelId) {
    super();
  }
}

export class AddWebUriList extends Action {
  constructor(public searchedElements: guiCutThrough[], public notSearchedElements: string[], public unsupportedElements: string[], public newlySearchedElements?: string[]) {
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
};

export class SetWeburiSearchBusy extends Action {
  constructor(public isbusy: boolean) {
    super();
  }
}

let isBusy = false;
export const findWebUrisForGuiCutThroughAsyncAction = (networkElementIds: string[]) => async (dispatcher: Dispatch, getState: () => IApplicationStoreState) => {

  // keep method from executing simultanously; state not used because change of iu isn't needed

  if (isBusy)
    return;
  isBusy = true;

  const { connect: { guiCutThrough: guiCutThrough2, networkElements } } = getState();

  let notConnectedElements: string[] = [];
  let elementsToSearch: string[] = [];
  let prevFoundElements: string[] = [];
  let unsupportedElements: string[] = [];

  networkElementIds.forEach(id => {
    const item = networkElements.rows.find((ne) => ne.id === id);
    if (item) {
      if (item.status) {

        // if (item.coreModelCapability !== "Unsupported") {
        // element is connected and is added to search list, if it doesn't exist already
        const exists = guiCutThrough2.searchedElements.filter(element => element.id === id).length > 0;
        if (!exists) {
          elementsToSearch.push(id);

          //element was found previously, but wasn't connected
          if (guiCutThrough2.notSearchedElements.length > 0 && guiCutThrough2.notSearchedElements.includes(id)) {
            prevFoundElements.push(id);
          }
        }
        // } else {
        //   // element does not support core model and must not be searched for a weburi  
        //   const id = item.id as string;
        //   const exists = guiCutThrough.unsupportedElements.filter(element => element === id).length > 0;
        //   if (!exists) {
        //     unsupportedElements.push(id);

        //     //element was found previously, but wasn't connected
        //     if (guiCutThrough.notSearchedElements.length > 0 && guiCutThrough.notSearchedElements.includes(id)) {
        //       prevFoundElements.push(id);
        //     }
        //   }
        // }
      } else {
        // element isn't connected and cannot be searched for a weburi
        if (!guiCutThrough2.notSearchedElements.includes(id)) {
          notConnectedElements.push(item.id as string);
        }
      }
    }
  });


  if (elementsToSearch.length > 0 || unsupportedElements.length > 0) {
    const result = await connectService.getAllWebUriExtensionsForNetworkElementListAsync(elementsToSearch);
    dispatcher(new AddWebUriList(result, notConnectedElements, unsupportedElements, prevFoundElements));
  }
  isBusy = false;

};

export const setPanelAction = (panelId: PanelId) => {
  return new SetPanelAction(panelId);
};

export const updateCurrentViewAsyncAction = () => (dispatch: Dispatch, getState: () => IApplicationStoreState) => {
  const { connect: { currentOpenPanel } } = getState();
  if (currentOpenPanel === 'NetworkElements') {
    return dispatch(networkElementsReloadAction);
  } else {
    return dispatch(connectionStatusLogReloadAction);
  }
};

>>>>>>> CHANGE (de91a1 Web Client context menu item display)
