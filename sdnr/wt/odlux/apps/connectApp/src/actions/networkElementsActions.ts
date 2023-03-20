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

import { AddSnackbarNotification } from '../../../../framework/src/actions/snackbarActions';

import { NetworkElementConnection, ConnectionStatus, UpdateNetworkElement } from '../models/networkElementConnection';
import { connectService } from '../services/connectService';
import { updateCurrentViewAsyncAction } from './commonNetworkElementsActions';
import { unmountNetworkElementAsyncActionCreator } from './mountedNetworkElementsActions';

/** Represents the base action. */
export class BaseAction extends Action { }

/** Represents an async thunk action creator to add an element to the network elements/nodes. */
export const addNewNetworkElementAsyncActionCreator = (element: NetworkElementConnection) => async (dispatch: Dispatch) => {
  await connectService.createNetworkElement({ ...element });
  dispatch(updateCurrentViewAsyncAction());
  dispatch(new AddSnackbarNotification({ message: `Successfully added [${element.nodeId}]`, options: { variant: 'success' } }));
};

/** Represents an async thunk action creator to edit network element/node. */
export const editNetworkElementAsyncActionCreator = (element: UpdateNetworkElement) => async (dispatch: Dispatch) => {
  const connectionStatus: ConnectionStatus[] = (await connectService.getNetworkElementConnectionStatus(element.id).then(ne => (ne))) || [];
  const currentConnectionStatus = connectionStatus[0].status;
  if (currentConnectionStatus === 'Disconnected') {
    await connectService.deleteNetworkElement(element);
  } else {
    await connectService.updateNetworkElement(element);
  }
  dispatch(updateCurrentViewAsyncAction());
  dispatch(new AddSnackbarNotification({ message: `Successfully modified [${element.id}]`, options: { variant: 'success' } }));
};


/** Represents an async thunk action creator to delete an element from network elements/nodes. */
export const removeNetworkElementAsyncActionCreator = (element: UpdateNetworkElement) => async (dispatch: Dispatch) => {
  await connectService.deleteNetworkElement(element);
  await dispatch(unmountNetworkElementAsyncActionCreator(element && element.id));
  await dispatch(updateCurrentViewAsyncAction());
};



