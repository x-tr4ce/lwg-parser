package ch.zhaw.lwgparserapp.token;

public enum TokenType {
    // static token types
    LOOP,
    WHILE,
    DO,
    END,
    EQUALS,
    PLUS,
    MINUS,
    GREATER_THAN,
    IF,
    THEN,
    GOTO,
    HALT,
    SEMICOLON,
    COLON,
    // dynamic token types
    VARIABLE,
    CONSTANT,
    MARKER
}