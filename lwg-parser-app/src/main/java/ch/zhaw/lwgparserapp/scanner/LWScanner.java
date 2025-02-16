package ch.zhaw.lwgparserapp.scanner;

import ch.zhaw.lwgparserapp.token.Token;
import ch.zhaw.lwgparserapp.token.TokenType;

import java.util.regex.Pattern;
/**
 * LoopWhileScanner the Scanner implementation for parsing Loop/While programs.
 * It recognizes keywords like LOOP, WHILE, DO, END, and operators like '=', '+', '-', and '>'.
 * The LoopWhileScanner class tokenizes input source code and generates a list of tokens.
 * It extends the abstract Scanner class and provides specific implementations for
 * keyword recognition.
 */
public class LWScanner extends Scanner {

    private static final Pattern KEYWORDS = Pattern.compile("Loop|While|Do|End|=|\\+|\\-|>|;");

    public LWScanner(String source) {
        super(source);
    }
    /**
     * Checks if a word matches any of the Loop/While language keywords.
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
            case "Loop":
                tokens.add(new Token(TokenType.LOOP, currentLine));
                break;
            case "While":
                tokens.add(new Token(TokenType.WHILE, currentLine));
                break;
            case "Do":
                tokens.add(new Token(TokenType.DO, currentLine));
                break;
            case "End":
                tokens.add(new Token(TokenType.END, currentLine));
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
            case ">":
                tokens.add(new Token(TokenType.GREATER_THAN, currentLine));
                break;
            case ";":
                tokens.add(new Token(TokenType.SEMICOLON, currentLine));
                break;
            default:
                throw new IllegalArgumentException("Invalid token: " + word);
        }
    }


}
