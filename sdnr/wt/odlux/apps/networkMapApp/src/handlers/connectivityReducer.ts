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

import { IActionHandler } from "../../../../framework/src/flux/action";
import { IsTopologyServerReachableAction, IsTileServerReachableAction, IsBusycheckingConnectivityAction } from "../actions/connectivityAction";


export type connectivityState = {isToplogyServerAvailable: boolean, isTileServerAvailable: boolean, isBusy: boolean };

const initialState: connectivityState = {isToplogyServerAvailable: true, isTileServerAvailable: true, isBusy: true};

export const ConnectivityReducer: IActionHandler<connectivityState> =(state=initialState, action)=> {

    if(action instanceof IsTopologyServerReachableAction){
        state = Object.assign({}, state, { isToplogyServerAvailable: action.reachable });
    }
    else if (action instanceof IsTileServerReachableAction){
        state = Object.assign({}, state, { isTileServerAvailable: action.reachable });

    }else if(action instanceof IsBusycheckingConnectivityAction){
        state = {...state, isBusy: action.isBusy}
        
    }

    return state;
} 