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
import { Dispatch } from '../flux/store';
import { Action } from '../flux/action';
import { AuthPolicy, User } from '../models/authentication';
import { Settings } from '../models/settings';
import { saveInitialSettings, SetGeneralSettingsAction } from './settingsAction';
import { endWebsocketSession } from '../services/notificationService';
import { endUserSession, startUserSession } from '../services/userSessionService';
import { IApplicationStoreState } from '../store/applicationStore';

export class UpdateUser extends Action {

  constructor(public user?: User) {
    super();
  }
}

export class UpdatePolicies extends Action {

  constructor(public authPolicies?: AuthPolicy[]) {
    super();
  }
}

export const logoutUser = () => (dispatcher: Dispatch, getState: () => IApplicationStoreState) =>{

  const { framework:{ applicationState:{ authentication }, authenticationState: { user } } } = getState();
  
  dispatcher(new UpdateUser(undefined));
  dispatcher(new SetGeneralSettingsAction(null));
  endWebsocketSession();
  endUserSession();
  localStorage.removeItem('userToken');


  //only call if a user is currently logged in
  if (authentication === 'oauth' && user) {

    const url = window.location.origin;
    window.location.href = `${url}/oauth/logout`;
  }
};

/**
 * Loads the user settings for the given user and dispatches a `saveInitialSettings` action with the result.
 * @param user The user for which to load the settings.
 * @param dispatcher The dispatcher function to use for dispatching the `saveInitialSettings` action.
 */
const loadUserSettings = (user: User | undefined, dispatcher: Dispatch) => {

  // fetch used, because state change for user login is not done when frameworks restRequest call is started (and is accordingly undefined -> /userdata call yields 401, unauthorized) and triggering an action from inside the handler / login event is impossible
  // no timeout used, because it's bad practice to add a timeout to hopefully avoid a race condition
  // hence, fetch used to simply use supplied user data for getting settings

  if (user && user.isValid) {

    fetch('/userdata', {
      method: 'GET', 
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'Authorization': `${user.tokenType} ${user.token}`,
      },
    }).then((res: Response)=>{
      if (res.status == 200) {
        return res.json();
      } else {
        return null;
      }
    }).then((result:Settings)=>{
      dispatcher(saveInitialSettings(result));
    });
  }  
};

/**
 * Dispatches an `UpdateUser` action with the given user and starts a user session if the user is defined.
 * Also loads the user settings for the given user and dispatches a `saveInitialSettings` action with the result.
 * Finally, saves the user token to local storage.
 * @param user The user to be logged in.
 * @param dispatcher The dispatcher function to use for dispatching the actions.
 */
export const loginUserAction = (user?: User) => (dispatcher: Dispatch) =>{
  
  dispatcher(new UpdateUser(user));
  if (user) {
    startUserSession(user);
    loadUserSettings(user, dispatcher);
    localStorage.setItem('userToken', user.toString());
  }
};