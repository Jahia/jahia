// EVENT LISTENERS
var DX_eventListeners = {
    /**
     * Attaches the majority of event listeners needed to make Anthracite work
     *  - All event listeners are attached to the body as the vast majority of nodes are not yet available to attach listeners to
     */
    attach: function () {
        var app = DX_app;
        DexV2('body')
        /**
         * Target: Dialog
         * Callback: => app.dialog.onOpen
         */
            .onOpen('.x-window-plain.x-window-dlg', app.dialog.onOpen)
            /**
             * Target: Admin > Side Panel > System Site Settings Label
             * Callback: => app.admin.sidepanel.toggleSiteSettingsMenu
             */
            .onClick('.tab_systemSiteSettings > .x-panel', app.admin.sidepanel.toggleSiteSettingsMenu)
            /**
             * Target: body
             * Attribute: data-sitekey
             * Callback: => app.common.resizeSiteSelector
             */
            .onAttribute('body', 'data-sitekey', app.edit.onNewSite)
            /**
             * Target: body
             * Attribute: data-langdisplayname
             * Callback: => app.edit.resizeLanguageInput
             */
            .onAttribute('body', 'data-lang', app.edit.resizeLanguageInput)
            .onAttribute('body', 'data-sitekey', app.edit.resizeLanguageInput)
            /**
             * Target: Splitter bar in pickers
             * Attribute: style
             * Callback: => app.picker.onResize
             */
            .onAttribute('#JahiaGxtContentPickerWindow .x-vsplitbar, #contentpicker .x-vsplitbar, #contentmanager .x-vsplitbar, #JahiaGxtContentPicker .x-vsplitbar', 'style', app.picker.onResize)
            /**
             * Target: Modals
             * Callback: => app.modals.onOpen
             */
            .onOpen('.x-window', app.modals.onOpen)
            /**
             * Target: Modals
             * Callback: => app.modals.onClose
             */
            .onClose('.x-window', app.modals.onClose)
            /**
             * Target: Edit Mode > Header > Publish Page / Publish Selection (arrow)
             * Callback: => app.edit.topbar.publicationButtonArrow.onMouseOver
             */
            .onMouseOver('.edit-menu-publication', app.edit.topbar.publicationButtonArrow.onMouseOver)
            /**
             * Target: Edit Mode > Header > Publish Page / Publish Selection (arrow)
             * Callback: app.edit.topbar.publicationButtonArrow.onMouseOut
             */
            .onMouseOut('.edit-menu-publication', app.edit.topbar.publicationButtonArrow.onMouseOut)
            /**
             * Target: Edit Mode > Header > Preview (arrow)
             * Callback: app.edit.topbar.previewButtonArrow.onMouseOver
             */
            .onMouseOver('.edit-menu-view', app.edit.topbar.previewButtonArrow.onMouseOver)
            /**
             * Target: Edit Mode > Header > Preview (Arrow)
             * Callback: => app.edit.topbar.previewButtonArrow.onMouseOut
             */
            .onMouseOut('.edit-menu-view', app.edit.topbar.previewButtonArrow.onMouseOut)
            /**
             * Target: Edit Mode > Header > Preview
             * Callback: => app.edit.topbar.previewButtonContainer.onMouseOver
             */
            .onMouseOver('.toolbar-item-preview', app.edit.topbar.previewButtonContainer.onMouseOver)
            /**
             * Target: Edit Mode > Header > Preview
             * Callback: app.edit.topbar.previewButtonContainer.onMouseOut
             */
            .onMouseOut('.toolbar-item-preview', app.edit.topbar.previewButtonContainer.onMouseOut)
            /**
             * Target: Edit Mode > Header > Publish Page / Publish Selection
             * Callback: app.edit.topbar.publicationButtonContainer.onMouseOver
             */
            .onMouseOver('.toolbar-item-publishone', app.edit.topbar.publicationButtonContainer.onMouseOver)
            /**
             * Target: Edit Mode > Header > Publish Page / Publish Selection
             * Callback: app.edit.topbar.publicationButtonContainer.onMouseOut
             */
            .onMouseOut('.toolbar-item-publishone', app.edit.topbar.publicationButtonContainer.onMouseOut)
            /**
             * Target: Edit Mode > Side Panel > Toggle Pin
             * Callback: Toggle pinning of the Side Panel
             */
            .onClick('.window-side-panel > .x-panel-bwrap > div:nth-child(2).x-panel-footer', app.edit.sidepanel.togglePin)
            /**
             * Target: Edit Mode > Background Jobs Modal
             * Callback: app.backgroundJobs.onOpen
             */
            .onOpen('.job-list-window', app.backgroundJobs.onOpen)
            /**
             * DO LATER - NOT EVEN SURE IF THIS IS STILL IN USE
             */
            .onMouseDown('.toolbar-item-studio', function () {
                var studioNodePath = sessionStorage.getItem('studiomode_nodePath');
                var baseURL = jahiaGWTParameters.contextPath + '/cms/studio/default/' + jahiaGWTParameters.uilang;

                var studioURL;
                if (studioNodePath && studioNodePath !== '/settings') {
                    studioURL = baseURL + studioNodePath + '.html';
                } else {
                    studioURL = baseURL + '/settings.manageModules.html';
                }
                window.location = studioURL;
            })
            /**
             * Target: Edit Mode => Add Content Modal => Filter Input
             * Callback: => app.edit.addPlaceholderToContentFilter
             */
            .onOpen('.content-type-window .x-form-field-wrap input', app.edit.addPlaceholderToContentFilter)
            /**
             * DELETE
             */
            .onClick('.toolbar-item-newfolder', function () {
                // Add new folder
                var isDisabled = DexV2.node(this).hasClass('x-item-disabled');
                if (isDisabled) {
                    // User has clicked on the ADD FOLDER icon whilst in the RIGHT tree, so trigger click on the CONTEXT MENU OF THE LEFT TREE
                    var selectedFolder = DexV2('#CRTbrowseTabItem .x-grid3-row-selected');
                    if (selectedFolder.nodes.length > 0) {
                        selectedFolder.trigger('contextmenu');
                    } else {
                        DexV2('#CRTbrowseTabItem .x-grid3-row')
                            .first() // Get first row
                            .trigger('mousedown') // Select it with a mouse down
                            .trigger('mouseup') // Release the mousedown
                            .trigger('contextmenu'); // Trigger a right click
                    }

                    // When context menu is opened click on the ADD FOLDER button
                    DexV2('body').onceOpen('.x-menu', function () {
                        // Need to shift the context menu out of view because it doesnt dissappear until the alert has been closed.
                        DexV2('.x-menu').css({
                            left: '-50000px'
                        });
                        DexV2('.x-menu .toolbar-item-newfolder').trigger('click');
                    });
                }
            })
            /**
             * DELETE
             */
            .onClick('.toolbar-item-newcontentfolder', function () {
                // Add new folder
                var isDisabled = DexV2.node(this).hasClass('x-item-disabled');
                if (isDisabled) {
                    // User has clicked on the ADD FOLDER icon whilst in the RIGHT tree, so trigger click on the CONTEXT MENU OF THE LEFT TREE
                    var selectedFolder = DexV2('#CRTbrowseTabItem .x-grid3-row-selected');
                    if (selectedFolder.nodes.length > 0) {
                        selectedFolder.trigger('contextmenu');
                    } else {
                        DexV2('#CRTbrowseTabItem .x-grid3-row')
                            .first() // Get first row
                            .trigger('mousedown') // Select it with a mouse down
                            .trigger('mouseup') // Release the mousedown
                            .trigger('contextmenu'); // Trigger a right click
                    }

                    // When context menu is opened click on the ADD FOLDER button
                    DexV2('body').onceOpen('.x-menu', function () {
                        DexV2('.x-menu .toolbar-item-newcontentfolder').trigger('click');
                    });
                }

            })
            /**
             * DELETE
             */
            .onClick('.toolbar-item-newpage', function () {
                // Add new folder
                var isDisabled = DexV2.node(this).hasClass('x-item-disabled');
                if (isDisabled) {
                    // User has clicked on the ADD FOLDER icon whilst in the RIGHT tree, so trigger click on the CONTEXT MENU OF THE LEFT TREE
                    var selectedFolder = DexV2('#CRTbrowseTabItem .x-grid3-row-selected');
                    if (selectedFolder.nodes.length > 0) {
                        selectedFolder.trigger('contextmenu');
                    } else {
                        DexV2('#CRTbrowseTabItem .x-grid3-row')
                            .first() // Get first row
                            .trigger('mousedown') // Select it with a mouse down
                            .trigger('mouseup') // Release the mousedown
                            .trigger('contextmenu'); // Trigger a right click
                    }

                    // When context menu is opened click on the ADD FOLDER button
                    DexV2('body').onceOpen('.x-menu', function () {
                        DexV2('.x-menu .toolbar-item-newpage').trigger('click');
                    });
                }
            })
            /**
             * DELETE
             */
            .onClick('.toolbar-item-upload', function () {
                // Add new folder
                var isDisabled = DexV2.node(this).hasClass('x-item-disabled');
                if (isDisabled) {
                    // User has clicked on the ADD FOLDER icon whilst in the RIGHT tree, so trigger click on the CONTEXT MENU OF THE LEFT TREE
                    var selectedFolder = DexV2('#CRTbrowseTabItem .x-grid3-row-selected');
                    if (selectedFolder.nodes.length > 0) {
                        selectedFolder.trigger('contextmenu');
                    } else {
                        DexV2('#CRTbrowseTabItem .x-grid3-row')
                            .first() // Get first row
                            .trigger('mousedown') // Select it with a mouse down
                            .trigger('mouseup') // Release the mousedown
                            .trigger('contextmenu'); // Trigger a right click
                    }

                    // When context menu is opened click on the ADD FOLDER button
                    DexV2('body').onceOpen('.x-menu', function () {
                        DexV2('.x-menu .toolbar-item-upload').trigger('click');
                    });
                }
            })
            /**
             * DELETE
             */
            .onClick('.toolbar-item-newcontent', function () {
                // Add new content folder
                var isDisabled = DexV2.node(this).hasClass('x-item-disabled');
                if (isDisabled) {
                    // User has clicked on the ADD FOLDER icon whilst in the RIGHT tree, so trigger click on the CONTEXT MENU OF THE LEFT TREE
                    var selectedFolder = DexV2('#CRTbrowseTabItem .x-grid3-row-selected');
                    if (selectedFolder.nodes.length > 0) {
                        selectedFolder.trigger('contextmenu');
                    } else {
                        DexV2('#CRTbrowseTabItem .x-grid3-row')
                            .first() // Get first row
                            .trigger('mousedown') // Select it with a mouse down
                            .trigger('mouseup') // Release the mousedown
                            .trigger('contextmenu'); // Trigger a right click
                    }

                    // When context menu is opened click on the ADD FOLDER button
                    DexV2('body').onceOpen('.x-menu', function () {
                        DexV2('.x-menu .toolbar-item-newcontent').trigger('click');
                    });
                }
            })
            /**
             * Target: Body
             * Attribute: data-singleselection-node-path
             * Callback: app.onChangeNodePath
             */
            .onAttribute('body', 'data-singleselection-node-path', app.onChangeNodePath)
            /**
             * Target: Edit Engine > Tabs > Workflow
             * Callback: app.engine.onOpenWorkflow
             */
            .onOpen('#JahiaGxtEditEnginePanel-workflow > div > div:nth-child(1) .x-grid-panel', app.engine.onOpenWorkflow)
            /**
             * Target: Edit Engine > Tabs > History
             * Callback: app.engine.onOpenHistory
             */
            .onOpen('#JahiaGxtEditEnginePanel-history', app.engine.onOpenHistory)
            /**
             * Target: Edit Engine > Live Roles | Edit Roles > Select Users Modal
             * Callback: app.pickers.users.onOpen
             */
            .onOpen('#JahiaGxtUserGroupSelect', app.pickers.users.onOpen)
            /**
             * Target: Edit Mode > Side Panel > Content > Browse Tree
             * Callback: app.edit.sidepanel.tabs.contentTab.onOpen
             */
            .onOpen('#JahiaGxtContentBrowseTab', app.edit.sidepanel.tabs.contentTab.onOpen)
            /**
             * Target: Edit Mode > Side Panel > Files > Browse Tree
             * Callback: => app.edit.sidepanel.tabs.filesTab.onOpen
             */
            .onOpen('#JahiaGxtFileImagesBrowseTab', app.edit.sidepanel.tabs.filesTab.onOpen)
            /**
             * Target: Edit Mode > Side Panel > Category > Browse Tree
             * Callback: Add Class to tree to create the sub tree drawer
             */
            .onOpen('#JahiaGxtCategoryBrowseTab', app.edit.sidepanel.tabs.categoryTab.onOpen)
            /**
             * Target: Edit Mode > Side Panel > [Create | Content | Files | Search] > Drawer without results
             * Callback: => app.edit.sidepanel.resultsPanel.onEmpty
             */
            .onOpen('.results-column .x-grid-empty', app.edit.sidepanel.resultsPanel.onEmpty)
            /**
             * Target: Edit Mode > Side Panel > [Create | Content | Files | Search] > Drawer with Results
             * Callback: => app.edit.sidepanel.resultsPanel.onResults
             */
            .onOpen('.results-column .x-grid3-row', app.edit.sidepanel.resultsPanel.onResults)
            /**
             * Target: Edit Mode > Side Panel > Filter input
             * Callback: => app.edit.sidepanel.tabs.createContent.onOpen
             */
            .onOpen('#JahiaGxtCreateContentTab', app.edit.sidepanel.tabs.createContent.onOpen)
            /**
             * Target: Edit Mode > Site Settings > Navigation
             * Callback: app.edit.settings.onTreeChange
             */
            .onGroupOpen('#JahiaGxtSettingsTab .x-grid3-row', app.edit.settings.onTreeChange) // Once matchType is improved the target selector can be changed to #JahiaGxtSettingsTab .x-grid3-row
            /**
             * Target: Empty table
             * Callback: app.edit.sidepanel.browseTree.onEmpty
             */
            .onOpen('.x-grid-empty', app.edit.sidepanel.browseTree.onEmpty)
            /**
             * Target: Edit Mode > Side Panel > Search
             * Callback: => app.edit.sidepanel.tabs.searchTab.onOpen
             */
            .onOpen('#JahiaGxtSearchTab', app.edit.sidepanel.tabs.searchTab.onOpen)
            /**
             * Target: Edit Mode > Side Panel > [Content | Files | Search] > Results Column
             *  - Actually targets ALL grid-3 rows, but the callback determines if the row is in the Results Column and only executes if its the case
             *  Callback: app.edit.sidepanel.browseTree.onResults
             */
            .onOpen('.x-grid3-row', app.edit.sidepanel.browseTree.onResults)
            /**
             * Target: Edit Mode > Side Panel > Files > Thumb Panel > Thumb
             * Callback: app.edit.sidepanel.thumbPanel.onClose
             */
            .onClose('.thumb-wrap', app.edit.sidepanel.thumbPanel.onClose)
            /**
             * Target: Edit Mode > Side Panel > Files > Thumb Panel > Thumb
             * Callback: app.edit.sidepanel.thumbPanel.onOpen
             */
            .onOpen('.thumb-wrap', app.edit.sidepanel.thumbPanel.onOpen)
            /**
             * Target: Edit Mode > Header > Workflow > Open Dashboard Button
             * Callback: app.edit.infoBar.tasks.updateMenuLabel.
             */
            .onOpen('.menu-edit-menu-workflow', app.edit.infoBar.tasks.updateMenuLabel)
            /**
             * Target: Contribute Mode > Header > Workflow > Open Dashboard Button
             * Callback: app.edit.infoBar.tasks.updateMenuLabel
             */
            .onOpen('.menu-contribute-menu-workflow', app.edit.infoBar.tasks.updateMenuLabel)
            /**
             * Target: Edit Mode > Top Bar > Preview Drop Down
             * Callback: app.contextMenus.previewMenu.onOpen
             */
            .onOpen('.menu-edit-menu-view', app.contextMenus.previewMenu.onOpen)
            /**
             * Target: Edit Mode > Top Bar > Publication Drop Down
             * Callback: app.contextMenus.publicationMenu.onOpen
             */
            .onOpen('.menu-edit-menu-publication', app.contextMenus.publicationMenu.onOpen)
            /**
             * Target: Edit Mode > More Info Context Menu
             * Callback: app.contextMenus.moreInfoMenu.onOpen
             */
            .onOpen('.menu-edit-menu-edit', app.contextMenus.moreInfoMenu.onOpen)
            /**
             * Target: Edit Mode > Top Bar > More Info
             * Callback: app.contextMenus.moreInfoMenu.onOpen
             */
            .onOpen('.editModeContextMenu', app.contextMenus.moreInfoMenu.onOpen)
            /**
             * Target: All Apps > Blue Menu
             * Callback: app.contextMenus.managerMenu.onOpen
             */
            .onOpen('.menu-editmode-managers-menu', app.contextMenus.managerMenu.onOpen)
            /**
             * Target: All Apps > Blue Menu
             * Callback: app.contextMenus.managerMenu.onOpen
             */
            .onClose('.menu-editmode-managers-menu', app.contextMenus.managerMenu.onClose)
            /**
             * Target: All Pickers
             * Callback: app.picker.onOpen
             */
            .onOpen('#' + app.picker.data.ID, app.picker.onOpen)
            /**
             * Target: Edit Engine
             * Callback: app.engine.onOpen
             */
            .onOpen('.engine-window, .engine-panel', app.engine.onOpen)
            /**
             * Target: Image Preview
             * Callback: app.imagePreview.onOpen
             */
            .onOpen('#JahiaGxtImagePopup', app.imagePreview.onOpen)
            /**
             * Target: Edit Mode > Task Info
             * Callback: app.edit.infoBar.tasks.onChange
             */
            .onAttribute('.edit-menu-tasks', 'class', app.edit.infoBar.tasks.onChange)
            /**
             * Target: Contribute Mode > Task Info
             * Callback: app.edit.infoBar.tasks.onChange
             */
            .onAttribute('.contribute-menu-tasks', 'class', app.edit.infoBar.tasks.onChange)
            /**
             * TO DO
             */
            .onAttribute('.toolbar-item-workinprogressadmin, .toolbar-item-workinprogress', 'class', app.edit.infoBar.jobs.onChange)
            /**
             * DELETE - ?
             */
            .onOpen('.x-dd-drag-proxy', app.edit.sidepanel.onDrag.bind(this, true))
            /**
             * DELETE - ?
             */
            .onClose('.x-dd-drag-proxy', app.edit.sidepanel.onDrag.bind(this, false))
            /**
             * Target: Body
             * Attribute data-sitesettings
             * Callback: app.edit.settings.onChange
             */
            .onAttribute('body', 'data-sitesettings', app.edit.settings.onChange)
            /**
             * Target: Body
             * Attribute data-selection-count
             * Callback: app.iframe.onSelect
             */
            .onAttribute('body', 'data-selection-count', app.iframe.onSelect)
            /**
             * Target: Body
             * Attribute data-main-node-displayname
             * Callback: app.iframe.onChange
             */
            .onAttribute('body', 'data-main-node-displayname', app.iframe.onChange)
            /**
             * Target: Body
             * Attribute data-main-node-path
             * Callback: app.contribute.onChangeMode
             */
            .onAttribute('body', 'data-main-node-path', app.contribute.onChangeMode)
            /**
             * Target: .window-iframe
             * Attribute: src
             * Callback: app.iframe.onChangeSRC
             */
            .onAttribute('.window-iframe', 'src', app.iframe.onChangeSRC)
            /**
             * Target: .x-jahia-root
             * Attribute: class
             * Callback: app.onChange
             */
            .onAttribute('.x-jahia-root', 'class', app.onChange)
            /**
             * Target: Picker
             * Callback: app.picker.onClose
             */
            .onClose('#' + app.picker.data.ID, app.picker.onClose)
            /**
             * Target: Picker
             * Callback: app.picker.onClose
             */
            .onClose('#JahiaGxtContentPickerWindow', app.picker.onClose)
            /**
             * Target: Edit Engine
             * Callback: app.engine.onClose
             */
            .onClose('#JahiaGxtEnginePanel, #JahiaGxtEngineWindow', app.engine.onClose)
            /**
             * Target: Image Preview
             * Callback: app.imagePreview.onClose
             */
            .onClose('#JahiaGxtImagePopup', app.imagePreview.onClose)
            /**
             * Target: Workflow Dashboard
             * Callback: app.workflow.dashboard.onOpen
             */
            .onOpen('.workflow-dashboard-engine', app.workflow.dashboard.onOpen)
            /**
             * Target: All Apps > App Container
             * Callback: app.onClick
             */
            .onClick('.app-container', app.onClick)
            /**
             * Target: Edit Mode > App Container
             * Callback: app.picker.previewButton.onClick
             */
            .onClick('.toolbar-item-filepreview', app.picker.previewButton.onClick)
            /**
             * Target: Picker > File List > Row
             * Callback: app.picker.row.onClick
             */
            .onClick('#JahiaGxtManagerLeftTree + div .x-grid3 .x-grid3-row', app.picker.row.onClick)
            /**
             * Target: Admin Mode > Side Panel > Navigation Item
             * Callback: app.admin.sidepanel.row.onClick
             */
            .onClick('.x-viewport-adminmode .x-grid3 .x-grid3-row', app.admin.sidepanel.row.onClick)
            /**
             * Target: Edit Engine > Visibility Tab > Condition Builder > Close / Create / Save
             * Callback: => app.engine.closeConditionEditor
             */
            .onClick('#JahiaGxtEditEnginePanel-visibility > .x-component:nth-child(3) .x-panel-footer', app.engine.closeConditionEditor)
            /**
             * Target: Edit Engine > Visibility Tab > Edit Condition
             * Callback: => app.engine.editCondition
             */
            .onClick('#JahiaGxtEditEnginePanel-visibility > .x-component:nth-child(2) .x-grid3-row', app.engine.editCondition)
            /**
             * Target: Edit Engine > Visibility Tab > Add Condition
             * Callback: => app.engine.addCondition
             */
            .onClick('#JahiaGxtEditEnginePanel-visibility > .x-component:nth-child(1) > .x-component:nth-child(2) td:nth-child(5) > table', app.engine.addCondition)
            /**
             * Target: Edit Engine > Visibility Tab > Add Condition
             * Callback: => app.engine.openConditionsMenu
             */
            .onMouseDown('#JahiaGxtEditEnginePanel-visibility > .x-component:nth-child(1) img.x-form-trigger', app.engine.openConditionsMenu)
            /**
             * DELETE - ?
             */
            .onClick('.x-grid3-row .x-grid3-td-size', app.picker.search.onContext)
            /**
             * DELETE - ?
             */
            .onClick('.x-grid3-row .x-tree3-el', app.picker.row.onContext)
            /**
             * DELETE - ?
             */
            .onMouseDown('#' + app.picker.data.ID + ' .cancel-edit', app.picker.thumb.closeEdit)
            /**
             * Target: Picker > Row[hover] > Edit Button
             * Callback: => app.picker.thumb.openEdit
             */
            .onMouseDown('#' + app.picker.data.ID + ' .edit-button', app.picker.thumb.openEdit)
            /**
             * Target: Picker > Row[hover] > More Info Button
             * Callback: => app.picker.thumb.onContext
             */
            .onMouseDown('#' + app.picker.data.ID + ' .more-info-button', app.picker.thumb.onContext)
            /**
             * Target: Picker > Row[hover] > Preview Button
             * Callback: => app.picker.thumb.openPreview
             */
            .onMouseDown('#' + app.picker.data.ID + ' .preview-button', app.picker.thumb.openPreview)
            /**
             * Target: Picker > Thumb View > Thumb
             * Callback: app.picker.thumb.onClick
             */
            .onClick('#JahiaGxtManagerLeftTree + div .thumb-wrap', app.picker.thumb.onClick)
            /**
             * DELETE - ?
             */
            .onClick('#JahiaGxtSidePanelTabs > .x-tab-panel-header .x-tab-strip-spacer', app.edit.settings.close)
            /**
             * DELETE - ?
             */
            .onMouseOver('.toolbar-item-filepreview', app.picker.previewButton.onMouseOver)
            /**
             * DELETE - ?
             */
            .onMouseOut('.toolbar-item-filepreview', app.picker.previewButton.onMouseOut)
            /**
             * Target: Trees > Toggle Child Nodes button
             * Callback: app.common.trees.onToggleChildNodes
             */
            .onMouseDown('.x-tree3-node-joint', app.common.trees.onToggleChildNodes)
            /**
             * Target: All Apps > Blue Menu
             * Callback: => app.contextMenus.managerMenu.onClose
             */
            .onMouseDown('.menu-edit-menu-mode', app.contextMenus.managerMenu.onClose)
            /**
             * DELETE - ?
             */
            .onMouseDown('.menu-edit-menu-user', app.contextMenus.managerMenu.onClose)
            /**
             * Target: Edit Mode > Side Panel Tabs > Tree > Row
             * Callback: => app.edit.sidepanel.row.onContext
             */
            .onClick('#JahiaGxtSidePanelTabs .x-grid3-td-displayName', app.edit.sidepanel.row.onContext)
            /**
             * Target: Pickers
             * Callback: app.picker.onClick
             */
            .onClick('#' + app.picker.data.ID, app.picker.onClick)
            /**
             * Target: Pickers > List View Button
             * Callback: app.picker.onListView
             */
            .onClick('#' + app.picker.data.ID + ' .x-panel-tbar .action-bar-tool-item.toolbar-item-listview', app.picker.onListView)
            /**
             * Target: Pickers > Thumb View Button
             * Callback: app.picker.onThumbView
             */
            .onClick('#' + app.picker.data.ID + ' .x-panel-tbar .action-bar-tool-item.toolbar-item-thumbsview', app.picker.onThumbView)
            /**
             * Target: Pickers > Details View Button
             * Callback: app.picker.onDetailView
             */
            .onClick('#' + app.picker.data.ID + ' .x-panel-tbar .action-bar-tool-item.toolbar-item-detailedview', app.picker.onDetailView)
            /**
             * Target: Pickers > List View Button
             * Callback: app.picker.onListView
             */
            .onMouseDown('.toolbar-item-listview', app.picker.onListView)
            /**
             * Target: Pickers > Thumb View Button
             * Callback: app.picker.onThumbView
             */
            .onMouseDown('.toolbar-item-thumbsview', app.picker.onThumbView)
            /**
             * Target: Pickers > Details View Button
             * Callback: app.picker.onDetailView
             */
            .onMouseDown('.toolbar-item-detailedview', app.picker.onDetailView)
            /**
             * DELETE - ?
             */
            .onMouseUp('#contentmanager .x-panel-bbar .x-toolbar-cell:nth-child(1) .x-btn', app.picker.thumb.closeEdit)
            /**
             * Target: Edit Mode (with selected node) > Clear Selection Button
             * Callback: app.iframe.clearSelection
             */
            .onClick('.node-path-title', app.iframe.clearSelection)
            /**
             * Target: Left menu > Submenu > Entries
             * Callback: app.edit.onNav
             */
            .onMouseDown('.x-viewport-editmode #JahiaGxtSidePanelTabs .x-grid3-row', app.edit.onNav)
            /**
             * Target:  Manager > search
             * Callback: app.edit.onNav
             */
            .onMouseDown('#JahiaGxtManagerLeftTree__CRTbrowseTabItem', app.picker.search.close)
            /**
             * Target: Manager > Search > Panel open
             * Callback: app.picker.search.close
             */
            .onClose('.indigo-picker-multi-select', function () {
                if (DexV2.class('search-panel-opened').exists()) {
                    DexV2.id('JahiaGxtManagerLeftTree__CRTbrowseTabItem').trigger('click');

                    DexV2.class('search-panel-opened').removeClass('search-panel-opened');
                }
            })
            /**
             * Target: Manager > Left menu > search button
             * Callback: app.picker.search.open
             */
            .onMouseUp('#JahiaGxtManagerLeftTree__CRTsearchTabItem', app.picker.search.open)
            /**
             * Target: Managers > Left Tree > Header (left arrow and the bar)
             * Callback: app.picker.source.close
             */
            .onClick('#' + app.picker.data.ID + ' #JahiaGxtManagerLeftTree .x-panel-header', app.picker.source.close)
            /**
             * Target: Managers > Left Tree > Header (left arrow and the bar)
             * Callback: app.picker.source.toggle
             */
            .onClick('#' + app.picker.data.ID + ' #JahiaGxtManagerLeftTree .x-tab-panel-header .x-tab-strip-spacer', app.picker.source.toggle)
            /**
             * Target: Managers > Left Tree > Header > Site Dropdown
             * Callback: app.picker.source.onMouseOver
             */
            .onMouseEnter('#' + app.picker.data.ID + ' #JahiaGxtManagerLeftTree .x-tab-panel-header .x-tab-strip-spacer', app.picker.source.onMouseOver)
            /**
             * Target: Managers > Left Tree > Header > Site Dropdown
             * Callback: app.picker.source.onMouseOver
             */
            .onMouseLeave('#' + app.picker.data.ID + ' #JahiaGxtManagerLeftTree .x-tab-panel-header .x-tab-strip-spacer', app.picker.source.onMouseOut)
            /**
             * Target: Managers > Left Tree > Header > Site Dropdown
             * Callback: app.picker.source.onMouseOut
             */
            .onMouseOver('#' + app.picker.data.ID + ' #JahiaGxtManagerTobTable .x-grid3-row', app.picker.row.onMouseOver)
            /**
             * Target: Managers > Content table > thumbnail
             * Callback: app.picker.thumb.onMouseOver
             */
            .onMouseOver('#' + app.picker.data.ID + ' #JahiaGxtManagerTobTable .thumb-wrap', app.picker.thumb.onMouseOver)
            /**
             * Target: Left menu > Tabs
             * Callback: app.edit.sidepanel.tabs.all.onClick
             */
            .onMouseUp('#JahiaGxtSidePanelTabs__JahiaGxtPagesTab, #JahiaGxtSidePanelTabs__JahiaGxtCreateContentTab, #JahiaGxtSidePanelTabs__JahiaGxtContentBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtFileImagesBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtSearchTab, #JahiaGxtSidePanelTabs__JahiaGxtCategoryBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtChannelsTab, #JahiaGxtSidePanelTabs__JahiaGxtSettingsTab', app.edit.sidepanel.tabs.all.onClick, 'OPEN-SIDE-PANEL-TAB')
            /**
             * Target: Left menu > Tabs > Sub menu
             * Callback:  app.edit.sidepanel.row.onMouseDown
             */
            .onMouseDown('#JahiaGxtContentBrowseTab .x-box-item:nth-child(1) .x-grid3-row, #JahiaGxtFileImagesBrowseTab .x-grid3-row, #JahiaGxtCategoryBrowseTab .x-xbox-item:nth-child(1) .x-grid3-row', app.edit.sidepanel.row.onMouseDown)
            /**
             * Target: Left menu > Tabs > Sub menu > Preview
             * Callback:  app.edit.sidepanel.toggleFloatingPanel
             */
            .onClick('#images-view, .x-box-inner .x-box-item:nth-child(2), .JahiaGxtSearchTab-results .x-panel-bwrap', app.edit.sidepanel.toggleFloatingPanel);

        // WINDOW LISTENERS
        /**
         * Target: Window object > each time the window is reasized
         * Callback: app.onResize
         */
        window.onresize = app.onResize; // Use some kind of timer to reduce repaints / DOM manipulations
        /**
         * Target: Window object > an element has lost focus
         * Callback: app.onBlur
         */
        window.addEventListener('blur', app.onBlur);

        /**
         * Target: Window object > each time the active history entry changes between two history entries for the same document
         * Callback: app.nav.onPopState
         */
        window.onpopstate = app.nav.onPopState;
    }
};
