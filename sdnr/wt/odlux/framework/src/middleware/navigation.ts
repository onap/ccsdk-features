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
import { Location, History, createHashHistory } from "history";

import { ApplicationStore } from "../store/applicationStore";
import { Dispatch } from '../flux/store';

import { LocationChanged, NavigateToApplication } from "../actions/navigationActions";
import { PushAction, ReplaceAction, GoAction, GoBackAction, GoForwardeAction } from '../actions/navigationActions';

import { applicationManager } from "../services/applicationManager";

const routerMiddlewareCreator = (history: History) => () => (next: Dispatch): Dispatch => (action) => {

  if (action instanceof NavigateToApplication) {
    const application = applicationManager.applications && applicationManager.applications[action.applicationName];
    if (application) {
      const href = `/${application.path || application.name}${action.href ? '/' + action.href : ''}`.replace(/\/{2,}/i, '/');
      if (action.replace) {
        history.replace(href, action.state);
      } else {
        history.push(href, action.state);
      }
    }
  } else if (action instanceof PushAction) {
    history.push(action.href, action.state);
  } else if (action instanceof ReplaceAction) {
    history.replace(action.href, action.state);
  } else if (action instanceof GoAction) {
    history.go(action.index);
  } else if (action instanceof GoBackAction) {
    history.goBack();
  } else if (action instanceof GoForwardeAction) {
    history.goForward();
  } else if (action instanceof LocationChanged) {
    // ensure user is logged in and token is valid
    if (!action.pathname.startsWith("/login") && applicationStore && (!applicationStore.state.framework.authenticationState.user || !applicationStore.state.framework.authenticationState.user.isValid)) {
      history.replace(`/login?returnTo=${action.pathname}`);
    } else {
      return next(action);
    }
  } else {
    return next(action);
  }
  return action;
};

function startListener(history: History, store: ApplicationStore) {
  store.dispatch(new LocationChanged(history.location.pathname, history.location.search, history.location.hash));
  history.listen((location: Location) => {
    store.dispatch(new LocationChanged(location.pathname, location.search, location.hash));
  });
}

const history = createHashHistory();
let applicationStore: ApplicationStore | null = null;

export function startHistoryListener(store: ApplicationStore) {
  applicationStore = store;
  startListener(history, store);
}

export const routerMiddleware = routerMiddlewareCreator(history);
export default routerMiddleware;
