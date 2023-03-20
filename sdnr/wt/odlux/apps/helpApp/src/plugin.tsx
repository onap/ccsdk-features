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
import { IApplicationStoreState } from "../../../framework/src/store/applicationStore";
import { connect, Connect, IDispatcher } from '../../../framework/src/flux/connect';

import { requestTocAsyncAction, requestDocumentAsyncActionCreator } from "./actions/helpActions";
import { helpAppRootHandler } from './handlers/helpAppRootHandler';

import { HelpApplication } from './views/helpApplication';
import { HelpStatus } from "./components/helpStatus";

import '!style-loader!css-loader!highlight.js/styles/default.css';
import HelpTocApp from "./views/helpTocApp";

const appIcon = require('./assets/icons/helpAppIcon.svg');  // select app icon

const mapProps = (state: IApplicationStoreState) => ({

});

const mapDispatch = (dispatcher: IDispatcher) => ({
  requestDocument: (path: string) => {
    dispatcher.dispatch(requestDocumentAsyncActionCreator(path));
  }
});

let currentHelpPath: string | undefined = undefined;

const HelpApplicationRouteAdapter = connect(mapProps, mapDispatch)((props: RouteComponentProps<{ '0'?: string }> & Connect<typeof mapProps, typeof mapDispatch>) => {

  if (currentHelpPath !== props.match.params["0"]) {
    // route parameter has changed
    currentHelpPath = props.match.params["0"] || undefined;
    // Hint: This timeout is need, since it is not recommended to change the state while rendering is in progress !
    window.setTimeout(() => {
      if (currentHelpPath) {
        props.requestDocument(currentHelpPath);
      }
    });
  }

  return (
    <HelpApplication />
  )
});

const App = withRouter((props: RouteComponentProps) => (
  <Switch>
    <Route exact path={`${props.match.path}/`} component={HelpTocApp} />
    <Route path={`${props.match.path}/*`} component={HelpApplicationRouteAdapter} />
    <Route path={`${props.match.path}`} component={HelpTocApp} />
  </Switch>
));

export async function register() {
  const applicationApi = applicationManager.registerApplication({
    name: "help",
    icon: appIcon,
    rootComponent: App,
    rootActionHandler: helpAppRootHandler,
    statusBarElement: HelpStatus,
    menuEntry: "Help",
    //subMenuEntry: SubMenuEntry 
  });

  // start the initial toc request after the application store is initialized
  const store = await applicationApi.applicationStoreInitialized;
  store.dispatch(requestTocAsyncAction);

}