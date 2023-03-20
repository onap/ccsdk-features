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

import { SetBusyAction, SetSearchTextAction, UpdateExpandedNodesAction, UpdateInventoryTreeAction, UpdateSelectedNodeAction } from '../actions/inventoryTreeActions';
import { InventoryTreeNode, InventoryType, TreeDemoItem } from '../models/inventory';


export interface IInvenroryTree {
  isBusy: boolean;
  rootNodes: TreeDemoItem[];
  selectedNode?: InventoryType;
  expandedItems: TreeDemoItem[];
  searchTerm: string;
}

const initialState: IInvenroryTree = {
  isBusy: false,
  rootNodes: [],
  searchTerm: '',
  selectedNode: undefined,
  expandedItems: [],
};


const getTreeDataFromInvetoryTreeNode = (node: InventoryTreeNode): TreeDemoItem[] => Object.keys(node).reduce<TreeDemoItem[]>((acc, key) => {
  const cur = node[key];
  acc.push({
    isMatch: cur.isMatch,
    content: cur.label || key,
    value: key,
    children: cur.children && getTreeDataFromInvetoryTreeNode(cur.children),
  });
  return acc;
}, []);

export const inventoryTreeHandler: IActionHandler<IInvenroryTree> = (state = initialState, action) => {
  if (action instanceof SetBusyAction) {
    state = { ...state, isBusy: action.busy };
  } else if (action instanceof SetSearchTextAction) {
    state = { ...state, searchTerm: action.searchTerm };
  } else if (action instanceof UpdateInventoryTreeAction) {
    const rootNodes = getTreeDataFromInvetoryTreeNode(action.rootNode);
    state = { ...state, rootNodes: rootNodes, expandedItems: [], selectedNode: undefined };
  } else if (action instanceof UpdateSelectedNodeAction) {
    state = { ...state, selectedNode: action.selectedNode };
  } else if (action instanceof UpdateExpandedNodesAction) {
    state = { ...state, expandedItems: action.expandedNodes || [] };
  }

  return state;
};