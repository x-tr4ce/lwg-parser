package ch.zhaw.lwgparserapp.token;

/**

 Represents a token in the source code.*/
public class Token {
    public final TokenType type;
    public final int value;
    public final int line;

    public Token(TokenType type, int line) {
        this.type = type;
        this.value = -1;
        this.line = line;
    }

    public Token(TokenType type, int line, int value) {
        this.type = type;
        this.value = value;
        this.line = line;
    }
}

