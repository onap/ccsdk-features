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
import { ApplicationStore } from '../store/applicationStore';
import { SetWebsocketAction } from '../actions/websocketAction';

const socketUrl = [location.protocol === 'https:' ? 'wss://' : 'ws://', location.hostname, ':', location.port, '/websocket'].join('');
const subscriptions: { [scope: string]: SubscriptionCallback[] } = {};
let socketReady: Promise<WebSocket>;
let wasWebsocketConnectionEstablished: undefined | boolean;
let applicationStore: ApplicationStore | null;

let areWebsocketsStoppedViaSettings = false;


export interface IFormatedMessage {
    "event-time": string,
    "data": {
        "counter": number,
        "attribute-name": string,
        "time-stamp": string,
        "object-id-ref": string,
        "new-value": string
    },
    "node-id": string,
    "type": {
        "namespace": string,
        "revision": string,
        "type": string
    }
}

export type SubscriptionCallback<TMessage extends IFormatedMessage = IFormatedMessage> = (msg: TMessage) => void;

function setCurrentSubscriptions(notificationSocket: WebSocket) {
  const scopesToSubscribe = Object.keys(subscriptions);
  if (notificationSocket.readyState === notificationSocket.OPEN) {
    const data = {
      'data': 'scopes',
      'scopes':[{
        "schema":{
            "namespace":"urn:opendaylight:params:xml:ns:yang:devicemanager",
            "revision":"*",
            "notification": scopesToSubscribe 
         }
      }]
    };
    notificationSocket.send(JSON.stringify(data));
    return true;
  };
  return false;
}

function addScope<TMessage extends IFormatedMessage = IFormatedMessage>(scope: string | string[], callback: SubscriptionCallback<TMessage>) {
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
    return false;
}

function removeScope<TMessage extends IFormatedMessage = IFormatedMessage>(scope: string | string[], callback: SubscriptionCallback<TMessage>) {
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
}

export function subscribe<TMessage extends IFormatedMessage = IFormatedMessage>(scope: string | string[], callback: SubscriptionCallback<TMessage>): Promise<boolean> {
  addScope(scope, callback)
  return socketReady && socketReady.then((notificationSocket) => {
    // send a subscription to all active scopes
    return setCurrentSubscriptions(notificationSocket);
  }) || true;
}

export function unsubscribe<TMessage extends IFormatedMessage = IFormatedMessage>(scope: string | string[], callback: SubscriptionCallback<TMessage>): Promise<boolean> {
  removeScope(scope, callback);
  return socketReady && socketReady.then((notificationSocket) => {
    // send a subscription to all active scopes
    return setCurrentSubscriptions(notificationSocket);
  }) || true;
}

export const startNotificationService = (store: ApplicationStore) => {
  applicationStore = store;
}

const connect = (): Promise<WebSocket> => {
  return new Promise((resolve, reject) => {
    const notificationSocket = new WebSocket(socketUrl);

    notificationSocket.onmessage = (event: MessageEvent<string>) => {
      // process received event
      
        if (event.data && typeof event.data === "string" ) {
          const msg = JSON.parse(event.data) as IFormatedMessage;
          const callbacks = msg?.type?.type && subscriptions[msg.type.type];
          if (callbacks) {
            callbacks.forEach(cb => {
              // ensure all callbacks will be called
              try {
                return cb(msg);
              } catch (reason) {
                console.error(reason);
              }
            });
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
      setCurrentSubscriptions(notificationSocket);
    };

    notificationSocket.onclose = function (event) {
      console.log("socket connection closed");
      dispatchSocketClose();

      const isUserLoggedIn = applicationStore?.state.framework.authenticationState.user && applicationStore?.state.framework.authenticationState.user?.isValid;

      if (isUserLoggedIn && !areWebsocketsStoppedViaSettings) {
        socketReady = connect();
      }
    };
  });
}


export const startWebsocketSession = () => {
  socketReady = connect();
  areWebsocketsStoppedViaSettings = false;
}

export const suspendWebsocketSession = () =>{
  areWebsocketsStoppedViaSettings = true;
  closeSocket();
}

export const endWebsocketSession = () => {
  closeSocket();
}

const closeSocket = () =>{
  
  if (socketReady) {
    socketReady.then(websocket => {
      websocket.close();
    });
  }else{
    dispatchSocketClose();
  }
}

const dispatchSocketClose = () =>{
  const isUserLoggedIn = applicationStore?.state.framework.authenticationState.user && applicationStore?.state.framework.authenticationState.user?.isValid;

  if(isUserLoggedIn){
    applicationStore?.dispatch(new SetWebsocketAction(false));
  }else{
    applicationStore?.dispatch(new SetWebsocketAction(null));
  }
}




