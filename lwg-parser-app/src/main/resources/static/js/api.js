import {saveCurrentVariableValues} from "./variables.js";
import {allProjects, codeEditor} from "./app.js";
import {activeTab} from "./tabs.js";
import {getDebugVariables, stopDebuggingProcess, initDebuggingProcess, getDebugLine} from "./debugger.js";

/**
 * Submit a code execution request to the api endpoint `/api/process`.
 * The request will send the code input and the current variable values
 * to the server for processing. The server will execute the code, update the variables
 * and display the output in the output section
 * If Code segment is empty, an error message will be displayed.
 *
 * @returns {Promise<void>}
 */
async function submitCodeExecutionRequest() {
    const runButton = document.getElementById('run-button');
    const debugButton = document.getElementById('debug-button');
    runButton.disabled = true;
    debugButton.disabled = true;
    codeEditor.setEditable(false);

    try {
        if (codeEditor.getText().length === 0) {
            document.querySelector('.output-section').innerHTML = '<p class="error">No code to execute.</p>';
            return;
        }
        const startingMessage = 'Success! Variables:<br>';
        const response = await submitPostRequest('/api/process');
        await handleResponse(response.type, response.variables, response.errors, startingMessage);
    } catch (error) {
        console.error('Error initiating code execution request', error);
    } finally {
        runButton.disabled = false;
        debugButton.disabled = false;
        codeEditor.setEditable(true);
    }
}

/**
 * Submit a debugging request to the api endpoint `/api/debug`.
 * The request will send the code input and the current variable values
 * to the server for debugging. The server will execute the code in debug mode, update the variables
 * and initialize the debugging process.
 * If Code segment is empty, an error message will be displayed.
 *
 * @returns {Promise<void>}
 */
async function submitDebugRequest() {
    const runButton = document.getElementById('run-button');
    const debugButton = document.getElementById('debug-button');
    runButton.disabled = true;
    debugButton.disabled = true;
    codeEditor.setEditable(false);

    try {
        if (codeEditor.getText().length === 0) {
            document.querySelector('.output-section').innerHTML = '<h2>Code Output</h2><p class="error">No code to execute.</p>';
            return;
        }
        const response = await submitPostRequest('/api/debug');
        switch (response.type) {
            case 'success': {
                await initDebuggingProcess();
                await getNextDebugStep();
                break;
            }
            case 'error':
                await handleFailedCodeExecution(response.errors, outputField);
                break;
            default:
                outputField.innerHTML = '<h2>Code Output</h2><p class="error">An unknown error occurred.</p>';
        }
    } catch (error) {
        console.error('Error initiating debugging process:', error);
    } finally {
        runButton.disabled = false;
        debugButton.disabled = false;
        codeEditor.setEditable(true);
    }
}

/**
 * Retrieve the next variables for the current debug step from the server.
 * The server will return the variables for the next debug step.
 * The variables will be updated in the UI.
 *
 * @returns {Promise<void>}
 */
async function getNextDebugStep() {
    try {
        const startingMessage = 'Debugging:';
        const response = await submitGetRequest('/api/debug/next');
        const debugVariables = await getDebugVariables(response.variables);
        await handleResponse(response.type, debugVariables, response.errors, startingMessage);
        // debug: line-numbers
        const debugLine = await getDebugLine(response.variables);
        codeEditor.setSelectedLine(debugLine);
    } catch (error) {
        console.error('Error getting next debug step:', error);
    }
}

/**
 * Retrieve the previous variables for the current debug step from the server.
 * The server will return the variables for the previous debug step.
 * The variables will be updated in the UI.
 *
 * @returns {Promise<void>}
 */
async function getPreviousDebugStep() {
    try {
        const startingMessage = 'Debugging:';
        const response = await submitGetRequest('/api/debug/previous');
        const debugVariables = await getDebugVariables(response.variables);
        await handleResponse(response.type, debugVariables, response.errors, startingMessage);
        const debugLine = await getDebugLine(response.variables);
        codeEditor.setSelectedLine(debugLine);
    } catch (error) {
        console.error('Error getting previous debug step:', error);
    }
}

/**
 * Send a request to the backend to stop the currently running project
 */
function stopProject() {
    fetch('/api/stop', {
        method: 'POST'
    }).then(response => {
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
    }).catch(error => {
        console.error('Error communicating with backend:', error);
    });
}

/**
 * Stop the debugging process and swap the buttons back to the code execution buttons
 * and set the event listeners for the code execution buttons.
 *
 * @returns {Promise<void>}
 */
async function stopDebugging() {
    stopDebuggingProcess();
    codeEditor.setEditable(true);
}

/**
 * Handle the response from the server after a code execution request or debug steps.
 * The type will contain the type of response (success or error),
 * the variables after execution, and any errors that occurred during execution.
 * The output field will be updated with the response data.
 * Also, the next and previous buttons will be enabled or disabled based on the response type if able.
 *
 * @param type the type of response (success or error)
 * @param variables the variables after execution
 * @param errors any errors that occurred during execution
 * @param startingMessage the starting message for the output field
 * @returns {Promise<void>}
 */
async function handleResponse(type, variables, errors, startingMessage) {
    const outputField = document.querySelector('.output-section');
    const nextButton = document.getElementById('next-button');
    const prevButton = document.getElementById('prev-button');

    switch (type) {
        case 'success':
            if (nextButton && prevButton) {
                nextButton.disabled = false;
                prevButton.disabled = false;
            }
            await handleSuccessfulCodeExecution(variables, outputField, startingMessage);
            break;
        case 'border_last': {
            nextButton.disabled = true;
            prevButton.disabled = false;
            await handleSuccessfulCodeExecution(variables, outputField, startingMessage);
            break;
        }
        case 'border_first': {
            prevButton.disabled = true;
            nextButton.disabled = false;
            await handleSuccessfulCodeExecution(variables, outputField, startingMessage);
            break;
        }
        case 'error':
            await handleFailedCodeExecution(errors, outputField);
            break;
        default:
            outputField.innerHTML = '<h2>Code Output</h2><p class="error">An unknown error occurred.</p>';
    }
}

/**
 * Submit a GET request to the specified api endpoint.
 * The response will be returned as a JSON object.
 * If the response is not successful, an error will be thrown.
 *
 * @param apiEndpoint the api endpoint to send the GET request to
 * @returns {Promise<any>} the response as a JSON object
 */
async function submitGetRequest(apiEndpoint) {
    const response = await fetch(apiEndpoint);

    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    return await response.json();
}

/**
 * Show the loading indicator.
 */
function showLoadingIndicator() {
    document.getElementById('loading-indicator').classList.remove('hidden');
}

/**
 * Hide the loading indicator.
 */
function hideLoadingIndicator() {
    document.getElementById('loading-indicator').classList.add('hidden');
}

/**
 * Submit a POST request to the specified api endpoint.
 * The response will be returned as a JSON object.
 * If the response is not successful, an error will be thrown.
 * The request will send the code input and the current variable values
 * to the server for processing.
 *
 * @param apiEndpoint the api endpoint to send the POST request to
 * @returns {Promise<any>} the response as a JSON object
 */
async function submitPostRequest(apiEndpoint) {
    saveCurrentVariableValues();
    const codeInput = codeEditor.getText();

    const variables = {};
    allProjects[activeTab].variables.forEach(variable => {
        variables[variable.key] = Number(variable.value) || 0;
    });

    showLoadingIndicator(); // Show loading indicator

    try {
        const response = await fetch(apiEndpoint, {
            method: 'POST', headers: {
                'Content-Type': 'application/json',
            }, body: JSON.stringify({code: codeInput, variables: variables})
        });

        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        return await response.json();
    } finally {
        hideLoadingIndicator(); // Hide loading indicator
    }
}

/**
 * Handle the output for a successful code execution.
 * The variables will be formatted and displayed in the output field.
 *
 * @param variables the variables after execution
 * @param outputField the output field to display the variables
 * @param startingMessage the starting message for the output field
 * @returns {Promise<void>}
 */
async function handleSuccessfulCodeExecution(variables, outputField, startingMessage) {
    let formattedOutput = startingMessage;

    for (const [key, value] of Object.entries(variables)) {
        formattedOutput += `<p>x${key}: ${value}</p>`;
    }

    await handleResponseOutput(formattedOutput, outputField);
}

/**
 * Handle the output for a failed code execution.
 * The errors will be formatted and displayed in the output field.
 * The output field will be updated with the response data.
 *
 * @param errors any errors that occurred during execution
 * @param outputField the output field to display the errors
 * @returns {Promise<void>}
 */
async function handleFailedCodeExecution(errors, outputField) {
    let formattedOutput = '';

    for (const error of errors) {
        formattedOutput += `<p>${error}</p>`;
    }

    await handleResponseOutput(formattedOutput, outputField);
}

/**
 * Handle the output for a code execution response.
 * The output will be displayed in the output field.
 *
 * @param output the output to display
 * @param outputField the output field to display the output
 * @returns {Promise<void>}
 */
async function handleResponseOutput(output, outputField) {
    outputField.innerHTML = '<h2>Code Output</h2>' + output;
    if (allProjects[activeTab]) allProjects[activeTab].output = output;
}

export {
    submitCodeExecutionRequest, submitDebugRequest, stopProject, getNextDebugStep,
    getPreviousDebugStep, stopDebugging
};