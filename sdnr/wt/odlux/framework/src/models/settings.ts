/**
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt odlux
 * =================================================================================================
 * Copyright (C) 2021 highstreet technologies GmbH Intellectual Property. All rights reserved.
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

export type TableSettingsColumn = {
  property: string;
  displayed: boolean;
};

export type TableSettings = {
  tables:{
    [key: string]: {
      columns: TableSettingsColumn[];
            
      //match prop names, hide them
      //via property name! -> only those which are hidden!
      //all others default false, oh yeah
      //or maybe the other way around, gotta think about that
    
    };
  };
};

export type GeneralSettings = {
  general:{
    areNotificationsEnabled: boolean | null;
  };
};

export type Settings = TableSettings & GeneralSettings;

export type SettingsComponentProps = {
  onClose(): void;
};