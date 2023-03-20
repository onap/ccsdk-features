import { ApplicationStore } from "../store/applicationStore";

let applicationStore: ApplicationStore | null = null;

export const startSoreService = (store: ApplicationStore) => {
  applicationStore = store;
};

export const storeService = { 
  get applicationStore() { return applicationStore; },
 };