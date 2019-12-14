export type ViewElementBase = {
  "id": string;
  "label": string;
  "config": boolean;
  "ifFeature"?: string;
  "when"?: string;
  "mandatory"?: boolean;
  "description"?: string;
  "isList"?: boolean;
  "default"?: string;
  "status"?: "current" | "deprecated" | "obsolete",
  "reference"?: string, // https://tools.ietf.org/html/rfc7950#section-7.21.4
}

// https://tools.ietf.org/html/rfc7950#section-9.8
export type ViewElementBinary = ViewElementBase & {
  "uiType": "binary";
  "length"?: number;  // number of octets
}

// https://tools.ietf.org/html/rfc7950#section-9.7.4
export type ViewElementBits = ViewElementBase & {
  "uiType": "bits";
  "flags": {
    [name: string]: number | undefined;    // 0 - 4294967295
  }
}

// https://tools.ietf.org/html/rfc7950#section-9
export type ViewElementString = ViewElementBase & {
  "uiType": "string";
  "pattern"?: string[];
  "length"?: string;
  "invertMatch"?: true;
}

// https://tools.ietf.org/html/rfc7950#section-9.3
export type ViewElementNumber = ViewElementBase & {
  "uiType": "number";
  "min"?: number;
  "max"?: number;
  "units"?: string;
  "format"?: string;
  "fDigits"?: number;
}

// https://tools.ietf.org/html/rfc7950#section-9.5
export type ViewElementBoolean = ViewElementBase & {
  "uiType": "boolean";
  "trueValue"?: string;
  "falseValue"?: string;
}

// https://tools.ietf.org/html/rfc7950#section-9.6.4
export type ViewElementSelection = ViewElementBase & {
  "uiType": "selection";
  "multiSelect"?: boolean
  "options": {
    "key": string;
    "value": string;
    "description"?: string,
    "status"?: "current" | "deprecated" | "obsolete",
    "reference"?: string,
  }[];
}

// is a list if isList is true ;-)
export type ViewElementObject = ViewElementBase & {
  "uiType": "object";
  "isList"?: false;
  "viewId": string;
}

// Hint: read only lists do not need a key
export type ViewElementList = (ViewElementBase & {
  "uiType": "object";
  "isList": true;
  "viewId": string;
  "key"?: string;
});

export type ViewElementReference = ViewElementBase & {
  "uiType": "reference";
  "referencePath": string;
  "ref": (currentPath: string) => ViewElement | null;
}

export type ViewElement =
  | ViewElementBits
  | ViewElementBinary
  | ViewElementString
  | ViewElementNumber
  | ViewElementBoolean
  | ViewElementObject
  | ViewElementList
  | ViewElementSelection
  | ViewElementReference;

export const isViewElementString = (viewElement: ViewElement): viewElement is ViewElementString => {
  return viewElement && viewElement.uiType === "string";
}

export const isViewElementNumber = (viewElement: ViewElement): viewElement is ViewElementNumber => {
  return viewElement && viewElement.uiType === "number" ;
}

export const isViewElementBoolean = (viewElement: ViewElement): viewElement is ViewElementBoolean => {
  return viewElement && viewElement.uiType === "boolean";
}

export const isViewElementObject = (viewElement: ViewElement): viewElement is ViewElementObject => {
  return viewElement && viewElement.uiType === "object" && viewElement.isList === false;
}

export const isViewElementList = (viewElement: ViewElement): viewElement is ViewElementList => {
  return viewElement && viewElement.uiType === "object" && viewElement.isList === true;
}

export const isViewElementObjectOrList = (viewElement: ViewElement): viewElement is ViewElementObject | ViewElementList => {
  return viewElement && viewElement.uiType === "object";
}

export const isViewElementSelection = (viewElement: ViewElement): viewElement is ViewElementSelection => {
  return viewElement && viewElement.uiType === "selection";
}

export const isViewElementReference = (viewElement: ViewElement): viewElement is ViewElementReference => {
  return viewElement && viewElement.uiType === "reference";
}

export type ViewSpecification = {
  "id": string;
  "name": string;
  "title"?: string;
  "parentView"?: string;
  "language": string;
  "ifFeature"?: string;
  "when"?: string;
  "uses"?: string[];
  "elements": { [name: string]: ViewElement };
  readonly "canEdit": boolean;
}
