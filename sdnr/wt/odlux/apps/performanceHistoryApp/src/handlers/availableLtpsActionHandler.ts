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

import {
  AllAvailableLtpsLoadedAction,
  LoadAllAvailableLtpsAction,
  SetInitialLoadedAction,
} from '../actions/ltpAction';

import { LtpIds } from '../models/availableLtps';

export interface IAvailableLtpsState {
  distinctLtps: LtpIds[];
  busy: boolean;
  loadedOnce: boolean;
}

const ltpListStateInit: IAvailableLtpsState = {
  distinctLtps: [],
  busy: false,
  loadedOnce: false
};

export const availableLtpsActionHandler: IActionHandler<IAvailableLtpsState> = (state = ltpListStateInit, action) => {
  if (action instanceof LoadAllAvailableLtpsAction) {

    state = {
      ...state,
      busy: true
    };

  } else if (action instanceof AllAvailableLtpsLoadedAction) {
    if (!action.error && action.availableLtps) {
      state = {
        ...state,
        distinctLtps: action.availableLtps,
        busy: false,
        loadedOnce: true
      };
    }
  } else if (action instanceof SetInitialLoadedAction) {

    state = {
      ...state,
      loadedOnce: action.initialLoaded
    };
  } else {
    state = {
      ...state,
      busy: false
    };
  }

  return state;
};