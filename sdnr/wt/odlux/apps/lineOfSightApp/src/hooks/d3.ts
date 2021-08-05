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

/* eslint-disable react-hooks/exhaustive-deps */
import { useEffect, useRef } from 'react';
import type { DependencyList } from 'react';

import * as d3 from 'd3';


type SelectionType =  d3.Selection<SVGSVGElement, d3.BaseType, null, undefined>;

export const useD3 = (renderChartFn: (selection: SelectionType) => void, dependencies: DependencyList) => {
  const ref = useRef<SVGSVGElement>(null);

  useEffect(() => {
    if (ref.current) renderChartFn(d3.select(ref.current)); 
    return () => { };
  }, dependencies); 
  
  return ref;
}
