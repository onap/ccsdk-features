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

import connectAppRootHandler from './handlers/connectAppRootHandler';
import ConnectApplication from './views/connectView';

import { AddSnackbarNotification } from '../../../framework/src/actions/snackbarActions';
import { updateCurrentViewAsyncAction } from './actions/commonNetworkElementsActions';

type ObjectNotification = {
  counter: string;
  nodeName: string;
  objectId: string;
  timeStamp: string;
}

export function register() {
  const applicationApi = applicationManager.registerApplication({
    name: "connect",
    icon: faPlug,
    rootComponent: ConnectApplication,
    rootActionHandler: connectAppRootHandler,
    menuEntry: "Connect"
  });

  // subscribe to the websocket notifications
  subscribe<ObjectNotification & IFormatedMessage>(["ObjectCreationNotification", "ObjectDeletionNotification", "AttributeValueChangedNotification"], (msg => {
    const store = applicationApi.applicationStore;
    if (msg && msg.notifType === "ObjectCreationNotification" && store) {
      store.dispatch(new AddSnackbarNotification({ message: `Adding network element [${msg.objectId}]`, options: { variant: 'info' } }));
    } else if (msg && (msg.notifType === "ObjectDeletionNotification" || msg.notifType === "AttributeValueChangedNotification") && store) {
      store.dispatch(new AddSnackbarNotification({ message: `Updating network element [${msg.objectId}]`, options: { variant: 'info' } }));
    }
    store && store.dispatch(updateCurrentViewAsyncAction());
  }));
}