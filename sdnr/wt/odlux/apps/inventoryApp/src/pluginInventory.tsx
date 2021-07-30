/**
* ============LICENSE_START========================================================================
* ONAP : ccsdk feature sdnr wt odlux
* =================================================================================================
* Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
* =================================================================================================
* Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
* in compliance with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software distributed under the License
* is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
* or implied. See the License for the specific language governing permissions and limitations under
* the License.
* ============LICENSE_END==========================================================================
*/
// app configuration and main entry point for the app

import * as React from "react";
import { withRouter, RouteComponentProps, Route, Switch, Redirect } from 'react-router-dom';
import { faShoppingBag } from '@fortawesome/free-solid-svg-icons'; // select app icon
import applicationManager from '../../../framework/src/services/applicationManager';
import connect, { Connect, IDispatcher } from '../../../framework/src/flux/connect';
import { IApplicationStoreState } from "../../../framework/src/store/applicationStore";

import { InventoryTreeView } from './views/treeview';
import Dashboard from './views/dashboard';

import { PanelId } from "./models/panelId";
import { SetPanelAction } from "./actions/panelActions";

import inventoryAppRootHandler from './handlers/inventoryAppRootHandler';
import { createInventoryElementsActions, createInventoryElementsProperties } from "./handlers/inventoryElementsHandler";
import { createConnectedNetworkElementsProperties, createConnectedNetworkElementsActions } from "./handlers/connectedNetworkElementsHandler";

let currentMountId: string | undefined = undefined;
const mapProps = (state: IApplicationStoreState) => ({
  inventoryProperties: createInventoryElementsProperties(state),
  panelId: state.inventory.currentOpenPanel,
  connectedNetworkElementsProperties: createConnectedNetworkElementsProperties(state),
});

const mapDisp = (dispatcher: IDispatcher) => ({
  inventoryActions: createInventoryElementsActions(dispatcher.dispatch, true),
  connectedNetworkElementsActions: createConnectedNetworkElementsActions(dispatcher.dispatch, true),
  setCurrentPanel: (panelId: PanelId) => dispatcher.dispatch(new SetPanelAction(panelId)),
});

const InventoryTableApplicationRouteAdapter = connect(mapProps, mapDisp)((props: RouteComponentProps<{ mountId?: string }> & Connect<typeof mapProps, typeof mapDisp>) => {
  if (currentMountId !== props.match.params.mountId) {
    // route parameter has changed
    currentMountId = props.match.params.mountId || undefined;
    // Hint: This timeout is needed, since it is not recommended to change the state while rendering is in progress !
    window.setTimeout(() => {
      if (currentMountId) {
        if (props.panelId) {
          props.setCurrentPanel(props.panelId);
        }
        else {
          props.setCurrentPanel("InventoryElementsTable");
        }
        props.inventoryActions.onFilterChanged("nodeId", currentMountId);
        props.connectedNetworkElementsActions.onFilterChanged("nodeId", currentMountId);
        if (!props.inventoryProperties.showFilter) {
          props.inventoryActions.onToggleFilter(false);
        }
        if (!props.connectedNetworkElementsProperties.showFilter) {
          props.connectedNetworkElementsActions.onToggleFilter(false);
        }
        props.inventoryActions.onRefresh();
        props.connectedNetworkElementsActions.onRefresh();
      }
    });
  }
  return (
    <Dashboard />
  )
});

const App = withRouter((props: RouteComponentProps) => (
  <Switch>
    <Route path={`${props.match.path}/dashboard/:mountId`} component={InventoryTableApplicationRouteAdapter} />
    <Route path={`${props.match.path}/:mountId`} component={InventoryTreeView} />
    <Route path={`${props.match.path}`} component={Dashboard} />
    <Redirect to={`${props.match.path}`} />
  </Switch>
));

export function register() {
  applicationManager.registerApplication({
    name: "inventory",
    icon: faShoppingBag,
    rootActionHandler: inventoryAppRootHandler,
    rootComponent: App,
    menuEntry: "Inventory"
  });
}

