package ch.zhaw.lwgparserapp.api;

import ch.zhaw.lwgparserapp.debugger.Debugger;
import ch.zhaw.lwgparserapp.error.ErrorHandler;
import ch.zhaw.lwgparserapp.interpreter.Environment;
import ch.zhaw.lwgparserapp.interpreter.GOTOInterpreter;
import ch.zhaw.lwgparserapp.interpreter.Interpreter;
import ch.zhaw.lwgparserapp.interpreter.LWInterpreter;
import ch.zhaw.lwgparserapp.parser.GOTOParser;
import ch.zhaw.lwgparserapp.parser.LWParser;
import ch.zhaw.lwgparserapp.parser.Parser;
import ch.zhaw.lwgparserapp.scanner.GOTOScanner;
import ch.zhaw.lwgparserapp.scanner.LWScanner;
import ch.zhaw.lwgparserapp.scanner.Scanner;
import ch.zhaw.lwgparserapp.syntax.Statement;
import ch.zhaw.lwgparserapp.syntax.Syntax;
import ch.zhaw.lwgparserapp.syntax.SyntaxDetector;
import ch.zhaw.lwgparserapp.token.Token;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The LWG controller class is used to handle incoming requests and process the code.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:8080")
public class LwgController {
    private static final String TYPE_SUCCESS = "success";
    private static final String TYPE_SUCCESS_DEBUG_LAST_ELEMENT = "border_last";
    private static final String TYPE_SUCCESS_DEBUG_FIRST_ELEMENT = "border_first";
    private static final String TYPE_ERROR = "error";
    private static Interpreter interpreter;

    /**
     * Processes the code and returns the variables.
     *
     * @param request the code request object containing the code to process
     * @return the response object containing the variables
     */
    @PostMapping("/process")
    public ResponseEntity<ApiResponse> processCode(@RequestBody CodeRequest request) {
        ErrorHandler.clearErrors();
        String inputCode = request.getCode();
        Map<String, Integer> inputVariables = request.getVariables();
        ApiResponse response = runCode(inputCode, inputVariables, false);
        return ResponseEntity.ok(response);
    }

    /**
     * Stops the interpreter from running.
     */
    @PostMapping("/stop")
    public void stop() {
        if (interpreter != null) interpreter.halt();
    }

    /**
     * Initiates the debugging process and returns the corresponding variables.
     *
     * @param request the code request object containing the code to process
     * @return the response object containing the variables
     */
    @PostMapping("/debug")
    public ResponseEntity<ApiResponse> debugCode(@RequestBody CodeRequest request) {
        ErrorHandler.clearErrors();
        Debugger.reset();
        String inputCode = request.getCode();
        Map<String, Integer> inputVariables = request.getVariables();
        ApiResponse response = runCode(inputCode, inputVariables, true);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/debug/next")
    public ResponseEntity<ApiResponse> debugStep() {
        Map<Integer, Map<Integer, Integer>> variables = Debugger.nextStep();
        ApiResponse response;
        if (Debugger.lastStep) {
            response = new DebugResponseBorderLast(variables);
        } else if (Debugger.firstStep) {
            response = new DebugResponseBorderFirst(variables);
        } else {
            response = new DebugResponseStandard(variables);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/debug/previous")
    public ResponseEntity<ApiResponse> debugPrevious() {
        Map<Integer, Map<Integer, Integer>> variables = Debugger.previousStep();
        ApiResponse response;
        if (Debugger.lastStep) {
            response = new DebugResponseBorderLast(variables);
        } else if (Debugger.firstStep) {
            response = new DebugResponseBorderFirst(variables);
        } else {
            response = new DebugResponseStandard(variables);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Runs the code and returns the environment.
     * Execute the code based on the syntax detected and
     * return the environment with the variables.
     * If there are any errors, return an error response.
     *
     * @param source the source code to run
     * @return the environment with the variables
     */
    private static ApiResponse runCode(String source, Map<String, Integer> inputVariables, boolean debugMode) {
        Objects.requireNonNull(source, "Source code must not be null");
        Syntax syntax = SyntaxDetector.detectSyntax(source);
        Environment environment = new Environment(debugMode);
        List<String> errors = new ArrayList<>();

        if (inputVariables != null) {
            environment = new Environment(inputVariables, debugMode);
        }

        switch (syntax) {
            case LW -> {
                interpreter = new LWInterpreter(environment);
                return run(errors, new LWScanner(source), new LWParser(), environment);
            }
            case GOTO -> {
                interpreter = new GOTOInterpreter(environment);
                return run(errors, new GOTOScanner(source), new GOTOParser(), environment);
            }
            case MIXED -> {
                errors.add("The code contains both LW and GOTO syntax.\nPlease choose only one syntax at a time.");
                return new ErrorResponse(errors);
            }
            default -> {
                errors.add("The code does not contain any valid syntax.");
                return new ErrorResponse(errors);
            }
        }
    }

    private static ApiResponse run(List<String> errors, Scanner scanner, Parser parser, Environment environment) {
        List<Token> tokens = scanner.scanProgram();
        if (ErrorHandler.hadError()) return craftErrorResponse(errors,
                "There were scanning errors in your code.");

        List<Statement> statements = parser.parse(tokens);
        if(parser instanceof GOTOParser gotoParser) {
            Map<Integer, Integer> markerLineMap = gotoParser.getMarkerLineMap();
            ((GOTOInterpreter) interpreter).setMarkerLineMap(markerLineMap);
        }
        if (ErrorHandler.hadError()) return craftErrorResponse(errors,
                "There were syntax errors in your code.");

        interpreter.interpretAsync(statements).join();
        if (ErrorHandler.hadError()) return craftErrorResponse(errors,
                "There were runtime errors in your code.");

        return new SuccessResponse(environment.getVariables());
    }

    private static ErrorResponse craftErrorResponse(List<String> errors, String message) {
        errors.add(message);
        errors.addAll(ErrorHandler.getErrors().stream()
                .map(Object::toString)
                .toList());
        return new ErrorResponse(errors);
    }

    /**
     * The code request class used to map the incoming JSON request.
     */
    public static class CodeRequest {
        private String code;
        private Map<String, Integer> variables;

        /**
         * Gets the code from the request.
         *
         * @return the code
         */
        public String getCode() {
            return code;
        }

        /**
         * Sets the code for the request.
         *
         * @param code the code to set
         */
        public void setCode(String code) {
            this.code = code;
        }

        /**
         * Gets the variables from the request.
         *
         * @return the variables
         */
        public Map<String, Integer> getVariables() {
            return variables;
        }

        /**
         * Sets the variables for the request.
         *
         * @param variables the variables to set
         */
        public void setVariables(Map<String, Integer> variables) {
            this.variables = variables;
        }
    }

    /**
     * The response class used to map the outgoing JSON response.
     */
    public sealed interface ApiResponse permits DebugResponseBorderFirst, DebugResponseBorderLast, DebugResponseStandard, ErrorResponse, SuccessResponse {
    }

    /**
     * The success response class used to map the outgoing JSON response.
     */
    public record SuccessResponse(Map<Integer, Integer> variables) implements ApiResponse {
        public String getType() {
            return TYPE_SUCCESS;
        }
    }

    /**
     * The error response class used to map the outgoing JSON response.
     */
    public record ErrorResponse(List<String> errors) implements ApiResponse {
        public String getType() {
            return TYPE_ERROR;
        }
    }

    public record DebugResponseStandard(Map<Integer, Map<Integer, Integer>> variables) implements ApiResponse {
        public String getType() {
            return TYPE_SUCCESS;
        }
    }

    public record DebugResponseBorderLast(Map<Integer, Map<Integer, Integer>> variables) implements ApiResponse {
        public String getType() {
            return TYPE_SUCCESS_DEBUG_LAST_ELEMENT;
        }
    }

    public record DebugResponseBorderFirst(Map<Integer, Map<Integer, Integer>> variables) implements ApiResponse {
        public String getType() {
            return TYPE_SUCCESS_DEBUG_FIRST_ELEMENT;
        }
    }
}
