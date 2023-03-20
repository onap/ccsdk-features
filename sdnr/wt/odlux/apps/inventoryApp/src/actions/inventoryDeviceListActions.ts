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
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';

import { InventoryDeviceListType } from '../models/inventoryDeviceListType';
import { inventoryService } from '../services/inventoryService';

/** 
 * Represents the base action. 
 */
export class BaseAction extends Action { }

/** 
 * Represents an action causing the store to load all nodes. 
 */
export class LoadAllInventoryDeviceListAction extends BaseAction { }

/** 
 * Represents an action causing the store to update all nodes. 
 */
export class AllInventoryDeviceListLoadedAction extends BaseAction {
  /**
   * Initialize this instance.
   * 
   * @param inventoryDeviceList All the distinct nodes from the Inventory  database.
   */
  constructor(public inventoryDeviceList: InventoryDeviceListType[] | null, public error?: string) {
    super();
  }
}

/** 
 * Represents an asynchronous thunk  action to load all nodes. 
 */
export const loadAllInventoryDeviceListAsync = async (dispatch: Dispatch) => {
  dispatch(new LoadAllInventoryDeviceListAction());
  const inventoryDeviceList: InventoryDeviceListType[] = (await inventoryService.getInventoryDeviceList().then(ne =>
    (ne))) || [];
  return inventoryDeviceList && dispatch(new AllInventoryDeviceListLoadedAction(inventoryDeviceList));
};

