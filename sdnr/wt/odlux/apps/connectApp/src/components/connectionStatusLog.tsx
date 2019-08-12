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
import connect, { IDispatcher, Connect } from '../../../../framework/src/flux/connect';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';
import { MaterialTable, ColumnType, MaterialTableCtorType } from '../../../../framework/src/components/material-table';

import { createConnectionStatusLogActions, createConnectionStatusLogProperties } from '../handlers/connectionStatusLogHandler';
import { ConnectionStatusLogType } from '../models/connectionStatusLog';

const mapProps = (state: IApplicationStoreState) => ({
  connectionStatusLogProperties: createConnectionStatusLogProperties(state),
});

const mapDispatch = (dispatcher: IDispatcher) => ({
    connectionStatusLogActions: createConnectionStatusLogActions(dispatcher.dispatch),
});
  
const ConnectionStatusTable = MaterialTable as MaterialTableCtorType<ConnectionStatusLogType>;

type ConnectionStatusLogComponentProps = Connect<typeof mapProps, typeof mapDispatch>;

class ConnectionStatusLogComponent extends React.Component<ConnectionStatusLogComponentProps> {
  render(): JSX.Element {
    return (
      <ConnectionStatusTable columns={ [
        { property: "timeStamp", title: "Time", type: ColumnType.text },
        { property: "objectId", title: "Name", type: ColumnType.text },
        { property: "elementStatus", title: "Connection status", type: ColumnType.text, disableFilter: true, disableSorting: true },
      ] } idProperty="_id" { ...this.props.connectionStatusLogActions } {...this.props.connectionStatusLogProperties } >
      </ConnectionStatusTable>
    );
  };

}

export const ConnectionStatusLog = connect(mapProps, mapDispatch)(ConnectionStatusLogComponent);
export default ConnectionStatusLog;