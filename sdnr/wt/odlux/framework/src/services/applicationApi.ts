import { Event } from '../common/event';
import { ApplicationStore } from '../store/applicationStore';

let resolveApplicationStoreInitialized: (store: ApplicationStore) => void;
let applicationStore: ApplicationStore | null = null;
const applicationStoreInitialized: Promise<ApplicationStore> = new Promise((resolve) => resolveApplicationStoreInitialized = resolve);

const loginEvent = new Event();
const logoutEvent = new Event();

export const onLogin = () => {
  loginEvent.invoke();
}

export const onLogout = () => {
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