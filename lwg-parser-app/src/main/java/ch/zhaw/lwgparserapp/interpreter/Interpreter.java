package ch.zhaw.lwgparserapp.interpreter;

import ch.zhaw.lwgparserapp.error.ErrorHandler;
import ch.zhaw.lwgparserapp.syntax.Statement;
import ch.zhaw.lwgparserapp.syntax.generalsyntax.Assignment;
import ch.zhaw.lwgparserapp.syntax.generalsyntax.Operator;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The Interpreter class is responsible for interpreting the statements of the LW or GOTO languages.
 * <p>
 * The Interpreter class is an abstract class that provides a common interface for all interpreters.
 * It contains a reference to the environment and an abstract method to interpret a list of statements.
 */
public abstract class Interpreter {
    final Environment environment;
    protected volatile boolean isHalted = false;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private int haltTimeout = 15; // Default timeout in seconds
    /**
     * Constructs an Interpreter object with the specified environment.
     *
     * @param environment the environment to use for interpretation
     */
    Interpreter(Environment environment) {
        Objects.requireNonNull(environment, "Environment must not be null");
        this.environment = environment;
    }

    public abstract void interpret(List<Statement> statements);

    public abstract CompletableFuture<Void> interpretAsync(List<Statement> statements);

    /**
     * Halts the interpreter.
     */
    public void halt() {
        isHalted = true;
        scheduler.shutdownNow();
    }

    public void setHaltTimeout(int seconds) {
        this.haltTimeout = seconds;
    }

    /**
     * Checks if the interpreter is halted.
     *
     * @return true if the interpreter is halted, false otherwise
     */
    public boolean isHalted() {
        return isHalted;
    }

    /**
     * Schedules the interpreter to halt after a specified delay.
     */
    protected void scheduleHalt() {
        scheduler.schedule(this::halt, haltTimeout, TimeUnit.SECONDS);
    }

    /**
     * Interprets an assignment statement <br>
     * <p>
     * This method interprets an assignment statement. The first variable number is the assignee variable and
     * the second variable number is the first operand of the operation. The operator is either addition or subtraction.
     * The second operand must be a constant.
     *
     * @param assignment the assignment statement to interpret
     */
    void interpretAssignment(Assignment assignment) {
        int line = assignment.line();
        int firstVariable = assignment.variable1Number();
        int secondVariable = assignment.variable2Number();
        Operator operator = Objects.requireNonNull(assignment.operator());
        int constant = assignment.constant();

        environment.initVariablesIfAbsent(firstVariable, secondVariable);
        int secondValue = environment.getVariable(secondVariable);
        try {
            switch (operator) {
                case ADDITION -> environment.setVariable(line, firstVariable, Math.addExact(secondValue, constant));
                case SUBTRACTION -> environment.setVariable(line, firstVariable, Math.max(0, secondValue - constant));
                default -> ErrorHandler.report(line,
                        "Error in Interpreter.interpretAssignment: Unknown operator type");
            }
        } catch (ArithmeticException e) {
            ErrorHandler.report(line, "There was an arithmetic overflow.");
            environment.setVariable(line, firstVariable, 0);
        }
    }
}
