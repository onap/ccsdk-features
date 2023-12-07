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

import React from "react";
import { withRouter, RouteComponentProps, Route, Switch, Redirect } from 'react-router-dom';

import applicationManager from '../../../framework/src/services/applicationManager';

import { connect, Connect, IDispatcher } from '../../../framework/src/flux/connect';

import { mediatorAppRootHandler } from './handlers/mediatorAppRootHandler';
import { avaliableMediatorServersReloadAction } from "./handlers/avaliableMediatorServersHandler";

import { MediatorApplication } from "./views/mediatorApplication";
import { MediatorServerSelection } from "./views/mediatorServerSelection";
import { initializeMediatorServerAsyncActionCreator } from "./actions/mediatorServerActions";

const appIcon = require('./assets/icons/mediatorAppIcon.svg');  // select app icon

let currentMediatorServerId: string | undefined = undefined;

const mapDisp = (dispatcher: IDispatcher) => ({
  loadMediatorServer : (mediatorServerId: string) => dispatcher.dispatch(initializeMediatorServerAsyncActionCreator(mediatorServerId)),
});

const MediatorServerRouteAdapter = connect(undefined, mapDisp)((props: RouteComponentProps<{ mediatorServerId: string }> & Connect<undefined, typeof mapDisp>) => {
  if (currentMediatorServerId !== props.match.params.mediatorServerId) {
    // route parameter has changed
    currentMediatorServerId = props.match.params.mediatorServerId || undefined;
    // Hint: This timeout is need, since it is not recommended to change the state while rendering is in progress !
    window.setTimeout(() => {
      if (currentMediatorServerId) {
        props.loadMediatorServer(currentMediatorServerId);
      }
    });
  }
  return (
    <MediatorApplication />
  )
});

type AppProps = RouteComponentProps & Connect;

const App = (props: AppProps) => (
  <Switch>
    <Route exact path={ `${ props.match.path }` } component={ MediatorServerSelection } />
    <Route path={ `${ props.match.path }/:mediatorServerId` } component={ MediatorServerRouteAdapter } />
    <Redirect to={ `${ props.match.path }` } />
   </Switch>
);

const FinalApp = withRouter(connect()(App));

export function register() {
  const applicationApi = applicationManager.registerApplication({
    name: "mediator",
    icon: appIcon,
    rootComponent: FinalApp,
    rootActionHandler: mediatorAppRootHandler,
    menuEntry: "Mediator"
  });

  // prefetch all available mediator servers
  applicationApi.applicationStoreInitialized.then(applicationStore => {
    applicationStore.dispatch(avaliableMediatorServersReloadAction)
  });
};
