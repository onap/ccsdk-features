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
import { ComponentType } from 'react';
import { IconType } from './iconDefinition';

import { IActionHandler } from '../flux/action';
import { Middleware } from '../flux/middleware';
import { SettingsComponentProps } from './settings';

/** Represents the information needed about an application to integrate. */
export class ApplicationInfo {
  /** The name of the application. */
  name: string;
  /** Optional: The title of the application, if null ot undefined the name will be used. */
  title?: string;
  /** Optional: The icon of the application for the navigation and title bar. */
  icon?: IconType;
  /** Optional: The description of the application. */
  description?: string;
  /** The root component of the application. */
  rootComponent: ComponentType;
  /** Optional: The root action handler of the application. */
  rootActionHandler?: IActionHandler<{ [key: string]: any }>;
  /** Optional: Application speciffic middlewares. */
  middlewares?: Middleware<{ [key: string]: any }>[];
  /** Optional: A mapping object with the exported components. */
  exportedComponents?: { [key: string]: ComponentType }
  /** Optional: The entry to be shown in the menu. If undefiened the name will be used. */
  menuEntry?: string | React.ComponentType;
  /** Optional: A component to be shown in the menu when this app is active below the main entry. If undefiened the name will be used. */
  subMenuEntry?: React.ComponentType;
  /** Optional: A component to be shown in the applications status bar. If undefiened the name will be used. */
  statusBarElement?: React.ComponentType;
  /** Optional: A component to be shown in the dashboardview. If undefiened the name will be used. */
  dashbaordElement?: React.ComponentType;
  /** Optional: A component shown in the settings view */
  settingsElement?: React.ComponentType<SettingsComponentProps>;
  /** Optional: The pasth for this application. If undefined the name will be use as path. */
  path?: string;
}
