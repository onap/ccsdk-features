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
import { createExternal,IExternalTableState } from '../../../../framework/src/components/material-table/utilities';
import { createSearchDataHandler } from '../../../../framework/src/utilities/elasticSearch';

import { ConnectionStatusLogType } from '../models/connectionStatusLog';
export interface IConnectionStatusLogState extends IExternalTableState<ConnectionStatusLogType> { }

// create eleactic search material data fetch handler
const connectionStatusLogSearchHandler = createSearchDataHandler<{ event: ConnectionStatusLogType }, ConnectionStatusLogType>('sdnevents_v1/eventlog', null,
  (event) => ({
    _id: event._id,
    timeStamp: event._source.event.timeStamp,
    objectId: event._source.event.objectId,
    type: event._source.event.type,
    elementStatus: event._source.event.type === 'ObjectCreationNotificationXml'
      ? 'mounted'
      : event._source.event.type === 'ObjectDeletionNotificationXml'
        ? 'unmounted'
        : event._source.event.type === 'AttributeValueChangedNotificationXml'
          ? event._source.event.newValue
          : 'unknown',
    newValue: ''

  }),
  (name) => `event.${ name }`);

export const {
  actionHandler: connectionStatusLogActionHandler,
  createActions: createConnectionStatusLogActions,
  createProperties: createConnectionStatusLogProperties,
  reloadAction: connectionStatusLogReloadAction,

  // set value action, to change a value
} = createExternal<ConnectionStatusLogType>(connectionStatusLogSearchHandler, appState => appState.connect.connectionStatusLog);

