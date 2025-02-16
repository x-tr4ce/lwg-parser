package ch.zhaw.lwgparserapp.parser;

import ch.zhaw.lwgparserapp.error.ErrorHandler;
import ch.zhaw.lwgparserapp.syntax.Statement;
import ch.zhaw.lwgparserapp.syntax.generalsyntax.Assignment;
import ch.zhaw.lwgparserapp.syntax.generalsyntax.Operator;
import ch.zhaw.lwgparserapp.syntax.gotosyntax.Goto;
import ch.zhaw.lwgparserapp.syntax.gotosyntax.Halt;
import ch.zhaw.lwgparserapp.syntax.gotosyntax.If;
import ch.zhaw.lwgparserapp.token.Token;
import ch.zhaw.lwgparserapp.token.TokenType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Test the GOTOParser class
 */
class GOTOParserTest {
    GOTOParser parser;
    List<Token> tokens;

    @BeforeEach
    void setUp() {
        parser = new GOTOParser();
        tokens = new ArrayList<>();
        ErrorHandler.clearErrors();
    }

    /**
     * positive test for the GOTO parser with all valid statements in the GOTO language
     * <p>
     * M1: x1 = x2 + 5; <br>
     * M2: If x1 = 10 Then Goto M6; <br>
     * M3: x2 = x1 - 3; <br>
     * M4: Goto M1; <br>
     * M6: Halt
     */
    @Test
    void testPositive() {
        tokens = new ArrayList<>(List.of(
                new Token(TokenType.MARKER, 1, 1),
                new Token(TokenType.COLON, 1),
                new Token(TokenType.VARIABLE, 1, 1),
                new Token(TokenType.EQUALS, 1),
                new Token(TokenType.VARIABLE, 1, 2),
                new Token(TokenType.PLUS, 1),
                new Token(TokenType.CONSTANT, 1, 5),
                new Token(TokenType.SEMICOLON, 1),

                new Token(TokenType.MARKER, 2, 2),
                new Token(TokenType.COLON, 2),
                new Token(TokenType.IF, 2),
                new Token(TokenType.VARIABLE, 2, 1),
                new Token(TokenType.EQUALS, 2),
                new Token(TokenType.CONSTANT, 2, 10),
                new Token(TokenType.THEN, 2),
                new Token(TokenType.GOTO, 2),
                new Token(TokenType.MARKER, 2, 6),
                new Token(TokenType.SEMICOLON, 2),

                new Token(TokenType.MARKER, 3, 3),
                new Token(TokenType.COLON, 3),
                new Token(TokenType.VARIABLE, 3, 2),
                new Token(TokenType.EQUALS, 3),
                new Token(TokenType.VARIABLE, 3, 1),
                new Token(TokenType.MINUS, 3),
                new Token(TokenType.CONSTANT, 3, 3),
                new Token(TokenType.SEMICOLON, 3),

                new Token(TokenType.MARKER, 4, 4),
                new Token(TokenType.COLON, 4),
                new Token(TokenType.GOTO, 4),
                new Token(TokenType.MARKER, 4, 1),
                new Token(TokenType.SEMICOLON, 4),

                new Token(TokenType.MARKER, 5, 6),
                new Token(TokenType.COLON, 5),
                new Token(TokenType.HALT, 5)
        ));

        //parse the tokens
        List<Statement> statements = parser.parse(tokens);

        assertFalse(ErrorHandler.hadError());

        assertInstanceOf(Assignment.class, statements.getFirst());
        Assignment assignment1 = (Assignment) statements.getFirst();
        assertEquals(1, assignment1.variable1Number());
        assertEquals(2, assignment1.variable2Number());
        assertEquals(Operator.ADDITION, assignment1.operator());
        assertEquals(5, assignment1.constant());
        assertEquals(1, assignment1.line());

        assertInstanceOf(If.class, statements.get(1));
        If ifStatement = (If) statements.get(1);
        assertEquals(1, ifStatement.variableNumber());
        assertEquals(10, ifStatement.constant());
        assertEquals(6, ifStatement.gotoMarkerNumber());
        assertEquals(2, ifStatement.line());

        assertInstanceOf(Assignment.class, statements.get(2));
        Assignment assignment2 = (Assignment) statements.get(2);
        assertEquals(2, assignment2.variable1Number());
        assertEquals(1, assignment2.variable2Number());
        assertEquals(Operator.SUBTRACTION, assignment2.operator());
        assertEquals(3, assignment2.constant());
        assertEquals(3, assignment2.line());

        assertInstanceOf(Goto.class, statements.get(3));
        Goto gotoStatement = (Goto) statements.get(3);
        assertEquals(1, gotoStatement.markerNumber());
        assertEquals(4, gotoStatement.line());

        assertInstanceOf(Halt.class, statements.get(4));
        Halt halt = (Halt) statements.get(4);
        assertEquals(5, halt.line());
    }

    /**
     * Tests the GOTO parser with a line that is missing the marker at the beginning
     * <p>
     * : x1 = x2 + 5; <br>
     * M2: Halt
     */
    @Test
    void testMissingLineMarker() {
        tokens = new ArrayList<>(List.of(
                new Token(TokenType.COLON, 1),
                new Token(TokenType.VARIABLE, 1, 1),
                new Token(TokenType.EQUALS, 1),
                new Token(TokenType.VARIABLE, 1, 2),
                new Token(TokenType.PLUS, 1),
                new Token(TokenType.CONSTANT, 1, 5),
                new Token(TokenType.SEMICOLON, 1),

                new Token(TokenType.MARKER, 2, 2),
                new Token(TokenType.COLON, 2),
                new Token(TokenType.HALT, 2)
        ));

        testInvalidCode(tokens);
    }

    /**
     * Tests the GOTO parser with a line that is missing the colon after the line marker
     * <p>
     * M1 x1 = x2 + 5; <br>
     * M2: Halt
     */
    @Test
    void testMissingColon() {
        tokens = new ArrayList<>(List.of(
                new Token(TokenType.MARKER, 1, 1),
                new Token(TokenType.VARIABLE, 1, 1),
                new Token(TokenType.EQUALS, 1),
                new Token(TokenType.VARIABLE, 1, 2),
                new Token(TokenType.PLUS, 1),
                new Token(TokenType.CONSTANT, 1, 5),
                new Token(TokenType.SEMICOLON, 1),

                new Token(TokenType.MARKER, 2, 2),
                new Token(TokenType.COLON, 2),
                new Token(TokenType.HALT, 2)
        ));

        testInvalidCode(tokens);
    }

    /**
     * Tests the GOTO parser with two lines of code that have the same line marker
     * <p>
     * M1: x1 = x2 + 5; <br>
     * M1: Halt
     */
    @Test
    void testDuplicateLineMarkers() {
        tokens = new ArrayList<>(List.of(
                new Token(TokenType.MARKER, 1, 1),
                new Token(TokenType.COLON, 1),
                new Token(TokenType.VARIABLE, 1, 1),
                new Token(TokenType.EQUALS, 1),
                new Token(TokenType.VARIABLE, 1, 2),
                new Token(TokenType.PLUS, 1),
                new Token(TokenType.CONSTANT, 1, 5),
                new Token(TokenType.SEMICOLON, 1),

                new Token(TokenType.MARKER, 2, 1),
                new Token(TokenType.COLON, 2),
                new Token(TokenType.HALT, 2)
        ));

        testInvalidCode(tokens);
    }

    /**
     * Tests the GOTO parser with code that is missing a HALT statement
     * <p>
     * M1: x1 = x2 + 5;
     */
    @Test
    void testMissingHalt() {
        tokens = new ArrayList<>(List.of(
                new Token(TokenType.MARKER, 1, 1),
                new Token(TokenType.COLON, 1),
                new Token(TokenType.VARIABLE, 1, 1),
                new Token(TokenType.EQUALS, 1),
                new Token(TokenType.VARIABLE, 1, 2),
                new Token(TokenType.PLUS, 1),
                new Token(TokenType.CONSTANT, 1, 5),
                new Token(TokenType.SEMICOLON, 1)
        ));

        testInvalidCode(tokens);
    }

    /**
     * Tests the GOTO parser with a GOTO statement that has a marker that does not exist in the code
     * <p>
     * M1: Goto M99; <br>
     * M2: Halt
     */
    @Test
    void testGotoInvalidMarker() {
        tokens = new ArrayList<>(List.of(
                new Token(TokenType.MARKER, 1, 1),
                new Token(TokenType.COLON, 1),
                new Token(TokenType.GOTO, 1),
                new Token(TokenType.MARKER, 1, 99),
                new Token(TokenType.SEMICOLON, 1),

                new Token(TokenType.MARKER, 2, 2),
                new Token(TokenType.COLON, 2),
                new Token(TokenType.HALT, 2)
        ));

        testInvalidCode(tokens);
    }

    /**
     * Tests the GOTO parser with a statement that starts with an invalid token
     * <p>
     * M1: M1 Goto M1; <br>
     * M2: Halt
     */
    @Test
    void invalidStatementStart() {
        tokens = new ArrayList<>(List.of(
                new Token(TokenType.MARKER, 1, 1),
                new Token(TokenType.COLON, 1),
                new Token(TokenType.MARKER, 1, 1),
                new Token(TokenType.GOTO, 1),
                new Token(TokenType.MARKER, 1, 1),
                new Token(TokenType.SEMICOLON, 1),

                new Token(TokenType.MARKER, 2, 2),
                new Token(TokenType.COLON, 2),
                new Token(TokenType.HALT, 2)
        ));

        testInvalidCode(tokens);
    }

    /**
     * Tests the GOTO parser with a line that has a marker but no other tokens
     * <p>
     * M1:
     */
    @Test
    void testEmptyLineWithMarker() {
        tokens = new ArrayList<>(List.of(
                new Token(TokenType.MARKER, 1, 1),
                new Token(TokenType.COLON, 1)
        ));

        testInvalidCode(tokens);
    }

    /**
     * Tests the GOTO parser with a line that is incomplete
     * <p>
     * M1: Goto
     */
    @Test
    void testSuddenEndOfCode() {
        tokens = new ArrayList<>(List.of(
                new Token(TokenType.MARKER, 1, 1),
                new Token(TokenType.COLON, 1),
                new Token(TokenType.GOTO, 1)
        ));

        testInvalidCode(tokens);
    }

    /**
     * helper method to test invalid code
     *
     * @param tokens the tokens to test
     */
    private void testInvalidCode(List<Token> tokens) {
        parser.parse(tokens);

        assertTrue(ErrorHandler.hadError());
        assertEquals(1, ErrorHandler.getErrors().size());
    }
}
