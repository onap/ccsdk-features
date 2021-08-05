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
import { Action } from "../../../../framework/src/flux/action";
import { Height } from "model/Height";

export class SetChartAction extends Action{
    constructor(public startPoint: LatLon, public endPoint: LatLon, public heightA: Height, public heightB: Height){
        super();
    }
}

export class SetStartPointAction extends Action{
    constructor(public startPoint: LatLon|null){
        super();
    }
}

export class SetEndpointAction extends Action{
    constructor(public endPoint: LatLon|null){
        super();
    }
}

export class SetHeightA extends Action{
    constructor(public height: Height){
        super();
    }
}

export class SetHeightB extends Action{
    constructor(public height: Height){
        super();
    }
}

export class ClearSavedChartAction extends Action{
    constructor(){
        super();
    }
}

export class SetMapCenterAction extends Action{
    /**
     *
     */
    constructor(public point: LatLon, public zoom: number) {
        super();
        
    }
}