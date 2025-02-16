package ch.zhaw.lwgparserapp.error;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles error reporting and stores errors for future retrieval.
 */
public class ErrorHandler {
    private static boolean hadError = false;  // Internal flag to track errors
    private static final List<ParseError> errorList = new ArrayList<>();  // Internal list of errors

    private ErrorHandler() {
        // Private constructor to prevent instantiation
    }

    /**
     * Saves the error into the list for later retrieval.
     *
     * @param line    the line number where the error occurred
     * @param message the error message describing the issue
     */
    public static void report(int line, String message) {
        errorList.add(new ParseError(line, message));
        hadError = true; // Marks that an error occurred
    }

    /**
     * Returns the list of errors encountered during parsing.
     *
     * @return an unmodifiable list of ParseError objects
     */
    public static List<ParseError> getErrors() {
        return errorList;
    }

    /**
     * Clears the error state and list of errors.
     */
    public static void clearErrors() {
        hadError = false;
        errorList.clear();
    }

    /**
     * Returns true if any errors were encountered.
     *
     * @return true if an error has occurred, false otherwise
     */
    public static boolean hadError() {
        return hadError;
    }
}

