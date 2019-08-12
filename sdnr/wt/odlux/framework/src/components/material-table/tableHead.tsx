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
import { withStyles, WithStyles, createStyles, Theme } from '@material-ui/core/styles';

import TableSortLabel from '@material-ui/core/TableSortLabel';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Checkbox from '@material-ui/core/Checkbox';
import Tooltip from '@material-ui/core/Tooltip';

interface IEnhancedTableHeadComponentProps {
  numSelected: number | null;
  onRequestSort: (event: React.SyntheticEvent, property: string) => void;
  onSelectAllClick: () => void;
  order: 'asc' | 'desc';
  orderBy: string | null;
  rowCount: number;
  columns: ColumnModel<{}>[];
  enableSelection?: boolean;
}

class EnhancedTableHeadComponent extends React.Component<IEnhancedTableHeadComponentProps> {
  createSortHandler = (property: string) => (event: React.SyntheticEvent) => {
    this.props.onRequestSort(event, property);
  };

  render() {
    const { onSelectAllClick, order, orderBy, numSelected, rowCount, columns } = this.props;

    return (
      <TableHead>
        <TableRow>
          { this.props.enableSelection 
           ? <TableCell padding="checkbox" style={ { width: "50px" } }>
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
            return (
              <TableCell
                key={ col.property }
                align={ col.type === ColumnType.numeric ? 'right' : 'left' } 
                padding={ col.disablePadding ? 'none' : 'default' }
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
                  : <Tooltip
                    title="Sort"
                    placement={ col.type === ColumnType.numeric ? 'bottom-end' : 'bottom-start' }
                    enterDelay={ 300 }
                  >
                    <TableSortLabel
                      active={ orderBy === col.property }
                      direction={ order || undefined }
                      onClick={ this.createSortHandler(col.property) }
                    >
                      { col.title || col.property }
                    </TableSortLabel>
                  </Tooltip> }
              </TableCell>
            );
          }, this) }
        </TableRow>
      </TableHead>
    );
  }
}

export const EnhancedTableHead = EnhancedTableHeadComponent;