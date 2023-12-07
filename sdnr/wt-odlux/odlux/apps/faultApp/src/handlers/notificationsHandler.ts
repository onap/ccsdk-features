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

import { AddFaultNotificationAction, ResetFaultNotificationsAction } from '../actions/notificationActions';
import { FaultAlarmNotification } from '../models/fault';

export interface IFaultNotifications {
  faults: FaultAlarmNotification[];
  since: Date;
}

const faultNotoficationsInit: IFaultNotifications = {
  faults: [],
  since: new Date(),
};

export const faultNotificationsHandler: IActionHandler<IFaultNotifications> = (state = faultNotoficationsInit, action) => {
  if (action instanceof AddFaultNotificationAction) {
    state = {
      ...state,
      faults: [...state.faults, action.fault],
    };
  } else if (action instanceof ResetFaultNotificationsAction) {
    state = {
      ...state,
      faults: [],
      since: new Date(),
    };
  }

  return state;
};