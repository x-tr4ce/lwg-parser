package ch.zhaw.lwgparserapp.syntax.lwsyntax;

import ch.zhaw.lwgparserapp.syntax.Statement;

import java.util.List;

/**
 * Represents a while loop in the LW syntax.
 */
public record While(int variableNumber, int constant, List<Statement> statements, int line) implements Statement {
}
