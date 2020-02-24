/**
* This object concerns the Pickers in Jahia.
 * @memberof Anthracite
 * @property {object} data Stores data for Object
 * @property {method} repositionSidePanel Fix the picker side panel when it has been resized
 * @property {method} onResize Callback executed whilst the picker side panel is being resized
 * @property {method} onOpen Callback executed when the picker is opened
 * @property {method} onCloseSubPicker Callback executed when a picker opened from within a picker is closed
 * @property {method} onOpenSubPicker Callback executed when a picker is opened from within a picker
 * @property {method} updateMultipleCount Callback executed when multiple picker selection changes
 * @property {method} updateMultipleSubCount Callback executed when nested multiple picker selection changes
 * @property {method} updateZoomLevel Callback executed when the thumbnail size slider is changed
 * @property {method} setPlaceholders Add placeholders to fields in pickers
 * @property {method} onClose Callback executed when picker closes
 * @property {method} onSubClose Callback executed when a picker opened from within a picker closes
 * @property {method} onClick Callback executed when user clicks on picker
 * @property {method} onListView Callback executed when picker changes to List View
 * @property {method} onThumbView Callback executed when picker changes to Thumb View
 * @property {method} onDetailView Callback executed when picker changes to Detailed View
 * @property {object} row XXX <br> See {@link Anthracite.picker.row}
 * @property {object} thumb XXX <br> See {@link Anthracite.picker.thumb}
 * @property {object} previewButton XXX <br> See {@link Anthracite.picker.previewButton}
 * @property {object} source XXX <br> See {@link Anthracite.picker.source}
 * @property {object} search XXX <br> See {@link Anthracite.picker.search}
 * @namespace Anthracite.picker
 * @type {object}
 */
 Anthracite.addModule("picker", {
     /**
      * Stores data for Object
      * @memberof Anthracite.picker
      * @property {boolean} currentItem XXX
      * @property {boolean} title XXX
      * @property {boolean} pickerTitle XXX
      * @property {string} displayType XXX
      * @property {string} subPickerDisplayType XXX
      * @property {string} currentDisplayType XXX
      * @property {boolean} previousDisplayType XXX
      * @property {string} ID XXX
      * @property {string} standaloneID XXX
      * @property {string} standaloneManagerID XXX
      * @property {string} inpageID XXX
      * @property {boolean} hasPreview XXX
      * @property {integer} selectedFileCount XXX
      * @property {integer} selectedSubFileCount XXX
      * @property {object} explorer XXX
      * @property {integer} explorer.width XXX
      * @property {object} zooms XXX
      * @property {integer} zooms.thumbsview XXX
      * @property {integer} zooms.detailedview XXX
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
        currentItem: null,
        title: null,
        pickerTitle: null,
        displayType: 'listview',
        subPickerDisplayType: 'listview',
        currentDisplayType: 'displayType',
        previousDisplayType: null,
        ID: 'JahiaGxtContentPickerWindow',
        standaloneID: 'contentpicker',
        standaloneManagerID: 'contentmanager',
        inpageID: 'JahiaGxtContentPickerWindow',
        hasPreview: null,
        selectedFileCount: 0,
        selectedSubFileCount: 0,
        explorer: {
            width: 340
        },
        zooms: {
            thumbsview: 1,
            detailedview: 2
        },
        open: false
    },
     /**
      * Fix the picker side panel when it has been resized
      * @memberof Anthracite.picker
      * @method repositionSidePanel
      * @param {integer} splitterLeft XXX
      * @example
      *
      *
      * Add Example here ...
      *
      *
      */
    repositionSidePanel: function (splitterLeft) {
        var searchIsOpen = jGet.getCached('body').getAttribute('data-INDIGO-PICKER-SEARCH') == 'open',
            explorerWidth = (searchIsOpen) ? 1 : (splitterLeft || Anthracite.picker.data.explorer.width);

        if(!searchIsOpen && splitterLeft){
            // Want to save the
            Anthracite.picker.data.explorer.width = splitterLeft;
        }

        Anthracite.dev.log('::: APP ::: PICKER ::: REPOSITIONSIDEPANEL');

        var isNestedPicker = jGet.getCached('body').getAttribute('data-indigo-sub-picker') == 'open';
        var DOMPaths = {
            'JahiaGxtManagerLeftTree': (isNestedPicker) ? '#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree' : '#JahiaGxtManagerLeftTree',
            'JahiaGxtManagerTobTable': (isNestedPicker) ? '#JahiaGxtContentPickerWindow #JahiaGxtManagerTobTable' : '#JahiaGxtManagerTobTable',
            'JahiaGxtManagerToolbar': (isNestedPicker) ? '#JahiaGxtContentPickerWindow #JahiaGxtManagerToolbar' : '#JahiaGxtManagerToolbar',
            '#JahiaGxtManagerLeftTree .x-tab-strip-spacer': (isNestedPicker) ? '#JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-tab-strip-spacer' : '#JahiaGxtManagerLeftTree .x-tab-strip-spacer',
            '#CRTbrowseTabItem > div > .x-panel > .x-panel-bwrap': (isNestedPicker) ? '#JahiaGxtContentPickerWindow #CRTbrowseTabItem > div > .x-panel > .x-panel-bwrap' : '#CRTbrowseTabItem > div > .x-panel > .x-panel-bwrap',
            '#CRTbrowseTabItem > div > .x-panel > .x-panel-bwrap .x-accordion-hd': (isNestedPicker) ? '#JahiaGxtContentPickerWindow #CRTbrowseTabItem > div > .x-panel > .x-panel-bwrap .x-accordion-hd' : '#CRTbrowseTabItem > div > .x-panel > .x-panel-bwrap .x-accordion-hd'
        };

        // Calculate Scale size for the picker title
        var pickerTitle = (Anthracite.picker.data.standalone) ? jGet('#pickerTitle')
            : jGet.id(Anthracite.picker.data.ID).filter('.x-window-tl .x-window-header-text');

        // Reset the Title size to recalculate the scale from scratch
        pickerTitle.css({
            transform: 'scale(1)',
            transformOrigin: 'left'
        });

        var pickerTitleBox = pickerTitle.getNode(0).getBoundingClientRect();
        var pickerTitleBoxLeft = pickerTitleBox.left;
        var pickerTitleBoxWidth = pickerTitleBox.width;
        var pickerTitleBoxPadding = (pickerTitleBoxLeft * 2);
        var searchButtonWidth = 50;
        var pickerTitleBoxScale = Math.min(explorerWidth / (pickerTitleBoxPadding + pickerTitleBoxWidth + searchButtonWidth), 1);

        // Set size of the Title
        pickerTitle.css({
            transform: 'scale(' + pickerTitleBoxScale + ')',
            transformOrigin: 'left center'
        });

        // Update title box info
        pickerTitleBox = pickerTitle.getNode(0).getBoundingClientRect();
        pickerTitleBoxLeft = pickerTitleBox.left;
        pickerTitleBoxWidth = pickerTitleBox.width;
        var searchLeftPosition = (pickerTitleBoxLeft + pickerTitleBoxWidth + 5);

        // Move the search button
        jGet.id('JahiaGxtManagerLeftTree__CRTsearchTabItem').css({
            left: searchLeftPosition + 'px'
        });

        // Set width of the side panel
        jGet(DOMPaths['JahiaGxtManagerLeftTree']).nodes[0].style.setProperty('width', explorerWidth + 'px',
            'important');
        jGet(DOMPaths['JahiaGxtManagerLeftTree']).nodes[0].style.setProperty('left', '0px', 'important');

        // Set width and position of right panel
        if (jGet.getCached('body').getAttribute('indigo-picker-panel') == 'collapsed') {
            jGet(DOMPaths['JahiaGxtManagerTobTable']).nodes[0].style.setProperty('left', '0px', 'important');
            jGet(DOMPaths['JahiaGxtManagerTobTable']).nodes[0].style.setProperty('width', '100%', 'important');
            // Move the top toolbar
            jGet(DOMPaths['JahiaGxtManagerToolbar']).nodes[0].style.setProperty('left', searchLeftPosition + 'px', 'important');

            // Move filter toolbar
            if (jGet(DOMPaths['JahiaGxtManagerTobTable']).filter('.x-panel-tbar').exists()) {
                jGet(DOMPaths['JahiaGxtManagerTobTable']).filter('.x-panel-tbar').nodes[0].style.setProperty('left', '20px',
                    'important');
            }
        } else {
            jGet(DOMPaths['JahiaGxtManagerTobTable']).nodes[0].style.setProperty('left', explorerWidth + 'px',
                'important');
            jGet(DOMPaths['JahiaGxtManagerTobTable']).nodes[0].style.setProperty('width',
                'calc(100% - ' + explorerWidth + 'px) ',
                'important');
            // Move the top toolbar
            jGet(DOMPaths['JahiaGxtManagerToolbar']).nodes[0].style.setProperty('left', explorerWidth + 'px',
                'important');

            // Move filter toolbar
            if (jGet(DOMPaths['JahiaGxtManagerTobTable']).filter('.x-panel-tbar').exists()) {
                jGet(DOMPaths['JahiaGxtManagerTobTable']).filter('.x-panel-tbar').nodes[0].style.setProperty('left',
                    (explorerWidth + 20) + 'px',
                    'important');
            }
        }

        // Move toggle button
        jGet.id('toggle-picker-files').css({
            left: (explorerWidth - 25) + 'px'
        });

        // Set the width of the left tree
        jGet(DOMPaths['#CRTbrowseTabItem > div > .x-panel > .x-panel-bwrap']).each(function () {
            this.style.setProperty('width', explorerWidth + 'px', 'important');
        });

        jGet(DOMPaths['#CRTbrowseTabItem > div > .x-panel > .x-panel-bwrap .x-accordion-hd']).each(function () {
            this.style.setProperty('width', explorerWidth + 'px', 'important');
        });

        // Set the position of the refresh button based on VISIBLE combo header
        var sourceCombo = jGet('#CRTbrowseTabItem > div > .x-panel:not(.x-panel-collapsed) .x-accordion-hd .x-panel-header-text');
        var sourceComboBox = sourceCombo.getNode(0).getBoundingClientRect();
        var sourceComboBoxLeft = sourceComboBox.left;
        var sourceComboBoxWidth = sourceComboBox.width;
        var RefreshLeftPosition = (sourceComboBoxLeft + sourceComboBoxWidth);

        jGet('#CRTbrowseTabItem > div > .x-panel .x-accordion-hd .x-tool-refresh').css({
            left: RefreshLeftPosition + 'px'
        });

        // Set the width of the Combo Hot Spot
        jGet(DOMPaths['#JahiaGxtManagerLeftTree .x-tab-strip-spacer']).css({
            width: (sourceComboBoxWidth + 4) + 'px'
        });
    },
     /**
      * Callback executed whilst the picker side panel is being resized
      * @memberof Anthracite.picker
      * @method onResize
      * @returns {boolean|undefined} XXX
      * @example
      *
      *
      * Add Example here ...
      *
      *
      */
    onResize: function () {
        Anthracite.dev.log('::: APP ::: PICKER ::: ONRESIZE');

        var previousDisplayButton = jGet('.toolbar-item-' + Anthracite.picker.data[Anthracite.picker.data.currentDisplayType]);

        if(previousDisplayButton.exists()){
            previousDisplayButton.trigger('click');
        }

        // User has resized the left panel in a picker
        if (jGet.getCached('body').getAttribute('data-indigo-picker') == 'open') {
            var splitterLeft = parseInt(jGet.node(this).nodes[0].style.left);

            // If the requested drag position of the user is too narrow, reset to minimum width
            if (splitterLeft < 290) {
                // Too narrow, so set to minimum width
                jGet('.x-vsplitbar').css({
                    left: '290px'
                });

                // Stop execution
                return false;
            } else {
                Anthracite.picker.repositionSidePanel(splitterLeft);
            }
        }
    },
     /**
      * Callback executed when the picker is opened
      * @memberof Anthracite.picker
      * @method onOpen
      * @example
      *
      *
      * Add Example here ...
      *
      *
      */
    onOpen: function () {
        Anthracite.dev.log('::: APP ::: PICKER ::: ONOPEN');

        // See if GWT has enabled previews for files, if so then set the preview flag to true
        Anthracite.picker.data.enablePreviews = jGet('#' + Anthracite.picker.data.ID + ' .toolbar-item-filepreview').nodes.length > 0;

        // Set flags for CSS
        jGet.getCached('body')
            .setAttribute('data-INDIGO-PICKER-SEARCH', '')
            .setAttribute('data-INDIGO-PICKER', 'open')
            .setAttribute('indigo-PICKER-DISPLAY', Anthracite.picker.data[Anthracite.picker.data.currentDisplayType]);

        if (Anthracite.picker.data.standalone) {
            // Create title
            var pickerH1 = document.createElement('h1');
            var pickerH1Label = document.createTextNode(Anthracite.dictionary.get('pickerTitle-' + Anthracite.data.HTTP.picker));

            pickerH1.id = 'pickerTitle';
            pickerH1.appendChild(pickerH1Label);

            // Add title to page
            jGet.id(Anthracite.picker.data.ID).prepend(pickerH1);

            if (Anthracite.data.HTTP.app == 'manager') {
                // See if GWT has enabled previews for files, if so then set the preview flag to true
                Anthracite.picker.data.enablePreviews = true;
            }
        }

        // Set zoom states
        Anthracite.picker.updateZoomLevel();

        var pickerTitle = (Anthracite.picker.data.standalone) ? jGet('#pickerTitle')
            : jGet.id(Anthracite.picker.data.ID).filter('.x-window-tl .x-window-header-text');
        var box = pickerTitle.getNode(0).getBoundingClientRect();
        var left = box.left;
        var width = box.width;
        var searchLeftPosition = (left + width + 5);

        jGet.id('JahiaGxtManagerLeftTree__CRTsearchTabItem').css({
            left: searchLeftPosition + 'px'
        });

        // Save picker title ( for later use in search placeholder )
        Anthracite.picker.data.pickerTitle = pickerTitle.getHTML();

        // Create button to toggle the left panel
        var toggleFilesButton = document.createElement('button');

        toggleFilesButton.id = 'toggle-picker-files';
        toggleFilesButton.classList.add('toggle-picker-files');

        jGet.id(Anthracite.picker.data.ID).prepend(toggleFilesButton);

        // Add placeholders to form elements
        Anthracite.picker.setPlaceholders();

        // Reset classes that may have been previously added
        jGet.id(Anthracite.picker.data.ID).removeClass('search-panel-opened');

        // Register the side panel as open:
        jGet.id(Anthracite.picker.data.ID).setAttribute('indigo-picker-panel', 'opened');

        // Listen for clicks on toggle button
        jGet.id(Anthracite.picker.data.ID).onClick({
            target: '#toggle-picker-files',
            callback: function () {
                jGet.id(Anthracite.picker.data.ID).toggleClass('indigo-collapsed');
                jGet.id(Anthracite.picker.data.ID).toggleAttribute('indigo-picker-panel', ['collapsed', 'opened']);
                jGet.getCached('body').toggleAttribute('indigo-picker-panel', ['collapsed', 'opened']);

                var pickerTitle = (Anthracite.picker.data.standalone) ? jGet('#pickerTitle')
                    : jGet.id(Anthracite.picker.data.ID).filter('.x-window-tl .x-window-header-text');
                var box = pickerTitle.getNode(0).getBoundingClientRect();
                var left = box.left;
                var width = box.width;
                var toolbarLeft = (left + width);

                jGet.id('JahiaGxtManagerToolbar').css({
                    left: toolbarLeft + 'px'
                });

                Anthracite.picker.repositionSidePanel();
            },
            uid: "TOGGLE-PICKER-FILES"
        });

        // Listen for changes in slider (input range)
        jGet.id(Anthracite.picker.data.ID).onInput({
            target: '#thumb-size-slider',
            callback: function (e) {
                var zoomSize = e.target.value;

                // Save zoom level
                Anthracite.picker.data.zooms[Anthracite.picker.data[Anthracite.picker.data.currentDisplayType]] = zoomSize;

                jGet('#' + Anthracite.picker.data.ID + ' #JahiaGxtManagerLeftTree + div #images-view .x-view')
                    .setAttribute('indigo-thumb-zoom', zoomSize);
            },
            uid: "THUMB-SIZE-SLIDER"
        });

        // If it is a multi picker we need to do this ...
        if (jGet.id(Anthracite.picker.data.ID).filter('#JahiaGxtManagerBottomTabs').exists()) {
            // Create a toggle button for multiple selection
            var toggleButton = document.createElement('button');
            var toggleButtonLabel = document.createTextNode('Multiple Selection');

            toggleButton.appendChild(toggleButtonLabel);
            toggleButton.classList.add('toggle-multiple-selection');

            jGet.id(Anthracite.picker.data.ID).filter('#JahiaGxtManagerBottomTabs').prepend(toggleButton);

            // Add class for CSS
            jGet.id(Anthracite.picker.data.ID).addClass('indigo-picker-multi-select');

            // Listen for files being added to the multiple selection
            jGet.id(Anthracite.picker.data.ID).onGroupOpen('#JahiaGxtManagerBottomTabs .x-grid-group', function () {
                Anthracite.picker.data.selectedFileCount = this.length;
                Anthracite.picker.updateMultipleCount();
            }, 'ADDED_FILES_MULTI_SELECT');

            // Listen for files being removed from the multiple selection
            jGet.id(Anthracite.picker.data.ID).onGroupClose({
                target: '.x-grid-group',
                callback: function () {
                    // Need to manually count the files in multiple selection ...
                    Anthracite.picker.data.selectedFileCount = jGet.id(Anthracite.picker.data.ID).filter('#JahiaGxtManagerBottomTabs .x-grid-group').nodes.length;
                    Anthracite.picker.updateMultipleCount();
                },
                uid: "REMOVED_FILES_MULTI_SELECT"
            });

            // Listen for clicks on the multiple selection toggle button
            jGet.id(Anthracite.picker.data.ID).onClick({
                target: '.toggle-multiple-selection',
                callback: function () {
                    jGet.id('JahiaGxtManagerBottomTabs').toggleClass('indigo-collapsed');
                },
                uid: "TOGGLE_MULTI_SELECT"
            });
        }

        // See if GWT has included a slider for thumb preview, if so then we can add ours ( which is a GWT replacement )
        var hasSlider = jGet('#' + Anthracite.picker.data.ID + ' .x-slider').nodes.length > 0;
        if (hasSlider || Anthracite.data.HTTP.app == 'manager') {
            var thumbSlider = document.createElement('input');
            thumbSlider.id = 'thumb-size-slider';
            thumbSlider.classList.add('thumb-size-slider');
            thumbSlider.type = 'range';
            thumbSlider.value = 4;
            thumbSlider.min = 1;
            thumbSlider.max = 6;

            jGet.id(Anthracite.picker.data.ID).prepend(thumbSlider);
        }
    },
     /**
      * Callback executed when a picker opened from within a picker is closed
      * @memberof Anthracite.picker
      * @method onCloseSubPicker
      * @example
      *
      *
      * Add Example here ...
      *
      *
      */
    onCloseSubPicker: function(){
        Anthracite.dev.log('::: APP ::: PICKER ::: ONCLOSESUBPICKER');
        Anthracite.picker.data.currentDisplayType = 'displayType';
        jGet.getCached('body').setAttribute('indigo-PICKER-DISPLAY', Anthracite.picker.data.displayType);
    },
     /**
      * Callback executed when a picker is opened from within a picker
      * @memberof Anthracite.picker
      * @method onOpenSubPicker
      * @example
      *
      *
      * Add Example here ...
      *
      *
      */
    onOpenSubPicker: function () {
        Anthracite.dev.log('::: APP ::: PICKER ::: ONOPENSUBPICKER');

        Anthracite.picker.data.currentDisplayType = 'subPickerDisplayType';

        // Set flags for CSS
        jGet.getCached('body')
            .setAttribute('data-INDIGO-SUB-PICKER', 'open')
            .setAttribute('data-INDIGO-PICKER-SEARCH', '')
            .setAttribute('data-INDIGO-PICKER', 'open')
            .setAttribute('indigo-PICKER-DISPLAY', Anthracite.picker.data.subPickerDisplayType);

        // Set zoom states
        Anthracite.picker.updateZoomLevel();

        var pickerTitle = jGet('body > #JahiaGxtContentPickerWindow .x-window-header-text');
        var box = pickerTitle.getNode(0).getBoundingClientRect();
        var left = box.left;
        var width = box.width;
        var searchLeftPosition = (left + width + 5);

        jGet('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree__CRTsearchTabItem').css({
            left: searchLeftPosition + 'px'
        });

        // Save picker title ( for later use in search placeholder )
        Anthracite.picker.data.subPickerTitle = pickerTitle.getHTML();

        // Create button to toggle the left panel
        var toggleFilesButton = document.createElement('button');
        toggleFilesButton.id = 'toggle-sub-picker-files';
        toggleFilesButton.classList.add('toggle-picker-files');

        jGet('body > #JahiaGxtContentPickerWindow').prepend(toggleFilesButton);

        // Add placeholders to form elements
        var filterField = jGet('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerTobTable .x-panel-tbar .x-toolbar-cell:nth-child(2) .x-form-text');
        var sortBy = jGet('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerTobTable .x-panel-tbar .x-toolbar-cell:nth-child(5) .x-form-text');

        filterField.setAttribute('placeholder', Anthracite.dictionary.get('filterField'));
        sortBy.setAttribute('placeholder', Anthracite.dictionary.get('sortBy'));

        // Reset classes that may have been previously added
        jGet('body > #JahiaGxtContentPickerWindow').removeClass('search-panel-opened');
        jGet.id(Anthracite.picker.data.ID).removeClass('search-panel-opened');

        // Register the side panel as open:
        jGet('body > #JahiaGxtContentPickerWindow').setAttribute('indigo-picker-panel', 'opened');

        // Listen for clicks on toggle button
        jGet('body > #JahiaGxtContentPickerWindow').onClick({
            target: '#toggle-sub-picker-files',
            callback: function () {
                jGet('body > #JahiaGxtContentPickerWindow').toggleClass('indigo-collapsed');
                jGet('body > #JahiaGxtContentPickerWindow').toggleAttribute('indigo-picker-panel', ['collapsed', 'opened']);
                jGet.getCached('body').toggleAttribute('indigo-sub-picker-panel', ['collapsed', 'opened']);

                var pickerTitle = jGet('body > #JahiaGxtContentPickerWindow .x-window-header-text');
                var box = pickerTitle.getNode(0).getBoundingClientRect();
                var left = box.left;
                var width = box.width;
                var toolbarLeft = (left + width);

                jGet('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerToolbar').css({
                    left: toolbarLeft + 'px'
                });
            },
            uid: "TOGGLE-SUB-PICKER-FILES"
        });

        // Listen for changes in slider (input range)
        jGet('body > #JahiaGxtContentPickerWindow').onInput({
            target: '#sub-picker-thumb-size-slider',
            callback: function (e) {
                var zoomSize = e.target.value;
                // Save zoom level
                Anthracite.picker.data.zooms[Anthracite.picker.data[Anthracite.picker.data.currentDisplayType]] = zoomSize;

                jGet('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree + div #images-view .x-view')
                    .setAttribute('indigo-thumb-zoom', zoomSize);
            },
            uid: "SUB-PICKER-THUMB-SIZE-SLIDER"
        });

        // If it is a multi picker we need to do this ...
        if (jGet('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerBottomTabs').exists()) {
            // Create a toggle button for multiple selection
            var toggleButton = document.createElement('button');
            var toggleButtonLabel = document.createTextNode('Multiple Selection');
            toggleButton.appendChild(toggleButtonLabel);
            toggleButton.classList.add('toggle-multiple-selection');

            jGet('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerBottomTabs').prepend(toggleButton);

            // Add class for CSS
            jGet('body > #JahiaGxtContentPickerWindow').addClass('indigo-picker-multi-select');

            // Listen for files being added to the multiple selection
            jGet('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerBottomTabs')
                .onGroupOpen({
                    target: '.x-grid-group',
                    callback: function () {
                        Anthracite.picker.data.selectedSubFileCount = this.length;
                        Anthracite.picker.updateMultipleSubCount();
                    },
                    uid: "ADDED_FILES_MULTI_SELECT_SUB"
                });

            // Listen for files being removed from the multiple selection
            jGet('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerBottomTabs')
                .onGroupClose({
                    target: '.x-grid-group',
                    callback: function () {
                        // Need to manually count the files in multiple selection ...
                        Anthracite.picker.data.selectedSubFileCount = jGet('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerBottomTabs')
                            .filter('.x-grid-group').nodes.length;
                        Anthracite.picker.updateMultipleSubCount();
                    },
                    uid: "REMOVED_FILES_MULTI_SELECT_SUB"
                });

            // Listen for clicks on the multiple selection toggle button
            jGet.id('JahiaGxtManagerBottomTabs').onClick({
                target: '.toggle-multiple-selection',
                callback: function () {
                    jGet('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerBottomTabs').toggleClass('indigo-collapsed');
                },
                uid: "TOGGLE_MULTI_SELECT_SUB"
            });
        }

        // Add slider for zooming images
        var thumbSlider = document.createElement('input');
        thumbSlider.id = 'sub-picker-thumb-size-slider';
        thumbSlider.classList.add('thumb-size-slider');
        thumbSlider.type = 'range';
        thumbSlider.value = 4;
        thumbSlider.min = 1;
        thumbSlider.max = 6;

        jGet('body > #JahiaGxtContentPickerWindow').prepend(thumbSlider);
        var subPickerListViewButton = jGet('.modal-imagepicker .toolbar-item-' + Anthracite.picker.data[Anthracite.picker.data.currentDisplayType]);
        subPickerListViewButton.trigger('click');
    },
    /**
     * Callback executed when multiple picker selection changes
     * @memberof Anthracite.picker
     * @method updateMultipleCount
     * @example
     *
     *
     * Add Example here ...
     *
     *
     */
    updateMultipleCount: function () {
        var toggleString;
        var selectedFileCount = Anthracite.picker.data.selectedFileCount;
        if (selectedFileCount > 0) {
            jGet.id('JahiaGxtManagerBottomTabs').addClass('selected-files');
            toggleString = 'Multiple selection (' + selectedFileCount + ')';
        } else {
            jGet.id('JahiaGxtManagerBottomTabs').removeClass('selected-files');
            toggleString = 'Multiple selection';
        }

        jGet.class('toggle-multiple-selection').setHTML(toggleString);
    },
    /**
     * Callback executed when nested multiple picker selection changes
     * @memberof Anthracite.picker
     * @method updateMultipleSubCount
     * @example
     *
     *
     * Add Example here ...
     *
     *
     */
    updateMultipleSubCount: function () {
        var toggleString;
        var selectedFileCount = Anthracite.picker.data.selectedSubFileCount;
        if (selectedFileCount > 0) {
            jGet('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerBottomTabs').addClass('selected-files');
            toggleString = 'Multiple selection (' + selectedFileCount + ')';
        } else {
            jGet('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerBottomTabs').removeClass('selected-files');
            toggleString = 'Multiple selection';
        }

        jGet('body > #JahiaGxtContentPickerWindow .toggle-multiple-selection').setHTML(toggleString);
    },
    /**
     * Callback executed when the thumbnail size slider is changed
     * @memberof Anthracite.picker
     * @method updateZoomLevel
     * @example
     *
     *
     * Add Example here ...
     *
     *
     */
    updateZoomLevel: function () {
        if (jGet.id('thumb-size-slider').nodes[0]) {
            jGet.id('thumb-size-slider').nodes[0].value = Anthracite.picker.data.zooms[Anthracite.picker.data[Anthracite.picker.data.currentDisplayType]];
        }

        jGet('#' + Anthracite.picker.data.ID + ' #JahiaGxtManagerLeftTree + div #images-view .x-view')
            .setAttribute('indigo-thumb-zoom', Anthracite.picker.data.zooms[Anthracite.picker.data[Anthracite.picker.data.currentDisplayType]]);
    },
    /**
     * Add placeholders to fields in pickers
     * @memberof Anthracite.picker
     * @method setPlaceholders
     * @example
     *
     *
     * Add Example here ...
     *
     *
     */
    setPlaceholders: function () {
        if (Anthracite.data.HTTP.app == 'manager') {
            // Have to wait until the fields are loaded
            jGet.id('JahiaGxtManagerTobTable').onOpen({
                target: '.x-panel-tbar',
                callback: function () {
                    var filterField = jGet('#JahiaGxtManagerTobTable .x-panel-tbar .x-toolbar-cell:nth-child(2) .x-form-text');
                    var sortBy = jGet('#JahiaGxtManagerTobTable .x-panel-tbar .x-toolbar-cell:nth-child(5) .x-form-text');

                    filterField.setAttribute('placeholder', Anthracite.dictionary.get('filterField'));
                    sortBy.setAttribute('placeholder', Anthracite.dictionary.get('sortBy'));
                },
                uid: "UPDATE_PLACEHOLDERS"
            });
        } else {
            var filterField = jGet('#images-view .x-toolbar .x-toolbar-left .x-toolbar-cell:nth-child(2) .x-form-text');
            var sortBy = jGet('#images-view .x-toolbar .x-toolbar-left .x-toolbar-cell:nth-child(5) .x-form-text');
            filterField.setAttribute('placeholder', Anthracite.dictionary.get('filterField'));
            sortBy.setAttribute('placeholder', Anthracite.dictionary.get('sortBy'));
        }
    },
    /**
     * Callback executed when picker closes
     * @memberof Anthracite.picker
     * @method onClose
     * @example
     *
     *
     * Add Example here ...
     *
     *
     */
    onClose: function () {
        Anthracite.dev.log('::: APP ::: PICKER ::: ONCLOSE');
        if (jGet.getCached('body').getAttribute('data-INDIGO-SUB-PICKER') == 'open') {
            // Closing a sub picker
            jGet.getCached('body')
                .setAttribute('data-indigo-sub-picker', '')
                .setAttribute('data-INDIGO-PICKER-SEARCH', '');
        } else {
            jGet.getCached('body')
                .setAttribute('data-INDIGO-PICKER', '');
        }
    },
    /**
     * Callback executed when a picker opened from within a picker closes
     * @memberof Anthracite.picker
     * @method onSubClose
     * @example
     *
     *
     * Add Example here ...
     *
     *
     */
    onSubClose: function () {
        Anthracite.dev.log('::: APP ::: PICKER ::: ONSUBCLOSE');

        jGet.getCached('body')
            .setAttribute('data-indigo-sub-picker', '')
            .setAttribute('data-INDIGO-PICKER', '');
    },
    /**
     * Callback executed when user clicks on picker
     * @memberof Anthracite.picker
     * @method onClick
     * @example
     *
     *
     * Add Example here ...
     *
     *
     */
    onClick: function () {
        Anthracite.dev.log('::: APP ::: PICKER ::: ONCLICK');
        jGet.getCached('body').setAttribute('data-INDIGO-PICKER-SOURCE-PANEL', '');
    },
    /**
     * Callback executed when picker changes to List View
     * @memberof Anthracite.picker
     * @method onListView
     * @example
     *
     *
     * Add Example here ...
     *
     *
     */
    onListView: function () {
        Anthracite.dev.log('::: APP ::: PICKER ::: ONLISTVIEW');
        Anthracite.picker.data[Anthracite.picker.data.currentDisplayType] = 'listview';
        jGet.getCached('body').setAttribute('indigo-PICKER-DISPLAY', 'listview');
        Anthracite.picker.repositionSidePanel();
    },
    /**
     * Callback executed when picker changes to Thumb View
     * @memberof Anthracite.picker
     * @method onThumbView
     * @example
     *
     *
     * Add Example here ...
     *
     *
     */
    onThumbView: function () {
        Anthracite.dev.log('::: APP ::: PICKER ::: ONTHUMBVIEW');
        Anthracite.picker.data[Anthracite.picker.data.currentDisplayType] = 'thumbsview';
        jGet.getCached('body').setAttribute('indigo-PICKER-DISPLAY', 'thumbsview');
        Anthracite.picker.setPlaceholders();
        Anthracite.picker.updateZoomLevel();
        Anthracite.picker.repositionSidePanel();
    },
    /**
     * Callback executed when picker changes to Detailed View
     * @memberof Anthracite.picker
     * @method onDetailView
     * @example
     *
     *
     * Add Example here ...
     *
     *
     */
    onDetailView: function () {
        Anthracite.dev.log('::: APP ::: PICKER ::: ONDETAILVIEW');
        Anthracite.picker.data[Anthracite.picker.data.currentDisplayType] = 'detailedview';
        jGet.getCached('body').setAttribute('indigo-PICKER-DISPLAY', 'detailedview');
        Anthracite.picker.setPlaceholders();
        Anthracite.picker.updateZoomLevel();
        Anthracite.picker.repositionSidePanel();
    },
    /**
     * XXX
     * @memberof Anthracite.picker
     * @property {method} onClick XXX
     * @property {method} onMouseOver XXX
     * @property {method} onContext Callback executed when the user clicks on the more info button
     * @namespace Anthracite.picker.row
     * @type {object}
     * @example
     *
     *
     * Add Example here ...
     *
     *
     */
    row: {
        /**
         * XXX
         * @memberof Anthracite.picker.row
         * @method onClick
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        onClick: function () {
            Anthracite.dev.log('::: APP ::: PICKER ::: ROW ::: ONCLICK');
            jGet.class('toolbar-item-filepreview').setAttribute('indigo-preview-button-state', 'selected');
        },
        /**
         * XXX
         * @memberof Anthracite.picker.row
         * @method onMouseOver
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        onMouseOver: function () {
            Anthracite.dev.log('::: APP ::: PICKER ::: ROW ::: MOUSEOVER');

            // Dealing with file manager, possibility of images ( and therefore preview button )
            if (Anthracite.data.HTTP.app == 'manager'
                && (Anthracite.data.HTTP.picker == 'filemanager-anthracite'
                    || Anthracite.data.HTTP.picker == 'repositoryexplorer-anthracite')) {
                var isImage = jGet.node(this).filter('img[src$="/jnt_file_img.png"]').nodes.length;
                // Preview is posible ( dealing with an image)
                if (isImage) {
                    // See if the button has already been added ...
                    if (jGet.node(this).filter('.preview-button').nodes.length == 0) {
                        var previewButton = document.createElement('button');
                        previewButton.classList.add('preview-button');
                        jGet.node(this).prepend(previewButton);
                    }
                }
            }

            // Create and more info button ( if it hasnt aleady been added )
            if (jGet.node(this).filter('.more-info-button').nodes.length == 0) {
                var moreInfoButton = document.createElement('button');
                moreInfoButton.classList.add('more-info-button');
                jGet.node(this).prepend(moreInfoButton);
            }

            // Create and edit button ( If this is a Manager and if it hasnt aleady been added )
            if (Anthracite.data.HTTP.app == 'manager') {
                if (jGet.node(this).filter('.edit-button').nodes.length == 0) {
                    var editButton = document.createElement('button');
                    editButton.classList.add('edit-button');
                    jGet.node(this).prepend(editButton);
                }
            }

            Anthracite.picker.data.currentItem = jGet.node(this).getNode(0);
            Anthracite.picker.data.title = jGet.node(this).filter('.x-grid3-col-name').getHTML();

            if (jGet.node(this).hasClass('x-grid3-row-selected')) {
                jGet.class('toolbar-item-filepreview').setAttribute('indigo-preview-button-state', 'selected');
            } else {
                jGet.class('toolbar-item-filepreview').setAttribute('indigo-preview-button-state', 'unselected');
            }

            jGet.class('toolbar-item-filepreview').setAttribute('indigo-preview-button', 'show');
        },
        /**
        * Callback executed when the user clicks on the more info button
        * @memberof Anthracite.picker.row
        * @method onContext
        * @param e XXX
        * @example
        *
        *
        * Add Example here ...
        *
        *
        */
        onContext: function (e) {
            Anthracite.dev.log('::: APP ::: PICKER ::: ROW ::: ONCONTEXT');
            // if matchClass is passed, then the click is ONLY accepted if the clicked element has that class.
            // if matchClass is not passed then it is accepted.
            var acceptClick = jGet.node(e.target).hasClass('x-tree3-el');

            if (acceptClick) {
                jGet.node(e.target).trigger('contextmenu', e.pageX, e.pageY);
            }
        }
    },
    /**
     * XXX
     * @memberof Anthracite.picker
     * @property {method} onClick XXX
     * @property {method} onMouseOver XXX
     * @property {method} onMouseOut XXX
     * @property {method} onContext XXX
     * @property {method} openPreview XXX
     * @property {method} openEdit Open the edit engine for the currently highlighted thumb in the picker
     * @property {method} closeEdit Callback executed when the Edit Engine is closed from within a picker
     * @namespace Anthracite.picker.thumb
     * @type {object}
     * @example
     *
     *
     * Add Example here ...
     *
     *
     */
    thumb: {
        /**
        * XXX
        * @memberof Anthracite.picker.thumb
        * @method onClick
        * @example
        *
        *
        * Add Example here ...
        *
        *
        */
        onClick: function () {
            Anthracite.dev.log('::: APP ::: PICKER ::: THUMB ::: ONCLICK');
            jGet.class('toolbar-item-filepreview').setAttribute('indigo-preview-button-state', 'selected');
            jGet.node(this).addClass('x-view-over');
        },
        /**
        * XXX
        * @memberof Anthracite.picker.thumb
        * @method onMouseOver
        * @example
        *
        *
        * Add Example here ...
        *
        *
        */
        onMouseOver: function () {
            Anthracite.dev.log('::: APP ::: PICKER ::: THUMB ::: MOUSEOVER');

            // Create and more info button ( if it hasnt aleady been added )
            if (jGet.node(this).filter('.thumb .more-info-button').nodes.length == 0) {
                var moreInfoButton = document.createElement('button');
                moreInfoButton.classList.add('more-info-button');
                jGet.node(this).filter('.thumb').prepend(moreInfoButton);
            }

            // Create and add preview button ( if previews exist and have not aleady been added )
            if (Anthracite.picker.data.enablePreviews) {
                if (jGet.node(this).filter('.thumb .preview-button').nodes.length == 0) {
                    var previewButton = document.createElement('button');
                    previewButton.classList.add('preview-button');
                    jGet.node(this).filter('.thumb').prepend(previewButton);
                }
            }

            // Create and edit button ( If this is a Manager and if it hasnt aleady been added )
            if (Anthracite.data.HTTP.app == 'manager') {
                if (jGet.node(this).filter('.thumb .edit-button').nodes.length == 0) {
                    var editButton = document.createElement('button');
                    editButton.classList.add('edit-button');
                    jGet.node(this).filter('.thumb').prepend(editButton);
                }
            }

            Anthracite.picker.data.currentItem = jGet.node(this).getNode(0);
            Anthracite.picker.data.title = jGet.node(this).getAttribute('id');

            if (!jGet.node(this).hasClass('indigo-force-open')) {
                jGet('.x-view-item-sel.indigo-force-open').removeClass('indigo-force-open');
            }
        },
        /**
        * XXX
        * @memberof Anthracite.picker.thumb
        * @method onMouseOut
        * @example
        *
        *
        * Add Example here ...
        *
        *
        */
        onMouseOut: function () {
            jGet.class('toolbar-item-filepreview').setAttribute('indigo-preview-button', '');
        },
        /**
        * XXX
        * @memberof Anthracite.picker.thumb
        * @method onContext
        * @param e XXX
        * @example
        *
        *
        * Add Example here ...
        *
        *
        */
        onContext: function (e) {
            var that = this;

            jGet.node(this).trigger('contextmenu', e.pageX, e.pageY);
            jGet.node(this).closest('.thumb-wrap').addClass('indigo-force-open');

            jGet('body').onceClose({
                target: '.imagepickerContextMenu',
                callback: function () {
                    jGet.node(that).closest('.thumb-wrap').removeClass('indigo-force-open');
                },
            });
        },
        /**
        * XXX
        * @memberof Anthracite.picker.thumb
        * @method openPreview
        * @example
        *
        *
        * Add Example here ...
        *
        *
        */
        openPreview: function () {
            if (Anthracite.data.HTTP.app == 'manager') {
                jGet.node(this).parent().trigger('dblclick');
            } else {
                jGet('#JahiaGxtManagerToolbar .toolbar-item-filepreview').trigger('click');
            }
        },
        /**
         * Open the edit engine for the currently highlighted thumb in the picker
         * @memberof Anthracite.picker.thumb
         * @method openEdit
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        openEdit: function () {
            jGet.node(this).parent().trigger('contextmenu');

            // When context menu is opened click on the EDIT button
            jGet('body').onceOpen({
                target: '.x-menu',
                callback: function () {
                    // Need to shift the context menu out of view because it doesnt dissappear until the alert has been closed.
                    jGet('.x-menu').css({
                        left: '-50000px'
                    });

                    jGet('.x-menu .toolbar-item-editcontent').trigger('click');
                },
            });
        },
        /**
         * Callback executed when the Edit Engine is closed from within a picker
         * @memberof Anthracite.picker.thumb
         * @method closeEdit
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        closeEdit: function () {
            // Called to close the Edit Engine, either when the user clicks Cancel or Save.
            jGet.getCached('body').setAttribute('data-indigo-edit-engine', '');
        }
    },
    /**
     * XXX
     * @memberof Anthracite.picker
     * @property {method} onMouseOver XXX
     * @property {method} onMouseOut XXX
     * @property {method} onClick XXX
     * @property {method} reposition XXX
     * @namespace Anthracite.picker.previewButton
     * @type {object}
     * @example
     *
     *
     * Add Example here ...
     *
     *
     */
    previewButton: {
        /**
         * XXX
         * @deprecated This can be removed - double check first
         * @memberof Anthracite.picker.previewButton
         * @method onMouseOver
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        onMouseOver: function () {
            Anthracite.dev.log('::: APP ::: PICKER ::: PREVIEWBUTTON ::: ONMOUSEOVER');
            jGet.node(Anthracite.picker.data.currentItem)
                .addClass('x-view-over')
                .addClass('x-grid3-row-over');
        },
        /**
         * XXX
         * @deprecated This can be removed - double check first
         * @memberof Anthracite.picker.previewButton
         * @method onMouseOut
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        onMouseOut: function () {
            Anthracite.dev.log('::: APP ::: PICKER ::: PREVIEWBUTTON ::: ONMOUSEOUT');
            jGet.node(Anthracite.picker.data.currentItem)
                .removeClass('x-view-over')
                .removeClass('x-grid3-row-over');
        },
        /**
         * XXX
         * @deprecated This can be removed - double check first
         * @memberof Anthracite.picker.previewButton
         * @method onClick
         * @param e XXX
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        onClick: function (e) {
            Anthracite.dev.log('::: APP ::: PICKER ::: PREVIEWBUTTON ::: ONCLICK');

            if (e.detail.secondClick) {
                // Just set the good title
                jGet('#JahiaGxtImagePopup .x-window-bwrap').setAttribute('data-file-name', Anthracite.picker.data.title);
            } else {
                // Need to select the currently hovered thumb first ...
                jGet.node(Anthracite.picker.data.currentItem)
                    .trigger('mousedown')
                    .trigger('mouseup');

                if (jGet('#' + Anthracite.picker.data.ID + ' .toolbar-item-filepreview').hasClass('x-item-disabled')) {
                    alert('Preview unavailable');
                } else {
                    // Now need to remove the preview ( just incase it is previewing a previously selected thumb)
                    jGet.id('JahiaGxtImagePopup').remove(); // remove OLD preview

                    // Reclick on the preview button for the newly selected thumb
                    jGet.node(this).customTrigger('click', { secondClick: true });
                }
            }

            jGet.class('toolbar-item-filepreview').setAttribute('indigo-preview-button', 'hide');
        },
        /**
         * XXX
         * @deprecated This can be removed - double check first
         * @memberof Anthracite.picker.previewButton
         * @method reposition
         * @param {HTMLElement} node XXX
         * @param {integer} offset XXX
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        reposition: function (node, offset) {
            Anthracite.dev.log('::: APP ::: PICKER ::: PREVIEWBUTTON ::: REPOSITION');
            offset = offset || {
                left: 0,
                top: 0
            };
            var box = node.getBoundingClientRect();
            var left = box.left;
            var top = box.top;
            var width = box.width;

            jGet('#JahiaGxtManagerToolbar .toolbar-item-filepreview')
                .css({
                    top: (top + (offset.top)) + 'px',
                    left: ((left + width) + offset.left + 5) + 'px'
                })
                .addClass('indigo-show-button');
        }
    },
    /**
     * XXX
     * @memberof Anthracite.picker
     * @property {method} onChange XXX
     * @property {method} onMouseOver XXX
     * @property {method} onMouseOut XXX
     * @property {method} close Callback executed when the source combo is closed
     * @property {method} open XXX
     * @property {method} toggle Callback executed when the source combo opens
     * @namespace Anthracite.picker.source
     * @type {object}
     * @example
     *
     *
     * Add Example here ...
     *
     *
     */
    source: {
        /**
         * XXX
         * @deprecated This can be removed - double check first
         * @memberof Anthracite.picker.source
         * @method XXX
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        onChange: function () {},
        /**
         * XXX
         * @deprecated This can be removed - double check first
         * @memberof Anthracite.picker.source
         * @method onMouseOver
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        onMouseOver: function () {
            Anthracite.dev.log('::: APP ::: PICKER ::: SOURCE ::: ONMOUSEOVER');
            // USER HAS ROLLED OVER THE COMBO TRIGGER
            if (jGet.getCached('body').getAttribute('data-indigo-picker-source-panel') != 'open') {
                jGet('#' + Anthracite.picker.data.ID + ' #JahiaGxtManagerLeftTree .x-panel-header').addClass('indigo-hover');
            }
        },
        /**
         * XXX
         * @deprecated This can be removed - double check first
         * @memberof Anthracite.picker.source
         * @method onMouseOut
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        onMouseOut: function () {
            Anthracite.dev.log('::: APP ::: PICKER ::: SOURCE ::: ONMOUSEOUT');
            // USER HAS ROLLED OUT OF THE COMBO TRIGGER
            jGet('#' + Anthracite.picker.data.ID + ' #JahiaGxtManagerLeftTree .x-panel-header').removeClass('indigo-hover');
        },
        /**
         * Callback executed when the source combo is closed
         * @memberof Anthracite.picker.source
         * @method close
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        close: function () {
            Anthracite.dev.log('::: APP ::: PICKER ::: SOURCE ::: CLOSE');
            // CHANGE SOURCE
            // The user has changed SOURCE, so we just need to hide the combo...
            jGet.getCached('body').setAttribute('data-INDIGO-PICKER-SOURCE-PANEL', '');
        },
        /**
         * XXX
         * @deprecated This can be removed - double check first
         * @memberof Anthracite.picker.source
         * @method open
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        open: function () {},
        /**
         * Callback executed when the source combo opens
         * @memberof Anthracite.picker.source
         * @method toggle
         * @param e XXX
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        toggle: function (e) {
            Anthracite.dev.log('::: APP ::: PICKER ::: SOURCE ::: TOGGLE');
            // USER HAS CLICKED THE COMBO TRIGGER
            e.stopPropagation();

            jGet('#' + Anthracite.picker.data.ID + ' #JahiaGxtManagerLeftTree .x-panel-header').removeClass('indigo-hover');

            jGet.getCached('body').toggleAttribute('data-INDIGO-PICKER-SOURCE-PANEL', ['open', '']);
        }
    },
    /**
     * XXX
     * @memberof Anthracite.picker
     * @property {method} setUpScreen Reorder the screen when the search has been opened in a picker
     * @property {method} setUpSubScreen Set up search screen in a picker opened from within a picker
     * @property {method} open Callback executed when the search is opened
     * @property {method} toggleModificationMenu Callback executed when user clicks on Picker > Search > Publication|Modification|Creation
     * @property {method} updateModificationLabel Callback executed when the user changes the filter by Publication|Modification|Creation
     * @property {method} updateMetaLabel Callback executed when the user clicks on the meta drop down
     * @property {method} close Callback executed when the search is closed
     * @property {method} onContext XXX
     * @namespace Anthracite.picker.search
     * @type {object}
     * @example
     *
     *
     * Add Example here ...
     *
     *
     */
    search: {
        /**
         * Reorder the screen when the search has been opened in a picker
         * @memberof Anthracite.picker.search
         * @method setUpScreen
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        setUpScreen: function () {
            // Save the current display time see we can switch back when closing the search panel
            Anthracite.picker.data.previousDisplayType = Anthracite.picker.data[Anthracite.picker.data.currentDisplayType];

            if(Anthracite.data.HTTP.app == 'manager'){
                // In a picker so go into list mode
                jGet('#' + Anthracite.picker.data.ID + ' .x-panel-tbar .action-bar-tool-item.toolbar-item-listview').trigger('click');
            }

            // Hide the browse panels (GWT does this automatically in Chrome, but not in Firefox - so we have to do it manually)
            jGet.id('CRTbrowseTabItem').addClass('x-hide-display');
            jGet('#CRTsearchTabItem').removeClass('x-hide-display');

            jGet.getCached('body').setAttribute('data-INDIGO-PICKER-SEARCH', 'open');
            jGet.id(Anthracite.picker.data.ID).addClass('search-panel-opened');

            // Ask for class names ...
            var searchField = jGet('#' + Anthracite.picker.data.ID + ' #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(1) td:nth-child(1) input');
            var languageField = jGet('#' + Anthracite.picker.data.ID + ' #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(2) input');
            var fromDate = jGet('#' + Anthracite.picker.data.ID + ' #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(5) input');
            var toDate = jGet('#' + Anthracite.picker.data.ID + ' #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(6) input');
            var dateRange = jGet('#' + Anthracite.picker.data.ID + ' #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(7) input');

            searchField.setAttribute('placeholder', Anthracite.dictionary.get('search').replace('%n%', Anthracite.picker.data.pickerTitle));
            languageField.setAttribute('placeholder', Anthracite.dictionary.get('languageField'));
            fromDate.setAttribute('placeholder', Anthracite.dictionary.get('fromDate'));
            toDate.setAttribute('placeholder', Anthracite.dictionary.get('toDate'));
            dateRange.setAttribute('placeholder', Anthracite.dictionary.get('dateAnyTime'));

            // Callback when user opens Date Range context menu ...
            jGet('#' + Anthracite.picker.data.ID + ' #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(7)')
                .oneClick({
                    target: 'img',
                    callback: function () {
                        var alreadyAddedButtons = jGet('.x-combo-list').hasClass('indigo-buttons');

                        if (!alreadyAddedButtons) {
                            var anyTimeButton = document.createElement('span');
                            var customRangeButton = document.createElement('span');

                            anyTimeButton.innerHTML = Anthracite.dictionary.get('dateAnyTime');
                            anyTimeButton.classList.add('search-anytime-button');
                            anyTimeButton.classList.add('x-combo-list-item');

                            customRangeButton.innerHTML = Anthracite.dictionary.get('dateCustomLabel');
                            customRangeButton.classList.add('search-custom-date-range-button');
                            customRangeButton.classList.add('x-combo-list-item');

                            jGet('.x-combo-list')
                            // Add Two new buttons to the context menu ...
                                .prepend(anyTimeButton)
                                .append(customRangeButton)
                                .addClass('indigo-buttons');
                        }

                        jGet('.x-combo-list')
                            .onMouseDown({
                                target: '.x-combo-list-item',
                                callback: function () {
                                    // Clicked on a Normal Date Filter ( ie. 1 day, 2 days, etc )
                                    jGet.id(Anthracite.picker.data.ID).setAttribute('data-indigo-search-date', 'simple');
                                },
                                uid: "PREDEFINED_DATE_RANGE"
                            })
                            .onMouseDown({
                                target: 'search-custom-date-range-button',
                                callback: function () {
                                    // Clicked on the custom date range button
                                    jGet.id(Anthracite.picker.data.ID).setAttribute('data-indigo-search-date', 'custom');
                                    dateRange.setAttribute('placeholder', Anthracite.dictionary.get('dateCustom'));

                                    // Close the context menu by trigger clicking the page
                                    jGet('#' + Anthracite.picker.data.ID).trigger('mousedown').trigger('mouseup');
                                },
                                uid: "CUSTOM_DATE_RANGE"
                            })
                            .onMouseDown({
                                target: '.search-anytime-button',
                                callback: function () {
                                    // Clicked on Any TIme ( removes times filter )
                                    dateRange.setAttribute('placeholder', Anthracite.dictionary.get('dateAnyTime'));
                                    jGet.id(Anthracite.picker.data.ID).setAttribute('data-indigo-search-date', '');

                                    // Close the context menu by trigger clicking the page
                                    jGet('#' + Anthracite.picker.data.ID).trigger('mousedown').trigger('mouseup');
                                },
                                uid: "ANY_TIME"
                            });
                    },
                    uid: "SEARCH_PANEL_DATE_RANGE_BUTTON"
                });

            jGet.id(Anthracite.picker.data.ID)
            // Listen for changes to meta tags ...
                .onClick({
                    target: '#' + Anthracite.picker.data.ID + ' #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(3)',
                    callback: Anthracite.picker.search.updateMetaLabel,
                    uid: "CHANGE_META_FILTER"
                })
                // Listen for changes to modification type ...
                .onClick({
                    target: '#' + Anthracite.picker.data.ID + ' #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(4) .x-form-check-wrap',
                    callback: Anthracite.picker.search.updateModificationLabel,
                    uid: "CHANGE_MODIFICATION_FILTER"
                })
                // Toggle modification menu when clicking ...
                .onClick({
                    target: '#' + Anthracite.picker.data.ID + ' #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(4) > label',
                    callback: Anthracite.picker.search.toggleModificationMenu,
                    uid: "TOGGLE_MODIFICATION_FILTER"
                });

            // Trigger clicks to initiate the labels of Modification and Meta
            jGet.id('CRTsearchTabItem')
                .filter('.x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(3)')
                .trigger('click');
            jGet.id('CRTsearchTabItem')
                .filter('.x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(4) input[type=radio]:checked')
                .trigger('click');

            // Set width of the side panel
            jGet.id('JahiaGxtManagerLeftTree').nodes[0].style.setProperty('left', '-' + Anthracite.picker.data.explorer.width + 'px', 'important');

            // Set position of display results
            jGet.id('JahiaGxtManagerTobTable').nodes[0].style.setProperty('left', '0px', 'important');
            jGet.id('JahiaGxtManagerTobTable').nodes[0].style.setProperty('width', '100%', 'important');

            Anthracite.picker.repositionSidePanel();
        },
        /**
         * Set up search screen in a picker opened from within a picker
         * @memberof Anthracite.picker.search
         * @method setUpSubScreen
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        setUpSubScreen: function () {
            // Save the current display time see we can switch back when closing the search panel
            Anthracite.picker.data.previousDisplayType = Anthracite.picker.data[Anthracite.picker.data.currentDisplayType];

            // Hide the browse panels (GWT does this automatically in Chrome, but not in Firefox - so we have to do it manually)
            jGet('body > #JahiaGxtContentPickerWindow #CRTbrowseTabItem').addClass('x-hide-display');
            jGet('body > #JahiaGxtContentPickerWindow #CRTsearchTabItem').removeClass('x-hide-display');

            jGet.getCached('body').setAttribute('data-INDIGO-PICKER-SEARCH', 'open');
            jGet('body > #JahiaGxtContentPickerWindow').addClass('search-panel-opened');

            // Ask for class names ...
            var searchField = jGet('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(1) td:nth-child(1) input');
            var languageField = jGet('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(2) input');
            var fromDate = jGet('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(5) input');
            var toDate = jGet('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(6) input');
            var dateRange = jGet('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(7) input');

            searchField.setAttribute('placeholder', Anthracite.dictionary.get('search').replace('%n%', Anthracite.picker.data.subPickerTitle));
            languageField.setAttribute('placeholder', Anthracite.dictionary.get('languageField'));

            fromDate.setAttribute('placeholder', Anthracite.dictionary.get('fromDate'));
            toDate.setAttribute('placeholder', Anthracite.dictionary.get('toDate'));
            dateRange.setAttribute('placeholder', Anthracite.dictionary.get('dateAnyTime'));

            // Callback when user opens Date Range context menu ...
            jGet('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(7)')
                .oneClick({
                    target: 'img',
                    callback: function () {
                        var alreadyAddedSubButtons = jGet('.x-combo-list').hasClass('indigo-sub-buttons');

                        if (!alreadyAddedSubButtons) {
                            var anyTimeButton = document.createElement('span');
                            var customRangeButton = document.createElement('span');

                            anyTimeButton.innerHTML = Anthracite.dictionary.get('dateAnyTime');
                            anyTimeButton.classList.add('search-anytime-button');
                            anyTimeButton.classList.add('x-combo-list-item');

                            customRangeButton.innerHTML = Anthracite.dictionary.get('dateCustomLabel');
                            customRangeButton.classList.add('search-custom-date-range-button');
                            customRangeButton.classList.add('x-combo-list-item');

                            jGet('.x-combo-list')
                            // Add Two new buttons to the context menu ...
                                .prepend(anyTimeButton)
                                .append(customRangeButton)
                                .addClass('indigo-sub-buttons');
                        }

                        jGet('.x-combo-list.indigo-sub-buttons')
                        // Clicked on a Normal Date Filter ( ie. 1 day, 2 days, etc )
                            .onMouseDown({
                                target: '.x-combo-list-item',
                                callback: function () {
                                    jGet('body > #JahiaGxtContentPickerWindow').setAttribute('data-indigo-search-date', 'simple');
                                },
                                uid: "PREDEFINED_DATE_RANGE_SUB"
                            })
                            // Clicked on the custom date range button
                            .onMouseDown({
                                target: '.search-custom-date-range-button',
                                callback: function () {
                                    jGet('body > #JahiaGxtContentPickerWindow').setAttribute('data-indigo-search-date', 'custom');
                                    dateRange.setAttribute('placeholder', Anthracite.dictionary.get('dateCustom'));

                                    // Close the context menu by trigger clicking the page
                                    jGet('body > #JahiaGxtContentPickerWindow').trigger('mousedown').trigger('mouseup');
                                },
                                uid: "CUSTOM_DATE_RANGE_SUB"
                            })
                            .onMouseDown({
                                target: '.search-anytime-button',
                                callback: function () {
                                    // Clicked on Any TIme ( removes times filter )
                                    dateRange.setAttribute('placeholder', Anthracite.dictionary.get('dateAnyTime'));
                                    jGet('body > #JahiaGxtContentPickerWindow').setAttribute('data-indigo-search-date', '');

                                    // Close the context menu by trigger clicking the page
                                    jGet('body > #JahiaGxtContentPickerWindow').trigger('mousedown').trigger('mouseup');
                                },
                                uid: "ANY_TIME_SUB"
                            });
                    },
                    uid: "SEARCH_PANEL_DATE_RANGE_BUTTON_SUB"
                });

            jGet('body > #JahiaGxtContentPickerWindow')
            // Listen for changes to meta tags ...
                .onClick({
                    target: 'body > #JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(3)',
                    callback: Anthracite.picker.search.updateMetaLabel,
                    uid: "CHANGE_META_FILTER_SUB"
                })

                // Listen for changes to modification type ...
                .onClick({
                    target: 'body > #JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(4) .x-form-check-wrap',
                    callback: Anthracite.picker.search.updateModificationLabel,
                    uid: "CHANGE_MODIFICATION_FILTER_SUB"
                })

                // Toggle modification menu when clicking ...
                .onClick({
                    target: 'body > #JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree #CRTsearchTabItem .x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(4) > label',
                    callback: Anthracite.picker.search.toggleModificationMenu,
                    uid: "TOGGLE_MODIFICATION_FILTER_SUB"
                });

            // Trigger clicks to initiate the labels of Modification and Meta
            jGet('body > #JahiaGxtContentPickerWindow #CRTsearchTabItem')
                .filter('.x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(3)')
                .trigger('click');
            jGet('body > #JahiaGxtContentPickerWindow #CRTsearchTabItem')
                .filter('.x-panel-mc > .x-panel-body > .x-component:nth-child(2) fieldset .x-form-item:nth-child(4) input[type=radio]:checked')
                .trigger('click');

            // Set width of the side panel
            jGet('.modal-imagepicker #JahiaGxtManagerLeftTree').nodes[0].style.setProperty('left', '-' + Anthracite.picker.data.explorer.width + 'px', 'important');

            // Set position of display results
            jGet('.modal-imagepicker #JahiaGxtManagerTobTable').nodes[0].style.setProperty('left', '0px', 'important');
            jGet('.modal-imagepicker #JahiaGxtManagerTobTable').nodes[0].style.setProperty('width', '100%', 'important');

        },
        /**
         * Callback executed when the search is opened
         * @memberof Anthracite.picker.search
         * @method open
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        open: function () {
            Anthracite.dev.log('::: APP ::: PICKER ::: SEARCH ::: OPEN');

            var searchTabAvailable;
            if (jGet.getCached('body').getAttribute('data-INDIGO-SUB-PICKER') == 'open') {
                searchTabAvailable = jGet('body > #JahiaGxtContentPickerWindow #CRTsearchTabItem').exists();

                if (searchTabAvailable) {
                    Anthracite.picker.search.setUpSubScreen();
                } else {
                    jGet('body > #JahiaGxtContentPickerWindow').onOpen({
                        target: '#CRTsearchTabItem',
                        callback: Anthracite.picker.search.setUpSubScreen,
                    });
                }
            } else {
                // OPEN SEARCH PANEL
                searchTabAvailable = jGet.id('CRTsearchTabItem').exists();

                if (searchTabAvailable) {
                    Anthracite.picker.search.setUpScreen();
                } else {
                    jGet.tag('body').onOpen({
                        target: '#CRTsearchTabItem',
                        callback: Anthracite.picker.search.setUpScreen,
                    });
                }
            }
        },
        /**
         * Callback executed when user clicks on Picker > Search > Publication|Modification|Creation
         * @memberof Anthracite.picker.search
         * @method toggleModificationMenu
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        toggleModificationMenu: function () {
            var menu = jGet.node(this).parent();
            menu.toggleClass('indigo-show-menu');
        },
        /**
         * Callback executed when the user changes the filter by Publication|Modification|Creation
         *  - Updates the text displayed in the combo according to what has been selected
         * @memberof Anthracite.picker.search
         * @method updateModificationLabel
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        updateModificationLabel: function () {
            var dateType = jGet.node(this).filter('label').getHTML();
            var label = Anthracite.dictionary.get('dateType').replace('%n%', dateType);

            jGet.node(this).closest('.x-form-item')
                .removeClass('indigo-show-menu')
                .setAttribute('data-indigo-modification-label', label);
        },
        /**
         * Callback executed when the user clicks on the meta drop down
         *  - Changes the text displayed in the combo according to selection
         * @memberof Anthracite.picker.search
         * @method updateMetaLabel
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        updateMetaLabel: function () {
            var checkboxes = jGet.node(this).filter('.x-form-check-wrap:not(.x-hide-display) input[type=\'checkbox\']');
            var selMeta = [];
            var checkboxCount = checkboxes.nodes.length;
            var metaMenuLabel;

            checkboxes.each(function () {
                var checkboxLabel = this.nextSibling.innerHTML;

                if (this.checked == true) {
                    // Its checked, so add to string ...
                    selMeta.push(checkboxLabel);
                }
            });

            if (selMeta.length == checkboxCount) {
                // ALL meta data
                metaMenuLabel = Anthracite.dictionary.get('allMetadata');
            } else if (selMeta.length == 0) {
                metaMenuLabel = Anthracite.dictionary.get('ignoreMetadata');
            } else {
                metaMenuLabel = Anthracite.dictionary.get('metaLabel').replace('%n%', selMeta.join(', '));
            }

            jGet.node(this).setAttribute('data-indigo-meta-label', metaMenuLabel);
        },
        /**
         * Callback executed when the search is closed
         * @memberof Anthracite.picker.search
         * @method close
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        close: function () {
            Anthracite.dev.log('::: APP ::: PICKER ::: SEARCH ::: CLOSE');

            // CLOSE SEARCH PANEL
            jGet.getCached('body').setAttribute('data-INDIGO-PICKER-SEARCH', '');

            // Hide the search panel
            jGet('body > #JahiaGxtContentPickerWindow').removeClass('search-panel-opened');

            jGet('body > #JahiaGxtContentPickerWindow #CRTsearchTabItem').addClass('x-hide-display');

            // Display the BROWSE panels
            jGet('body > #JahiaGxtContentPickerWindow #JahiaGxtManagerLeftTree .x-tab-panel-body > div:nth-child(1)')
                .removeClass('x-hide-display');

            // Hide the search panel
            jGet.id(Anthracite.picker.data.ID).removeClass('search-panel-opened');

            jGet.id('CRTsearchTabItem').addClass('x-hide-display');

            // Display the BROWSE panels
            jGet('#' + Anthracite.picker.data.ID + ' #JahiaGxtManagerLeftTree .x-tab-panel-body > div:nth-child(1)')
                .removeClass('x-hide-display');

            Anthracite.picker.repositionSidePanel();
        },
        /**
         * XXX
         * @deprecated This can be removed - double check first
         * @memberof Anthracite.picker.search
         * @param e XXX
         * @method onContext
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        onContext: function (e) {
            Anthracite.dev.log('::: APP ::: PICKER ::: SEARCH ::: ONCONTEXT');
            // Open Context Menu when clicking "More" button.
            jGet.node(e.target).trigger('contextmenu', e.pageX, e.pageY);
        }
    }
});
