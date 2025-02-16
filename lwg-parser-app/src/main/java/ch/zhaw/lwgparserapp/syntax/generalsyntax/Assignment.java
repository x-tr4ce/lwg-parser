package ch.zhaw.lwgparserapp.syntax.generalsyntax;

import ch.zhaw.lwgparserapp.syntax.Statement;

/**
 * Represents an assignment statement.
 */
public record Assignment(int variable1Number, int variable2Number, Operator operator, int constant,
                         int line) implements Statement {
}
