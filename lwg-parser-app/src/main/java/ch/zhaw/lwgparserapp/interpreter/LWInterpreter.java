package ch.zhaw.lwgparserapp.interpreter;

import ch.zhaw.lwgparserapp.error.ErrorHandler;
import ch.zhaw.lwgparserapp.syntax.Statement;
import ch.zhaw.lwgparserapp.syntax.generalsyntax.Assignment;
import ch.zhaw.lwgparserapp.syntax.lwsyntax.Loop;
import ch.zhaw.lwgparserapp.syntax.lwsyntax.While;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * The LWInterpreter class is responsible for interpreting the statements of the LW language.
 * <p>
 * The LWInterpreter class extends the Interpreter class and implements the interpret method.
 * The interpret method is responsible for interpreting a list of statements.
 */
public class LWInterpreter extends Interpreter {
    public LWInterpreter(Environment environment) {
        super(environment);
    }

    /**
     * Interprets a list of statements <br>
     * <p>
     * The method iterates over the list of statements received from the parser and interprets each one of them.
     * The interpretation of a statement depends on its type.
     * The method uses a switch statement to determine the type of the statement and
     * calls the appropriate method to interpret it.
     *
     * @param statements the list of statement to interpret
     */
    public void interpret(List<Statement> statements) {
        if (Objects.isNull(statements)) {
            ErrorHandler.report(0, "Error in Interpreter.interpret: statements must not be null");
            return;
        }

        try {
            for (Statement statement : statements) {
                if (isHalted()) {
                    break;
                }
                interpretStatement(statement);
            }
        } catch (Exception e) {
            ErrorHandler.report(0, "Unexpected runtime exception:" + e.getMessage());
        }
    }

    /**
     * Interprets a list of statements asynchronously <br>
     * <p>
     * The method interprets a list of statements asynchronously by running the interpretation in a separate thread.
     *
     * @param statements the list of statements to interpret
     * @return a CompletableFuture representing the asynchronous interpretation
     */
    public CompletableFuture<Void> interpretAsync(List<Statement> statements) {
        scheduleHalt();
        return CompletableFuture.runAsync(() -> {
            interpret(statements);
            if (isHalted) {
                ErrorHandler.report(0, "Execution got halted, possibly due to an infinite loop");
            }
        });
    }

    /**
     * Interprets a statement <br>
     * <p>
     * A statement is a sequence of code that can be executed. The method uses a switch statement to determine
     * the type of the statement and calls the appropriate method to interpret it.
     *
     * @param statement the current statement to interpret
     */
    private void interpretStatement(Statement statement) {
        switch (statement) {
            case Assignment assignment -> interpretAssignment(assignment);
            case Loop loop -> interpretLoop(loop);
            case While whileStatement -> interpretWhile(whileStatement);
            default -> ErrorHandler.report(0, "Error in Interpreter.interpretStatement: Unknown statement type");
        }
    }

    /**
     * Interprets a loop statement <br>
     * <p>
     * This method interprets a loop statement. The loop number must be a variable or a constant.
     *
     * @param loop the loop statement to interpret
     */
    private void interpretLoop(Loop loop) {
        int line = loop.line();
        List<Statement> statements = loop.statements();
        boolean usesConstant = loop.usesConstant();
        int number = loop.number();

        if (!usesConstant) {
            environment.initVariablesIfAbsent(number);
            number = environment.getVariable(number);
        }

        for (int i = 0; i < number; i++) {
            environment.addDebugVariablesInDebugMode(line);
            if (isHalted()) {
                break;
            }
            interpret(statements);
        }
    }

    /**
     * Interprets a while statement <br>
     * <p>
     * This method interprets a while statement. The while condition is always from the following form:
     * while (variable > 0) { ... }. The variableNumber must be from the type variable and never a constant as this
     * leads to an infinite loop.
     *
     * @param whileStatement the while statement to interpret
     */
    private void interpretWhile(While whileStatement) {
        int line = whileStatement.line();
        List<Statement> statements = whileStatement.statements();
        int variable = whileStatement.variableNumber();
        int constant = whileStatement.constant();

        environment.initVariablesIfAbsent(variable);

        while (environment.getVariable(variable) > constant) {
            environment.addDebugVariablesInDebugMode(line);
            if (isHalted()) {
                break;
            }
            interpret(statements);
        }
    }
}
