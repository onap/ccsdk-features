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

// app configuration and main entry point for the app

import * as React from "react";
import { faRoute } from '@fortawesome/free-solid-svg-icons'; // select app icon
import applicationManager from '../../../framework/src/services/applicationManager';


import { lineofSightRootHandler } from './handlers/rootHandler';
import MainView from "./views/main";
import applicationApi from "../../../framework/src/services/applicationApi";

import { Redirect, Route, RouteComponentProps, Switch, useLocation, withRouter } from "react-router-dom";
import connect, { Connect, IDispatcher } from "../../../framework/src/flux/connect";
import { IApplicationStoreState } from "../../../framework/src/store/applicationStore";
import { SetPassedInValues, SetReachableAction } from "./actions/commonActions";
import { TERRAIN_URL, TILE_URL } from "./config";
import { isNumber } from "./utils/math";

const mapProps = (state: IApplicationStoreState) => ({
});

const mapDisp = (dispatcher: IDispatcher) => ({
  setPassedInValues: (values: (string | null)[]) => dispatcher.dispatch(SetPassedInValues(values)),
  setReachable: (reachable: boolean) => dispatcher.dispatch(new SetReachableAction(reachable))

});

let lastSearch = "";

const useQuery = () => {
  return new URLSearchParams(useLocation().search);
}


const LineOfSightApplicationRouteAdapter = connect(mapProps, mapDisp)((props: RouteComponentProps<{ mountId?: string }> & Connect<typeof mapProps, typeof mapDisp>) => {

  let query = useQuery();

  // called when component finshed mounting
  React.useEffect(() => {
    extractAndDispatchUrlValues(props.location.search);

    //check tiles/terrain connectivity
    tryCheckConnection();

  }, []);


  const extractAndDispatchUrlValues = (url: string) => {

    if (lastSearch !== url) {
      lastSearch = url;

      //if mandatory values aren't there, do nothing
      if (areMandatoryParamsPresent(query)) {
        const values = extractValuesFromURL(query);
        props.setPassedInValues(values);
      }
    }
  }

  const tryCheckConnection =() =>{
    const terrain = fetch(`${TERRAIN_URL}/`);
    const tiles = fetch(`${TILE_URL}/10/0/0.png`);

    Promise.all([terrain, tiles])
      .then((result) => {
        props.setReachable(true);

    })
    .catch(error=>{
      console.error("services not reachable.");
      console.error(error);
      props.setReachable(false);

      })

  }

  /***
   * 
   * Checks if lat1, lon1, lat2, lon2 were passed in as url parameters
   */
  const areMandatoryParamsPresent = (query: URLSearchParams) => {

    return isNumber(query.get("lat1")) && isNumber(query.get("lon1")) && isNumber(query.get("lat2")) && isNumber(query.get("lon2"))

  }

  const extractValuesFromURL = (query: URLSearchParams) => { 

    return [query.get("lat1"), query.get("lon1"), query.get("lat2"), query.get("lon2"), query.get("amslA"), query.get("antennaHeightA"), query.get("amslB"), query.get("antennaHeightB")]
  }

  return (
    <MainView />
  );
});


const LoSRouterApp = withRouter(connect(mapProps, mapDisp)((props: RouteComponentProps & Connect<typeof mapProps, typeof mapDisp>) => {

  return (
    <Switch>
      <Route path={`${props.match.path}`} component={LineOfSightApplicationRouteAdapter} />
      <Redirect to={`${props.match.path}`} />
    </Switch>
  )
}));

export function register() {
  applicationManager.registerApplication({
    name: "lineOfSight", // used as name of state as well
    icon: faRoute,
    rootActionHandler: lineofSightRootHandler,
    rootComponent: LoSRouterApp,
    menuEntry: "Line of Sight"
  });
}

