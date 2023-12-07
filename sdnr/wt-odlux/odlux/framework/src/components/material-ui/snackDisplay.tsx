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

import { IApplicationStoreState } from '../../store/applicationStore';
import { Connect, connect, IDispatcher } from '../../flux/connect';
import { RemoveSnackbarNotification } from '../../actions/snackbarActions';

import { WithSnackbarProps, withSnackbar } from 'notistack';

const mapProps = (state: IApplicationStoreState) => ({
  notifications: state.framework.applicationState.snackBars
});

const mapDispatch = (dispatcher: IDispatcher) => ({
  removeSnackbar: (key: number) => {
    dispatcher.dispatch(new RemoveSnackbarNotification(key));
   }
});

type DisplaySnackbarsComponentProps = Connect<typeof mapProps, typeof mapDispatch> & WithSnackbarProps;

class DisplaySnackbarsComponent extends React.Component<DisplaySnackbarsComponentProps> {
  private displayed: number[] = [];

  private storeDisplayed = (id: number) => {
    this.displayed = [...this.displayed, id];
  };

  public shouldComponentUpdate({ notifications: newSnacks = [] }: DisplaySnackbarsComponentProps) {
    
    const { notifications: currentSnacks } = this.props;
    let notExists = false;
    for (let i = 0; i < newSnacks.length; i++) {
      if (notExists) continue;
      notExists = notExists || !currentSnacks.filter(({ key }) => newSnacks[i].key === key).length;
    }
    return notExists;
  }

  componentDidUpdate() {
    const { notifications = [] } = this.props;

    notifications.forEach(notification => {
      if (this.displayed.includes(notification.key)) return;
      const options = notification.options || {};
      this.props.enqueueSnackbar(notification.message, options);
      this.storeDisplayed(notification.key);
      this.props.removeSnackbar(notification.key);
    });
  }

  render() {
    return null;
  }
}

const DisplayStackbars = withSnackbar(connect(mapProps, mapDispatch)(DisplaySnackbarsComponent));
export default DisplayStackbars;