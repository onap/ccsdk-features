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
import { GeneralSettings } from '../models/settings';
import { SetGeneralSettingsAction, setGeneralSettingsAction } from './settingsAction';
import { endWebsocketSession } from '../services/notificationService';

export class UpdateUser extends Action {

  constructor (public user?: User) {
    super();
  }
}

export class UpdatePolicies extends Action {

  constructor (public authPolicies?: AuthPolicy[]) {
    super();
  }
}


export const loginUserAction = (user?: User) => (dispatcher: Dispatch) =>{
  
  dispatcher(new UpdateUser(user));
  loadUserSettings(user, dispatcher);


}

export const logoutUser = () => (dispatcher: Dispatch) =>{
  
  dispatcher(new UpdateUser(undefined));
  dispatcher(new SetGeneralSettingsAction(null));
  endWebsocketSession();
}

const loadUserSettings = (user: User | undefined, dispatcher: Dispatch) =>{


  //fetch used, because state change for user login is not done when frameworks restRequest call is started (and is accordingly undefined -> /userdata call yields 401, unauthorized) and triggering an action from inside the handler / login event is impossible
  //no timeout used, because it's bad practise to add a timeout to hopefully avoid a race condition
  //hence, fetch used to simply use supplied user data for getting settings

  if(user && user.isValid){

    fetch("/userdata", {
      method: 'GET', 
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'Authorization': `${user.tokenType} ${user.token}`
      }
    }).then((res: Response)=>{
      if(res.status==200){
        return res.json();
      }else{
        return null;
      }
    }).then((result:GeneralSettings)=>{
      if(result?.general){
        //will start websocket session if applicable
        dispatcher(setGeneralSettingsAction(result.general.areNotificationsEnabled!));
  
      }else{
        dispatcher(setGeneralSettingsAction(false));
      }
    })
  }  
}