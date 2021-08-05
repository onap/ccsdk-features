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

 import { LatLon } from "../model/LatLon";
import { IActionHandler } from "../../../../framework/src/flux/action";
import { SetPassedInValuesAction, SetReachableAction } from "../actions/commonActions";
import { ClearSavedChartAction, SetChartAction, SetEndpointAction, SetHeightA, SetHeightB, SetMapCenterAction, SetStartPointAction } from "../actions/mapActions";
import { Height } from "model/Height";
import { isNullOrUndefined } from "util";

 
 
 export interface IMap {
   center: LatLon;
   zoom: number;
   start: LatLon |null;
   heightA: Height | null;
   end: LatLon|null;
   heightB: Height | null;
   ready: boolean |null;
 }
 
 const initialState: IMap = {
    center: {latitude:52.4003, longitude:13.0584},
    zoom: 12,
    start: null,
    end: null,
    ready: null,
    heightA: null,
    heightB: null

 }
 
 export const mapHandler: IActionHandler<IMap> = (state = initialState, action) => {
   if (action instanceof SetPassedInValuesAction) {
     state = { ...state, start: action.start, end: action.end, center: action.center, heightA: action.heightA, heightB: action.heightB };
   }
   else if(action instanceof SetReachableAction){
    state = { ...state, ready: action.reachable };
     
   }else if(action instanceof SetChartAction){
     state = {...state, start:action.startPoint, end: action.endPoint, heightA: action.heightA, heightB: action.heightB}
   }
   else if(action instanceof SetStartPointAction){
    state = {...state, start:action.startPoint}

   }
   else if(action instanceof SetEndpointAction){
    state = {...state, end:action.endPoint}

   }
   else if(action instanceof SetHeightA){
    state = {...state, heightA:action.height}

   }
   else if(action instanceof SetHeightB){
    state = {...state, heightB:action.height}

   }
   else if(action instanceof ClearSavedChartAction){
     state= {...state, start: null, end: null, heightA:null, heightB: null}
   }else if(action instanceof SetMapCenterAction){
     state={...state, zoom: action.zoom,center:action.point}
   }
 
   return state;
 }