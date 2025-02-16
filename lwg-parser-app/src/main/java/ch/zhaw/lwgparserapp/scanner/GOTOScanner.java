package ch.zhaw.lwgparserapp.scanner;

import ch.zhaw.lwgparserapp.error.ErrorHandler;
import ch.zhaw.lwgparserapp.token.Token;
import ch.zhaw.lwgparserapp.token.TokenType;

import java.util.regex.Pattern;

/**
 * GOTOScanner is the Scanner implementation for parsing GOTO programs.
 * It recognizes keywords like GOTO, IF, THEN, HALT, operators like '=', '+', '-', and
 * markers like M1, M2, etc.
 * <p>
 * The GOTOScanner tokenizes input source code and generates a list of tokens.
 * It extends the abstract Scanner class and provides specific implementations for
 * keyword and marker recognition.
 */
public class GOTOScanner extends Scanner {

    private static final Pattern KEYWORDS = Pattern.compile("Goto|If|Then|Halt|=|\\+|-|;");
    private static final Pattern MARKER = Pattern.compile("(?<!\\S)M\\d+:?(?!\\S)");

    public GOTOScanner(String source) {
        super(source);
    }

    /**
     * Checks if a word matches any of the GOTO language keywords.
     *
     * @param word the word to check
     * @return true if the word is a keyword, false otherwise
     */
    @Override
    protected boolean isKeyword(String word) {
        return KEYWORDS.matcher(word).matches();
    }

    /**
     * Adds a token for the specified keyword.
     *
     * @param word the keyword to add as a token
     */
    @Override
    protected void addKeywordToken(String word) {
        switch (word) {
            case "Goto":
                tokens.add(new Token(TokenType.GOTO, currentLine));
                break;
            case "If":
                tokens.add(new Token(TokenType.IF, currentLine));
                break;
            case "Then":
                tokens.add(new Token(TokenType.THEN, currentLine));
                break;
            case "Halt":
                tokens.add(new Token(TokenType.HALT, currentLine));
                break;
            case "=":
                tokens.add(new Token(TokenType.EQUALS, currentLine));
                break;
            case "+":
                tokens.add(new Token(TokenType.PLUS, currentLine));
                break;
            case "-":
                tokens.add(new Token(TokenType.MINUS, currentLine));
                break;
            case ";":
                tokens.add(new Token(TokenType.SEMICOLON, currentLine));
                break;
            default:
                throw new IllegalArgumentException("Invalid token: " + word);
        }
    }

    /**
     * Checks if a word matches the marker pattern (e.g., M1, M2).
     *
     * @param word the word to check
     * @return true if the word is a marker, false otherwise
     */
    protected boolean isMarker(String word) {
        return MARKER.matcher(word).matches();
    }

    /**
     * Adds a token for the specified marker.
     * Adds a second token if a colon follows the marker.
     *
     * @param word the marker to add as a token
     */

    protected void addMarkerToken(String word) {
        try {
            int value = Integer.parseInt(word.substring(1, word.length() - (word.endsWith(":") ? 1 : 0)));
            tokens.add(new Token(TokenType.MARKER, currentLine, value));
            if (word.endsWith(":")) {
                tokens.add(new Token(TokenType.COLON, currentLine));
            }
        } catch (NumberFormatException e) {
            ErrorHandler.report(currentLine, "Marker is too big");
        }
    }

    /**
     * Matches tokens specific to GOTO programs, including markers.
     *
     * @param word the word to match
     * @return true if the word was successfully tokenized, false otherwise
     */
    @Override
    protected boolean matchToken(String word) {
        if (isKeyword(word)) {
            addKeywordToken(word);
            return true;
        }

        if (isMarker(word)) {
            addMarkerToken(word);
            return true;
        }

        return super.matchToken(word); // Fallback to common token matching
    }
}
