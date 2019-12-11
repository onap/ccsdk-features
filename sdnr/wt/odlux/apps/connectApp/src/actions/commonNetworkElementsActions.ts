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

export class SetPanelAction extends Action {
  constructor (public panelId: PanelId) {
    super();
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

