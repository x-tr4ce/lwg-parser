import {allProjects} from "./app.js";
import {activeTab} from "./tabs.js";
import {renderVariableFields} from "./dom.js";

/**
 * Set up event listeners for the variable management section
 */
function setupVariableManagement() {
    document.getElementById('add-variable').addEventListener('click', addVariable);
}

/**
 * Add a new variable field to the project data and re-render the variable fields
 * new field gets focus after being added
 */
function addVariable() {
    const project = allProjects[activeTab];

    const existingKeys = project.variables.map(variable => variable.key);

    let newIndex = 0;
    while (existingKeys.includes(`X${newIndex}`)) {
        newIndex++;
    }
    const newKey = `X${newIndex}`;

    project.variables.push({key: newKey, value: ''});

    renderVariableFields();

    // Focus on the newly added field
    setTimeout(() => {
        const newField = document.getElementById(newKey);
        if (newField) {
            newField.focus();
        }
    }, 0);

    // Scroll the container to the bottom, including the button
    const scrollableVariables = document.querySelector('.scrollable-variables');
    scrollableVariables.scrollTop = scrollableVariables.scrollHeight;
}

/**
 * Delete the variable field associated with the delete button that was clicked
 * from the project data and re-render the variable fields
 *
 * @param event
 */
function deleteVariableField(event) {
    const button = event.target.closest('.delete-variable');

    if (!button) return;

    const variableName = button.dataset.name;

    allProjects[activeTab].variables = allProjects[activeTab].variables.filter(
        variable => variable.key !== variableName
    );


    renderVariableFields();
}

/**
 * Prevent the page from reloading when enter is pressed in the variable form
 */
function preventFromSubmit() {
    const variableForm = document.getElementById('variable-form');
    if (variableForm) {
        variableForm.addEventListener('submit', (e) => {
            e.preventDefault();
        });
    }
}

/**
 * Replace the label with an input field for renaming the variable
 * and add event listeners to handle renaming
 *
 * @param oldKey the original key of the variable
 * @param parentDiv the parent div of the variable field
 */
function renameVariable(oldKey, parentDiv) {
    const label = parentDiv.querySelector('.variable-label');

    // Replace label with input field for renaming
    const renameInput = document.createElement('input');
    renameInput.type = 'text';
    renameInput.value = oldKey;
    renameInput.className = 'rename-input';

    // Match the input width to the label's size
    renameInput.style.width = `${label.offsetWidth}px`;

    label.replaceWith(renameInput);
    renameInput.focus();

    // Ensure 'X' can not be deleted and only numbers follow it with no leading zeros
    renameInput.addEventListener('input', () => {
        if (!renameInput.value.startsWith('X')) {
            renameInput.value = `X${renameInput.value.replace(/[^0-9]/g, '')}`;
        } else {

            let numericPart = renameInput.value.slice(1).replace(/[^0-9]/g, '');

            // cut out leading zeros
            if (numericPart >= 1) {
                numericPart = numericPart.replace(/^0+/, '');
            } else if (numericPart.length > 1) {
                numericPart = '0';
            }

            renameInput.value = `X${numericPart}`;
        }

        if (renameInput.value.length > 4) {
            renameInput.value = renameInput.value.slice(0, 4);
        }
    });

    // Handle renaming on blur or Enter key
    renameInput.addEventListener('blur', () => completeRename(oldKey, renameInput.value));
    renameInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            renameInput.blur();
        }
    });
}

/**
 * Complete the renaming process by validating the new key
 * and updating the key in the project data. Re-render the variable fields.
 *
 * @param oldKey the original key of the variable
 * @param newKey the new key of the variable
 */
function completeRename(oldKey, newKey) {
    const project = allProjects[activeTab];

    if (oldKey === newKey) {
        renderVariableFields();
        return;
    }

    // Validation: Must match "X" followed by a number and be unique
    const isValid = /^X\d+$/.test(newKey) && !project.variables.some(v => v.key === newKey);

    if (!isValid) {
        alert("Invalid or duplicate variable name. Reverting to original name.");
        renderVariableFields();
        return;
    }

    // Update the variable name
    const variable = project.variables.find(v => v.key === oldKey);
    if (variable) {
        variable.key = newKey;
    }

    renderVariableFields();
}


/**
 * Save the current values in the variable fields to the project data for the active tab
 */
function saveCurrentVariableValues() {
    const variableForm = document.getElementById('variable-form');
    const project = allProjects[activeTab];

    variableForm.querySelectorAll('input').forEach(input => {
        const variable = project.variables.find(v => v.key === input.name);
        if (variable) {
            variable.value = input.value;
        }
    });
}

export { setupVariableManagement, deleteVariableField, renameVariable, completeRename, saveCurrentVariableValues, preventFromSubmit };

