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
import React, { FC, memo } from 'react';
import { Router, Route, Redirect, Switch } from 'react-router-dom';

import { Theme } from '@mui/material/styles';
import { makeStyles } from '@mui/styles';

import { SnackbarProvider } from 'notistack';
import { ConfirmProvider } from 'material-ui-confirm';

import AppFrame from '../components/routing/appFrame';
import TitleBar from '../components/titleBar';
import Menu from '../components/navigationMenu';
import ErrorDisplay from '../components/errorDisplay';
import SnackDisplay from '../components/material-ui/snackDisplay';

import Home from '../views/home';
import Login from '../views/login';
import About from '../views/about';
import Test from '../views/test';
import UserSettings from '../views/settings';

import applicationService from '../services/applicationManager';

import { history } from '../middleware/navigation';

const aboutIcon = require('../assets/icons/About.svg');
const homeIcon = require('../assets/icons/Home.svg');
const loginIcon = require('../assets/icons/User.svg');
const settingsIcon = require('../assets/icons/Tools.svg');


const styles = makeStyles((theme: Theme) => {

  return {
  root: {
    flexGrow: 1,
    height: '100%',
    zIndex: 1,
    overflow: 'hidden',
    position: 'relative',
    display: 'flex',
  },
  content: {
    flexGrow: 1,
    display: "flex",
    flexDirection: "column",
    backgroundColor: '#fafafa',
    padding: theme.spacing(3),
    minWidth: 0, // So the Typography noWrap works
  },
  toolbar: theme.mixins.toolbar as any
  };
});

const FrameComponent: FC = memo(() => {

  const registrations = applicationService.applications;
  const classes = styles();
  return (
    <ConfirmProvider>
      <SnackbarProvider maxSnack={3}>
        <Router history={history as any} >
          <div className={classes.root}>
            <SnackDisplay />
            <ErrorDisplay />
            <TitleBar />
            <Menu />
            <main className={classes.content}>
              {
                <div className={classes.toolbar} /> //needed for margins, don't remove!
              }
              <Switch>
                <Route exact path="/" component={() => (
                  <AppFrame title={"Home"} icon={homeIcon} >
                    <Home />
                  </AppFrame>
                )} />
                <Route path="/about" component={() => (
                  <AppFrame title={"About"} icon={aboutIcon} >
                    <About />
                  </AppFrame>
                )} />
                <Route path="/settings" component={() => (
                  <AppFrame title={"Settings"} icon={settingsIcon} >
                    <UserSettings />
                  </AppFrame>
                )} />
                {process.env.NODE_ENV === "development" ? <Route path="/test" component={() => (
                  <AppFrame title={"Test"} icon={settingsIcon} >
                    <Test />
                  </AppFrame>
                )} /> : null}
                <Route path="/login" component={() => (
                  <AppFrame title={"Login"} icon={loginIcon} >
                    <Login />
                  </AppFrame>
                )} />
                {Object.keys(registrations).map(p => {
                  const application = registrations[p];
                  return (<Route key={application.name} path={application.path || `/${application.name}`} component={() => (
                    <AppFrame title={application.title || (typeof application.menuEntry === 'string' && application.menuEntry) || application.name} icon={application.icon} appId={application.name} >
                      <application.rootComponent />
                    </AppFrame>
                  )} />)
                })}
                <Redirect to="/" />
              </Switch>
            </main>
          </div>
        </Router>
      </SnackbarProvider>
    </ConfirmProvider>
  );
});

export const Frame = FrameComponent;
export default Frame;
