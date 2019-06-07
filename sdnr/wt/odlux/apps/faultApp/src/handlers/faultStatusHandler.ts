import { IActionHandler } from "../../../../framework/src/flux/action";
import { SetFaultStatusAction } from "../actions/statusActions";

export interface IFaultStatus {
  critical: number,
  major: number,
  minor: number,
  warning: number
}

const faultStatusInit: IFaultStatus = {
  critical: 0,
  major: 0,
  minor: 0,
  warning: 0
};

export const faultStatusHandler: IActionHandler<IFaultStatus> = (state = faultStatusInit, action) => {
  if (action instanceof SetFaultStatusAction) {
    state = {
      critical: action.criticalFaults,
      major: action.majorFaults,
      minor: action.minorFaults,
      warning: action.warnings
    }
  }

  return state;
}