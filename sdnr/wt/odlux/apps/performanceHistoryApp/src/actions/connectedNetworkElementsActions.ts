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

import { ConnectedNetworkElementIds } from '../models/connectedNetworkElements';

/** 
 * Represents the base action. 
 */
export class BaseAction extends Action { }

/** 
 * Represents an action causing the store to load all connected network element Ids. 
 */
export class LoadAllConnectedNetworkElementsAction extends BaseAction { }

/** 
 * Represents an action causing the store to update all connected network element Ids. 
 */
export class AllConnectedNetworkElementsLoadedAction extends BaseAction {
  /**
   * Initialize this instance.
   * 
   * @param connectedNetworkElements The connected network element Ids which are loaded from the state of connectApp.
   */
  constructor(public connectedNetworkElementIds: ConnectedNetworkElementIds[] | null, public error?: string) {
    super();
  }
}

/** 
 * Represents an asynchronous thunk  action to load all connected network element Ids. 
 */
export const loadAllConnectedNetworkElementsAsync = (dispatch: Dispatch, getState: () => IApplicationStoreState) => {
  window.setTimeout(() => {
    dispatch(new LoadAllConnectedNetworkElementsAction());
    const connectedNetworkElementsIds = getState().connect.mountedNetworkElements;
    let mountIdList: ConnectedNetworkElementIds[] = [];
    connectedNetworkElementsIds.elements.forEach(element => {
      const connectedNetworkElement = {
        mountId: element.mountId
      }
      mountIdList.push(connectedNetworkElement);
    });
    mountIdList.sort((a, b) => {
      if (a.mountId < b.mountId) return -1;
      if (a.mountId > b.mountId) return 1;
      return 0;
    });
    dispatch(new AllConnectedNetworkElementsLoadedAction(mountIdList));
  }, 500);
};

/** 
 * Represents an action causing the store to update mountId. 
 */
export class UpdateMountId extends BaseAction {
  constructor (public nodeId?: string) {
    super();
  }
}

/** 
 * Represents an asynchronous thunk  action to load updated mountId. 
 */
export const updateMountIdActionCreator = (nodeId: string ) => async (dispatch: Dispatch) => {
  return dispatch(new UpdateMountId(nodeId));
}
