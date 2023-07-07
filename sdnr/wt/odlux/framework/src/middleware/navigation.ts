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
import * as jwt from 'jsonwebtoken';
import { History, createHashHistory } from 'history';

import { User } from '../models/authentication';

import { LocationChanged, NavigateToApplication } from '../actions/navigationActions';
import { PushAction, ReplaceAction, GoAction, GoBackAction, GoForwardeAction } from '../actions/navigationActions';

import { applicationManager } from '../services/applicationManager';
import { loginUserAction, logoutUser } from '../actions/authentication';

import { ApplicationStore } from '../store/applicationStore';
import { Dispatch } from '../flux/store';

export const history = createHashHistory();
let applicationStore: ApplicationStore | null = null;

const routerMiddlewareCreator = (historyParam: History) => () => (next: Dispatch): Dispatch => (action) => {

  if (action instanceof NavigateToApplication) {
    const application = applicationManager.applications && applicationManager.applications[action.applicationName];
    if (application) {
      const href = `/${application.path || application.name}${action.href ? '/' + action.href : ''}`.replace(/\/{2,}/i, '/');
      if (action.replace) {
        historyParam.replace(href, action.state);
      } else {
        historyParam.push(href, action.state);
      }
    }
  } else if (action instanceof PushAction) {
    historyParam.push(action.href, action.state);
  } else if (action instanceof ReplaceAction) {
    historyParam.replace(action.href, action.state);
  } else if (action instanceof GoAction) {
    historyParam.go(action.index);
  } else if (action instanceof GoBackAction) {
    historyParam.goBack();
  } else if (action instanceof GoForwardeAction) {
    historyParam.goForward();
  } else if (action instanceof LocationChanged) {
    // ensure user is logged in and token is valid
    if (action.pathname.startsWith('/oauth') && (action.search.startsWith('?token='))) {
      const ind =  action.search.lastIndexOf('token=');
      const tokenStr = ind > -1 ? action.search.substring(ind + 6) : null;
      const token = tokenStr && jwt.decode(tokenStr);
      if (tokenStr && token) {
        // @ts-ignore
        const user = new User({ username: token.name, access_token: tokenStr, token_type: 'Bearer', expires: token.exp, issued: token.iat }) || undefined;
        applicationStore?.dispatch(loginUserAction(user));
      }
    } if (!action.pathname.startsWith('/login') && applicationStore && (!applicationStore.state.framework.authenticationState.user || !applicationStore.state.framework.authenticationState.user.isValid)) {
      historyParam.replace(`/login?returnTo=${action.pathname}`);
      applicationStore.dispatch(logoutUser());
    
    } else if (action.pathname.startsWith('/login') && applicationStore && (applicationStore.state.framework.authenticationState.user && applicationStore.state.framework.authenticationState.user.isValid)) {
      historyParam.replace('/');
    } else {
      return next(action);
    }
  } else {
    return next(action);
  }
  return action;
};

const startListener = (historyParam: History, store: ApplicationStore) => {
  store.dispatch(new LocationChanged(historyParam.location.pathname, historyParam.location.search, historyParam.location.hash));
  historyParam.listen((location) => {
    store.dispatch(new LocationChanged(location.pathname, location.search, location.hash));
  });
};

export const startHistoryListener = (store: ApplicationStore) => {
  applicationStore = store;
  startListener(history, store);
};

export const routerMiddleware = routerMiddlewareCreator(history);
export default routerMiddleware;
