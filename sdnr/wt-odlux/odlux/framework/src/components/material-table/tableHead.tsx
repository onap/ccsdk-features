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
import { ColumnModel, ColumnType } from './columnModel';
import { Theme } from '@mui/material/styles';

import { WithStyles } from '@mui/styles';
import withStyles from '@mui/styles/withStyles';
import createStyles from '@mui/styles/createStyles';

import TableSortLabel from '@mui/material/TableSortLabel';
import TableCell from '@mui/material/TableCell';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Checkbox from '@mui/material/Checkbox';
import Tooltip from '@mui/material/Tooltip';

const styles = (theme: Theme) => createStyles({
  header: {
    backgroundColor: "#fafafa",
    position: "sticky",
    top: 0
  }
});


type styles_header = WithStyles<typeof styles>;

interface IEnhancedTableHeadComponentProps extends styles_header {
  numSelected: number | null;
  onRequestSort: (event: React.SyntheticEvent, property: string) => void;
  onSelectAllClick: () => void;
  order: 'asc' | 'desc';
  orderBy: string | null;
  rowCount: number;
  columns: ColumnModel<{}>[];
  hiddenColumns: string[];
  enableSelection?: boolean;
  allowHtmlHeader?: boolean;
}

class EnhancedTableHeadComponent extends React.Component<IEnhancedTableHeadComponentProps> {
  createSortHandler = (property: string) => (event: React.SyntheticEvent) => {
    this.props.onRequestSort(event, property);
  };

  render() {
    const { onSelectAllClick, order, orderBy, numSelected, rowCount, columns } = this.props;
    const {classes} = this.props;

    return (
      <TableHead>
        <TableRow>
          { this.props.enableSelection 
           ? <TableCell padding="checkbox" style={ { width: "50px" } } className= {classes.header} >
              <Checkbox
                 indeterminate={ numSelected && numSelected > 0 && numSelected < rowCount || undefined }
                 checked={ numSelected === rowCount }
                 onChange={ onSelectAllClick }
              />
            </TableCell>
          : null
          }
          { columns.map(col => {
            const style = col.width ? { width: col.width } : {};
            const tableCell = (
              <TableCell className= {classes.header}
                key={ col.property }
                align={ col.type === ColumnType.numeric ? 'right' : 'left' } 
                padding={ col.disablePadding ? 'none' : 'normal' }
                sortDirection={ orderBy === (col.property) ? order : false }
                style={ style }
              >
                { col.disableSorting || (col.type === ColumnType.custom)
                  ? <TableSortLabel
                    active={ false }
                    direction={ undefined }
                  >
                    { col.title || col.property }
                  </TableSortLabel>
                  : <Tooltip disableInteractive
                    title="Sort"
                    placement={ col.type === ColumnType.numeric ? 'bottom-end' : 'bottom-start' }
                    enterDelay={ 300 }
                  >
                    <TableSortLabel
                      active={ orderBy === col.property }
                      direction={ order || undefined }
                      onClick={ this.createSortHandler(col.property) }
                    >
                      {
                        this.props.allowHtmlHeader ? <div className="content" dangerouslySetInnerHTML={{__html: col.title || col.property}}></div>
                       :  (col.title || col.property )
                      }
                    </TableSortLabel>
                  </Tooltip> }
              </TableCell>
            );

            //show column if...
            const showColumn = !this.props.hiddenColumns.includes(col.property);

            return showColumn && tableCell;
          }, this) }
        </TableRow>
      </TableHead>
    );
  }
}

export const EnhancedTableHead = withStyles(styles)(EnhancedTableHeadComponent);