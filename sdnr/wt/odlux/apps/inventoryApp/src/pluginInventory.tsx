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

import { Dashboard } from './views/dashboard';
import inventoryAppRootHandler from './handlers/inventoryAppRootHandler';

import { createInventoryElementsProperties, createInventoryElementsActions, inventoryElementsReloadAction } from "./handlers/inventoryElementsHandler";

let currentMountId: string | undefined = undefined;

const mapProps = (state: IApplicationStoreState) => ({
  inventoryProperties: createInventoryElementsProperties(state),
});

const mapDisp = (dispatcher: IDispatcher) => ({
  inventoryActions: createInventoryElementsActions(dispatcher.dispatch, true)
});

const InventoryApplicationRouteAdapter = connect(mapProps, mapDisp)((props: RouteComponentProps<{ mountId?: string }> & Connect<typeof mapProps, typeof mapDisp>) => {
  if (currentMountId !== props.match.params.mountId) {
    currentMountId = props.match.params.mountId || undefined;
    window.setTimeout(() => {
      if (currentMountId) {
        props.inventoryActions.onFilterChanged("nodeId", currentMountId);
        props.inventoryProperties.showFilter;
        props.inventoryActions.onRefresh();
      }
    });
  }
  return (
    <Dashboard />
  )
});

const App = withRouter((props: RouteComponentProps) => (
  <Switch>
    <Route path={`${props.match.path}/:mountId?`} component={InventoryApplicationRouteAdapter} />
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

