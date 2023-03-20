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

import { IActionHandler } from '../../../../framework/src/flux/action';
import { combineActionHandler } from '../../../../framework/src/flux/middleware';
// ** do not remove **
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';

import { SetPanelAction } from '../actions/panelChangeActions';
import { PanelId } from '../models/panelId';
import { alarmLogEntriesActionHandler, IAlarmLogEntriesState } from './alarmLogEntriesHandler';
import { currentAlarmsActionHandler, ICurrentAlarmsState } from './currentAlarmsHandler';
import { faultStatusHandler, IFaultStatus } from './faultStatusHandler';
import { faultNotificationsHandler, IFaultNotifications } from './notificationsHandler';

export interface IFaultAppStoreState {
  currentAlarms: ICurrentAlarmsState;
  faultNotifications: IFaultNotifications;
  alarmLogEntries: IAlarmLogEntriesState;
  currentOpenPanel: PanelId | null;
  faultStatus: IFaultStatus;
}

const currentOpenPanelHandler: IActionHandler<PanelId | null> = (state = null, action) => {
  if (action instanceof SetPanelAction) {
    state = action.panelId;
  }
  return state;
};

declare module '../../../../framework/src/store/applicationStore' {
  interface IApplicationStoreState {
    fault: IFaultAppStoreState;
  }
}

const actionHandlers = {
  currentAlarms: currentAlarmsActionHandler,
  faultNotifications: faultNotificationsHandler,
  alarmLogEntries: alarmLogEntriesActionHandler,
  currentOpenPanel: currentOpenPanelHandler,
  faultStatus: faultStatusHandler,
};

export const faultAppRootHandler = combineActionHandler<IFaultAppStoreState>(actionHandlers);
export default faultAppRootHandler;
