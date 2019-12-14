import { createExternal, IExternalTableState } from '../../../../framework/src/components/material-table/utilities';
import { createSearchDataHandler } from '../../../../framework/src/utilities/elasticSearch';

import { NetworkElementConnection } from '../models/networkElementConnection';

export interface IConnectedNetworkElementsState extends IExternalTableState<NetworkElementConnection> { }

// create eleactic search material data fetch handler
const connectedNetworkElementsSearchHandler = createSearchDataHandler<NetworkElementConnection>('network-element-connection', { status: "Connected" });

export const {
  actionHandler: connectedNetworkElementsActionHandler,
  createActions: createConnectedNetworkElementsActions,
  createProperties: createConnectedNetworkElementsProperties,
  reloadAction: connectedNetworkElementsReloadAction,

  // set value action, to change a value
} = createExternal<NetworkElementConnection>(connectedNetworkElementsSearchHandler, appState => appState.configuration.connectedNetworkElements);
 