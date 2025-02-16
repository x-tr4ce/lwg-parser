package ch.zhaw.lwgparserapp.syntax;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Detects the syntax of a given code.
 * The syntax is determined by counting the occurrences of certain keywords in the code.
 */
public final class SyntaxDetector {
    /**
     * The pattern to match GOTO statements.
     * The pattern matches the following keywords:
     * - goto
     * - m followed by a digit
     * - if
     * - halt
     */
    private static final Pattern GOTO_PATTERN = Pattern.compile("\\bgoto\\b|\\bm\\d+\\b|\\bif\\b|\\bhalt\\b",
            Pattern.CASE_INSENSITIVE);

    /**
     * The pattern to match LW statements.
     * The pattern matches the following keywords:
     * - loop
     * - while
     * - do
     * - end
     */
    private static final Pattern WHILE_PATTERN = Pattern.compile("\\bloop\\b|\\bwhile\\b|\\bdo\\b|\\bend\\b",
            Pattern.CASE_INSENSITIVE);

    private SyntaxDetector() {
        throw new UnsupportedOperationException("SyntaxDetector is a utility class and cannot be instantiated");
    }

    /**
     * Detects the syntax of the given code.
     * The syntax is determined by counting the occurrences of certain keywords in the code.
     * If the number of GOTO statements is greater than the number of LW statements, the syntax is GOTO.
     * If the number of LW statements is greater than the number of GOTO statements, the syntax is LW.
     * If the number of GOTO statements is equal to the number of LW statements, the syntax is MIXED.
     *
     * @param code the code to detect the syntax of
     * @return the detected syntax
     * @throws NullPointerException if the code is null
     */
    public static Syntax detectSyntax(String code) {
        Objects.requireNonNull(code, "Code must not be null");
        long gotoCount = countMatches(code, GOTO_PATTERN);
        long lwCount = countMatches(code, WHILE_PATTERN);

        if (gotoCount == 0) return Syntax.LW;
        if (lwCount == 0) return Syntax.GOTO;

        return Syntax.MIXED;
    }

    /**
     * Counts the number of matches of the given pattern in the code.
     * The pattern is case-insensitive.
     *
     * @param code    the code to count the matches in
     * @param pattern the pattern to match
     * @return the number of matches
     */
    private static long countMatches(String code, Pattern pattern) {
        return pattern.matcher(code).results().count();
    }
}