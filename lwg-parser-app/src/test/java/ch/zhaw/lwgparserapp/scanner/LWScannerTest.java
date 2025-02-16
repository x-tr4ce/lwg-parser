package ch.zhaw.lwgparserapp.scanner;

import ch.zhaw.lwgparserapp.error.ErrorHandler;
import ch.zhaw.lwgparserapp.token.Token;
import ch.zhaw.lwgparserapp.token.TokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test the LWScanner class
 */
class LWScannerTest {

    @BeforeEach
    void setUp() {
        ErrorHandler.clearErrors();
    }

    /**
     * Test with all valid keywords.
     */
    @Test
    void testLoopProgram() {
        String source = "Loop  While Do End; ;  x1324\n = + - 1567 >";
        LWScanner scanner = new LWScanner(source);
        List<Token> tokens = scanner.scanProgram();

        assertNotNull(tokens);

        assertFalse(ErrorHandler.hadError());


        assertEquals(12, tokens.size());

        assertEquals(TokenType.LOOP, tokens.get(0).type);
        assertEquals(TokenType.WHILE, tokens.get(1).type);
        assertEquals(TokenType.DO, tokens.get(2).type);
        assertEquals(TokenType.END, tokens.get(3).type);
        assertEquals(TokenType.SEMICOLON, tokens.get(4).type);
        assertEquals(TokenType.SEMICOLON, tokens.get(5).type);
        assertEquals(TokenType.VARIABLE, tokens.get(6).type);
        assertEquals(1324, tokens.get(6).value);
        assertEquals(TokenType.EQUALS, tokens.get(7).type);
        assertEquals(TokenType.PLUS, tokens.get(8).type);
        assertEquals(TokenType.MINUS, tokens.get(9).type);
        assertEquals(TokenType.CONSTANT, tokens.get(10).type);
        assertEquals(1567, tokens.get(10).value);
        assertEquals(TokenType.GREATER_THAN, tokens.get(11).type);
    }

    /**
     * Test the LoopWhile Scanner with invalid keywords
     */
    @Test
    void testInvalidLoopProgram() {
        // all uppercase
        testInvalidCode("WHILE");

        // all lowercase
        testInvalidCode(" end ");

        // mixed case
        testInvalidCode("LoOp");

        // invalid keyword followed by a semicolon
        testInvalidCode("end;");

        // invalid keyword followed by a colon
        testInvalidCode("X1:");
    }

    /**
     * Test the Goto Scanner with a variable number too big for an integer and a negative number
     */
    @Test
    void testInvalidVariables() {
        testInvalidCode("x12345678901234567890");
        testInvalidCode("x-1");
    }

    /**
     * Test the Goto Scanner with a constant too big for an integer
     */
    @Test
    void testInvalidConstant() {
        testInvalidCode("12345678901234567890");
    }

    /**
     * helper method to test invalid code
     *
     * @param source the source code to test
     */
    private void testInvalidCode(String source) {
        GOTOScanner scanner = new GOTOScanner(source);
        scanner.scanProgram();

        assertTrue(ErrorHandler.hadError());
    }
}