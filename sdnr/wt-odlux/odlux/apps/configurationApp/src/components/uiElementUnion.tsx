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

import * as React from 'react'
import { BaseProps } from './baseProps';
import { Tooltip } from '@mui/material';
import { IfWhenTextInput } from './ifWhenTextInput';
import { ViewElementUnion, isViewElementString, isViewElementNumber, isViewElementObject, ViewElementNumber } from '../models/uiModels';
import { checkRange, checkPattern } from '../utilities/verifyer';

type UiElementUnionProps = { isKey: boolean } & BaseProps;

export const UIElementUnion = (props: UiElementUnionProps) => {

  const [isError, setError] = React.useState(false);
  const [helperText, setHelperText] = React.useState("");
  const [isTooltipVisible, setTooltipVisibility] = React.useState(true);

  const element = props.value as ViewElementUnion;

  const verifyValues = (data: string) => {

    let foundObjectElements = 0;
    let errorMessage = "";
    let isPatternCorrect = null;

    for (let i = 0; i < element.elements.length; i++) {
      const unionElement = element.elements[i];

      if (isViewElementNumber(unionElement)) {

        errorMessage = checkRange(unionElement, Number(data));

      } else if (isViewElementString(unionElement)) {
        errorMessage += checkRange(unionElement, data.length);
        isPatternCorrect = checkPattern(unionElement.pattern, data).isValid;


      } else if (isViewElementObject(unionElement)) {
        foundObjectElements++;
      }

      if (isPatternCorrect || errorMessage.length === 0) {
        break;
      }
    }

    if (errorMessage.length > 0 || isPatternCorrect !== null && !isPatternCorrect) {
      setError(true);
      setHelperText("Input is wrong.");
    } else {
      setError(false);
      setHelperText("");
    }

    if (foundObjectElements > 0 && foundObjectElements != element.elements.length) {
      throw new Error(`The union element ${element.id} can't be changed.`);

    } else {
      props.onChange(data);
    }
  };

  return <Tooltip disableInteractive title={isTooltipVisible ? element.description || '' : ''}>
    <IfWhenTextInput element={element} onChangeTooltipVisibility={setTooltipVisibility}
      spellCheck={false} autoFocus margin="dense"
      id={element.id} label={props.isKey ? "🔑 " + element.label : element.label} type="text" value={props.inputValue}
      onChange={(e: any) => { verifyValues(e.target.value) }}
      error={isError}
      style={{ width: 485, marginLeft: 20, marginRight: 20 }}
      readOnly={props.readOnly}
      disabled={props.disabled}
      helperText={helperText}
    />
  </Tooltip>;
}