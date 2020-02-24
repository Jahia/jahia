/**
* This object concerns Background Jobs in Jahia.
 * @memberof Anthracite
 * @property {object} data Stores data for Object
 * @property {method} onOpen Callback executed when the Background Jobs modal is opened in Edit Mode
 * @property {method} onClose Callback executed when the Background Jobs modal is closed
 * @property {method} autoRefreshUpdate If the user has chosen auto refresh of background jobs we need to display the text input so user can select interval
 * @property {method} buildFilterMenu Build the filter list on the side panel of the Background Jobs Modal
 * @namespace Anthracite.backgroundJobs
 * @type {object}
 */
 Anthracite.addModule("backgroundJobs", {
    /**
    * Stores data for Object
    * @memberof Anthracite.backgroundJobs
    * @type {object}
    * @property {object} filters XXX
    * @property {boolean} open XXX
    */
    data: {
        filters: [],
        open: false
    },
    /**
     * Callback executed when the Background Jobs modal is opened in Edit Mode
     * @memberof Anthracite.backgroundJobs
     * @method onOpen

     * @example
     *
     * // Callback is executed with the following listener:
     * Anthracite.listeners.queue.add([{
     *    root: 'body',
     *    type: 'onOpen',
     *    target: '.job-list-window',
     *    callback: Anthracite.backgroundJobs.onOpen,
     * }]);
     *
     *
     *
     */
    onOpen: function () {
        // Update title
        jGet.class('job-list-window').filter('.x-window-tl .x-window-header-text').setHTML(Anthracite.dictionary.get('backgroundJobs'));
        jGet.class('job-list-window').filter('.x-window-bwrap .x-panel:nth-child(1) .x-panel-bwrap .x-panel-mc .x-panel-tbar')
            .setAttribute('indigo-label', Anthracite.dictionary.get('jobs'));
        jGet.class('job-list-window')
            .filter('.x-window-bwrap .x-panel:nth-child(1) .x-panel-bwrap .x-panel-mc .x-panel-tbar .x-toolbar-left .x-toolbar-cell:nth-child(3) > div')
            .setAttribute('indigo-label', Anthracite.dictionary.get('autoRefresh'));

        // Reset the filters array
        Anthracite.backgroundJobs.data.filters = [];

        // Open GWT Filter menu to copy the entries and build our own menu
        jGet.class('job-list-window') // Get thomas to add a class on the filtered combo ...
            .filter('.x-window-bwrap .x-panel:nth-child(1) .x-panel-bwrap .x-panel-mc .x-panel-tbar .x-toolbar-left .x-toolbar-cell:nth-child(1) > table')
            .trigger('click');

        // Wait until the filter menu is opened, then copy the contents to create our own filter menu
        jGet('body').onceOpen({
            target: '.x-menu-list-item',
            callback: function () {
                var menu = jGet('.x-menu-list .x-menu-list-item span');
                menu.each(function (menuItem) {
                    // Get Label
                    var textNode = menuItem.getHTML();
                    var labelSplit = textNode.split('<');
                    var label = labelSplit[0];

                    // Get checked status
                    var img = menuItem.filter('img');
                    var backgroundPosition = window.getComputedStyle(img.nodes[0])['background-position'];
                    var isChecked = (backgroundPosition !== '-18px 0px');

                    // Save to filters array
                    Anthracite.backgroundJobs.data.filters.push({
                        label: label,
                        isChecked: isChecked
                    });
                });

                // Build the side menu
                Anthracite.backgroundJobs.buildFilterMenu();

                // Close the drop down menu
                jGet.getCached('body').trigger('mousedown').trigger('mouseup');
            },
            uid: "BACKGROUND-JOBS-INIT-FILTER"
        });

        // Filter toggles
        jGet.class('job-list-window').onClick({
            target: '.indigo-switch > div',
            callback: function () {
                var filterEntry = jGet.node(this).parent();
                var filterID = filterEntry.getAttribute('data-indigo-switch-id');

                filterEntry.toggleAttribute('data-indigo-switch-checked', ['true', 'false']);

                // Open the GWT filter combo
                jGet.class('job-list-window') // Get thomas to add a class on the filtered combo ...
                    .filter('.x-window-bwrap .x-panel:nth-child(1) .x-panel-bwrap .x-panel-mc .x-panel-tbar .x-toolbar-left .x-toolbar-cell:nth-child(1) > table')
                    .trigger('click');
                // When it has opened, trigger click the selected filter type
                jGet('body').onceOpen({
                    target: '.x-menu',
                    callback: function () {
                        var menu = jGet('.x-menu-list .x-menu-list-item span').index(filterID);
                        menu.trigger('click');
                    },
                    uid: "BACKGROUND-JOBS-TRIGGER-FILTER"
                });
            },
            uid: "BACKGROUND-JOBS-TOGGLE-FILTER"
        });

        // Executes when results are loaded into the list
        jGet.class('job-list-window').onOpen({
            target: '.x-grid-group, .x-grid3-row',
            callback: function () {
                var previousButton = jGet
                    .class('job-list-window')
                    .filter('.x-window-bwrap .x-panel:nth-child(1) .x-panel-bwrap .x-panel-mc .x-panel-bbar .x-toolbar-layout-ct .x-toolbar-left .x-toolbar-cell:nth-child(2) > table');
                var nextButton = jGet
                    .class('job-list-window')
                    .filter('.x-window-bwrap .x-panel:nth-child(1) .x-panel-bwrap .x-panel-mc .x-panel-bbar .x-toolbar-layout-ct .x-toolbar-left .x-toolbar-cell:nth-child(8) > table');

                // Look at the previous and next buttons to determine if there is more than one page of results
                if (previousButton.hasClass('x-item-disabled') && nextButton.hasClass('x-item-disabled')) {
                    // Only one page, so hide pager
                    jGet.class('job-list-window').setAttribute('indigo-results-multiple-pages', 'false');
                } else {
                    // More than one page, so show pager
                    jGet.class('job-list-window').setAttribute('indigo-results-multiple-pages', 'true');
                }

                // Add info and delete button to each row
                var rows = jGet.class('job-list-window').filter('.x-grid3-row');

                // Build the menu
                var actionMenu = document.createElement('menu');
                var deleteButton = document.createElement('button');
                var infoButton = document.createElement('button');

                // Add classes to menu elements
                actionMenu.classList.add('action-menu');
                deleteButton.classList.add('delete-button');
                infoButton.classList.add('info-button');

                // Add buttons to the menu
                actionMenu.appendChild(infoButton);
                actionMenu.appendChild(deleteButton);

                // Duplicate and add the menu to each row
                rows.each(function () {
                    var clonedActionMenu = actionMenu.cloneNode(true);

                    // This listener is sometimes called more than once, so check if the row has already had action menu added before adding ...
                    if (!jGet.node(this).hasClass('indigo-contains-actions-menu')) {
                        jGet.node(this)
                            .addClass('indigo-contains-actions-menu')
                            .append(clonedActionMenu);
                    }
                });

                // Flag that there are results ...
                jGet.class('job-list-window').setAttribute('indigo-results', 'true');
            },
            uid: "BACKGROUND-JOBS-FILTERED-RESULTS"
        });

        // Excutes when there are no rsults ...
        jGet.class('job-list-window').onOpen({
            target: '.x-grid-empty',
            callback: function () {
                // Flag that there are no results
                jGet.class('job-list-window').setAttribute('indigo-results', 'false');

            },
            uid: "BACKGROUND-JOBS-FILTERED-RESULTS"
        });

        // User has toggled the auto refresh checkbox, display the seconds input accordingly
        jGet('.job-list-window').onClick({
            target: 'input[type=\'checkbox\']',
            callback: function () {
                Anthracite.backgroundJobs.autoRefreshUpdate();
            },
        });

        // User has clicked on the delete entry button
        jGet('.job-list-window').onClick({
            target: '.delete-button',
            callback: function () {
                // Trigger click on the hidden GWT delete button
                jGet.class('job-list-window').filter(
                    '.x-window-bwrap .x-panel:nth-child(1) .x-panel-bwrap .x-panel-mc .x-panel-tbar .x-toolbar-left .x-toolbar-cell:nth-child(7) > table')
                    .trigger('click');
            },
            uid: "BACKGROUND-JOBS-DELETE-ENTRY"
        });

        // User has clicked on the info button
        jGet('.job-list-window').onClick({
            target: '.info-button',
            callback: function () {
                // Open the details panel by flagging the attribute
                jGet.class('job-list-window').setAttribute('data-indigo-details', 'open');
            },
            uid: "BACKGROUND-JOBS-DETAILS-ENTRY"
        });

        // User has clicked on the close details panel
        jGet('.job-list-window').onClick({
            target: '.x-window-bwrap .x-panel:nth-child(2) .x-panel-header .x-panel-toolbar',
            callback: function () {
                // Remove the  flag that displays the details panel
                jGet.class('job-list-window').setAttribute('data-indigo-details', '');
            },
        });

        jGet('.job-list-window').onMouseOver({
            target: '.x-grid3-row',
            callback: function (e) {
                // Impossible to know if an entry can be deleted or not WITHOUT first selecting it.
                // Now we select a row when it is rolled over, check if the delete button is enabled, if it is not clickable,
                //   then we hide the delete button that we previously added to the row
                var row = jGet.node(e.target);
                var isRow = row.hasClass('x-grid3-row');
                if (isRow) {
                    // Select the row
                    row.trigger('mousedown');

                    // See if the GWT delete button is clickable
                    var cantDelete = jGet
                        .class('job-list-window')
                        .filter('.x-window-bwrap .x-panel:nth-child(1) .x-panel-bwrap .x-panel-mc .x-panel-tbar .x-toolbar-left .x-toolbar-cell:nth-child(7) > table')
                        .hasClass('x-item-disabled');

                    if (cantDelete) {
                        // The GWT delete button is disactivated, so hide our delete button
                        row.addClass('indigo-cant-delete');
                    }
                }
            },
            uid: "BACKGROUND-JOBS-DETAILS-ROW-OVER"
        });

        // Initiate the auto refresh display type
        Anthracite.backgroundJobs.autoRefreshUpdate();
    },
    /**
    * Callback executed when the Background Jobs modal is closed
    * @memberof Anthracite.backgroundJobs
    * @method onClose

    * @example
    *
    *
    * Add Example here ...
    *
    *
    */
    onClose: function () {},
    /**
    * If the user has chosen auto refresh of background jobs we need to display the text input so user can select interval
    * @memberof Anthracite.backgroundJobs
    * @method autoRefreshUpdate

    * @example
    *
    *
    * Add Example here ...
    *
    *
    */
    autoRefreshUpdate: function () {
        var isChecked = jGet.class('job-list-window').filter('input[type=\'checkbox\']').nodes[0].checked;
        jGet.class('job-list-window').setAttribute('indigo-auto-refresh', isChecked);
    },
    /**
    * Build the filter list on the side panel of the Background Jobs Modal
    * @memberof Anthracite.backgroundJobs
    * @method buildFilterMenu

    * @example
    *
    *
    * Add Example here ...
    *
    *
    */
    buildFilterMenu: function () {
        // Build the filter menu on the left side of the screen
        // The details have been previously recuperated from the GWT filter by combo
        var filters = Anthracite.backgroundJobs.data.filters;

        var filterMenu = document.createElement('div');
        var filterMenuTitle = document.createElement('h1');
        var filterMenuTitleText = document.createTextNode('Filters ...');

        var switchHolder = document.createElement('div');
        var switchRail = document.createElement('div');
        var switchShuttle = document.createElement('div');

        // Define Menu
        filterMenu.classList.add('indigo-background-jobs-filters');

        // Define Title
        filterMenuTitle.appendChild(filterMenuTitleText);
        filterMenuTitle.classList.add('indigo-background-jobs-filters-title');

        // Create Switch Master
        switchHolder.classList.add('indigo-switch');
        switchRail.classList.add('indigo-switch--rail');
        switchShuttle.classList.add('indigo-switch--shuttle');
        switchHolder.appendChild(switchRail);
        switchHolder.appendChild(switchRail);
        switchHolder.appendChild(switchShuttle);

        for (var n = 0; n < filters.length; n++) {
            var filterEntry = switchHolder.cloneNode(true);

            filterEntry.setAttribute('data-indigo-switch-id', n);
            filterEntry.setAttribute('data-indigo-switch-label', filters[n].label);
            filterEntry.setAttribute('data-indigo-switch-checked', filters[n].isChecked);

            filterMenu.appendChild(filterEntry);
        }

        // Remove the filters, just incase it has already been added
        jGet('.indigo-background-jobs-filters').remove();

        // Add the new Filters Menu
        jGet('.job-list-window').append(filterMenu);
    }
});
