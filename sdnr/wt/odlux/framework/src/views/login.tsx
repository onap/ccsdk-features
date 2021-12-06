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
import { withRouter, RouteComponentProps } from 'react-router-dom';

import Alert from '@material-ui/lab/Alert';
import Avatar from '@material-ui/core/Avatar';
import Button from '@material-ui/core/Button';
import CssBaseline from '@material-ui/core/CssBaseline';
import FormControl from '@material-ui/core/FormControl';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Checkbox from '@material-ui/core/Checkbox';
import Input from '@material-ui/core/Input';
import InputLabel from '@material-ui/core/InputLabel';
import LockIcon from '@material-ui/icons/LockOutlined';
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import { withStyles, WithStyles, createStyles, Theme } from '@material-ui/core/styles';

import connect, { Connect, IDispatcher } from '../flux/connect';
import authenticationService from '../services/authenticationService';

import { updateExternalLoginProviderAsyncActionCreator } from '../actions/loginProvider';
import { loginUserAction, UpdatePolicies } from '../actions/authentication';

import { IApplicationStoreState } from '../store/applicationStore';
import { AuthPolicy, AuthToken, User } from '../models/authentication';
import Menu from '@material-ui/core/Menu';
import { MenuItem } from '@material-ui/core';

const styles = (theme: Theme) => createStyles({
  layout: {
    width: 'auto',
    display: 'block', // Fix IE11 issue.
    marginLeft: theme.spacing(3),
    marginRight: theme.spacing(3),
    [theme.breakpoints.up(400 + theme.spacing(3) * 2)]: {
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
    padding: `${theme.spacing(2)}px ${theme.spacing(3)}px ${theme.spacing(3)}px`,
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
});

const mapProps = (state: IApplicationStoreState) => ({
  search: state.framework.navigationState.search,
  authentication: state.framework.applicationState.authentication,
  externalLoginProviders: state.framework.applicationState.externalLoginProviders ,
});

const mapDispatch = (dispatcher: IDispatcher) => ({
  updateExternalProviders: () => dispatcher.dispatch(updateExternalLoginProviderAsyncActionCreator()),
  updateAuthentication: (token: AuthToken | null) => {
    const user = token && new User(token) || undefined;
    dispatcher.dispatch(loginUserAction(user));
  },
  updatePolicies: (policies?: AuthPolicy[]) => {
    return dispatcher.dispatch(new UpdatePolicies(policies));
  },
});

type LoginProps = RouteComponentProps<{}> & WithStyles<typeof styles> & Connect<typeof mapProps, typeof mapDispatch>;

interface ILoginState {
  externalProviderAnchor: HTMLElement | null;
  busy: boolean;
  username: string;
  password: string;
  scope: string;
  message: string;
  providers: {
    id: string;
    title: string;
    loginUrl: string;
  }[] | null;
}


// todo: ggf. redirect to einbauen
class LoginComponent extends React.Component<LoginProps, ILoginState> {

  constructor(props: LoginProps) {
    super(props);

    this.state = {
      externalProviderAnchor: null,
      busy: false,
      username: '',
      password: '',
      scope: 'sdn',
      message: '',
      providers: null,
    };
  }

  async componentDidMount(){
     if (this.props.authentication === "oauth" && (this.props.externalLoginProviders == null || this.props.externalLoginProviders.length === 0)){
       this.props.updateExternalProviders();
     }
  }

  private setExternalProviderAnchor = (el: HTMLElement | null) => {
    this.setState({externalProviderAnchor: el })
  }

  render(): JSX.Element {
    const { classes } = this.props;
    const areProvidersAvailable = this.props.externalLoginProviders && this.props.externalLoginProviders.length > 0;
    return (
      <>
        <CssBaseline />
        <main className={classes.layout}>
          <Paper className={classes.paper}>
            <Avatar className={classes.avatar}>
              <LockIcon />
            </Avatar>
            <Typography variant="caption">Sign in</Typography>
            <form className={classes.form}>


              {areProvidersAvailable &&
                <>
                  {
                    this.props.externalLoginProviders!.map((provider, index) => (
                      <Button
                        aria-controls="externalLogin"
                        aria-label={"external-login-identity-provider-" + (index + 1)}
                        aria-haspopup="true"
                        fullWidth
                        variant="contained"
                        color="primary"
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

              <FormControl margin="normal" required fullWidth>
                <InputLabel htmlFor="username">Username</InputLabel>
                <Input id="username" name="username" autoComplete="username" autoFocus
                  disabled={this.state.busy}
                  value={this.state.username}
                  onChange={event => { this.setState({ username: event.target.value }) }} />
              </FormControl>
              <FormControl margin="normal" required fullWidth>
                <InputLabel htmlFor="password">Password</InputLabel>
                <Input
                  name="password"
                  type="password"
                  id="password"
                  autoComplete="current-password"
                  disabled={this.state.busy}
                  value={this.state.password}
                  onChange={event => { this.setState({ password: event.target.value }) }}
                />
              </FormControl>
              <FormControl margin="normal" required fullWidth>
                <InputLabel htmlFor="password">Domain</InputLabel>
                <Input
                  name="scope"
                  type="scope"
                  id="scope"
                  disabled={this.state.busy}
                  value={this.state.scope}
                  onChange={event => { this.setState({ scope: event.target.value }) }}
                />
              </FormControl>
              <Button
                aria-label="login-button"
                type="submit"
                fullWidth
                variant="contained"
                color="primary"
                disabled={this.state.busy}
                className={classes.submit}
                onClick={this.onSignIn}
              >
                Sign in
              </Button>

            </form>
            {this.state.message && <Alert severity="error">{this.state.message}</Alert>}
          </Paper>
        </main>
      </>
    );
  }

  private onSignIn = async (event: React.MouseEvent<HTMLButtonElement>) => {
    event.preventDefault();

    this.setState({ busy: true });

    const token = this.props.authentication === "oauth" 
      ? await authenticationService.authenticateUserOAuth(this.state.username, this.state.password, this.state.scope)
      : await authenticationService.authenticateUserBasicAuth(this.state.username, this.state.password, this.state.scope); 

    this.props.updateAuthentication(token);
    this.setState({ busy: false });

    if (token) {
      const query = this.props.search && this.props.search.replace(/^\?/, "").split('&').map(e => e.split("="));
      const returnTo = query && query.find(e => e[0] === "returnTo");
      this.props.history.replace(returnTo && returnTo[1] || "/");
    }
    else {
      this.setState({
        message: "Could not log in. Please check your credentials or ask your administrator for assistence.",
        password: ""
      })
    }
  }
}

export const Login = withStyles(styles)(withRouter(connect(mapProps, mapDispatch)(LoginComponent)));
export default Login;