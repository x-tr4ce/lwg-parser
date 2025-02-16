package ch.zhaw.lwgparserapp.scanner;

import ch.zhaw.lwgparserapp.error.ErrorHandler;
import ch.zhaw.lwgparserapp.token.Token;
import ch.zhaw.lwgparserapp.token.TokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test the GOTOScanner class
 */
class GOTOScannerTest {

    @BeforeEach
    void setUp() {
        ErrorHandler.clearErrors();
    }

    /**
     * Test with all valid keywords.
     */
    @Test
    void testPositive() {
        String source = "M23489: ;    Halt; x30942 = + -\n Goto Halt If Then M0 25432";

        GOTOScanner scanner = new GOTOScanner(source);
        List<Token> tokens = scanner.scanProgram();

        assertNotNull(tokens);

        assertFalse(ErrorHandler.hadError());

        assertEquals(15, tokens.size());

        assertEquals(TokenType.MARKER, tokens.get(0).type);
        assertEquals(23489, tokens.get(0).value);
        assertEquals(TokenType.COLON, tokens.get(1).type);
        assertEquals(TokenType.SEMICOLON, tokens.get(2).type);
        assertEquals(TokenType.HALT, tokens.get(3).type);
        assertEquals(TokenType.SEMICOLON, tokens.get(4).type);
        assertEquals(TokenType.VARIABLE, tokens.get(5).type);
        assertEquals(30942, tokens.get(5).value);
        assertEquals(TokenType.EQUALS, tokens.get(6).type);
        assertEquals(TokenType.PLUS, tokens.get(7).type);
        assertEquals(TokenType.MINUS, tokens.get(8).type);
        assertEquals(TokenType.GOTO, tokens.get(9).type);
        assertEquals(TokenType.HALT, tokens.get(10).type);
        assertEquals(TokenType.IF, tokens.get(11).type);
        assertEquals(TokenType.THEN, tokens.get(12).type);
        assertEquals(TokenType.MARKER, tokens.get(13).type);
        assertEquals(0, tokens.get(13).value);
        assertEquals(TokenType.CONSTANT, tokens.get(14).type);
        assertEquals(25432, tokens.get(14).value);
    }

    /**
     * Test the Goto Scanner with invalid keywords
     */
    @Test
    void testInvalidKeyWords() {
        // all uppercase
        testInvalidCode("GOTO");

        // all lowercase
        testInvalidCode(" halt ");

        // mixed case
        testInvalidCode("GoTo");

        // invalid keyword followed by a semicolon
        testInvalidCode("goto;");

        // invalid keyword followed by a colon
        testInvalidCode("m1:");
    }

    /**
     * Test the Goto Scanner with a marker number too big for an integer and a negative number
     */
    @Test
    void testInvalidMarkers() {
        testInvalidCode("M12345678901234567890");
        testInvalidCode("M-1");
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
