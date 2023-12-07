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

import { YangRange, Operator, ViewElementNumber, ViewElementString, isViewElementNumber, isViewElementString } from '../models/uiModels';

export type validated = { isValid: boolean; error?: string };

export type validatedRange = { isValid: boolean; error?: string };

const rangeErrorStartNumber = 'The entered number must be';
const rangeErrorInnerMinTextNumber = 'greater or equals than';
const rangeErrorInnerMaxTextNumber = 'less or equals than';
const rangeErrorEndTextNumber = '.';

const rangeErrorStartString = 'The entered text must have';
const rangeErrorInnerMinTextString = 'no more than';
const rangeErrorInnerMaxTextString = 'less than';
const rangeErrorEndTextString = ' characters.';

let errorMessageStart = '';
let errorMessageMiddleMinPart = '';
let errorMessageMiddleMaxPart = '';
let errorMessageEnd = '';

const isYangRange = (val: YangRange | Operator<YangRange>): val is YangRange => (val as YangRange).min !== undefined;
  
const isYangOperator = (val: YangRange | Operator<YangRange>): val is Operator<YangRange> => (val as Operator<YangRange>).operation !== undefined;

const isRegExp = (val: RegExp | Operator<RegExp>): val is RegExp => (val as RegExp).source !== undefined;

const isRegExpOperator = (val: RegExp | Operator<RegExp>): val is Operator<RegExp> => (val as Operator<RegExp>).operation !== undefined;

const getRangeErrorMessagesRecursively = (value: Operator<YangRange>, data: number): string[] => {
  let currentIteration: string[] = [];
  
  // iterate over all elements
  for (let i = 0; i < value.arguments.length; i++) {
    const element = value.arguments[i];
  
    let min = undefined;
    let max = undefined;
  
    let isNumberCorrect = false;
  
    if (isYangRange(element)) {
  
      //check found min values
      if (!isNaN(element.min)) {
        if (data < element.min) {
          min = element.min;
        } else {
          isNumberCorrect = true;
        }
      }
  
      // check found max values
      if (!isNaN(element.max)) {
        if (data > element.max) {
          max = element.max;
        } else {
          isNumberCorrect = true;
        }
      }
  
      // construct error messages
      if (min != undefined) {
        currentIteration.push(`${value.operation.toLocaleLowerCase()} ${errorMessageMiddleMinPart} ${min}`);
      } else if (max != undefined) {
        currentIteration.push(`${value.operation.toLocaleLowerCase()} ${errorMessageMiddleMaxPart} ${max}`);
  
      }
  
    } else if (isYangOperator(element)) {
  
      //get error_message from expression
      const result = getRangeErrorMessagesRecursively(element, data);
      if (result.length === 0) {
        isNumberCorrect = true;
      }
      currentIteration = currentIteration.concat(result);
    }
  
    // if its an OR operation, the number has been checked and min/max are empty (thus not violated)
    // delete everything found (because at least one found is correct, therefore all are correct) and break from loop
    if (min === undefined && max === undefined && isNumberCorrect && value.operation === 'OR') {
  
      currentIteration.splice(0, currentIteration.length);
      break;
    }
  }
  
  return currentIteration;
};

const createStartMessage = (element: string) => {
  //remove leading or or and from text
  if (element.startsWith('and')) {
    element = element.replace('and', '');
  } else if (element.startsWith('or')) {
    element = element.replace('or', '');
  }
  return `${errorMessageStart} ${element}`;
};
  
const getRangeErrorMessages = (value: Operator<YangRange>, data: number): string => {

  const currentIteration = getRangeErrorMessagesRecursively(value, data);
  
  // build complete error message from found parts
  let errorMessage = '';
  if (currentIteration.length > 1) {
  
    currentIteration.forEach((element, index) => {
      if (index === 0) {
        errorMessage = createStartMessage(element);
      } else if (index === currentIteration.length - 1) {
        errorMessage += ` ${element}${errorMessageEnd}`;
      } else {
        errorMessage += `, ${element}`;
      }
    });
  } else if (currentIteration.length == 1) {
    errorMessage = `${createStartMessage(currentIteration[0])}${errorMessageEnd}`;
  }
  
  return errorMessage;
};

export const checkRange = (element: ViewElementNumber | ViewElementString, data: number): string => {
  const number = data;

  let expression = undefined;

  if (isViewElementString(element)) {
    expression = element.length;

    errorMessageStart = rangeErrorStartString;
    errorMessageMiddleMaxPart = rangeErrorInnerMaxTextString;
    errorMessageMiddleMinPart = rangeErrorInnerMinTextString;
    errorMessageEnd = rangeErrorEndTextString;

  } else if (isViewElementNumber(element)) {
    expression = element.range;

    errorMessageStart = rangeErrorStartNumber;
    errorMessageMiddleMaxPart = rangeErrorInnerMaxTextNumber;
    errorMessageMiddleMinPart = rangeErrorInnerMinTextNumber;
    errorMessageEnd = rangeErrorEndTextNumber;
  }

  if (expression) {
    if (isYangOperator(expression)) {

      const errorMessage = getRangeErrorMessages(expression, data);
      return errorMessage;

    } else
    if (isYangRange(expression)) {

      if (!isNaN(expression.min)) {
        if (number < expression.min) {
          return `${errorMessageStart} ${errorMessageMiddleMinPart} ${expression.min}${errorMessageEnd}`;
        }
      }

      if (!isNaN(expression.max)) {
        if (number > expression.max) {
          return `${errorMessageStart} ${errorMessageMiddleMaxPart} ${expression.max}${errorMessageEnd}`;
        }
      }
    }
  }

  return '';
};

const getRegexRecursively = (value: Operator<RegExp>, data: string): boolean[] => {
  let currentItteration: boolean[] = [];
  for (let i = 0; i < value.arguments.length; i++) {
    const element = value.arguments[i];
    if (isRegExp(element)) {
      // if regex is found, add it to list
      currentItteration.push(element.test(data));
    } else if (isRegExpOperator(element)) {
      //if RegexExpression is found, try to get regex from it
      currentItteration = currentItteration.concat(getRegexRecursively(element, data));
    }
  }
  
  if (value.operation === 'OR') {
    // if one is true, all are true, all found items can be discarded
    let result = currentItteration.find(element => element);
    if (result) {
      return [];
    }
  }
  return currentItteration;
};
  
const isPatternValid = (value: Operator<RegExp>, data: string): boolean => {
  // get all regex
  const result = getRegexRecursively(value, data);
  
  if (value.operation === 'AND') {
    // if AND operation is executed...
    // no element can be false
    const check = result.find(element => element !== true);
    if (check)
      return false;
    else
      return true;
  } else {
    // if OR operation is executed...
    // ... just one element must be true
    const check = result.find(element => element === true);
    if (check)
      return true;
    else
      return false;
  
  }
};

export const checkPattern = (expression: RegExp | Operator<RegExp> | undefined, data: string): validated => {

  if (expression) {
    if (isRegExp(expression)) {
      const isValid = expression.test(data);
      if (!isValid)
        return { isValid: isValid, error: 'The input is in a wrong format.' };

    } else if (isRegExpOperator(expression)) {
      const result = isPatternValid(expression, data);

      if (!result) {
        return { isValid: false, error: 'The input is in a wrong format.' };
      }
    }
  }

  return { isValid: true };
};




