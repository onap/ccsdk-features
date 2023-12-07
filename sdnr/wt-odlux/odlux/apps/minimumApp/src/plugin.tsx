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

import React, { FC } from 'react';
import { withRouter, RouteComponentProps } from 'react-router-dom';

import applicationManager from '../../../framework/src/services/applicationManager';
import { connect, Connect } from '../../../framework/src/flux/connect';

import { minimumAppRootHandler } from './handlers/minimumAppRootHandler';

// const appIcon = require('./assets/icons/minimunAppIcon.svg');  // select app icon

type AppProps = RouteComponentProps & Connect;

const App: FC<AppProps> = (_props) => (
  <div>Start your app here!!</div>
);

const FinalApp = withRouter(connect()(App));

export function register() {
  applicationManager.registerApplication({
    name: 'minimum',
    // icon: appIcon,
    rootComponent: FinalApp,
    rootActionHandler: minimumAppRootHandler,
    menuEntry: 'Minimum',
  });
}


