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
import { INetworkElementsState, networkElementsActionHandler } from './networkElementsHandler';
import { IConnectionStatusLogState, connectionStatusLogActionHandler } from './connectionStatusLogHandler';
import { IInfoNetworkElementsState, infoNetworkElementsActionHandler } from './infoNetworkElementHandler';
import { SetPanelAction, AddWebUriList, RemoveWebUri } from '../actions/commonNetworkElementsActions';
import { PanelId } from '../models/panelId';
import { guiCutThrough } from '../models/guiCutTrough';

export interface IConnectAppStoreState {
  networkElements: INetworkElementsState;
  connectionStatusLog: IConnectionStatusLogState;
  currentOpenPanel: PanelId;
  elementInfo: IInfoNetworkElementsState;
  guiCutThrough: guiCutThroughState;
}

const currentOpenPanelHandler: IActionHandler<PanelId> = (state = null, action) => {
  if (action instanceof SetPanelAction) {
    state = action.panelId;
  }
  return state;
}

interface guiCutThroughState {
  availableWebUris: guiCutThrough[];
  knownElements: string[];
}

const guiCutThroughHandler: IActionHandler<guiCutThroughState> = (state = { availableWebUris: [], knownElements: [] }, action) => {
  if (action instanceof AddWebUriList) {
    let knownElements: string[];
    let availableWebUris: guiCutThrough[];

    knownElements = state.knownElements.concat(action.knownElements);

    availableWebUris = state.availableWebUris.concat(action.element);

    state = { availableWebUris: availableWebUris, knownElements: knownElements }

  } else if (action instanceof RemoveWebUri) {
    const nodeId = action.element;
    const webUris = state.availableWebUris.filter(item => item.nodeId !== nodeId);
    const knownElements = state.knownElements.filter(item => item !== nodeId);
    state = { knownElements: knownElements, availableWebUris: webUris };
  }
  return state;
}

declare module '../../../../framework/src/store/applicationStore' {
  interface IApplicationStoreState {
    connect: IConnectAppStoreState
  }
}

const actionHandlers = {
  networkElements: networkElementsActionHandler,
  connectionStatusLog: connectionStatusLogActionHandler,
  currentOpenPanel: currentOpenPanelHandler,
  elementInfo: infoNetworkElementsActionHandler,
  guiCutThrough: guiCutThroughHandler
};

export const connectAppRootHandler = combineActionHandler<IConnectAppStoreState>(actionHandlers);
export default connectAppRootHandler;
