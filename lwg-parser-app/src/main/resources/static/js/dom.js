import {deleteVariableField, renameVariable, saveCurrentVariableValues} from "./variables.js";
import {allProjects} from "./app.js";
import {activeTab, closeTab, switchTab} from "./tabs.js";

// constants
const closeIconHTML = '<img src="/assets/close_icon.svg" alt="Close icon" class="close-icon">';

const sections = {
    parser: document.getElementById('parser-section'),
    documentation: document.getElementById('documentation-section'),
    tutorial: document.getElementById('tutorial-section')
};

/**
 * Show the specified section and hide the others
 *
 * @param section
 */
function showSection(section) {
    Object.keys(sections).forEach(key => {
        if (key === section) {
            sections[key].classList.remove('hidden');
            sections[key].classList.add('visible');
        } else {
            sections[key].classList.remove('visible');
            sections[key].classList.add('hidden');
        }
    });
}


/**
 * Render the variable fields based on the data in the projectData object
 * Clear the existing fields, generate new fields for each variable
 * and add event listeners to the delete buttons
 *
 */
function renderVariableFields() {

    saveCurrentVariableValues();

    const variableForm = document.getElementById('variable-form');
    const project = allProjects[activeTab];

    project.variables.sort((a, b) => {
        const indexA = parseInt(a.key.substring(1), 10);
        const indexB = parseInt(b.key.substring(1), 10);
        return indexA - indexB;
    });

    variableForm.innerHTML = '';

    project.variables.forEach(({key, value}) => {
        const fieldDiv = document.createElement('div');

        // Add label with double-click functionality
        const label = document.createElement('span');
        label.textContent = `${key}:`;
        label.className = 'variable-label';
        label.addEventListener('dblclick', () => renameVariable(key, fieldDiv));

        // Add input field with input validation
        const input = document.createElement('input');
        input.type = 'number';
        input.id = key;
        input.name = key;
        input.value = value;
        input.placeholder = `value for ${key}`;
        input.oninput = () => {
            let value = input.value.replace(/[^0-9.]/g, ''); // Allow only numbers and '.'

            // Remove leading zeros unless the value is exactly '0'
            if (value.length > 1 && value[0] === '0' && value[1] !== '.') {
                value = value.replace(/^0+/, '');
            }

            // Backend safety check: Java integer max value is 2147483647
            const maxJavaInteger = 2147483647;
            if (value && Number(value) > maxJavaInteger) {
                value = maxJavaInteger.toString();
            }

            input.value = value || '0';
        };


        // Add delete button with click functionality
        const deleteButton = document.createElement('button');
        deleteButton.type = 'button';
        deleteButton.className = 'delete-variable';
        deleteButton.dataset.name = key;
        deleteButton.innerHTML = closeIconHTML;
        deleteButton.addEventListener('click', deleteVariableField);

        // Append elements to the div
        fieldDiv.appendChild(label);
        fieldDiv.appendChild(input);
        fieldDiv.appendChild(deleteButton);

        variableForm.appendChild(fieldDiv);

    });
}

/**
 * Render the tabs based on the data in the projectData object
 * Clear the existing tabs, generate new tabs for each project
 */
function renderTabs() {
    const tabContainer = document.querySelector('.tab-container');
    tabContainer.innerHTML = '';

    Object.keys(allProjects).forEach((projectId) => {
        const tab = document.createElement('div');
        tab.classList.add('tab');
        tab.dataset.tabId = projectId;
        tab.innerHTML = `
            Project ${projectId}
            <button class="close-tab">
                <svg class="close-icon" viewBox="0 0 24 24">
                    <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
            </button>
        `;
        tab.addEventListener('click', () => switchTab(projectId));
        tabContainer.appendChild(tab);

        // Add event listener for the close button
        const closeButton = tab.querySelector('.close-tab');
        closeButton.addEventListener('click', (event) => closeTab(event, projectId));
    });
}

export { showSection, renderVariableFields, renderTabs };