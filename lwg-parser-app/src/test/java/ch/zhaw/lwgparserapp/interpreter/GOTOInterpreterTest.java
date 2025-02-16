package ch.zhaw.lwgparserapp.interpreter;

import ch.zhaw.lwgparserapp.error.ErrorHandler;
import ch.zhaw.lwgparserapp.syntax.Statement;
import ch.zhaw.lwgparserapp.syntax.generalsyntax.Assignment;
import ch.zhaw.lwgparserapp.syntax.generalsyntax.Operator;
import ch.zhaw.lwgparserapp.syntax.gotosyntax.Goto;
import ch.zhaw.lwgparserapp.syntax.gotosyntax.Halt;
import ch.zhaw.lwgparserapp.syntax.gotosyntax.If;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Test the GOTOParser class
 */
class GOTOInterpreterTest {
	GOTOInterpreter interpreter;
	Environment environment;

	@BeforeEach
	public void setUp() {
		environment = new Environment();
		interpreter = new GOTOInterpreter(environment);
		ErrorHandler.clearErrors();
	}

	/**
	 * Tests correct execution order with Goto statements.
	 * Program logic:
	 * M1: x1 = x1 + 5;
	 * M2: Goto M4;
	 * M3: x0 = x0 + 3; (should be skipped)
	 * M4: HALT;
	 */
	@Test
	void testGotoExecutionOrder() {
		List<Statement> statementList = new ArrayList<>();
		statementList.add(new Assignment(1, 1, Operator.ADDITION, 5, 1));
		statementList.add(new Goto(4, 2, 2));
		statementList.add(new Assignment(0, 0, Operator.ADDITION, 3, 3));
		statementList.add(new Halt(4, 4));

		Map<Integer, Integer> markerLineMap = Map.of(1, 1, 2, 2, 3, 3, 4, 4);
		interpreter.setMarkerLineMap(markerLineMap);

		interpreter.interpret(statementList);

		assertFalse(ErrorHandler.hadError());

		assertEquals(0, environment.getVariable(0)); // x0 should remain unchanged
		assertEquals(5, environment.getVariable(1)); // x1 = 5
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

		Map<Integer, Integer> markerLineMap = Map.of(1, 1, 2, 2);
		interpreter.setMarkerLineMap(markerLineMap);

		interpreter.interpret(statementList);

		// Verify that the overflow was detected
		assertEquals(0, environment.getVariable(0), "x0 should be set to 0 after overflow");
		assertTrue(ErrorHandler.hadError(), "An error should be reported for overflow");
		assertEquals("There was an arithmetic overflow.", ErrorHandler.getErrors().getFirst().getMessage());
	}


	/**
	 * Tests program termination with a Halt statement.
	 * Program logic:
	 * M1: x0 = x0 + 5;
	 * M2: Halt
	 * M3: x1 = x1 + 3; (should not execute)
	 */
	@Test
	void testHaltStatement() {
		List<Statement> statementList = new ArrayList<>();
		statementList.add(new Assignment(0, 0, Operator.ADDITION, 5, 1));
		statementList.add(new Halt(2, 2));
		statementList.add(new Assignment(1, 1, Operator.ADDITION, 3, 3));

		Map<Integer, Integer> markerLineMap = Map.of(1, 1, 2, 2, 3, 3);
		interpreter.setMarkerLineMap(markerLineMap);

		interpreter.interpret(statementList);

		// Verify execution stops at Halt
		assertEquals(5, environment.getVariable(0)); // x0 = 5
		assertEquals(0, environment.getVariable(1)); // x1 remains 0
		assertFalse(ErrorHandler.hadError());
	}

	/**
	 * Test the GOTO statement separately <br>
	 * <p>
	 * we test with a program: <br>
	 * M1 x1 = x2 + 5; <br>
	 * M2 Goto M4; <br>
	 * M3 x0 = x1 + 3; <br>
	 * M4 x0 = x1 - 2; <br>
	 * M5 HALT; <br>
	 */
	@Test
	void testGotoStatement() {
		List<Statement> statementList = new ArrayList<>();
		statementList.add(new Assignment(1, 2, Operator.ADDITION, 5, 1));
		statementList.add(new Goto(4, 2, 2));
		statementList.add(new Assignment(0, 1, Operator.ADDITION, 3, 3));
		statementList.add(new Assignment(0, 1, Operator.SUBTRACTION, 2, 4));
		statementList.add(new Halt(5, 5));

		Map<Integer, Integer> markerLineMap = Map.of(1, 1, 2, 2, 3, 3, 4, 4, 5, 5);
		interpreter.setMarkerLineMap(markerLineMap);

		interpreter.interpret(statementList);

		assertEquals(3, environment.getVariable(0)); // x0 should be 3 after x1 - 2
		assertFalse(ErrorHandler.hadError());
	}

	/**
	 * Test the If statement separately <br>
	 * <p>
	 * we test with a program: <br>
	 * M1 x1 = x1 + 5; <br>
	 * M2 If x1 = 5 Then Goto M4; <br>
	 * M3 x0 = x1 + 3; <br>
	 * M4 x0 = x1 - 2; <br>
	 * M5 Halt <br>
	 */
	@Test
	void testIfStatement() {
		List<Statement> statementList = new ArrayList<>();
		statementList.add(new Assignment(1, 1, Operator.ADDITION, 5, 1));
		statementList.add(new If(1, 5, 4, 2, 2));
		statementList.add(new Assignment(0, 1, Operator.ADDITION, 3, 3));
		statementList.add(new Assignment(0, 1, Operator.SUBTRACTION, 2, 4));
		statementList.add(new Halt(5, 5));

		Map<Integer, Integer> markerLineMap = Map.of(1, 1, 2, 2, 3, 3, 4, 4, 5, 5);
		interpreter.setMarkerLineMap(markerLineMap);

		interpreter.interpret(statementList);

		assertEquals(3, environment.getVariable(0)); // x0 should be 3 after x1 - 2
		assertFalse(ErrorHandler.hadError());
	}

	/**
	 * Tests behavior when the If statement condition is false.
	 * Program:
	 * M1: x1 = x1 + 5;
	 * M2: If x1 = 10 Then Goto M4; (if condition false, should be skipped)
	 * M3: x0 = x1 + 3; (should execute)
	 * M4: Halt
	 */
	@Test
	void testIfStatementConditionFalse() {
		List<Statement> statementList = new ArrayList<>();
		statementList.add(new Assignment(1, 1, Operator.ADDITION, 5, 1));
		statementList.add(new If(1, 10, 4, 2, 2));
		statementList.add(new Assignment(0, 1, Operator.ADDITION, 3, 3));
		statementList.add(new Halt(4, 4));

		Map<Integer, Integer> markerLineMap = Map.of(1, 1, 2, 2, 3, 3, 4, 4);
		interpreter.setMarkerLineMap(markerLineMap);

		interpreter.interpret(statementList);

		// Verify M3 executed and M4 was not reached
		assertEquals(8, environment.getVariable(0), "x0 should equal x1 + 3");
		assertEquals(5, environment.getVariable(1), "x1 should equal 5");
		assertFalse(ErrorHandler.hadError(), "No errors should occur during interpretation");
	}

	/**
	 * Tests behavior when the Goto statement refers to a negative marker.
	 * Program logic:
	 * M1: Goto M-1; (Invalid marker)
	 */
	@Test
	void testNegativeMarker() {
		List<Statement> statementList = new ArrayList<>();
		statementList.add(new Goto(-1, 1, 1));

		Map<Integer, Integer> markerLineMap = Map.of(1, 1);
		interpreter.setMarkerLineMap(markerLineMap);

		interpreter.interpret(statementList);

		// Verify that an error is reported
		assertTrue(ErrorHandler.hadError());
	}


	/**
	 * Test if the interpreter can handle a null statement
	 */
	@Test
	void testNullStatement() {
		// Verify that NullPointerException is thrown for null input
		NullPointerException exception = assertThrows(NullPointerException.class, () -> interpreter.interpret(null));

		// Check the exception message
		assertEquals("Statements must not be null", exception.getMessage());
	}



	/**
	 * Tests handling of an empty statement list.
	 */
	@Test
	void testEmptyStatementList() {
		interpreter.interpret(new ArrayList<>());

		// Verify that an error is reported for empty statements
		assertTrue(ErrorHandler.hadError());
		assertEquals("Statements must not be empty", ErrorHandler.getErrors().getFirst().getMessage());
	}

	/**
	 * Tests looping control flow with a Goto statement that creates an infinite loop.
	 * Program logic:
	 * M1: x0 = x0 + 1;
	 * M2: Goto M1; (Infinite loop)
	 * The interpreter should halt after the timeout to prevent infinite execution.
	 */
	@Test
	void testLoopingControlFlow() {
		interpreter.setHaltTimeout(2);

		List<Statement> statementList = new ArrayList<>();
		statementList.add(new Assignment(0, 0, Operator.ADDITION, 1, 1));
		statementList.add(new Goto(1, 2, 2));

		Map<Integer, Integer> markerLineMap = Map.of(1, 1, 2, 2);
		interpreter.setMarkerLineMap(markerLineMap);

		// Interpret asynchronously to ensure halt mechanism can kick in
		CompletableFuture<Void> future = interpreter.interpretAsync(statementList);
		future.join(); // Wait for execution to complete

		// Verify the interpreter halted
		assertTrue(interpreter.isHalted());
		assertTrue(ErrorHandler.hadError());
		assertEquals("Execution got halted, possibly due to an infinite loop", ErrorHandler.getErrors().getFirst().getMessage());
	}
	/**
	 * Tests handling of multiple Goto statements in a program with complex control flow.
	 * Program logic:
	 * M1: x1 = x1 + 5;
	 * M2: Goto M4;
	 * M3: x0 = x0 + 3; (skipped)
	 * M4: x1 = x1 - 2;
	 * M5: Goto M6;
	 * M6: Halt
	 */
	@Test
	void testMultipleGotoStatements() {
		List<Statement> statementList = new ArrayList<>();
		statementList.add(new Assignment(1, 1, Operator.ADDITION, 5, 1));
		statementList.add(new Goto(4, 2, 2));
		statementList.add(new Assignment(0, 0, Operator.ADDITION, 3, 3));
		statementList.add(new Assignment(1, 1, Operator.SUBTRACTION, 2, 4));
		statementList.add(new Goto(6, 5, 5));
		statementList.add(new Halt(6, 6));

		Map<Integer, Integer> markerLineMap = Map.of(1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6);
		interpreter.setMarkerLineMap(markerLineMap);

		// Interpret the program
		interpreter.interpret(statementList);

		// Verify expected behavior
		assertEquals(0, environment.getVariable(0)); // x0 remains unchanged (M3 skipped)
		assertEquals(3, environment.getVariable(1)); // x1 = 5 (M1) - 2 (M4)
		assertFalse(ErrorHandler.hadError()); // No errors should occur
	}
}