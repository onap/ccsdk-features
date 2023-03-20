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

import { LtpIds } from '../models/availableLtps';
import { PerformanceHistoryService } from '../services/performanceHistoryService';

/** 
 * Represents the base action. 
 */
export class BaseAction extends Action { }

/** 
 * Represents an action causing the store to load available ltps. 
 */
export class LoadAllAvailableLtpsAction extends BaseAction { }

/** 
 * Represents an action causing the store to update available ltps. 
 */
export class AllAvailableLtpsLoadedAction extends BaseAction {
  /**
   * Initialize this instance.
   * @param availableLtps The available ltps which are returned from the database.
   */
  constructor(public availableLtps: LtpIds[] | null, public error?: string) {
    super();
  }
}

export class SetInitialLoadedAction extends BaseAction {
  constructor(public initialLoaded: boolean) {
    super();
  }
}

export class NoLtpsFoundAction extends BaseAction { }

export class ResetLtpsAction extends BaseAction { }

const getDistinctLtps = (distinctLtps: LtpIds[], selectedLtp: string, selectFirstLtp?: Function, resetLtp?: Function) => {
  let ltpNotSelected: boolean = true;
  // eslint-disable-next-line @typescript-eslint/no-unused-expressions
  selectFirstLtp && selectFirstLtp(distinctLtps[0].key);
  distinctLtps.forEach((value: LtpIds) => {
    if (value.key === selectedLtp) {
      ltpNotSelected = false;
    }
  });
  // eslint-disable-next-line @typescript-eslint/no-unused-expressions
  resetLtp && resetLtp(ltpNotSelected);
  return distinctLtps;
};

/** 
 * Represents an asynchronous thunk action to load available distinctLtps by networkElement from the database and set the returned first Ltp as default. 
 * @param networkElement The network element sent to database to get its available distinct Ltps.
 * @param selectedTimePeriod The time period selected sent to database to get the distinct Ltps of the selected network element.
 * @param selectedLtp The Ltp which is selected in the dropdown.
 * @param selectFirstLtp The function to get the first ltp returned from the database to be selected as default on selection upon network element.
 * @param resetLtp The function to verify if the selected ltp is also available in the selected time period database else reset the Ltp dropdown to select.
 */
export const loadDistinctLtpsbyNetworkElementAsync = (networkElement: string, selectedTimePeriod: string, selectedLtp: string, selectFirstLtp?: Function, resetLtp?: Function) => (dispatch: Dispatch) => {
  dispatch(new LoadAllAvailableLtpsAction());
  PerformanceHistoryService.getDistinctLtpsFromDatabase(networkElement, selectedTimePeriod).then(distinctLtps => {
    if (distinctLtps) {
      const ltps = getDistinctLtps(distinctLtps, selectedLtp, selectFirstLtp, resetLtp);
      dispatch(new AllAvailableLtpsLoadedAction(ltps));
    } else {
      if (resetLtp)
        resetLtp();
      dispatch(new NoLtpsFoundAction());
    }
  }).catch(error => {
    dispatch(new AllAvailableLtpsLoadedAction(null, error));
  });
};

