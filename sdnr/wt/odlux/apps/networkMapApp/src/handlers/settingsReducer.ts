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

import { NetworkMapSettings, NetworkMapThemes } from "../model/settings";
import { IActionHandler } from "../../../../framework/src/flux/action";
import { SetBusyLoadingAction, SetMapSettingsAction, SetSettingsAction, SetThemeSettingsAction } from "../actions/settingsAction";

export type SettingsState = {
    mapSettings: NetworkMapSettings|null,
    themes: NetworkMapThemes,
    isLoadingData: boolean
};


const defaultThemes:NetworkMapThemes = {networkMapThemes:{themes: [

    {key: "light", site: "#11b4da", selectedSite: "#116bda", fiberLink: "#1154d9", microwaveLink: "#039903"},
    {key: "dark", site: "#000000", selectedSite: "#6e6e6e", fiberLink: "#0a2a6b", microwaveLink: "#005200"},
]}}

const initialState: SettingsState = {
    mapSettings: null,
    themes: defaultThemes,
    isLoadingData: true

};

export const SettingsReducer: IActionHandler<SettingsState> = (state = initialState, action) => {
   
    if(action instanceof SetSettingsAction){
        state = {
            isLoadingData: false,
            mapSettings: {networkMap: action.settings.networkMap},
            themes:{networkMapThemes: {themes: action.settings.networkMapThemes.themes}}
        };
    }else if(action instanceof SetMapSettingsAction){
        state={...state, mapSettings: action.settings};
    }else if(action instanceof SetThemeSettingsAction){
        state={...state, themes:{networkMapThemes: {themes: action.settings.networkMapThemes.themes}}};
    }else if(action instanceof SetBusyLoadingAction){
        state={...state, isLoadingData: action.busy};
    }

    return state;

}