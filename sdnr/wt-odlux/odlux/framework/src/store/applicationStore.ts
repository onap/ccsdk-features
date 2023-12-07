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

import { Store } from '../flux/store';
import { combineActionHandler, MiddlewareArg, Middleware, chainMiddleware } from '../flux/middleware';

import applicationService from '../services/applicationManager';

import { applicationRegistryHandler, IApplicationRegistration } from '../handlers/applicationRegistryHandler';
import { authenticationStateHandler, IAuthenticationState } from '../handlers/authenticationHandler';
import { applicationStateHandler, IApplicationState } from '../handlers/applicationStateHandler';
import { navigationStateHandler, INavigationState } from '../handlers/navigationStateHandler';

import { setApplicationStore } from '../services/applicationApi';

import apiMiddleware from '../middleware/api';
import thunkMiddleware from '../middleware/thunk';
import loggerMiddleware from '../middleware/logger';
import routerMiddleware from '../middleware/navigation';
import { updatePolicies } from '../middleware/policies';

export type MiddlewareApi = MiddlewareArg<IApplicationStoreState>;

export interface IFrameworkStoreState {
  applicationRegistration: IApplicationRegistration;
  applicationState: IApplicationState;
  authenticationState: IAuthenticationState;
  navigationState: INavigationState;
}

export interface IApplicationStoreState {
  framework: IFrameworkStoreState;
}

const frameworkHandlers = combineActionHandler({
  applicationRegistration: applicationRegistryHandler,
  applicationState: applicationStateHandler,
  authenticationState: authenticationStateHandler,
  navigationState: navigationStateHandler
});

export class ApplicationStore extends Store<IApplicationStoreState> { }

/** This function will create the application store considering the currently registered application ans their middlewares. */
export const applicationStoreCreator = (): ApplicationStore => {
  const middlewares: Middleware<IApplicationStoreState>[] = [];
  const actionHandlers = Object.keys(applicationService.applications).reduce((acc, cur) => {
    const reg = applicationService.applications[cur];
    reg && typeof reg.rootActionHandler === 'function' && (acc[cur] = reg.rootActionHandler);
    reg && reg.middlewares && Array.isArray(reg.middlewares) && middlewares.push(...(reg.middlewares as Middleware<IApplicationStoreState>[]));
    return acc;
  }, { framework: frameworkHandlers } as any);

  const applicationStore = new ApplicationStore(combineActionHandler(actionHandlers), chainMiddleware(loggerMiddleware, thunkMiddleware, routerMiddleware, apiMiddleware, updatePolicies, ...middlewares));
  setApplicationStore(applicationStore);
  return applicationStore;
}

export default applicationStoreCreator;