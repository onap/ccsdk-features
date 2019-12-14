import { IActionHandler } from "../../../../framework/src/flux/action";

import { UpdatViewDescription } from "../actions/deviceActions";
import { ViewSpecification } from "../models/uiModels";

export interface IViewDescriptionState {
  vPath: string | null;
  keyProperty: string | undefined;
  displayAsList: boolean;
  viewSpecification: ViewSpecification;
  viewData: any
}

const viewDescriptionStateInit: IViewDescriptionState = {
  vPath: null,
  keyProperty: undefined,
  displayAsList: false,
  viewSpecification: {
    id: "empty",
    canEdit: false,
    parentView: "",
    name: "emplty",
    language: "en-US",
    title: "empty",
    elements: {}
  },
  viewData: null
};

export const viewDescriptionHandler: IActionHandler<IViewDescriptionState> = (state = viewDescriptionStateInit, action) => {
  if (action instanceof UpdatViewDescription) {
    state = {
      ...state,
      vPath: action.vPath,
      keyProperty: action.key,
      displayAsList: action.displayAsList,
      viewSpecification: action.view,
      viewData: action.viewData,
    }
  }
  return state;
};
