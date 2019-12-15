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
import { faBookOpen } from '@fortawesome/free-solid-svg-icons'; // select app icon
import applicationManager from '../../../framework/src/services/applicationManager';

import { EventLog } from './views/eventLog';
import eventLogAppRootHandler from './handlers/eventLogAppRootHandler';

const App : React.SFC = (props) => {
  return <EventLog />
};

export function register() {
  applicationManager.registerApplication({
    name: "eventLog",
    icon: faBookOpen,
    rootActionHandler: eventLogAppRootHandler,
    rootComponent: App,
    menuEntry: "EventLog"
  });
}

