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

import { SetFaultStatusAction } from '../actions/statusActions';

export interface IFaultStatus {
  critical: number;
  major: number;
  minor: number;
  warning: number;
  isLoadingAlarmStatusChart: boolean;
  Connected: number;
  Connecting: number;
  Disconnected: number;
  Mounted: number;
  UnableToConnect: number;
  Undefined: number;
  Unmounted: number;
  total: number;
  isLoadingConnectionStatusChart: boolean;
}

const faultStatusInit: IFaultStatus = {
  critical: 0,
  major: 0,
  minor: 0,
  warning: 0,
  isLoadingAlarmStatusChart: false,
  Connected: 0,
  Connecting: 0,
  Disconnected: 0,
  Mounted: 0,
  UnableToConnect: 0,
  Undefined: 0,
  Unmounted: 0,
  total: 0,
  isLoadingConnectionStatusChart: false,
};

export const faultStatusHandler: IActionHandler<IFaultStatus> = (state = faultStatusInit, action) => {
  if (action instanceof SetFaultStatusAction) {
    state = {
      critical: action.criticalFaults,
      major: action.majorFaults,
      minor: action.minorFaults,
      warning: action.warnings,
      isLoadingAlarmStatusChart: action.isLoadingAlarmStatusChart,
      Connected: action.ConnectedCount,
      Connecting: action.ConnectingCount,
      Disconnected: action.DisconnectedCount,
      Mounted: action.MountedCount,
      UnableToConnect: action.UnableToConnectCount,
      Undefined: action.UndefinedCount,
      Unmounted: action.UnmountedCount,
      total: action.totalCount,
      isLoadingConnectionStatusChart: action.isLoadingConnectionStatusChart,
    };
  }

  return state;
};