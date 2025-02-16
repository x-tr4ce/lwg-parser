package ch.zhaw.lwgparserapp.debugger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The Debugger class is used to store and retrieve debug variables for the code execution.
 * It provides methods to add, clear, and retrieve debug variables for each line of code.
 * The debugger can step through the code execution to view the variables at each step.
 * The debugger is used to assist in debugging the code execution process.
 */
public final class Debugger {
    private static final List<Map<Integer, Map<Integer, Integer>>> debugVariables = new ArrayList<>();
    private static int currentStep = -1;
    public static boolean firstStep = true;
    public static boolean lastStep = false;

    // Private constructor to prevent instantiation
    private Debugger() {
    }

    /**
     * Adds the debug variables for a specific line of code.
     *
     * @param line      the line number of the code
     * @param variables the variables for the line of code
     */
    public static void addDebugVariable(int line, Map<Integer, Integer> variables) {
        debugVariables.add(Map.of(line, variables));
    }

    /**
     * Clears all debug variables stored in the debugger and resets the current step to 0.
     */
    public static void reset() {
        debugVariables.clear();
        currentStep = -1;
        firstStep = true;
        lastStep = false;
    }

    /**
     * Returns the next step of debug variables.
     * If the current step is at the end, it returns the last step of debug variables.
     * If there are no debug variables, it returns an empty map.
     * Otherwise, it returns the next step of debug variables.
     *
     * @return the next step of debug variables
     */
    public static Map<Integer, Map<Integer, Integer>> nextStep() {
        if (debugVariables.isEmpty()) return Collections.emptyMap();

        if (currentStep == debugVariables.size() - 1) {
            lastStep = true;
        } else {
            currentStep++;
            firstStep = false;
        }
        return debugVariables.get(currentStep);
    }

    /**
     * Returns the previous step of debug variables.
     * If the current step is at the beginning, it returns the first step of debug variables.
     * If there are no debug variables, it returns an empty map.
     * Otherwise, it returns the previous step of debug variables.
     *
     * @return the previous step of debug variables
     */
    public static Map<Integer, Map<Integer, Integer>> previousStep() {
        if (debugVariables.isEmpty()) return Collections.emptyMap();
        if (currentStep == 0) {
            firstStep = true;
        } else{
            currentStep--;
            lastStep = false;
        }
        return debugVariables.get(currentStep);
    }
}
