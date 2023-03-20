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

import { Event } from '../common/event';
import { ApplicationStore } from '../store/applicationStore';
import { AuthMessage, getBroadcastChannel, sendMessage } from './broadcastService';

let resolveApplicationStoreInitialized: (store: ApplicationStore) => void;
let applicationStore: ApplicationStore | null = null;
const applicationStoreInitialized: Promise<ApplicationStore> = new Promise((resolve) => resolveApplicationStoreInitialized = resolve);

const loginEvent = new Event();
const logoutEvent = new Event();
let channel : BroadcastChannel | undefined;
const authChannelName = "odlux_auth";

export const onLogin = () => {

  const message : AuthMessage = {key: 'login', data: {}}
  sendMessage(message, authChannelName);
  loginEvent.invoke();

}

export const onLogout = () => {
  
  document.cookie = "JSESSIONID=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";

  const message : AuthMessage = {key: 'logout', data: {}}
  sendMessage(message, authChannelName);
  logoutEvent.invoke();
}

export const setApplicationStore = (store: ApplicationStore) => {
  if (!applicationStore && store) {
    applicationStore = store;
    resolveApplicationStoreInitialized(store);
  }
}

export const applicationApi = {
  get applicationStore(): ApplicationStore | null {
    return applicationStore;
  },

  get applicationStoreInitialized(): Promise<ApplicationStore> {
    return applicationStoreInitialized;
  },

  get loginEvent() {
    return loginEvent;
  },

  get logoutEvent() {
    return logoutEvent;
  }
};

export default applicationApi;