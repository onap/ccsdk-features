import { Module } from "../models/yang";
import { ViewSpecification } from "../models/uiModels";
import { IActionHandler } from "../../../../framework/src/flux/action";
import { UpdateDeviceDescription } from "../actions/deviceActions";

export interface IDeviceDescriptionState {
  nodeId: string,
  modules: {
    [name: string]: Module
  },
  views: ViewSpecification[],
}

const deviceDescriptionStateInit: IDeviceDescriptionState = {
  nodeId: "",
  modules: {},
  views: []
};

export const deviceDescriptionHandler: IActionHandler<IDeviceDescriptionState> = (state = deviceDescriptionStateInit, action) => {
  if (action instanceof UpdateDeviceDescription) {
    state = {
      ...state,
      nodeId: action.nodeId,
      modules: action.modules,
      views: action.views
    };
  } 
  return state;
};
