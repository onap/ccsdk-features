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
import { faMapMarked } from '@fortawesome/free-solid-svg-icons'; // select app icon
import applicationManager from '../../../framework/src/services/applicationManager';


import { networkmapRootHandler } from './handlers/rootReducer';
import MainView from "./App";
import { subscribe, IFormatedMessage } from "../../../framework/src/services/notificationService";
import applicationApi from "../../../framework/src/services/applicationApi";
import { UpdateDetailsView } from "./actions/detailsAction";
import { findSiteToAlarm } from "./actions/mapActions";
import { URL_BASEPATH } from "./config";
import { Redirect, Route, RouteComponentProps, Switch, withRouter } from "react-router-dom";
import CustomizationView from "./components/customize/customizationView";
import { getSettings } from "./actions/settingsAction";
import connect, { Connect, IDispatcher } from "../../../framework/src/flux/connect";
import { IApplicationStoreState } from "../../../framework/src/store/applicationStore";

const mapProps = (state: IApplicationStoreState) => ({
});

const mapDisp = (dispatcher: IDispatcher) => ({
  getSettings: () => dispatcher.dispatch(getSettings())

});


const NetworkRouterApp = withRouter(connect(mapProps, mapDisp)((props: RouteComponentProps & Connect<typeof mapProps, typeof mapDisp>) => {

  React.useLayoutEffect(() => {
    (async function waitFor() {
      await  props.getSettings();
    })();
   
  }, []);

  //props.history.action = "POP";
  return (
    <Switch>
      <Route path={`${props.match.path}/customize`} component={CustomizationView} />
      <Route path={`${props.match.path}`} component={MainView} />
      <Redirect to={`${props.match.path}`} />
    </Switch>
  )
}));

export function register() {
  applicationManager.registerApplication({
    name: URL_BASEPATH, // used as name of state as well
    icon: faMapMarked,
    rootActionHandler: networkmapRootHandler,
    rootComponent: NetworkRouterApp,
    menuEntry: "Network Map"
  });
}

type ObjectNotification = {
  counter: string;
  nodeName: string;
  objectId: string;
  timeStamp: string;
}

type FaultAlarmNotification = {
  id: string;
  nodeName: string;
  counter: number;
  timeStamp: string;
  objectId: string;
  problem: string;
  severity: null | 'Warning' | 'Minor' | 'Major' | 'Critical';
  type: string;
  sourceType: string;
}
/*
// subscribe to the websocket notifications from connect
subscribe<ObjectNotification & IFormatedMessage>(["ObjectCreationNotification", "ObjectDeletionNotification", "AttributeValueChangedNotification"], (msg => {
  const store = applicationApi.applicationStore;
  
 //store && store.dispatch(UpdateDetailsView(msg.nodeName))

}));
*/

/*
subscribe<FaultAlarmNotification & IFormatedMessage>("ProblemNotification", (fault => {
  const store = applicationApi && applicationApi.applicationStore;
  if (fault && store) {
    store.dispatch(findSiteToAlarm(fault.nodeName));


  }
}));*/

