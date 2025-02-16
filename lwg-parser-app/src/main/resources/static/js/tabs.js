import {allProjects, codeEditor} from "./app.js";
import {renderTabs, renderVariableFields} from "./dom.js";


// Initial project data
const emptyProjectContent = {
    variables: [],
    code: '',
    output: ''
};


let activeTab = 1;

/**
 * Set up the project tabs and add the first project
 */
function setupTabs() {
    addProject();
}

/**
 * Set up event listeners for the tab bar
 */
function setupTabEvents() {
    document.querySelector('.add-tab').addEventListener('click', addProject);
}


/**
 * Add a new project tab to the UI and switch to it
 */
function addProject() {
    let newProjectID = 0;

    // Find the first available project ID
    for (let i = 1; i <= Object.keys(allProjects).length + 1; i++) {
        if (!allProjects[i]) {
            newProjectID = i;
            break;
        }
    }
    newProjectID = newProjectID === 0 ? Object.keys(allProjects).length + 1 : newProjectID;

    allProjects[newProjectID] = JSON.parse(JSON.stringify(emptyProjectContent));

    renderTabs();
    switchTab(newProjectID);

    const projectBar = document.querySelector('.project-bar');
    projectBar.scrollLeft = projectBar.scrollWidth;

    // Add event listener for the close button
    const closeButton = document.querySelector(`.tab[data-tab-id="${newProjectID}"] .close-tab`);
    closeButton.addEventListener('click', (event) => closeTab(event, newProjectID));
}


/**
 * Switch the active tab to the tab with the specified ID
 * and load the data for that project into the UI
 *
 * @param tabId
 */
function switchTab(tabId) {

    if (allProjects[activeTab]) {
        saveProjectData(activeTab);
    }
    activeTab = tabId;

    // Remove 'active' class from all tabs
    document.querySelectorAll('.tab').forEach(tab => {
        tab.classList.remove('active');
    });

    document.querySelector(`.tab[data-tab-id="${tabId}"]`).classList.add('active');

    loadProjectData(activeTab);
}

/**
 * Close the tab with the specified ID and remove its data from the projectData
 * object. If the tab being closed is the active tab, switch to the first remaining tab.
 * If there are no remaining tabs, display a message indicating that no projects are open.
 *
 * @param event
 * @param tabId
 */
function closeTab(event, tabId) {
    event.stopPropagation();

    saveProjectData(tabId);

    if (allProjects[tabId].code !== "" && allProjects[tabId].output === "") {
        const confirmClose = confirm("Are you sure you want to close this project?");
        if (!confirmClose) return;
    }

    delete allProjects[tabId];
    renderTabs();

    const keys = Object.keys(allProjects).map(Number).sort((a, b) => a - b);

    // If the tab being closed is the active tab, switch to the previous tab, or the next tab if there is no previous tab
    if (Number(activeTab) === Number(tabId)) {
        if (keys.length > 0) {
            switchTab(keys[0]);
        } else {
            addProject();
        }
    }
}

/**
 * Save the data from the UI into the projectData object
 *
 * @param projectId
 */
function saveProjectData(projectId) {
    const variableForm = document.getElementById('variable-form');
    const variables = [];

    variableForm.querySelectorAll('input').forEach(input => {
        variables.push({key: input.name, value: input.value});
    });

    const code = codeEditor.getText();
    const outputField = document.querySelector('.output-section');
    const output = outputField ? outputField.innerHTML : '';

    // Save data to projectData

    allProjects[projectId] = {
        variables: variables,
        code: code,
        output: output
    };
}

/**
 * Load the data for the specified project into the UI
 *
 *
 * @param projectId
 */
function loadProjectData(projectId) {
    const data = allProjects[projectId];

    if (!data) {
        console.error(`Project data for Project${projectId} not found.`);
        return;
    }

    renderVariableFields();

    codeEditor.setText(data.code || '');

    const outputField = document.querySelector('.output-section');
    if (outputField) {
        outputField.innerHTML = data.output || '';
    }
}

export { setupTabs, setupTabEvents, addProject, switchTab, closeTab, activeTab };


