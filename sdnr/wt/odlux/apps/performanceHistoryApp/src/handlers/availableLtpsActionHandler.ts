import { IActionHandler } from '../../../../framework/src/flux/action';

import {
  AllAvailableLtpsLoadedAction,
  LoadAllAvailableLtpsAction,
} from '../actions/ltpAction';

import { LtpIds } from '../models/availableLtps';

export interface IAvailableLtpsState {
  distinctLtps: LtpIds[];
  busy: boolean;
}

const connectedNetworkElementsStateInit: IAvailableLtpsState = {
    distinctLtps: [],
  busy: false
};

export const availableLtpsActionHandler: IActionHandler<IAvailableLtpsState> = (state = connectedNetworkElementsStateInit, action) => {
  if (action instanceof LoadAllAvailableLtpsAction) {

    state = {
      ...state,
      busy: true
    };

  } else if (action instanceof AllAvailableLtpsLoadedAction) {
    if (!action.error && action.availableLtps) {
      state = {
        ...state,
        distinctLtps: action.availableLtps,
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