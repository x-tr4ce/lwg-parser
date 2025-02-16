package ch.zhaw.lwgparserapp.error;

/**
 * Represents a parsing error with details about the line number and error message.
 * This class is used to describe errors encountered during the parsing process.
 */
public class ParseError {
    private final int line; // the line where the error occurred
    private final String message; // message describing the error

    /**
     * Constructs a new {@link ParseError} with the specified line number and message.
     *
     * @param line    the line number where the error occurred
     * @param message a message describing the error
     */
    public ParseError(int line, String message) {
        this.line = line;
        this.message = message;
    }

    /**
     * Returns the line number where the error occurred.
     *
     * @return the line number of the error
     */
    public int getLine() {
        return line;
    }

    /**
     * Returns the message describing the error.
     *
     * @return the error message
     */
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "[line " + line  + "] "   + message;
    }


}

