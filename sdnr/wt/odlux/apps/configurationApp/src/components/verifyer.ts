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

import { Expression, YangRange, Operator, ViewElementNumber, ViewElementString, isViewElementNumber, isViewElementString } from '../models/uiModels';

export type validated = { isValid: boolean, error?: string }

export type validatedRange = { isValid: boolean, error?: string };


const rangeErrorStartNumber = "The entered number must be";
const rangeErrorinnerMinTextNumber = "greater or equals than";
const rangeErrorinnerMaxTextNumber = "less or equals than";
const rangeErrorEndTextNumber = ".";

const rangeErrorStartString = "The entered text must have";
const rangeErrorinnerMinTextString = "no more than";
const rangeErrorinnerMaxTextString = "less than";
const rangeErrorEndTextString = " characters.";

let errorMessageStart = "";
let errorMessageMiddleMinPart = "";
let errorMessageMiddleMaxPart = "";
let errorMessageEnd = "";


export function checkRange(element: ViewElementNumber | ViewElementString, data: number): string {

    //let test1: Operator<YangRange> = { operation: "AND", arguments: [{ operation: "OR", arguments: [{ operation: "AND", arguments: [new RegExp("^z", "g"), new RegExp("z$", "g")] }, new RegExp("^abc", "g"), new RegExp("^123", "g")] }, new RegExp("^def", "g"), new RegExp("^ppp", "g"), new RegExp("^aaa", "g")] };
    //let test1: Operator<YangRange> = { operation: "AND", arguments: [{ operation: "OR", arguments: [{ operation: "AND", arguments: [{ min: -5, max: 10 }, { min: -30, max: -20 }] }, { min: 8, max: 15 }] }] };
    //let test1: Operator<YangRange> = { operation: "OR", arguments: [{ operation: "OR", arguments: [{ min: -50, max: -40 }] }, { min: -30, max: -20 }, { min: 8, max: 15 }] };
    //let test1: Operator<YangRange> = { operation: "AND", arguments: [{ operation: "OR", arguments: [{ min: -5, max: 10 }, { min: 17, max: 23 }] }] };

    const number = data;

    var expression = undefined;

    if (isViewElementString(element)) {
        expression = element.length;

        errorMessageStart = rangeErrorStartString;
        errorMessageMiddleMaxPart = rangeErrorinnerMaxTextString;
        errorMessageMiddleMinPart = rangeErrorinnerMinTextString;
        errorMessageEnd = rangeErrorEndTextString;

    } else if (isViewElementNumber(element)) {
        expression = element.range;

        errorMessageStart = rangeErrorStartNumber;
        errorMessageMiddleMaxPart = rangeErrorinnerMaxTextNumber;
        errorMessageMiddleMinPart = rangeErrorinnerMinTextNumber;
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


    return "";
}

function isYangRange(val: YangRange | Operator<YangRange>): val is YangRange {
    return (val as YangRange).min !== undefined;
}

function isYangOperator(val: YangRange | Operator<YangRange>): val is Operator<YangRange> {
    return (val as Operator<YangRange>).operation !== undefined;
}

function getRangeErrorMessagesRecursively(value: Operator<YangRange>, data: number): string[] {
    let currentItteration: string[] = [];
    console.log(value);

    // itterate over all elements
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
                currentItteration.push(`${value.operation.toLocaleLowerCase()} ${errorMessageMiddleMinPart} ${min}`);
            } else if (max != undefined) {
                currentItteration.push(`${value.operation.toLocaleLowerCase()} ${errorMessageMiddleMaxPart} ${max}`);

            }

        } else if (isYangOperator(element)) {

            //get errormessages from expression
            const result = getRangeErrorMessagesRecursively(element, data);
            if (result.length === 0) {
                isNumberCorrect = true;
            }
            currentItteration = currentItteration.concat(result);
        }

        // if its an OR operation, the number has been checked and min/max are empty (thus not violated)
        // delete everything found (because at least one found is correct, therefore all are correct) and break from loop
        if (min === undefined && max === undefined && isNumberCorrect && value.operation === "OR") {

            currentItteration.splice(0, currentItteration.length);
            break;
        }
    }

    return currentItteration;
}

function getRangeErrorMessages(value: Operator<YangRange>, data: number): string {

    const currentItteration = getRangeErrorMessagesRecursively(value, data);

    // build complete error message from found parts
    let errormessage = "";
    if (currentItteration.length > 1) {

        currentItteration.forEach((element, index) => {
            if (index === 0) {
                errormessage = createStartMessage(element);
            } else if (index === currentItteration.length - 1) {
                errormessage += ` ${element}${errorMessageEnd}`;
            } else {
                errormessage += `, ${element}`
            }
        });
    } else if (currentItteration.length == 1) {
        errormessage = `${createStartMessage(currentItteration[0])}${errorMessageEnd}`;
    }

    return errormessage;
}

function createStartMessage(element: string) {

    //remove leading or or and from text
    if (element.startsWith("and"))
        element = element.replace("and", "");
    else if (element.startsWith("or"))
        element = element.replace("or", "");

    return `${errorMessageStart} ${element}`;
}

export const checkPattern = (expression: RegExp | Operator<RegExp> | undefined, data: string): validated => {

    if (expression) {
        if (isRegExp(expression)) {
            const isValid = expression.test(data);
            if (!isValid)
                return { isValid: isValid, error: "The input is in a wrong format." };

        } else if (isRegExpOperator(expression)) {
            const result = isPatternValid(expression, data);

            if (!result) {
                return { isValid: false, error: "The input is in a wrong format." };
            }
        }
    }

    return { isValid: true }
}

function getRegexRecursively(value: Operator<RegExp>, data: string): boolean[] {
    let currentItteration: boolean[] = [];
    for (let i = 0; i < value.arguments.length; i++) {
        const element = value.arguments[i];
        if (isRegExp(element)) {
            // if regex is found, add it to list
            currentItteration.push(element.test(data))
        } else if (isRegExpOperator(element)) {
            //if RegexExpression is found, try to get regex from it
            currentItteration = currentItteration.concat(getRegexRecursively(element, data));
        }
    }

    if (value.operation === "OR") {
        // if one is true, all are true, all found items can be discarded
        let result = currentItteration.find(element => element);
        if (result) {
            return [];
        }
    }
    return currentItteration;
}

function isPatternValid(value: Operator<RegExp>, data: string): boolean {


    // get all regex
    const result = getRegexRecursively(value, data);
    console.log(value);


    if (value.operation === "AND") {
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
}

function isRegExp(val: RegExp | Operator<RegExp>): val is RegExp {
    return (val as RegExp).source !== undefined;
}

function isRegExpOperator(val: RegExp | Operator<RegExp>): val is Operator<RegExp> {
    return (val as Operator<RegExp>).operation !== undefined;
}