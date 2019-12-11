import * as React from 'react';
import { RouteComponentProps, withRouter } from 'react-router-dom';

import connect, { IDispatcher, Connect } from "../../../../framework/src/flux/connect";
import { IApplicationStoreState } from "../../../../framework/src/store/applicationStore";
import { MaterialTable, MaterialTableCtorType, ColumnType } from "../../../../framework/src/components/material-table";
import { createConnectedNetworkElementsProperties, createConnectedNetworkElementsActions } from "../../../configurationApp/src/handlers/connectedNetworkElementsHandler";

import { NetworkElementConnection } from "../models/networkElementConnection";
import { Tooltip, Button, IconButton } from "@material-ui/core";

const mapProps = (state: IApplicationStoreState) => ({
  connectedNetworkElementsProperties: createConnectedNetworkElementsProperties(state),
});

const mapDispatch = (dispatcher: IDispatcher) => ({
  connectedNetworkElementsActions: createConnectedNetworkElementsActions(dispatcher.dispatch),
});

const ConnectedElementTable = MaterialTable as MaterialTableCtorType<NetworkElementConnection>;

type NetworkElementSelectorComponentProps = RouteComponentProps & Connect<typeof mapProps, typeof mapDispatch>;

class NetworkElementSelectorComponent extends React.Component<NetworkElementSelectorComponentProps> {

  componentDidMount() {
    this.props.connectedNetworkElementsActions.onRefresh();
  }

  render() {
    return (
      <ConnectedElementTable onHandleClick={(e, row) => { this.props.history.push(`${ this.props.match.path }/${row.nodeId}`) }} columns={[
        { property: "nodeId", title: "Name", type: ColumnType.text },
        { property: "isRequired", title: "Required ?", type: ColumnType.text },
        { property: "host", title: "Host", type: ColumnType.text },
        { property: "port", title: "Port", type: ColumnType.text },
        { property: "coreModelCapability", title: "Core Model", type: ColumnType.text },
        { property: "deviceType", title: "Type", type: ColumnType.text },
      ]} idProperty="id" {...this.props.connectedNetworkElementsActions} {...this.props.connectedNetworkElementsProperties} asynchronus >
      </ConnectedElementTable>
    );
  }
}

export const NetworkElementSelector = withRouter(connect(mapProps, mapDispatch)(NetworkElementSelectorComponent));
export default NetworkElementSelector;

