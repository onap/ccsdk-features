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

import IconButton from '@material-ui/core/IconButton';
import Tooltip from '@material-ui/core/Tooltip';
import Toolbar from '@material-ui/core/Toolbar';
import Typography from '@material-ui/core/Typography';
import DeleteIcon from '@material-ui/icons/Delete';
import MoreIcon from '@material-ui/icons/MoreVert';
import FilterListIcon from '@material-ui/icons/FilterList';
import MenuItem from '@material-ui/core/MenuItem';
import Menu from '@material-ui/core/Menu';
import { lighten } from '@material-ui/core/styles/colorManipulator';
import { SvgIconProps } from '@material-ui/core/SvgIcon/SvgIcon';
import { Button } from '@material-ui/core';

const styles = (theme: Theme) => createStyles({
  root: {
    paddingRight: theme.spacing(1),
  },
  highlight:
    theme.palette.type === 'light'
      ? {
        color: theme.palette.secondary.main,
        backgroundColor: lighten(theme.palette.secondary.light, 0.85),
      }
      : {
        color: theme.palette.text.primary,
        backgroundColor: theme.palette.secondary.dark,
      },
  spacer: {
    flex: '1 1 100%',
  },
  actions: {
    color: theme.palette.text.secondary,
    display: "flex",
    flex: "auto",
    flexDirection: "row"
  },
  title: {
    flex: '0 0 auto',
  },
  menuButton: {
    marginLeft: -12,
    marginRight: 20,
  },
});

interface ITableToolbarComponentProps extends WithStyles<typeof styles> {
  numSelected: number | null;
  title?: string;
  tableId?: string;
  customActionButtons?: { icon: React.ComponentType<SvgIconProps>, tooltip?: string, onClick: () => void }[];
  onToggleFilter: () => void;
  onExportToCsv: () => void;
}

class TableToolbarComponent extends React.Component<ITableToolbarComponentProps, { anchorEl: EventTarget & HTMLElement | null }> {
  constructor(props: ITableToolbarComponentProps) {
    super(props);

    this.state = {
      anchorEl: null
    };
  }

  private handleMenu = (event: React.MouseEvent<HTMLElement>) => {
    this.setState({ anchorEl: event.currentTarget });
  };

  private handleClose = () => {
    this.setState({ anchorEl: null });
  };
  render() {
    const { numSelected, classes } = this.props;
    const open = !!this.state.anchorEl;
    const buttonPrefix = this.props.tableId !== undefined ? this.props.tableId + '-' : '';
    return (
      <Toolbar className={`${classes.root} ${numSelected && numSelected > 0 ? classes.highlight : ''} `} >
        <div className={classes.title}>
          {numSelected && numSelected > 0 ? (
            <Typography color="inherit" variant="subtitle1">
              {numSelected} selected
          </Typography>
          ) : (
              <Typography variant="h5" id="tableTitle">
                {this.props.title || null}
              </Typography>
            )}
        </div>
        <div className={classes.spacer} />
        <div className={classes.actions}>
          {this.props.customActionButtons
            ? this.props.customActionButtons.map((action, ind) => (
              <Tooltip key={`custom-action-${ind}`} title={action.tooltip || ''}>
                <IconButton aria-label={buttonPrefix + `custom-action-${ind}`} onClick={() => action.onClick()}>
                  <action.icon />
                </IconButton>
              </Tooltip>
            ))
            : null}
          {numSelected && numSelected > 0 ? (
            <Tooltip title="Delete">
              <IconButton aria-label={buttonPrefix + "delete"}>
                <DeleteIcon />
              </IconButton>
            </Tooltip>
          ) : (
              <Tooltip title="Filter list">
                <IconButton aria-label={buttonPrefix + "filter-list"} onClick={() => { this.props.onToggleFilter && this.props.onToggleFilter() }}>
                  <FilterListIcon />
                </IconButton>
              </Tooltip>
            )}
          <Tooltip title="Actions">
            <IconButton color="inherit"
            aria-label={buttonPrefix +"additional-actions-button"}
              aria-owns={open ? 'menu-appbar' : undefined}
              aria-haspopup="true"
              onClick={this.handleMenu} >
              <MoreIcon />
            </IconButton>
          </Tooltip>
          <Menu id="menu-appbar" anchorEl={this.state.anchorEl} anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
            transformOrigin={{ vertical: 'top', horizontal: 'right' }} open={open} onClose={this.handleClose} >
            <MenuItem aria-label="export-table-as-csv" onClick={(e) =>{ this.props.onExportToCsv(); this.handleClose()}}>Export as CSV</MenuItem>
          </Menu>
        </div>
      </Toolbar>
    );
  }
};

export const TableToolbar = withStyles(styles)(TableToolbarComponent);