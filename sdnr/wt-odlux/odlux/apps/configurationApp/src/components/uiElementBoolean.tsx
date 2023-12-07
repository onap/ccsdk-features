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

import React from 'react';

import MenuItem from '@mui/material/MenuItem';
import FormHelperText from '@mui/material/FormHelperText';
import Select from '@mui/material/Select';
import FormControl from '@mui/material/FormControl';
import InputLabel from '@mui/material/InputLabel';

import { ViewElementBoolean } from '../models/uiModels';
import { BaseProps } from './baseProps';

type BooleanInputProps = BaseProps<boolean>;

export const UiElementBoolean = (props: BooleanInputProps) => {

  const element = props.value as ViewElementBoolean;

  const value = String(props.inputValue).toLowerCase();
  const mandatoryError = element.mandatory && value !== 'true' && value !== 'false';
    
  return (!props.readOnly || element.id != null
    ? (<FormControl variant="standard" style={{ width: 485, marginLeft: 20, marginRight: 20 }}>
            <InputLabel htmlFor={`select-${element.id}`} >{element.label}</InputLabel>
            <Select variant="standard"
                aria-label={element.label + '-selection'}
                required={!!element.mandatory}
                error={mandatoryError}
                onChange={(e) => { props.onChange(e.target.value === 'true'); }}
                readOnly={props.readOnly}
                disabled={props.disabled}
                value={value}
                inputProps={{
                  name: element.id,
                  id: `select-${element.id}`,
                }}
            >
                <MenuItem value={'true'} aria-label="true">{element.trueValue || 'True'}</MenuItem>
                <MenuItem value={'false'} aria-label="false">{element.falseValue || 'False'}</MenuItem>

            </Select>
            <FormHelperText>{mandatoryError ? 'Value is mandatory' : ''}</FormHelperText>
        </FormControl>)
    : null
  );
};