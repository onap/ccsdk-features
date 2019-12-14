import { combineActionHandler } from '../../../../framework/src/flux/middleware';

import { IConnectedNetworkElementsState, connectedNetworkElementsActionHandler } from './connectedNetworkElementsHandler';
import { IDeviceDescriptionState, deviceDescriptionHandler } from "./deviceDescriptionHandler";
import { IViewDescriptionState, viewDescriptionHandler } from "./viewDescriptionHandler";
import { IValueSelectorState, valueSelectorHandler } from "./valueSelectorHandler";

interface IConfigurationAppStoreState {
  connectedNetworkElements: IConnectedNetworkElementsState; // used for ne selection
  deviceDescription: IDeviceDescriptionState;               // contains ui and device descriptions
  viewDescription: IViewDescriptionState;                   // contains current ui description
  valueSelector: IValueSelectorState;
}

declare module '../../../../framework/src/store/applicationStore' {
  interface IApplicationStoreState {
    configuration: IConfigurationAppStoreState,
  }
}

const actionHandlers = {
  connectedNetworkElements: connectedNetworkElementsActionHandler,
  deviceDescription: deviceDescriptionHandler,
  viewDescription: viewDescriptionHandler,
  valueSelector: valueSelectorHandler,
};

export const configurationAppRootHandler = combineActionHandler<IConfigurationAppStoreState>(actionHandlers);
export default configurationAppRootHandler;
