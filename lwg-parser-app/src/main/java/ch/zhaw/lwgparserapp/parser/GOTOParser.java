package ch.zhaw.lwgparserapp.parser;

import ch.zhaw.lwgparserapp.error.ErrorHandler;
import ch.zhaw.lwgparserapp.syntax.Statement;
import ch.zhaw.lwgparserapp.syntax.gotosyntax.Goto;
import ch.zhaw.lwgparserapp.syntax.gotosyntax.Halt;
import ch.zhaw.lwgparserapp.syntax.gotosyntax.If;
import ch.zhaw.lwgparserapp.token.Token;
import ch.zhaw.lwgparserapp.token.TokenType;

import java.util.*;

/**
 * GOTO parser
 */
public class GOTOParser extends Parser {
    // list of marker numbers, for checking duplicates
    List<Integer> markerNumberList;
    // maps goto numbers to the line number of the goto statement, for later checking
    Map<Integer, Integer> gotoValuesMap;
    Map<Integer, Integer> markerLineMap;

    boolean containsHalt;

    private static final String EXPECTED_MARKER = "Expected a marker";
    private static final String EXPECTED_COLON = "Expected a colon after the marker";


    public GOTOParser() {
        super();
    }

    public List<Statement> parse(List<Token> tokens) {
        markerNumberList = new ArrayList<>();
        gotoValuesMap = new HashMap<>();
        markerLineMap = new HashMap<>();
        containsHalt = false;
        super.setTokens(tokens);
        return parseGOTO(tokens);
    }

    public Map<Integer, Integer> getMarkerLineMap () {
        return markerLineMap;
    }

    /**
     * Parses a list of tokens into a block of expressions
     *
     * @param tokens the list of tokens to parse
     * @return the block of expressions
     */
    private List<Statement> parseGOTO(List<Token> tokens) {
        List<Statement> statements = new LinkedList<>();

        while (!tokens.isEmpty()) {
            try {
                // every statement starts with a marker
                int lineDifference = tokens.getFirst().line - lastLine - 1;
                for(int i = 0; i < lineDifference; i++) {
                    statements.add(null);
                }
                Token marker = super.consume(true, EXPECTED_MARKER, TokenType.MARKER);
                int markerLine = marker.value; // for GOTO, the marker value is treated as the line number
                int line = marker.line;
                if (markerNumberList.contains(markerLine)) {
                    throw new ParseException("Duplicate line marker found", marker.line);
                }
                markerNumberList.add(marker.value);
                markerLineMap.put(markerLine, line);
                super.consume(EXPECTED_COLON, TokenType.COLON);

                if (tokens.isEmpty()) {
                    throw new ParseException("Expected a statement after the marker", marker.line);
                }
                TokenType type = tokens.getFirst().type;
                switch (type) {
                    case VARIABLE -> statements.add(super.parseAssignment(line));
                    case IF -> statements.add(parseIf(line, markerLine));
                    case GOTO -> statements.add(parseGotoStatement(line, markerLine));
                    case HALT -> statements.add(parseHalt(line, markerLine));
                    default -> throw new ParseException(
                            "Expected VARIABLE, IF, GOTO or HALT but got " + type,
                            super.peek().line);
                }
                super.validateSemicolon(tokens);
            } catch (ParseException e) {
                ErrorHandler.report(e.getLine(), e.getMessage());
                super.skipToNextLine();
            }
        }
        if (!ErrorHandler.hadError()) {
            checkGotoValues();
            if (!containsHalt) {
                ErrorHandler.report(0, "No HALT statement found");
            }
        }
        return statements;
    }

    private Halt parseHalt(int line, int markerLine) throws ParseException {
        super.consume("Expected HALT", TokenType.HALT);

        containsHalt = true;

        return new Halt(markerLine, line);
    }

    /**
     * Parses an if expression
     *
     * @throws ParseException if the tokens are invalid
     */
    private If parseIf(int line, int markerLine) throws ParseException {
        super.consume("Expected an if statement", TokenType.IF);
        Token variable = super.consume("Expected a variable", TokenType.VARIABLE);
        super.consume("Expected an equals operator", TokenType.EQUALS);
        Token constant = super.consume("Expected a constant", TokenType.CONSTANT);
        super.consume("Expected a then operator", TokenType.THEN);
        super.consume("Expected a goto operator", TokenType.GOTO);
        Token gotoMarker = super.consume(EXPECTED_MARKER, TokenType.MARKER);

        gotoValuesMap.put(gotoMarker.value, gotoMarker.line);

        return new If(variable.value, constant.value, gotoMarker.value, markerLine, line);
    }

    /**
     * Parses a goto expression
     *
     * @throws ParseException if the tokens are invalid
     */
    private Goto parseGotoStatement(int line, int markerLine) throws ParseException {
        super.consume("Expected a goto statement", TokenType.GOTO);
        Token gotoMarker = super.consume(EXPECTED_MARKER, TokenType.MARKER);
        gotoValuesMap.put(gotoMarker.value, gotoMarker.line);
        return new Goto(gotoMarker.value, markerLine, line);
    }


    /**
     * Checks if all goto values are valid
     */
    private void checkGotoValues() {
        for (Map.Entry<Integer, Integer> entry : gotoValuesMap.entrySet()) {
            if (!markerNumberList.contains(entry.getKey())) {
                ErrorHandler.report(
                        entry.getValue(),
                        "No line with goto marker value " + entry.getKey() + " found"
                );
            }
        }
    }
}