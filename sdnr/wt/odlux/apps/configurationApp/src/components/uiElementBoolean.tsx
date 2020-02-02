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

import { ViewElementBoolean } from "../models/uiModels";
import * as React from "react"
import { MenuItem, FormHelperText, Select, FormControl, InputLabel } from "@material-ui/core";
import { baseProps } from "./baseProps";

type booleanInputProps = baseProps;

export const UiElementBoolean = (props: booleanInputProps) => {

    const element = props.value as ViewElementBoolean;

    let error = "";
    const value = String(props.inputValue).toLowerCase();
    if (element.mandatory && value !== "true" && value !== "false") {
        error = "Error";
    }
    return (!props.readOnly || element.id != null
        ? (<FormControl style={{ width: 485, marginLeft: 20, marginRight: 20 }}>
            <InputLabel htmlFor={`select-${element.id}`} >{element.label}</InputLabel>
            <Select
                required={!!element.mandatory}
                error={!!error}
                onChange={(e) => { props.onChange(e.target.value as any) }}
                readOnly={props.readOnly}
                disabled={props.disabled}
                value={value}
                inputProps={{
                    name: element.id,
                    id: `select-${element.id}`,
                }}
            >
                <MenuItem value={'true'}>{element.trueValue || 'True'}</MenuItem>
                <MenuItem value={'false'}>{element.falseValue || 'False'}</MenuItem>

            </Select>
            <FormHelperText>{error}</FormHelperText>
        </FormControl>)
        : null
    );
}