package ch.zhaw.lwgparserapp.interpreter;

import ch.zhaw.lwgparserapp.error.ErrorHandler;
import ch.zhaw.lwgparserapp.syntax.Statement;
import ch.zhaw.lwgparserapp.syntax.generalsyntax.Assignment;
import ch.zhaw.lwgparserapp.syntax.generalsyntax.Operator;
import ch.zhaw.lwgparserapp.syntax.lwsyntax.Loop;
import ch.zhaw.lwgparserapp.syntax.lwsyntax.While;
import ch.zhaw.lwgparserapp.syntax.gotosyntax.Goto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the LWInterpreter class
 */
class LWInterpreterTest {
    LWInterpreter lwInterpreter;
    Environment environment;

    @BeforeEach
    public void setUp() {
        environment = new Environment();
        lwInterpreter = new LWInterpreter(environment);
        ErrorHandler.clearErrors();
    }

    /**
     * Test the loop interpreter with a simple program <br>
     * <p>
     * we test with a multiplication program: <br>
     * x0 = x0 + 3; <br>
     * x1 = x1 + 2; <br>
     * <br>
     * x1 = x1 - 1; <br>
     * x9 = x0 + 0; <br>
     * LOOP x1 DO <br>
     *   LOOP x9 DO <br>
     *       x0 = x0 + 1 <br>
     *   END <br>
     * END <br>
     */
    @Test
    void testLoopProgram() {
        List<Statement> statementList = new ArrayList<>();
        statementList.add(new Assignment(0, 0, Operator.ADDITION, 3, 1));
        statementList.add(new Assignment(1, 1, Operator.ADDITION, 2, 2));
        statementList.add(new Assignment(1, 1, Operator.SUBTRACTION, 1, 4));
        statementList.add(new Assignment(9, 0, Operator.ADDITION, 0, 5));
        List<Statement> loopStatements1 = new ArrayList<>();
        List<Statement> loopStatements2 = new ArrayList<>();
        loopStatements2.add(new Assignment(0, 0, Operator.ADDITION, 1, 8));
        loopStatements1.add(new Loop(false, 9, loopStatements2, 7));
        statementList.add(new Loop(false, 1, loopStatements1, 6));

        lwInterpreter.interpret(statementList);

        assertEquals(6, environment.getVariable(0));
        assertEquals(1, environment.getVariable(1));
        assertEquals(3, environment.getVariable(9));
        assertFalse(ErrorHandler.hadError());
    }

    /**
     * Test the while interpreter with a simple program <br>
     * <p>
     * we test with a multiplication program: <br>
     * x0 = x0 + 3; <br>
     * x1 = x1 + 2; <br>
     * <br>
     * x1 = x1 - 1; <br>
     * x9 = x0 + 0; <br>
     * WHILE x1 > 0 DO <br>
     *   WHILE x9 > 0 DO <br>
     *       x0 = x0 + 1; <br>
     *       x9 = x9 - 1 <br>
     *   END; <br>
     *   x1 = x1 - 1 <br>
     * END <br>
     */
    @Test
    void testWhileProgram() {
        List<Statement> statementList = new ArrayList<>();
        statementList.add(new Assignment(0, 0, Operator.ADDITION, 3, 1));
        statementList.add(new Assignment(1, 1, Operator.ADDITION, 2, 2));
        statementList.add(new Assignment(1, 1, Operator.SUBTRACTION, 1, 4));
        statementList.add(new Assignment(9, 0, Operator.ADDITION, 0, 5));
        List<Statement> loopStatements1 = new ArrayList<>();
        List<Statement> loopStatements2 = new ArrayList<>();
        loopStatements2.add(new Assignment(0, 0, Operator.ADDITION, 1, 8));
        loopStatements2.add(new Assignment(9, 9, Operator.SUBTRACTION, 1, 9));
        loopStatements1.add(new While(9, 0, loopStatements2, 7));
        loopStatements1.add(new Assignment(1, 1, Operator.SUBTRACTION, 1, 11));
        statementList.add(new While(1, 0, loopStatements1, 6));

        lwInterpreter.interpret(statementList);

        assertEquals(6, environment.getVariable(0));
        assertEquals(0, environment.getVariable(1));
        assertEquals(0, environment.getVariable(9));
        assertFalse(ErrorHandler.hadError());
    }
    /**
     * Tests the interpretation of Assignment statements. <br>
     * Program: <br>
     * x0 = x0 + 5; <br>
     * x1 = x1 + 3; <br>
     * x0 = x0 - 2; <br>
     * x1 = x1 - 5 (clamped to 0); <br>
     */
    @Test
    void testInterpretAssignmentStatements() {
        List<Statement> statementList = new ArrayList<>();

        // Add addition assignments
        statementList.add(new Assignment(0, 0, Operator.ADDITION, 5, 1));
        statementList.add(new Assignment(1, 1, Operator.ADDITION, 3, 2));

        // Add subtraction assignments
        statementList.add(new Assignment(0, 0, Operator.SUBTRACTION, 2, 3));
        statementList.add(new Assignment(1, 1, Operator.SUBTRACTION, 5, 4));

        // Interpret the assignment statements
        lwInterpreter.interpret(statementList);

        // Verify the results
        assertEquals(3, environment.getVariable(0)); // x0 = 5 (add) - 2 (subtract)
        assertEquals(0, environment.getVariable(1)); // x1 = 3 (add) - 5 (clamped to 0)
        assertFalse(ErrorHandler.hadError());
    }


    /**
     * Tests the interpretation of Loop statements. <br>
     * Program: <br>
     * x1 = x1 + 3; <br>
     * LOOP x1 DO <br>
     *   x0 = x0 + 1; <br>
     * END <br>
     */
    @Test
    void testInterpretLoopStatements() {
        List<Statement> statementList = new ArrayList<>();

        // Add an assignment to initialize x1
        statementList.add(new Assignment(1, 1, Operator.ADDITION, 3, 0));

        // Add a loop
        List<Statement> loopBody = new ArrayList<>();
        loopBody.add(new Assignment(0, 0, Operator.ADDITION, 1, 1));
        statementList.add(new Loop(false, 1, loopBody, 2));

        // Interpret the loop statement
        lwInterpreter.interpret(statementList);

        // Verify the results
        assertEquals(3, environment.getVariable(0)); // x0 = 3 (3 iterations)
        assertEquals(3, environment.getVariable(1)); // x1 unchanged after loop
        assertFalse(ErrorHandler.hadError());
    }

    /**
     * Tests the interpretation of While statements. <br>
     * Program: <br>
     * x1 = x1 + 3; <br>
     * x9 = x9 + 2; <br>
     * WHILE x9 > 0 DO <br>
     *   x9 = x9 - 1; <br>
     * END <br>
     */
    @Test
    void testInterpretWhileStatements() {
        List<Statement> statementList = new ArrayList<>();

        // Add assignments to initialize x1 and x9
        statementList.add(new Assignment(1, 1, Operator.ADDITION, 3, 0));
        statementList.add(new Assignment(9, 9, Operator.ADDITION, 2, 1));

        // Add a while loop
        List<Statement> whileBody = new ArrayList<>();
        whileBody.add(new Assignment(9, 9, Operator.SUBTRACTION, 1, 2));
        statementList.add(new While(9, 0, whileBody, 3));

        // Interpret the while statement
        lwInterpreter.interpret(statementList);

        // Verify the results
        assertEquals(0, environment.getVariable(9)); // x9 reduced to 0 in the while loop
        assertEquals(3, environment.getVariable(1)); // x1 unchanged
        assertFalse(ErrorHandler.hadError());
    }



    /**
     * Test if the interpreter can handle a null statement
     */
    @Test
    void testNullStatement() {
        lwInterpreter.interpret(null);
        assertTrue(ErrorHandler.hadError());
        assertEquals("Error in Interpreter.interpret: statements must not be null", ErrorHandler.getErrors().getFirst().getMessage());
    }

    /**
     * Tests the interpreter's handling of an empty statement list.
     */
    @Test
    void testEmptyStatementList() {
        lwInterpreter.interpret(new ArrayList<>());
        assertFalse(ErrorHandler.hadError());
    }

    /**
     * Tests that integer overflow is correctly handled.
     * Program:
     * x0 = x0 + Integer.MAX_VALUE;
     * x0 = x0 + 1 // Causes overflow
     * Expected Result: Overflow error reported, x0 set to 0.
     */
    @Test
    void testIntegerOverflow() {
        List<Statement> statementList = new ArrayList<>();
        statementList.add(new Assignment(0, 0, Operator.ADDITION, Integer.MAX_VALUE, 1));
        statementList.add(new Assignment(0, 0, Operator.ADDITION, 1, 2));

        lwInterpreter.interpret(statementList);

        // Verify that the overflow was detected
        assertEquals(0, environment.getVariable(0), "x0 should be set to 0 after overflow");
        assertTrue(ErrorHandler.hadError(), "An error should be reported for overflow");
        assertEquals("There was an arithmetic overflow.", ErrorHandler.getErrors().getFirst().getMessage());
    }

    /**
     * Test for infinite loop handling in the interpreter. <br>
     * Program: <br>
     * x0 = x0 + 3; <br>
     * x1 = x1 + 2; <br>
     * WHILE x1 > 0 DO <br>
     *   x0 = x0 + 1; <br>
     * END <br>
     */
    @Test
    void testInfiniteWhileLoop() {

        lwInterpreter.setHaltTimeout(2); // Reduce timeout to 2 seconds for this test

        List<Statement> statementList = new ArrayList<>();
        statementList.add(new Assignment(0, 0, Operator.ADDITION, 3, 1));
        statementList.add(new Assignment(1, 1, Operator.ADDITION, 2, 2));

        // Create an infinite while loop
        List<Statement> whileBody = new ArrayList<>();
        whileBody.add(new Assignment(0, 0, Operator.ADDITION, 1, 3));
        statementList.add(new While(1, 0, whileBody, 4));

        // Interpret the program asynchronously
        CompletableFuture<Void> future = lwInterpreter.interpretAsync(statementList);

        // Wait for the scheduled halt to trigger
        future.join();

        // Verify that the interpreter halted due to the infinite loop
        assertTrue(lwInterpreter.isHalted());
        assertTrue(ErrorHandler.hadError());
        assertEquals("Execution got halted, possibly due to an infinite loop", ErrorHandler.getErrors().getFirst().getMessage());
    }

    /**
     * Tests that unsupported statements trigger an error in interpretStatement.
     */
    @Test
    void testUnsupportedStatement() {
        // Create a Goto statement (unsupported in LWInterpreter)
        Statement unsupportedStatement = new Goto(0,5, 1);

        // Add it to a statement list
        List<Statement> statementList = new ArrayList<>();
        statementList.add(unsupportedStatement);

        // Interpret the list using LWInterpreter
        lwInterpreter.interpret(statementList);

        // Verify that an error is reported
        assertTrue(ErrorHandler.hadError());
        assertEquals("Error in Interpreter.interpretStatement: Unknown statement type",
                ErrorHandler.getErrors().getFirst().getMessage());
    }









}
