import * as React from "react";
import { withRouter, RouteComponentProps, Route, Switch, Redirect } from 'react-router-dom';

import { faAdjust } from '@fortawesome/free-solid-svg-icons';  // select app icon

import connect, { Connect, IDispatcher } from '../../../framework/src/flux/connect';
import applicationManager from '../../../framework/src/services/applicationManager';
import { IApplicationStoreState } from "../../../framework/src/store/applicationStore";
import { configurationAppRootHandler } from "./handlers/configurationAppRootHandler";
import { NetworkElementSelector } from "./views/networkElementSelector";

import ConfigurationApplication from "./views/configurationApplication";
import { updateNodeIdAsyncActionCreator, updateViewActionAsyncCreator } from "./actions/deviceActions";

let currentNodeId: string | null | undefined = undefined;
let currentVirtualPath: string | null | undefined = undefined;
let lastUrl: string | undefined = undefined;

const mapProps = (state: IApplicationStoreState) => ({
  // currentProblemsProperties: createCurrentProblemsProperties(state),
});

const mapDisp = (dispatcher: IDispatcher) => ({
  updateNodeId: (nodeId: string) => dispatcher.dispatch(updateNodeIdAsyncActionCreator(nodeId)),
  updateView: (vPath: string) => dispatcher.dispatch(updateViewActionAsyncCreator(vPath)),
});

const ConfigurationApplicationRouteAdapter = connect(mapProps, mapDisp)((props: RouteComponentProps<{ nodeId?: string, 0: string }> & Connect<typeof mapProps, typeof mapDisp>) => {
  React.useEffect(() => {
    return () => {
      lastUrl = undefined;
      currentNodeId = undefined;
      currentVirtualPath = undefined;
    }
  },[]);
  if (props.location.pathname !== lastUrl) {
    // ensure the asynchronus update will only be called once per path
    lastUrl = props.location.pathname;
    window.setTimeout(async () => {

      // check if the nodeId has changed
      if (currentNodeId !== props.match.params.nodeId) {
        currentNodeId = props.match.params.nodeId || undefined;
        currentVirtualPath = null;
        currentNodeId && await props.updateNodeId(currentNodeId);
      }

      if (currentVirtualPath !== props.match.params[0]) {
        currentVirtualPath = props.match.params[0];
        await props.updateView(currentVirtualPath);
      }

    });
  }
  return (
    <ConfigurationApplication />
  );
});

const App = withRouter((props: RouteComponentProps) => (
  <Switch>
    <Route path={`${props.match.url}/:nodeId/*`} component={ ConfigurationApplicationRouteAdapter } />
    <Route path={`${props.match.url}/:nodeId`} component={ ConfigurationApplicationRouteAdapter } />
    <Route path={`${props.match.url}`} component={ NetworkElementSelector } />
    <Redirect to={`${props.match.url}`} />
  </Switch>
));

export function register() {
  applicationManager.registerApplication({
    name: "configuration",
    icon: faAdjust,
    rootComponent: App,
    rootActionHandler: configurationAppRootHandler,
    menuEntry: "Configuration"
  });
}
