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
import { Dispatch } from '../../../../framework/src/flux/store';

import { getFaultStateFromDatabase } from '../services/faultStatusService';
import { FaultApplicationBaseAction } from './notificationActions';


export class SetFaultStatusAction extends FaultApplicationBaseAction {
  constructor(public criticalFaults: number, public majorFaults: number, public minorFaults: number, public warnings: number,
    public isLoadingAlarmStatusChart: boolean, public ConnectedCount: number, public ConnectingCount: number, public DisconnectedCount: number,
    public MountedCount: number, public UnableToConnectCount: number, public UndefinedCount: number, public UnmountedCount: number,
    public totalCount: number, public isLoadingConnectionStatusChart: boolean) {
    super();
  }
}


export const refreshFaultStatusAsyncAction = async (dispatch: Dispatch) => {

  // dispatch(new SetFaultStatusAction(0, 0, 0, 0, true, 0, 0, 0, 0, 0, 0, 0, 0, true));
  const result = await getFaultStateFromDatabase().catch(_ => null);
  if (result) {
    const statusAction = new SetFaultStatusAction(
      result.Critical || 0,
      result.Major || 0,
      result.Minor || 0,
      result.Warning || 0,
      false,
      result.Connected || 0,
      result.Connecting || 0,
      result.Disconnected || 0,
      result.Mounted || 0,
      result.UnableToConnect || 0,
      result.Undefined || 0,
      result.Unmounted || 0,
      result.total || 0,
      false,
    );
    dispatch(statusAction);
    return;
  } else {
    dispatch(new SetFaultStatusAction(0, 0, 0, 0, false, 0, 0, 0, 0, 0, 0, 0, 0, false));
  }
};
