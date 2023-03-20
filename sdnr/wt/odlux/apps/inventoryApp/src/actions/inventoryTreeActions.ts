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

import { AddErrorInfoAction } from '../../../../framework/src/actions/errorActions';
import { NavigateToApplication } from '../../../../framework/src/actions/navigationActions';
import { Action } from '../../../../framework/src/flux/action';
import { Dispatch } from '../../../../framework/src/flux/store';

import { InventoryTreeNode, InventoryType, TreeDemoItem } from '../models/inventory';
import { inventoryService } from '../services/inventoryService';

/**
 * Represents the base action.
 */
export class BaseAction extends Action { }

export class SetBusyAction extends BaseAction {
  constructor(public busy: boolean = true) {
    super();

  }
}

export class SetSearchTextAction extends BaseAction {
  constructor(public searchTerm: string = '') {
    super();

  }
}

export class UpdateInventoryTreeAction extends BaseAction {
  constructor(public rootNode: InventoryTreeNode) {
    super();

  }
}

export class UpdateSelectedNodeAction extends BaseAction {
  constructor(public selectedNode?: InventoryType) {
    super();

  }
}

export class UpdateExpandedNodesAction extends BaseAction {
  constructor(public expandedNodes?: TreeDemoItem[]) {
    super();

  }
}

export const setSearchTermAction = (searchTerm: string) => (dispatch: Dispatch) =>{
  dispatch(new SetSearchTextAction(searchTerm));
};


export const updateInventoryTreeAsyncAction = (mountId: string, searchTerm?: string) => async (dispatch: Dispatch) => {
  dispatch(new SetBusyAction(true));
  dispatch(new SetSearchTextAction(searchTerm));
  try {
    const result = await inventoryService.getInventoryTree(mountId, searchTerm);
    if (!result) {
      dispatch(new AddErrorInfoAction({ title: 'Error', message: `Could not load inventory tree for [${mountId}]. Please check you connection to the server and try later.` }));
      dispatch(new NavigateToApplication('inventory'));
    } else {
      dispatch(new UpdateInventoryTreeAction(result));
    }
  } catch (err) {
    throw new Error('Could not load inventory tree from server.');
  } finally {
    dispatch(new SetBusyAction(false));
  }
};

export const selectInventoryNodeAsyncAction = (nodeId: string) => async (dispatch: Dispatch) => {
  dispatch(new SetBusyAction(true));
  try {
    const result = await inventoryService.getInventoryEntry(nodeId);
    if (!result) throw new Error('Could not load inventory tree from server.');
    dispatch(new UpdateSelectedNodeAction(result));
  } catch (err) {
    throw new Error('Could not load inventory tree from server.');
  } finally {
    dispatch(new SetBusyAction(false));
  }
};
