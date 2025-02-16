package ch.zhaw.lwgparserapp.syntax.gotosyntax;

import ch.zhaw.lwgparserapp.syntax.Statement;

/**
 * Represents an If statement.
 */
public record If(int variableNumber, int constant, int gotoMarkerNumber, int markerLine, int line) implements Statement {
}
