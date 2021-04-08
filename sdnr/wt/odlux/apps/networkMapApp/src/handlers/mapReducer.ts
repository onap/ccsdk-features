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
import { Feature } from "../model/Feature";
import { HighlightLinkAction, HighlightSiteAction, ZoomToSearchResultAction, AddAlarmAction, SetCoordinatesAction, SetStatistics, SetIconSwitchAction, RemoveHighlightingAction } from '../actions/mapActions';

export type location = {lat: number, lon:number}

export type mapState = {
    selectedLink: Feature | null,
    selectedSite: Feature | null,
    zoomToElement: location | null,
    alarmlement: Feature|null,
    lat: number,
    lon: number, 
    zoom: number,
    statistics:{links: string, sites: string},
    allowIconSwitch: boolean
}

const initialState: mapState ={
    selectedLink: null,
    selectedSite: null,
    zoomToElement: null,
    alarmlement: null,
    lat: 52.5095,
    lon: 13.3290,
    zoom: 10,
    statistics:{links:"Not counted yet.", sites: "Not counted yet."},
    allowIconSwitch: true
}

export const MapReducer: IActionHandler<mapState> = (state=initialState, action: any) => {
    
    if(action instanceof HighlightLinkAction){
      
        state = Object.assign({}, state, {selectedSite: null, selectedLink:{type: "Feature", properties:{id:action.link.id, type: action.link.type}, geometry:{type:"LineString", coordinates:[[action.link.locationA.lon,action.link.locationA.lat ],[action.link.locationB.lon,action.link.locationB.lat ]]}}})


    }
    else if(action instanceof HighlightSiteAction){
       
    state = Object.assign({}, state, {selectedLink: null, selectedSite:{type: "Feature", properties: {id: action.site.id, type:action.site.type}, geometry:{type:"Point", coordinates:[action.site.location.lon,action.site.location.lat ]}}})

    }else if (action instanceof ZoomToSearchResultAction){
        state = Object.assign({}, state, {zoomToElement:{lat: action.lat, lon: action.lon}});
    }else if (action instanceof AddAlarmAction){
        state = Object.assign({}, state, {alarmlement:{type: "Feature", properties: {id: action.site.id, type:action.site.type}, geometry:{type:"Point", coordinates:[action.site.location.lon,action.site.location.lat ]}}});

    }else if(action instanceof SetCoordinatesAction){
        state = Object.assign({}, state, {lat:action.lat, lon: action.lon, zoom:action.zoom});
        
    }else if(action instanceof SetStatistics){
        state = Object.assign({}, state, {statistics:{sites: action.siteCount, links: action.linkCount}});

    }else if (action instanceof SetIconSwitchAction){
        state = Object.assign({}, state, {allowIconSwitch: action.enable});

    }else if(action instanceof RemoveHighlightingAction){
        state = Object.assign({}, state, {selectedLink: null, selectedSite:null})

    }

    return state;
}