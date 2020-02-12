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
import * as X2JS from 'x2js';
import { ApplicationStore } from '../store/applicationStore';
import { SetWebsocketAction } from '../actions/websocketAction';

const socketUrl = [location.protocol === 'https:' ? 'wss://' : 'ws://', 'admin', ':', 'admin', '@', location.hostname, ':', location.port, '/websocket'].join('');
const subscriptions: { [scope: string]: SubscriptionCallback[] } = {};
let socketReady: Promise<WebSocket>;
let userLoggedOut = false;
let wasWebsocketConnectionEstablished: undefined | boolean;
let applicationStore: ApplicationStore | null;


export interface IFormatedMessage {
  notifType: string | null;
  time: string;
}

export type SubscriptionCallback<TMessage extends IFormatedMessage = IFormatedMessage> = (msg: TMessage) => void;

function formatData(event: MessageEvent): IFormatedMessage | undefined {

  var x2js = new X2JS();
  var jsonObj: { [key: string]: IFormatedMessage } = x2js.xml2js(event.data);
  if (jsonObj && typeof (jsonObj) === 'object') {

    const notifType = Object.keys(jsonObj)[0];
    const formated = jsonObj[notifType];
    formated.notifType = notifType;
    formated.time = new Date().toISOString();
    return formated;
  }
  return undefined;

}

export function subscribe<TMessage extends IFormatedMessage = IFormatedMessage>(scope: string | string[], callback: SubscriptionCallback<TMessage>): boolean {
  const scopes = scope instanceof Array ? scope : [scope];

  // send all new scopes to subscribe
  const newScopesToSubscribe: string[] = scopes.reduce((acc: string[], cur: string) => {
    const currentCallbacks = subscriptions[cur];
    if (currentCallbacks) {
      if (!currentCallbacks.some(c => c === callback)) {
        currentCallbacks.push(callback);
      }
    } else {
      subscriptions[cur] = [callback];
      acc.push(cur);
    }
    return acc;
  }, []);

  if (newScopesToSubscribe.length === 0) {
    return true;
  }

  return true;
}


export function unsubscribe<TMessage extends IFormatedMessage = IFormatedMessage>(scope: string | string[], callback: SubscriptionCallback<TMessage>): Promise<boolean> {
  return socketReady.then((notificationSocket) => {
    const scopes = scope instanceof Array ? scope : [scope];
    scopes.forEach(s => {
      const callbacks = subscriptions[s];
      const index = callbacks && callbacks.indexOf(callback);
      if (index > -1) {
        callbacks.splice(index, 1);
      }
      if (callbacks.length === 0) {
        subscriptions[s] === undefined;
      }
    });

    // send a subscription to all active scopes
    const scopesToSubscribe = Object.keys(subscriptions);
    if (notificationSocket.readyState === notificationSocket.OPEN) {
      const data = {
        'data': 'scopes',
        'scopes': scopesToSubscribe
      };
      notificationSocket.send(JSON.stringify(data));
      return true;
    }
    return false;
  });
}

export const startNotificationService = (store: ApplicationStore) => {
  applicationStore = store;
}

const connect = (): Promise<WebSocket> => {
  return new Promise((resolve, reject) => {
    const notificationSocket = new WebSocket(socketUrl);

    notificationSocket.onmessage = (event) => {
      // process received event
      if (typeof event.data === 'string') {
        const formated = formatData(event);
        if (formated && formated.notifType) {
          const callbacks = subscriptions[formated.notifType];
          if (callbacks) {
            callbacks.forEach(cb => {
              // ensure all callbacks will be called
              try {
                return cb(formated);
              } catch (reason) {
                console.error(reason);
              }
            });
          }
        }
      }
    };

    notificationSocket.onerror = function (error) {
      console.log("Socket error:");
      console.log(error);
      reject("Socket error: " + error);
      if (applicationStore) {
        applicationStore.dispatch(new SetWebsocketAction(false));
      }
    };

    notificationSocket.onopen = function (event) {
      if (applicationStore) {
        applicationStore.dispatch(new SetWebsocketAction(true));
      }
      console.log("Socket connection opened.");
      resolve(notificationSocket);

      // send a subscription to all active scopes
      const scopesToSubscribe = Object.keys(subscriptions);
      if (notificationSocket.readyState === notificationSocket.OPEN) {
        const data = {
          'data': 'scopes',
          'scopes': scopesToSubscribe
        };
        notificationSocket.send(JSON.stringify(data));
      };
    };

    notificationSocket.onclose = function (event) {
      console.log("socket connection closed");
      if (applicationStore) {
        applicationStore.dispatch(new SetWebsocketAction(false));
      }
      if (!userLoggedOut) {
        socketReady = connect();
      }
    };
  });
}




export const startWebsocketSession = () => {
  socketReady = connect();
  userLoggedOut = false;
}

export const endWebsocketSession = () => {
  if (socketReady) {
    socketReady.then(websocket => {
      websocket.close();
      userLoggedOut = true;
    });
  }

}




