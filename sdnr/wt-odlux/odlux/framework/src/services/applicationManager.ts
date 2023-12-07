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
import { ApplicationInfo } from '../models/applicationInfo';
import { Event } from '../common/event';

import { applicationApi } from './applicationApi';

/** Represents registry to manage all applications. */
class ApplicationManager {
    
  /** Stores all registered applications.  */
  private _applications: { [key: string]: ApplicationInfo }; 
  
  /** Initializes a new instance of this class. */
  constructor() {
    this._applications = {};
    this.changed = new Event<void>(); 
  }

  /** The changed event will fire if the registration has changed. */
  public changed: Event<void>;

  /** Registers a new application. */
  public registerApplication(applicationInfo: ApplicationInfo) {
    this._applications[applicationInfo.name] = applicationInfo;
    this.changed.invoke();
    return applicationApi;
  }

  /** Gets all registered applications. */
  public get applications() {
    return this._applications;
  }
}

/** A singleton instance of the application manager. */
export const applicationManager = new ApplicationManager();
export default applicationManager;