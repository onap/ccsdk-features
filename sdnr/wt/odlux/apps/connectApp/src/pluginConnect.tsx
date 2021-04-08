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

import { faPlug } from '@fortawesome/free-solid-svg-icons';

import applicationManager from '../../../framework/src/services/applicationManager';
import { subscribe, IFormatedMessage } from '../../../framework/src/services/notificationService';
import { AddSnackbarNotification } from '../../../framework/src/actions/snackbarActions';

import connectAppRootHandler from './handlers/connectAppRootHandler';
import ConnectApplication from './views/connectView';

import { findWebUrisForGuiCutThroughAsyncAction, updateCurrentViewAsyncAction } from './actions/commonNetworkElementsActions';

export function register() {
  const applicationApi = applicationManager.registerApplication({
    name: "connect",
    icon: faPlug,
    rootComponent: ConnectApplication,
    rootActionHandler: connectAppRootHandler,
    menuEntry: "Connect"
  });

  // subscribe to the websocket notifications
  subscribe<IFormatedMessage>(["object-creation-notification", "object-deletion-notification", "attribute-value-changed-notification"], (msg => {
    const store = applicationApi.applicationStore;
    if (msg && msg.type.type === "object-creation-notification" && store) {
      store.dispatch(new AddSnackbarNotification({ message: `Adding network element [${msg['node-id']}]`, options: { variant: 'info' } }));
    } else if (msg && (msg.type.type === "object-deletion-notification" || msg.type.type === "attribute-value-changed-notification") && store) {
      store.dispatch(new AddSnackbarNotification({ message: `Updating network element [${msg['node-id']}]`, options: { variant: 'info' } }));
    }
    if (store) {
      store.dispatch(updateCurrentViewAsyncAction() as any).then(() => {
        if (msg['node-id']) {
          store.dispatch(findWebUrisForGuiCutThroughAsyncAction([msg['node-id']]));
        }
      });
    }
  }));
}