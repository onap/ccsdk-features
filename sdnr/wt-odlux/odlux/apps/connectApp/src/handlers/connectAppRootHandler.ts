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

import { IActionHandler } from '../../../../framework/src/flux/action';
import { combineActionHandler } from '../../../../framework/src/flux/middleware';

import { AddWebUriList, RemoveWebUri, SetPanelAction } from '../actions/commonNetworkElementsActions';
import { guiCutThrough } from '../models/guiCutTrough';
import { PanelId } from '../models/panelId';
import { connectionStatusLogActionHandler, IConnectionStatusLogState } from './connectionStatusLogHandler';
import { IInfoNetworkElementFeaturesState, IInfoNetworkElementsState, infoNetworkElementFeaturesActionHandler, infoNetworkElementsActionHandler } from './infoNetworkElementHandler';
import { INetworkElementsState, networkElementsActionHandler } from './networkElementsHandler';
import { availableTlsKeysActionHandler, IAvailableTlsKeysState } from './tlsKeyHandler';

export interface IConnectAppStoreState {
  networkElements: INetworkElementsState;
  connectionStatusLog: IConnectionStatusLogState;
  currentOpenPanel: PanelId;
  elementInfo: IInfoNetworkElementsState;
  elementFeatureInfo: IInfoNetworkElementFeaturesState;
  guiCutThrough: guiCutThroughState;
  availableTlsKeys: IAvailableTlsKeysState;
}

const currentOpenPanelHandler: IActionHandler<PanelId> = (state = null, action) => {
  if (action instanceof SetPanelAction) {
    state = action.panelId;
  }
  return state;
};

interface guiCutThroughState {
  searchedElements: guiCutThrough[];
  notSearchedElements: string[];
  unsupportedElements: string[];
}

const guiCutThroughHandler: IActionHandler<guiCutThroughState> = (state = { searchedElements: [], notSearchedElements: [], unsupportedElements: [] }, action) => {
  if (action instanceof AddWebUriList) {
    let notSearchedElements: string[];
    let searchedElements: guiCutThrough[];
    let unsupportedElements: string[];

    notSearchedElements = state.notSearchedElements.concat(action.notSearchedElements);
    unsupportedElements = state.unsupportedElements.concat(action.unsupportedElements);

    //remove elements, which were just searched
    if (action.newlySearchedElements) {
      action.newlySearchedElements.forEach(item => {
        notSearchedElements = notSearchedElements.filter(id => id !== item);
      });
    }

    searchedElements = state.searchedElements.concat(action.searchedElements);

    state = { searchedElements: searchedElements, notSearchedElements: notSearchedElements, unsupportedElements: unsupportedElements };

  } else if (action instanceof RemoveWebUri) {
    const nodeId = action.element;
    const webUris = state.searchedElements.filter(item => item.id !== nodeId);
    const knownElements = state.notSearchedElements.filter(item => item !== nodeId);
    const unsupportedElement = state.unsupportedElements.filter(item => item != nodeId);
    state = { notSearchedElements: knownElements, searchedElements: webUris, unsupportedElements: unsupportedElement };
  }
  return state;
};

declare module '../../../../framework/src/store/applicationStore' {
  interface IApplicationStoreState {
    connect: IConnectAppStoreState;
  }
}

const actionHandlers = {
  networkElements: networkElementsActionHandler,
  connectionStatusLog: connectionStatusLogActionHandler,
  currentOpenPanel: currentOpenPanelHandler,
  elementInfo: infoNetworkElementsActionHandler,
  elementFeatureInfo: infoNetworkElementFeaturesActionHandler,
  guiCutThrough: guiCutThroughHandler,
  availableTlsKeys: availableTlsKeysActionHandler,
};

export const connectAppRootHandler = combineActionHandler<IConnectAppStoreState>(actionHandlers);
export default connectAppRootHandler;
