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
import { withRouter, RouteComponentProps } from 'react-router-dom';

import { faNewspaper } from '@fortawesome/free-solid-svg-icons/faNewspaper';

import applicationManager from '../../../framework/src/services/applicationManager';
import { connect, Connect } from '../../../framework/src/flux/connect';
import { ApiAction } from '../../../framework/src/middleware/api'; // for RestConf

import { apiDemoRootHandler } from './handlers/apiDemoRootHandler';
import { ModulesRequestSuccess } from './actions/modulesSuccess';
import { Module } from './models/module';

type AppProps = RouteComponentProps & Connect & { modules: Module[]; requestModules: () => void };

const App = (props: AppProps ) => (
  <>
    <button color="inherit" onClick={ props.requestModules }>Load Modules</button>
    <ul>{ props.modules.map((mod, ind) => (<li key={ ind }>{ mod.name }</li>)) }</ul>
  </>
);

const FinalApp = withRouter(connect((state) => ({
  modules: state.apiDemo.modules,
}), (dispatcher => ({
  requestModules: () => { dispatcher.dispatch(new ApiAction('restconf/modules', ModulesRequestSuccess, true)); },
})))(App));

applicationManager.registerApplication({
  name: 'apiDemo',
  icon: faNewspaper,
  rootComponent: FinalApp,
  rootActionHandler: apiDemoRootHandler,
  menuEntry: 'API Demo',
});

