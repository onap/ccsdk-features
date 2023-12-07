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
// main state handler

import { combineActionHandler } from '../../../../framework/src/flux/middleware';
// ** do not remove **
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';

import { PanelId } from '../models/panelId';
import { IInventoryDeviceListState, inventoryDeviceListActionHandler } from './inventoryDeviceListActionHandler';
import { IInventoryElementsState, inventoryElementsActionHandler } from './inventoryElementsHandler';
import { IInvenroryTree, inventoryTreeHandler } from './inventoryTreeHandler';
import { currentOpenPanelHandler } from './panelHandler';

export interface IInventoryAppStateState {
  inventoryTree: IInvenroryTree;
  currentOpenPanel: PanelId;
  inventoryElements: IInventoryElementsState;
  inventoryDeviceList: IInventoryDeviceListState;
}

declare module '../../../../framework/src/store/applicationStore' {
  interface IApplicationStoreState {
    inventory: IInventoryAppStateState;
  }
}

const actionHandlers = {
  inventoryTree: inventoryTreeHandler,
  currentOpenPanel: currentOpenPanelHandler,
  inventoryElements: inventoryElementsActionHandler,
  inventoryDeviceList: inventoryDeviceListActionHandler,
};

export const inventoryAppRootHandler = combineActionHandler<IInventoryAppStateState>(actionHandlers);
export default inventoryAppRootHandler;

