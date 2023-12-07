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
import { withRouter, RouteComponentProps, Route, Switch, Redirect } from 'react-router-dom';

import { faAddressBook } from '@fortawesome/free-solid-svg-icons/faAddressBook';

import applicationManager from '../../../framework/src/services/applicationManager';
import { connect, Connect } from '../../../framework/src/flux/connect';

import { demoAppRootHandler } from './handlers/demoAppRootHandler';

import AuthorsList from './views/authorsList';
import EditAuthor from './views/editAuthor';

import { Counter } from './components/counter';

type AppProps = RouteComponentProps & Connect;

const App = (props: AppProps) => (
  <Switch>
    <Route exact path={ `${ props.match.path }/authors` } component={AuthorsList} />
    <Route path={ `${ props.match.path }/authors/:authorId` } component={EditAuthor } />
    <Redirect to={ `${ props.match.path }/authors` } />
   </Switch>
);

const FinalApp = withRouter(connect()(App));

export function register() {
  applicationManager.registerApplication({
    name: 'demo',
    icon: faAddressBook,
    rootComponent: FinalApp,
    rootActionHandler: demoAppRootHandler,
    exportedComponents: { counter: Counter },
    menuEntry: 'Demo',
  });
}
