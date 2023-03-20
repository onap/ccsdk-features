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

import { setGeneralSettingsAction } from "../actions/settingsAction";
import { loginUserAction, logoutUser } from "../actions/authentication";
import { ReplaceAction } from "../actions/navigationActions";
import { User } from "../models/authentication";
import { ApplicationStore } from "../store/applicationStore";

type Broadcaster = {
  channel: BroadcastChannel;
  key: String;
};

type AuthTypes = 'login' | 'logout';
export type AuthMessage = {
  key: AuthTypes;
  data: any;
};

type SettingsType = 'general';
export type SettingsMessage = {
  key: SettingsType;
  enableNotifications: boolean;
  user: string;
};

const channels: Broadcaster[] = [];
let store: ApplicationStore | null = null;

export const saveChannel = (channel: BroadcastChannel, channelName: string) => {
  channels.push({ channel: channel, key: channelName });
};

export const startBroadcastChannel = (applicationStore: ApplicationStore) => {
  store = applicationStore;

  //might decide to use one general broadcast channel with more keys in the future
  createAuthBroadcastChannel();
  createSettingsBroadcastChannel();
};

const createSettingsBroadcastChannel = () => {

  const name = "odlux_settings";
  const bc: BroadcastChannel = new BroadcastChannel(name);
  channels.push({ channel: bc, key: name });

  bc.onmessage = (eventMessage: MessageEvent<SettingsMessage>) => {
    console.log(eventMessage);

    if (eventMessage.data.key === 'general') {

      if (store?.state.framework.authenticationState.user) {
        const data = eventMessage.data;
        if (store.state.framework.authenticationState.user.user === data.user) {
          store?.dispatch(setGeneralSettingsAction(data.enableNotifications));
        }
      }
    }
  }
};

const createAuthBroadcastChannel = () => {
  const name = "odlux_auth";
  const bc: BroadcastChannel = new BroadcastChannel(name);
  channels.push({ channel: bc, key: name });

  bc.onmessage = (eventMessage: MessageEvent<AuthMessage>) => {
    console.log(eventMessage)

    if (eventMessage.data.key === 'login') {
      if (!store?.state.framework.authenticationState.user) {
        const initialToken = localStorage.getItem("userToken");
        if (initialToken) {
          store?.dispatch(loginUserAction(User.fromString(initialToken)));
          store?.dispatch(new ReplaceAction("/"));
        }
      }
    }
    else if (eventMessage.data.key === 'logout') {

      if (store?.state.framework.authenticationState.user) {
        store?.dispatch(logoutUser());
        store?.dispatch(new ReplaceAction("/login"));
      }
    }
  }
};

export const getBroadcastChannel = (channelName: string) => {
  const foundChannel = channels.find(s => s.key === channelName);
  return foundChannel?.channel;
};

export const sendMessage = (data: any, channel: string) => {
  const foundChannel = channels.find(s => s.key === channel);
  if (foundChannel) {
    foundChannel.channel.postMessage(data);
  }
};
