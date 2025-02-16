package ch.zhaw.lwgparserapp.syntax.lwsyntax;

import ch.zhaw.lwgparserapp.syntax.Statement;

import java.util.List;

/**
 * Represents a for loop in the LW syntax.
 */
public record Loop(boolean usesConstant, int number, List<Statement> statements, int line) implements Statement {
}
