package ch.zhaw.lwgparserapp.interpreter;

import ch.zhaw.lwgparserapp.debugger.Debugger;
import ch.zhaw.lwgparserapp.error.ErrorHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * The environment class is used to store variables and their values.
 */
public class Environment {
    private final Map<Integer, Integer> variables;
    private final boolean debugMode;

    /**
     * Creates a new environment with an empty variables map
     * and debug mode disabled.
     */
    public Environment() {
        this(false);
    }

    /**
     * Creates a new environment with an empty variables map
     * and the specified debug mode.
     *
     * @param debugMode the debug mode to use
     */
    public Environment(boolean debugMode) {
        this.debugMode = debugMode;
        variables = new HashMap<>();
        variables.put(0, 0); // Initialize x0 with a default value of 0
    }

    /**
     * Creates a new environment with the specified variables map
     * and the specified debug mode.
     *
     * @param variables the variables map to use
     * @param debugMode the debug mode to use
     */
    public Environment(Map<String, Integer> variables, boolean debugMode) {
        this.debugMode = debugMode;
        this.variables = new HashMap<>();
        setVariables(variables);
    }

    /**
     * A helper method to initialize variables
     *
     * @param variables the variables to initialize
     */
    public void initVariablesIfAbsent(int... variables) {
        initVariableIfAbsent(0); // Ensure x0 is always initialized
        for (int variable : variables) {
            initVariableIfAbsent(variable);
        }
    }

    /**
     * Initializes a variable <br>
     * <p>
     * If the variable identifier is not found, it is initialized to 0.
     *
     * @param variable the variable identifier to initialize or get
     */
    private void initVariableIfAbsent(int variable) {
        if (variables.get(variable) == null) {
            setVariable(variable, 0);
        }
    }

    /**
     * Sets a variable to a specific value <br>
     * <p>
     * If the value is negative, an error is reported.
     *
     * @param variable the variable identifier
     * @param value    the value to set the variable to
     */
    public void setVariable(int line, int variable, int value) {
        if (value < 0) {
            ErrorHandler.report(0, "Variable value cannot be negative");
        }
        variables.put(variable, value);
        addDebugVariablesInDebugMode(line);
    }


    /**
     * Sets a variable to a specific value <br>
     * <p>
     * If the value is negative, an error is reported.
     *
     * @param variable the variable identifier
     * @param value    the value to set the variable to
     */
    private void setVariable(int variable, int value) {
        if (value < 0) {
            ErrorHandler.report(0, "Variable value cannot be negative");
        }
        variables.put(variable, value);
    }


    /**
     * Gets the value of a variable <br>
     * <p>
     * If the variable is not found, an error is reported.
     *
     * @param variable the variable identifier
     * @return the value of the variable
     */
    public int getVariable(int variable) {
        initVariableIfAbsent(variable);
        return variables.get(variable);
    }

    /**
     * Gets the variables map
     *
     * @return the variables map
     */
    public Map<Integer, Integer> getVariables() {
        return variables;
    }

    private void setVariables(Map<String, Integer> variables) {
        for (Map.Entry<String, Integer> entry : variables.entrySet()) {
            try {
                int key = Integer.parseInt(entry.getKey().substring(1));
                setVariable(0, key, entry.getValue());
            } catch (NumberFormatException e) {
                ErrorHandler.report(0, "Invalid variable key format: " + entry.getKey());
            }
        }
    }

    public void addDebugVariablesInDebugMode(int line) {
        initVariableIfAbsent(0); // Ensure x0 exists before adding to debug
        if (debugMode) {
            Debugger.addDebugVariable(line, new HashMap<>(variables));
        }
    }
}
