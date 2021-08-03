export type ApplicationConfig = { 
    authentication: "basic"|"oauth",  // basic 
    enablePolicy: false,               // false 
    transportpceUrl? : string
};