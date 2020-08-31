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

import { Action } from '../../../../framework/src/flux/action';
import { Dispatch } from '../../../../framework/src/flux/store';


import { link } from "../model/link";
import { site } from "../model/site";
import { Feature } from '../model/Feature';
import { URL_API } from '../config';


export class HighlightLinkAction extends Action{
    constructor(public link: link){
        super();
    }
}

export class HighlightSiteAction extends Action{
    constructor(public site: site){
        super();
    }
}

export class RemoveHighlightingAction extends Action {
   constructor(){
       super();
   }
}

export class ZoomToSearchResultAction extends Action{
    constructor(public lat: number, public lon: number){
        super();
    }
}

export class AddAlarmAction extends Action{
    constructor(public element: Feature){
        super();
    }
}

export class SetCoordinatesAction extends Action{
    constructor(public lat: number, public lon: number, public zoom: number){
        super();
    }
}

export class SetStatistics extends Action{
    constructor(public siteCount: string, public linkCount: string){
        super();
    }
}

export class SetIconSwitchAction extends Action{
    constructor(public enable:boolean){
        super();
    }
}

export const findSiteToAlarm = (alarmedNodeId: string) => (dispatcher: Dispatch) =>{
    fetch(URL_API+"/site/geojson/device/"+alarmedNodeId)
    .then(res => res.json())
    .then(result=>{
        dispatcher(new AddAlarmAction(result));
    });
}