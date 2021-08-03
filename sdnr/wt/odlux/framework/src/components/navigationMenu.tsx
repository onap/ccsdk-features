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
import { faProjectDiagram } from '@fortawesome/free-solid-svg-icons';

import ListItemLink from '../components/material-ui/listItemLink';

import connect, { Connect } from '../flux/connect';
import { MenuAction } from '../actions/menuAction';
import * as classNames from 'classnames';
import { transportPCEUrl } from '../app';


const drawerWidth = 240;

const extraLinks = (window as any)._odluxExtraLinks as [string, string][];

const styles = (theme: Theme) => createStyles({
  drawerPaper: {
    position: 'relative',
    width: drawerWidth,
  },
  toolbar: theme.mixins.toolbar as any,

  drawerOpen: {
    width: drawerWidth,
    transition: theme.transitions.create('width', {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.enteringScreen,
    }),
  },
  drawerClose: {
    transition: theme.transitions.create('width', {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.leavingScreen,
    }),
    overflowX: 'hidden',
    width: theme.spacing(7) + 1,
    [theme.breakpoints.up('sm')]: {
      width: theme.spacing(9) + 1,
    },
  },
  drawer: {

  },
  menu: {
    flex: "1 0 0%",
  },
  optLinks: {
    borderTop: "2px solid #cfcfcf",
    display: "flex",
    flexDirection: "row",
    flexWrap: "wrap",
    justifyContent: "space-around"
  },
  link: {
    margin: theme.spacing(1)+1,
    fontSize: theme.typography.fontSize-2,
  },
});

const tabletWidthBreakpoint = 768;

export const NavigationMenu = withStyles(styles)(connect()(({ classes, state, dispatch }: WithStyles<typeof styles> & Connect & Connect) => {
  const { user } = state.framework.authenticationState;
  const isOpen = state.framework.applicationState.isMenuOpen;
  const closedByUser = state.framework.applicationState.isMenuClosedByUser;
  const transportUrl = state.framework.applicationState.transportpceUrl;

  const [responsive, setResponsive] = React.useState(false);

  React.useEffect(() => {

    function handleResize() {
      if (user && user.isValid) {
        if (window.innerWidth < tabletWidthBreakpoint && !responsive) {
          setResponsive(true);
          if (!closedByUser) {
            console.log("responsive menu collapsed")
            dispatch(new MenuAction(false));
          }

        } else if (window.innerWidth > tabletWidthBreakpoint && responsive) {
          setResponsive(false);
          if (!closedByUser) {
            console.log("responsive menu restored")
            dispatch(new MenuAction(true));
          }

        }
      }
    }
    window.addEventListener("resize", handleResize);


    return () => {
      window.removeEventListener("resize", handleResize);
    }
  })

  React.useEffect(()=>{
    // trigger a resize if menu changed in case elements have to re-arrange
    window.dispatchEvent(new Event('menu-resized'));
  }, [isOpen])

  let menuItems = state.framework.applicationRegistraion && Object.keys(state.framework.applicationRegistraion).map(key => {
    const reg = state.framework.applicationRegistraion[key];
    return reg && (
      <ListItemLink
        key={reg.name}
        to={reg.path || `/${reg.name}`}
        primary={reg.menuEntry || reg.name}
        secondary={reg.subMenuEntry}
        icon={reg.icon && <FontAwesomeIcon icon={reg.icon} /> || null} />
    ) || null;
  }) || null;

  if(transportUrl.length>0){

    const transportPCELink = <ListItemLink
      key={"transportPCE"}
      to={transportUrl}
      primary={"TransportPCE"}
      icon={<FontAwesomeIcon icon={faProjectDiagram} />}
      external />;

    const linkFound = menuItems.find(obj => obj.key === "linkCalculation");
    
    if (linkFound) {
      const index = menuItems.indexOf(linkFound);
      menuItems.splice(index + 1, 0, transportPCELink);
    } else {
      menuItems.push(transportPCELink);
    }
  }
  

  return (
    <Drawer
      variant="permanent"
      className={
        classNames(classes.drawer, {
          [classes.drawerOpen]: isOpen,
          [classes.drawerClose]: !isOpen
        })
      }
      classes={{
        paper: classes.drawerPaper,
      }}
    >
      {user && user.isValid && <>
        <div className={classes.toolbar} />
        { /* https://fiffty.github.io/react-treeview-mui/ */}
        <List className={classes.menu} component="nav">
          <ListItemLink exact to="/" primary="Home" icon={<FontAwesomeIcon icon={faHome} />} />
          <Divider />
          {
          menuItems
          }
          <Divider />
          <ListItemLink to="/about" primary="About" icon={<FontAwesomeIcon icon={faAddressBook} />} />
          {(false && process.env.NODE_ENV === "development")
            ? <>
              <Divider />
              <ListItemLink to="/test" primary="Test" icon={<FontAwesomeIcon icon={faHome} />} />
            </>
            : null
          }
        </List>
        {isOpen && extraLinks && <div className={classes.optLinks}>
          {extraLinks.map(linkInfo => (<a className={classes.link} href={linkInfo[1]}>{linkInfo[0]}</a>))}
        </div> || null}
      </> || null
      }
    </Drawer>)
}));

export default NavigationMenu;