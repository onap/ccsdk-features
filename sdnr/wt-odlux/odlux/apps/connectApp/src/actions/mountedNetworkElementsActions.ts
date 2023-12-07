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

import { connectService } from '../services/connectService';
import { NetworkElementConnection } from '../models/networkElementConnection';
import { AddSnackbarNotification } from '../../../../framework/src/actions/snackbarActions';
import { updateCurrentViewAsyncAction } from './commonNetworkElementsActions';

/** Represents the base action. */
export class BaseAction extends Action { }

/** Represents an action creator for a async thunk action to mount a network element/node. */
export const mountNetworkElementAsyncActionCreator = (networkElement: NetworkElementConnection) => (dispatch: Dispatch) => {
  return connectService.mountNetworkElement(networkElement).then((success) => {
    if (success) {
      dispatch(updateCurrentViewAsyncAction());
      dispatch(new AddSnackbarNotification({ message: `Requesting mount [${networkElement.nodeId}]`, options: { variant: 'info' } }));
    } else {
      dispatch(new AddSnackbarNotification({ message: `Failed to mount [${networkElement.nodeId}]`, options: { variant: 'warning' } }));
    }
  }).catch(error => {
    dispatch(new AddSnackbarNotification({ message: `Failed to mount [${networkElement.nodeId}]`, options: { variant: 'error' } }));
    console.error(error);
  });
};

/** Represents an action creator for a async thunk action to unmount a network element/node. */
export const unmountNetworkElementAsyncActionCreator = (nodeId: string) => (dispatch: Dispatch) => {
  return connectService.unmountNetworkElement(nodeId).then((success) => {
    if (success) {
      dispatch(updateCurrentViewAsyncAction());
      dispatch(new AddSnackbarNotification({ message: `Requesting unmount [${nodeId}]`, options: { variant: 'info' } }));
    } else {
      dispatch(new AddSnackbarNotification({ message: `Failed to unmount [${nodeId}]`, options: { variant: 'warning' } }));
    }
  }).catch(error => {
    dispatch(new AddSnackbarNotification({ message: `Failed to unmount [${nodeId}]`, options: { variant: 'error' } }));
    console.error(error);
  });
};


