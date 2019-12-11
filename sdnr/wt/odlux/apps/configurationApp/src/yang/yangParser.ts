
import { Token, Statement, Module } from "../models/yang";
import { ViewSpecification, ViewElement, isViewElementObjectOrList, ViewElementBase, isViewElementReference } from "../models/uiModels";
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
    while (endpos < this.buf.length &&
      this._isAlphanum(this.buf.charAt(endpos))) {
      endpos++;
    }

    const tok = {
      name: 'IDENTIFIER',
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
      } else if (this._isAlpha(char)) {
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
  private _groupingsToResolve: (() => void)[] = [];

  private _modules: { [name: string]: Module } = {};
  private _views: ViewSpecification[] = [{
    id: "0",
    name: "root",
    language: "en-US",
    canEdit: false,
    parentView: "0",
    title: "root",
    elements: {},
   }];

  constructor() {

  }

  public get modules() {
    return this._modules;
  }

  public get views() {
    return this._views;
  }

  public async addCapability(capability: string, version?: string) {
    // do not add twice
    if (this._modules[capability]) {
      return;
    }

    const data = await yangService.getCapability(capability, version);
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
      elements: {}
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
      ...revisions.reduce<{ [version: string]: { }}>((acc, version) => {
        if (!version.arg) {
          throw new Error(`Module [${module.name}] has a version w/o version number.`);
        }
        const description = this.extractValue(version, "description");
        const reference = this.extractValue(version,"reference");
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
      ...imports.reduce < { [key: string]: string }>((acc, imp) => {
        const prefix = imp.sub && imp.sub.filter(s => s.key === "prefix");
        if (!imp.arg) {
           throw new Error(`Module [${module.name}] has an import with neither name nor prefix.`);
        }
        acc[prefix && prefix.length === 1 && prefix[0].arg || imp.arg] = imp.arg;
        return acc;
      }, {})
    };

    // import all required files
    if (imports) for (let ind = 0; ind < imports.length; ++ind) {
      await this.addCapability(imports[ind].arg!);
    }

    this.extractTypeDefinitions(rootStatement, module, "");

    const groupings = this.extractGroupings(rootStatement, 0, module, "");
    this._views.push(...groupings);

    const augments = this.extractAugments(rootStatement, 0, module, "");
    this._views.push(...augments);

    // the default for config on module level is config = true;
    const [currentView, subViews] = this.extractSubViews(rootStatement, 0, module, "");
    this._views.push(currentView, ...subViews);

    // create the root elements for this module
    module.elements = currentView.elements;
    Object.keys(module.elements).forEach(key => {
      const viewElement = module.elements[key];
      if (!isViewElementObjectOrList(viewElement)) {
        throw new Error(`Module: [${module}]. Only List or Object allowed on root level.`);
      }
      const viewIdIndex = Number(viewElement.viewId);
      module.views[key] = this._views[viewIdIndex];
      this._views[0].elements[key] = module.elements[key];
    });
    return module;
  }

  public postProcess() {
    // process all groupings
    // execute all post processes like resolving in propper order
    this._groupingsToResolve.forEach(cb => {
      try { cb(); } catch (error) {
        console.warn(`Error resolving: [${error.message}]`);
      }
    });

    // process all augmentations
    Object.keys(this.modules).forEach(modKey => {
      const module = this.modules[modKey];
      Object.keys(module.augments).forEach(augKey => {
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
      if (! def.arg) {
        throw new Error(`Module: [${module.name}]. Found typefed without name.`);
      }
      module.typedefs[def.arg] = this.getViewElement(def, module, 0, currentPath, false);
    });
  }

  /** Handles Goupings like named Container */
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
        const [currentView, subViews] = this.extractSubViews(cur, parentId, module, currentPath);
        grouping && (module.groupings[grouping] = currentView);
        acc.push(currentView, ...subViews);
        return acc;
      }, []));
    }

    return subViews;
  }

  /** Handles Augmants also like named Container */
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

  private extractSubViews(statement: Statement, parentId: number, module: Module, currentPath: string): [ViewSpecification, ViewSpecification[]] {
    const subViews: ViewSpecification[] = [];
    const currentId = this.nextId;
    let elements: ViewElement[] = [];

    const configValue = this.extractValue(statement, "config");
    const config = configValue == null ? true : configValue.toLocaleLowerCase() !== "false";

    // extract conditions
    const ifFeature = this.extractValue(statement, "if-feature");
    const whenCondition = this.extractValue(statement, "when");

    // extract all container
    const container = this.extractNodes(statement, "container");
    if (container && container.length > 0) {
      subViews.push(...container.reduce<ViewSpecification[]>((acc, cur) => {
        if (!cur.arg) {
          throw new Error(`Module: [${module.name}]. Found container without name.`);
        }
        const [currentView, subViews] = this.extractSubViews(cur, currentId, module, `${currentPath}/${module.name}:${cur.arg}`);
        elements.push({
          id: parentId === 0 ? `${module.name}:${cur.arg}` : cur.arg,
          label: cur.arg,
          uiType: "object",
          viewId: currentView.id,
          config: config
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
        if (!cur.arg) {
          throw new Error(`Module: [${module.name}]. Found list without name.`);
        }
        const key = this.extractValue(cur, "key") || undefined;
        if (config && !key) {
          throw new Error(`Module: [${module.name}]. Found configurable list without key.`);
        }
        const [currentView, subViews] = this.extractSubViews(cur, currentId, module, `${currentPath}/${module.name}:${cur.arg}`);
        elements.push({
          id: parentId === 0 ? `${module.name}:${cur.arg}` : cur.arg,
          label: cur.arg,
          isList: true,
          uiType: "object",
          viewId: currentView.id,
          key: key,
          config: config
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
        const element = this.getViewElement(cur, module, parentId, currentPath, true);
        element && acc.push(element);
        return acc;
      }, []));
    }

    // process all leafs
    // a leaf is mainly a property of an object
    const leafs = this.extractNodes(statement, "leaf");
    if (leafs && leafs.length > 0) {
      elements.push(...leafs.reduce<ViewElement[]>((acc, cur) => {
        const element = this.getViewElement(cur, module, parentId, currentPath, false);
        element && acc.push(element);
        return acc;
      }, []));
    }


    const choiceStms = this.extractNodes(statement, "choice");
    if (choiceStms && choiceStms.length > 0) {
      for (let i = 0; i < choiceStms.length; ++i) {
        const cases = this.extractNodes(choiceStms[i], "case");
        console.warn(`Choice found ${choiceStms[i].arg}::${cases.map(c => c.arg).join(";")}`, choiceStms[i]);
      }
    }

    const rpcs = this.extractNodes(statement, "rpc");
    if (rpcs && rpcs.length > 0) {
      // todo:
    }

    if (!statement.arg) {
      throw new Error(`Module: [${module.name}]. Found statement without name.`);
    }

    const viewSpec: ViewSpecification = {
      id: String(currentId),
      parentView: String(parentId),
      name: statement.arg,
      title: statement.arg,
      language: "en-us",
      canEdit: false,
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
      for (let i = 0; i < usesRefs.length; ++i) {
        const groupingName = usesRefs[i].arg;
        if (!groupingName) {
          throw new Error(`Module: [${module.name}]. Found an uses statement without a grouping name.`);
        }

        viewSpec.uses.push(this.resolveReferencePath(groupingName, module));

        this._groupingsToResolve.push(() => {
          const groupingViewSpec = this.resolveGrouping(groupingName, module);
          if (groupingViewSpec) {
            Object.keys(groupingViewSpec.elements).forEach(key => {
              const elm = groupingViewSpec.elements[key];
              viewSpec.elements[key] = {
                ...groupingViewSpec.elements[key],
                when: elm.when ? `(${groupingViewSpec.when}) and (${elm.when})` : groupingViewSpec.when,
                ifFeature: elm.ifFeature ? `(${groupingViewSpec.ifFeature}) and (${elm.ifFeature})` : groupingViewSpec.ifFeature,
              };
            });
          }
        });
      }
    }

    return [viewSpec, subViews];
  }

  /** Extracts the UI View from the type in the cur statement. */
  private getViewElement(cur: Statement, module: Module, parentId: number, currentPath: string, isList: boolean): ViewElement {

    const type = this.extractValue(cur, "type");
    const defaultVal = this.extractValue(cur, "default") || undefined;
    const description = this.extractValue(cur, "description") || undefined;
    const rangeMatch = this.extractValue(cur, "range", /^(\d+)\.\.(\d+)/) || undefined;

    const configValue = this.extractValue(cur, "config");
    const config = configValue == null ? true : configValue.toLocaleLowerCase() !== "false";

    const mandatory = this.extractValue(cur, "mandatory") === "true" || false;

    if (!cur.arg) {
      throw new Error(`Module: [${module.name}]. Found element without name.`);
    }

    if (!type) {
      throw new Error(`Module: [${module.name}].[${cur.arg}]. Found element without type.`);
    }

    const element: ViewElementBase = {
      id: parentId === 0 ? `${module.name}:${cur.arg}`: cur.arg,
      label: cur.arg,
      config: config,
      mandatory: mandatory,
      isList: isList,
      default: defaultVal,
      description: description
    };

    if (type === "string") {
      return ({
        ...element,
        uiType: "string",
        pattern: this.extractNodes(this.extractNodes(cur, "type")[0]!, "pattern").map(p => p.arg!).filter(p => !!p),
      });
    } else if (type === "boolean") {
      return ({
        ...element,
        uiType: "boolean"
      });
    } else if (type === "uint8") {
      return ({
        ...element,
        uiType: "number",
        min: rangeMatch ? Number(rangeMatch[0]) : 0,
        max: rangeMatch ? Number(rangeMatch[1]) : +255,
        units: this.extractValue(cur, "units") || undefined,
        format: this.extractValue(cur, "format") || undefined,
      });
    } else if (type === "uint16") {
      return ({
        ...element,
        uiType: "number",
        min: rangeMatch ? Number(rangeMatch[0]) : 0,
        max: rangeMatch ? Number(rangeMatch[1]) : +65535,
        units: this.extractValue(cur, "units") || undefined,
        format: this.extractValue(cur, "format") || undefined,
      });
    } else if (type === "uint32") {
      return ({
        ...element,
        uiType: "number",
        min: rangeMatch ? Number(rangeMatch[0]) : 0,
        max: rangeMatch ? Number(rangeMatch[1]) : +4294967295,
        units: this.extractValue(cur, "units") || undefined,
        format: this.extractValue(cur, "format") || undefined,
      });
    } else if (type === "uint64") {
      return ({
        ...element,
        uiType: "number",
        min: rangeMatch ? Number(rangeMatch[0]) : 0,
        max: rangeMatch ? Number(rangeMatch[1]) : +18446744073709551615,
        units: this.extractValue(cur, "units") || undefined,
        format: this.extractValue(cur, "format") || undefined,
      });
    } else if (type === "int8") {
      return ({
        ...element,
        uiType: "number",
        min: rangeMatch ? Number(rangeMatch[0]) : -128,
        max: rangeMatch ? Number(rangeMatch[1]) : +127,
        units: this.extractValue(cur, "units") || undefined,
        format: this.extractValue(cur, "format") || undefined,
      });
    } else if (type === "int16") {
      return ({
        ...element,
        uiType: "number",
        min: rangeMatch ? Number(rangeMatch[0]) : -32768,
        max: rangeMatch ? Number(rangeMatch[1]) : +32767,
        units: this.extractValue(cur, "units") || undefined,
        format: this.extractValue(cur, "format") || undefined,
      });
    } else if (type === "int32") {
      return ({
        ...element,
        uiType: "number",
        min: rangeMatch ? Number(rangeMatch[0]) : -2147483648,
        max: rangeMatch ? Number(rangeMatch[1]) : +2147483647,
        units: this.extractValue(cur, "units") || undefined,
        format: this.extractValue(cur, "format") || undefined,
      });
    } else if (type === "int64") {
      return ({
        ...element,
        uiType: "number",
        min: rangeMatch ? Number(rangeMatch[0]) : 0,
        max: rangeMatch ? Number(rangeMatch[1]) : +18446744073709551615,
        units: this.extractValue(cur, "units") || undefined,
        format: this.extractValue(cur, "format") || undefined,
      });
    } else if (type === "decimal16") {
      return ({
        ...element,
        uiType: "number",
        min: rangeMatch ? Number(rangeMatch[0]) : 0,
        max: rangeMatch ? Number(rangeMatch[1]) : +18446744073709551615,
        units: this.extractValue(cur, "units") || undefined,
        format: this.extractValue(cur, "format") || undefined,
        fDigits: Number(this.extractValue(this.extractNodes(cur, "type")[0]!, "fraction-digits")) || -1
      });
    } else if (type === "decimal32") {
      return ({
        ...element,
        uiType: "number",
        min: rangeMatch ? Number(rangeMatch[0]) : 0,
        max: rangeMatch ? Number(rangeMatch[1]) : +18446744073709551615,
        units: this.extractValue(cur, "units") || undefined,
        format: this.extractValue(cur, "format") || undefined,
        fDigits: Number(this.extractValue(this.extractNodes(cur, "type")[0]!, "fraction-digits")) || -1
      });
    } else if (type === "decimal64") {
      return ({
        ...element,
        uiType: "number",
        min: rangeMatch ? Number(rangeMatch[0]) : 0,
        max: rangeMatch ? Number(rangeMatch[1]) : +18446744073709551615,
        units: this.extractValue(cur, "units") || undefined,
        format: this.extractValue(cur, "format") || undefined,
        fDigits: Number(this.extractValue(this.extractNodes(cur, "type")[0]!, "fraction-digits")) || -1
      });
    } else if (type === "enumeration") {
      //  todo: ❗ identity beachten (erweiterbare enums) ⚡
      const typeNode = this.extractNodes(cur, "type")[0]!;
      const enumNodes = this.extractNodes(typeNode, "enum");
      return ({
        ...element,
        uiType: "selection",
        options: enumNodes.reduce<{ key: string; value: string; description?: string }[]>((acc, e) => {
          if (!e.arg) {
            throw new Error(`Module: [${module.name}].[${cur.arg}]. Found option without name.`);
          }
          const ifClause = this.extractValue(cur, "if-feature");
          const enumOption = {
            key: e.arg,
            value: e.arg,
            description: this.extractValue(cur, "description") || undefined
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
        throw new Error(`Module: [${module.name}].[${cur.arg}]. Found leafref without path.`);
      }
      const refPath = this.resolveReferencePath(vPath, module);
      const resolve = this.resolveReference.bind(this);
      const res : ViewElement = {
        ...element,
        uiType: "reference",
        referencePath: refPath,
        ref(this: ViewElement, currentPath: string) {
          const resolved = resolve(refPath, currentPath);
          return resolved && {
            ...resolved,
            id: this.id,
            label: this.label,
            config: this.config,
            mandatory: this.mandatory,
            isList: this.isList,
            default: this.default,
            description: this.description,
          } as ViewElement;
        }
      };
      return res;
    } else if (type === "identityref") {
      // todo: ❗ handle identityref ⚡
      console.warn(`found type: identityref in [${module.name}][${currentPath}][${element.label}]`);
      return {
        ...element,
        uiType: "string",
      };
    } else if (type === "empty") {
      // todo: ❗ handle empty ⚡
      /*  9.11.  The empty Built-In Type
          The empty built-in type represents a leaf that does not have any
          value, it conveys information by its presence or absence. */
      console.warn(`found type: empty in [${module.name}][${currentPath}][${element.label}]`);
      return {
        ...element,
        uiType: "string",
      };
    } else if (type === "union") {
      // todo: ❗ handle union ⚡
      /* 9.12.  The union Built-In Type

        The union built-in type represents a value that corresponds to one of
        its member types.

        When the type is "union", the "type" statement (Section 7.4) MUST be
        present.  It is used to repeatedly specify each member type of the
        union.  It takes as an argument a string that is the name of a member
        type.

        A member type can be of any built-in or derived type, except it MUST
        NOT be one of the built-in types "empty" or "leafref".

        When a string representing a union data type is validated, the string
        is validated against each member type, in the order they are
        specified in the "type" statement, until a match is found.

        Any default value or "units" property defined in the member types is
        not inherited by the union type. */
      console.warn(`found type: union in [${module.name}][${currentPath}][${element.label}]`);
      return {
        ...element,
        uiType: "string",
      };
    } else if (type === "bits") {
      // todo: ❗ handle bits ⚡
      console.warn(`found type: bits in [${module.name}][${currentPath}][${element.label}]`);
      return {
        ...element,
        uiType: "string",
      };
    } else if (type === "binary") {
      // todo: ❗ handle binary ⚡
      console.warn(`found type: binary in [${module.name}][${currentPath}][${element.label}]`);
      return {
        ...element,
        uiType: "string",
      };
    } else {
      // not a build in type, have to resolve type
      const typeRef = this.resolveType(type, module);
      if (typeRef == null) console.error(new Error(`Could not resolve type ${type} in [${module.name}][${currentPath}].`));
      return ({
        ...typeRef,
        ...element,
        description: description
      }) as ViewElement;
    }
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
    let element : ViewElement | null = null;
    let moduleName = "";

    const vPathParts = splitVPath(vPath, vPathParser).map(p => ({ ns: p[1], property: p[2], ind: p[3] }));
    const resultPathParts = !vPath.startsWith("/")
      ? splitVPath(currentPath, vPathParser).map(p => ({ ns: p[1], property: p[2], ind: p[3] }))
      : [];

    for (let i = 0; i < vPathParts.length; ++i){
      const vPathPart = vPathParts[i];
      if (vPathPart.property === "..") {
        resultPathParts.pop();
      } else if (vPathPart.property !== ".") {
        resultPathParts.push(vPathPart);
      }
    }

    // resolve element by path
    for (let j = 0; j < resultPathParts.length;++j){
      const pathPart = resultPathParts[j];
        if (j===0) {
          moduleName = pathPart.ns;
          const rootModule = this._modules[moduleName];
          if (!rootModule) throw new Error("Could not resolve module [" + moduleName +"].\r\n" + vPath);
          element = rootModule.elements[`${pathPart.ns}:${pathPart.property}`];
        } else if (element && isViewElementObjectOrList(element)) {
          const view: ViewSpecification = this._views[+element.viewId];
          if (moduleName !== pathPart.ns) {
            moduleName = pathPart.ns;
            element = view.elements[`${moduleName}:${pathPart.property}`];
          } else {
            element = view.elements[pathPart.property] || view.elements[`${moduleName}:${pathPart.property}`];
          }
        } else {
          throw new Error("Could not resolve reference.\r\n" + vPath);
        }
        if (!element) throw new Error("Could not resolve path [" + pathPart.property + "] in ["+ currentPath +"] \r\n" + vPath);
      }

    return element;
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

    const res = preFix
      ? this._modules[module.imports[preFix]].groupings[groupingName]
      : module.groupings[groupingName];

    return res;
  }

}