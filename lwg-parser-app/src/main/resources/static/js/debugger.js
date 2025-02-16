import {
    setDebugEventListeners, removeCodeExecutionEventListeners,
    setCodeExecutionEventListeners, removeDebugEventListeners
} from "./events.js";

/**
 * Get the variables for the current debug step
 * The variables are stored in an object where the key is the line number
 * and the value is an object containing the variables for that line.
 *
 * @param variables
 * @returns {Promise<{}>}
 */
async function getDebugVariables(variables) {
    let debugVariables = {};
    let line = 0;

    for (const lineNumber in variables) {
        const varData = variables[lineNumber];
        line = Number(lineNumber);
        debugVariables = {...debugVariables, ...varData};
    }

    return debugVariables;
}

/**
 * Get the line number of the current debug step.
 * The line number is the key of the object containing the variables for that line.
 *
 * @param variables
 * @returns {Promise<string>}
 */
async function getDebugLine(variables) {
    return Object.keys(variables)[0];
}

/**
 * Initialize the debugging process by swapping the buttons and setting the event listeners
 * for the debug buttons.
 *
 * @returns {Promise<void>}
 */
async function initDebuggingProcess() {
    removeCodeExecutionEventListeners();
    swapToDebugButtons();
    await setDebugEventListeners();
}

/**
 * Stop the debugging process and swap the buttons back to the code execution buttons
 * and set the event listeners for the code execution buttons.
 *
 * @returns {void}
 */
function stopDebuggingProcess() {
    removeDebugEventListeners().then(() => {
        swapToCodeExecutionButtons();
        setCodeExecutionEventListeners();
    });
}

/**
 * Swap the buttons in the UI to the debug buttons
 * The swap logic is as follows:
 * Run -> Prev
 * Debug -> Stop
 * Stop -> Next
 *
 * @returns {void}
 */
function swapToDebugButtons() {
    const runButton = document.getElementById('run-button');
    const stopButton = document.getElementById('stop-button');
    const debugButton = document.getElementById('debug-button');

    if (!runButton || !stopButton || !debugButton) {
        console.error("Some buttons are missing!");
        return;
    }

    runButton.id = 'prev-button';
    runButton.innerHTML = `
    Prev
    <svg class="button-icon" height="32" viewBox="0 0 32 32" width="32" xmlns="http://www.w3.org/2000/svg">
        <path d="M10 16l10-10v20L10 16z"
            fill="currentColor"
            stroke="currentColor"
            stroke-width="3"/>
    </svg>
    `;

    stopButton.id = 'next-button';
    stopButton.innerHTML = `
    Next
    <svg class="button-icon" height="32" viewBox="0 0 32 32" width="32" xmlns="http://www.w3.org/2000/svg">
        <path d="M22 16L12 6v20l10-10z" 
                fill="currentColor"
                stroke="currentColor"
                stroke-width="3"/>
    </svg>
    `;

    debugButton.id = 'stop-button';
    debugButton.innerHTML = `
        Stop
        <svg class="button-icon" height="32" viewBox="0 0 32 32" width="32" xmlns="http://www.w3.org/2000/svg">
            <path d="M 24 29 L 8 29 C 5.238576 29 3 26.761423 3 24 L 3 8 C 3 5.238577 
            5.238576 3 8 3 L 24 3 C 26.761423 3 29 5.238577 29 8 L 29 24 C 29 26.761423 26.761423 29 24 29 Z M 8 5 C 
            6.343146 5 5 6.343145 5 8 L 5 24 C 5 25.656855 6.343146 27 8 27 L 24 27 C 25.656855 27 27 25.656855 27 24 L
            27 8 C 27 6.343145 25.656855 5 24 5 Z"
            fill="#000000"
            stroke="currentColor"
            stroke-width="3"/>
        </svg>
    `;
}

/**
 * Swap the buttons in the UI to the code execution buttons.
 * The swap logic is as follows:
 * Prev -> Run
 * Stop -> Debug
 * Next -> Stop
 *
 * @returns {void}
 */
function swapToCodeExecutionButtons() {
    const prevButton = document.getElementById('prev-button');
    const nextButton = document.getElementById('next-button');
    const stopButton = document.getElementById('stop-button');

    nextButton.disabled = false;
    prevButton.disabled = false;

    if (!prevButton || !nextButton || !stopButton) {
        console.error("Debug buttons are missing!");
        return;
    }

    prevButton.id = 'run-button';
    prevButton.innerHTML = `
        Run
        <svg class="button-icon" height="60" viewBox="0 0 60 60" width="60"
            xmlns="http://www.w3.org/2000/svg">
            <path d="M 17.52 53 L 16.184517 53 C 13.876852 52.991859 12.00814 51.123146 12 48.815483 L 12 11.184517 C 
            12.00814 8.876854 13.876852 7.008141 16.184517 7 L 17.52 7 C 19.006285 7.2882 20.371349 8.017269 21.437418 
            9.092258 L 45.461288 26.097416 C 47.616119 28.252966 47.616119 31.747034 45.461288 33.902576 L 45.283226 
            34.065804 L 21.437418 50.907742 C 20.371349 51.982731 19.006285 52.7118 17.52 53 Z M 17.430967 9.967743 L 
            16.184517 9.967743 C 15.513463 9.983677 14.975544 10.528156 14.967742 11.199356 L 14.967742 48.815483 C 
            14.967742 49.487488 15.51251 50.032257 16.184517 50.032257 L 17.52 50.032257 C 18.215706 49.683521 18.855799
            49.233456 19.419355 48.696774 L 19.671614 48.548386 L 43.413548 31.750969 C 44.301926 30.781073 44.301926 
            29.293125 43.413548 28.323225 L 19.419355 11.288387 C 18.832182 10.743736 18.160698 10.297756 17.430967 
            9.967743 Z"
            id="Pfad"
            stroke="currentColor"
            stroke-width="3.709677"/>
        </svg>
    `;
    prevButton.style.backgroundColor = '';
    prevButton.style.color = '';

    stopButton.id = 'debug-button';
    stopButton.innerHTML = `
        Debug
        <svg class="button-icon" height="32" viewBox="0 0 32 32" width="32"
            xmlns="http://www.w3.org/2000/svg">
            <path d="M 29.83 20 L 30.17 18 L 25 17.15 L 25 13 C 25 12.92 25 12.85 25 12.77 L 30.059999 11.41 L 29.549999
             9.48 L 24.719999 10.77 C 24.054602 8.254856 22.333361 6.150711 20 5 L 20 2 L 18 2 L 18 4.23 C 16.684303 
             3.923309 15.315698 3.923309 14 4.23 L 14 2 L 12 2 L 12 5 C 9.65927 6.163904 7.940258 8.288033 7.29 10.82 L 
             2.46 9.48 L 2 11.41 L 7 12.77 C 7 12.85 7 12.92 7 13 L 7 17.15 L 1.84 18 L 2.16 20 L 7 19.18 C 7.022844 
             20.413984 7.302091 21.629728 7.82 22.75 L 3.29 27.290001 L 4.71 28.709999 L 8.9 24.51 C 10.605294 26.699112
             13.22507 27.979177 16 27.979177 C 18.774929 27.979177 21.394707 26.699112 23.1 24.51 L 27.290001 28.709999
             L 28.709999 27.290001 L 24.17 22.75 C 24.691359 21.630474 24.974018 20.414698 25 19.18 Z M 15 25.92 C 
             11.559073 25.423347 9.004074 22.476582 9 19 L 9 13 L 15 13 Z M 9.29 11 C 10.172473 8.030342 12.901998 
             5.993965 16 5.993965 C 19.098003 5.993965 21.827526 8.030342 22.709999 11 Z M 23 19 C 22.995926 22.476582 
             20.440928 25.423347 17 25.92 L 17 13 L 23 13 Z"
              fill="currentColor"
              id="Pfad"
              stroke="currentColor"
              stroke-width="1.2"/>
              <path d="M 0 0 L 32 0 L 32 32 L 0 32 Z" fill="none" id="path1" stroke="none"/>
        </svg>
    `;
    stopButton.style.backgroundColor = '';
    stopButton.style.color = '';

    nextButton.id = 'stop-button';
    nextButton.innerHTML = `
    Stop
    <svg class="button-icon" height="32" viewBox="0 0 32 32" width="32"
        xmlns="http://www.w3.org/2000/svg">
        <path d="M 24 29 L 8 29 C 5.238576 29 3 26.761423 3 24 L 3 8 C 3 5.238577 5.238576 3 8 3 L 24 3 C 26.761423 3 
        29 5.238577 29 8 L 29 24 C 29 26.761423 26.761423 29 24 29 Z M 8 5 C 6.343146 5 5 6.343145 5 8 L 5 24 C 5 
        25.656855 6.343146 27 8 27 L 24 27 C 25.656855 27 27 25.656855 27 24 L 27 8 C 27 6.343145 25.656855 5 24 5 Z"
        fill="#000000"
        id="Pfad"
        stroke="currentColor"
        stroke-width="3"/>
    </svg>
    `;
    nextButton.style.backgroundColor = '';
    nextButton.style.color = '';
}

export {
    getDebugVariables, getDebugLine, initDebuggingProcess, swapToCodeExecutionButtons,
    stopDebuggingProcess
};