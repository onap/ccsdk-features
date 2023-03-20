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

import Refresh from '@mui/icons-material/Refresh';

import { ColumnType, MaterialTable, MaterialTableCtorType } from '../../../../framework/src/components/material-table';
import { connect, Connect, IDispatcher } from '../../../../framework/src/flux/connect';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';

import { createConnectionStatusLogActions, createConnectionStatusLogProperties } from '../handlers/connectionStatusLogHandler';
import { NetworkElementConnectionLog } from '../models/networkElementConnectionLog';
import RefreshConnectionStatusLogDialog, { RefreshConnectionStatusLogDialogMode } from './refreshConnectionStatusLogDialog';

const mapProps = (state: IApplicationStoreState) => ({
  connectionStatusLogProperties: createConnectionStatusLogProperties(state),
});

const mapDispatch = (dispatcher: IDispatcher) => ({
  connectionStatusLogActions: createConnectionStatusLogActions(dispatcher.dispatch),
});

const ConnectionStatusTable = MaterialTable as MaterialTableCtorType<NetworkElementConnectionLog>;

type ConnectionStatusLogComponentProps = Connect<typeof mapProps, typeof mapDispatch>;
type ConnectionStatusLogComponentState = {
  refreshConnectionStatusLogEditorMode: RefreshConnectionStatusLogDialogMode;
};

let initialSorted = false;


class ConnectionStatusLogComponent extends React.Component<ConnectionStatusLogComponentProps, ConnectionStatusLogComponentState > {
  constructor(props: ConnectionStatusLogComponentProps) {
    super(props);

    this.state = {
      refreshConnectionStatusLogEditorMode: RefreshConnectionStatusLogDialogMode.None,
    };
  }

  render(): JSX.Element {
    const refreshConnectionStatusLogAction = {
      icon: Refresh, tooltip: 'Refresh Connection Status Log Table', ariaLabel:'refresh', onClick: () => {
        this.setState({
          refreshConnectionStatusLogEditorMode: RefreshConnectionStatusLogDialogMode.RefreshConnectionStatusLogTable,
        });
      },
    };

    return (
    <>
      <ConnectionStatusTable stickyHeader tableId="connection-status-table" customActionButtons={[refreshConnectionStatusLogAction]}  columns={[
        { property: 'timestamp', title: 'Timestamp', type: ColumnType.text },
        { property: 'nodeId', title: 'Node ID', type: ColumnType.text },
        { property: 'status', title: 'Connection Status', type: ColumnType.text },
      ]} idProperty="id" {...this.props.connectionStatusLogActions} {...this.props.connectionStatusLogProperties} >
      </ConnectionStatusTable>
       <RefreshConnectionStatusLogDialog
        mode={ this.state.refreshConnectionStatusLogEditorMode }
        onClose={ this.onCloseRefreshConnectionStatusLogDialog }
      />
    </>
    );
  }

  private onCloseRefreshConnectionStatusLogDialog = () => {
    this.setState({
      refreshConnectionStatusLogEditorMode: RefreshConnectionStatusLogDialogMode.None,
    });
  };

  componentDidMount() {
    if (!initialSorted) {
      initialSorted = true;
      this.props.connectionStatusLogActions.onHandleExplicitRequestSort('timestamp', 'desc');
    } else {
      this.props.connectionStatusLogActions.onRefresh();
    }
  }
}

export const ConnectionStatusLog = connect(mapProps, mapDispatch)(ConnectionStatusLogComponent);
export default ConnectionStatusLog;