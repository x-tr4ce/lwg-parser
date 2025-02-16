package ch.zhaw.lwgparserapp.interpreter;

import ch.zhaw.lwgparserapp.error.ErrorHandler;
import ch.zhaw.lwgparserapp.syntax.Statement;
import ch.zhaw.lwgparserapp.syntax.generalsyntax.Assignment;
import ch.zhaw.lwgparserapp.syntax.gotosyntax.Goto;
import ch.zhaw.lwgparserapp.syntax.gotosyntax.Halt;
import ch.zhaw.lwgparserapp.syntax.gotosyntax.If;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * An interpreter for the GOTO language <br>
 * <p>
 * This class interprets a list of statements in the GOTO language. It uses a program counter to keep track of the
 * current statement being interpreted. The program counter is manipulated by the GOTO statement.
 */
public class GOTOInterpreter extends Interpreter {
    private int programCounter;
    List<Statement> statementList;
    private Map<Integer, Integer> markerLineMap;

    /**
     * Creates a new GOTOInterpreter <br>
     * <p>
     * Initializes the environment and sets the statement list to an empty list.
     *
     * @param environment the environment to use
     */
    public GOTOInterpreter(Environment environment) {
        super(environment);
        this.statementList = List.of();
        this.programCounter = 0;
    }

    public void setMarkerLineMap(Map<Integer, Integer> markerLineMap) {
        Objects.requireNonNull(markerLineMap, "Goto values map must not be null");
        this.markerLineMap = markerLineMap;
    }

    /**
     * Interprets a list of statements <br>
     * <p>
     * It checks if the list of statements is not null or empty
     * and then call the executeStatements method to interpret the statements.
     *
     * @param statements the list of statement to interpret
     */
    public void interpret(List<Statement> statements) {
        Objects.requireNonNull(statements, "Statements must not be null");

        if (statements.isEmpty()) {
            ErrorHandler.report(0, "Statements must not be empty");
            return;
        }
        this.statementList = statements;

        try {
            executeStatements();
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
     * Executes the statements <br>
     * <p>
     * This method executes the statements in the statement list. It iterates through the list of statements
     * and interprets each statement until the program counter is out of bounds or a Halt statement is reached.
     */
    private void executeStatements() {
        try {
            while (!isHalted()) {
                Statement currentStatement = statementList.get(programCounter);
                if(currentStatement == null) {
                    programCounter++;
                } else if (currentStatement instanceof Halt halt) {
                    environment.addDebugVariablesInDebugMode(halt.line());
                    break;
                } else {
                    interpretStatement(currentStatement);
                }
            }
        } catch (Exception e) {
            ErrorHandler.report(0, "Unexpected runtime exception:" + e.getMessage());
        }
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
            case Assignment assignment -> {
                interpretAssignment(assignment);
                programCounter++;
            }
            case If ifStatement -> interpretIf(ifStatement);
            case Goto gotoStatement -> interpretGoto(gotoStatement);
            default -> ErrorHandler.report(0, "Error in Interpreter.interpretStatement: Unknown statement type");
        }
    }

    /**
     * Interprets an If statement <br>
     * <p>
     * This method interprets an If statement. The variable number is the variable to compare with the constant.
     * If the variable is equal to the constant, the program counter is set to the marker number.
     *
     * @param ifStatement the If statement to interpret
     */
    private void interpretIf(If ifStatement) {
        int line = ifStatement.line();
        int variable = ifStatement.variableNumber();
        int constant = ifStatement.constant();
        int marker = ifStatement.gotoMarkerNumber();

        environment.addDebugVariablesInDebugMode(line);
        environment.initVariablesIfAbsent(variable);
        int value = environment.getVariable(variable);

        if (value == constant) {
            programCounter = findLineWithMarker(marker) ;
        } else {
            programCounter++;
        }
    }

    /**
     * Interprets a Goto statement <br>
     * Sets the program counter to the marker number of the Goto statement.
     *
     * @param gotoStatement the Goto statement to interpret
     */
    private void interpretGoto(Goto gotoStatement) {
        int line = gotoStatement.line();
        environment.addDebugVariablesInDebugMode(line);
        programCounter = findLineWithMarker(gotoStatement.markerNumber());
    }

    private int findLineWithMarker(int marker) {
        return markerLineMap.get(marker) - 1;
    }
}
