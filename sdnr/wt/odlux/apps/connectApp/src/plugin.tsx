
import { faPlug } from '@fortawesome/free-solid-svg-icons';

import applicationManager from '../../../framework/src/services/applicationManager';
import { subscribe, IFormatedMessage } from '../../../framework/src/services/notificationService';

import connectAppRootHandler from './handlers/connectAppRootHandler';
import ConnectApplication  from './views/connectView';

import {
  addMountedNetworkElementAsyncActionCreator,
  updateMountedNetworkElementAsyncActionCreator,
  loadAllMountedNetworkElementsAsync
} from './actions/mountedNetworkElementsActions';

import { AddSnackbarNotification } from '../../../framework/src/actions/snackbarActions';

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

  applicationApi.applicationStoreInitialized.then(applicationStore => { applicationStore.dispatch(loadAllMountedNetworkElementsAsync); });
  // subscribe to the websocket notifications
  subscribe<ObjectNotification & IFormatedMessage>(["ObjectCreationNotification", "ObjectDeletionNotification", "AttributeValueChangedNotification"], (msg => {
    const store = applicationApi.applicationStore;
    if (msg && msg.notifType === "ObjectCreationNotification" && store) {
      store.dispatch(addMountedNetworkElementAsyncActionCreator(msg.objectId));
      store.dispatch(new AddSnackbarNotification({ message: `Adding network element [${msg.objectId}]`, options: { variant: 'info' } }));
    } else if (msg && (msg.notifType === "ObjectDeletionNotification" || msg.notifType === "AttributeValueChangedNotification") && store) {
      store.dispatch(new AddSnackbarNotification({ message: `Updating network element [${msg.objectId}]`, options: { variant: 'info' } }));
      store.dispatch(updateMountedNetworkElementAsyncActionCreator(msg.objectId));
    }
  }));
}