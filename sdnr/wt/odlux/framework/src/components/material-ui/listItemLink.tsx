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
import { NavLink, Link, Route } from 'react-router-dom';

import ListItem from '@mui/material/ListItem';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';

import { Theme } from '@mui/material/styles';
import { WithStyles } from '@mui/styles';
import withStyles from '@mui/styles/withStyles';
import createStyles from '@mui/styles/createStyles';
import { toAriaLabel } from '../../utilities/yangHelper';
import { IconType } from '../../models/iconDefinition';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

const styles = (theme: Theme) => createStyles({
  active: {
    backgroundColor: theme.palette.action.selected
  }
});

export interface IListItemLinkProps extends WithStyles<typeof styles> {
  icon: IconType | null;
  primary: string | React.ComponentType;
  secondary?: React.ComponentType;
  to: string;
  exact?: boolean;
  external?: boolean;
}

export const ListItemLink = withStyles(styles)((props: IListItemLinkProps) => {
  const { icon, primary: Primary, secondary: Secondary, classes, to, exact = false, external=false } = props;
  const renderLink = (itemProps: any): JSX.Element => (
    props.external ? <a target="_blank" href={to} { ...itemProps }></a> :
  <NavLink exact={ exact } to={ to } activeClassName={ classes.active } { ...itemProps } />);
  
  const customIconHeight = 22;
  const ariaLabel = typeof Primary === 'string' ? toAriaLabel("link-to-"+Primary) : toAriaLabel("link-to-"+Primary.displayName);
  
  //create menu icon, either using an faIcon or a link to a custom svg icon
  //moved to one place for easier usage
  const listItemIcon = icon && ( typeof icon === 'string' ? <img height={customIconHeight} src={icon} /> : <FontAwesomeIcon icon={icon} /> );
    
  return (
       <>
        <ListItem button component={ renderLink } aria-label={ariaLabel}>
          { icon
            ? <ListItemIcon>{ listItemIcon }</ListItemIcon>
            : null
          }
        { typeof Primary === 'string'
          ? <ListItemText primary={ Primary } style={{ padding: 0 }} /> 
          : <Primary />
          }
        </ListItem>
        { Secondary 
          ? <Route exact={ exact } path={ to } component={ Secondary } />
          : null
        }
      </>
    );
  }
);

export default ListItemLink;

