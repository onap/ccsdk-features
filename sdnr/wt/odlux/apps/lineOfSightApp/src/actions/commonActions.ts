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

 import { Height } from "model/Height";
import { isNumber } from "../utils/math";
import { Action } from "../../../../framework/src/flux/action";
 import { Dispatch } from "../../../../framework/src/flux/store";
 import { IApplicationStoreState } from "../../../../framework/src/store/applicationStore";
 
 import { LatLon } from "../model/LatLon";

 
 export class SetPassedInValuesAction extends Action{
     constructor(public start: LatLon, public end: LatLon, public center: LatLon, public heightA : Height |null, public heightB: Height |null){
         super();
     }
 }

 export class SetReachableAction extends Action{
     constructor(public reachable: boolean | null){
         super();
     }
 }

 export const SetPassedInValues = (values: (string|null)[]) => (dispatcher: Dispatch) =>{

     const start: LatLon = {latitude: Number(values[0]), longitude: Number(values[1])}
     const end: LatLon = {latitude: Number(values[2]), longitude: Number(values[3])};
     const midpoint = calculateMidPoint(start.latitude, start.longitude, end.latitude, end.longitude);
     const center: LatLon = {latitude: midpoint[1], longitude: midpoint[0]};
     const heightA: Height | null = isNumber(values[4]) && isNumber(values[5]) ? {amsl:+values[4]!, antennaHeight: +values[5]!} : null;
     const heightB: Height | null = isNumber(values[6]) && isNumber(values[7]) ? {amsl:+values[6]!, antennaHeight: +values[7]!} : null;


    dispatcher(new SetPassedInValuesAction(start, end, center, heightA, heightB));
 }

 //taken from https://www.movable-type.co.uk/scripts/latlong.html
const calculateMidPoint = (lat1: number, lon1: number, lat2: number, lon2: number) =>{

    const dLon = degrees_to_radians(lon2 - lon1);

    //convert to radians
    lat1 = degrees_to_radians(lat1);
    lat2 = degrees_to_radians(lat2);
    lon1 = degrees_to_radians(lon1);

    const Bx = Math.cos(lat2) * Math.cos(dLon);
    const By = Math.cos(lat2) * Math.sin(dLon);
    const lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By * By));
    const lon3 = lon1 + Math.atan2(By, Math.cos(lat1) + Bx);

    return [radians_to_degrees(lon3), radians_to_degrees(lat3)];
}

const degrees_to_radians = (degrees: number) =>
{
return degrees * (Math.PI/180);
}

const radians_to_degrees = (radians:number) =>{
    
    var pi = Math.PI;
    return radians * (180/pi);
}