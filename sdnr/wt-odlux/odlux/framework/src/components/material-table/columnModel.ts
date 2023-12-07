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

export enum ColumnType {
  text,
  numeric,
  boolean,
  date,
  custom
}

type CustomControl<TData> = {
  className?: string;
  style?: React.CSSProperties;
  rowData: TData;
}

export type ColumnModel<TData> = {
  title?: string;
  disablePadding?: boolean;
  width?: string | number ;
  className?: string;
  hide?: boolean;
  style?: React.CSSProperties;
  align?: 'inherit' | 'left' | 'center' | 'right' | 'justify';
  disableSorting?: boolean;
  disableFilter?: boolean;
} & ({
  property: string;
  type: ColumnType.custom;
  customControl: React.ComponentType<CustomControl<TData>>;
} | {
  property: keyof TData;
  type: ColumnType.boolean;
  labels?: { "true": string, "false": string };
} | {
    property: keyof TData;
    type?: ColumnType.numeric | ColumnType.text | ColumnType.date;
});