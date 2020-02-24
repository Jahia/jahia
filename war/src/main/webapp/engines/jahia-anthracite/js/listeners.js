/**
* This object concerns the Edit Mode in Jahia.
 * @type {array}
 */
 Anthracite.listeners.queue.add([{
    root: 'body',
    type: 'onMouseOver',
    target: '.x-current-page-path',
    callback: Anthracite.edit.togglePageInfoToolTip,
},{
    root: 'body',
    type: 'onMouseOut',
    target: '.x-current-page-path',
    callback: Anthracite.edit.togglePageInfoToolTip,
},{
    root: 'body',
    type: 'onOpen',
    target: '.x-window-plain.x-window-dlg',
    callback: Anthracite.dialog.onOpen,
},{
    root: 'body',
    type: 'onClick',
    target: '.tab_systemSiteSettings > .x-panel',
    callback: Anthracite.admin.sidepanel.toggleSiteSettingsMenu,
},{
    root: 'body',
    type: 'onAttribute',
    target: 'body',
    attrKey: 'data-sitekey',
    callback: Anthracite.edit.onNewSite,
},{
    root: 'body',
    type: 'onAttribute',
    target: 'body',
    attrKey: 'data-lang',
    callback: Anthracite.common.resizeLanguageInput,
},{
    root: 'body',
    type: 'onAttribute',
    target: 'body',
    attrKey: 'data-sitekey',
    callback: Anthracite.common.resizeLanguageInput,
},{
    root: 'body',
    type: 'onAttribute',
    target: '#JahiaGxtContentPickerWindow .x-vsplitbar, #contentpicker .x-vsplitbar, #contentmanager .x-vsplitbar, #JahiaGxtContentPicker .x-vsplitbar',
    attrKey: 'style',
    callback: Anthracite.picker.onResize,
},{
    root: 'body',
    type: 'onOpen',
    target: '.x-window',
    callback: Anthracite.modals.onOpen,
},{
    root: 'body',
    type: 'onClose',
    target: '.x-window',
    callback: Anthracite.modals.onClose,
},{
    root: 'body',
    type: 'onMouseOver',
    target: '.edit-menu-publication',
    callback: Anthracite.edit.topbar.publicationButtonArrow.onMouseOver,
},{
    root: 'body',
    type: 'onMouseOut',
    target: '.edit-menu-publication',
    callback: Anthracite.edit.topbar.publicationButtonArrow.onMouseOut,
},{
    root: 'body',
    type: 'onMouseOver',
    target: '.edit-menu-view',
    callback: Anthracite.edit.topbar.previewButtonArrow.onMouseOver,
},{
    root: 'body',
    type: 'onMouseOut',
    target: '.edit-menu-view',
    callback: Anthracite.edit.topbar.previewButtonArrow.onMouseOut,
},{
    root: 'body',
    type: 'onMouseOver',
    target: '.toolbar-item-preview',
    callback: Anthracite.edit.topbar.previewButtonContainer.onMouseOver,
},{
    root: 'body',
    type: 'onMouseOut',
    target: '.toolbar-item-preview',
    callback: Anthracite.edit.topbar.previewButtonContainer.onMouseOut,
},{
    root: 'body',
    type: 'onMouseOver',
    target: '.toolbar-item-publishone',
    callback: Anthracite.edit.topbar.publicationButtonContainer.onMouseOver,
},{
    root: 'body',
    type: 'onMouseOut',
    target: '.toolbar-item-publishone',
    callback: Anthracite.edit.topbar.publicationButtonContainer.onMouseOut,
},{
    root: 'body',
    type: 'onClick',
    target: '.window-side-panel > .x-panel-bwrap > div:nth-child(2).x-panel-footer',
    callback: Anthracite.edit.sidepanel.togglePin,
},{
    root: 'body',
    type: 'onOpen',
    target: '.job-list-window',
    callback: Anthracite.backgroundJobs.onOpen,
},{
    root: 'body',
    type: 'onMouseDown',
    target: '.toolbar-item-studio',
    callback: function () {
        var studioNodePath = sessionStorage.getItem('studiomode_nodePath');
        var baseURL = jahiaGWTParameters.contextPath + '/cms/studio/default/' + jahiaGWTParameters.uilang;

        var studioURL;
        if (studioNodePath && studioNodePath !== '/settings') {
            studioURL = baseURL + studioNodePath + '.html';
        } else {
            studioURL = baseURL + '/settings.manageModules.html';
        }
        window.location = studioURL;
    },
},{
    root: 'body',
    type: 'onOpen',
    target: '.content-type-window .x-form-field-wrap input',
    callback: Anthracite.edit.addPlaceholderToContentFilter,
},{
    root: 'body',
    type: 'onClick',
    target: '.toolbar-item-newfolder',
    callback: function () {
        // Add new folder
        var isDisabled = jGet.node(this).hasClass('x-item-disabled');
        if (isDisabled) {
            // User has clicked on the ADD FOLDER icon whilst in the RIGHT tree, so trigger click on the CONTEXT MENU OF THE LEFT TREE
            var selectedFolder = jGet('#CRTbrowseTabItem .x-grid3-row-selected');
            if (selectedFolder.nodes.length > 0) {
                selectedFolder.trigger('contextmenu');
            } else {
                jGet('#CRTbrowseTabItem .x-grid3-row')
                    .first() // Get first row
                    .trigger('mousedown') // Select it with a mouse down
                    .trigger('mouseup') // Release the mousedown
                    .trigger('contextmenu'); // Trigger a right click
            }

            // When context menu is opened click on the ADD FOLDER button
            jGet('body').onceOpen({
                target: '.x-menu',
                callback: function () {
                    // Need to shift the context menu out of view because it doesnt dissappear until the alert has been closed.
                    jGet('.x-menu').css({
                        left: '-50000px'
                    });
                    jGet('.x-menu .toolbar-item-newfolder').trigger('click');
                },
            });
        }
    },
},{
    root: 'body',
    type: 'onClick',
    target: '.toolbar-item-newcontentfolder',
    callback: function () {
        // Add new folder
        var isDisabled = jGet.node(this).hasClass('x-item-disabled');
        if (isDisabled) {
            // User has clicked on the ADD FOLDER icon whilst in the RIGHT tree, so trigger click on the CONTEXT MENU OF THE LEFT TREE
            var selectedFolder = jGet('#CRTbrowseTabItem .x-grid3-row-selected');
            if (selectedFolder.nodes.length > 0) {
                selectedFolder.trigger('contextmenu');
            } else {
                jGet('#CRTbrowseTabItem .x-grid3-row')
                    .first() // Get first row
                    .trigger('mousedown') // Select it with a mouse down
                    .trigger('mouseup') // Release the mousedown
                    .trigger('contextmenu'); // Trigger a right click
            }

            // When context menu is opened click on the ADD FOLDER button
            jGet('body').onceOpen({
                target: '.x-menu',
                callback: function () {
                    jGet('.x-menu .toolbar-item-newcontentfolder').trigger('click');
                },
            });
        }

    },
},{
    root: 'body',
    type: 'onClick',
    target: '.toolbar-item-newpage',
    callback: function () {
        // Add new folder
        var isDisabled = jGet.node(this).hasClass('x-item-disabled');
        if (isDisabled) {
            // User has clicked on the ADD FOLDER icon whilst in the RIGHT tree, so trigger click on the CONTEXT MENU OF THE LEFT TREE
            var selectedFolder = jGet('#CRTbrowseTabItem .x-grid3-row-selected');
            if (selectedFolder.nodes.length > 0) {
                selectedFolder.trigger('contextmenu');
            } else {
                jGet('#CRTbrowseTabItem .x-grid3-row')
                    .first() // Get first row
                    .trigger('mousedown') // Select it with a mouse down
                    .trigger('mouseup') // Release the mousedown
                    .trigger('contextmenu'); // Trigger a right click
            }

            // When context menu is opened click on the ADD FOLDER button
            jGet('body').onceOpen({
                target: '.x-menu',
                callback: function () {
                    jGet('.x-menu .toolbar-item-newpage').trigger('click');
                },
            });
        }
    },
},{
    root: 'body',
    type: 'onClick',
    target: '.toolbar-item-upload',
    callback: function () {
        // Add new folder
        var isDisabled = jGet.node(this).hasClass('x-item-disabled');
        if (isDisabled) {
            // User has clicked on the ADD FOLDER icon whilst in the RIGHT tree, so trigger click on the CONTEXT MENU OF THE LEFT TREE
            var selectedFolder = jGet('#CRTbrowseTabItem .x-grid3-row-selected');
            if (selectedFolder.nodes.length > 0) {
                selectedFolder.trigger('contextmenu');
            } else {
                jGet('#CRTbrowseTabItem .x-grid3-row')
                    .first() // Get first row
                    .trigger('mousedown') // Select it with a mouse down
                    .trigger('mouseup') // Release the mousedown
                    .trigger('contextmenu'); // Trigger a right click
            }

            // When context menu is opened click on the ADD FOLDER button
            jGet('body').onceOpen({
                target: '.x-menu',
                callback: function () {
                    jGet('.x-menu .toolbar-item-upload').trigger('click');
                },
            });
        }
    },
},{
    root: 'body',
    type: 'onClick',
    target: '.toolbar-item-newcontent',
    callback: function () {
        // Add new content folder
        var isDisabled = jGet.node(this).hasClass('x-item-disabled');
        if (isDisabled) {
            // User has clicked on the ADD FOLDER icon whilst in the RIGHT tree, so trigger click on the CONTEXT MENU OF THE LEFT TREE
            var selectedFolder = jGet('#CRTbrowseTabItem .x-grid3-row-selected');
            if (selectedFolder.nodes.length > 0) {
                selectedFolder.trigger('contextmenu');
            } else {
                jGet('#CRTbrowseTabItem .x-grid3-row')
                    .first() // Get first row
                    .trigger('mousedown') // Select it with a mouse down
                    .trigger('mouseup') // Release the mousedown
                    .trigger('contextmenu'); // Trigger a right click
            }

            // When context menu is opened click on the ADD FOLDER button
            jGet('body').onceOpen({
                target: '.x-menu',
                callback: function () {
                    jGet('.x-menu .toolbar-item-newcontent').trigger('click');
                },
            });
        }
    },
},{
    root: 'body',
    type: 'onAttribute',
    target: 'body',
    attrKey: 'data-singleselection-node-path',
    callback: Anthracite.onChangeNodePath,
},{
    root: 'body',
    type: 'onOpen',
    target: '#JahiaGxtEditEnginePanel-workflow > div > div:nth-child(1) .x-grid-panel',
    callback: Anthracite.engine.onOpenWorkflow,
},{
    root: 'body',
    type: 'onOpen',
    target: '#JahiaGxtEditEnginePanel-history',
    callback: Anthracite.engine.onOpenHistory,
},{
    root: 'body',
    type: 'onOpen',
    target: '#JahiaGxtUserGroupSelect',
    callback: Anthracite.userPickers.users.onOpen,
},{
    root: 'body',
    type: 'onOpen',
    target: '#JahiaGxtContentBrowseTab',
    callback: Anthracite.edit.sidepanel.tabs.contentTab.onOpen,
},{
    root: 'body',
    type: 'onOpen',
    target: '#JahiaGxtFileImagesBrowseTab',
    callback: Anthracite.edit.sidepanel.tabs.filesTab.onOpen,
},{
    root: 'body',
    type: 'onOpen',
    target: '#JahiaGxtCategoryBrowseTab',
    callback: Anthracite.edit.sidepanel.tabs.categoryTab.onOpen,
},{
    root: 'body',
    type: 'onOpen',
    target: '.results-column .x-grid-empty',
    callback: Anthracite.edit.sidepanel.resultsPanel.onEmpty,
},{
    root: 'body',
    type: 'onOpen',
    target: '.results-column .x-grid3-row',
    callback: Anthracite.edit.sidepanel.resultsPanel.onResults,
},{
    root: 'body',
    type: 'onOpen',
    target: '#JahiaGxtCreateContentTab',
    callback: Anthracite.edit.sidepanel.tabs.createContent.onOpen,
},{
    root: 'body',
    type: 'onGroupOpen',
    target: '#JahiaGxtSettingsTab .x-grid3-row',
    callback: Anthracite.edit.settings.onTreeChange,
},{
    root: 'body',
    type: 'onOpen',
    target: '.x-grid-empty',
    callback: Anthracite.edit.sidepanel.browseTree.onEmpty,
},{
    root: 'body',
    type: 'onOpen',
    target: '#JahiaGxtSearchTab',
    callback: Anthracite.edit.sidepanel.tabs.searchTab.onOpen,
},{
    root: 'body',
    type: 'onOpen',
    target: '.x-grid3-row',
    callback: Anthracite.edit.sidepanel.browseTree.onResults,
},{
    root: 'body',
    type: 'onClose',
    target: '.thumb-wrap',
    callback: Anthracite.edit.sidepanel.thumbPanel.onClose,
},{
    root: 'body',
    type: 'onOpen',
    target: '.thumb-wrap',
    callback: Anthracite.edit.sidepanel.thumbPanel.onOpen,
},{
    root: 'body',
    type: 'onOpen',
    target: '.menu-edit-menu-workflow',
    callback: Anthracite.edit.infoBar.tasks.updateMenuLabel,
},{
    root: 'body',
    type: 'onOpen',
    target: '.menu-contribute-menu-workflow',
    callback: Anthracite.edit.infoBar.tasks.updateMenuLabel,
},{
    root: 'body',
    type: 'onOpen',
    target: '.menu-edit-menu-view',
    callback: Anthracite.contextMenus.previewMenu.onOpen,
},{
    root: 'body',
    type: 'onOpen',
    target: '.menu-edit-menu-publication',
    callback: Anthracite.contextMenus.publicationMenu.onOpen,
},{
    root: 'body',
    type: 'onOpen',
    target: '.menu-edit-menu-edit',
    callback: Anthracite.contextMenus.moreInfoMenu.onOpen,
},{
    root: 'body',
    type: 'onOpen',
    target: '.editModeContextMenu',
    callback: Anthracite.contextMenus.moreInfoMenu.onOpen,
},{
    root: 'body',
    type: 'onOpen',
    target: '.menu-editmode-managers-menu',
    callback: Anthracite.contextMenus.managerMenu.onOpen,
},{
    root: 'body',
    type: 'onClose',
    target: '.menu-editmode-managers-menu',
    callback: Anthracite.contextMenus.managerMenu.onClose,
},{
    root: 'body',
    type: 'onOpen',
    target: '#' + Anthracite.picker.data.ID,
    callback: Anthracite.picker.onOpen,
},{
    root: 'body',
    type: 'onOpen',
    target: '.engine-window, .engine-panel',
    callback: Anthracite.engine.onOpen,
},{
    root: 'body',
    type: 'onOpen',
    target: '#JahiaGxtImagePopup',
    callback: Anthracite.imagePreview.onOpen,
},{
    root: 'body',
    type: 'onAttribute',
    target: '.edit-menu-tasks',
    attrKey: 'class',
    callback: Anthracite.edit.infoBar.tasks.onChange,
},{
    root: 'body',
    type: 'onAttribute',
    target: '.contribute-menu-tasks',
    attrKey: 'class',
    callback: Anthracite.edit.infoBar.tasks.onChange,
},{
    root: 'body',
    type: 'onAttribute',
    target: '.toolbar-item-workinprogressadmin, .toolbar-item-workinprogress',
    attrKey: 'class',
    callback: Anthracite.edit.infoBar.jobs.onChange,
},{
    root: 'body',
    type: 'onOpen',
    target: '.x-dd-drag-proxy',
    callback: Anthracite.edit.sidepanel.onDrag.bind(this, true),
},{
    root: 'body',
    type: 'onClose',
    target: '.x-dd-drag-proxy',
    callback: Anthracite.edit.sidepanel.onDrag.bind(this, false),
},{
    root: 'body',
    type: 'onAttribute',
    target: 'body',
    attrKey: 'data-sitesettings',
    callback: Anthracite.edit.settings.onChange,
},{
    root: 'body',
    type: 'onAttribute',
    target: 'body',
    attrKey: 'data-selection-count',
    callback: Anthracite.iframe.onSelect,
},{
    root: 'body',
    type: 'onAttribute',
    target: 'body',
    attrKey: 'data-main-node-displayname',
    callback: Anthracite.iframe.onChange,
},{
    root: 'body',
    type: 'onAttribute',
    target: 'body',
    attrKey: 'data-main-node-path',
    callback: Anthracite.contribute.onChangeMode,
},{
    root: 'body',
    type: 'onAttribute',
    target: 'body',
    attrKey: 'data-main-node-path',
    callback: Anthracite.edit.onNewPage,
},{
    root: 'body',
    type: 'onAttribute',
    target: '.window-iframe',
    attrKey: 'src',
    callback: Anthracite.iframe.onChangeSRC,
},{
    root: 'body',
    type: 'onAttribute',
    target: '.x-jahia-root',
    attrKey: 'class',
    callback: Anthracite.onChange,
},{
    root: 'body',
    type: 'onClose',
    target: '#' + Anthracite.picker.data.ID,
    callback: Anthracite.picker.onClose,
},{
    root: 'body',
    type: 'onClose',
    target: '#JahiaGxtContentPickerWindow',
    callback: Anthracite.picker.onClose,
},{
    root: 'body',
    type: 'onClose',
    target: '#JahiaGxtEnginePanel, #JahiaGxtEngineWindow',
    callback: Anthracite.engine.onClose,
},{
    root: 'body',
    type: 'onClose',
    target: '#JahiaGxtImagePopup',
    callback: Anthracite.imagePreview.onClose,
},{
    root: 'body',
    type: 'onOpen',
    target: '.workflow-dashboard-engine',
    callback: Anthracite.workflow.dashboard.onOpen,
},{
    root: 'body',
    type: 'onClick',
    target: '.app-container',
    callback: Anthracite.onClick,
},{
    root: 'body',
    type: 'onClick',
    target: '.toolbar-item-filepreview',
    callback: Anthracite.picker.previewButton.onClick,
},{
    root: 'body',
    type: 'onClick',
    target: '#JahiaGxtManagerLeftTree + div .x-grid3 .x-grid3-row',
    callback: Anthracite.picker.row.onClick,
},{
    root: 'body',
    type: 'onClick',
    target: '.x-viewport-adminmode .x-grid3 .x-grid3-row',
    callback: Anthracite.admin.sidepanel.row.onClick,
},{
    root: 'body',
    type: 'onClick',
    target: '#JahiaGxtEditEnginePanel-visibility > .x-component:nth-child(3) .x-panel-footer',
    callback: Anthracite.engine.closeConditionEditor,
},{
    root: 'body',
    type: 'onClick',
    target: '#JahiaGxtEditEnginePanel-visibility > .x-component:nth-child(2) .x-grid3-row',
    callback: Anthracite.engine.editCondition,
},{
    root: 'body',
    type: 'onClick',
    target: '#JahiaGxtEditEnginePanel-visibility > .x-component:nth-child(1) > .x-component:nth-child(2) td:nth-child(5) > table',
    callback: Anthracite.engine.addCondition,
},{
    root: 'body',
    type: 'onMouseDown',
    target: '#JahiaGxtEditEnginePanel-visibility > .x-component:nth-child(1) img.x-form-trigger',
    callback: Anthracite.engine.openConditionsMenu,
},{
    root: 'body',
    type: 'onClick',
    target: '.x-grid3-row .x-grid3-td-size',
    callback: Anthracite.picker.search.onContext,
},{
    root: 'body',
    type: 'onClick',
    target: '.x-grid3-row .x-tree3-el',
    callback: Anthracite.picker.row.onContext,
},{
    root: 'body',
    type: 'onMouseDown',
    target: '#' + Anthracite.picker.data.ID + ' .cancel-edit',
    callback: Anthracite.picker.thumb.closeEdit,
},{
    root: 'body',
    type: 'onMouseDown',
    target: '#' + Anthracite.picker.data.ID + ' .edit-button',
    callback: Anthracite.picker.thumb.openEdit,
},{
    root: 'body',
    type: 'onMouseDown',
    target: '#' + Anthracite.picker.data.ID + ' .more-info-button',
    callback: Anthracite.picker.thumb.onContext,
},{
    root: 'body',
    type: 'onMouseDown',
    target: '#' + Anthracite.picker.data.ID + ' .preview-button',
    callback: Anthracite.picker.thumb.openPreview,
},{
    root: 'body',
    type: 'onClick',
    target: '#JahiaGxtManagerLeftTree + div .thumb-wrap',
    callback: Anthracite.picker.thumb.onClick,
},{
    root: 'body',
    type: 'onClick',
    target: '#JahiaGxtSidePanelTabs > .x-tab-panel-header .x-tab-strip-spacer',
    callback: Anthracite.edit.settings.close,
},{
    root: 'body',
    type: 'onMouseOver',
    target: '.toolbar-item-filepreview',
    callback: Anthracite.picker.previewButton.onMouseOver,
},{
    root: 'body',
    type: 'onMouseOut',
    target: '.toolbar-item-filepreview',
    callback: Anthracite.picker.previewButton.onMouseOut,
},{
    root: 'body',
    type: 'onMouseDown',
    target: '.x-tree3-node-joint',
    callback: Anthracite.common.trees.onToggleChildNodes,
},{
    root: 'body',
    type: 'onMouseDown',
    target: '.menu-edit-menu-mode',
    callback: Anthracite.contextMenus.managerMenu.onClose,
},{
    root: 'body',
    type: 'onMouseDown',
    target: '.menu-edit-menu-user',
    callback: Anthracite.contextMenus.managerMenu.onClose,
},{
    root: 'body',
    type: 'onClick',
    target: '#JahiaGxtSidePanelTabs .x-grid3-td-displayName',
    callback: Anthracite.edit.sidepanel.row.onContext,
},{
    root: 'body',
    type: 'onClick',
    target: '#' + Anthracite.picker.data.ID,
    callback: Anthracite.picker.onClick,
},{
    root: 'body',
    type: 'onClick',
    target: '#' + Anthracite.picker.data.ID + ' .x-panel-tbar .action-bar-tool-item.toolbar-item-listview',
    callback: Anthracite.picker.onListView,
},{
    root: 'body',
    type: 'onClick',
    target: '#' + Anthracite.picker.data.ID + ' .x-panel-tbar .action-bar-tool-item.toolbar-item-thumbsview',
    callback: Anthracite.picker.onThumbView,
},{
    root: 'body',
    type: 'onClick',
    target: '#' + Anthracite.picker.data.ID + ' .x-panel-tbar .action-bar-tool-item.toolbar-item-detailedview',
    callback: Anthracite.picker.onDetailView,
},{
    root: 'body',
    type: 'onMouseDown',
    target: '.toolbar-item-listview',
    callback: Anthracite.picker.onListView,
},{
    root: 'body',
    type: 'onMouseDown',
    target: '.toolbar-item-thumbsview',
    callback: Anthracite.picker.onThumbView,
},{
    root: 'body',
    type: 'onMouseDown',
    target: '.toolbar-item-detailedview',
    callback: Anthracite.picker.onDetailView,
},{
    root: 'body',
    type: 'onMouseUp',
    target: '#contentmanager .x-panel-bbar .x-toolbar-cell:nth-child(1) .x-btn',
    callback: Anthracite.picker.thumb.closeEdit,
},{
    root: 'body',
    type: 'onClick',
    target: '.node-path-title',
    callback: Anthracite.iframe.clearSelection,
},{
    root: 'body',
    type: 'onMouseDown',
    target: '.x-viewport-editmode #JahiaGxtSidePanelTabs .x-grid3-row',
    callback: Anthracite.edit.onNav,
},{
    root: 'body',
    type: 'onMouseDown',
    target: '#JahiaGxtManagerLeftTree__CRTbrowseTabItem',
    callback: Anthracite.picker.search.close,
},{
    root: 'body',
    type: 'onClose',
    target: '.indigo-picker-multi-select',
    callback: function () {
        if (jGet.class('search-panel-opened').exists()) {
            jGet.id('JahiaGxtManagerLeftTree__CRTbrowseTabItem').trigger('click');

            jGet.class('search-panel-opened').removeClass('search-panel-opened');
        }
    },
},{
    root: 'body',
    type: 'onMouseUp',
    target: '#JahiaGxtManagerLeftTree__CRTsearchTabItem',
    callback: Anthracite.picker.search.open,
},{
    root: 'body',
    type: 'onClick',
    target: '#' + Anthracite.picker.data.ID + ' #JahiaGxtManagerLeftTree .x-panel-header',
    callback: Anthracite.picker.source.close,
},{
    root: 'body',
    type: 'onClick',
    target: '#' + Anthracite.picker.data.ID + ' #JahiaGxtManagerLeftTree .x-tab-panel-header .x-tab-strip-spacer',
    callback: Anthracite.picker.source.toggle,
},{
    root: 'body',
    type: 'onMouseEnter',
    target: '#' + Anthracite.picker.data.ID + ' #JahiaGxtManagerLeftTree .x-tab-panel-header .x-tab-strip-spacer',
    callback: Anthracite.picker.source.onMouseOver,
},{
    root: 'body',
    type: 'onMouseLeave',
    target: '#' + Anthracite.picker.data.ID + ' #JahiaGxtManagerLeftTree .x-tab-panel-header .x-tab-strip-spacer',
    callback: Anthracite.picker.source.onMouseOut,
},{
    root: 'body',
    type: 'onMouseOver',
    target: '#' + Anthracite.picker.data.ID + ' #JahiaGxtManagerTobTable .x-grid3-row',
    callback: Anthracite.picker.row.onMouseOver,
},{
    root: 'body',
    type: 'onMouseOver',
    target: '#' + Anthracite.picker.data.ID + ' #JahiaGxtManagerTobTable .thumb-wrap',
    callback: Anthracite.picker.thumb.onMouseOver,
},{
    root: 'body',
    type: 'onMouseUp',
    target: '#JahiaGxtSidePanelTabs__JahiaGxtPagesTab, #JahiaGxtSidePanelTabs__JahiaGxtCreateContentTab, #JahiaGxtSidePanelTabs__JahiaGxtContentBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtFileImagesBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtSearchTab, #JahiaGxtSidePanelTabs__JahiaGxtCategoryBrowseTab, #JahiaGxtSidePanelTabs__JahiaGxtChannelsTab, #JahiaGxtSidePanelTabs__JahiaGxtSettingsTab',
    callback: Anthracite.edit.sidepanel.tabs.all.onClick,
    mutation_id: 'OPEN-SIDE-PANEL-TAB'
},{
    root: 'body',
    type: 'onMouseDown',
    target: '#JahiaGxtContentBrowseTab .x-box-item:nth-child(1) .x-grid3-row, #JahiaGxtFileImagesBrowseTab .x-grid3-row, #JahiaGxtCategoryBrowseTab .x-xbox-item:nth-child(1) .x-grid3-row',
    callback: Anthracite.edit.sidepanel.row.onMouseDown,
},{
    root: 'body',
    type: 'onClick',
    target: '#images-view, .x-box-inner .x-box-item:nth-child(2), .JahiaGxtSearchTab-results .x-panel-bwrap',
    callback: Anthracite.edit.sidepanel.toggleFloatingPanel,
}]);
