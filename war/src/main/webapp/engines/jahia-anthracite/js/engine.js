/**
* This object concerns the Edit Engine in Jahia.
 * @memberof Anthracite
 * @property {object} data Stores data for Object
 * @property {object} methods XXX <br> See {@link Anthracite.engine.methods}
 * @property {method} resizeLanguageInput XXX
 * @property {method} onOpen Callback executed when the Edit Engine is opened
 * @property {method} onClose Callback executed when the Edit Engine is closed
 * @property {method} onOpenHistory Callback executed when the History tab is opened in the Edit Engine
 * @property {method} onOpenWorkflow Callback executed when the Workflow tab is opened in the Edit Engine
 * @property {method} closeConditionEditor Callback executed when the Condition Editor is closed in the Edit Engine Visibility tab
 * @property {method} createConditionMenu Adds a cancel and OK button to the create condition modal
 * @property {method} editCondition Callback executed when the user clicks on the Edit Condition button
 * @property {method} addCondition Callback executed when the user clicks on a menu item in the Add New Condition drop down
 * @property {method} openConditionsMenu Callback executed when the user clicks on the Add new Condition button
 * @namespace Anthracite.engine
 * @type {object}
 */
 Anthracite.addModule("engine", {
     /**
      * Stores data for Object
      * @memberof Anthracite.engine
      * @property {boolean} returnToEditEngine XXX
      * @property {boolean} open XXX
      * @type {object}
      * @example
      *
      *
      * Add Example here ...
      *
      *
      */
    data: {
        returnToEditEngine: false,
        open: false
    },
     /**
      * XXX
      * @memberof Anthracite.engine
      * @property {method} resizeInput Callback executed when the language has been changed in Edit Mode
      * @namespace Anthracite.engine.methods
      * @type {object}
      * @example
      *
      *
      * Add Example here ...
      *
      *
      */
    methods: {
        /**
         * Callback executed when the language has been changed in Edit Mode
         *  - This function resizes the width of the language input field to the size of the text so that the arrow is displayed just after the text
         * @memberof Anthracite.engine.methods
         * @method resizeInput
         * @param {string} selector XXX

         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        resizeInput: function (selector) {
            var inputHolder = jGet(selector),
                input = jGet(selector).filter('input');

            if (input.nodes[0]) {
                var inputValue = input.nodes[0].value;

                var wideChars = 'ABCDEFGHJKLMNOPQRSTUVWXYZ';
                var mediumChars = 'abcdefghkmnopqrstuvwxyzI';
                var slimChars = '()ijl ';

                var textWidth = function (inputValue) {
                    var returnWidth = 0;

                    for (var charIndex in inputValue) {
                        var isWide = (wideChars.indexOf(inputValue[charIndex]) > -1) ? 10 : 0;
                        var isMedium = (mediumChars.indexOf(inputValue[charIndex]) > -1) ? 7 : 0;
                        var isSlim = (slimChars.indexOf(inputValue[charIndex]) > -1) ? 5 : 0;
                        var addWidth = (isWide + isMedium + isSlim);


                        returnWidth = returnWidth + (addWidth || 10);
                    }

                    return returnWidth;

                }(inputValue);

                inputHolder.nodes[0].style.setProperty('width', ((textWidth + 15) + 'px'), 'important');
            }

        }
    },
    /**
     * XXX
     * @memberof Anthracite.engine
     * @method resizeLanguageInput

     * @example
     *
     *
     * Add Example here ...
     *
     *
     */
    resizeLanguageInput: function() {
        Anthracite.edit.methods.resizeInput('.toolbar-itemsgroup-languageswitcher');

        jGet.getCached('body').onAttribute({
            target: 'body',
            attrKey: "data-main-node-displayname",
            callback: function () {
                Anthracite.edit.methods.resizeInput('.toolbar-itemsgroup-languageswitcher');
            },
            uid: "RESIZE_LANGUAGE_INPUT"
        });

    },
     /**
      * Callback executed when the Edit Engine is opened
      * @memberof Anthracite.engine
      * @method onOpen

      * @example
      *
      *
      * Add Example here ...
      *
      *
      */
    onOpen: function () {
        Anthracite.dev.log('::: APP ::: ENGINE ::: ONOPEN');

        // Get close button
        var closeButton = jGet.node(this).filter('.button-cancel');

        // Push State
        Anthracite.nav.pushState(closeButton);

        // Register Edit Engine as Opened and set locked flag to off
        jGet.getCached('body').setAttribute('data-INDIGO-EDIT-ENGINE', 'open');

        // Check if we need to create our own toggle switch when a fieldset header is loaded
        jGet('body').onOpen({
            target: '.x-fieldset-header',
            callback: function () {
                var fieldsetInput = jGet.node(this).filter('input');

                // This fieldset HAS a toggle, so replace with our own ...
                if (fieldsetInput.nodes.length > 0) {
                    this.classList.add('contains-indigo-switch');
                    this.parentNode.classList.add('fieldset-contains-indigo-switch');

                    var switchHolder = document.createElement('div');
                    var switchRail = document.createElement('div');
                    var switchShuttle = document.createElement('div');

                    switchHolder.classList.add('indigo-switch');
                    switchRail.classList.add('indigo-switch--rail');
                    switchShuttle.classList.add('indigo-switch--shuttle');

                    switchHolder.appendChild(switchRail);
                    switchHolder.appendChild(switchRail);
                    switchHolder.appendChild(switchShuttle);

                    this.insertBefore(switchHolder, this.children[0]);
                }
            },
            uid: "EDIT-ENGINE-INDIGO-SWITCH-CREATOR"
        });

        // Trigger click the GWT checkbox when the user clicks out toggle switch
        jGet('body').onClick({
            target: '.x-fieldset-header .indigo-switch',
            callback: function () {
                var checkbox = jGet.node(this).parent().filter('input');
                checkbox.trigger('click');
            },
            uid: "EDIT-ENGINE-INDIGO-SWITCH-LISTENER"
        });
    },
     /**
      * Callback executed when the Edit Engine is closed
      * @memberof Anthracite.engine
      * @method onClose

      * @example
      *
      *
      * Add Example here ...
      *
      *
      */
    onClose: function () {
        Anthracite.dev.log('::: APP ::: ENGINE ::: ONCLOSE');

        // Get close button
        var closeButton = jGet.node(this).filter('.button-cancel');

        // Remove state
        Anthracite.nav.pullState(closeButton);

        Anthracite.iframe.clearSelection();
        jGet.getCached('body').setAttribute('data-INDIGO-EDIT-ENGINE', '');

        // If there is a picker open we need to reposition the page elements ( ie. Search button, refresh button )
        if (jGet.getCached('body').getAttribute('data-indigo-picker') == 'open') {
            var splitVBar = jGet.class('x-vsplitbar');
            var splitVBarLeft = parseInt(splitVBar.nodes[0].style.left);
            Anthracite.picker.repositionSidePanel(splitVBarLeft);
        }

        if(Anthracite.data.HTTP.app == 'manager'){
            jGet('.toolbar-item-' + Anthracite.picker.data[Anthracite.picker.data.currentDisplayType]).trigger('click');
        }
    },
     /**
      * Callback executed when the History tab is opened in the Edit Engine
      * @memberof Anthracite.engine
      * @method onOpenHistory

      * @example
      *
      *
      * Add Example here ...
      *
      *
      */
    onOpenHistory: function () {
        // User has clicked on the close details panel
        jGet.id('JahiaGxtEditEnginePanel-history').onClick({
            target: '.x-panel:nth-child(2) .x-panel-toolbar',
            callback: function () {
                // Remove the  flag that displays the details panel
                jGet.id('JahiaGxtEditEnginePanel-history').setAttribute('data-indigo-details', '');
                jGet.getCached('body').setAttribute('data-indigo-history-display', '');
            },
        });

        // Executes when results are loaded into the list
        jGet.id('JahiaGxtEditEnginePanel-history').onGroupOpen({
            target: '.x-grid3-row',
            callback: function () {
                var previousButton = jGet.id('JahiaGxtEditEnginePanel-history').filter('.x-panel-bbar .x-toolbar-left .x-toolbar-cell:nth-child(2) > table');
                var nextButton = jGet.id('JahiaGxtEditEnginePanel-history').filter('.x-panel-bbar .x-toolbar-left .x-toolbar-cell:nth-child(8) > table');

                // Look at the previous and next buttons to determine if there is more than one page of results
                if (previousButton.hasClass('x-item-disabled') &&
                    nextButton.hasClass('x-item-disabled')) {
                    // Only one page, so hide pager
                    jGet.id('JahiaGxtEditEnginePanel-history').setAttribute('indigo-results-multiple-pages', 'false');
                } else {
                    // More than one page, so show pager
                    jGet.id('JahiaGxtEditEnginePanel-history').setAttribute('indigo-results-multiple-pages', 'true');
                }

                // Add info and delete button to each row
                var rows = jGet.id('JahiaGxtEditEnginePanel-history').filter('.x-grid3-row');

                // Build the menu
                var actionMenu = document.createElement('menu');
                var infoButton = document.createElement('button');

                // Add classes to menu elements
                actionMenu.classList.add('action-menu');
                infoButton.classList.add('info-button');

                // Add buttons to the menu
                actionMenu.appendChild(infoButton);

                // Duplicate and add the menu to each row
                rows.each(function () {
                    var clonedActionMenu = actionMenu.cloneNode(true);

                    // This listener is sometimes called more than once, so check if the row has already had action menu added before adding ...
                    if (!jGet.node(this).hasClass('indigo-contains-actions-menu')) {
                        jGet.node(this).addClass('indigo-contains-actions-menu').append(clonedActionMenu);
                    }
                });

                // Flag that there are results ...
                jGet.id('JahiaGxtEditEnginePanel-history').setAttribute('indigo-results', 'true');
            },
            uid: "HISTORY-RESULTS-LIST"
        });

        // Excutes when there are no results ...
        jGet.id('JahiaGxtEditEnginePanel-history').onOpen({
            target: '.x-grid-empty',
            callback: function () {
                // Flag that there are no results
                jGet.id('JahiaGxtEditEnginePanel-history').setAttribute('indigo-results', 'false');
            },
            uid: "HISTORY-NO-RESULTS-LIST"
        });

        // User has clicked on the info button
        jGet.id('JahiaGxtEditEnginePanel-history').onClick({
            target: '.info-button',
            callback: function () {
                // Open the details panel by flagging the attribute
                jGet.id('JahiaGxtEditEnginePanel-history').setAttribute('data-indigo-details', 'open');
                jGet.getCached('body').setAttribute('data-indigo-history-display', 'true');
            },
            uid: "HISTORY-DETAILS-ENTRY"
        });
    },
     /**
      * Callback executed when the Workflow tab is opened in the Edit Engine
      * @memberof Anthracite.engine
      * @method onOpenWorkflow

      * @example
      *
      *
      * Add Example here ...
      *
      *
      */
    onOpenWorkflow: function () {
        // Used to prefix the labels with the name of the Selected workflows ...
        jGet.node(this).onClick({
            target: '.x-grid3-row',
            callback: function () {
                var label = jGet.node(this).filter('.x-grid3-col-displayName').getHTML();
                var localisedLabel = (Anthracite.dictionary.get(label + 'WorkflowType')) ?
                    Anthracite.dictionary.get(label + 'WorkflowType') :
                    Anthracite.dictionary.get('workflowType').replace('%n%', label)
                var localisedChooseLabel = Anthracite.dictionary.get('chooseWorkflowType').replace('%n%', label);

                // Update labels
                jGet('#JahiaGxtEditEnginePanel-workflow > div > div:nth-child(1) > .x-panel form > div:nth-child(1)')
                    .setAttribute('data-indigo-workflow-type', localisedLabel);
                jGet('#JahiaGxtEditEnginePanel-workflow > div > div:nth-child(1) > .x-panel .x-form-field-wrap')
                    .setAttribute('data-indigo-workflow-type', localisedChooseLabel);

            },
            uid: "CHANGE_WORKFLOW_TYPE"
        });

        // Init by clicking first workflow item
        jGet.node(this).onceOpen({
            target: '.x-grid3-row',
            callback: function () {
                jGet.node(this).trigger('click');
            },
        });
    },
     /**
      * Callback executed when the Condition Editor is closed in the Edit Engine Visibility tab
      * @memberof Anthracite.engine
      * @method closeConditionEditor

      * @example
      *
      *
      * Add Example here ...
      *
      *
      */
    closeConditionEditor: function () {
        jGet('#JahiaGxtEditEnginePanel-visibility > .x-component:nth-child(4)').removeClass('indigo-show');
        jGet.getCached('body').setAttribute('data-indigo-editing-condition', false);
    },
     /**
      * Adds a cancel and OK button to the create condition modal
      * @memberof Anthracite.engine
      * @method createConditionMenu
      * @param newMenu XXX

      * @example
      *
      *
      * Add Example here ...
      *
      *
      */
    createConditionMenu: function (newMenu) {
        jGet('#JahiaGxtEditEnginePanel-visibility > .x-component:nth-child(4) .x-panel-footer').setHTML('').append(newMenu);
    },
     /**
      * Callback executed when the user clicks on the Edit Condition button
      * @memberof Anthracite.engine
      * @method editCondition

      * @example
      *
      *
      * Add Example here ...
      *
      *
      */
    editCondition: function () {
        jGet('#JahiaGxtEditEnginePanel-visibility > .x-component:nth-child(4)').addClass('indigo-show');

        jGet.getCached('body').setAttribute('data-indigo-add-visibility-condition', '').setAttribute('data-indigo-editing-condition', true);
        // Create menu ...
        var newMenu = document.createElement('menu');
        var doneButton = document.createElement('button');
        var doneButtonLabel = document.createTextNode(Anthracite.dictionary.get('save'));

        jGet.node(doneButton).addClass('done-with-condition');

        doneButton.appendChild(doneButtonLabel);
        newMenu.appendChild(doneButton);

        if (jGet('#JahiaGxtEditEnginePanel-visibility > .x-component:nth-child(4) .x-panel-footer').exists()) {
            Anthracite.engine.createConditionMenu(newMenu);
        } else {
            jGet.id('JahiaGxtEditEnginePanel-visibility').onceOpen({
                target: '.x-component:nth-child(4) .x-panel-footer',
                callback: function () {
                    Anthracite.engine.createConditionMenu(newMenu);
                },
            });
        }
    },
     /**
      * Callback executed when the user clicks on a menu item in the Add New Condition drop down
      * @memberof Anthracite.engine
      * @method addCondition

      * @example
      *
      *
      * Add Example here ...
      *
      *
      */
    addCondition: function () {
        jGet('#JahiaGxtEditEnginePanel-visibility > .x-component:nth-child(4)').addClass('indigo-show');
        jGet.getCached('body').setAttribute('data-indigo-editing-condition', 'new');

        var newMenu = document.createElement('menu');
        var saveButton = document.createElement('button');
        var saveButtonLabel = document.createTextNode(Anthracite.dictionary.get('create'));
        var closeButton = document.createElement('button');
        var closeButtonLabel = document.createTextNode(Anthracite.dictionary.get('cancel'));

        jGet.node(saveButton).addClass('save-new-condition');
        jGet.node(closeButton).addClass('cancel-new-condition');

        closeButton.appendChild(closeButtonLabel);
        saveButton.appendChild(saveButtonLabel);

        newMenu.appendChild(closeButton);
        newMenu.appendChild(saveButton);

        jGet('body').oneClick({
            target: '#JahiaGxtEditEnginePanel-visibility .cancel-new-condition',
            callback: function () {
                // DEV NOTE ::: Get rid of this timeout
                setTimeout(function () {
                    jGet.id('JahiaGxtEditEnginePanel-visibility').filter('.x-grid3-row.x-grid3-row-selected .x-grid3-col-remove > table .x-btn-small').trigger('click');
                }, 5);
            },
        });

        if (jGet('#JahiaGxtEditEnginePanel-visibility > .x-component:nth-child(4) .x-panel-footer').exists()) {
            Anthracite.engine.createConditionMenu(newMenu);
        } else {
            jGet.id('JahiaGxtEditEnginePanel-visibility').onceOpen({
                target: '.x-component:nth-child(4) .x-panel-footer',
                callback: function () {
                    Anthracite.engine.createConditionMenu(newMenu);
                },
            });
        }
    },
     /**
      * Callback executed when the user clicks on the Add new Condition button
      * @memberof Anthracite.engine
      * @method openConditionsMenu

      * @example
      *
      *
      * Add Example here ...
      *
      *
      */
    openConditionsMenu: function () {
        jGet('body').onceOpen({
            target: '.x-combo-list',
            callback: function () {
                jGet('body').setAttribute('data-indigo-add-visibility-condition', 'new');
                jGet('body').oneMouseDown({
                    target: '.x-combo-list-item',
                    callback: function () {
                        // DEV NOTE ::: Get rid of this timeout
                        setTimeout(function () {
                            jGet('#JahiaGxtEditEnginePanel-visibility > .x-component:nth-child(2) > .x-component:nth-child(2) > table > tbody > tr > td:nth-child(5) > table').trigger('click');
                        }, 5);
                    },
                });
            },
        });

        jGet('body').onceClose({
            target: '.x-combo-list',
            callback: function () {
                // Remove class that modifies the set up for this context menu
                jGet('body').setAttribute('data-indigo-add-visibility-condition', '');
            },
        });
    }
});
