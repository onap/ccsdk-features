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
import { withStyles, WithStyles, createStyles, Theme } from '@material-ui/core/styles';

import { faHome, faAddressBook } from '@fortawesome/free-solid-svg-icons';

import Drawer from '@material-ui/core/Drawer';
import List from '@material-ui/core/List';

import Divider from '@material-ui/core/Divider';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import ListItemLink from '../components/material-ui/listItemLink';

import connect, { Connect } from '../flux/connect';

const drawerWidth = 240;

const styles = (theme: Theme) => createStyles({
  drawerPaper: {
    position: 'relative',
    width: drawerWidth,
  },
  toolbar: theme.mixins.toolbar
});

export const NavigationMenu = withStyles(styles)(connect()(({ classes, state }: WithStyles<typeof styles> & Connect) => {
  const { user } = state.framework.authenticationState
  return (
    <Drawer
      variant="permanent"
      classes={{
        paper: classes.drawerPaper,
      }}
    >
      {user && user.isValid && <>
        <div className={classes.toolbar} />
      { /* https://fiffty.github.io/react-treeview-mui/ */}
      <List component="nav">
          <ListItemLink exact to="/" primary="Home" icon={<FontAwesomeIcon icon={faHome} />} />
          <Divider />
        {
          state.framework.applicationRegistraion && Object.keys(state.framework.applicationRegistraion).map(key => {
            const reg = state.framework.applicationRegistraion[key];
            return reg && (
              <ListItemLink
                key={reg.name}
                to={reg.path || `/${reg.name}`}
                primary={reg.menuEntry || reg.name}
                secondary={reg.subMenuEntry}
                icon={reg.icon && <FontAwesomeIcon icon={reg.icon} /> || null} />
            ) || null;
          }) || null
        }
        <Divider />
        <ListItemLink to="/about" primary="About" icon={<FontAwesomeIcon icon={faAddressBook} />} />
        </List>
        </> || null
      }
    </Drawer>)
}));

export default NavigationMenu;