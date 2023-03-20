/**
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt odlux
 * =================================================================================================
 * Copyright (C) 2021 highstreet technologies GmbH Intellectual Property. All rights reserved.
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

import { TlsKeys } from '../models/networkElementConnection';
import { connectService } from '../services/connectService';

/**
 * Represents the base action.
 */
export class BaseAction extends Action { }

/**
 * Represents an action causing the store to load all TLS Keys.
 */
export class LoadAllTlsKeyListAction extends BaseAction { }

/**
 * Represents an action causing the store to get all TLS Keys.
 */
export class AllTlsKeyListLoadedAction extends BaseAction {
  /**
     * Initialize this instance.
     * 
     * @param gets all the tlsKey list from the  database.
     */
  constructor(public tlsList: TlsKeys[] | null, public error?: string) {
    super();
  }
}

/**
 * Represents an asynchronous thunk action to load all tlsKeys 
 */

export const loadAllTlsKeyListAsync = () => async (dispatch: Dispatch) => {
  dispatch(new LoadAllTlsKeyListAction());
  connectService.getTlsKeys().then(TlsKeyList => {
    dispatch(new AllTlsKeyListLoadedAction(TlsKeyList));
  }).catch(error => {
    dispatch(new AllTlsKeyListLoadedAction(null, error));
  });
};
