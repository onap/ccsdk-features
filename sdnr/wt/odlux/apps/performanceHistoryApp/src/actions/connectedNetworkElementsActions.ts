import { Action } from '../../../../framework/src/flux/action';
import { Dispatch } from '../../../../framework/src/flux/store';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';

import { ConnectedNetworkElementIds } from '../models/connectedNetworkElements';

/** 
 * Represents the base action. 
 */
export class BaseAction extends Action { }

/** 
 * Represents an action causing the store to load all connected network element Ids. 
 */
export class LoadAllConnectedNetworkElementsAction extends BaseAction { }

/** 
 * Represents an action causing the store to update all connected network element Ids. 
 */
export class AllConnectedNetworkElementsLoadedAction extends BaseAction {
  /**
   * Initialize this instance.
   * 
   * @param connectedNetworkElements The connected network element Ids which are loaded from the state of connectApp.
   */
  constructor(public connectedNetworkElementIds: ConnectedNetworkElementIds[] | null, public error?: string) {
    super();
  }
}

/** 
 * Represents an asynchronous thunk  action to load all connected network element Ids. 
 */
export const loadAllConnectedNetworkElementsAsync = (dispatch: Dispatch, getState: () => IApplicationStoreState) => {
    dispatch(new LoadAllConnectedNetworkElementsAction());
    const connectedNetworkElementsIds = getState().connect.mountedNetworkElements;
    let mountIdList: ConnectedNetworkElementIds[] = [];
    connectedNetworkElementsIds.elements.forEach(element => {
      const connectedNetworkElement = {
        mountId: element.mountId
      }
      mountIdList.push(connectedNetworkElement);
    });
    mountIdList.sort((a, b) => {
      if (a.mountId < b.mountId) return -1;
      if (a.mountId > b.mountId) return 1;
      return 0;
    });
    dispatch(new AllConnectedNetworkElementsLoadedAction(mountIdList));
};