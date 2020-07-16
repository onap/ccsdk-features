import { PanelId } from "../models/panelId";
import { IActionHandler } from "../../../../framework/src/flux/action";
import { SetPanelAction } from "../actions/panelActions";


export const currentOpenPanelHandler: IActionHandler<PanelId> = (state = null, action) => {
    if (action instanceof SetPanelAction) {
      state = action.panelId;
    }
    return state;
  }