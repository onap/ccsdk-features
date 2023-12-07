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
import * as React from "react";

import { Connect, connect, IDispatcher } from '../../../../framework/src/flux/connect';
import { MaterialTable, MaterialTableCtorType } from '../../../../framework/src/components/material-table';
import Refresh from '@mui/icons-material/Refresh';

import { EventLogType } from '../models/eventLogType';
import { IApplicationStoreState } from "../../../../framework/src/store/applicationStore";
import { createEventLogProperties, createEventLogActions } from "../handlers/eventLogHandler";
import RefreshEventLogDialog, { RefreshEventLogDialogMode } from '../components/refreshEventLogDialog';

const EventLogTable = MaterialTable as MaterialTableCtorType<EventLogType & { _id: string }>;

const mapProps = (state: IApplicationStoreState) => ({
  eventLogProperties: createEventLogProperties(state),
  eventLog: state.eventLog.logEntries
});

const mapDispatch = (dispatcher: IDispatcher) => ({
  eventLogActions: createEventLogActions(dispatcher.dispatch)
});

type EventLogComponentProps = Connect<typeof mapProps, typeof mapDispatch>;
type EventLogComponentState = {
  refreshEventLogEditorMode: RefreshEventLogDialogMode
}
let initalSorted = false;

class EventLogComponent extends React.Component<EventLogComponentProps, EventLogComponentState> {
  constructor(props: EventLogComponentProps) {
    super(props);

    this.state = {
      refreshEventLogEditorMode: RefreshEventLogDialogMode.None
    };
  }

  render(): JSX.Element {

    const refreshEventLogAction = {
      icon: Refresh, tooltip: 'Refresh Event log', ariaLabel:'refresh', onClick: () => {
        this.setState({
          refreshEventLogEditorMode: RefreshEventLogDialogMode.RefreshEventLogTable
        });
      }
    };
    return (
      <>
        <EventLogTable stickyHeader title="Event Log" tableId="event-log-table" idProperty="_id" customActionButtons={[refreshEventLogAction]}
          columns={[
            { property: "nodeId", title: "Node Name" },
            { property: "counter", title: "Counter" },
            { property: "timestamp", title: "Timestamp" },
            { property: "objectId", title: "Object ID" },
            { property: "attributeName", title: "Attribute Name" },
            { property: "newValue", title: "Message" },
            { property: "sourceType", title: "Source" }
          ]}  {...this.props.eventLogActions} {...this.props.eventLogProperties} >
        </EventLogTable>
        <RefreshEventLogDialog
          mode={this.state.refreshEventLogEditorMode}
          onClose={this.onCloseRefreshEventLogDialog}
        />
      </>
    )
  }

  private onCloseRefreshEventLogDialog = () => {
    this.setState({
      refreshEventLogEditorMode: RefreshEventLogDialogMode.None
    });
  }
  componentDidMount() {

    if (!initalSorted) {
      initalSorted = true;
      this.props.eventLogActions.onHandleExplicitRequestSort("timestamp", "desc");
    } else {
      this.props.eventLogActions.onRefresh();
    }
  }
}

export const EventLog = connect(mapProps, mapDispatch)(EventLogComponent);
export default EventLog;