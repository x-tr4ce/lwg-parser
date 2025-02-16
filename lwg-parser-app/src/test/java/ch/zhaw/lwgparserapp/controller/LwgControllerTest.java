package ch.zhaw.lwgparserapp.controller;

import ch.zhaw.lwgparserapp.api.LwgController;
import ch.zhaw.lwgparserapp.api.LwgController.CodeRequest;
import ch.zhaw.lwgparserapp.api.LwgController.ApiResponse;
import ch.zhaw.lwgparserapp.api.LwgController.SuccessResponse;
import ch.zhaw.lwgparserapp.api.LwgController.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LwgControllerTest {

    private LwgController controller;

    @BeforeEach
    void setUp() {
        controller = new LwgController();
    }

    /**
     * Tests the processing of valid LW code.
     * Program:
     * Loop x2 Do
     *   x1 = x1 + 1
     * End
     * Variables: x1 = 0, x2 = 5
     * Expected Result: x1 is incremented to 5.
     */
    @Test
    void testProcessValidLWCode() {
        CodeRequest request = new CodeRequest();
        request.setCode(
        """
        Loop x2 Do
          x1 = x1 + 1
        End
        """);
        request.setVariables(Map.of("x1", 0));
        request.setVariables(Map.of("x2", 5));// Use correct variable format


        ResponseEntity<ApiResponse> response = controller.processCode(request);


        // Assertions
        assertNotNull(response, "Response should not be null");
        assertInstanceOf(SuccessResponse.class, response.getBody(), "Response should be a SuccessResponse");

        LwgController.SuccessResponse successResponse = (LwgController.SuccessResponse) response.getBody();
        assertNotNull(successResponse.variables(), "Variables should not be null");
        assertEquals(5, successResponse.variables().get(1), "x1 should be incremented to 5");
    }


    /**
     * Tests the processing of valid GOTO code.
     * Program:
     * M1: x0 = x1 + 2;
     * M2: Halt
     * Variables: x1 = 4
     * Expected Result: x0 is calculated as 6.
     */
    @Test
    void testProcessValidGOTOCode() {
        // Arrange
        CodeRequest request = new CodeRequest();
        request.setCode(
        """
        M1: x0 = x1 + 2;
        M2: Halt
        """);
        request.setVariables(Map.of("x1", 4));

        // Act
        ResponseEntity<ApiResponse> response = controller.processCode(request);

        // Assert: Check final value of the variable
        assertNotNull(response, "Response should not be null");
        assertInstanceOf(SuccessResponse.class, response.getBody(), "Response should be a SuccessResponse");

        SuccessResponse successResponse = (SuccessResponse) response.getBody();
        assertEquals(6, successResponse.variables().get(0), "result x0 should be 6");
    }

    /**
     * Tests the processing of mixed LW and GOTO syntax
     * Program:
     * x1 = x1 + 2;
     * Loop x2 Do
     *   M1: x1 = x1 - 1;
     *   Goto M3;
     *   x0 = x0 + 5; // Skipped
     *   M3: Halt;
     * End;
     * Expected Result: An error indicating mixed syntax is not allowed.
     */
    @Test
    void testProcessMixedSyntax() {
        // Arrange
        CodeRequest request = new CodeRequest();
        request.setCode(
                """
                x1 = x1 + 2;
                Loop x2 Do
                  M1: x1 = x1 - 1;
                  Goto M3;
                  x0 = x0 + 5;
                  M3: Halt;
                End
                """);

        request.setVariables(Map.of("x1", 5, "x2", 3)); // Initialize x1 and x2

        // Act
        ResponseEntity<ApiResponse> response = controller.processCode(request);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertInstanceOf(ErrorResponse.class, response.getBody(), "Response should be an ErrorResponse");

        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertNotNull(errorResponse.errors(), "Errors should not be null");
        assertTrue(errorResponse.errors().contains("The code contains both LW and GOTO syntax.\nPlease choose only one syntax at a time."),
                "Should be a mixed Syntax error");
    }


    /**
     * Tests the processing of code with syntax errors.
     * Program:
     * While x2 > 0 Do
     *   x1 = x1 + 1 (Missing semicolon)
     *   x2 = x2 - 1
     *   End
     * Variables: x1 = 10
     * Expected Result: A syntax error message is returned.
     */
    @Test
    void testProcessSyntaxError() {
        CodeRequest request = new CodeRequest();
        request.setCode(
                """
                While x2 > 0 + Do
                  x1 = x1 + 1
                  x2 = x2 - 1
                  End
                """);
        request.setVariables(Map.of("x1", 10));

        ResponseEntity<ApiResponse> response = controller.processCode(request);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertInstanceOf(ErrorResponse.class, response.getBody(), "Response should be an ErrorResponse");

        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertNotNull(errorResponse.errors(), "Errors should not be null");

        // Match the specific syntax error message
        assertTrue(errorResponse.errors().contains("There were syntax errors in your code."),
                "Should be a Syntax error");
    }

    /**
     * Tests the processing of empty source code.
     * Expected Result: No errors are thrown, and the response indicates success.
     */
    @Test
    void testProcessEmptyCode() {
        CodeRequest request = new CodeRequest();
        request.setCode(""); // Empty code

        ResponseEntity<ApiResponse> response = controller.processCode(request);

        // Assert
        assertNotNull(response, "Response should not be null");

        // Check if no errors are thrown and the response is of type SuccessResponse
        assertInstanceOf(SuccessResponse.class, response.getBody(), "Response should be a SuccessResponse");

        SuccessResponse successResponse = (SuccessResponse) response.getBody();
        assertNotNull(successResponse.variables(), "Variables should not be null");
    }


    /**
     * Tests the debugging of valid LW code.
     * Program:
     * Loop x2 Do
     *   x1 = x1 + 1
     * End
     * Variables: x1 = 0, x2 = 5
     */
    @Test
    void testDebug() {
        CodeRequest request = new CodeRequest();
        request.setCode(
                """
                Loop x2 Do
                  x1 = x1 + 1
                End
                """);
        request.setVariables(Map.of("x1", 0));
        request.setVariables(Map.of("x2", 5));// Use correct variable format


        controller.debugCode(request);
        controller.debugStep();
        controller.debugStep();
        controller.debugStep();
        controller.debugStep();
        ResponseEntity<ApiResponse> response = controller.debugPrevious();

        // Assertions
        assertNotNull(response, "Response should not be null");
        assertInstanceOf(LwgController.DebugResponseStandard.class, response.getBody(), "Response should be a SuccessResponse");

        LwgController.DebugResponseStandard debugResponse = (LwgController.DebugResponseStandard) response.getBody();
        assertNotNull(debugResponse.variables(), "Variables should not be null");
        assertEquals(1, debugResponse.variables().get(2).get(1));
        assertEquals(5, debugResponse.variables().get(2).get(2));
    }
}
