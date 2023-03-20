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

import * as React from "react"
import { Tooltip, TextField } from "@mui/material";
import { ViewElementString } from "../models/uiModels";
import { BaseProps } from "./baseProps";
import { IfWhenTextInput } from "./ifWhenTextInput";
import { checkRange, checkPattern } from "../utilities/verifyer";

type stringEntryProps = BaseProps ;

export const UiElementString = (props: stringEntryProps) => {

    const [isError, setError] = React.useState(false);
    const [helperText, setHelperText] = React.useState("");
    const [isTooltipVisible, setTooltipVisibility] = React.useState(true);

    const element = props.value as ViewElementString;

    const verifyValues = (data: string) => {

        if (data.trim().length > 0) {

            let errorMessage = "";
            const result = checkRange(element, data.length);

            if (result.length > 0) {
                errorMessage += result;
            }

            const patternResult = checkPattern(element.pattern, data)

            if (patternResult.error) {
                errorMessage += patternResult.error;
            }

            if (errorMessage.length > 0) {
                setError(true);
                setHelperText(errorMessage);
            } else {
                setError(false);
                setHelperText("");
            }
        } else {
            setError(false);
            setHelperText("");
        }


        props.onChange(data);

    }

    return (
        <Tooltip disableInteractive title={isTooltipVisible ? element.description || '' : ''}>
            <IfWhenTextInput element={element} onChangeTooltipVisibility={setTooltipVisibility}
                spellCheck={false} autoFocus margin="dense"
                id={element.id} label={props?.isKey ? "🔑 " + element.label : element.label} type="text" value={props.inputValue}
                style={{ width: 485, marginLeft: 20, marginRight: 20 }}
                onChange={(e: any) => { verifyValues(e.target.value) }}
                error={isError}
                readOnly={props.readOnly}
                disabled={props.disabled}
                helperText={helperText}
            />
        </Tooltip>
    );
}