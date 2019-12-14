import { ViewElement, ViewSpecification } from "./uiModels";

export type Token = {
  name: string;
  value: string;
  start: number;
  end: number;
}

export type Statement = {
  key: string;
  arg?: string;
  sub?: Statement[];
}

export type Identity = {
  id: string,
  label: string,
  base?: string,
  description?: string,
  reference?: string,
  children?: Identity[],
  values?: Identity[],
}

export type Revision = {
  description?: string,
  reference?: string
};

export type Module = {
  name: string;
  namespace?: string;
  prefix?: string;
  identities: { [name: string]: Identity };
  revisions: { [version: string]: Revision } ;
  imports: { [prefix: string]: string };
  features: { [feature: string]: { description?: string } };
  typedefs: { [type: string]: ViewElement };
  augments: { [path: string]: ViewSpecification[] };
  groupings: { [group: string]: ViewSpecification };
  views: { [view: string]: ViewSpecification };
  elements: { [view: string]: ViewElement };
}