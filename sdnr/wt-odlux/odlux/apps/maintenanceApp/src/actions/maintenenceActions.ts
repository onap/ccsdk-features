/* eslint-disable @typescript-eslint/no-unused-expressions */
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
import { AddSnackbarNotification } from '../../../../framework/src/actions/snackbarActions';
import { Action } from '../../../../framework/src/flux/action';
import { Dispatch } from '../../../../framework/src/flux/store';

import { maintenanceEntriesReloadAction } from '../handlers/maintenanceEntriesHandler';
import { MaintenanceEntry, spoofSymbol } from '../models/maintenanceEntryType';
import { maintenenceService } from '../services/maintenenceService';

export class BaseAction extends Action { }

export class LoadAllMainteneceEntriesAction extends BaseAction { }

export class AllMainteneceEntriesLoadedAction extends BaseAction {

  constructor(public maintenenceEntries: MaintenanceEntry[] | null) {
    super();

  }
}


export class UpdateMaintenanceEntry extends BaseAction {
  constructor(public maintenenceEntry: MaintenanceEntry) {
    super();
  }
}

/** Represents an async thunk action creator to add an element to the maintenence entries. */
export const addOrUpdateMaintenenceEntryAsyncActionCreator = (entry: MaintenanceEntry) => (dispatch: Dispatch) => {
  maintenenceService.writeMaintenenceEntry(entry).then(result => {
    result && window.setTimeout(() => {
      // dispatch(loadAllMountedNetworkElementsAsync);
      dispatch(new UpdateMaintenanceEntry(entry));
      dispatch(new AddSnackbarNotification({ message: `Successfully ${result && result.created ? 'created' : 'updated'} maintenance settings for [${entry.nodeId}]`, options: { variant: 'success' } }));
    }, 900);
    dispatch(maintenanceEntriesReloadAction);
  });
};

/** Represents an async thunk action creator to delete an element from the maintenence entries. */
export const removeFromMaintenenceEntrysAsyncActionCreator = (entry: MaintenanceEntry) => (dispatch: Dispatch) => {
  maintenenceService.deleteMaintenenceEntry(entry).then(result => {
    result && window.setTimeout(() => {
      dispatch(new UpdateMaintenanceEntry({
        [spoofSymbol]: true,
        mId: entry.mId,
        nodeId: entry.nodeId,
        description: '',
        start: '',
        end: '',
        active: false,
      }));
      dispatch(new AddSnackbarNotification({ message: `Successfully removed [${entry.nodeId}]`, options: { variant: 'success' } }));
    }, 900);
    dispatch(maintenanceEntriesReloadAction);
  });
};

// Hint: since there is no notification of changed required network elements, this code is not aware of changes caused outiside of this browser.