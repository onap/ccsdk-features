import { IActionHandler } from '../../../../framework/src/flux/action';

import { AllConnectedNetworkElementsLoadedAction, LoadAllConnectedNetworkElementsAction } from '../actions/connectedNetworkElementsActions';
import { ConnectedNetworkElementIds } from '../models/connectedNetworkElements';

export interface IConnectedNetworkElementsState {
  connectedNetworkElementIds: ConnectedNetworkElementIds[];
  busy: boolean;
}

const connectedNetworkElementsStateInit: IConnectedNetworkElementsState = {
  connectedNetworkElementIds: [],
  busy: false
};

export const connectedNetworkElementsActionHandler: IActionHandler<IConnectedNetworkElementsState> = (state = connectedNetworkElementsStateInit, action) => {
  if (action instanceof LoadAllConnectedNetworkElementsAction) {

    state = {
      ...state,
      busy: true
    };

  } else if (action instanceof AllConnectedNetworkElementsLoadedAction) {
    if (!action.error && action.connectedNetworkElementIds) {
      state = {
        ...state,
        connectedNetworkElementIds: action.connectedNetworkElementIds,
        busy: false
      };
    } else {
      state = {
        ...state,
        busy: false
      };
    }
  }
  return state;
};