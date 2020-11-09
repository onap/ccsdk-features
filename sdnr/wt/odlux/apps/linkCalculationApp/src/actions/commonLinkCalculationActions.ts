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

import { Action } from "../../../../framework/src/flux/action";
import { Dispatch } from "../../../../framework/src/flux/store";
import { IApplicationStoreState } from "../../../../framework/src/store/applicationStore";


export class UpdateLinkIdAction extends Action{
    constructor(public linkId: string){
        super();
    }
}

export class UpdateFrequencyAction extends Action{
    constructor(public frequency: number){
        super();
    }
}
export class UpdateSiteAction extends Action{
    constructor(
        public siteA?: any,
        public siteB?: any
         ){
        super();
    }
}
export class UpdateRainAttAction extends Action{
    
    constructor(public rainAtt: number){
        super();
    }
}
export class UpdateRainValAction extends Action{
    constructor(public rainVal: number){
        super();
    }
}

export class updateHideForm extends Action{
    constructor(public formView: boolean){
        super();
    }
}
export class UpdateDistanceAction extends Action{
    constructor(public distance: number){
        super();
    }
}

export class UpdateFslCalculation extends Action{
    constructor(public fsl: number){
        super();
    }
}


export class UpdateLatLonAction extends Action{
    constructor(
        public Lat1: number,
        public Lon1:number,
        public Lat2: number, 
        public Lon2: number
        ){
        super();
        
    }
}
export class UpdatePolAction extends Action{
    constructor(public polarization: string){
        super();
    }
}
export class isCalculationServerReachableAction extends Action{
    constructor(public reachable: boolean){
        super();
    }
}
export class updateAltitudeAction extends Action{
    constructor(
        public amslA:number,
        public aglA:number,
        public amslB:number,
        public aglB:number
        ){
        super();
    }
}
export class UpdateAbsorptionLossAction extends Action{
        constructor(
            public absorptionOxygen:number,
            public absorptionWater:number,
            
            ){
            super();
        }
}
export class UpdateWorstMonthRainAction extends Action{
    constructor(public month: string){
        super();
    }
}

