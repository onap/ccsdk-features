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

import { DeviceListType } from '../models/deviceListType';
import { PerformanceHistoryService } from '../services/performanceHistoryService';

/** 
 * Represents the base action. 
 */
export class BaseAction extends Action { }

/** 
 * Represents an action causing the store to load all devices. 
 */
export class LoadAllDeviceListAction extends BaseAction { }

/** 
 * Represents an action causing the store to update all devices. 
 */
export class AllDeviceListLoadedAction extends BaseAction {
  /**
   * Initialize this instance.
   * 
   * @param deviceList All the distinct devices from the performance history database.
   */
  constructor(public deviceList: DeviceListType[] | null, public error?: string) {
    super();
  }
}

/** 
 * Represents an asynchronous thunk  action to load all devices. 
 */
export const loadAllDeviceListAsync = async (dispatch: Dispatch) => {
  dispatch(new LoadAllDeviceListAction());
  const deviceListFromPerfHistory: DeviceListType[] = (await PerformanceHistoryService.getDeviceListfromPerf15minHistory().then(ne => (ne))) || [];
  const deviceListFromPerf24History: DeviceListType[] = (await PerformanceHistoryService.getDeviceListfromPerf24hHistory().then(ne => (ne))) || [];
  deviceListFromPerf24History.forEach(deviceList24h => {
    if (deviceListFromPerfHistory.findIndex(deviceList15min => deviceList15min.nodeId === deviceList24h.nodeId) < 0) {
      deviceListFromPerfHistory.push(deviceList24h);
    }
  });
  return deviceListFromPerfHistory && dispatch(new AllDeviceListLoadedAction(deviceListFromPerfHistory));
};

/** 
 * Represents an action causing the store to update mountId. 
 */
export class UpdateMountId extends BaseAction {
  constructor(public nodeId?: string) {
    super();
  }
}

/** 
 * Represents an asynchronous thunk  action to load updated mountId. 
 */
export const updateMountIdActionCreator = (nodeId: string) => async (dispatch: Dispatch) => {
  return dispatch(new UpdateMountId(nodeId));
};
