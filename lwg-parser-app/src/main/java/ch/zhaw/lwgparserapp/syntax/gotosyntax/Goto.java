package ch.zhaw.lwgparserapp.syntax.gotosyntax;

import ch.zhaw.lwgparserapp.syntax.Statement;

/**
 * Represents a Goto statement.
 */
public record Goto(int markerNumber, int markerLine, int line) implements Statement {
}
