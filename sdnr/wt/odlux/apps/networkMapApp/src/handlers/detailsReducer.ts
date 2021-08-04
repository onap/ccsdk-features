/**
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt odlux
 * =================================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property. All rights reserved.
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

import { IActionHandler } from '../../../../framework/src/flux/action';
import { link } from "../model/link";
import { Site, Device } from "../model/site";
import { HistoryEntry } from "../model/historyEntry";
import { SelectSiteAction, SelectLinkAction, AddToHistoryAction, ClearHistoryAction, IsBusyCheckingDeviceListAction, FinishedLoadingDeviceListAction, ClearLoadedDevicesAction, ClearDetailsAction, InitializeLoadedDevicesAction, IsSitedocReachableAction } from '../actions/detailsAction';

export type DetailsStoreState={
    data: Site | link | null,
    history: HistoryEntry[],
    isBusyCheckingDeviceList: boolean,
    checkedDevices: Device[],
    isSitedocReachable: boolean

}

const initialState: DetailsStoreState = {
    data: null,
    history:[],
    isBusyCheckingDeviceList: false,
    checkedDevices: [],
    isSitedocReachable: false
}

export const DetailsReducer:IActionHandler<DetailsStoreState>=(state = initialState, action)=>{
    
    if(action instanceof SelectSiteAction){
        state= Object.assign({}, state, {data: action.site});
    }
    else if(action instanceof SelectLinkAction){
        state =  Object.assign({}, state, {data: action.link});
    }else if(action instanceof ClearDetailsAction){
        state =  Object.assign({}, state, {data: null});
    }
    else if(action instanceof AddToHistoryAction){
        state = Object.assign({}, state, {history: [...state.history, action.entry]})

    }else if(action instanceof ClearHistoryAction){
        state = Object.assign({}, state, {history: []});

    }else if(action instanceof IsBusyCheckingDeviceListAction){
        state = Object.assign({}, state, {isBusyCheckingDeviceList: action.isBusy});
    }else if (action instanceof FinishedLoadingDeviceListAction){
        state = Object.assign({}, state, {checkedDevices: action.devices});
        
    }else if(action instanceof ClearLoadedDevicesAction){
        state = Object.assign({}, state, {checkedDevices: []});

    }else if(action instanceof InitializeLoadedDevicesAction){
        state = Object.assign({}, state, {checkedDevices: action.devices});
    }else if(action instanceof IsSitedocReachableAction){
        state = Object.assign({}, state, {isSitedocReachable: action.isReachable});
    }


    return state;

}

