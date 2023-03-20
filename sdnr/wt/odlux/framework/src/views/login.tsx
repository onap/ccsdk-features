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
import React, { FC, useEffect, useState } from 'react';
import { RouteComponentProps, withRouter } from 'react-router-dom';

import Alert from '@mui/material/Alert';
import Avatar from '@mui/material/Avatar';
import Button from '@mui/material/Button';
import CssBaseline from '@mui/material/CssBaseline';
import FormControl from '@mui/material/FormControl';
import Input from '@mui/material/Input';
import InputLabel from '@mui/material/InputLabel';
import Paper from '@mui/material/Paper';
import { Theme } from '@mui/material/styles';
import Typography from '@mui/material/Typography';

import { makeStyles } from '@mui/styles';

import { useApplicationDispatch, useSelectApplicationState } from '../flux/connect';
import authenticationService from '../services/authenticationService';

import { loginUserAction, UpdatePolicies } from '../actions/authentication';
import { updateExternalLoginProviderAsyncActionCreator } from '../actions/loginProvider';

import { AuthPolicy, AuthToken, User } from '../models/authentication';

const loginIcon = require('../assets/icons/User.svg');

const styles = makeStyles((theme: Theme) =>{
  return{
  layout: {
    width: 'auto',
    display: 'block', // Fix IE11 issue.
    marginLeft: theme.spacing(3),
    marginRight: theme.spacing(3),
    [theme.breakpoints.up(400 + Number(theme.spacing(3).replace('px','')) * 2)]: {
      width: 400,
      marginLeft: 'auto',
      marginRight: 'auto',
    },
  },
  paper: {
    marginTop: theme.spacing(8),
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    padding: `${theme.spacing(2)} ${theme.spacing(3)} ${theme.spacing(3)}`,
  },
  avatar: {
    margin: theme.spacing(1),
    backgroundColor: theme.palette.secondary.main,
  },
  form: {
    width: '100%', // Fix IE11 issue.
    marginTop: theme.spacing(1),
  },
  submit: {
    marginTop: theme.spacing(3),
  },
  lineContainer:{
    width: '100%',
    height: 10,
    borderBottom: '1px solid grey',
    textAlign: 'center',
    marginTop:15,
    marginBottom:5
  },
  thirdPartyDivider:{
    fontSize: 15,
     backgroundColor: 'white',
     padding: '0 10px',
     color: 'grey'
  }
};
});


type LoginProps = RouteComponentProps;

// todo: ggf. redirect to einbauen
const LoginComponent:  FC<LoginProps> = (props) => {

  const search = useSelectApplicationState(state => state.framework.navigationState.search);
  const authentication = useSelectApplicationState(state => state.framework.applicationState.authentication);
  const externalLoginProviders = useSelectApplicationState(state => state.framework.applicationState.externalLoginProviders);
  
  const dispatch = useApplicationDispatch();
  const updateExternalProviders = () => dispatch(updateExternalLoginProviderAsyncActionCreator());
  const updateAuthentication = (token: AuthToken | null) => {
    const user = token && new User(token) || undefined;
    dispatch(loginUserAction(user));
  }
  const updatePolicies = (policies?: AuthPolicy[]) => {
    return dispatch(new UpdatePolicies(policies));
  }

  const [isBusy, setBusy] = useState(false);
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [scope, setScope] = useState("sdn");
  const [message, setMessage] = useState("");
  const [isServerReady, setIsServerReady] = useState(false);

  useEffect(()=>{
     if (authentication === "oauth" && (externalLoginProviders == null || externalLoginProviders.length === 0)){
       updateExternalProviders();
     }

    authenticationService.getServerReadyState().then(result =>{
      setIsServerReady(result);
    })
  },[]);

  const onSignIn = async (event: React.MouseEvent<HTMLButtonElement>) => {
    event.preventDefault();
  
    setBusy(true);

    const token = authentication === "oauth" 
      ? await authenticationService.authenticateUserOAuth(username, password, scope)
      : await authenticationService.authenticateUserBasicAuth(username, password, scope); 

    updateAuthentication(token);
    setBusy(false);

    if (token) {
      const query = search && search.replace(/^\?/, "").split('&').map(e => e.split("="));
      const returnTo = query && query.find(e => e[0] === "returnTo");
      props.history.replace(returnTo && returnTo[1] || "/");
    }
    else {

      if(!isServerReady){
        const ready = await authenticationService.getServerReadyState();
        if(ready){
          setIsServerReady(true);
        }else{
          setMessage("Login is currently not possible. Please re-try in a few minutes. If the problem persists, ask your administrator for assistance.");
        }
  
      }else{
        setMessage("Could not log in. Please check your credentials or ask your administrator for assistance.");
        setPassword("");
      }
    }
  }
  
  const classes = styles();
  const areProvidersAvailable = externalLoginProviders && externalLoginProviders.length > 0;

  return (
    <>
      <CssBaseline />
      <main className={classes.layout}>
        <Paper className={classes.paper}>
          <Avatar className={classes.avatar}>
            <img src={loginIcon} alt="loginIcon" />
          </Avatar>
          <Typography variant="caption">Sign in</Typography>
          <form className={classes.form}>
            {areProvidersAvailable &&
              <>
                {
                  externalLoginProviders!.map((provider, index) => (
                    <Button
                      aria-controls="externalLogin"
                      aria-label={"external-login-identity-provider-" + (index + 1)}
                      aria-haspopup="true"
                      fullWidth
                      variant="contained"
                      color="inherit"
                      className={classes.submit} onClick={() => { window.location = provider.loginUrl as any; }}>
                      {provider.title}
                    </Button>))
                }
                <div className={classes.lineContainer}>
                  <span className={classes.thirdPartyDivider}>
                    OR
                  </span>
                </div>
              </>
            }
            <FormControl variant="standard" margin="normal" required fullWidth>
              <InputLabel htmlFor="username">Username</InputLabel>
              <Input id="username" name="username" autoComplete="username" autoFocus
                disabled={isBusy}
                value={username}
                onChange={event => { setUsername(event.target.value); }} />
            </FormControl>
            <FormControl variant="standard" margin="normal" required fullWidth>
              <InputLabel htmlFor="password">Password</InputLabel>
              <Input
                name="password"
                type="password"
                id="password"
                autoComplete="current-password"
                disabled={isBusy}
                value={password}
                onChange={event => { setPassword(event.target.value); }}
              />
            </FormControl>
            <FormControl variant="standard" margin="normal" required fullWidth>
              <InputLabel htmlFor="password">Domain</InputLabel>
              <Input
                name="scope"
                type="scope"
                id="scope"
                disabled={isBusy}
                value={scope}
                onChange={event => { setScope(event.target.value); }}
              />
            </FormControl>
            <Button
              aria-label="login-button"
              type="submit"
              fullWidth
              variant="contained"
              color="inherit"
              disabled={isBusy}
              className={classes.submit}
              onClick={onSignIn}
            >
              Sign in
            </Button>

          </form>
          {message && <Alert severity="error">{message}</Alert>}
        </Paper>
      </main>
    </>
  );
}

export const Login = withRouter(LoginComponent);
export default Login;