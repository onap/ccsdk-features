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
import { withRouter, RouteComponentProps } from 'react-router-dom';

import { Theme } from '@mui/material/styles';
import { WithStyles } from '@mui/styles';
import withStyles from '@mui/styles/withStyles';
import createStyles from '@mui/styles/createStyles';
import AppBar from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import IconButton from '@mui/material/IconButton';
import AccountCircle from '@mui/icons-material/AccountCircle';
import MenuItem from '@mui/material/MenuItem';
import Menu from '@mui/material/Menu';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faBan } from '@fortawesome/free-solid-svg-icons';
import { faDotCircle } from '@fortawesome/free-solid-svg-icons';

import { logoutUser } from '../actions/authentication';
import { PushAction, ReplaceAction } from '../actions/navigationActions';

import { connect, Connect, IDispatcher } from '../flux/connect';
import { MenuAction, MenuClosedByUser } from '../actions/menuAction';

import MenuIcon from './icons/menuIcon';
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
    marginRight: 8,
    marginBottom: -2,
  },
  connected: {
    color: "green"
  },
  notConnected: {
    color: "red"
  },
  notificationInfo: {
    marginLeft: 5
  }
});

const mapDispatch = (dispatcher: IDispatcher) => {
  return {
    logout: () => {
      dispatcher.dispatch(logoutUser());
      dispatcher.dispatch(new ReplaceAction("/login"));
    },
    openSettings : () =>{
      dispatcher.dispatch(new PushAction("/settings"));
    },
    toggleMainMenu: (value: boolean, value2: boolean) => {
      dispatcher.dispatch(new MenuAction(value));
      dispatcher.dispatch(new MenuClosedByUser(value2))
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
    let toolbarElements: Array<JSX.Element>;
    toolbarElements = [];

    // create notificationInfo element
    const notificationInfo = state.framework.applicationState.isWebsocketAvailable != undefined ?
      (state.framework.applicationState.isWebsocketAvailable ?
        <Typography aria-label="notifications-are-active" variant="body1" className={classes.notificationInfo}>Notifications <FontAwesomeIcon className={classes.connected} icon={faDotCircle} />  |</Typography> : <Typography aria-label="notifications-are-inactive" variant="body1" className={classes.notificationInfo}>Notifications <FontAwesomeIcon className={classes.notConnected} icon={faBan} /> |</Typography>)
      : <Typography variant="body1" aria-label="notifications-are-not-available" className={classes.notificationInfo}>Notifications N/A |</Typography>;


    // add notificationInfo element before help
    if (state.framework.applicationRegistration) {
      let isNotificationInfoAdded = false;
      Object.keys(state.framework.applicationRegistration).map(key => {
        const reg = state.framework.applicationRegistration[key];
        if (reg && reg.statusBarElement) {
          if (key === "help") {
            isNotificationInfoAdded = true;
            toolbarElements.push(notificationInfo);
          }
          toolbarElements.push(<reg.statusBarElement key={key} />);
        }
      });

      // add notificationInfo in case help wasn't found
      if (!isNotificationInfoAdded) {
        toolbarElements.push(notificationInfo);
      }
    }

    const stateIcon = state.framework.applicationState.icon;
    const customIconHeight = 22; 
    const icon = !stateIcon
      ? null
      : (typeof stateIcon === 'string'
        ? <img className={classes.icon} height={customIconHeight} src={stateIcon} />
        : <FontAwesomeIcon className={classes.icon} icon={stateIcon} />)
    

    return (
      <AppBar enableColorOnDark position="absolute" className={classes.appBar}>
        <Toolbar>
          <IconButton
            className={classes.menuButton}
            color="inherit"
            aria-label="Menu"
            onClick={this.toggleMainMenu}
            size="large">
            <MenuIcon />
          </IconButton>
          <Logo />
          <Typography variant="h6" color="inherit" >
            {icon}
            {state.framework.applicationState.title}
          </Typography>
          <div className={classes.grow}></div>
          {
            // render toolbar
            toolbarElements.map((item) => {
              return item
            })
          }

          {state.framework.authenticationState.user
            ? (<div>
              <Button aria-label="current user menu button"
                aria-owns={open ? 'menu-appbar' : undefined}
                aria-haspopup="true"
                onClick={this.openMenu}
                color="inherit"
              >
                <AccountCircle />
                {state.framework.authenticationState.user.user}
              </Button>
              <Menu
                id="menu-appbar"
                anchorEl={this.state.anchorEl}
                anchorOrigin={{
                  vertical: 'top',
                  horizontal: 'right',
                }}
                transformOrigin={{
                  vertical: 'top',
                  horizontal: 'right',
                }}
                open={open}
                onClose={this.closeMenu}
              >
                {/* <MenuItem onClick={ this.closeMenu }>Profile</MenuItem> */}
                <MenuItem 
                 aria-label="settings-button"
                 onClick={ () =>{
                    this.props.openSettings();
                    this.closeMenu(); }}>Settings</MenuItem>
                <MenuItem
                aria-label="logout-button"
                onClick={() => {
                  this.props.logout();
                  this.closeMenu();
                }}>Logout</MenuItem>
              </Menu>
            </div>)
            : (<Button onClick={() => { history.push('/login') }} color="inherit" disabled={location.pathname == "/login"}>Login</Button>)}
        </Toolbar>
      </AppBar>
    );
  };

  private toggleMainMenu = (event: React.MouseEvent<HTMLElement>) => {
    console.log(this.props);
    if (this.props.state.framework.authenticationState.user && this.props.state.framework.authenticationState.user.isValid) {
      const isMainMenuOpen = this.props.state.framework.applicationState.isMenuOpen
      const isClosedByUser = this.props.state.framework.applicationState.isMenuClosedByUser
      this.props.toggleMainMenu(!isMainMenuOpen, !isClosedByUser);
    }
  }

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