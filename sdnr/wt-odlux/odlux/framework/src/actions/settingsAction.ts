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
import { GeneralSettings, Settings, TableSettings, TableSettingsColumn } from "../models/settings";
import { getUserData, saveUserData } from "../services/userdataService";
import { startWebsocketSession, suspendWebsocketSession } from "../services/notificationService";
import { IApplicationStoreState } from "../store/applicationStore";


export class SetGeneralSettingsAction extends Action {
    /**
     *
     */
    constructor(public areNoticationsActive: boolean | null) {
        super();
    }
}

export class SetTableSettings extends Action {

    constructor(public tableName: string, public updatedColumns: TableSettingsColumn[]) {
        super();
    }
}

export class LoadSettingsAction extends Action {

    constructor(public settings: Settings & { isInitialLoadDone: true }) {
        super();
    }

}

export class SettingsDoneLoadingAction extends Action {

}

export const setGeneralSettingsAction = (value: boolean) => (dispatcher: Dispatch) => {

    dispatcher(new SetGeneralSettingsAction(value));

    if (value) {
        startWebsocketSession();
    } else {
        suspendWebsocketSession();
    }
}


export const updateGeneralSettingsAction = (activateNotifications: boolean) => async (dispatcher: Dispatch) => {

    const value: GeneralSettings = { general: { areNotificationsEnabled: activateNotifications } };
    const result = await saveUserData("/general", JSON.stringify(value.general));
    dispatcher(setGeneralSettingsAction(activateNotifications));

}

export const updateTableSettings = (tableName: string, columns: TableSettingsColumn[]) => async (dispatcher: Dispatch, getState: () => IApplicationStoreState) => {


    //TODO: ask micha how to handle object with variable properties!
    //fix for now: just safe everything!

     let {framework:{applicationState:{settings:{tables}}}} = getState();

     tables[tableName] = { columns: columns };
     const json=JSON.stringify(tables);

    // would only save latest entry
    //const json = JSON.stringify({ [tableName]: { columns: columns } });

    const result = await saveUserData("/tables", json);

    dispatcher(new SetTableSettings(tableName, columns));
}

export const getGeneralSettingsAction = () => async (dispatcher: Dispatch) => {

    const result = await getUserData<GeneralSettings>();

    if (result && result.general) {
        dispatcher(new SetGeneralSettingsAction(result.general.areNotificationsEnabled!))
    }
}

export const saveInitialSettings = (settings: any) => async (dispatcher: Dispatch) => {

    const defaultSettings = {general:{ areNotificationsEnabled: false }, tables:{}};

    const initialSettings = {...defaultSettings, ...settings};

    if (initialSettings) {
        if (initialSettings.general) {
            const settingsActive = initialSettings.general.areNotificationsEnabled;

            if (settingsActive) {
                startWebsocketSession();
            } else {
                suspendWebsocketSession();
            }
        }

        dispatcher(new LoadSettingsAction(initialSettings));
    }
    else {
        dispatcher(new SettingsDoneLoadingAction());

    }




}