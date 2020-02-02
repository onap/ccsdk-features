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
import { Panel } from '../../../../framework/src/components/material-ui';
import { networkElementsReloadAction, createNetworkElementsActions } from '../handlers/networkElementsHandler';
import { connectionStatusLogReloadAction, createConnectionStatusLogActions } from '../handlers/connectionStatusLogHandler';

import { NetworkElementsList } from '../components/networkElements';
import { ConnectionStatusLog } from '../components/connectionStatusLog';
import { setPanelAction, findWebUrisForGuiCutThroughAsyncAction, SetWeburiSearchBusy } from '../actions/commonNetworkElementsActions';
import { PanelId } from '../models/panelId';
import { NetworkElementConnection } from 'models/networkElementConnection';

const mapProps = (state: IApplicationStoreState) => ({
  panelId: state.connect.currentOpenPanel,
  user: state.framework.authenticationState.user,
  netWorkElements: state.connect.networkElements,
  availableGuiCutroughs: state.connect.guiCutThrough
});

const mapDispatcher = (dispatcher: IDispatcher) => ({
  networkElementsActions: createNetworkElementsActions(dispatcher.dispatch),
  connectionStatusLogActions: createConnectionStatusLogActions(dispatcher.dispatch),
  onLoadNetworkElements: () => {
    dispatcher.dispatch(networkElementsReloadAction);
  },
  loadWebUris: async (networkElements: NetworkElementConnection[]) => { await dispatcher.dispatch(findWebUrisForGuiCutThroughAsyncAction(networkElements)) },
  isBusy: (busy: boolean) => dispatcher.dispatch(new SetWeburiSearchBusy(busy)),
  onLoadConnectionStatusLog: () => {
    dispatcher.dispatch(connectionStatusLogReloadAction);
  },
  switchActivePanel: (panelId: PanelId) => {
    dispatcher.dispatch(setPanelAction(panelId));
  }
});

type ConnectApplicationComponentProps = Connect<typeof mapProps, typeof mapDispatcher>;

class ConnectApplicationComponent extends React.Component<ConnectApplicationComponentProps>{

  componentDidUpdate = async () => {
    // search for guicutthroughs after networkelements were found
    const networkElements = this.props.netWorkElements;

    if (networkElements.rows.length > 0) {
      await this.props.loadWebUris(networkElements.rows);
    }
  }

  private onTogglePanel = (panelId: PanelId) => {
    const nextActivePanel = panelId === this.props.panelId ? null : panelId;
    this.props.switchActivePanel(nextActivePanel);

    switch (nextActivePanel) {
      case 'NetworkElements':
        this.props.onLoadNetworkElements();
        break;
      case 'ConnectionStatusLog':
        this.props.onLoadConnectionStatusLog();
        break;
      case null:
        // do nothing if all panels are closed
        break;
      default:
        console.warn("Unknown nextActivePanel [" + nextActivePanel + "] in connectView");
        break;
    }

  };

  render(): JSX.Element {
    const { panelId } = this.props;

    return (
      <>
        <Panel activePanel={panelId} panelId={'NetworkElements'} onToggle={this.onTogglePanel} title={"Network Elements"}>
          <NetworkElementsList />
        </Panel>
        <Panel activePanel={panelId} panelId={'ConnectionStatusLog'} onToggle={this.onTogglePanel} title={"Connection Status Log"}>
          <ConnectionStatusLog />
        </Panel>
      </>
    );
  };
  public componentDidMount() {
    this.onTogglePanel("NetworkElements");
    this.props.networkElementsActions.onToggleFilter();
    this.props.connectionStatusLogActions.onToggleFilter();
  }
}

export const ConnectApplication = (connect(mapProps, mapDispatcher)(ConnectApplicationComponent));
export default ConnectApplication;