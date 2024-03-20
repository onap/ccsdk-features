<<<<<<< HEAD   (907af9 fix oauth code)
=======
enum WhenTokenType {
  AND = 'AND',
  OR = 'OR',
  NOT = 'NOT',
  EQUALS = 'EQUALS',
  NOT_EQUALS = 'NOT_EQUALS',
  COMMA = 'COMMA',
  STRING = 'STRING',
  FUNCTION = 'FUNCTION',
  IDENTIFIER = 'IDENTIFIER',
  OPEN_PAREN = 'OPEN_PAREN',
  CLOSE_PAREN = 'CLOSE_PAREN',
  EXPRESSION = 'EXPRESSION',
}

type Token = {
  type: WhenTokenType;
  value: string;
};

const isAlpha = (char: string) => {
  if (!char) return false;
  const code = char.charCodeAt(0);
  return (code >= 65 && code <= 90) || (code >= 97 && code <= 122);
};

const isAlphaNumeric = (char: string) => {
  if (!char) return false;
  const code = char.charCodeAt(0);
  return (
    isAlpha(char) ||
    (code >= 48 && code <= 57) ||
    code === 95 || // underscore
    code === 45 || // hyphen
    code === 47 || // slash
    code === 58 || // colon
    code === 46 // dot
  );
};

const isOperator = (char: string) => {
  if (!char) return false;
  const code = char.charCodeAt(0);
  return code === 33 || code === 38 || code === 124 || code === 61;
};

const lex = (input: string) : Token[] => {
  let tokens = [] as any[];
  let current = 0;

  while (current < input.length) {
    let char = input[current];

    if (char === ' ') {
      current++;
      continue;
    }

    if (char === '(') {
      tokens.push({ type: WhenTokenType.OPEN_PAREN, value: char });
      current++;
      continue;
    }

    if (char === ')') {
      tokens.push({ type: WhenTokenType.CLOSE_PAREN, value: char });
      current++;
      continue;
    }

    if (char === '=') {
      tokens.push({ type: WhenTokenType.EQUALS, value: char });
      current++;
      continue;
    }

    if (char === ',') {
      tokens.push({ type: WhenTokenType.COMMA, value: char });
      current++;
      continue;
    }

    if (char === '\"' || char === '\'') {
      let value = '';
      let start = current;
      current++;

      while (current < input.length) {
        let innerChar = input[current];
        if (innerChar === '\\') {
          value += input[current] + input[current + 1];
          current += 2;
        } else if (innerChar === input[start]) {
          current++;
          break;
        } else {
          value += innerChar;
          current++;
        }
      }

      tokens.push({ type: WhenTokenType.STRING, value });
      continue;
    }

    if (isAlpha(char)) {
      let value = '';
      while (isAlpha(char)) {
        value += char;
        char = input[++current];
      }

      switch (value) {
        case 'and':
          tokens.push({ type: WhenTokenType.AND });
          break;
        case 'or':
          tokens.push({ type: WhenTokenType.OR });
          break;
        case 'not':
          tokens.push({ type: WhenTokenType.NOT });
          break;
        case 'eq':
          tokens.push({ type: WhenTokenType.EQUALS });
          break;
        default:
          while (isAlphaNumeric(char)) {
            value += char;
            char = input[++current];
          }
          tokens.push({ type: WhenTokenType.IDENTIFIER, value });
      }

      continue;
    }
    
    if (isAlphaNumeric(char)) {
      let value = '';
      while (isAlphaNumeric(char)) {
        value += char;
        char = input[++current];
      }

      tokens.push({ type: WhenTokenType.IDENTIFIER, value });
      continue;
    }

    if (isOperator(char)) {
      let value = '';
      while (isOperator(char)) {
        value += char;
        char = input[++current];
      }

      switch (value) {
        case '&&':
          tokens.push({ type: WhenTokenType.AND });
          break;
        case '||':
          tokens.push({ type: WhenTokenType.OR });
          break;
        case '!':
          tokens.push({ type: WhenTokenType.NOT });
          break;
        case '==':
          tokens.push({ type: WhenTokenType.EQUALS });
          break;
        case '!=':
          tokens.push({ type: WhenTokenType.NOT_EQUALS });
          break;  
        default:
          throw new TypeError(`I don't know what this operator is: ${value}`);
      }
      continue;
    }
    
    throw new TypeError(`I don't know what this character is: ${char}`);
  }
  return tokens;
};

type WhenAST = {
  type: WhenTokenType;
  left?: WhenAST;
  right?: WhenAST;
  value?: string | WhenAST;
  name?: string;
  args?: WhenAST[];
};

const precedence : { [index: string] : number } = {
  'EQUALS': 4,
  'NOT': 3,
  'AND': 2,
  'OR': 1,
};

const parseWhen = (whenExpression: string) => {
  const tokens = lex(whenExpression);
  let current = 0;

  const walk = (precedenceLevel = 0) : WhenAST => {
    let token = tokens[current];
    let node: WhenAST | null = null;

    if (token.type === WhenTokenType.OPEN_PAREN) {
      token = tokens[++current];
      let innerNode: WhenAST = { type: WhenTokenType.EXPRESSION, value: walk() };
      token = tokens[current];

      while (token.type !== WhenTokenType.CLOSE_PAREN) {
        innerNode = {
          type: token.type,
          value: token.value,
          left: innerNode,
          right: walk(),
        };
        token = tokens[current];
      }
      current++;
      return innerNode;
    }

    if (token.type === WhenTokenType.STRING ) {
      current++;
      node = { type: token.type, value: token.value };
    }

    if (token.type === WhenTokenType.NOT) {
      token = tokens[++current];
      node = { type: WhenTokenType.NOT, value: token.value, right: walk() };
    }

    if (token.type === WhenTokenType.IDENTIFIER) {
      const nextToken = tokens[current + 1];
      if (nextToken.type === WhenTokenType.OPEN_PAREN) {
        let name = token.value;
        token = tokens[++current];

        let args = [];
        token = tokens[++current];

        while (token.type !== WhenTokenType.CLOSE_PAREN) {
          if (token.type === WhenTokenType.COMMA) {
            current++;
          } else {
            args.push(walk());
          }
          token = tokens[current];
        }

        current++;
        node = { type: WhenTokenType.FUNCTION, name, args };
      } else {
        current++;
        node = { type: WhenTokenType.IDENTIFIER, value: token.value };
      }
    }   

    if (!node) throw new TypeError('Unexpected token: ' + token.type);

    token = tokens[current];
    while (current < tokens.length && precedence[token.type] >= precedenceLevel) {
      console.log(current, tokens[current], tokens[current].type, precedenceLevel, precedence[token.type]);
      token = tokens[current];
      if (token.type === WhenTokenType.EQUALS || token.type === WhenTokenType.AND || token.type === WhenTokenType.OR) {
        current++;
        node = {
          type: token.type,
          left: node,
          right: walk(precedence[token.type]),
        };
      } else {
        break;
      }
    }

    return node;
   
  };

  return walk();
};

export {
  parseWhen,
  WhenAST,
  WhenTokenType,
};
>>>>>>> CHANGE (5418ff ODLUX Update)
