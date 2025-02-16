package ch.zhaw.lwgparserapp.syntax.gotosyntax;

import ch.zhaw.lwgparserapp.syntax.Statement;

/**
 * Represents a halt statement.
 */
public record Halt(int markerLine, int line) implements Statement {
}
