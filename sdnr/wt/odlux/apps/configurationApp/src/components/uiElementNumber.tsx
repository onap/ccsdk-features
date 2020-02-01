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

import { ViewElementNumber } from "models/uiModels";
import { Tooltip, InputAdornment } from "@material-ui/core";
import * as React from 'react';
import { baseProps } from "./baseProps";
import { IfWhenTextInput } from "./ifWhenTextInput";
import { checkRange } from "./verifyer";

type numberInputProps = baseProps;

export const UiElementNumber = (props: numberInputProps) => {


    const [error, setError] = React.useState(false);
    const [helperText, setHelperText] = React.useState("");
    const [isTooltipVisible, setTooltipVisibility] = React.useState(true);

    const element = props.value as ViewElementNumber;

    const verifyValue = (data: string) => {

        if (data.trim().length > 0) {
            const num = Number(data);
            if (!isNaN(num)) {
                const result = checkRange(element, num);
                if (result.length > 0) {
                    setError(true);
                    setHelperText(result);
                } else {
                    setError(false);
                    setHelperText("");
                }
            } else {
                setError(true);
                setHelperText("Input is not a number.");
            }
        } else {
            setError(false);
            setHelperText("");
        }

        props.onChange(data);
    }

    return (
        <Tooltip title={isTooltipVisible ? element.description || '' : ''}>
            <IfWhenTextInput element={element} toogleTooltip={(val: boolean) => setTooltipVisibility(val)}
                spellCheck={false} autoFocus margin="dense"
                id={element.id} label={element.label} type="text" value={props.inputValue}
                style={{ width: 485, marginLeft: 20, marginRight: 20 }}
                onChange={(e: any) => { verifyValue(e.target.value) }}
                error={error}
                readOnly={props.readOnly}
                disabled={props.disabled}
                helperText={helperText}
                startAdornment={element.units != null ? <InputAdornment position="start">{element.units}</InputAdornment> : undefined}
            />
        </Tooltip>
    );
}