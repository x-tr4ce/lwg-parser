import {basicSetup, EditorView} from 'codemirror'
import {Compartment, EditorState} from '@codemirror/state'
import {Decoration, drawSelection, keymap, MatchDecorator, placeholder, ViewPlugin} from '@codemirror/view'
import {indentWithTab} from '@codemirror/commands'
import {indentUnit} from "@codemirror/language";

export function initializeCodeMirror(containerId) {

    // keywords categories
    const keywords = ["Loop", "While", "Do", "End", "Goto", "If", "Then", "Halt"];
    const operators = ["\\+", "-", "=", ">", ";"]; // Escape "+" since it's a special character in regex
    const variableRegex = /\bx\d+\b/g;
    const constantRegex = /\b\d+\b/g;
    const markerRegex = /M\d*:?/g;
    const commentRegex = /\/\/.*/g;

    // custom syntax highlighting
    const style = document.createElement('style');
    style.textContent = `
    .cm-keyword { color: #1AD1FF; }
    .cm-operator { color: #7de8b3; }
    .cm-variable { color: #e955f6; }
    .cm-constant { color: #fc8383; }
    .cm-marker { color: #d9d98c; }
    .cm-comment { color: #757575; }
    .cm-error { text-decoration: red wavy underline; }
    `;
    document.head.appendChild(style);


    /**
     * Custom command to handle indentation on Enter key.
     */
    function customEnterCommand({ state, dispatch }) {
        const line = state.doc.lineAt(state.selection.main.head);
        const text = line.text;
        const trimmedText = text.trim();
        const position = state.selection.main.head;

        // Base change: insert a new line with the current indentation
        let newLineIndent = text.match(/^\s*/)[0];

        // 1. Indent more if the line ends with "Do"
        if (trimmedText.endsWith("Do")) {
            newLineIndent += '   '; // Add one additional level of indentation (3 spaces)
        }

        // 2. Dedent if the line doesn't end with ";" or "Do"
        if (!trimmedText.endsWith(";") && !trimmedText.endsWith("Do")) {
            newLineIndent = newLineIndent.slice(0, -3); // Remove one level of indentation (3 spaces)
        }

        // Create the transaction to insert the new line with the calculated indentation
        dispatch(state.update({
            changes: { from: position, insert: `\n${newLineIndent}` },
            selection: { anchor: position + newLineIndent.length + 1 },
        }));

        return true;
    }

    // Keymap extension to bind the Enter key to the custom command
    const customKeymap = keymap.of([
        { key: 'Enter', run: customEnterCommand },
    ]);


    /**
     * Create a MatchDecorator for a given regex pattern and class name.
     * @param {RegExp} regex - The regular expression to match.
     * @param {string} className - The CSS class to apply.
     * @returns {MatchDecorator}
     */
    function createRegexDecorator(regex, className) {
        return new MatchDecorator({
            regexp: regex,
            decoration: Decoration.mark({class: className}),
        });
    }

    /**
     * Create a ViewPlugin for a given MatchDecorator.
     * @param {MatchDecorator} decorator - The MatchDecorator instance.
     * @param {MatchDecorator} excludeDecorator - excluded decorator
     * @returns {ViewPlugin}
     */
    function createHighlightPlugin(decorator, excludeDecorator = null) {
        return ViewPlugin.fromClass(
            class {
                constructor(view) {
                    this.decorations = decorator.createDeco(view);
                }

                update(update) {
                    let baseDecorations = decorator.createDeco(update.view);

                    if (excludeDecorator) {
                        let excludeRanges = excludeDecorator.createDeco(update.view);

                        // Filter out decorations that overlap with excludeRanges
                        baseDecorations = baseDecorations.update({
                            filter: (from, to) => {
                                let exclude = false;
                                excludeRanges.between(from, to, () => {
                                    exclude = true;
                                });
                                return !exclude;
                            },
                        });
                    }

                    this.decorations = baseDecorations;
                }
            },
            {
                decorations: (v) => v.decorations,
            }
        );
    }


    // Create decorators
    const keywordDecorator = createRegexDecorator(new RegExp(`\\b(${keywords.join('|')})\\b`, 'g'), 'cm-keyword');
    const operatorDecorator = createRegexDecorator(new RegExp(`(${operators.join('|')})`, 'g'), 'cm-operator');
    const variableDecorator = createRegexDecorator(variableRegex, 'cm-variable');
    const constantDecorator = createRegexDecorator(constantRegex, 'cm-constant');
    const markerDecorator = createRegexDecorator(markerRegex, 'cm-marker');
    const commentDecorator = createRegexDecorator(commentRegex, 'cm-comment');
    const errorDecorator = new MatchDecorator({
        regexp: /\b(?!Loop|While|Do|End|Goto|If|Then|Halt|\bx\d+\b|\b\d+\b|M\d*:?)(?!.*\/\/.*)[a-zA-Z0-9_]+\b/g,
        decoration: Decoration.mark({ class: 'cm-error' }),
    });


    // Create plugins for each decorator
    const keywordPlugin = createHighlightPlugin(keywordDecorator, commentDecorator);
    const operatorPlugin = createHighlightPlugin(operatorDecorator, commentDecorator);
    const variablePlugin = createHighlightPlugin(variableDecorator, commentDecorator);
    const constantPlugin = createHighlightPlugin(constantDecorator, commentDecorator);
    const markerPlugin = createHighlightPlugin(markerDecorator, commentDecorator);
    const commentPlugin = createHighlightPlugin(commentDecorator);
    const errorPlugin = createHighlightPlugin(errorDecorator, commentDecorator);

    const editableCompartment = new Compartment;
    let editable = true;

    const editor = new EditorView({
        state: EditorState.create({
            extensions: [
                customKeymap,
                drawSelection(),
                indentUnit.of("   "),
                placeholder("Write your code here..."),
                basicSetup,
                EditorView.theme({
                    "&": {
                        height: "100%",
                        border: "3px solid #333333;",
                        borderRadius: "5px",
                    },
                    ".cm-scroller": {
                        overflow: "auto",
                    },
                    "&.cm-editor.cm-focused": {border: "3px solid #7714ff;",},

                    ".cm-gutters": {
                        color: "#f4f4f4",
                        backgroundColor: "#202020",
                    },
                    ".cm-lineNumbers": {fontSize: "1.2rem"},
                    ".cm-lineNumbers .cm-gutterElement": {
                        alignContent: "center",
                        width: "3rem"
                    },
                    ".cm-activeLineGutter": {backgroundColor: "#752DE0"},
                    ".cm-activeLine": {backgroundColor: "#752DE03B"},
                    ".cm-cursor": {borderLeft: "2px solid #f4f4f4"},
                    ".cm-content": {
                        fontSize: "1.2rem",
                    },
                    ".cm-selectionBackground": {backgroundColor: "#7714ff88 !important"},
                }),
                keymap.of([indentWithTab]),
                EditorView.lineWrapping,
                editableCompartment.of(EditorView.editable.of(true)),
                commentPlugin,
                keywordPlugin,
                operatorPlugin,
                variablePlugin,
                constantPlugin,
                markerPlugin,
                errorPlugin,
                // Additional plugins...
            ],
        }),
        lineWrapping: true,
        parent: document.getElementById(containerId), // Links to the specified container
    });

    const setEditable = (isEditable) => {
        editor.dispatch({
            effects: editableCompartment.reconfigure(EditorView.editable.of(isEditable)),
        });
    };

    // Return an object with methods for external access
    return {
        getText: () => editor.state.doc.toString(),
        setText: (newText) => {
            editor.dispatch({
                changes: { from: 0, to: editor.state.doc.length, insert: newText },
            });
        },
        setSelectedLine: (lineNumber) => {
            const line = editor.state.doc.line(lineNumber);
            editor.dispatch({
                selection: { head: line.from, anchor: line.to },
                scrollIntoView: true
            });
        },
        setEditable : setEditable,
        editor, // Expose the editor instance for further customization if needed
    };
}