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

import React from 'react';
import { Redirect, Route, RouteComponentProps, Switch, withRouter } from 'react-router-dom';

import { AddSnackbarNotification } from '../../../framework/src/actions/snackbarActions';
import { connect, Connect, IDispatcher } from '../../../framework/src/flux/connect';
import applicationManager from '../../../framework/src/services/applicationManager';
import { IFormatedMessage, subscribe } from '../../../framework/src/services/notificationService';
import { IApplicationStoreState } from '../../../framework/src/store/applicationStore';

import { findWebUrisForGuiCutThroughAsyncAction, SetPanelAction, updateCurrentViewAsyncAction } from './actions/commonNetworkElementsActions';
import { NetworkElementsList } from './components/networkElements';
import connectAppRootHandler from './handlers/connectAppRootHandler';
import { createNetworkElementsActions, createNetworkElementsProperties, networkElementsReloadAction } from './handlers/networkElementsHandler';
import { PanelId } from './models/panelId';
import ConnectApplication from './views/connectView';

const appIcon = require('./assets/icons/connectAppIcon.svg');  // select app icon

let currentStatus: string | undefined = undefined;

const mapProps = (state: IApplicationStoreState) => ({
  networkElementDashboardProperties: createNetworkElementsProperties(state),
});

const mapDisp = (dispatcher: IDispatcher) => ({
  networkElementsDashboardActions: createNetworkElementsActions(dispatcher.dispatch, true),
  setCurrentPanel: (panelId: PanelId) => dispatcher.dispatch(new SetPanelAction(panelId)),
});

const ConnectApplicationRouteAdapter = connect(mapProps, mapDisp)((props: RouteComponentProps<{ status?: string }> & Connect<typeof mapProps, typeof mapDisp>) => {
  
  // TODO: move into useEffect!
  if (currentStatus !== props.match.params.status) {
    currentStatus = props.match.params.status || undefined;
    window.setTimeout(() => {
      if (currentStatus) {
        props.setCurrentPanel('NetworkElements');
        props.networkElementsDashboardActions.onFilterChanged('status', currentStatus);
        if (!props.networkElementDashboardProperties.showFilter) {
          props.networkElementsDashboardActions.onToggleFilter(false);
          props.networkElementsDashboardActions.onRefresh();
        } else
          props.networkElementsDashboardActions.onRefresh();
      }
    });
  }
  return (
    <NetworkElementsList />
  );
});


const App = withRouter((props: RouteComponentProps) => (
  <Switch>
    <Route path={`${props.match.path}/connectionStatus/:status?`} component={ConnectApplicationRouteAdapter} />
    <Route path={`${props.match.path}`} component={ConnectApplication} />
    <Redirect to={`${props.match.path}`} />
  </Switch>
));

export function register() {
  const applicationApi = applicationManager.registerApplication({
    name: 'connect',
    icon: appIcon,
    rootComponent: App,
    rootActionHandler: connectAppRootHandler,
    menuEntry: 'Connect',
  });

  // subscribe to the websocket notifications
  subscribe<IFormatedMessage>(['object-creation-notification', 'object-deletion-notification', 'attribute-value-changed-notification'], (msg => {
    const store = applicationApi.applicationStore;
    if (msg && msg.type.type === 'object-creation-notification' && store) {
      store.dispatch(new AddSnackbarNotification({ message: `Adding node [${msg.data['object-id-ref']}]`, options: { variant: 'info' } }));
    } else if (msg && (msg.type.type === 'object-deletion-notification' || msg.type.type === 'attribute-value-changed-notification') && store) {
      store.dispatch(new AddSnackbarNotification({ message: `Updating node [${msg.data['object-id-ref']}]`, options: { variant: 'info' } }));
    }
    if (store) {
      store.dispatch(updateCurrentViewAsyncAction() as any).then(() => {
        if (msg['node-id']) {
          store.dispatch(findWebUrisForGuiCutThroughAsyncAction([msg['node-id']]));
        }
      });
    }
  }));

  applicationApi.applicationStoreInitialized.then(store => {
    store.dispatch(networkElementsReloadAction);
  });

}