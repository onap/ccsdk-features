import { IActionHandler } from '../../../../framework/src/flux/action';

import { SetPanelAction } from '../actions/panelActions';
import { PanelId } from '../models/panelId';

export const currentOpenPanelHandler: IActionHandler<PanelId> = (state = null, action) => {
  if (action instanceof SetPanelAction) {
    state = action.panelId;
  }
  return state;
};