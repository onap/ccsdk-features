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

import * as React from 'react';
import * as ReactDOM from 'react-dom';

import { ThemeProvider, Theme, StyledEngineProvider } from '@mui/material/styles';

import { Frame } from './views/frame';

import { User } from './models/authentication';

import { AddErrorInfoAction } from './actions/errorActions';
import { loginUserAction } from './actions/authentication';

import { applicationStoreCreator } from './store/applicationStore';
import { ApplicationStoreProvider } from './flux/connect';

import { startHistoryListener } from './middleware/navigation';
import { startSoreService } from './services/storeService';

import { startUserSessionService } from './services/userSessionService';
import { startNotificationService } from './services/notificationService';

import { startBroadcastChannel } from './services/broadcastService';

import theme from './design/default';
import '!style-loader!css-loader!./app.css';

declare module '@mui/material/styles' {

  interface IDesign {
    id: string,
    name: string,
    url: string,        // image url of a company logo, which will be presented in the ui header
    height: number,     // image height [px] as delivered by the url
    width: number,      // image width [px] as delivered by the url
    logoHeight: number  // height in [px] of the logo (see url) within the ui header
  }

  interface Theme {
    design?: IDesign
  }
  interface DeprecatedThemeOptions {
    design?: IDesign
  }
}


declare module '@mui/styles/defaultTheme' {
  // eslint-disable-next-line @typescript-eslint/no-empty-interface (remove this line if you don't have the rule enabled)
  interface DefaultTheme extends Theme {}
}

export { configureApplication } from "./handlers/applicationStateHandler";

export const transportPCEUrl = "transportPCEUrl";

export const runApplication = () => {
  
  const initialToken = localStorage.getItem("userToken");
  const applicationStore = applicationStoreCreator();

  startBroadcastChannel(applicationStore);
  startUserSessionService(applicationStore);
  
  if (initialToken) {
    applicationStore.dispatch(loginUserAction(User.fromString(initialToken) || undefined));
  }

  window.onerror = function (msg: string, url: string, line: number, col: number, error: Error) {
    // Note that col & error are new to the HTML 5 spec and may not be
    // supported in every browser.  It worked for me in Chrome.
    var extra = !col ? '' : '\ncolumn: ' + col;
    extra += !error ? '' : '\nerror: ' + error;

    // You can view the information in an alert to see things working like this:
    applicationStore.dispatch(new AddErrorInfoAction({ error, message: msg, url, line, col, info: { extra } }));

    var suppressErrorAlert = true;
    // If you return true, then error alerts (like in older versions of
    // Internet Explorer) will be suppressed.
    return suppressErrorAlert;
  };
  

  startSoreService(applicationStore);
  startHistoryListener(applicationStore);
  startNotificationService(applicationStore);

  const App = (): JSX.Element => (
    <ApplicationStoreProvider applicationStore={applicationStore} >
      <StyledEngineProvider injectFirst>
        <ThemeProvider theme={theme}>
          <Frame />
        </ThemeProvider>
      </StyledEngineProvider>
    </ApplicationStoreProvider>
  );

  ReactDOM.render(<App />, document.getElementById('app'));

  

};
