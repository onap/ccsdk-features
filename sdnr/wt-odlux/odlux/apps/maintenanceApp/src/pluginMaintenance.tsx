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

import React, { FC } from 'react';

import applicationManager from '../../../framework/src/services/applicationManager';

import { maintenanceAppRootHandler } from './handlers/maintenanceAppRootHandler';

import { MaintenanceView } from './views/maintenanceView';

const appIcon = require('./assets/icons/maintenanceAppIcon.svg');  // select app icon

const App : FC = () => {
  return <MaintenanceView />;
};

export function register() {
  applicationManager.registerApplication({
    name: 'maintenance',
    icon: appIcon,
    rootComponent: App,
    rootActionHandler: maintenanceAppRootHandler,
    menuEntry: 'Maintenance',
  });
}


