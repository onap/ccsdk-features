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

import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';

import { withStyles, WithStyles, createStyles, Theme } from '@material-ui/core/styles';

const styles = (theme: Theme) => createStyles({
  active: {
    backgroundColor: theme.palette.action.selected
  }
});

export interface IListItemLinkProps extends WithStyles<typeof styles> {
  icon: JSX.Element | null;
  primary: string | React.ComponentType;
  secondary?: React.ComponentType;
  to: string;
  exact?: boolean;
}

export const ListItemLink = withStyles(styles)((props: IListItemLinkProps) => {
  const { icon, primary: Primary, secondary: Secondary, classes, to, exact = false } = props;
  const renderLink = (itemProps: any): JSX.Element => (<NavLink exact={ exact } to={ to } activeClassName={ classes.active } { ...itemProps } />);

  const ariaLabel = typeof Primary === 'string' ? "link-to-"+Primary.toLowerCase().replace(/\s/g, "-") : "link-to-"+Primary.displayName?.toLowerCase();
  return (
       <>
        <ListItem button component={ renderLink } aria-label={ariaLabel}>
          { icon
            ? <ListItemIcon>{ icon }</ListItemIcon>
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

