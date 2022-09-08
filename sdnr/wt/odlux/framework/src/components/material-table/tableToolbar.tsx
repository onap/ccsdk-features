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
import { Theme, lighten } from '@mui/material/styles';

import { WithStyles } from '@mui/styles';
import withStyles from '@mui/styles/withStyles';
import createStyles from '@mui/styles/createStyles';

import IconButton from '@mui/material/IconButton';
import Tooltip from '@mui/material/Tooltip';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import DeleteIcon from '@mui/icons-material/Delete';
import MoreIcon from '@mui/icons-material/MoreVert';
import FilterListIcon from '@mui/icons-material/FilterList';
import MenuItem from '@mui/material/MenuItem';
import Menu from '@mui/material/Menu';
import { SvgIconProps } from '@mui/material/SvgIcon';
import { Button } from '@mui/material';
import { ColumnModel } from './columnModel';
import  ShowColumnsDialog  from './showColumnDialog'

const styles = (theme: Theme) => createStyles({
  root: {
    paddingRight: theme.spacing(1),
  },
  highlight:
    theme.palette.mode === 'light'
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
  tableId: string | null;
  customActionButtons?: { icon: React.ComponentType<SvgIconProps>, tooltip?: string, ariaLabel: string, onClick: () => void, disabled?: boolean }[];
  columns: ColumnModel<{}>[];
  onHideColumns: (columnNames: string[]) => void
  onShowColumns: (columnNames: string[]) => void
  onToggleFilter: () => void;
  onExportToCsv: () => void;
}

class TableToolbarComponent extends React.Component<ITableToolbarComponentProps, { anchorEl: EventTarget & HTMLElement | null, anchorElDialog: HTMLElement | null }> {

  
  constructor(props: ITableToolbarComponentProps) {
    super(props);

    this.state = {
      anchorEl: null,
      anchorElDialog: null
    };
  }

  private handleMenu = (event: React.MouseEvent<HTMLElement>) => {
    this.setState({ anchorEl: event.currentTarget });
  };

  private handleClose = () => {
    this.setState({ anchorEl: null });
  };

  private showColumnsDialog = (event: React.MouseEvent<HTMLElement>) =>{
    this.setState({ anchorElDialog: this.state.anchorEl });
  }

  private onCloseDialog = () =>{
    this.setState({ anchorElDialog: null });

  }

  render() {
    const { numSelected, classes } = this.props;
    const open = !!this.state.anchorEl;
    const buttonPrefix = this.props.tableId !== null ? this.props.tableId : 'table';
    return (
      <>
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
              <Tooltip disableInteractive key={`custom-action-${ind}`} title={action.tooltip || ''}>
                <IconButton
                  disabled={action.disabled}
                  aria-label={`${buttonPrefix}-${action.ariaLabel}-button`}
                  onClick={() => action.onClick()}
                  size="large">
                  <action.icon />
                </IconButton>
              </Tooltip>
            ))
            : null}
          {numSelected && numSelected > 0 ? (
            <Tooltip disableInteractive title="Delete">
              <IconButton aria-label={`${buttonPrefix}-delete-button`} size="large">
                <DeleteIcon />
              </IconButton>
            </Tooltip>
          ) : (
              <Tooltip disableInteractive title="Filter list">
                <IconButton
                  aria-label={`${buttonPrefix}-filter-list-button`}
                  onClick={() => { this.props.onToggleFilter && this.props.onToggleFilter() }}
                  size="large">
                  <FilterListIcon />
                </IconButton>
              </Tooltip>
            )}
          <Tooltip disableInteractive title="Actions">
            <IconButton
              color="inherit"
              aria-label={`${buttonPrefix}-additional-actions-button`}
              aria-owns={open ? 'menu-appbar' : undefined}
              aria-haspopup="true"
              onClick={this.handleMenu}
              size="large">
              <MoreIcon />
            </IconButton>
          </Tooltip>
          <Menu id="menu-appbar" anchorEl={this.state.anchorEl} anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
            transformOrigin={{ vertical: 'top', horizontal: 'right' }} open={open} onClose={this.handleClose} >
            <MenuItem aria-label="export-table-as-csv" onClick={(e) =>{ this.props.onExportToCsv(); this.handleClose()}}>Export as CSV</MenuItem>
            <MenuItem aria-label="hide-show-table-columns" onClick={(e) =>{ this.showColumnsDialog(e); this.handleClose()}}>Hide/show columns</MenuItem>
          </Menu>
        </div>
      </Toolbar>
      <ShowColumnsDialog 
        anchorEl={this.state.anchorElDialog} 
        onClose={this.onCloseDialog}
        settingsName={this.props.tableId} 
        columns={this.props.columns} 
        hideColumns={this.props.onHideColumns} 
        showColumns={this.props.onShowColumns} />
      </>
    );
  }
}

export const TableToolbar = withStyles(styles)(TableToolbarComponent);