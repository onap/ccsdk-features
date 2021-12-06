/**
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt odlux
 * =================================================================================================
 * Copyright (C) 2021 highstreet technologies GmbH Intellectual Property. All rights reserved.
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

import { Dispatch } from "../flux/store";
import { Action } from "../flux/action";
import { GeneralSettings } from "../models/settings";
import { getSettings, putSettings } from "../services/settingsService";
import { startWebsocketSession, suspendWebsocketSession } from "../services/notificationService";


export class SetGeneralSettingsAction extends Action{
    /**
     *
     */
    constructor(public areNoticationsActive: boolean|null) {
        super();
        
    }
}

export const setGeneralSettingsAction = (value: boolean) => (dispatcher: Dispatch) =>{

    dispatcher(new SetGeneralSettingsAction(value));

    if(value){
        startWebsocketSession();
    }else{
        suspendWebsocketSession();
    }
}


export const updateGeneralSettingsAction = (activateNotifications: boolean) => async (dispatcher: Dispatch) =>{

    const value: GeneralSettings = {general:{areNotificationsEnabled: activateNotifications}};
    const result = await putSettings("/general", JSON.stringify(value.general));
    dispatcher(setGeneralSettingsAction(activateNotifications));

}

export const getGeneralSettingsAction = () => async (dispatcher: Dispatch) => {

    const result = await getSettings<GeneralSettings>();

    if(result && result.general){
        dispatcher(new SetGeneralSettingsAction(result.general.areNotificationsEnabled!))
    }

}