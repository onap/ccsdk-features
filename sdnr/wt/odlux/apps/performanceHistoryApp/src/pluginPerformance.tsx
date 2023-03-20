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

import React from 'react';
import { Redirect, Route, RouteComponentProps, Switch, withRouter } from 'react-router-dom';

import { connect, Connect, IDispatcher } from '../../../framework/src/flux/connect';
import applicationManager from '../../../framework/src/services/applicationManager';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { IApplicationStoreState } from '../../../framework/src/store/applicationStore';
import { ApplicationStore } from '../../../framework/src/store/applicationStore';

import { updateMountIdActionCreator } from './actions/deviceListActions';
import { ResetLtpsAction } from './actions/ltpAction';
import { ReloadAction } from './actions/reloadAction';
import { ResetAllSubViewsAction } from './actions/toggleActions';
import performanceHistoryRootHandler from './handlers/performanceHistoryRootHandler';
import { PmDataInterval } from './models/performanceDataType';
import PerformanceHistoryApplication from './views/performanceHistoryApplication';

const appIcon = require('./assets/icons/performanceHistoryAppIcon.svg');  // select app icon

let api: {
  readonly applicationStore: ApplicationStore | null;
  readonly applicationStoreInitialized: Promise<ApplicationStore>;
};

const mapProps = () => ({
});

const mapDisp = (dispatcher: IDispatcher) => ({
  updateMountId: (mountId: string) => dispatcher.dispatch(updateMountIdActionCreator(mountId)),
  resetLtps: () => dispatcher.dispatch(new ResetLtpsAction()),
  resetSubViews: () => dispatcher.dispatch(new ResetAllSubViewsAction()),
  setScheduleReload: (show: boolean) => dispatcher.dispatch(new ReloadAction(show)),
});

let currentMountId: string | null = null;
let lastUrl: string = '/performanceHistory';
const PerformanceHistoryApplicationRouteAdapter = connect(mapProps, mapDisp)((props: RouteComponentProps<{ mountId?: string }> & Connect<typeof mapProps, typeof mapDisp>) => {
  let mountId: string = '';

  const getMountId = (last_url: string) => {
    let index = last_url.lastIndexOf('performanceHistory/');
    if (index >= 0) {
      mountId = last_url.substring(index + 19);
    } else {
      mountId = '';
    }

    return mountId;
  };

  const scheduleReload = (current_mount_id: string) => {
    props.updateMountId(current_mount_id);
    props.resetLtps();
    props.resetSubViews();
    props.setScheduleReload(true);
  };
  
  // called when component finished mounting
  React.useEffect(() => {

    lastUrl = props.location.pathname;
    mountId = getMountId(lastUrl);

    if (currentMountId !== mountId) { // new element is loaded
      currentMountId = mountId;
      scheduleReload(currentMountId);
    } else
    if (currentMountId !== '') { // same element is loaded again
      scheduleReload(currentMountId);
    }
  }, []);

  // called when component gets updated
  React.useEffect(() => {

    lastUrl = props.location.pathname;
    mountId = getMountId(lastUrl);

    if (currentMountId !== mountId) {
      currentMountId = mountId;
      scheduleReload(currentMountId);
    }

  });

  return (
    <PerformanceHistoryApplication />
  );
});

const PerformanceHistoryRouterApp = withRouter((props: RouteComponentProps) => {
  props.history.action = 'POP';
  return (
    <Switch>
      <Route path={`${props.match.path}/:mountId`} component={PerformanceHistoryApplicationRouteAdapter} />
      <Route path={`${props.match.path}`} component={PerformanceHistoryApplicationRouteAdapter} />
      <Redirect to={`${props.match.path}`} />
    </Switch>
  );
});

export function register() {
  api = applicationManager.registerApplication({
    name: 'performanceHistory',
    icon: appIcon,
    rootComponent: PerformanceHistoryRouterApp,
    rootActionHandler: performanceHistoryRootHandler,
    menuEntry: 'Performance',
  });
}

export function setPmDataInterval(pmDataInterval: PmDataInterval): boolean {
  let reload: boolean = true;
  if (api && api.applicationStore) {
    if (api.applicationStore.state.performanceHistory.pmDataIntervalType !== pmDataInterval) {
      reload = true;
    }
    api.applicationStore.state.performanceHistory.pmDataIntervalType = pmDataInterval;
  }
  return reload;
}


export function getPmDataInterval(): PmDataInterval {
  let result = api && api.applicationStore
    ? api.applicationStore.state.performanceHistory.pmDataIntervalType
    : PmDataInterval.pmInterval15Min;
  return result ? result : PmDataInterval.pmInterval15Min;
}
