package ch.zhaw.lwgparserapp.parser;

import ch.zhaw.lwgparserapp.error.ErrorHandler;
import ch.zhaw.lwgparserapp.syntax.Statement;
import ch.zhaw.lwgparserapp.syntax.generalsyntax.Assignment;
import ch.zhaw.lwgparserapp.syntax.generalsyntax.Operator;
import ch.zhaw.lwgparserapp.syntax.lwsyntax.Loop;
import ch.zhaw.lwgparserapp.syntax.lwsyntax.While;
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
 * Test the LWParser class
 */
class LWParserTest {
    LWParser parser;
    List<Token> tokens;


    @BeforeEach
    void setUp() {
        parser = new LWParser();
        tokens = new ArrayList<>();
        ErrorHandler.clearErrors();
    }

    /**
     * positive test for the LOOP WHILE parser with all valid statements in the LW language
     * <p>
     * x1 = x2 + 5; <br>
     * While x1 > 0 Do <br>
     *    Loop x1 Do <br>
     *       Loop 5 Do <br>
     *       End <br>
     *    End; <br>
     *    x1 = x1 - 1 <br>
     * End
     */
    @Test
    void testPositive() {
        tokens = new ArrayList<>(List.of(
                new Token(TokenType.VARIABLE, 1, 1),
                new Token(TokenType.EQUALS, 1),
                new Token(TokenType.VARIABLE, 1, 2),
                new Token(TokenType.PLUS, 1),
                new Token(TokenType.CONSTANT, 1, 5),
                new Token(TokenType.SEMICOLON, 1),

                new Token(TokenType.WHILE, 2),
                new Token(TokenType.VARIABLE, 2, 1),
                new Token(TokenType.GREATER_THAN, 2),
                new Token(TokenType.CONSTANT, 2, 0),
                new Token(TokenType.DO, 2),

                new Token(TokenType.LOOP, 3),
                new Token(TokenType.VARIABLE, 3, 1),
                new Token(TokenType.DO, 3),

                new Token(TokenType.LOOP, 4),
                new Token(TokenType.CONSTANT, 4, 5),
                new Token(TokenType.DO, 4),

                new Token(TokenType.END, 5),

                new Token(TokenType.END, 6),
                new Token(TokenType.SEMICOLON, 6),

                new Token(TokenType.VARIABLE, 7, 1),
                new Token(TokenType.EQUALS, 7),
                new Token(TokenType.VARIABLE, 7, 1),
                new Token(TokenType.MINUS, 7),
                new Token(TokenType.CONSTANT, 7, 1),

                new Token(TokenType.END, 8)
        ));

        List<Statement> statements = parser.parse(tokens);

        assertFalse(ErrorHandler.hadError());

        assertInstanceOf(Assignment.class, statements.getFirst());
        Assignment assignment1 = (Assignment) statements.getFirst();
        assertEquals(1, assignment1.variable1Number());
        assertEquals(2, assignment1.variable2Number());
        assertEquals(Operator.ADDITION, assignment1.operator());
        assertEquals(5, assignment1.constant());
        assertEquals(1, assignment1.line());

        assertInstanceOf(While.class, statements.get(1));
        While while1 = (While) statements.get(1);
        assertEquals(1, while1.variableNumber());
        assertEquals(0, while1.constant());
        assertEquals(2, while1.line());

        assertInstanceOf(Loop.class, while1.statements().getFirst());
        Loop loop1 = (Loop) while1.statements().getFirst();
        assertFalse(loop1.usesConstant());
        assertEquals(1, loop1.number());
        assertEquals(3, loop1.line());

        assertInstanceOf(Loop.class, loop1.statements().getFirst());
        Loop loop2 = (Loop) loop1.statements().getFirst();
        assertTrue(loop2.usesConstant());
        assertEquals(5, loop2.number());
        assertEquals(4, loop2.line());

        assertInstanceOf(Assignment.class, while1.statements().get(1));
        Assignment assignment2 = (Assignment) while1.statements().get(1);
        assertEquals(1, assignment2.variable1Number());
        assertEquals(1, assignment2.variable2Number());
        assertEquals(Operator.SUBTRACTION, assignment2.operator());
        assertEquals(1, assignment2.constant());
        assertEquals(7, assignment2.line());
    }

    /**
     * Tests the LW parser with code that is missing an END statement
     * <p>
     * x1 = x2 + 5; <br>
     * While x1 > 0 Do <br>
     *    Loop x1 Do <br>
     *       Loop 5 Do <br>
     *    End; <br>
     *    x1 = x1 - 1 <br>
     * End
     */
    @Test
    void testMissingEnd() {
        tokens = new ArrayList<>(List.of(
                new Token(TokenType.VARIABLE, 1, 1),
                new Token(TokenType.EQUALS, 1),
                new Token(TokenType.VARIABLE, 1, 2),
                new Token(TokenType.PLUS, 1),
                new Token(TokenType.CONSTANT, 1, 5),
                new Token(TokenType.SEMICOLON, 1),

                new Token(TokenType.WHILE, 2),
                new Token(TokenType.VARIABLE, 2, 1),
                new Token(TokenType.GREATER_THAN, 2),
                new Token(TokenType.CONSTANT, 2, 0),
                new Token(TokenType.DO, 2),

                new Token(TokenType.LOOP, 3),
                new Token(TokenType.VARIABLE, 3, 1),
                new Token(TokenType.DO, 3),

                new Token(TokenType.LOOP, 4),
                new Token(TokenType.CONSTANT, 4, 5),
                new Token(TokenType.DO, 4),

                new Token(TokenType.END, 6),
                new Token(TokenType.SEMICOLON, 6),

                new Token(TokenType.VARIABLE, 7, 1),
                new Token(TokenType.EQUALS, 7),
                new Token(TokenType.VARIABLE, 7, 1),
                new Token(TokenType.MINUS, 7),
                new Token(TokenType.CONSTANT, 7, 1),

                new Token(TokenType.END, 8)
        ));

        testInvalidCode(tokens);
    }

    /**
     * Tests the LW parser with a semicolon before an END statement
     * <p>
     * While x1 > 22 Do <br>
     *    x1 = x1 - 1; <br>
     * End
     */
    @Test
    void testSemicolonBeforeEnd() {
        tokens = new ArrayList<>(List.of(
                new Token(TokenType.WHILE, 1),
                new Token(TokenType.VARIABLE, 1, 1),
                new Token(TokenType.GREATER_THAN, 1),
                new Token(TokenType.CONSTANT, 1, 0),
                new Token(TokenType.DO, 1),
                new Token(TokenType.VARIABLE, 2, 1),
                new Token(TokenType.EQUALS, 2),
                new Token(TokenType.VARIABLE, 2, 1),
                new Token(TokenType.MINUS, 2),
                new Token(TokenType.CONSTANT, 2, 1),
                new Token(TokenType.SEMICOLON, 2),
                new Token(TokenType.END, 3)
        ));

        testInvalidCode(tokens);
    }

    /**
     * Tests the LW parser with a WHILE statement that has a constant != 0
     * <p>
     * While x1 > 22 Do <br>
     * End
     */
    @Test
    void testInvalidWhileConstant() {
        tokens = new ArrayList<>(List.of(
                new Token(TokenType.WHILE, 1),
                new Token(TokenType.VARIABLE, 1, 1),
                new Token(TokenType.GREATER_THAN, 1),
                new Token(TokenType.CONSTANT, 1, 22),
                new Token(TokenType.DO, 1),
                new Token(TokenType.END, 2)
        ));

        testInvalidCode(tokens);
    }

    /**
     * Tests the LW parser with a code that contains too many END statements
     * <p>
     * While x1 > 22 Do <br>
     * End <br>
     * End
     */
    @Test
    void testTooManyEnds() {
        tokens = new ArrayList<>(List.of(
                new Token(TokenType.WHILE, 1),
                new Token(TokenType.VARIABLE, 1, 1),
                new Token(TokenType.GREATER_THAN, 1),
                new Token(TokenType.CONSTANT, 1, 0),
                new Token(TokenType.DO, 1),
                new Token(TokenType.END, 2),
                new Token(TokenType.END, 3)
        ));

        testInvalidCode(tokens);
    }

    /**
     * Tests the LW parser with a statement that starts with an invalid token
     * <p>
     * = x1 = x2 + 5;
     */
    @Test
    void testInvalidStatementStart() {
        tokens = new ArrayList<>(List.of(
                new Token(TokenType.EQUALS, 1),
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
     * Tests the LW parser with a line that is incomplete
     * <p>
     * x1 = x2 +
     */
    @Test
    void testSuddenEndOfCode() {
        tokens = new ArrayList<>(List.of(
                new Token(TokenType.VARIABLE, 1, 1),
                new Token(TokenType.EQUALS, 1),
                new Token(TokenType.VARIABLE, 1, 2),
                new Token(TokenType.PLUS, 1)
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
