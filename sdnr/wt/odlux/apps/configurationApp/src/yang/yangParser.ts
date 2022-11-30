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
import { Token, Statement, Module, Identity, ModuleState } from "../models/yang";
import {
  ViewSpecification, ViewElement, isViewElementObjectOrList, ViewElementBase,
  isViewElementReference, ViewElementChoise, ViewElementBinary, ViewElementString, isViewElementString,
  isViewElementNumber, ViewElementNumber, Expression, YangRange, ViewElementUnion, ViewElementRpc, isViewElementRpc, ResolveFunction, ViewElementDate
} from "../models/uiModels";
import { yangService } from "../services/yangService";


export const splitVPath = (vPath: string, vPathParser: RegExp): RegExpMatchArray[] => {
  const pathParts: RegExpMatchArray[] = [];
  let partMatch: RegExpExecArray | null;
  if (vPath) do {
    partMatch = vPathParser.exec(vPath);
    if (partMatch) {
      pathParts.push(partMatch);
    }
  } while (partMatch)
  return pathParts;
}

class YangLexer {

  private pos: number = 0;
  private buf: string = "";

  constructor(input: string) {
    this.pos = 0;
    this.buf = input;
  }

  private _optable: { [key: string]: string } = {
    ';': 'SEMI',
    '{': 'L_BRACE',
    '}': 'R_BRACE',
  };

  private _isNewline(char: string): boolean {
    return char === '\r' || char === '\n';
  }

  private _isWhitespace(char: string): boolean {
    return char === ' ' || char === '\t' || this._isNewline(char);
  }

  private _isDigit(char: string): boolean {
    return char >= '0' && char <= '9';
  }

  private _isAlpha(char: string): boolean {
    return (char >= 'a' && char <= 'z') ||
      (char >= 'A' && char <= 'Z')
  }

  private _isAlphanum(char: string): boolean {
    return this._isAlpha(char) || this._isDigit(char) ||
      char === '_' || char === '-' || char === '.';
  }

  private _skipNontokens() {
    while (this.pos < this.buf.length) {
      const char = this.buf.charAt(this.pos);
      if (this._isWhitespace(char)) {
        this.pos++;
      } else {
        break;
      }
    }
  }

  private _processString(terminator: string | null): Token {
    // this.pos points at the opening quote. Find the ending quote.
    let end_index = this.pos + 1;
    while (end_index < this.buf.length) {
      const char = this.buf.charAt(end_index);
      if (char === "\\") {
        end_index += 2;
        continue;
      };
      if (terminator === null && (this._isWhitespace(char) || this._optable[char] !== undefined) || char === terminator) {
        break;
      }
      end_index++;
    }

    if (end_index >= this.buf.length) {
      throw Error('Unterminated quote at ' + this.pos);
    } else {
      const start = this.pos + (terminator ? 1 : 0);
      const end = end_index;
      const tok = {
        name: 'STRING',
        value: this.buf.substring(start, end),
        start,
        end
      };
      this.pos = terminator ? end + 1 : end;
      return tok;
    }
  }

  private _processIdentifier(): Token {
    let endpos = this.pos + 1;
    while (endpos < this.buf.length && this._isAlphanum(this.buf.charAt(endpos))) {
      ++endpos;
    }

    let name = 'IDENTIFIER'
    if (this.buf.charAt(endpos) === ":") {
      name = 'IDENTIFIERREF';
      ++endpos;
      while (endpos < this.buf.length && this._isAlphanum(this.buf.charAt(endpos))) {
        ++endpos;
      }
    }

    const tok = {
      name: name,
      value: this.buf.substring(this.pos, endpos),
      start: this.pos,
      end: endpos
    };

    this.pos = endpos;
    return tok;
  }

  private _processNumber(): Token {
    let endpos = this.pos + 1;
    while (endpos < this.buf.length &&
      this._isDigit(this.buf.charAt(endpos))) {
      endpos++;
    }

    const tok = {
      name: 'NUMBER',
      value: this.buf.substring(this.pos, endpos),
      start: this.pos,
      end: endpos
    };
    this.pos = endpos;
    return tok;
  }

  private _processLineComment() {
    var endpos = this.pos + 2;
    // Skip until the end of the line
    while (endpos < this.buf.length && !this._isNewline(this.buf.charAt(endpos))) {
      endpos++;
    }
    this.pos = endpos + 1;
  }

  private _processBlockComment() {
    var endpos = this.pos + 2;
    // Skip until the end of the line
    while (endpos < this.buf.length && !((this.buf.charAt(endpos) === "/" && this.buf.charAt(endpos - 1) === "*"))) {
      endpos++;
    }
    this.pos = endpos + 1;
  }

  public tokenize(): Token[] {
    const result: Token[] = [];
    this._skipNontokens();
    while (this.pos < this.buf.length) {

      const char = this.buf.charAt(this.pos);
      const op = this._optable[char];

      if (op !== undefined) {
        result.push({ name: op, value: char, start: this.pos, end: ++this.pos });
      } else if (this._isAlpha(char)) {
        result.push(this._processIdentifier());
        this._skipNontokens();
        const peekChar = this.buf.charAt(this.pos);
        if (this._optable[peekChar] === undefined) {
          result.push((peekChar !== "'" && peekChar !== '"')
            ? this._processString(null)
            : this._processString(peekChar));
        }
      } else if (char === '/' && this.buf.charAt(this.pos + 1) === "/") {
        this._processLineComment();
      } else if (char === '/' && this.buf.charAt(this.pos + 1) === "*") {
        this._processBlockComment();
      } else {
        throw Error('Token error at ' + this.pos + " " + this.buf[this.pos]);
      }
      this._skipNontokens();
    }
    return result;
  }

  public tokenize2(): Statement {
    let stack: Statement[] = [{ key: "ROOT", sub: [] }];
    let current: Statement | null = null;

    this._skipNontokens();
    while (this.pos < this.buf.length) {

      const char = this.buf.charAt(this.pos);
      const op = this._optable[char];

      if (op !== undefined) {
        if (op === "L_BRACE") {
          current && stack.unshift(current);
          current = null;
        } else if (op === "R_BRACE") {
          current = stack.shift() || null;
        }
        this.pos++;
      } else if (this._isAlpha(char) || char === "_") {
        const key = this._processIdentifier().value;
        this._skipNontokens();
        let peekChar = this.buf.charAt(this.pos);
        let arg = undefined;
        if (this._optable[peekChar] === undefined) {
          arg = (peekChar === '"' || peekChar === "'")
            ? this._processString(peekChar).value
            : this._processString(null).value;
        }
        do {
          this._skipNontokens();
          peekChar = this.buf.charAt(this.pos);
          if (peekChar !== "+") break;
          this.pos++;
          this._skipNontokens();
          peekChar = this.buf.charAt(this.pos);
          arg += (peekChar === '"' || peekChar === "'")
            ? this._processString(peekChar).value
            : this._processString(null).value;
        } while (true);
        current = { key, arg, sub: [] };
        stack[0].sub!.push(current);
      } else if (char === '/' && this.buf.charAt(this.pos + 1) === "/") {
        this._processLineComment();
      } else if (char === '/' && this.buf.charAt(this.pos + 1) === "*") {
        this._processBlockComment();
      } else {
        throw Error('Token error at ' + this.pos + " " + this.buf.slice(this.pos - 10, this.pos + 10));
      }
      this._skipNontokens();
    }
    if (stack[0].key !== "ROOT" || !stack[0].sub![0]) {
      throw new Error("Internal Perser Error");
    }
    return stack[0].sub![0];
  }
}

export class YangParser {
  private _groupingsToResolve: ViewSpecification[] = [];

  private _identityToResolve: (() => void)[] = [];
  private _unionsToResolve: (() => void)[] = [];
  private _modulesToResolve: (() => void)[] = [];

  private _modules: { [name: string]: Module } = {};
  private _views: ViewSpecification[] = [{
    id: "0",
    name: "root",
    language: "en-US",
    canEdit: false,
    config: true,
    parentView: "0",
    title: "root",
    elements: {},
  }];

  public static ResolveStack = Symbol("ResolveStack");

  constructor(private _unavailableCapabilities: { failureReason: string; capability: string; }[] = [], private _importOnlyModules: { name: string; revision: string; }[] = [], private nodeId: string) {
   
  }

  public get modules() {
    return this._modules;
  }

  public get views() {
    return this._views;
  }

  public async addCapability(capability: string, version?: string, parentImportOnlyModule?: boolean) {
    // do not add twice
    if (this._modules[capability]) {
      // console.warn(`Skipped capability: ${capability} since already contained.` );
      return;
    }

    // // do not add unavailable capabilities
    // if (this._unavailableCapabilities.some(c => c.capability === capability)) {
    //   // console.warn(`Skipped capability: ${capability} since it is marked as unavailable.` );
    //   return;
    // }
    const data = await yangService.getCapability(capability, this.nodeId, version);
    if (!data) {
      throw new Error(`Could not load yang file for ${capability}.`);
    }

    const rootStatement = new YangLexer(data).tokenize2();

    if (rootStatement.key !== "module") {
      throw new Error(`Root element of ${capability} is not a module.`);
    }
    if (rootStatement.arg !== capability) {
      throw new Error(`Root element capability ${rootStatement.arg} does not requested ${capability}.`);
    }

    const isUnavailable = this._unavailableCapabilities.some(c => c.capability === capability);
    const isImportOnly = parentImportOnlyModule === true || this._importOnlyModules.some(c => c.name === capability);
    
    const module = this._modules[capability] = {
      name: rootStatement.arg,
      revisions: {},
      imports: {},
      features: {},
      identities: {},
      augments: {},
      groupings: {},
      typedefs: {},
      views: {},
      elements: {},
      state: isUnavailable
           ? ModuleState.unavailable 
           : isImportOnly 
             ? ModuleState.importOnly
             : ModuleState.stable,
    };

    await this.handleModule(module, rootStatement, capability);
  }

  private async handleModule(module: Module, rootStatement: Statement, capability: string) {

    // extract namespace && prefix
    module.namespace = this.extractValue(rootStatement, "namespace");
    module.prefix = this.extractValue(rootStatement, "prefix");
    if (module.prefix) {
      module.imports[module.prefix] = capability;
    }

    // extract revisions
    const revisions = this.extractNodes(rootStatement, "revision");
    module.revisions = {
      ...module.revisions,
      ...revisions.reduce<{ [version: string]: {} }>((acc, version) => {
        if (!version.arg) {
          throw new Error(`Module [${module.name}] has a version w/o version number.`);
        }
        const description = this.extractValue(version, "description");
        const reference = this.extractValue(version, "reference");
        acc[version.arg] = {
          description,
          reference,
        };
        return acc;
      }, {})
    };

    // extract features
    const features = this.extractNodes(rootStatement, "feature");
    module.features = {
      ...module.features,
      ...features.reduce<{ [version: string]: {} }>((acc, feature) => {
        if (!feature.arg) {
          throw new Error(`Module [${module.name}] has a feature w/o name.`);
        }
        const description = this.extractValue(feature, "description");
        acc[feature.arg] = {
          description,
        };
        return acc;
      }, {})
    };

    // extract imports
    const imports = this.extractNodes(rootStatement, "import");
    module.imports = {
      ...module.imports,
      ...imports.reduce<{ [key: string]: string }>((acc, imp) => {
        const prefix = imp.sub && imp.sub.filter(s => s.key === "prefix");
        if (!imp.arg) {
          throw new Error(`Module [${module.name}] has an import with neither name nor prefix.`);
        }
        acc[prefix && prefix.length === 1 && prefix[0].arg || imp.arg] = imp.arg;
        return acc;
      }, {})
    };

    // import all required files and set module state 
    if (imports) for (let ind = 0; ind < imports.length; ++ind) {
      const moduleName = imports[ind].arg!; 

      //TODO: Fix imports getting loaded without revision
      await this.addCapability(moduleName, undefined, module.state === ModuleState.importOnly);
      const importedModule = this._modules[imports[ind].arg!];
      if (importedModule && importedModule.state > ModuleState.stable) {
          module.state = Math.max(module.state, ModuleState.instable);
      }
    }

    this.extractTypeDefinitions(rootStatement, module, "");

    this.extractIdentities(rootStatement, 0, module, "");

    const groupings = this.extractGroupings(rootStatement, 0, module, "");
    this._views.push(...groupings);

    const augments = this.extractAugments(rootStatement, 0, module, "");
    this._views.push(...augments);

    // the default for config on module level is config = true;
    const [currentView, subViews] = this.extractSubViews(rootStatement, 0, module, "");
    this._views.push(currentView, ...subViews);

    // create the root elements for this module
    module.elements = currentView.elements;
    this._modulesToResolve.push(() => {
      Object.keys(module.elements).forEach(key => {
        const viewElement = module.elements[key];
        if (!(isViewElementObjectOrList(viewElement) || isViewElementRpc(viewElement))) {
          console.error(new Error(`Module: [${module}]. Only Object, List or RPC are allowed on root level.`));
        }
        if (isViewElementObjectOrList(viewElement)) {
          const viewIdIndex = Number(viewElement.viewId);
          module.views[key] = this._views[viewIdIndex];
        }
        
        // add only the UI View if the module is available
        if (module.state === ModuleState.stable || module.state === ModuleState.instable) this._views[0].elements[key] = module.elements[key];
      });
    });
    return module;
  }

  public postProcess() {

    // execute all post processes like resolving in proper order
    this._unionsToResolve.forEach(cb => {
      try { cb(); } catch (error) {
        console.warn(error.message);
      }
    });

    // process all groupings
    this._groupingsToResolve.filter(vs => vs.uses && vs.uses[ResolveFunction]).forEach(vs => {
      try { vs.uses![ResolveFunction] !== undefined && vs.uses![ResolveFunction]!("|"); } catch (error) {
        console.warn(`Error resolving: [${vs.name}] [${error.message}]`);
      }
    });

    /**
     * This is to fix the issue for sequential execution of modules based on their child and parent relationship
     * We are sorting the module object based on their augment status
     */
      Object.keys(this.modules)
      .map(elem => {
          if(this.modules[elem].augments && Object.keys(this.modules[elem].augments).length > 0) {
              const {augments, ...rest} = this.modules[elem];
              const partsOfKeys = Object.keys(augments).map((key) => (key.split("/").length - 1))
              this.modules[elem].executionOrder= Math.max(...partsOfKeys)
          } else {
            this.modules[elem].executionOrder=0;
          }
      })

    // process all augmentations / sort by namespace changes to ensure proper order 
    Object.keys(this.modules).sort((a, b) => this.modules[a].executionOrder! - this.modules[b].executionOrder!).forEach(modKey => {
      const module = this.modules[modKey];
      const augmentKeysWithCounter = Object.keys(module.augments).map((key) => {
        const pathParts = splitVPath(key, /(?:(?:([^\/\:]+):)?([^\/]+))/g);  // 1 = opt: namespace / 2 = property 
        let nameSpaceChangeCounter = 0;
        let currentNS = module.name; // init namespace
        pathParts.forEach(([ns, _])=> {
          if (ns === currentNS){
            currentNS = ns;
            nameSpaceChangeCounter++;
          }
        });
        return {
          key,
          nameSpaceChangeCounter,
        }
      });
      
      const augmentKeys = augmentKeysWithCounter
        .sort((a,b) => a.nameSpaceChangeCounter > b.nameSpaceChangeCounter ? 1 : a.nameSpaceChangeCounter === b.nameSpaceChangeCounter ? 0 : -1 )
        .map((a) => a.key);

      augmentKeys.forEach(augKey => {
        const augments = module.augments[augKey];
        const viewSpec = this.resolveView(augKey);
        if (!viewSpec) console.warn(`Could not find view to augment [${augKey}] in [${module.name}].`);
        if (augments && viewSpec) {
          augments.forEach(augment => Object.keys(augment.elements).forEach(key => {
            const elm = augment.elements[key];
            viewSpec.elements[key] = {
              ...augment.elements[key],
              
              when: elm.when ? `(${augment.when}) and (${elm.when})` : augment.when,
              ifFeature: elm.ifFeature ? `(${augment.ifFeature}) and (${elm.ifFeature})` : augment.ifFeature,
            };
          }));
        }
      });
    });

    // process Identities
    const traverseIdentity = (identities: Identity[]) => {
      const result: Identity[] = [];
      for (let identity of identities) {
        if (identity.children && identity.children.length > 0) {
          result.push(...traverseIdentity(identity.children));
        } else {
          result.push(identity);
        }
      }
      return result;
    }

    const baseIdentities: Identity[] = [];
    Object.keys(this.modules).forEach(modKey => {
      const module = this.modules[modKey];
      Object.keys(module.identities).forEach(idKey => {
        const identity = module.identities[idKey];
        if (identity.base != null) {
          const base = this.resolveIdentity(identity.base, module);
          base.children?.push(identity);
        } else {
          baseIdentities.push(identity);
        }
      });
    });
    baseIdentities.forEach(identity => {
      identity.values = identity.children && traverseIdentity(identity.children) || [];
    });

    this._identityToResolve.forEach(cb => {
      try { cb(); } catch (error) {
        console.warn(error.message);
      }
    });

    this._modulesToResolve.forEach(cb => {
      try { cb(); } catch (error) {
        console.warn(error.message);
      }
    });

    // resolve readOnly
    const resolveReadOnly = (view: ViewSpecification, parentConfig: boolean) => {
      
      // update view config
      view.config = view.config && parentConfig;
      
      Object.keys(view.elements).forEach((key) => {
        const elm = view.elements[key];

        // update element config
        elm.config = elm.config && view.config;
        
        // update all sub-elements of objects
        if (elm.uiType === "object") {
          resolveReadOnly(this.views[+elm.viewId], elm.config);
        }

      })
    }

    const dump = resolveReadOnly(this.views[0], true); 
  };

  private _nextId = 1;
  private get nextId() {
    return this._nextId++;
  }

  private extractNodes(statement: Statement, key: string): Statement[] {
    return statement.sub && statement.sub.filter(s => s.key === key) || [];
  }

  private extractValue(statement: Statement, key: string): string | undefined;
  private extractValue(statement: Statement, key: string, parser: RegExp): RegExpExecArray | undefined;
  private extractValue(statement: Statement, key: string, parser?: RegExp): string | RegExpExecArray | undefined {
    const typeNodes = this.extractNodes(statement, key);
    const rawValue = typeNodes.length > 0 && typeNodes[0].arg || undefined;
    return parser
      ? rawValue && parser.exec(rawValue) || undefined
      : rawValue;
  }

  private extractTypeDefinitions(statement: Statement, module: Module, currentPath: string): void {
    const typedefs = this.extractNodes(statement, "typedef");
    typedefs && typedefs.forEach(def => {
      if (!def.arg) {
        throw new Error(`Module: [${module.name}]. Found typefed without name.`);
      }
      module.typedefs[def.arg] = this.getViewElement(def, module, 0, currentPath, false);
    });
  }

  /** Handles groupings like named Container */
  private extractGroupings(statement: Statement, parentId: number, module: Module, currentPath: string): ViewSpecification[] {
    const subViews: ViewSpecification[] = [];
    const groupings = this.extractNodes(statement, "grouping");
    if (groupings && groupings.length > 0) {
      subViews.push(...groupings.reduce<ViewSpecification[]>((acc, cur) => {
        if (!cur.arg) {
          throw new Error(`Module: [${module.name}][${currentPath}]. Found grouping without name.`);
        }
        const grouping = cur.arg;

        // the default for config on module level is config = true;
        const [currentView, subViews] = this.extractSubViews(cur, /* parentId */ -1, module, currentPath);
        grouping && (module.groupings[grouping] = currentView);
        acc.push(currentView, ...subViews);
        return acc;
      }, []));
    }

    return subViews;
  }

  /** Handles augments also like named container */
  private extractAugments(statement: Statement, parentId: number, module: Module, currentPath: string): ViewSpecification[] {
    const subViews: ViewSpecification[] = [];
    const augments = this.extractNodes(statement, "augment");
    if (augments && augments.length > 0) {
      subViews.push(...augments.reduce<ViewSpecification[]>((acc, cur) => {
        if (!cur.arg) {
          throw new Error(`Module: [${module.name}][${currentPath}]. Found augment without path.`);
        }
        const augment = this.resolveReferencePath(cur.arg, module);

        // the default for config on module level is config = true;
        const [currentView, subViews] = this.extractSubViews(cur, parentId, module, currentPath);
        if (augment) {
          module.augments[augment] = module.augments[augment] || [];
          module.augments[augment].push(currentView);
        }
        acc.push(currentView, ...subViews);
        return acc;
      }, []));
    }

    return subViews;
  }

  /** Handles identities  */
  private extractIdentities(statement: Statement, parentId: number, module: Module, currentPath: string) {
    const identities = this.extractNodes(statement, "identity");
    module.identities = identities.reduce<{ [name: string]: Identity }>((acc, cur) => {
      if (!cur.arg) {
        throw new Error(`Module: [${module.name}][${currentPath}]. Found identiy without name.`);
      }
      acc[cur.arg] = {
        id: `${module.name}:${cur.arg}`,
        label: cur.arg,
        base: this.extractValue(cur, "base"),
        description: this.extractValue(cur, "description"),
        reference: this.extractValue(cur, "reference"),
        children: []
      }
      return acc;
    }, {});
  }

   // Hint: use 0 as parentId for rootElements and -1 for rootGroupings.
  private extractSubViews(statement: Statement, parentId: number, module: Module, currentPath: string): [ViewSpecification, ViewSpecification[]] {
    // used for scoped definitions
    const context: Module = {
      ...module,
      typedefs: {
        ...module.typedefs
      }
    };

    const currentId = this.nextId;
    const subViews: ViewSpecification[] = [];
    let elements: ViewElement[] = [];

    const configValue = this.extractValue(statement, "config");
    const config = configValue == null ? true : configValue.toLocaleLowerCase() !== "false";

    // extract conditions
    const ifFeature = this.extractValue(statement, "if-feature");
    const whenCondition = this.extractValue(statement, "when");
    if (whenCondition) console.warn("Found in [" + context.name + "]" + currentPath + " when: " + whenCondition);

    // extract all scoped typedefs
    this.extractTypeDefinitions(statement, context, currentPath);

    // extract all scoped groupings
    subViews.push(
      ...this.extractGroupings(statement, parentId, context, currentPath)
    );

    // extract all container
    const container = this.extractNodes(statement, "container");
    if (container && container.length > 0) {
      subViews.push(...container.reduce<ViewSpecification[]>((acc, cur) => {
        if (!cur.arg) {
          throw new Error(`Module: [${context.name}]${currentPath}. Found container without name.`);
        }
        const [currentView, subViews] = this.extractSubViews(cur, currentId, context, `${currentPath}/${context.name}:${cur.arg}`);
        elements.push({
          id: parentId === 0 ? `${context.name}:${cur.arg}` : cur.arg,
          label: cur.arg,
          path: currentPath,
          module: context.name || module.name || '',
          uiType: "object",
          viewId: currentView.id,
          config: currentView.config,
        });
        acc.push(currentView, ...subViews);
        return acc;
      }, []));
    }

    // process all lists
    // a list is a list of containers with the leafs contained in the list
    const lists = this.extractNodes(statement, "list");
    if (lists && lists.length > 0) {
      subViews.push(...lists.reduce<ViewSpecification[]>((acc, cur) => {
        let elmConfig = config;
        if (!cur.arg) {
          throw new Error(`Module: [${context.name}]${currentPath}. Found list without name.`);
        }
        const key = this.extractValue(cur, "key") || undefined;
        if (elmConfig && !key) {
          console.warn(`Module: [${context.name}]${currentPath}. Found configurable list without key. Assume config shell be false.`);
          elmConfig = false;
        }
        const [currentView, subViews] = this.extractSubViews(cur, currentId, context, `${currentPath}/${context.name}:${cur.arg}`);
        elements.push({
          id: parentId === 0 ? `${context.name}:${cur.arg}` : cur.arg,
          label: cur.arg,
          path: currentPath,
          module: context.name || module.name || '',
          isList: true,
          uiType: "object",
          viewId: currentView.id,
          key: key,
          config: elmConfig && currentView.config,
        });
        acc.push(currentView, ...subViews);
        return acc;
      }, []));
    }

    // process all leaf-lists
    // a leaf-list is a list of some type
    const leafLists = this.extractNodes(statement, "leaf-list");
    if (leafLists && leafLists.length > 0) {
      elements.push(...leafLists.reduce<ViewElement[]>((acc, cur) => {
        const element = this.getViewElement(cur, context, parentId, currentPath, true);
        element && acc.push(element);
        return acc;
      }, []));
    }

    // process all leafs
    // a leaf is mainly a property of an object
    const leafs = this.extractNodes(statement, "leaf");
    if (leafs && leafs.length > 0) {
      elements.push(...leafs.reduce<ViewElement[]>((acc, cur) => {
        const element = this.getViewElement(cur, context, parentId, currentPath, false);
        element && acc.push(element);
        return acc;
      }, []));
    }


    const choiceStms = this.extractNodes(statement, "choice");
    if (choiceStms && choiceStms.length > 0) {
      elements.push(...choiceStms.reduce<ViewElementChoise[]>((accChoise, curChoise) => {
        if (!curChoise.arg) {
          throw new Error(`Module: [${context.name}]${currentPath}. Found choise without name.`);
        }
        // extract all cases like containers
        const cases: { id: string, label: string, description?: string, elements: { [name: string]: ViewElement } }[] = [];
        const caseStms = this.extractNodes(curChoise, "case");
        if (caseStms && caseStms.length > 0) {
          cases.push(...caseStms.reduce((accCase, curCase) => {
            if (!curCase.arg) {
              throw new Error(`Module: [${context.name}]${currentPath}/${curChoise.arg}. Found case without name.`);
            }
            const description = this.extractValue(curCase, "description") || undefined;
            const [caseView, caseSubViews] = this.extractSubViews(curCase, parentId, context, `${currentPath}/${context.name}:${curChoise.arg}`);
            subViews.push(caseView, ...caseSubViews);

            const caseDef: { id: string, label: string, description?: string, elements: { [name: string]: ViewElement } } = {
              id: parentId === 0 ? `${context.name}:${curCase.arg}` : curCase.arg,
              label: curCase.arg,
              description: description,
              elements: caseView.elements
            };
            accCase.push(caseDef);
            return accCase;
          }, [] as { id: string, label: string, description?: string, elements: { [name: string]: ViewElement } }[]));
        }

        // extract all simple cases (one case per leaf, container, etc.)
        const [choiseView, choiseSubViews] = this.extractSubViews(curChoise, parentId, context, `${currentPath}/${context.name}:${curChoise.arg}`);
        subViews.push(choiseView, ...choiseSubViews);
        cases.push(...Object.keys(choiseView.elements).reduce((accElm, curElm) => {
          const elm = choiseView.elements[curElm];
          const caseDef: { id: string, label: string, description?: string, elements: { [name: string]: ViewElement } } = {
            id: elm.id,
            label: elm.label,
            description: elm.description,
            elements: { [elm.id]: elm }
          };
          accElm.push(caseDef);
          return accElm;
        }, [] as { id: string, label: string, description?: string, elements: { [name: string]: ViewElement } }[]));

        const description = this.extractValue(curChoise, "description") || undefined;
        const configValue = this.extractValue(curChoise, "config");
        const config = configValue == null ? true : configValue.toLocaleLowerCase() !== "false";

        const mandatory = this.extractValue(curChoise, "mandatory") === "true" || false;

        const element: ViewElementChoise = {
          uiType: "choise",
          id: parentId === 0 ? `${context.name}:${curChoise.arg}` : curChoise.arg,
          label: curChoise.arg,
          path: currentPath,
          module: context.name || module.name || '',
          config: config,
          mandatory: mandatory,
          description: description,
          cases: cases.reduce((acc, cur) => {
            acc[cur.id] = cur;
            return acc;
          }, {} as { [name: string]: { id: string, label: string, description?: string, elements: { [name: string]: ViewElement } } })
        };

        accChoise.push(element);
        return accChoise;
      }, []));
    }

    const rpcStms = this.extractNodes(statement, "rpc");
    if (rpcStms && rpcStms.length > 0) {
      elements.push(...rpcStms.reduce<ViewElementRpc[]>((accRpc, curRpc) => {
        if (!curRpc.arg) {
          throw new Error(`Module: [${context.name}]${currentPath}. Found rpc without name.`);
        }

        const description = this.extractValue(curRpc, "description") || undefined;
        const configValue = this.extractValue(curRpc, "config");
        const config = configValue == null ? true : configValue.toLocaleLowerCase() !== "false";

        let inputViewId: string | undefined = undefined;
        let outputViewId: string | undefined = undefined;

        const input = this.extractNodes(curRpc, "input") || undefined;
        const output = this.extractNodes(curRpc, "output") || undefined;

        if (input && input.length > 0) {
          const [inputView, inputSubViews] = this.extractSubViews(input[0], parentId, context, `${currentPath}/${context.name}:${curRpc.arg}`);
          subViews.push(inputView, ...inputSubViews);
          inputViewId = inputView.id;
        }

        if (output && output.length > 0) {
          const [outputView, outputSubViews] = this.extractSubViews(output[0], parentId, context, `${currentPath}/${context.name}:${curRpc.arg}`);
          subViews.push(outputView, ...outputSubViews);
          outputViewId = outputView.id;
        }

        const element: ViewElementRpc = {
          uiType: "rpc",
          id: parentId === 0 ? `${context.name}:${curRpc.arg}` : curRpc.arg,
          label: curRpc.arg,
          path: currentPath,
          module: context.name || module.name || '',
          config: config,
          description: description,
          inputViewId: inputViewId,
          outputViewId: outputViewId,
        };

        accRpc.push(element);

        return accRpc;
      }, []));
    }

    // if (!statement.arg) {
    //   throw new Error(`Module: [${context.name}]. Found statement without name.`);
    // }

    const viewSpec: ViewSpecification = {
      id: String(currentId),
      parentView: String(parentId),
      ns: context.name,
      name: statement.arg != null ? statement.arg : undefined,
      title: statement.arg != null ? statement.arg : undefined,
      language: "en-us",
      canEdit: false,
      config: config,
      ifFeature: ifFeature,
      when: whenCondition,
      elements: elements.reduce<{ [name: string]: ViewElement }>((acc, cur) => {
        acc[cur.id] = cur;
        return acc;
      }, {}),
    };

    // evaluate canEdit depending on all conditions
    Object.defineProperty(viewSpec, "canEdit", {
      get: () => {
        return Object.keys(viewSpec.elements).some(key => {
          const elm = viewSpec.elements[key];
          return (!isViewElementObjectOrList(elm) && elm.config);
        });
      }
    });

    // merge in all uses references and resolve groupings
    const usesRefs = this.extractNodes(statement, "uses");
    if (usesRefs && usesRefs.length > 0) {

      viewSpec.uses = (viewSpec.uses || []);
      const resolveFunctions : ((parentElementPath: string)=>void)[] = [];

      for (let i = 0; i < usesRefs.length; ++i) {
        const groupingName = usesRefs[i].arg;
        if (!groupingName) {
          throw new Error(`Module: [${context.name}]. Found an uses statement without a grouping name.`);
        }

        viewSpec.uses.push(this.resolveReferencePath(groupingName, context));
        
        resolveFunctions.push((parentElementPath: string) => {
          const groupingViewSpec = this.resolveGrouping(groupingName, context);
          if (groupingViewSpec) {

            // resolve recursive
            const resolveFunc = groupingViewSpec.uses && groupingViewSpec.uses[ResolveFunction];
            resolveFunc && resolveFunc(parentElementPath);

            Object.keys(groupingViewSpec.elements).forEach(key => {
              const elm = groupingViewSpec.elements[key];
              // a useRef on root level need a namespace
              viewSpec.elements[parentId === 0 ? `${module.name}:${key}` : key] = {
                ...elm,
                when: elm.when ? `(${groupingViewSpec.when}) and (${elm.when})` : groupingViewSpec.when,
                ifFeature: elm.ifFeature ? `(${groupingViewSpec.ifFeature}) and (${elm.ifFeature})` : groupingViewSpec.ifFeature,
              };
            });
          }
        });
      }

      viewSpec.uses[ResolveFunction] = (parentElementPath: string) => {
        const currentElementPath = `${parentElementPath} -> ${viewSpec.ns}:${viewSpec.name}`;  
        resolveFunctions.forEach(resolve => {
            try {
                resolve(currentElementPath);
            } catch (error) {
                console.error(error);
            }
        });
        // console.log("Resolved "+currentElementPath, viewSpec);
        if (viewSpec?.uses) {
          viewSpec.uses[ResolveFunction] = undefined;
        }
      }

      this._groupingsToResolve.push(viewSpec);
    }

    return [viewSpec, subViews];
  }

  // https://tools.ietf.org/html/rfc7950#section-9.3.4
  private static decimalRange = [
    { min: -9223372036854775808, max: 9223372036854775807 },
    { min: -922337203685477580.8, max: 922337203685477580.7 },
    { min: -92233720368547758.08, max: 92233720368547758.07 },
    { min: -9223372036854775.808, max: 9223372036854775.807 },
    { min: -922337203685477.5808, max: 922337203685477.5807 },
    { min: -92233720368547.75808, max: 92233720368547.75807 },
    { min: -9223372036854.775808, max: 9223372036854.775807 },
    { min: -922337203685.4775808, max: 922337203685.4775807 },
    { min: -92233720368.54775808, max: 92233720368.54775807 },
    { min: -9223372036.854775808, max: 9223372036.854775807 },
    { min: -922337203.6854775808, max: 922337203.6854775807 },
    { min: -92233720.36854775808, max: 92233720.36854775807 },
    { min: -9223372.036854775808, max: 9223372.036854775807 },
    { min: -922337.2036854775808, max: 922337.2036854775807 },
    { min: -92233.72036854775808, max: 92233.72036854775807 },
    { min: -9223.372036854775808, max: 9223.372036854775807 },
    { min: -922.3372036854775808, max: 922.3372036854775807 },
    { min: -92.23372036854775808, max: 92.23372036854775807 },
    { min: -9.223372036854775808, max: 9.223372036854775807 },
  ];

  /** Extracts the UI View from the type in the cur statement. */
  private getViewElement(cur: Statement, module: Module, parentId: number, currentPath: string, isList: boolean): ViewElement {

    const type = this.extractValue(cur, "type");
    const defaultVal = this.extractValue(cur, "default") || undefined;
    const description = this.extractValue(cur, "description") || undefined;

    const configValue = this.extractValue(cur, "config");
    const config = configValue == null ? true : configValue.toLocaleLowerCase() !== "false";

    const extractRange = (min: number, max: number, property: string = "range"): { expression: Expression<YangRange> | undefined, min: number, max: number } => {
      const ranges = this.extractValue(this.extractNodes(cur, "type")[0]!, property) || undefined;
      const range = ranges ?.replace(/min/i, String(min)).replace(/max/i, String(max)).split("|").map(r => {
        let minValue: number;
        let maxValue: number;
        
        if (r.indexOf('..') > -1) {
            const [minStr, maxStr] = r.split('..');
            minValue = Number(minStr);
            maxValue = Number(maxStr);
        } else if (!isNaN(maxValue = Number(r && r.trim() )) ) {
            minValue = maxValue;
        } else {
            minValue = min,
            maxValue = max;
        }

        if (minValue > min) min = minValue;
        if (maxValue < max) max = maxValue;

        return {
          min: minValue,
          max: maxValue
        };
      });
      return {
        min: min,
        max: max,
        expression: range && range.length === 1
          ? range[0]
          : range && range.length > 1
            ? { operation: "OR", arguments: range }
            : undefined
      }
    };

    const extractPattern = (): Expression<RegExp> | undefined => {
      const pattern = this.extractNodes(this.extractNodes(cur, "type")[0]!, "pattern").map(p => p.arg!).filter(p => !!p).map(p => `^${p}$`);
      return pattern && pattern.length == 1
        ? new RegExp(pattern[0])
        : pattern && pattern.length > 1
          ? { operation: "AND", arguments: pattern.map(p => new RegExp(p)) }
          : undefined;
    }

    const mandatory = this.extractValue(cur, "mandatory") === "true" || false;

    if (!cur.arg) {
      throw new Error(`Module: [${module.name}]. Found element without name.`);
    }

    if (!type) {
      throw new Error(`Module: [${module.name}].[${cur.arg}]. Found element without type.`);
    }

    const element: ViewElementBase = {
      id: parentId === 0 ? `${module.name}:${cur.arg}` : cur.arg,
      label: cur.arg, 
      path: currentPath,
      module: module.name || "",
      config: config,
      mandatory: mandatory,
      isList: isList,
      default: defaultVal,
      description: description
    };

    if (type === "string") {
      const length = extractRange(0, +18446744073709551615, "length");
      return ({
        ...element,
        uiType: "string",
        length: length.expression,
        pattern: extractPattern(),
      });
    } else if (type === "boolean") {
      return ({
        ...element,
        uiType: "boolean"
      });
    } else if (type === "uint8") {
      const range = extractRange(0, +255);
      return ({
        ...element,
        uiType: "number",
        range: range.expression,
        min: range.min,
        max: range.max,
        units: this.extractValue(cur, "units") || undefined,
        format: this.extractValue(cur, "format") || undefined,
      });
    } else if (type === "uint16") {
      const range = extractRange(0, +65535);
      return ({
        ...element,
        uiType: "number",
        range: range.expression,
        min: range.min,
        max: range.max,
        units: this.extractValue(cur, "units") || undefined,
        format: this.extractValue(cur, "format") || undefined,
      });
    } else if (type === "uint32") {
      const range = extractRange(0, +4294967295);
      return ({
        ...element,
        uiType: "number",
        range: range.expression,
        min: range.min,
        max: range.max,
        units: this.extractValue(cur, "units") || undefined,
        format: this.extractValue(cur, "format") || undefined,
      });
    } else if (type === "uint64") {
      const range = extractRange(0, +18446744073709551615);
      return ({
        ...element,
        uiType: "number",
        range: range.expression,
        min: range.min,
        max: range.max,
        units: this.extractValue(cur, "units") || undefined,
        format: this.extractValue(cur, "format") || undefined,
      });
    } else if (type === "int8") {
      const range = extractRange(-128, +127);
      return ({
        ...element,
        uiType: "number",
        range: range.expression,
        min: range.min,
        max: range.max,
        units: this.extractValue(cur, "units") || undefined,
        format: this.extractValue(cur, "format") || undefined,
      });
    } else if (type === "int16") {
      const range = extractRange(-32768, +32767);
      return ({
        ...element,
        uiType: "number",
        range: range.expression,
        min: range.min,
        max: range.max,
        units: this.extractValue(cur, "units") || undefined,
        format: this.extractValue(cur, "format") || undefined,
      });
    } else if (type === "int32") {
      const range = extractRange(-2147483648, +2147483647);
      return ({
        ...element,
        uiType: "number",
        range: range.expression,
        min: range.min,
        max: range.max,
        units: this.extractValue(cur, "units") || undefined,
        format: this.extractValue(cur, "format") || undefined,
      });
    } else if (type === "int64") {
      const range = extractRange(-9223372036854775808, +9223372036854775807);
      return ({
        ...element,
        uiType: "number",
        range: range.expression,
        min: range.min,
        max: range.max,
        units: this.extractValue(cur, "units") || undefined,
        format: this.extractValue(cur, "format") || undefined,
      });
    } else if (type === "decimal64") {
      // decimalRange
      const fDigits = Number(this.extractValue(this.extractNodes(cur, "type")[0]!, "fraction-digits")) || -1;
      if (fDigits === -1) {
        throw new Error(`Module: [${module.name}][${currentPath}][${cur.arg}]. Found decimal64 with invalid fraction-digits.`);
      }
      const range = extractRange(YangParser.decimalRange[fDigits].min, YangParser.decimalRange[fDigits].max);
      return ({
        ...element,
        uiType: "number",
        fDigits: fDigits,
        range: range.expression,
        min: range.min,
        max: range.max,
        units: this.extractValue(cur, "units") || undefined,
        format: this.extractValue(cur, "format") || undefined,
      });
    } else if (type === "enumeration") {
      const typeNode = this.extractNodes(cur, "type")[0]!;
      const enumNodes = this.extractNodes(typeNode, "enum");
      return ({
        ...element,
        uiType: "selection",
        options: enumNodes.reduce<{ key: string; value: string; description?: string }[]>((acc, enumNode) => {
          if (!enumNode.arg) {
            throw new Error(`Module: [${module.name}][${currentPath}][${cur.arg}]. Found option without name.`);
          }
          const ifClause = this.extractValue(enumNode, "if-feature");
          const value = this.extractValue(enumNode, "value");
          const enumOption = {
            key: enumNode.arg,
            value: value != null ? value : enumNode.arg,
            description: this.extractValue(enumNode, "description") || undefined
          };
          // todo: ❗ handle the if clause ⚡
          acc.push(enumOption);
          return acc;
        }, [])
      });
    } else if (type === "leafref") {
      const typeNode = this.extractNodes(cur, "type")[0]!;
      const vPath = this.extractValue(typeNode, "path");
      if (!vPath) {
        throw new Error(`Module: [${module.name}][${currentPath}][${cur.arg}]. Found leafref without path.`);
      }
      const refPath = this.resolveReferencePath(vPath, module);
      const resolve = this.resolveReference.bind(this);
      const res: ViewElement = {
        ...element,
        uiType: "reference",
        referencePath: refPath,
        ref(this: ViewElement, currentPath: string) {
          const elementPath = `${currentPath}/${cur.arg}`;  
          
          const result = resolve(refPath, elementPath);
          if (!result) return undefined;

          const [resolvedElement, resolvedPath] = result;
          return resolvedElement && [{
            ...resolvedElement,
            id: this.id,
            label: this.label,
            config: this.config,
            mandatory: this.mandatory,
            isList: this.isList,
            default: this.default,
            description: this.description,
          } as ViewElement , resolvedPath] || undefined;
        }
      };
      return res;
    } else if (type === "identityref") {
      const typeNode = this.extractNodes(cur, "type")[0]!;
      const base = this.extractValue(typeNode, "base");
      if (!base) {
        throw new Error(`Module: [${module.name}][${currentPath}][${cur.arg}]. Found identityref without base.`);
      }
      const res: ViewElement = {
        ...element,
        uiType: "selection",
        options: []
      };
      this._identityToResolve.push(() => {
        const identity: Identity = this.resolveIdentity(base, module);
        if (!identity) {
          throw new Error(`Module: [${module.name}][${currentPath}][${cur.arg}]. Could not resolve identity [${base}].`);
        }
        if (!identity.values || identity.values.length === 0) {
          throw new Error(`Identity: [${base}] has no values.`);
        }
        res.options = identity.values.map(val => ({
          key: val.id,
          value: val.id,
          description: val.description
        }));
      });
      return res;
    } else if (type === "empty") {
      // todo: ❗ handle empty ⚡
      /*  9.11.  The empty Built-In Type
          The empty built-in type represents a leaf that does not have any
          value, it conveys information by its presence or absence. */
      return {
        ...element,
        uiType: "empty",
      };
    } else if (type === "union") {
      // todo: ❗ handle union ⚡
      /* 9.12.  The union Built-In Type */
      const typeNode = this.extractNodes(cur, "type")[0]!;
      const typeNodes = this.extractNodes(typeNode, "type");

      const resultingElement = {
        ...element,
        uiType: "union",
        elements: []
      } as ViewElementUnion;

      const resolveUnion = () => {
        resultingElement.elements.push(...typeNodes.map(node => {
          const stm: Statement = {
            ...cur,
            sub: [
              ...(cur.sub ?.filter(s => s.key !== "type") || []),
              node
            ]
          };
          return {
            ...this.getViewElement(stm, module, parentId, currentPath, isList),
            id: node.arg!
          };
        }));
      };

      this._unionsToResolve.push(resolveUnion);

      return resultingElement;
    } else if (type === "bits") {
      const typeNode = this.extractNodes(cur, "type")[0]!;
      const bitNodes = this.extractNodes(typeNode, "bit");
      return {
        ...element,
        uiType: "bits",
        flags: bitNodes.reduce<{ [name: string]: number | undefined; }>((acc, bitNode) => {
          if (!bitNode.arg) {
            throw new Error(`Module: [${module.name}][${currentPath}][${cur.arg}]. Found bit without name.`);
          }
          const ifClause = this.extractValue(bitNode, "if-feature");
          const pos = Number(this.extractValue(bitNode, "position"));
          acc[bitNode.arg] = pos === pos ? pos : undefined;
          return acc;
        }, {})
      };
    } else if (type === "binary") {
      return {
        ...element,
        uiType: "binary",
        length: extractRange(0, +18446744073709551615, "length"),
      };
    } else if (type === "instance-identifier") {
      // https://tools.ietf.org/html/rfc7950#page-168
      return {
        ...element,
        uiType: "string",
        length: extractRange(0, +18446744073709551615, "length"),
      };
    } else {
      // not a build in type, need to resolve type
      let typeRef = this.resolveType(type, module);
      if (typeRef == null) console.error(new Error(`Could not resolve type ${type} in [${module.name}][${currentPath}].`));


      if (isViewElementString(typeRef)) {
        typeRef = this.resolveStringType(typeRef, extractPattern(), extractRange(0, +18446744073709551615));
      } else if (isViewElementNumber(typeRef)) {
        typeRef = this.resolveNumberType(typeRef, extractRange(typeRef.min, typeRef.max));
      }

      // spoof date type here from special string type
      if ((type === 'date-and-time' || type.endsWith(':date-and-time') ) && typeRef.module === "ietf-yang-types") {
          return {
             ...typeRef,
             ...element,
             description: description,
             uiType: "date", 
          };
      }

      return ({
        ...typeRef,
        ...element,
        description: description,
      }) as ViewElement;
    }
  }

  private resolveStringType(parentElement: ViewElementString, pattern: Expression<RegExp> | undefined, length: { expression: Expression<YangRange> | undefined, min: number, max: number }) {
    return {
      ...parentElement,
      pattern: pattern != null && parentElement.pattern
        ? { operation: "AND", arguments: [pattern, parentElement.pattern] }
        : parentElement.pattern
          ? parentElement.pattern
          : pattern,
      length: length.expression != null && parentElement.length
        ? { operation: "AND", arguments: [length.expression, parentElement.length] }
        : parentElement.length
          ? parentElement.length
          : length ?.expression,
    } as ViewElementString;
  }

  private resolveNumberType(parentElement: ViewElementNumber, range: { expression: Expression<YangRange> | undefined, min: number, max: number }) {
    return {
      ...parentElement,
      range: range.expression != null && parentElement.range
        ? { operation: "AND", arguments: [range.expression, parentElement.range] }
        : parentElement.range
          ? parentElement.range
          : range,
      min: range.min,
      max: range.max,
    } as ViewElementNumber;
  }

  private resolveReferencePath(vPath: string, module: Module) {
    const vPathParser = /(?:(?:([^\/\:]+):)?([^\/]+))/g // 1 = opt: namespace / 2 = property
    return vPath.replace(vPathParser, (_, ns, property) => {
      const nameSpace = ns && module.imports[ns] || module.name;
      return `${nameSpace}:${property}`;
    });
  }

  private resolveReference(vPath: string, currentPath: string) {
    const vPathParser = /(?:(?:([^\/\[\]\:]+):)?([^\/\[\]]+)(\[[^\]]+\])?)/g // 1 = opt: namespace / 2 = property / 3 = opt: indexPath
    let element: ViewElement | null = null;
    let moduleName = "";

    const vPathParts = splitVPath(vPath, vPathParser).map(p => ({ ns: p[1], property: p[2], ind: p[3] }));
    const resultPathParts = !vPath.startsWith("/")
      ? splitVPath(currentPath, vPathParser).map(p => { moduleName = p[1] || moduleName ; return { ns: moduleName, property: p[2], ind: p[3] } })
      : [];

    for (let i = 0; i < vPathParts.length; ++i) {
      const vPathPart = vPathParts[i];
      if (vPathPart.property === "..") {
        resultPathParts.pop();
      } else if (vPathPart.property !== ".") {
        resultPathParts.push(vPathPart);
      }
    }

    // resolve element by path
    for (let j = 0; j < resultPathParts.length; ++j) {
      const pathPart = resultPathParts[j];
      if (j === 0) {
        moduleName = pathPart.ns;
        const rootModule = this._modules[moduleName];
        if (!rootModule) throw new Error("Could not resolve module [" + moduleName + "].\r\n" + vPath);
        element = rootModule.elements[`${pathPart.ns}:${pathPart.property}`];
      } else if (element && isViewElementObjectOrList(element)) {
        const view: ViewSpecification = this._views[+element.viewId];
        if (moduleName !== pathPart.ns) {
          moduleName = pathPart.ns;
        }   
        element = view.elements[pathPart.property] || view.elements[`${moduleName}:${pathPart.property}`];
      } else {
        throw new Error("Could not resolve reference.\r\n" + vPath);
      }
      if (!element) throw new Error("Could not resolve path [" + pathPart.property + "] in [" + currentPath + "] \r\n" + vPath);
    }

    moduleName = ""; // create the vPath for the resolved element, do not add the element itself this will be done later in the res(...) function
    return [element, resultPathParts.slice(0,-1).map(p => `${moduleName !== p.ns ? `${moduleName=p.ns}:` : ""}${p.property}${p.ind || ''}`).join("/")];
  }

  private resolveView(vPath: string) {
    const vPathParser = /(?:(?:([^\/\[\]\:]+):)?([^\/\[\]]+)(\[[^\]]+\])?)/g // 1 = opt: namespace / 2 = property / 3 = opt: indexPath
    let element: ViewElement | null = null;
    let partMatch: RegExpExecArray | null;
    let view: ViewSpecification | null = null;
    let moduleName = "";
    if (vPath) do {
      partMatch = vPathParser.exec(vPath);
      if (partMatch) {
        if (element === null) {
          moduleName = partMatch[1]!;
          const rootModule = this._modules[moduleName];
          if (!rootModule) return null;
          element = rootModule.elements[`${moduleName}:${partMatch[2]!}`];
        } else if (isViewElementObjectOrList(element)) {
          view = this._views[+element.viewId];
          if (moduleName !== partMatch[1]) {
            moduleName = partMatch[1];
            element = view.elements[`${moduleName}:${partMatch[2]}`];
          } else {
            element = view.elements[partMatch[2]];
          }
        } else {
          return null;
        }
        if (!element) return null;
      }
    } while (partMatch)
    return element && isViewElementObjectOrList(element) && this._views[+element.viewId] || null;
  }

  private resolveType(type: string, module: Module) {
    const collonInd = type.indexOf(":");
    const preFix = collonInd > -1 ? type.slice(0, collonInd) : "";
    const typeName = collonInd > -1 ? type.slice(collonInd + 1) : type;

    const res = preFix
      ? this._modules[module.imports[preFix]].typedefs[typeName]
      : module.typedefs[typeName];
    return res;
  }

  private resolveGrouping(grouping: string, module: Module) {
    const collonInd = grouping.indexOf(":");
    const preFix = collonInd > -1 ? grouping.slice(0, collonInd) : "";
    const groupingName = collonInd > -1 ? grouping.slice(collonInd + 1) : grouping;

    return preFix
      ? this._modules[module.imports[preFix]].groupings[groupingName]
      : module.groupings[groupingName];

  }

  private resolveIdentity(identity: string, module: Module) {
    const collonInd = identity.indexOf(":");
    const preFix = collonInd > -1 ? identity.slice(0, collonInd) : "";
    const identityName = collonInd > -1 ? identity.slice(collonInd + 1) : identity;

    return preFix
      ? this._modules[module.imports[preFix]].identities[identityName]
      : module.identities[identityName];

  }
}