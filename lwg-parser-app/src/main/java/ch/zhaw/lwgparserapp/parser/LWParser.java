package ch.zhaw.lwgparserapp.parser;

import ch.zhaw.lwgparserapp.error.ErrorHandler;
import ch.zhaw.lwgparserapp.syntax.Statement;
import ch.zhaw.lwgparserapp.syntax.lwsyntax.Loop;
import ch.zhaw.lwgparserapp.syntax.lwsyntax.While;
import ch.zhaw.lwgparserapp.token.Token;
import ch.zhaw.lwgparserapp.token.TokenType;

import java.util.List;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Arrays;

/**
 * LOOP WHILE parser
 */
public class LWParser extends Parser {
    private final Deque<TokenType> balancedDeque;
    private boolean encounteredEnd;

    public LWParser() {
        super();
        balancedDeque = new ArrayDeque<>();
    }

    public List<Statement> parse(List<Token> tokens) {
        super.setTokens(tokens);
        encounteredEnd = false;
        List<Statement> statements = parseLW(tokens);
        if (!ErrorHandler.hadError()) {
            validateClosingSequence(lastLine);
        }
        return statements;
    }

    /**
     * Parses a list of tokens into a block of expressions
     *
     * @param tokens the list of tokens to parse
     * @return the block of expressions
     */
    private List<Statement> parseLW(List<Token> tokens) {
        List<Statement> statements = new ArrayList<>();
        int line;

        while (!tokens.isEmpty()) {
            try {
                TokenType type = tokens.getFirst().type;
                line = tokens.getFirst().line;
                switch (type) {
                    case LOOP -> statements.add(parseLoop());
                    case WHILE -> statements.add(parseWhile());
                    case VARIABLE -> {
                        statements.add(parseAssignment(line));
                        validateSemicolon(tokens);
                    }
                    case END -> {
                        parseEnd();
                        validateSemicolon(tokens);
                        encounteredEnd = true;
                    }
                    default -> throw new ParseException("Expected VARIABLE, LOOP, WHILE or END but got " + type, line);
                }
                if (encounteredEnd) {
                    encounteredEnd = false;
                    break;
                }
            } catch (ParseException e) {
                ErrorHandler.report(e.getLine(), e.getMessage());
                super.skipToNextLine();
            }
        }
        return statements;
    }

    /**
     * Parses a loop statement
     *
     * @return the loop statement
     * @throws ParseException if the loop statement is invalid
     */
    private Loop parseLoop() throws ParseException {
        pushOnDeque(TokenType.LOOP);
        super.consume("Expected a Loop.", TokenType.LOOP);
        Token condition = super.consume("Expected a variable or constant",
                TokenType.VARIABLE, TokenType.CONSTANT);
        super.consume("Expected a Do", TokenType.DO);

        return new Loop(
                condition.type == TokenType.CONSTANT,
                condition.value,
                parseLW(super.getTokens()),
                condition.line);
    }

    /**
     * Parses a while statement
     *
     * @return the while statement
     * @throws ParseException if the while statement is invalid
     */
    private While parseWhile() throws ParseException {
        pushOnDeque(TokenType.WHILE);
        super.consume("Expected a While", TokenType.WHILE);
        Token condition = super.consume("Expected a variable",
                TokenType.VARIABLE);
        super.consume("Expected a greater than sign",
                TokenType.GREATER_THAN);
        Token constant = super.consume("Expected a constant",
                TokenType.CONSTANT);
        if (constant.value != 0) {
            ErrorHandler.report(constant.line, "Only while x > 0 is allowed.");
        }
        super.consume("Expected a Do", TokenType.DO);

        return new While(condition.value, constant.value,
                parseLW(super.getTokens()), condition.line);
    }

    /**
     * Validates if the closing sequence is balanced
     * and if the statement sequence is not empty
     * it consumes an END token.
     *
     * @throws ParseException if the closing sequence is not balanced
     */
    private void parseEnd() throws ParseException {
        if (!isBalancedStatementSequence(TokenType.WHILE, TokenType.LOOP)) {
            ErrorHandler.report(super.peek().line, "Unexpected END token");
        }
        super.consume("Expected an END", TokenType.END);
        encounteredEnd = true;
    }

    /**
     * Validates the closing sequence of a block of expressions
     * by checking if the expected type is the same as the last token in the deque
     * It adds an error if the queue is not empty at the end of the parsing process.
     *
     * @param line the line number of the last token
     */
    private void validateClosingSequence(int line) {
        if (isBalancedStatementSequence(TokenType.WHILE, TokenType.LOOP)) {
            ErrorHandler.report(line, "You need to close the loop or while statement.");
        }
    }

    /**
     * Push a token type onto the deque
     * to keep track of the opening and closing of blocks of expressions
     * and statements.
     * The deque is used to validate the closing sequence of a block of expressions
     *
     * @param type the token type to push onto the deque
     */

    private void pushOnDeque(TokenType type) {
        balancedDeque.push(type);
    }

    /**
     * Validates the closing sequence of a block of expressions
     * by checking if the expected type is the same as the last token in the deque
     * and then popping the last token from the deque.
     * If the queue is not empty at the end of the parsing process there is an error in the parsed
     * source code.
     *
     * @param expectedType the expected type of the closing token
     * @return true if the closing token is valid, false otherwise
     */
    private boolean isBalancedStatementSequence(TokenType... expectedType) {
        return Arrays.stream(expectedType)
                .filter(type -> !balancedDeque.isEmpty() && balancedDeque.peek() == type)
                .findFirst()
                .map(type -> {
                    balancedDeque.pop();
                    return true;
                })
                .orElse(false);
    }

}