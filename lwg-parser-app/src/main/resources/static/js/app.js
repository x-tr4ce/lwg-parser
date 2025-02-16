// Imports
import { setupTabs, setupTabEvents } from './tabs.js';
import { setupVariableManagement } from './variables.js';
import { setupEventListeners } from './events.js';
import { initializeCodeMirror } from './bundled/editor.js';
import { preventFromSubmit } from './variables.js';

// Global variables
export const allProjects = {};
export let codeEditor;

// Initialization
document.addEventListener('DOMContentLoaded', () => {

    const containerID = 'code-editor';
    const editorContainer = document.getElementById(containerID);

    if (editorContainer) {
        codeEditor = initializeCodeMirror(containerID);
    } else {
        console.error(`Element with ID "${containerID}" not found.`);
    }


    setupTabs();
    setupTabEvents();
    setupVariableManagement();
    setupEventListeners();
    preventFromSubmit();
});