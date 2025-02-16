// Imports
import {showSection} from './dom.js';
import {
    submitCodeExecutionRequest,
    submitDebugRequest,
    stopProject,
    getNextDebugStep,
    getPreviousDebugStep,
    stopDebugging
} from './api.js';

let keyEvent;

/**
 * Set up event listeners for the UI
 * for tabs: add, close, switch
 * for variable management: add, delete
 * for text area: tab indent
 * for documentation: copy button
 */
function setupEventListeners() {
    setNavigationEventListeners();
    setCodeExecutionEventListeners();
    setCopyButtonEventListeners()
}

/**
 * Set up event listeners for the navigation tabs
 * for the parser, documentation, and tutorial sections.
 */
function setNavigationEventListeners() {
    document.querySelector('.parser-tab').addEventListener('click',
        () => showSection('parser'));
    document.querySelector('.documentation-tab').addEventListener('click',
        () => showSection('documentation'));
    document.querySelector('.tutorial-tab').addEventListener('click',
        () => showSection('tutorial'));
}

/**
 * Set up event listeners for the code execution buttons
 * run, debug, stop
 */
function setCodeExecutionEventListeners() {
    document.getElementById('run-button').addEventListener('click', submitCodeExecutionRequest);
    document.getElementById('debug-button').addEventListener('click', submitDebugRequest);
    document.getElementById('stop-button').addEventListener('click', stopProject);
}

/**
 * Remove the event listeners for the code execution buttons
 * run, debug, stop
 */
function removeCodeExecutionEventListeners() {
    document.getElementById('run-button').removeEventListener('click', submitCodeExecutionRequest);
    document.getElementById('debug-button').removeEventListener('click', submitDebugRequest);
    document.getElementById('stop-button').removeEventListener('click', stopProject);
}

/**
 * Set up event listeners for the debug buttons
 * previous, next, stop
 */
async function setDebugEventListeners() {
    document.getElementById('prev-button').addEventListener('click', getPreviousDebugStep);
    document.getElementById('next-button').addEventListener('click', getNextDebugStep);
    document.getElementById('stop-button').addEventListener('click', stopDebugging);
    await setDebugKeyListeners();

}

/**
 * Remove the event listeners for the debug buttons
 * previous, next, stop
 */
async function removeDebugEventListeners() {
    document.getElementById('prev-button').removeEventListener('click', getPreviousDebugStep);
    document.getElementById('next-button').removeEventListener('click', getNextDebugStep);
    document.getElementById('stop-button').removeEventListener('click', stopDebugging);
    await removeDebugKeyListeners();
}

/**
 * Set up event listeners for previous and next buttons in debug mode.
 * The previous button will be usable via F10 and the next button via F11.
 */
async function setDebugKeyListeners() {
    keyEvent = event => {
        switch(event.key) {
            case 'F10':
                event.preventDefault();
                getPreviousDebugStep();
                break;
            case 'F11':
                event.preventDefault();
                getNextDebugStep();
                break;
        }
    }
    await document.addEventListener('keydown', await keyEvent);
}

/**
 * Remove the event listeners for the debug
 * previous and next buttons.
 *
 * @returns {Promise<void>}
 */
async function removeDebugKeyListeners() {
    if(keyEvent) await document.removeEventListener('keydown', keyEvent);
}



/**
 * Set up event listeners for copy buttons
 */
function setCopyButtonEventListeners() {
    document.body.addEventListener('click', (event) => {
        if (event.target.closest('.copy-button')) {
            const button = event.target.closest('.copy-button');
            copyToClipboard(button);
        }
    });
}

/**
 * Copy the text content of a <pre> element inside the same container as the button
 */
function copyToClipboard(button) {
    const preElement = button.parentElement.querySelector('pre');
    if (!preElement) return; // Safeguard if no <pre> exists

    navigator.clipboard.writeText(preElement.textContent)
        .then(() => {
            const originalContent = button.innerHTML;
            button.innerHTML = 'Copied!';
            setTimeout(() => {
                button.innerHTML = originalContent;
            }, 2000); // Reset button text after 2 seconds
        })
        .catch((err) => {
            console.error('Failed to copy text: ', err);
        });
}

/**
 * Show loading dots in the loading message
 */
document.addEventListener('DOMContentLoaded', () => {
    const loadingDots = document.getElementById('loading-dots');
    let dotCount = 0;

    setInterval(() => {
        dotCount = (dotCount + 1) % 4;
        loadingDots.textContent = '.'.repeat(dotCount);
    }, 500);
});


export {
    setupEventListeners, setDebugEventListeners, removeCodeExecutionEventListeners,
    removeDebugEventListeners, setCodeExecutionEventListeners, copyToClipboard
};