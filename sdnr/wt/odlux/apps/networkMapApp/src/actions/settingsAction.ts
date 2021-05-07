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

import { NetworkMapSettings, NetworkMapThemes, NetworkSettings } from '../model/settings';
import { Action } from '../../../../framework/src/flux/action';
import { Dispatch } from '../../../../framework/src/flux/store';
import { settingsService } from '../services/settingsService';

export class SetMapSettingsAction extends Action{

    constructor(public settings:NetworkMapSettings) {
        super();
    }
}

export class SetThemeSettingsAction extends Action{

    constructor(public settings:NetworkMapThemes) {
        super();
    }
}

export class SetSettingsAction extends Action{

    constructor(public settings:NetworkSettings) {
        super();
    }
}

export class SetBusyLoadingAction extends Action{
   
    constructor(public busy: boolean) {
        super();
        
    }
}


export const getSettings = () => async (dispatcher: Dispatch) => {
    dispatcher(new SetBusyLoadingAction(true));
    console.log("getting settings in action..")
 settingsService.getMapSettings().then(result =>{
     if(result){
         if(result.networkMap && result.networkMapThemes){
            const mapSettings : NetworkSettings = { networkMap: result.networkMap, networkMapThemes: result.networkMapThemes}
            dispatcher(new SetSettingsAction(mapSettings));
         }else if(result.networkMap){
            dispatcher(new SetMapSettingsAction(result));
         }else if(result.networkMapThemes){
             dispatcher(new SetThemeSettingsAction(result));
         }
    }
    else{
        console.warn("settings couldn't be loaded.");
    }
    dispatcher(new SetBusyLoadingAction(false));
 });
}

export const updateSettings = (mapSettings: NetworkMapSettings) => async (dispatcher: Dispatch) =>{

    const result = await settingsService.updateMapSettings(mapSettings);
    console.log("update settings");
    dispatcher(new SetMapSettingsAction(mapSettings));

    console.log(result);
    if(result){
    }

}
