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

import { withStyles, WithStyles, createStyles, Theme } from '@material-ui/core/styles';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import IconButton from '@material-ui/core/IconButton';
import MenuIcon from '@material-ui/icons/Menu';
import AccountCircle from '@material-ui/icons/AccountCircle';
import MenuItem from '@material-ui/core/MenuItem';
import Menu from '@material-ui/core/Menu';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { UpdateAuthentication } from '../actions/authentication';
import { ReplaceAction } from '../actions/navigationActions';

import connect, { Connect, IDispatcher } from '../flux/connect';
import Logo from './logo';

const styles = (theme: Theme) => createStyles({
  appBar: {
    zIndex: theme.zIndex.drawer + 1,
  },
  grow: {
    flexGrow: 1,
  },
  menuButton: {
    marginLeft: -12,
    marginRight: 20,
  },
  icon: {
    marginLeft: 16,
    marginRight: 8
  }
});

const mapDispatch = (dispatcher: IDispatcher) => {
  return {
    logout: () => {
      dispatcher.dispatch(new UpdateAuthentication(null));
      dispatcher.dispatch(new ReplaceAction("/login"));
    }
  }
};

type TitleBarProps = RouteComponentProps<{}> & WithStyles<typeof styles> & Connect<undefined, typeof mapDispatch>

class TitleBarComponent extends React.Component<TitleBarProps, { anchorEl: HTMLElement | null }> {

  constructor(props: TitleBarProps) {
    super(props);

    this.state = {
      anchorEl: null
    }

  }
  render(): JSX.Element {
    const { classes, state, history, location } = this.props;
    const open = !!this.state.anchorEl;

    return (
      <AppBar position="absolute" className={ classes.appBar }>
        <Toolbar>
          <IconButton className={ classes.menuButton } color="inherit" aria-label="Menu">
            <MenuIcon />
          </IconButton>
          <Logo />
          <Typography variant="title" color="inherit" >
            { state.framework.applicationState.icon
              ? (<FontAwesomeIcon className={ classes.icon } icon={ state.framework.applicationState.icon } />)
              : null }
            { state.framework.applicationState.title }
          </Typography>
          <div className={classes.grow}></div>
          { state.framework.applicationRegistraion && Object.keys(state.framework.applicationRegistraion).map(key => {
            const reg = state.framework.applicationRegistraion[key];
            return reg && reg.statusBarElement && <reg.statusBarElement key={key} /> || null
          })}

          { state.framework.authenticationState.user
            ? (<div>
              <Button
                aria-owns={ open ? 'menu-appbar' : undefined }
                aria-haspopup="true"
                onClick={ this.openMenu }
                color="inherit"
              >
                <AccountCircle />
                { state.framework.authenticationState.user.user }
              </Button>
              <Menu
                id="menu-appbar"
                anchorEl={ this.state.anchorEl }
                anchorOrigin={ {
                  vertical: 'top',
                  horizontal: 'right',
                } }
                transformOrigin={ {
                  vertical: 'top',
                  horizontal: 'right',
                } }
                open={ open }
                onClose={ this.closeMenu }
              >
                <MenuItem onClick={ this.closeMenu }>Profile</MenuItem>
                <MenuItem onClick={ () => {
                  this.props.logout();
                  this.closeMenu();
                } }>Logout</MenuItem>
              </Menu>
            </div>)
            : (<Button onClick={ () => { history.push('/login') } } color="inherit" disabled={ location.pathname == "/login" }>Login</Button>) }
        </Toolbar>
      </AppBar>
    );
  };


  private openMenu = (event: React.MouseEvent<HTMLElement>) => {
    this.setState({ anchorEl: event.currentTarget });
  };

  private closeMenu = () => {
    this.setState({ anchorEl: null });
  };
}

//todo: ggf. https://github.com/acdlite/recompose verwenden zur Vereinfachung

export const TitleBar = withStyles(styles)(withRouter(connect(undefined, mapDispatch)(TitleBarComponent)));
export default TitleBar;