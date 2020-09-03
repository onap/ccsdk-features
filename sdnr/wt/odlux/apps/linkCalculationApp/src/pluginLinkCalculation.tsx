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
// app configuration and main entry point for the app

import * as React from "react";
import { withRouter, RouteComponentProps, Route, Switch, Redirect } from 'react-router-dom';

import { faBookOpen } from '@fortawesome/free-solid-svg-icons'; // select app icon
import applicationManager from '../../../framework/src/services/applicationManager';

import  LinkCalculation  from './views/linkCalculationComponent';
import LinkCalculationAppRootHandler from './handlers/linkCalculationAppRootHandler';
import connect, { Connect, IDispatcher } from '../../../framework/src/flux/connect';
import { IApplicationStoreState } from "../../../framework/src/store/applicationStore";
import { UpdateLinkIdAction, UpdateLatLonAction, updateHideForm, UpdateSiteAction, UpdateDistanceAction, isCalculationServerReachableAction, updateAltitudeAction } from "./actions/commonLinkCalculationActions";


let currentLinkId: string | null = null;
let lastUrl: string = "/linkCalculation";

const mapProps = (state: IApplicationStoreState) => ({
  reachable: state.linkCalculation.calculations.reachable
});

const mapDisp = (dispatcher: IDispatcher) => ({
  updateLinkId: (mountId: string) => dispatcher.dispatch(new UpdateLinkIdAction(mountId)),

  updateSiteName: (siteNameA?:any, siteNameB?:any)=>{
    dispatcher.dispatch(new UpdateSiteAction(siteNameA, siteNameB))
  },
  updateDistance :(distance:number) =>{
    dispatcher.dispatch(new UpdateDistanceAction(distance))
  },
  updateLatLon : (Lat1:number, Lon1:number, Lat2:number, Lon2:number)=> {

    dispatcher.dispatch(new UpdateLatLonAction(Lat1, Lon1, Lat2, Lon2))
    dispatcher.dispatch(new updateHideForm (true))
  },
  updateAltitude : (amslA:number, aglA:number, amslB:number, aglB:number) => {
    dispatcher.dispatch(new updateAltitudeAction(amslA,aglA,amslB,aglB))
  }
  // UpdateConectivity : (reachable:boolean) => {
  //   dispatcher.dispatch (new isCalculationServerReachableAction (reachable))
  // }
});


const LinkCalculationRouteAdapter = connect(mapProps, mapDisp)((props: RouteComponentProps<{ mountId?: string }> & Connect<typeof mapProps, typeof mapDisp>) => {
  let linkId: string = "";

  // called when component finshed mounting
  React.useEffect(() => {

    lastUrl = props.location.pathname;
    linkId = getLinkId(lastUrl);

    const data= props.location.search

    

    if (data !== undefined && data.length>0){

    
    const lat1 = data.split('&')[0].split('=')[1]
    const lon1 = data.split('&')[1].split('=')[1]
    const lat2 = data.split('&')[2].split('=')[1]
    const lon2 = data.split('&')[3].split('=')[1]

    const siteNameA = data.split('&')[4].split('=')[1]
    const siteNameB = data.split('&')[5].split('=')[1]

    const distance = data.split('&')[8].split('=')[1]

    const amslA = data.split('&')[9].split('=')[1]
    const aglA = data.split('&')[10].split('=')[1]

    const amslB = data.split('&')[11].split('=')[1]
    const aglB = data.split('&')[12].split('=')[1]


    props.updateSiteName(String(siteNameA), String(siteNameB))

    props.updateDistance(Number(distance))

    props.updateLatLon(Number(lat1),Number(lon1),Number(lat2),Number(lon2))

    props.updateAltitude (Number(amslA), Number(aglA), Number(amslB), Number(aglB))
    
    }
    

    if (currentLinkId !== linkId) { // new element is loaded
      currentLinkId = linkId;
      props.updateLinkId(currentLinkId);
    } 
  }, []);

  // called when component gets updated
  React.useEffect(() => {

    lastUrl = props.location.pathname;
    linkId = getLinkId(lastUrl);

    if (currentLinkId !== linkId) {
      currentLinkId = linkId;
      props.updateLinkId(currentLinkId);
    }
  });

  const getLinkId = (lastUrl: string) => {
    let index = lastUrl.lastIndexOf("linkCalculation/");
    if (index >= 0) {
      linkId = lastUrl.substr(index+16);
    } else {
      linkId = "";
    }

    return linkId;
  }

  
  return (
    <LinkCalculation />
  );
});

const App = withRouter((props: RouteComponentProps) => {
  props.history.action = "POP";
  return (
    <Switch>
      <Route path={`${props.match.path}/:linkId`} component={LinkCalculationRouteAdapter} />
      <Route path={`${props.match.path}`} component={LinkCalculationRouteAdapter} />
      <Redirect to={`${props.match.path}`} />
    </Switch>
  )
});

export function register() {
  applicationManager.registerApplication({
    name: "linkCalculation",
    icon: faBookOpen,
    rootActionHandler: LinkCalculationAppRootHandler,
    rootComponent: App,
    menuEntry: "Link Calculation"
  });
}

