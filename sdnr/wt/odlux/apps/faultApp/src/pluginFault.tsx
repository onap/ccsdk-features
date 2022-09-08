/**
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt odlux
 * =================================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
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

import connect, { Connect, IDispatcher } from '../../../framework/src/flux/connect';

import { faBell } from '@fortawesome/free-solid-svg-icons';  // select app icon
import applicationManager from '../../../framework/src/services/applicationManager';
import { subscribe, IFormatedMessage } from '../../../framework/src/services/notificationService';
import { IApplicationStoreState } from "../../../framework/src/store/applicationStore";

import { faultAppRootHandler } from './handlers/faultAppRootHandler';
import { FaultApplication } from "./views/faultApplication";

import { FaultAlarmNotificationWS } from "./models/fault";
import { PanelId } from "./models/panelId";

import { SetPanelAction } from "./actions/panelChangeActions";
import { AddFaultNotificationAction } from "./actions/notificationActions";

import { createCurrentProblemsProperties, createCurrentProblemsActions, currentProblemsReloadAction } from "./handlers/currentProblemsHandler";
import { FaultStatus } from "./components/faultStatus";
import { refreshFaultStatusAsyncAction, SetFaultStatusAction } from "./actions/statusActions";

import DashboardHome from "./components/dashboardHome";

let currentMountId: string | undefined = undefined;
let currentSeverity: string | undefined = undefined;
let refreshInterval: ReturnType<typeof window.setInterval> | null = null;

const mapProps = (state: IApplicationStoreState) => ({
  currentProblemsProperties: createCurrentProblemsProperties(state),
});

const mapDisp = (dispatcher: IDispatcher) => ({
  currentProblemsActions: createCurrentProblemsActions(dispatcher.dispatch, true),
  setCurrentPanel: (panelId: PanelId) => dispatcher.dispatch(new SetPanelAction(panelId)),
});

const FaultApplicationRouteAdapter = connect(mapProps, mapDisp)((props: RouteComponentProps<{ mountId?: string }> & Connect<typeof mapProps, typeof mapDisp>) => {
  if (currentMountId !== props.match.params.mountId) {
    // route parameter has changed
    currentMountId = props.match.params.mountId || undefined;
    // Hint: This timeout is need, since it is not recommended to change the state while rendering is in progress !
    window.setTimeout(() => {
      if (currentMountId) {
        props.setCurrentPanel("CurrentProblem");
        props.currentProblemsActions.onFilterChanged("nodeId", currentMountId);
        if (!props.currentProblemsProperties.showFilter) {
          props.currentProblemsActions.onToggleFilter(false);
          props.currentProblemsActions.onRefresh();
        }
        else
          props.currentProblemsActions.onRefresh();
      }
    });
  }
  return (
    <FaultApplication />
  )
});

const FaulttApplicationAlarmStatusRouteAdapter = connect(mapProps, mapDisp)((props: RouteComponentProps<{ severity?: string }> & Connect<typeof mapProps, typeof mapDisp>) => {
  if (currentSeverity !== props.match.params.severity) {
    currentSeverity = props.match.params.severity || undefined;
    window.setTimeout(() => {
      if (currentSeverity) {
        props.setCurrentPanel("CurrentProblem");
        props.currentProblemsActions.onFilterChanged("severity", currentSeverity);
        if (!props.currentProblemsProperties.showFilter) {
          props.currentProblemsActions.onToggleFilter(false);
          props.currentProblemsActions.onRefresh();
        }
        else
          props.currentProblemsActions.onRefresh();
      }
    });
  }
  return (
    <FaultApplication />
  )
});

const App = withRouter((props: RouteComponentProps) => (
  <Switch>
    <Route path={`${props.match.path}/alarmStatus/:severity?`} component={FaulttApplicationAlarmStatusRouteAdapter} />
    <Route path={`${props.match.path}/:mountId?`} component={FaultApplicationRouteAdapter} />
    <Redirect to={`${props.match.path}`} />
  </Switch>
));

export function register() {
  const applicationApi = applicationManager.registerApplication({
    name: "fault",
    icon: faBell,
    rootComponent: App,
    rootActionHandler: faultAppRootHandler,
    statusBarElement: FaultStatus,
    dashbaordElement: DashboardHome,
    menuEntry: "Fault"
  });

  let counter = 0;
  // subscribe to the websocket notifications
  subscribe<FaultAlarmNotificationWS & IFormatedMessage>("problem-notification", (fault => {
    const store = applicationApi && applicationApi.applicationStore;
    if (fault && store) {

      store.dispatch(new AddFaultNotificationAction({
        id: String(counter++),
        nodeName: fault["node-id"],
        counter: +fault.data.counter,
        objectId: fault.data["object-id-ref"],
        problem: fault.data.problem,
        severity: fault.data.severity || '',
        timeStamp: fault.data["time-stamp"],
      }));
    }
  }));

  applicationApi.applicationStoreInitialized.then(store => {
    store.dispatch(currentProblemsReloadAction);
  });

  applicationApi.applicationStoreInitialized.then(store => {
    store.dispatch(refreshFaultStatusAsyncAction);
  });

  applicationApi.loginEvent.addHandler(e=>{
    refreshInterval = startRefreshInterval() as any;
  })

  applicationApi.logoutEvent.addHandler(e=>{

    applicationApi.applicationStoreInitialized.then(store => {
      store.dispatch(new SetFaultStatusAction(0, 0, 0, 0, false, 0, 0, 0, 0, 0, 0, 0, 0, false));
      clearInterval(refreshInterval!);
    });
  })
  
 

  function startRefreshInterval()  {
    const refreshFaultStatus = window.setInterval(() => {
      applicationApi.applicationStoreInitialized.then(store => {
  
        store.dispatch(refreshFaultStatusAsyncAction);
      });
    }, 15000);

    return refreshFaultStatus;
  }

}
