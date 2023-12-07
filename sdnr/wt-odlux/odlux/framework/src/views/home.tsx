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

import React, {FC, useState} from 'react';
import applicationService from '../services/applicationManager';


type DashboardElement = { name: string, element: JSX.Element };

const DashboardView: FC = (props) => {

  const registrations = applicationService.applications;

  const [selectedIndex] = useState(0);

  let dashboardArray: DashboardElement[] = [];

  let dashboardElements: (DashboardElement)[] = Object.keys(registrations).map(p => {
    const application = registrations[p];

    if (application.dashbaordElement) {
      const value: DashboardElement = { name: application.menuEntry?.toString()!, element: <application.dashbaordElement /> };
      return value;

    } else {
      return null;
    }
  }).filter((x): x is DashboardElement => x !== null);

  dashboardArray.push(...dashboardElements);

  return <div>
    <div>
      <div>
        {
          dashboardArray[selectedIndex]?.element
        }
      </div>
    </div>
  </div>
}


export default DashboardView;