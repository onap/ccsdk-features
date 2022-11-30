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
import { BaseProps } from './baseProps';
import { ViewElementSelection } from '../models/uiModels'
import { FormControl, InputLabel, Select, FormHelperText, MenuItem, Tooltip } from '@mui/material';

type selectionProps = BaseProps;

export const UiElementSelection = (props: selectionProps) => {

    const element = props.value as ViewElementSelection;

    let error = "";
    const value = String(props.inputValue);
    if (element.mandatory && Boolean(!value)) {
        error = "Error";
    }

    return (props.readOnly || props.inputValue != null
        ? (<FormControl variant="standard" style={{ width: 485, marginLeft: 20, marginRight: 20 }}>
            <InputLabel htmlFor={`select-${element.id}`} >{element.label}</InputLabel>
            <Select variant="standard"
                required={!!element.mandatory}
                error={!!error}
                onChange={(e) => { props.onChange(e.target.value as string) }}
                readOnly={props.readOnly}
                disabled={props.disabled}
                value={value.toString()}
                aria-label={element.label+'-selection'}
                inputProps={{
                    name: element.id,
                    id: `select-${element.id}`,
                }}
            >
          {element.options.map(option => (
            <MenuItem key={option.key} value={option.key} aria-label={option.key}><Tooltip disableInteractive title={option.description || '' }><div style={{width:"100%"}}>{option.key}</div></Tooltip></MenuItem>
          ))}
            </Select>
            <FormHelperText>{error}</FormHelperText>
        </FormControl>)
        : null
    );
}