package ch.zhaw.lwgparserapp.parser;

import ch.zhaw.lwgparserapp.syntax.Statement;
import ch.zhaw.lwgparserapp.syntax.generalsyntax.Assignment;
import ch.zhaw.lwgparserapp.syntax.generalsyntax.Operator;
import ch.zhaw.lwgparserapp.token.Token;
import ch.zhaw.lwgparserapp.token.TokenType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Parser {
    private List<Token> tokens;
    protected int lastLine = 1;

    Parser() {
        this.tokens = new ArrayList<>();
    }

    void setTokens(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Token> getTokens() {
        return tokens;
    }

    /**
     * Parses a list of tokens into a block of expressions
     *
     * @param tokens the list of tokens to parse
     * @return the block of expressions
     */
    public abstract List<Statement> parse(List<Token> tokens);

    /**
     * Parses an assignment statement
     *
     * @param line the line number of the assignment statement
     * @return the assignment statement
     * @throws ParseException if the assignment statement is invalid
     */
    Assignment parseAssignment(int line) throws ParseException {
        Token assigneeVariable = consume("Expected a variable", TokenType.VARIABLE);
        consume("Expected an equals operator", TokenType.EQUALS);
        Token assignVariable = consume("Expected a variable", TokenType.VARIABLE);
        Token operator = consume("Expected an operator", TokenType.PLUS, TokenType.MINUS);
        Token constant = consume("Expected a constant", TokenType.CONSTANT);

        Operator op = switch (operator.type) {
            case PLUS -> Operator.ADDITION;
            case MINUS -> Operator.SUBTRACTION;
            default -> throw new ParseException("Invalid operator", operator.line);
        };

        return new Assignment(assigneeVariable.value, assignVariable.value, op,
                constant.value, line);
    }

    /**
     * Consumes a token from the list of tokens
     * and checks if the token type is the same as the expected type.
     * If the token type is not the same as the expected type
     * a parse exception is thrown. <br>
     * If isFirst is true, the current line number is used for the error message,
     * otherwise the line number of the previous token is used.
     *
     * @param isFirst if the token is the first token of the line
     * @param message the message to throw in the parse exception
     * @param types   the expected token types
     * @return the consumed token
     * @throws ParseException if the token type is not the same as the expected type
     */
    Token consume(boolean isFirst, String message, TokenType... types) throws ParseException {
        if (isAtEnd()) {
            throw new ParseException("Unexpected end of code", lastLine);
        }
        if (!check(types)) {
            int line;
            if(isFirst) {
                line = peek().line;
            } else {
                line = lastLine;
                lastLine = peek().line;
            }
            throw new ParseException(message + " but got " + peek().type.toString(), line);
        }

        Token token = tokens.removeFirst();
        lastLine = token.line;
        return token;
    }

    /**
     * Consumes a token from the list of tokens
     * and checks if the token type is the same as the expected type.
     * If the token type is not the same as the expected type
     * a parse exception is thrown. <br>
     * The line number of the previous token is used for the error message.
     *
     * @param message the message to throw in the parse exception
     * @param types   the expected token types
     * @return the consumed token
     * @throws ParseException if the token type is not the same as the expected type
     */
    Token consume(String message, TokenType... types) throws ParseException {
        return consume(false, message, types);
    }

    /**
     * Peeks at the first token in the list.
     * It is always the first token in the list, due to the way the tokens are consumed.
     * The token consume method removes the current token from the list, so the next token
     * is always the first token in the list.
     *
     * @return the first token in the list
     */
    Token peek() {
        return tokens.getFirst();
    }

    private boolean check(TokenType... type) {
        return Arrays.stream(type).anyMatch(t -> t == peek().type);
    }

    private boolean isAtEnd() {
        return tokens.isEmpty();
    }

    /**
     * Represents a parse exception
     */
    static class ParseException extends Exception {
        private final int line;

        public ParseException(String message, int line) {
            super(message);
            this.line = line;
        }

        public int getLine() {
            return line;
        }
    }

    /**
     * Validates if the line is correctly ended with a semicolon if the next token is not an END token.
     *
     * @throws ParseException if the closing sequence is not balanced
     */
    void validateSemicolon(List<Token> tokens) throws ParseException {
        // Semicolon before END is not allowed
        if(tokens.size() >= 2 && tokens.get(0).type == TokenType.SEMICOLON && tokens.get(1).type == TokenType.END) {
            throw new ParseException("Semicolon before END is not allowed", tokens.getFirst().line);
        }

        if (!tokens.isEmpty() && tokens.getFirst().type != TokenType.END) {
            consume("Expected a semicolon.", TokenType.SEMICOLON);
        }
    }

    /**
     * Skip to the next line in the list of tokens
     */
    void skipToNextLine() {
        if (tokens.isEmpty()) return;
        int currentLine = tokens.getFirst().line;
        while (!tokens.isEmpty() && tokens.getFirst().line == currentLine) {
            lastLine = tokens.removeFirst().line;
        }
    }
}
