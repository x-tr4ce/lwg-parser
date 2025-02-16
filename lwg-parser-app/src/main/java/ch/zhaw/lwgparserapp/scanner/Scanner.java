package ch.zhaw.lwgparserapp.scanner;

import ch.zhaw.lwgparserapp.token.Token;
import ch.zhaw.lwgparserapp.token.TokenType;
import ch.zhaw.lwgparserapp.error.ErrorHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Scanner is an abstract base class for tokenizing source code written in various custom languages.
 * It provides common functionality for identifying variables, constants, and tokens, as well as
 * abstract methods for subclass-specific implementations.
 * <p>
 * Subclasses must implement methods for recognizing keywords and markers.
 */
public abstract class Scanner {
    protected final String source;
    protected final List<Token> tokens;
    protected int currentLine;

    // Common patterns for variables and constants
    protected static final Pattern IDENTIFIER = Pattern.compile("(?<!\\S)x\\d+(?!\\S)");
    protected static final Pattern CONSTANT = Pattern.compile("\\d+");

    protected Scanner(String source) {
        this.source = source;
        this.tokens = new ArrayList<>();
        this.currentLine = 1;
    }
    /**
     * Scans the entire program and returns the list of tokens.
     *
     * @return the list of tokens, or an empty list if an error occurred
     */
    public List<Token> scanProgram() {
        String[] lines = source.split("\n");
        for (String line : lines) {
            scanLine(line);
            currentLine++;
        }

        return ErrorHandler.hadError() ? Collections.emptyList() : tokens;
    }
    /**
     * Scans a single line and tokenizes its content.
     *
     * @param line the line to scan
     */
    private void scanLine(String line) {
        line = stripComments(line);

        String[] words = line.split("\\s+");
        for (String word : words) {
            if (word.isEmpty() || matchToken(word)) continue;
            if (checkSemicolon(word)) continue;
            ErrorHandler.report(currentLine, "Unexpected token '" + word + "'");
        }
    }

    /**
     * Strips comments from a line of code.
     *
     * @param line the line to strip
     * @return the line with comments removed
     */
    private static String stripComments(String line) {
        // Check for comments and strip them
        int commentIndex = line.indexOf("//");
        if (commentIndex != -1) {
            line = line.substring(0, commentIndex);
        }
        return line;
    }

    /**
     * Checks if a word ends with a semicolon and tokenizes it if so.
     *
     * @param word the word to check
     * @return true if the word was tokenized, false otherwise
     */
    private boolean checkSemicolon(String word) {
        if (word.endsWith(";")) {
            String tokenPart = word.substring(0, word.length() - 1);
            if (!tokenPart.isEmpty() && matchToken(tokenPart)) {
                tokens.add(new Token(TokenType.SEMICOLON, currentLine));
                return true;
            } else {
                ErrorHandler.report(currentLine, "Unexpected token '" + word + "'");
                return true;
            }
        }
        return false;
    }

    /**
     * Matches and tokenizes a single word.
     *
     * @param word the word to match
     * @return true if the word was successfully tokenized, false otherwise
     */
    protected boolean matchToken(String word) {
        // Match keywords using each scanner's specific keywords
        if (isKeyword(word)) {
            addKeywordToken(word);
            return true;
        }

        // Match variables (e.g., x1, x2)
        Matcher matcher = IDENTIFIER.matcher(word);
        if (matcher.matches()) {
            String number = word.substring(1);
            int value;
            try {
                value = Integer.parseInt(number);
                tokens.add(new Token(TokenType.VARIABLE, currentLine, value));
                return true;
            } catch (NumberFormatException e) {
                ErrorHandler.report(currentLine, "Variable is too big");
                return false;
            }
        }

        // Match constants (e.g., 0, 1, 2)
        matcher = CONSTANT.matcher(word);
        if (matcher.matches()) {
            try {
                int value = Integer.parseInt(word);
                tokens.add(new Token(TokenType.CONSTANT, currentLine, value));
                return true;
            } catch (NumberFormatException e) {
                ErrorHandler.report(currentLine, "Constant is too big");
                return false;
            }
        }

        return false;
    }


    // Abstract methods to be implemented in each subclass
    protected abstract boolean isKeyword(String word);

    protected abstract void addKeywordToken(String word);


}

