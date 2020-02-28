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
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';
import { IActionHandler } from '../../../../framework/src/flux/action';

import { IFaultNotifications, faultNotificationsHandler } from './notificationsHandler';
import { ICurrentProblemsState, currentProblemsActionHandler } from './currentProblemsHandler';
import { IAlarmLogEntriesState, alarmLogEntriesActionHandler } from './alarmLogEntriesHandler';
import { SetPanelAction, RememberCurrentPanelAction } from '../actions/panelChangeActions';
import { IFaultStatus, faultStatusHandler } from './faultStatusHandler';
import { stuckAlarmHandler } from './clearStuckAlarmsHandler';
import { PanelId } from 'models/panelId';

export interface IFaultAppStoreState {
  currentProblems: ICurrentProblemsState;
  faultNotifications: IFaultNotifications;
  alarmLogEntries: IAlarmLogEntriesState;
  currentOpenPanel: ICurrentOpenPanelState;
  faultStatus: IFaultStatus;
}

type ICurrentOpenPanelState = { openPanel: string | null, savedPanel: PanelId | null };
const panelInitState = { openPanel: null, savedPanel: null };
const currentOpenPanelHandler: IActionHandler<ICurrentOpenPanelState> = (state = panelInitState, action) => {
  if (action instanceof SetPanelAction) {
    state = { ...state, openPanel: action.panelId };
  } else if (action instanceof RememberCurrentPanelAction) {
    state = { ...state, savedPanel: action.panelId };
  }
  return state;
}

declare module '../../../../framework/src/store/applicationStore' {
  interface IApplicationStoreState {
    fault: IFaultAppStoreState;
  }
}

const actionHandlers = {
  currentProblems: currentProblemsActionHandler,
  faultNotifications: faultNotificationsHandler,
  alarmLogEntries: alarmLogEntriesActionHandler,
  currentOpenPanel: currentOpenPanelHandler,
  faultStatus: faultStatusHandler,
  stuckAlarms: stuckAlarmHandler
};

export const faultAppRootHandler = combineActionHandler<IFaultAppStoreState>(actionHandlers);
export default faultAppRootHandler;
