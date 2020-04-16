/**
* This object concerns the Edit Mode in Jahia.
 * @memberof Anthracite
 * @property {object} config Configuration settings for Edit Mode
 * @property {object} data Stores data for Object
 * @property {method} onNewPage XXX
 * @property {method} onNewSite Callback executed when the site is changed in the Edit Mode
 * @property {method} addPlaceholderToContentFilter Add a placeholder the input filter and focus it
 * @property {method} togglePageInfoToolTip Toggle the tooltip for page info when the user hovers the page title
 * @property {method} onOpen Callback executed when witching to the Edit Mode
 * @property {method} onClose Callback executes when leaving the Edit Mode
 * @property {method} onNav Callback executed when the User changes page in Edit Mode
 * @property {object} infoBar XXX <br> See {@link Anthracite.edit.infoBar}
 * @property {object} history XXX <br> See {@link Anthracite.edit.history}
 * @property {object} topbar XXX <br> See {@link Anthracite.edit.topbar}
 * @property {object} sidepanel Everything here concerns the Side Panel <br> See {@link Anthracite.edit.sidepanel}
 * @property {object} settings Everything here concerns settings in Edit Mode ( not to be confused with Admin mode) <br> See {@link Anthracite.edit.settings}
 * @namespace Anthracite.edit
 * @type {object}
 */
Anthracite.addModule("edit", {
     /**
      * XXX
      * @memberof Anthracite.edit
      * @property {boolean} chrome XXX
      * @type {object}
      * @example
      *
      *
      * Add Example here ...
      *
      *
      */
    config: {
        chrome: true
    },
    /**
    * Stores data for Object
    * @memberof Anthracite.edit
    * @property {object} history XXX
    * @property {boolean} history.settingspage XXX
    * @property {boolean} history.editpage XXX
    * @property {object} search XXX
    * @property {boolean} search.refreshButtonClasslist XXX
    * @property {boolean} search.emptyGridStyle XXX
    * @property {boolean} search.status XXX
    * @property {boolean} returnURL XXX
    * @type {object}
    * @example
    *
    *
    * Add Example here ...
    *
    *
    */
    data: {
        history: {
            settingspage: null,
            editpage: null
        },
        search: {
            refreshButtonClasslist: null,
            emptyGridStyle: null,
            status: null
        },
        returnURL: null
    },
    /**
     * XXX
     * @memberof Anthracite.edit
     * @method onNewPage
     * @example
     *
     *
     * Add Example here ...
     *
     *
     */
    onNewPage: function(){
        Anthracite.dev.log("app ::: edit ::: onNewPage");
        Anthracite.common.resizeLanguageInput();
        Anthracite.common.resizeSiteSelector();
    },
    /**
     * Callback executed when the site is changed in the Edit Mode
     * @memberof Anthracite.edit
     * @method onNewSite

     * @example
     *
     *
     * Add Example here ...
     *
     *
     */
    onNewSite: function(){
        Anthracite.dev.log("app ::: edit ::: onNewSite (disabled the dodgy code )");

        // Flag that a new site has been loaded
        Anthracite.edit.sidepanel.data.newSite = true;

        jGet.getCached('body').setAttribute("data-edit-mode-status", "loading");

        Anthracite.common.resizeSiteSelector();
    },
    /**
     * Add a placeholder the input filter and focus it
     * @memberof Anthracite.edit
     * @method addPlaceholderToContentFilter

     * @example
     *
     *
     * Add Example here ...
     *
     *
     */
    addPlaceholderToContentFilter: function () {
        jGet('.content-type-window .x-form-field-wrap input').setAttribute('placeholder', Anthracite.dictionary.get('filterContent'));
        // Firefox has bug which doesnt always set focus on text input, wait a split second before settings focus
        var that = this;
        setTimeout(function () {
            that.focus();
        }, 100);
    },
    /**
    * Toggle the tooltip for page info when the user hovers the page title
    * @memberof Anthracite.edit
    * @method togglePageInfoToolTip

    * @example
    *
    *
    * Add Example here ...
    *
    *
    */
    togglePageInfoToolTip: function() {
      jGet.class("publication-status-tooltip").toggleClass("indigo-show");
    },
    /**
     * Callback executed when witching to the Edit Mode
     * @memberof Anthracite.edit
     * @method onOpen

     * @example
     *
     *
     * Add Example here ...
     *
     *
     */
    onOpen: function () {
        Anthracite.dev.log('::: APP ::: EDIT ::: ONOPEN');
        jGet.getCached('body').setAttribute("data-edit-mode-status", "loading");

        // Add Background mask used for modals
        if(!jGet.class('background-mask').exists()){
          var backGroundMask = document.createElement('div');

          backGroundMask.classList.add('background-mask');
          jGet.getCached('body').append(backGroundMask);

        }

        // Add Publication Status Bar
        if (!jGet.class('publication-status').exists()) {
            // Create div for publication status of page / selected element because currently it is a pseudo element and we cant reposition when in pinned mode
            var publicationStatus = document.createElement('div'),
                publicationStatusTooltip = document.createElement('div'),
                publicationStatusLabel = document.createElement('label'),
                publicationStatusPath = document.createElement('p'),

                status = (Anthracite.iframe.data.publication && Anthracite.iframe.data.publication.status) ? jahia_gwt_messages["label_publication_" + Anthracite.iframe.data.publication.status] : 'unknown',
                path = jahiaGWTParameters[jahiaGWTParameters.lang];

            publicationStatusLabel.setAttribute("data-label", jahia_gwt_messages.label_publication_status + ": ");
            publicationStatusLabel.innerHTML = status;
            publicationStatusLabel.classList.add('publication-status-label');

            publicationStatusPath.setAttribute("data-label", jahia_gwt_messages.label_path + ": ");
            publicationStatusPath.innerHTML = path;
            publicationStatusPath.classList.add('publication-status-path');

            publicationStatusTooltip.classList.add('publication-status-tooltip');
            publicationStatusTooltip.appendChild(publicationStatusLabel);
            publicationStatusTooltip.appendChild(publicationStatusPath);

            publicationStatus.appendChild(publicationStatusTooltip);
            publicationStatus.classList.add('publication-status');
            publicationStatus.setAttribute('data-publication-status', status);
            jGet.getCached('body').prepend(publicationStatus);
        }

        jGet('.mainmodule > div:nth-child(2)').nodes[0].style.removeProperty('width');
        jGet('.mainmodule > div:nth-child(2)').nodes[0].style.removeProperty('left');

        jGet('.window-side-panel > .x-panel-bwrap > div:nth-child(2).x-panel-footer').addClass('side-panel-pin');

        jGet.getCached('body').setAttribute('data-indigo-styled-combos', 'true');
        jGet.getCached('body').setAttribute('data-indigo-sidepanel-pinned', 'false');
        Anthracite.edit.sidepanel.data.pinned = false;
        Anthracite.edit.data.returnURL = window.location.pathname;

        // Reset History
        Anthracite.edit.history.reset();

        Anthracite.edit.topbar.build();

        // Set attributes to be used by CSS
        jGet.getCached('body')
            .setAttribute('data-edit-window-style', 'default')
            .setAttribute('data-INDIGO-GWT-SIDE-PANEL', '')
            .setAttribute('data-INDIGO-COLLAPSABLE-SIDE-PANEL', 'yes');

        // Setup the alternative channels system
        Anthracite.edit.sidepanel.initChannels();

        Anthracite.common.resizeLanguageInput();

        if (jGet.id('JahiaGxtSidePanelTabs').exists()) {
            jGet.id('JahiaGxtSidePanelTabs').nodes[0].style.setProperty('width', '245px', 'important');
            jGet.getCached('body').setAttribute('data-indigo-gwt-side-panel', '');
        }

        var pinnedPanel = Anthracite.storage.get('pinnedPanel') || window.anthraciteV8;

        if (pinnedPanel) {
            jGet.class('side-panel-pin').trigger('click');
            jGet('#JahiaGxtSidePanelTabs .x-tab-strip-active').trigger('mousedown').trigger('mouseup');
        }
    },
    /**
     * Callback executes when leaving the Edit Mode
     * @memberof Anthracite.edit
     * @method onClose
     * @example
     *
     *
     * Add Example here ...
     *
     *
     */
    onClose: function () {
        jGet.getCached('body').setAttribute("data-edit-mode-status", null);
    },
    /**
     * Callback executed when the User changes page in Edit Mode
     * @memberof Anthracite.edit
     * @method onNav

     * @example
     *
     *
     * Add Example here ...
     *
     *
     */
    onNav: function () {
        Anthracite.dev.log('::: APP ::: EDIT ::: ONNAV');
        if (Anthracite.edit.settings.data.opened) {
            // CLicked on a settings page
            Anthracite.dev.log(['ONNAV ::: ', this]);
            if (this.classList.contains('unselectable-row')) {
                Anthracite.dev.log('DO NOT REMEMBER THIS PAGE IN HISTORY AS IT IS A FOLDER');
            } else {
                Anthracite.edit.history.add('settingspage', this);
            }
        } else if (jGet.getCached('body').getAttribute('data-indigo-gwt-panel-tab') == 'JahiaGxtSidePanelTabs__JahiaGxtPagesTab') {
            Anthracite.edit.history.add('editpage', this);
        }
    },
    /**
     * XXX
     * @memberof Anthracite.edit
     * @property {object} data Stores data for Object
     * @property {method} toggle XXX
     * @property {object} tasks XXX <br> See {@link Anthracite.edit.infoBar.tasks}
     * @property {object} jobs XXX <br> See {@link Anthracite.edit.infoBar.jobs}
     * @property {object} publicationStatus XXX <br> See {@link Anthracite.edit.infoBar.publicationStatus}
     * @namespace Anthracite.edit.infoBar
     * @type {object}
     * @example
     *
     *
     * Add Example here ...
     *
     *
     */
    infoBar: {
        /**
         * Stores data for Object
         * @memberof Anthracite.edit.infoBar
         * @property {boolean} on XXX
         * @property {integer} taskCount XXX
         * @type {object}
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        data: {
            on: false,
            taskCount: 0
        },
        /**
         * XXX
         * @memberof Anthracite.edit.infoBar
         * @method toggle

         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        toggle: function () {
            Anthracite.dev.log('::: APP ::: EDIT ::: INFOBAR ::: TOGGLE');
            Anthracite.edit.infoBar.data.on = !Anthracite.edit.infoBar.data.on;

            jGet.getCached('body').setAttribute('data-indigo-infoBar', Anthracite.edit.infoBar.data.on);
        },
        /**
         * XXX
         * @memberof Anthracite.edit.infoBar
         * @property {object} data Stores data for Object
         * @property {method} onChange XXX
         * @property {method} updateMenuLabel XXX
         * @namespace Anthracite.edit.infoBar.tasks
         * @type {object}
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        tasks: {
            /**
             * Stores data for Object
             * @memberof Anthracite.edit.infoBar.tasks
             * @property {boolean} classes XXX
             * @property {integer} taskCount XXX
             * @property {boolean} dashboardButtonLabel XXX
             * @type {object}
             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            data: {
                classes: null,
                taskCount: 0,
                dashboardButtonLabel: null
            },
             /**
              * Callback executes when tasks are being run
              * @memberof Anthracite.edit.infoBar.tasks
              * @method onChange
              * @param {string} attrKey XXX
              * @param {string} attrValue XXX
              * @returns {boolean|undefined} XXX
              * @example
              *
              *
              * Add Example here ...
              *
              *
              */
            onChange: function (attrKey, attrValue) {
                if (Anthracite.edit.infoBar.tasks.data.classes == attrValue) {
                    return false;
                }

                Anthracite.edit.infoBar.tasks.data.classes = attrValue;

                Anthracite.dev.log('::: APP ::: EDIT ::: INFOBAR ::: TASKS ::: ONCHANGE');

                var taskButton = jGet('.' + Anthracite.data.currentApp + '-menu-tasks button');
                if (taskButton.exists()) {
                    var taskCount;
                    var regexp = /\(([^)]+)\)/;
                    var taskString = taskButton.getHTML();
                    var result = taskString.match(regexp);
                    if (result) {
                        taskCount = parseInt(result[1]);
                    } else {
                        taskCount = 0;
                    }

                    var dashboardButtonLabel;
                    var dashboardButton = jGet.class('menu-edit-menu-workflow');
                    switch (taskCount) {
                        case 0:
                            dashboardButtonLabel = Anthracite.dictionary.get('zeroTasks');
                            break;
                        case 1:
                            dashboardButtonLabel = Anthracite.dictionary.get('singleTask').replace('%n%', taskCount);
                            break;
                        default:
                            dashboardButtonLabel = Anthracite.dictionary.get('multipleTasks').replace('%n%', taskCount);
                            break;
                    }

                    var workflowButtonLabel;
                    if (taskCount > 9) {
                        workflowButtonLabel = '+9';
                    } else {
                        workflowButtonLabel = taskCount;
                    }

                    jGet('.edit-menu-workflow').setAttribute('data-info-count', workflowButtonLabel);
                    jGet('.contribute-menu-workflow').setAttribute('data-info-count', workflowButtonLabel);

                    if (dashboardButton.exists()) {
                        dashboardButton.filter('.toolbar-item-workflowdashboard').setHTML(dashboardButtonLabel);
                    }

                    Anthracite.edit.infoBar.data.taskCount = taskCount;
                    Anthracite.edit.infoBar.data.workflowButtonLabel = workflowButtonLabel;
                    Anthracite.edit.infoBar.data.dashboardButtonLabel = dashboardButtonLabel;
                }
            },
            /**
             * Add number of tasks to the Dashboard Button, ie: Dashboard (1 task)
             * @memberof Anthracite.edit.infoBar.tasks
             * @method updateMenuLabel

             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            updateMenuLabel: function () {
                jGet.node(this).filter('.toolbar-item-workflowdashboard').setHTML(Anthracite.edit.infoBar.data.dashboardButtonLabel);
            }
        },
        /**
         * XXX
         * @memberof Anthracite.edit.infoBar
         * @property {object} data Stores data for Object
         * @property {method} onChange XXX
         * @namespace Anthracite.edit.infoBar.jobs
         * @type {object}
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        jobs: {
            /**
             * Stores data for Object
             * @memberof Anthracite.edit.infoBar.jobs
             * @property {boolean} classes XXX
             * @property {boolean} jobString XXX
             * @type {object}
             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            data: {
                classes: null,
                jobString: null
            },
            /**
             * Callback executes when tasks are being run
             * @memberof Anthracite.edit.infoBar.jobs
             * @method onChange
             * @param {string} attrKey XXX
             * @param {string} attrValue XXX

             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            onChange: function (attrKey, attrValue) {
                var workInProgressAdminButton = (jGet.node(this).hasClass('toolbar-item-workinprogressadmin')) ? 'toolbar-item-workinprogressadmin' : 'toolbar-item-workinprogress';
                Anthracite.edit.infoBar.jobs.data.classes = attrValue;

                Anthracite.dev.log('::: APP ::: EDIT ::: INFOBAR ::: JOBS ::: ONCHANGE');

                var jobButton = jGet('.' + workInProgressAdminButton + ' button');
                if (jobButton.exists()) {
                    var jobStringSplit = jobButton.getHTML().split('<');
                    var jobString = jobStringSplit[0];

                    var activeJob;
                    var jobTooltip;
                    var jobIcon = jobButton.filter('img');
                    if (jobIcon.getAttribute('src').indexOf('workInProgress.png') == -1) {
                        // A job is active
                        activeJob = true;
                        jobTooltip = jobString;
                        jGet.class(workInProgressAdminButton).setAttribute('job-in-progress', 'true');
                        jGet('.x-viewport-editmode .action-toolbar .x-toolbar-cell:nth-child(10)').addClass('indigo-job-running');
                    } else {
                        // No Jobs active
                        activeJob = false;
                        jobTooltip = Anthracite.dictionary.get('jobs');

                        jGet.class(workInProgressAdminButton).setAttribute('job-in-progress', '');

                        jGet('.x-viewport-editmode .action-toolbar .x-toolbar-cell:nth-child(10)').removeClass('indigo-job-running');
                    }

                    Anthracite.edit.infoBar.jobs.data.jobString = jobString;
                    Anthracite.edit.infoBar.jobs.data.activeJob = activeJob;

                    var buttonParent = jGet.class(workInProgressAdminButton);
                    buttonParent.setAttribute('data-indigo-label', jobTooltip);
                }
            }
        },
        /**
         * XXX
         * @memberof Anthracite.edit.infoBar
         * @property {method} onChange XXX
         * @namespace Anthracite.edit.infoBar.publicationStatus
         * @type {object}
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        publicationStatus: {
            /**
             * XXX
             * @memberof Anthracite.edit.infoBar.publicationStatus
             * @method onChange

             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            onChange: function () {
                Anthracite.dev.log('::: APP ::: EDIT ::: INFOBAR ::: PUVLICATIONSTATUS ::: ONCHANGE');
            }
        }
    },

    /**
     * XXX
     * @memberof Anthracite.edit
     * @property {object} data Stores data for Object
     * @property {method} add XXX
     * @property {method} get XXX
     * @property {method} reset XXX
     * @namespace Anthracite.edit.history
     * @type {object}
     * @example
     *
     *
     * Add Example here ...
     *
     *
     */
     history: {
         /**
          * Stores data for Object
          * @memberof Anthracite.edit.history
          * @type {object}
          * @example
          *
          *
          * Add Example here ...
          *
          *
          */
        data: {},
        /**
         * XXX
         * @memberof Anthracite.edit.history
         * @method add
         * @param {string} type XXX
         * @param {string} node XXX

         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        add: function (type, node) {
            Anthracite.dev.log('::: APP ::: EDIT ::: HISTORY ::: ADD');
            Anthracite.edit.history.data[type] = node;
        },
        /**
         * XXX
         * @memberof Anthracite.edit.history
         * @method get
         * @param {string} type XXX
         * @returns {object} XXX

         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        get: function (type) {
            Anthracite.dev.log('::: APP ::: EDIT ::: HISTORY ::: GET');

            var returnResult = null;
            if (Anthracite.edit.history.data[type]) {
                var stillInVisibleDOM = document.body.contains(Anthracite.edit.history.data[type]);
                if (stillInVisibleDOM) {
                    returnResult = Anthracite.edit.history.data[type];
                }
            }

            return returnResult;
        },
        /**
         * XXX
         * @memberof Anthracite.edit.history
         * @method reset

         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        reset: function () {
            Anthracite.dev.log('::: APP ::: EDIT ::: HISTORY ::: RESET');
            Anthracite.edit.history.data = {
                settingspage: null,
                editpage: null
            };
        }
    },
    /**
     * XXX
     * @memberof Anthracite.edit
     * @property {object} publicationButtonContainer XXX <br> See {@link Anthracite.edit.topbar.publicationButtonContainer}
     * @property {object} publicationButtonArrow XXX <br> See {@link Anthracite.edit.topbar.publicationButtonArrow}
     * @property {object} previewButtonContainer XXX <br> See {@link Anthracite.edit.topbar.previewButtonContainer}
     * @property {object} previewButtonArrow XXX <br> See {@link Anthracite.edit.topbar.previewButtonArrow}
     * @property {method} build Builds the top bar in Edit Mode
     * @property {method} reposition Callback executes when the window resizes
     * @namespace Anthracite.edit.topbar
     * @type {object}
     * @example
     *
     *
     * Add Example here ...
     *
     *
     */
    topbar: {
        /**
         * XXX
         * @memberof Anthracite.edit.topbar
         * @property {method} onMouseOver Callback executed when user hovers the Publication Button Container
         * @property {method} onMouseOut Callback executed when user leaves the Publication Button Container
         * @namespace Anthracite.edit.topbar.publicationButtonContainer
         * @type {object}
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        publicationButtonContainer: {
            /**
            * Callback executed when user hovers the Publication Button Container
            * @memberof Anthracite.edit.topbar.publicationButtonContainer
            * @method onMouseOver

            * @example
            *
            *
            * Add Example here ...
            *
            *
            */
            onMouseOver: function () {
                // Add class to Menu Group to allow advanced CSS styling of the Publish Button
                var editMenuCentertop = document.querySelectorAll('.edit-menu-centertop')[0];
                editMenuCentertop.classList.add('hover-publish');
            },
            /**
             * Callback executed when user leaves the Publication Button Container
             * @memberof Anthracite.edit.topbar.publicationButtonContainer
             * @method onMouseOut

             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            onMouseOut: function () {
                // Anthracite.edit.topbar.publicationButtonContainer.onMouseOver
                var editMenuCentertop = document.querySelectorAll('.edit-menu-centertop')[0];
                editMenuCentertop.classList.remove('hover-publish');
            }
        },
        /**
         * XXX
         * @memberof Anthracite.edit.topbar
         * @property {method} onMouseOver Callback execute when the user hover the arrow on the publication button
         * @property {method} onMouseOut Callback execute when the user leaves the arrow on the publication button
         * @namespace Anthracite.edit.topbar.publicationButtonArrow
         * @type {object}
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        publicationButtonArrow: {
            /**
             * Callback execute when the user hover the arrow on the publication button
             * @memberof Anthracite.edit.topbar.publicationButtonArrow
             * @method onMouseOver

             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            onMouseOver: function () {
                // Add class to Menu Group to allow advanced CSS styling of the Publish Button
                var editMenuCentertop = document.querySelectorAll('.edit-menu-centertop')[0];
                editMenuCentertop.classList.add('hover-publish-advanced');
            },
            /**
             * Callback execute when the user leaves the arrow on the publication button
             * @memberof Anthracite.edit.topbar.publicationButtonArrow
             * @method onMouseOut

             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            onMouseOut: function () {
                // Remove class to Menu Group to allow advanced CSS styling of the Publish Button
                var editMenuCentertop = document.querySelectorAll('.edit-menu-centertop')[0];
                editMenuCentertop.classList.remove('hover-publish-advanced');
            }
        },
        /**
         * XXX
         * @memberof Anthracite.edit.topbar
         * @property {method} onMouseOver Callback execute when the use hovers the preview button container
         * @property {method} onMouseOut XXX
         * @namespace Anthracite.edit.topbar.previewButtonContainer
         * @type {object}
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        previewButtonContainer: {
            /**
             * Callback execute when the use hovers the preview button container
             * @memberof Anthracite.edit.topbar.previewButtonContainer
             * @method onMouseOver

             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            onMouseOver: function () {
                // Add class to Menu Group to allow advanced CSS styling of the Preview Button
                var editMenuCentertop = document.querySelectorAll('.edit-menu-centertop')[0];
                editMenuCentertop.classList.add('hover-preview');
            },
            /**
             * XXX
             * @memberof Anthracite.edit.topbar.previewButtonContainer
             * @method onMouseOut

             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            onMouseOut: function () {
                // Remove class to Menu Group to allow advanced CSS styling of the Preview Button
                var editMenuCentertop = document.querySelectorAll('.edit-menu-centertop')[0];
                editMenuCentertop.classList.remove('hover-preview');
            }
        },
        /**
         * XXX
         * @memberof Anthracite.edit.topbar
         * @property {method} onMouseOver Callback executed when user hovers the Preview Arrow button
         * @property {method} onMouseOut Callback executed when user leaves the Preview Arrow button
         * @namespace Anthracite.edit.topbar.previewButtonArrow
         * @type {object}
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        previewButtonArrow: {
            /**
             * Callback executed when user hovers the Preview Arrow button
             * @memberof Anthracite.edit.topbar.previewButtonArrow
             * @method onMouseOver

             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            onMouseOver: function () {
                // Add class to Menu Group to allow advanced CSS styling of the Preview Button
                var editMenuCentertop = document.querySelectorAll('.edit-menu-centertop')[0];
                editMenuCentertop.classList.add('hover-preview-advanced');
            },
            /**
             * Callback executed when user leaves the Preview Arrow button
             * @memberof Anthracite.edit.topbar.previewButtonArrow
             * @method onMouseOut

             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            onMouseOut: function () {
                // Remove class to Menu Group to allow advanced CSS styling of the Preview Button
                var editMenuCentertop = document.querySelectorAll('.edit-menu-centertop')[0];
                editMenuCentertop.classList.remove('hover-preview-advanced');
            }
        },
         /**
          * Builds the top bar in Edit Mode
          * @memberof Anthracite.edit.topbar
          * @method build
          * @returns {string|undefined} XXX
          * @example
          *
          *
          * Add Example here ...
          *
          *
          */
        build: function () {
            Anthracite.dev.log('::: APP ::: EDIT ::: TOPBAR ::: BUILD');

            // TEMP BLIND
            if (Anthracite.data.currentApp == 'edit' || Anthracite.data.currentApp == 'contribute') {
                var elements = {
                    title: document.getElementsByClassName('x-current-page-path')[0],
                    previewButton: document.getElementsByClassName('edit-menu-view')[0],
                    moreInfo: document.getElementsByClassName('edit-menu-edit')[0]
                };

                if (elements.title && elements.title.style) {
                    elements.title.style.opacity = 1;
                }

                if (elements.previewButton && elements.previewButton.style) {
                    elements.previewButton.style.opacity = 1;
                }

                if (elements.moreInfo && elements.moreInfo.style) {
                    elements.moreInfo.style.opacity = 1;
                }

                var pageTitle;
                var selectType = 'none';
                var multiselect = 'off';
                var publicationStatus = document.querySelectorAll('.toolbar-item-publicationstatuswithtext .gwt-Image')[0];

                var extractStatus = function (url) {
                    var urlSplit = url.split('/');
                    var fileName = urlSplit[urlSplit.length - 1];
                    var statusSplit = fileName.split('.png');

                    return statusSplit[0];
                };

                // Presumably in Edit Mode or Contribute Mode, in which case we need to set the page title
                switch (Anthracite.iframe.data.selectionCount) {
                    case 0:
                        pageTitle = Anthracite.iframe.data.displayName;
                        selectType = 'none';
                        break;
                    case 1:
                        pageTitle = jGet.getCached('body').getAttribute('data-singleselection-node-displayname');
                        multiselect = 'on';
                        selectType = 'single';
                        break;
                    default:
                        pageTitle = Anthracite.dictionary.get('optionsMultipleSelection').replace('{{count}}', Anthracite.iframe.data.selectionCount);
                        multiselect = 'on';
                        selectType = 'multiple';
                        break;
                }

                // Set multiselect status in body attribute...
                jGet.getCached('body')
                    .setAttribute('data-multiselect', multiselect)
                    .setAttribute('data-select-type', selectType);

                // Page Title in Edit Made
                if (pageTitle) {
                    jGet.class('x-current-page-path').setAttribute('data-PAGE-NAME', pageTitle);
                }
                jGet.class('node-path-text-inner').setHTML(Anthracite.iframe.data.displayName);

                // Determine publication status
                if (publicationStatus) {
                    Anthracite.iframe.data.publication = {
                        status: extractStatus(publicationStatus.getAttribute('src')),
                        label: publicationStatus.getAttribute('title')
                    };
                } else {
                    Anthracite.iframe.data.publication = {
                        status: null,
                        label: null
                    };
                }

                Anthracite.dev.log('::: Anthracite.iframe.data.publication.status [\'' + Anthracite.iframe.data.publication.status + '\']');

                Anthracite.iframe.data.pageTitle = pageTitle;

                // Page Titles need centering
                Anthracite.edit.topbar.reposition();

                if(jGet.class('window-actions-refresh').exists() && window.anthraciteV8 && jGet.class('action-bar-container').exists()) {
                    var position = jGet.class('action-bar-container').nodes[0].style.left.substring(0, 3) - 40;
                    jGet.class('window-actions-refresh').nodes[0].style.setProperty('left', position + 'px', 'important');
                }

                if(jGet.class('x-border-panel').exists() && window.anthraciteV8) {
                    jGet.class('x-border-panel').nodes[3].style.setProperty('left', '251px', 'important');
                    jGet.class('x-border-panel').nodes[3].style.setProperty('width', 'calc(100% - 245px)', 'important');
                }
            }
        },
        /**
         * Callback executes when the window resizes
         * @memberof Anthracite.edit.topbar
         * @method reposition

         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        reposition: function () {
            Anthracite.dev.log('::: APP ::: EDIT ::: TOPBAR ::: REPOSITION');

            // Center title to page and move surrounding menus to right and left.
            Anthracite.edit.sidepanel.resizeSidePanel();

            var offset = (jGet.getCached('body').getAttribute('data-indigo-sidepanel-pinned') == 'true') ? 160 : 0;

            if (document.getElementsByClassName('x-current-page-path').length > 0) {
                if (jGet.class('x-current-page-path').getAttribute('data-page-name') != null) {

                    var elements = {
                        body: document.getElementsByTagName('body')[0],
                        title: document.getElementsByClassName('x-current-page-path')[0],
                        innerTitle: document.getElementsByClassName('node-path-text-inner')[0],
                        publishButton: document.getElementsByClassName('edit-menu-publication')[0],
                        refreshButton: document.getElementsByClassName('window-actions-refresh')[0],
                        nodePathTitle: document.getElementsByClassName('node-path-title')[0],
                        previewButton: document.getElementsByClassName('edit-menu-view')[0],
                        moreInfo: document.getElementsByClassName('edit-menu-edit')[0]
                    };

                    var boxes = {
                        body: elements.body.getBoundingClientRect(),
                        title: elements.title.getBoundingClientRect()
                    };

                    if(elements.publishButton){
                        document.getElementsByClassName('edit-menu-publication')[0].style.display = 'block';
                    }

                    // Center Page Title
                    if(elements.title && boxes.body && boxes.title){
                        elements.title.style.left = (((boxes.body.width / 2) - (boxes.title.width / 2)) + offset - 30) + 'px';
                    }

                    if (elements.innerTitle && boxes.body) {
                        // Get Inner title bunding box
                        boxes.innerTitle = elements.innerTitle.getBoundingClientRect();

                        // Center Inner title bounding box
                        elements.innerTitle.style.left = ((boxes.body.width / 2) - (boxes.innerTitle.width / 2)) + 5 + offset + 'px';
                    }

                    // Refresh bounding box for title as it has moved
                    if(elements.title){
                        boxes.title = elements.title.getBoundingClientRect();
                    }

                    if (Anthracite.iframe.data.selectionCount > 0 && boxes.title) {
                        // Multiselect, so display differently
                        if(elements.previewButton){
                            elements.previewButton.style.left = (boxes.title.left + boxes.title.width + 10) + 'px';
                        }

                        if(elements.moreInfo){
                            elements.moreInfo.style.left = (boxes.title.left + boxes.title.width + 30) + 'px';
                        }

                        if(elements.nodePathTitle){
                            elements.nodePathTitle.style.left = (boxes.title.left - 20) + 'px';
                        }

                        jGet('.edit-menu-publication .x-btn-mc').setAttribute('data-publication-label', Anthracite.iframe.data.pageTitle);
                    } else {
                        // No Select

                        if(boxes.title){
                            if (elements.refreshButton) {
                                elements.refreshButton.style.left = (boxes.title.left + boxes.title.width) + 'px';
                            }

                            if(elements.previewButton){
                                elements.previewButton.style.left = (boxes.title.left + boxes.title.width + 39) + 'px';
                            }

                            if(elements.moreInfo){
                                elements.moreInfo.style.left = (boxes.title.left + boxes.title.width + 63) + 'px';
                            }

                            if(elements.nodePathTitle){
                                elements.nodePathTitle.style.left = (boxes.title.left - 20) + 'px';
                                elements.nodePathTitle.setAttribute('data-indigo-file-path', jGet.getCached('body').getAttribute('data-main-node-path'));
                            }
                        }

                        jGet('.edit-menu-publication .x-btn-mc').setAttribute('data-publication-label', Anthracite.iframe.data.publication.label);
                    }

                    // Make sure correct class is added to publication button
                    if (elements.publishButton) {
                        elements.publishButton.setAttribute('data-publication-status', Anthracite.iframe.data.publication.status);
                    }

                    if (jGet.class('publication-status').exists()) {
                        jGet.class("publication-status-path").setHTML(jGet.getCached('body').getAttribute('data-main-node-path'));
                        jGet.class("publication-status-label").setHTML(jahia_gwt_messages["label_publication_" + Anthracite.iframe.data.publication.status]);
                        jGet.class('publication-status').setAttribute('data-publication-status', Anthracite.iframe.data.publication.status);
                    }
                } else {
                    if(document.getElementsByClassName('edit-menu-publication')[0]){
                        document.getElementsByClassName('edit-menu-publication')[0].style.display = 'none';

                    }
                }
            }
        }
    },
     /**
      * Everything here concerns the Side Panel
      * @memberof Anthracite.edit
      * @property {object} data XXX
      * @property {object} browseTree XXX <br> See {@link Anthracite.edit.sidepanel.browseTree}
      * @property {object} thumbPanel XXX <br> See {@link Anthracite.edit.sidepanel.thumbPanel}
      * @property {object} resultsPanel XXX <br> See {@link Anthracite.edit.sidepanel.resultsPanel}
      * @property {object} siteSelector XXX <br> See {@link Anthracite.edit.sidepanel.siteSelector}
      * @property {method} resizeSidePanel Callback executed when the side panel is resized
      * @property {method} onStartResize Callback executed once when the user starts to resize the side panel
      * @property {method} onResize Callback executes whilst the side panel is being resized
      * @property {method} onStopResize Callback is executed when the user stops resizing the side panel
      * @property {method} onNewChannel Callback executed when the user changes the Mobile View
      * @property {method} onWindowResize Callback executed when the window resizes
      * @property {method} initChannels Builds the Channel controls
      * @property {method} zoomChannel Callback executed when the user zooms on the Channel View
      * @property {method} togglePin Callback executed when the user toggle the Side Panel Pin
      * @property {method} toggleFloatingPanel XXX
      * @property {method} onDrag Callback executed when the user starts to drag an element from the Side Panel
      * @property {method} buildSplitter Builds a splitter used for controlling the width of the side panel
      * @property {method} clipPageTitle Used to hide the title of the page when the side panel is opened
      * @property {method} open Opens the Side Panel
      * @property {method} close Closes the Side Panel
      * @property {method} isOpen Used to determine whether or not the side panel is opened
      * @property {object} tabs XXX
      * @property {object} tab XXX
      * @property {object} row XXX
      * @property {object} tab XXX
      * @property {object} tab XXX
      * @namespace Anthracite.edit.sidepanel
      * @type {object}
      * @example
      *
      *
      * Add Example here ...
      *
      *
      */
    sidepanel: {
        /**
         * Stores data for Object
         * @memberof Anthracite.edit.sidepanel
         * @property {boolean} open XXX
         * @property {boolean} currentTab XXX
         * @property {boolean} previousTab XXX
         * @property {boolean} pinned XXX
         * @property {boolean} firstRun XXX
         * @property {object} channel XXX
         * @property {boolean} channel.autofit XXX
         * @property {boolean} channel.opened XXX
         * @type {object}
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        data: {
            open: false,
            currentTab: null,
            previousTab: null,
            pinned: false,
            firstRun: true,
            channel: {
                autofit: false,
                opened: false
            }
        },
        /**
         * XXX
         * @memberof Anthracite.edit.sidepanel
         * @property {method} onEmpty XXX
         * @property {method} onResults XXX
         * @namespace Anthracite.edit.sidepanel.browseTree
         * @type {object}
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        browseTree: {
            /**
             * XXX
             * @memberof Anthracite.edit.sidepanel.browseTree
             * @method onEmpty

             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            onEmpty: function () {
                // Hide Edit Mode > Side Panel > [Category | Content] > Drawer if nothing to display
                if (Anthracite.edit.sidepanel.data.open) {
                    var isTreeEntry = jGet.node(this).parent().hasClass('results-column');
                    if (isTreeEntry) {
                        switch (Anthracite.edit.sidepanel.data.currentTab) {
                            case 'JahiaGxtSidePanelTabs__JahiaGxtCategoryBrowseTab':
                                jGet.id('JahiaGxtCategoryBrowseTab').removeClass('show-results');
                                break;
                            case 'JahiaGxtSidePanelTabs__JahiaGxtContentBrowseTab':
                                jGet.id('JahiaGxtContentBrowseTab').removeClass('show-results');
                                break;
                        }

                        jGet.getCached('body').removeClass('show-results');
                    }
                }
            },
            /**
             * XXX
             * @memberof Anthracite.edit.sidepanel.browseTree
             * @method onResults

             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            onResults: function () {
                if (Anthracite.edit.sidepanel.data.open) {
                    if (jGet.getCached('body').getAttribute('data-INDIGO-GWT-SIDE-PANEL') == 'open' && jGet.getCached('body').getAttribute('data-INDIGO-SIDEPANEL-PINNED') != 'true') {
                        Anthracite.edit.sidepanel.resizeSidePanel();
                    }

                    switch(Anthracite.edit.sidepanel.data.currentTab){
                        case 'JahiaGxtSidePanelTabs__JahiaGxtCategoryBrowseTab':
                            jGet.id('JahiaGxtCategoryBrowseTab').addClass('show-results');
                            break;
                        case 'JahiaGxtSidePanelTabs__JahiaGxtContentBrowseTab':
                            jGet.id('JahiaGxtContentBrowseTab').addClass('show-results');
                            break;
                        case 'JahiaGxtSidePanelTabs__JahiaGxtSearchTab':
                            jGet.id('JahiaGxtSearchTab').addClass('show-results');
                            break;
                    }

                    jGet.getCached('body').addClass('show-results');
                }
            }
        },
        /**
         * XXX
         * @memberof Anthracite.edit.sidepanel
         * @property {method} onClose XXX
         * @property {method} onOpen XXX
         * @namespace Anthracite.edit.sidepanel.thumbPanel
         * @type {object}
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        thumbPanel: {
            /**
             * XXX
             * @memberof Anthracite.edit.sidepanel.thumbPanel
             * @method onClose

             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            onClose: function () {
                if (Anthracite.edit.sidepanel.data.open) {
                    jGet.id('JahiaGxtFileImagesBrowseTab').removeClass('show-results');
                    jGet.getCached('body').removeClass('show-results');
                }
            },
            /**
             * XXX
             * @memberof Anthracite.edit.sidepanel.thumbPanel
             * @method onOpen

             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            onOpen: function () {
                if (Anthracite.edit.sidepanel.data.open) {
                    var isTreeEntry = jGet.node(this).parent().hasClass('results-column');

                    if (isTreeEntry) {
                        jGet.id('JahiaGxtFileImagesBrowseTab').addClass('show-results');
                        jGet.getCached('body').addClass('show-results');
                    }
                }
            }
        },
        /**
         * XXX
         * @memberof Anthracite.edit.sidepanel
         * @property {method} onEmpty XXX
         * @property {method} onResults XXX
         * @namespace Anthracite.edit.sidepanel.resultsPanel
         * @type {object}
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        resultsPanel: {
            /**
             * XXX
             * @memberof Anthracite.edit.sidepanel.resultsPanel
             * @method onEmpty

             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            onEmpty: function () {
                // Display empty list message
                var myPagingDisplay = jGet('#JahiaGxtSearchTab .my-paging-display');
                if (myPagingDisplay.exists()) {
                    var pagingValue = myPagingDisplay.nodes[0].innerHTML;
                    var noResults = pagingValue === 'No data to display' || pagingValue === 'Aucune donnée à afficher' || pagingValue === 'Keine Daten vorhanden';
                    var status = null;

                    if (noResults) {
                        status = 'no-results';
                    } else if (pagingValue === '') {
                        status = 'init';
                    } else {
                        status = 'searching';
                    }

                    if (Anthracite.edit.data.search.status !== status) {
                        Anthracite.edit.data.search.status = status;
                        jGet.id('JahiaGxtSearchTab').filter('.results-column').setAttribute('data-results-status', Anthracite.edit.data.search.status);
                    }
                }
            },
            /**
             * XXX
             * @memberof Anthracite.edit.sidepanel.resultsPanel
             * @method onResults

             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            onResults: function () {
                var status = 'results';
                if (Anthracite.edit.data.search.status !== status) {
                    Anthracite.edit.data.search.status = status;
                    jGet.id('JahiaGxtSearchTab').filter('.results-column').setAttribute('data-results-status', Anthracite.edit.data.search.status);
                }

            }
        },
        /**
         * XXX
         * @memberof Anthracite.edit.sidepanel
         * @property {method} blur Removes focus from the site selector
         * @property {method} onClick Callback executed when the user clicks on the Site Selector text input field
         * @property {method} onKeyUp Callback executed after the user presses a key (onKeyUp)
         * @namespace Anthracite.edit.sidepanel.siteSelector
         * @type {object}
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        siteSelector: {
            /**
             * Removes focus from the site selector
             * @memberof Anthracite.edit.sidepanel.siteSelector
             * @method blur

             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            blur: function(){
               setTimeout(function(){
                   jGet(".edit-menu-sites").trigger("mousedown").trigger("mouseup");
               }, 50);
           },
           /**
            * Callback executed when the user clicks on the Site Selector text input field
            * @memberof Anthracite.edit.sidepanel.siteSelector
            * @method onClick

            * @example
            *
            *
            * Add Example here ...
            *
            *
            */
            onClick: function(){
                // This opens the drop down menu when the user clicks on the text input
                var arrowButton = this.nextElementSibling;

                jGet.node(arrowButton).trigger('click');
            },
            /**
             * Callback executed after the user presses a key (onKeyUp)
             * - Detects up and down arrow keys (navigates through the menu items)
             * - Detects Enter key (will trigger click the menu item that matches text in the input field)
             * @memberof Anthracite.edit.sidepanel.siteSelector
             * @method onKeyUp
             * @param {keyEvent} e XXX

             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            onKeyUp: function(e){
               var textInput = this,
                   skip = function(direction){
                       var currentEntry = jGet('.menu-edit-menu-sites .x-combo-list-item.x-view-highlightrow').nodes[0],
                           skipTo = (direction === 'up') ? currentEntry.previousElementSibling : currentEntry.nextElementSibling,
                           skipToValue = (skipTo) ? skipTo.innerHTML : null;

                       if(skipToValue){
                           jGet.node(currentEntry).removeClass('x-view-highlightrow');
                           jGet.node(skipTo).addClass('x-view-highlightrow');
                           textInput.value = skipToValue;
                           Anthracite.common.resizeSiteSelector();
                       }
                   };

               switch(e.keyCode){
                   // UP Arrow
                   case 38:
                       skip('up');
                       break;

                   // DOWN Arrow
                   case 40:
                       skip('down');
                       break;

                   // ENTER Key
                   case 13:
                       var dropDownEntries = jGet('.menu-edit-menu-sites .x-combo-list-item'),
                       siteName = e.target.value;

                       // Loop through entries to find the predicted site name and click it
                       dropDownEntries.each(function(){
                       if(this.innerHTML === siteName){
                           // GWT fires on the MouseDown / MouseUp events and not Click event
                           jGet.node(this)
                               .trigger('mousedown')
                               .trigger('mouseup');
                           }
                       });
                       break;
               }

            }
        },
         /**
          * Callback executed when the side panel is resized
          * @memberof Anthracite.edit.sidepanel.siteSelector
          * @method resizeSidePanel
          * @param {integer} xPos XXX
          * @returns {boolean|undefined} XXX
          * @example
          *
          *
          * Add Example here ...
          *
          *
          */
        resizeSidePanel: function (xPos) {
            Anthracite.dev.log('APP ::: SIDEPANEL ::: RESIZESIDEPANEL (' + xPos + ')');

            xPos = xPos || function () {
                var splitter = jGet.id('indigoSplitter');
                var splitterXPos = (splitter.exists()) ? parseInt(splitter.nodes[0].style.getPropertyValue('left')) : null;

                return splitterXPos || null;
            }();

            if (xPos == null || jGet.getCached('body').getAttribute('data-indigo-gwt-side-panel') !== 'open') {
                return false;
            }

            // Block the minimum and maximum widths of the side panel
            if (xPos < 245) {
                // Block at minimum width
                xPos = 245;

                // This is the minimum it can go, so change to an east only cursor when hovering the split bar
                jGet.id('indigoSplitter').addClass('move-east-only');
            } else if (xPos > 500) {
                // Block at maximum width
                xPos = 500;

                // This is the maximum it can go, so change to an west only cursor when hovering the split bar
                jGet.id('indigoSplitter').addClass('move-west-only');
            } else {
                // The split bar can be made smaller or bigger, so just display the normal cursor when hovering
                jGet.id('indigoSplitter').removeClass('move-east-only');
                jGet.id('indigoSplitter').removeClass('move-west-only');
            }

            // Reposition the main frame
            var dashboardMode = Anthracite.data.currentApp === 'dashboard';
            var settingsMode = jGet.getCached('body').getAttribute('data-edit-window-style') == 'settings'
                || jGet.getCached('body').getAttribute('data-sitesettings') == 'true'
                || jGet.getCached('body').getAttribute('data-indigo-app') == 'admin';
            var mainFrameWidth = (settingsMode || dashboardMode) ? xPos - 68 : xPos + 5;
            var mainFrameLeft = (settingsMode || dashboardMode) ? xPos : xPos + 10;

            var pageTitle;
            var pageTitleBox;
            var siteSettings = jGet.getCached('body').getAttribute('data-sitesettings') === 'true';
            if (siteSettings || Anthracite.data.currentApp === 'admin' || Anthracite.data.currentApp === 'dashboard') {
                // Site Settings, Admin or Dashboard

                mainFrameWidth = xPos - 48;
                mainFrameLeft = xPos + 21;

                jGet('.mainmodule > div:nth-child(2)').nodes[0].style.setProperty('width', 'calc(100% - ' + mainFrameWidth + 'px)',
                    'important');
                jGet('.mainmodule > div:nth-child(2)').nodes[0].style.setProperty('left', mainFrameLeft + 'px', 'important');

            } else if (Anthracite.edit.sidepanel.data.pinned) {
                // Edit Mode pinned
                mainFrameWidth = xPos + 6;
                mainFrameLeft = xPos + 45;

                if (jGet.class('publication-status').exists() && !window.anthraciteV8) {
                    jGet.class('publication-status').nodes[0].style.setProperty('left', mainFrameLeft + 'px', 'important');
                }

                jGet('.mainmodule > div:nth-child(2)').nodes[0].style.setProperty('width', 'calc(100% - ' + mainFrameWidth + 'px)', 'important');
                jGet('.mainmodule > div:nth-child(2)').nodes[0].style.setProperty('left', mainFrameLeft + 'px', 'important');

                // Title
                if (jGet.class('node-path-container').exists()) {
                    jGet.class('node-path-container').nodes[0].style.setProperty('left', (mainFrameLeft - 5) + 'px', 'important');
                }

                if (jGet.class('node-path-text').exists()) {
                    jGet.class('node-path-text').nodes[0].style.setProperty('max-width', 'calc(100vw - ' + (mainFrameLeft + 60 + 380) + 'px)', 'important');
                }

                pageTitle = document.getElementsByClassName('x-current-page-path')[0];
                pageTitleBox = (pageTitle) ? pageTitle.getBoundingClientRect() : null;

                if (jGet.class('window-actions-refresh').exists() && pageTitleBox) {
                    //jGet.class('window-actions-refresh').nodes[0].style.setProperty('left', (pageTitleBox.left + pageTitleBox.width) + 'px', 'important');
                }


            } else if (Anthracite.data.currentApp === 'edit') {
                // Edit Mode Unpinned
                jGet('.mainmodule > div:nth-child(2)').nodes[0].style.removeProperty('width');
                jGet('.mainmodule > div:nth-child(2)').nodes[0].style.removeProperty('left');

                // Title
                if (jGet.class('publication-status').exists()) {
                    jGet.class('publication-status').nodes[0].style.removeProperty('left');
                }

                if (jGet.class('node-path-container').exists() && jGet.class('node-path-text').exists()) {
                    jGet.class('node-path-container').nodes[0].style.removeProperty('left');
                    jGet.class('node-path-text').nodes[0].style.removeProperty('max-width');
                }

                pageTitle = document.getElementsByClassName('x-current-page-path')[0];
                pageTitleBox = (pageTitle) ? pageTitle.getBoundingClientRect() : null;

                if (jGet.class('window-actions-refresh').exists() && pageTitleBox) {
                    //jGet.class('window-actions-refresh').nodes[0].style.setProperty('left', (pageTitleBox.left + pageTitleBox.width) + 'px', 'important');
                }
            }

            // Reposition the pin button
            if (jGet.class('side-panel-pin').exists()) {
                jGet.class('side-panel-pin').css({
                    left: (xPos - 45) + 'px'
                });
            }

            if (jGet.id('JahiaGxtRefreshSidePanelButton')) {
                jGet.id('JahiaGxtRefreshSidePanelButton').css({
                    left: (xPos - 85) + 'px'
                });
            }

            // Set position of content create text filter
            var contentCreateFilter = jGet('#JahiaGxtCreateContentTab > .x-border-layout-ct > .x-form-field-wrap');

            if (contentCreateFilter.exists()) {
                contentCreateFilter.nodes[0].style.setProperty('width', (xPos - 109) + 'px', 'important');
            }

            // Set position of Results panels
            var categoriesResultsPane = jGet('#JahiaGxtCategoryBrowseTab.tab_categories .x-box-inner .x-box-item:nth-child(2)');
            var searchResultPane = jGet('#JahiaGxtSearchTab.tab_search .JahiaGxtSearchTab-results .x-panel-bwrap');
            var imagesResultPane = jGet('#JahiaGxtFileImagesBrowseTab.tab_filesimages #images-view');
            var contentResultsPane = jGet('#JahiaGxtContentBrowseTab.tab_content .x-box-inner .x-box-item:nth-child(2)');
            var xPosOffset = 20;

            if (searchResultPane.exists()) {
                searchResultPane.nodes[0].style.setProperty('left', (xPos + xPosOffset) + 'px', 'important');
            }

            if (categoriesResultsPane.exists()) {
                categoriesResultsPane.nodes[0].style.setProperty('left', (xPos + xPosOffset) + 'px', 'important');
            }

            if (imagesResultPane.exists()) {
                imagesResultPane.nodes[0].style.setProperty('left', (xPos + xPosOffset) + 'px', 'important');
            }

            if (contentResultsPane.exists()) {
                contentResultsPane.nodes[0].style.setProperty('left', (xPos + xPosOffset) + 'px', 'important');
            }

            // Set width of the Side Panel
            if (jGet.id('JahiaGxtSidePanelTabs').exists()) {
                jGet.id('JahiaGxtSidePanelTabs').nodes[0].style.setProperty('width', xPos + 'px', 'important');
            }

             if(jGet.class('action-bar-container').exists() && window.anthraciteV8) {
                 jGet.class('action-bar-container').nodes[0].style.setProperty('width', 'calc(100% - ' + xPos + 'px)', 'important');
                 jGet.class('action-bar-container').nodes[0].style.setProperty('left', xPos + 'px', 'important');
             }

             if(jGet.class('window-actions-refresh').exists() && window.anthraciteV8) {
                 jGet.class('window-actions-refresh').nodes[0].style.setProperty('left', (xPos - 40) + 'px', 'important');
             }

             if(jGet.class('x-border-panel').exists() && window.anthraciteV8) {
                 jGet.class('x-border-panel').nodes[3].style.setProperty('left', (xPos + 6) + 'px', 'important');
                 jGet.class('x-border-panel').nodes[3].style.setProperty('width', 'calc(100% - ' + xPos + ')', 'important');
             }

            // Move the split bar to the position of the mouse
            if (jGet.id('indigoSplitter').exists()) {
                jGet.id('indigoSplitter').nodes[0].style.setProperty('left', xPos + 'px', 'important');
            }
        },
        /**
         * Callback executed once when the user starts to resize the side panel
         * @memberof Anthracite.edit.sidepanel.siteSelector
         * @method onStartResize

         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        onStartResize: function () {
            Anthracite.dev.log('::: APP ::: EDIT ::: SIDEPANEL ::: ONSTARTRESIZE');

            // Register that the side panel is being resized ( CSS uses this to remove pointer events on iframe )
            jGet.getCached('body').setAttribute('indigo-dragging-panel', 'true');

            // Cancel the resizing when the mouse is released
            document.onmouseup = Anthracite.edit.sidepanel.onStopResize;

            // Update the width of the Side panel when mouse is being moved
            document.onmousemove = Anthracite.edit.sidepanel.onResize;
        },
        /**
         * Callback executes whilst the side panel is being resized
         * @memberof Anthracite.edit.sidepanel.siteSelector
         * @method onResize

         * @param {windowEvent} e XXX
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        onResize: function (e) {
            Anthracite.dev.log('::: APP ::: EDIT ::: SIDEPANEL ::: ONRESIZE');
            e = e || window.event;

            // Get position of Split bar that will be used to calculate the width of the side panel:
            var xPos = e.clientX;

            Anthracite.edit.sidepanel.resizeSidePanel(xPos);
            Anthracite.edit.sidepanel.clipPageTitle();
        },
        /**
         * Callback is executed when the user stops resizing the side panel
         * @memberof Anthracite.edit.sidepanel.siteSelector
         * @method onStopResize

         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        onStopResize: function () {
            // Stop listening to the mouse and kill mousemove and mouseup listeners
            document.onmousemove = null;
            document.onmouseup = null;

            // Unregister the resizing of the side panel ( CSS will now remove the no pointer events on the iframe )
            jGet.getCached('body').setAttribute('indigo-dragging-panel', '');
        },
        /**
         * Callback executed when the user changes the Mobile View
         * @memberof Anthracite.edit.sidepanel.siteSelector
         * @method onNewChannel

         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        onNewChannel: function () {
            // Dev note: This is also triggered when the user changes pages by navigation in Device Channel Preview
            if (Anthracite.edit.sidepanel.data.channel.opened) {
                jGet.id('channel-auto-fit-button').addClass('selected');
                jGet.id('channel-zoom-button').removeClass('selected');
                jGet.id('channel-size-slider-holder').addClass('disabled');

                Anthracite.edit.sidepanel.zoomChannel(0);
                Anthracite.edit.sidepanel.data.channel.autofit = true;

                Anthracite.edit.sidepanel.close();

                jGet('.mainmodule > div:nth-child(2)').removeClass('channel-zoom');
            }
        },
        /**
         * Callback executed when the window resizes
         * @memberof Anthracite.edit.sidepanel.siteSelector
         * @method onWindowResize

         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        onWindowResize: function () {
            if (Anthracite.edit.sidepanel.data.channel.opened
                && Anthracite.edit.sidepanel.data.channel.autofit) {
                Anthracite.edit.sidepanel.zoomChannel(0);
            }
        },
        /**
         * Builds the Channel controls
         * @memberof Anthracite.edit.sidepanel.siteSelector
         * @method initChannels

         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        initChannels: function () {
            // Force GWT to load the GWT tab for channels
            // Remove just incase already been added ( can happen when returning to Edit Mode from another app)
            jGet.id('channel-menu').remove();

            jGet.id('JahiaGxtSidePanelTabs__JahiaGxtChannelsTab').trigger('click');

            // Build the Channels bar
            var channelMenu = document.createElement('menu');
            var channelCloseButton = document.createElement('button');
            var channelZoomHolder = document.createElement('div');
            var channelAutoFitButton = document.createElement('div');
            var channelAutoFitButtonLabel = document.createTextNode('Autofit');
            var channelZoomButton = document.createElement('div');
            var channelZoomButtonLabel = document.createTextNode('Default');
            var channelSlider = document.createElement('input');
            var channelSliderHolder = document.createElement('div');
            var channelTitle = document.createElement('div');
            var channelOrientaion = document.createElement('div');

            // CLose button
            channelCloseButton.id = 'channel-close-button';

            // Channel Menu
            channelMenu.id = 'channel-menu';

            // Channel Holder
            channelZoomHolder.id = 'channel-zoom-holder';

            // Channel Title
            channelTitle.id = 'channel-title';

            // Auto fit button
            channelAutoFitButton.appendChild(channelAutoFitButtonLabel);
            channelAutoFitButton.id = 'channel-auto-fit-button';

            // Auto fit button
            channelZoomButton.appendChild(channelZoomButtonLabel);
            channelZoomButton.id = 'channel-zoom-button';
            channelZoomButton.classList.add('selected');

            // Orientation button
            channelOrientaion.id = 'channel-orientation';

            // Channel Slider Holder
            channelSliderHolder.id = 'channel-size-slider-holder';

            // Channel Slider
            channelSlider.id = 'channel-size-slider';
            channelSlider.type = 'range';
            channelSlider.value = 100;
            channelSlider.min = 30;
            channelSlider.max = 100;

            // Stick them together
            channelSliderHolder.appendChild(channelSlider);
            channelZoomHolder.appendChild(channelAutoFitButton);
            channelZoomHolder.appendChild(channelZoomButton);
            channelZoomHolder.appendChild(channelSliderHolder);
            channelMenu.appendChild(channelCloseButton);
            channelMenu.appendChild(channelTitle);
            channelMenu.appendChild(channelOrientaion);
            channelMenu.appendChild(channelZoomHolder);

            // Add the bar to the body
            jGet.getCached('body').prepend(channelMenu);

            // Get title of clicked channel for the DX Menu
            jGet('body').onMouseDown({
                target: '.x-combo-list.channel-device-combo-box .thumb-wrap .x-editable',
                callback: function () {
                    var channelLabel = jGet.node(this).getHTML();
                    jGet.id('channel-title').setAttribute('data-indigo-label', channelLabel);
                },
                uid: "CHANNEL-ONCLICK"
            });

            // Auto fit the channel preview to the screen
            // Dev note: When this is ON we need to update on page resize
            jGet.getCached('body').onClick({
                target: '#channel-auto-fit-button',
                callback: function () {
                    jGet.id('channel-auto-fit-button').addClass('selected');
                    jGet.id('channel-zoom-button').removeClass('selected');
                    jGet.id('channel-size-slider-holder').addClass('disabled');

                    Anthracite.edit.sidepanel.zoomChannel(0);
                    Anthracite.edit.sidepanel.data.channel.autofit = true;

                    jGet('.mainmodule > div:nth-child(2)').removeClass('channel-zoom');
                },
                uid: "CHANNEL-AUTO-FIT-BUTTON-ONCLICK"
            });

            // Close button
            jGet.getCached('body').onClick({
                target: '#channel-close-button',
                callback: function () {
                    // Trigger the close button
                    // Click on the Channel drop down in the (hidden) side panel
                    jGet.id('JahiaGxtChannelsTab').filter('.x-form-trigger').index(0).trigger('click');

                    // When the combo menu opens, add a class to enable repositioning to bottom of screen
                    jGet.getCached('body').onceOpen({
                        target: '.x-combo-list',
                        callback: function () {
                            // CLick first in the list
                            jGet.class('x-combo-list').filter('.thumb-wrap').index(0).trigger('mousedown');

                        },
                    });

                    Anthracite.edit.sidepanel.data.channel.opened = false;
                },
                uid: "CHANNEL-CLOSE-ONCLICK"
            });

            jGet.getCached('body').onClick({
                target: '#channel-zoom-button',
                callback: function () {
                    jGet.id('channel-auto-fit-button').removeClass('selected');
                    jGet.id('channel-zoom-button').addClass('selected');
                    jGet.id('channel-size-slider-holder').removeClass('disabled');

                    Anthracite.edit.sidepanel.data.channel.autofit = false;

                    jGet('.mainmodule > div:nth-child(2)').addClass('channel-zoom');

                    jGet.id('channel-size-slider').trigger('input');
                },
                uid: "CHANNEL-ZOOM-ONCLICK"
            });

            // Open the combo to change the Channel
            jGet.getCached('body').onClick({
                target: '#channel-title',
                callback: function () {
                    // Click on the Channel drop down in the (hidden) side panel
                    jGet.id('JahiaGxtChannelsTab').filter('.x-form-trigger').index(0).trigger('click');

                    // When the combo menu opens, add a class to enable repositioning to bottom of screen
                    jGet.getCached('body').onceOpen({
                        target: '.x-combo-list',
                        callback: function () {
                            jGet.class('x-combo-list').addClass('channel-device-combo-box');
                        },
                    });

                    Anthracite.edit.sidepanel.data.channel.opened = true;
                },
                uid: "CHANNEL-TITLE-ONCLICK"
            });

            // Toggle between orientations
            jGet.getCached('body').onClick({
                target: '#channel-orientation',
                callback: function () {
                    // Open the Orrientation combo in the (hidden) side panel
                    jGet.id('JahiaGxtChannelsTab').filter('.x-form-trigger').index(1).trigger('click');

                    // When it is opened, click on the orientation that is NOT selected
                    jGet.getCached('body').onceOpen({
                        target: '.x-combo-list',
                        callback: function () {
                            jGet('.x-combo-list .x-combo-list-item:not(.x-view-highlightrow)').trigger('mousedown');
                        },
                    });
                },
                uid: "CHANNEL-ORIENTATION-ONCLICK"
            });

            // Redimension the Channel Preview
            jGet.getCached('body').onInput({
                target: '#channel-size-slider',
                callback: function (e) {
                    var zoomSize = e.target.value;
                    Anthracite.edit.sidepanel.zoomChannel(zoomSize);
                },
                uid: "CHANNEL-SIZE-SLIDER"
            });
        },
        /**
         * Callback executed when the user zooms on the Channel View
         * @memberof Anthracite.edit.sidepanel.siteSelector
         * @param {integer} zoomSize XXX
         * @method zoomChannel

         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        zoomChannel: function (zoomSize) {
            var windowHeight = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;
            var actualHeight = parseInt(jGet('.mainmodule > div:nth-child(2) > div').nodes[0].style.height);
            var windowPadding = 136;
            var transformOrigin = '50% 0';
            var scale = (zoomSize > 0) ? (zoomSize / 100) : ((windowHeight - windowPadding) / actualHeight);

            if (scale > 1) {
                scale = 1;
            }

            jGet('.x-abs-layout-container').css({
                transform: 'scale(' + scale + ')',
                transformOrigin: transformOrigin
            });
        },
        /**
         * Callback executed when the user toggle the Side Panel Pin
         * @memberof Anthracite.edit.sidepanel.siteSelector
         * @method togglePin

         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        togglePin: function () {
            Anthracite.edit.sidepanel.data.pinned = !Anthracite.edit.sidepanel.data.pinned;

            jGet.getCached('body').setAttribute('data-INDIGO-SIDEPANEL-PINNED', Anthracite.edit.sidepanel.data.pinned);
            jGet.iframe('.window-iframe').filter('body').nodes[0].style.pointerEvents = 'all';

            if (Anthracite.edit.sidepanel.data.pinned && jGet.id('indigoSplitter').exists()) {
                var xPos = parseInt(jGet.id('indigoSplitter').nodes[0].style.getPropertyValue('left'));
                jGet('.mainmodule > div:nth-child(2)').nodes[0].style.setProperty('width', 'calc(100% - ' + (xPos + 5) + 'px)',
                    'important');
                jGet('.mainmodule > div:nth-child(2)').nodes[0].style.setProperty('left', (xPos + 10) + 'px', 'important');
            } else {
                jGet('.mainmodule > div:nth-child(2)').nodes[0].style.removeProperty('width');
                jGet('.mainmodule > div:nth-child(2)').nodes[0].style.removeProperty('left');
            }

            Anthracite.edit.topbar.reposition();
            Anthracite.edit.sidepanel.clipPageTitle();

            Anthracite.storage.set('pinnedPanel', Anthracite.edit.sidepanel.data.pinned);

            if(Anthracite.iframe.data.selectionCount > 0){
                // At least one node in the page has been selected, so we should redraw
                authoringApi.redrawSelection();
            }
        },
        /**
         * XXX
         * @memberof Anthracite.edit.sidepanel.siteSelector
         * @method toggleFloatingPanel
         * @param {event} e XXX

         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        toggleFloatingPanel: function (e) {
            Anthracite.dev.log('::: APP ::: EDIT ::: SIDEPANEL ::: TOGGLEFLOATINGPANEL');

            if (jGet.node(e.target).getAttribute('id') == 'images-view'
                || jGet.node(e.target).hasClass('x-panel-bwrap')
                || jGet.node(e.target).hasClass('x-box-item')) {

                if (jGet.getCached('body').hasClass('show-results')) {
                    jGet.getCached('body').toggleClass('minimise-results');
                } else {
                    jGet.getCached('body').removeClass('minimise-results');
                }
            }

            Anthracite.edit.sidepanel.clipPageTitle();
        },
        /**
         * Callback executed when the user starts to drag an element from the Side Panel
         * @memberof Anthracite.edit.sidepanel.siteSelector
         * @method onDrag
         * @param {boolean} start XXX

         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        onDrag: function (start) {
            Anthracite.dev.log('::: APP ::: EDIT ::: SIDEPANEL :::' + start ? 'ONSTARTDRAG' : 'ONSTOPDRAG');

            var cachedBody = jGet.getCached('body');
            cachedBody.toggleClass('indigo-drag-to-drop');
            if (Anthracite.edit.sidepanel.data.pinned) {
                cachedBody.toggleClass('show-results');
                cachedBody.toggleClass('minimise-results');
                Anthracite.edit.sidepanel.clipPageTitle();
            } else if (cachedBody.getAttribute('data-indigo-gwt-panel-tab') === 'JahiaGxtSidePanelTabs__JahiaGxtPagesTab') {
                // do nothing if pages is open
            } else {
                start ? Anthracite.edit.sidepanel.close() : Anthracite.edit.sidepanel.open();
            }
        },
        /**
         * Builds a splitter used for controlling the width of the side panel
         * @memberof Anthracite.edit.sidepanel.siteSelector
         * @method buildSplitter

         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        buildSplitter: function () {
            // Handle Splitter ( used for changing width of Side Panel )
            if (!jGet.id('indigoSplitter').exists()) {
                // Create Side Panel splitter ( cant gain proper control of GWT splitter)

                var sidePanelSplitter = document.createElement('div');
                // Set ID
                sidePanelSplitter.id = 'indigoSplitter';
                sidePanelSplitter.style.setProperty('left', '245px', 'important');

                // Attach event listener for drag start
                sidePanelSplitter.onmousedown = Anthracite.edit.sidepanel.onStartResize;

                // Add the spliiter to the body
                jGet.getCached('body').prepend(sidePanelSplitter);
            }
        },
        /**
         * Used to hide the title of the page when the side panel is opened
         * @memberof Anthracite.edit.sidepanel.siteSelector
         * @method clipPageTitle
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        clipPageTitle: function () {
            Anthracite.dev.log('::: APP ::: EDIT ::: SIDEPANEL ::: CLIPPAGETITLE');

            if (Anthracite.data.currentApp === 'edit') {
                var sidepanelWidth = parseInt(document.getElementById('JahiaGxtSidePanelTabs').style.width) - 78;
                var pageTitleClip = null;
                var wideSidepanels = ['JahiaGxtSidePanelTabs__JahiaGxtContentBrowseTab', 'JahiaGxtSidePanelTabs__JahiaGxtFileImagesBrowseTab', 'JahiaGxtSidePanelTabs__JahiaGxtSearchTab', 'JahiaGxtSidePanelTabs__JahiaGxtCategoryBrowseTab'];
                var isWide = wideSidepanels.indexOf(Anthracite.edit.sidepanel.data.currentTab) > -1;
                var isMinimised = isWide && jGet.getCached('body').hasClass('minimise-results');
                var isPinned = Anthracite.edit.sidepanel.data.pinned;
                var topRightMenuClip = null;
                var topRightMenuWidth = parseInt(window.getComputedStyle(jGet.class('edit-menu-topright').nodes[0])['width']);

                if (Anthracite.edit.sidepanel.data.firstRun) {
                    sidepanelWidth += 60;
                    Anthracite.edit.sidepanel.data.firstRun = false;
                }

                if (Anthracite.edit.sidepanel.data.open) {
                    if (isPinned && isWide && !isMinimised) {
                        // PINNED - WIDE PANEL - EXPANDED
                        pageTitleClip = 343;
                        topRightMenuClip = pageTitleClip - topRightMenuWidth + 128 + sidepanelWidth;
                    } else if (!isPinned && isWide && !isMinimised) {
                        // UNPINNED - WIDE PANEL - EXPANDED
                        pageTitleClip = sidepanelWidth + 353;
                        topRightMenuClip = pageTitleClip - topRightMenuWidth + 118;
                    } else if (isPinned && isWide && isMinimised) {
                        // PINNED - WIDE PANEL - COLLAPSED
                        pageTitleClip = 14;
                        topRightMenuClip = null;
                    } else if (!isPinned && isWide && isMinimised) {
                        // UNPINNED - WIDE PANEL - COLLAPSED
                        pageTitleClip = sidepanelWidth + 24;
                        topRightMenuClip = pageTitleClip - topRightMenuWidth + 118;
                    } else if (isPinned && !isWide) {
                        // PINNED - NORMAL PANEL
                        pageTitleClip = null;
                        topRightMenuClip = null;
                    } else if (!isPinned && !isWide) {
                        // UNPINNED - NORMAL PANEL
                        pageTitleClip = 282;
                        topRightMenuClip = pageTitleClip - topRightMenuWidth + 118;
                    }
                }

                if (pageTitleClip === null) {
                    jGet.class('x-current-page-path').nodes[0].style.removeProperty('clip');
                    jGet.class('edit-menu-topright').nodes[0].style.removeProperty('clip');
                } else {
                    jGet.class('x-current-page-path').nodes[0].style.setProperty('clip', 'rect(0px, 100vw, 30px, ' + pageTitleClip + 'px)', 'important');
                }

                if (topRightMenuClip === null) {
                    jGet.class('edit-menu-topright').nodes[0].style.removeProperty('clip');
                } else {
                    jGet.class('edit-menu-topright').nodes[0].style.setProperty('clip', 'rect(0px, 100vw, 30px, ' + topRightMenuClip + 'px)', 'important');
                }
            }
        },
        /**
         * Opens the Side Panel
         * @memberof Anthracite.edit.sidepanel.siteSelector
         * @method open
         * @param {boolean} isSettings XXX

         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        open: function (isSettings) {
            Anthracite.dev.log('::: APP ::: EDIT ::: SIDEPANEL ::: OPEN [isSettings=\'' + isSettings + '\']');
            // Set CSS to open side panel
            jGet.getCached('body').setAttribute('data-INDIGO-GWT-SIDE-PANEL', 'open');
            Anthracite.edit.sidepanel.data.open = true;
            Anthracite.edit.sidepanel.resizeSidePanel();

            Anthracite.edit.sidepanel.buildSplitter();

            var keepCheckingForEmpties = true;
            // Check if there are any empty rows, if so then refresh the panel
            jGet.id('JahiaGxtPagesTab').filter('.x-grid3-row').each(function (dexObject) {
                if (keepCheckingForEmpties) {
                    if (dexObject.getHTML() == '') {
                        keepCheckingForEmpties = false;
                        jGet.id('JahiaGxtRefreshSidePanelButton').trigger('click');
                    }
                }
            });

            if (!isSettings) {
                // Disable clicks
                Anthracite.iframe.disableClicks();
            }

            Anthracite.edit.sidepanel.clipPageTitle();
        },
        /**
         * Closes the Side Panel
         * @memberof Anthracite.edit.sidepanel.siteSelector
         * @method close

         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        close: function () {
            if (jGet.getCached('body').getAttribute('data-sitesettings') !== 'true'
                && jGet.getCached('body').getAttribute('data-edit-window-style') !== 'settings'
                && jGet.getCached('body').getAttribute('data-INDIGO-GWT-SIDE-PANEL') == 'open'
                && jGet.getCached('body').getAttribute('data-INDIGO-COLLAPSABLE-SIDE-PANEL') == 'yes'
                && jGet.getCached('body').getAttribute('data-INDIGO-SIDEPANEL-PINNED') != 'true') {
                Anthracite.dev.log('::: APP ::: EDIT ::: SIDEPANEL ::: CLOSE');
                Anthracite.edit.sidepanel.data.open = false;

                var siteCombo = jGet('body[data-indigo-gwt-side-panel=\'open\'] .window-side-panel div[role=\'combobox\']');
                if (siteCombo.exists()) {
                    siteCombo.nodes[0].style.setProperty('width', 'auto', 'important');
                }

                jGet.getCached('body').setAttribute('data-INDIGO-GWT-SIDE-PANEL', '');

                // Revert iframes body style attribute to what it was originally
                jGet.iframe('.window-iframe').filter('body').nodes[0].style.pointerEvents = 'all';

                if (jGet.id('JahiaGxtSidePanelTabs').exists()) {
                    jGet.id('JahiaGxtSidePanelTabs').nodes[0].style.setProperty('width', '245px', 'important');
                    jGet.getCached('body').setAttribute('data-indigo-gwt-side-panel', '');
                }
            }

            Anthracite.edit.sidepanel.clipPageTitle();
        },
        /**
         * Used to determine whether or not the side panel is opened
         * @memberof Anthracite.edit.sidepanel.siteSelector
         * @method isOpen
         * @returns {boolean} XXX
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        isOpen: function () {
            return Anthracite.edit.sidepanel.data.open;
        },
        /**
         * Stores data for Object
         * @memberof Anthracite.edit.sidepanel.siteSelector
         * @property {object} all XXX <br> See {@link Anthracite.edit.sidepanel.siteSelector.tabs.all}
         * @property {object} contentTab XXX <br> See {@link Anthracite.edit.sidepanel.siteSelector.tabs.contentTab}
         * @property {object} categoryTab XXX <br> See {@link Anthracite.edit.sidepanel.siteSelector.tabs.categoryTab}
         * @property {object} filesTab XXX <br> See {@link Anthracite.edit.sidepanel.siteSelector.tabs.filesTab}
         * @property {object} createContent XXX <br> See {@link Anthracite.edit.sidepanel.siteSelector.tabs.createContent}
         * @property {object} searchTab XXX <br> See {@link Anthracite.edit.sidepanel.siteSelector.tabs.searchTab}
         * @namespace Anthracite.edit.sidepanel.siteSelector.tabs
         * @type {object}
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        tabs: {
            /**
            * XXX
             * @memberof Anthracite.edit.sidepanel.siteSelector.tabs
             * @property {method} onClick XXX
             * @namespace Anthracite.edit.sidepanel.siteSelector.tabs.all
             * @type {object}
             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            all: {
                /**
                 * XXX
                 * @memberof Anthracite.edit.sidepanel.siteSelector.tabs.all
                 * @method onClick
                 * @returns {boolean|undefined} XXX

                 * @example
                 *
                 *
                 * Add Example here ...
                 *
                 *
                 */
                onClick: function () {
                    Anthracite.dev.log('APP ::: EDIT ::: SIDEPANEL ::: TAB ::: ONCLICK');

                    if(jGet.node(this).getAttribute('id') == 'JahiaGxtSidePanelTabs__JahiaGxtSettingsTab' && jGet.getCached('body').getAttribute('data-edit-mode-status') !== 'initialised'){
                        // Edit Mode has not yet finsihed loading the page, so just ignore clicks on settings button until it is ready
                        return false;
                    }

                    // User has clicked on one of the side panel tabs (except for Settings Tab which calls eventHandlers.clickSidePanelSettingsTab)
                    var clickedTabID = jGet.node(this).getAttribute('id');

                    Anthracite.edit.sidepanel.data.previousTab = Anthracite.edit.sidepanel.data.currentTab;
                    Anthracite.edit.sidepanel.data.currentTab = clickedTabID;

                    if(Anthracite.edit.sidepanel.data.newSite){
                        Anthracite.edit.sidepanel.data.newSite = false;

                    } else {
                        if (Anthracite.edit.sidepanel.data.previousTab === Anthracite.edit.sidepanel.data.currentTab) {
                            if (jGet.getCached('body').getAttribute('data-sitesettings') == 'true'
                                && clickedTabID !== 'JahiaGxtSidePanelTabs__JahiaGxtSettingsTab') {
                                setTimeout(function () {
                                    jGet.id('JahiaGxtSidePanelTabs__JahiaGxtSettingsTab').trigger('mousedown').trigger('mouseup').trigger('click');
                                }, 0);
                            } else if (jGet.getCached('body').getAttribute('data-sitesettings') !== 'true'
                                && jGet.getCached('body').getAttribute('data-indigo-sidepanel-pinned') == 'true'
                                && clickedTabID === 'JahiaGxtSidePanelTabs__JahiaGxtSettingsTab') {
                                setTimeout(function () {
                                    jGet.id('JahiaGxtSidePanelTabs__JahiaGxtPagesTab').trigger('mousedown').trigger('mouseup').trigger('click');
                                }, 0);
                            }
                        }
                    }

                    jGet.getCached('body').setAttribute('data-INDIGO-GWT-PANEL-TAB', clickedTabID);

                    // Menus for the Tabs that call this listener require a normal side panel display
                    var tabMenuActive = jGet.node(this).hasClass('x-tab-strip-active');
                    var sidePanelOpen = jGet.getCached('body').getAttribute('data-INDIGO-GWT-SIDE-PANEL') == 'open';
                    if (tabMenuActive && sidePanelOpen) {
                        // CLOSE SIDE PANEL: Already open for current Tab Menu
                        Anthracite.edit.sidepanel.close();
                    } else {
                        // OPEN SIDE PANEL.
                        Anthracite.edit.sidepanel.open(false);
                    }

                    if(Anthracite.edit.sidepanel.data.currentTab == 'JahiaGxtSidePanelTabs__JahiaGxtSearchTab'){
                        jGet.id('JahiaGxtSearchTab').addClass('show-results');
                        jGet.getCached('body').addClass('show-results');
                    }
                }
            },
            /**
            * XXX
             * @memberof Anthracite.edit.sidepanel.siteSelector.tabs
             * @property {method} onOpen XXX
             * @namespace Anthracite.edit.sidepanel.siteSelector.tabs.contentTab
             * @type {object}
             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            contentTab: {
                /**
                * Callback executed when the Files Tab is opened
                * @memberof Anthracite.edit.sidepanel.siteSelector.tabs.contentTab
                * @method onOpen

                * @example
                *
                *
                * Add Example here ...
                *
                *
                */
                onOpen: function () {
                    // Add Class to tree to create the sub tree drawer
                    jGet.node(this).filter('.x-box-item:nth-child(2) .x-grid3-body').addClass('results-column');
                }
            },
            /**
            * XXX
             * @memberof Anthracite.edit.sidepanel.siteSelector.tabs
             * @property {method} onOpen XXX
             * @namespace Anthracite.edit.sidepanel.siteSelector.tabs.categoryTab
             * @type {object}
             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            categoryTab: {
                /**
                 * Callback executed when the Files Tab is opened
                 * @memberof Anthracite.edit.sidepanel.siteSelector.tabs.categoryTab
                 * @method onOpen

                 * @example
                 *
                 *
                 * Add Example here ...
                 *
                 *
                 */
                onOpen: function () {
                    // Add Class to tree to create the sub tree drawer
                    jGet.node(this).filter('.x-box-item:nth-child(2) .x-grid3-body').addClass('results-column');
                }
            },
            /**
            * XXX
             * @memberof Anthracite.edit.sidepanel.siteSelector.tabs
             * @property {method} onOpen XXX
             * @namespace Anthracite.edit.sidepanel.siteSelector.tabs.filesTab
             * @type {object}
             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            filesTab: {
                /**
                 * Callback executed when the Files Tab is opened
                 * @memberof Anthracite.edit.sidepanel.siteSelector.tabs.filesTab
                 * @method onOpen

                 * @example
                 *
                 *
                 * Add Example here ...
                 *
                 *
                 */
                onOpen: function () {
                    // Add Class to tree to create the sub tree drawer
                    jGet.node(this).filter('#images-view > div').addClass('results-column');
                }
            },
            /**
            * XXX
             * @memberof Anthracite.edit.sidepanel.siteSelector.tabs
             * @property {method} onOpen XXX
             * @namespace Anthracite.edit.sidepanel.siteSelector.tabs.createContent
             * @type {object}
             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            createContent: {
                /**
                 * Callback executed when the Files Tab is opened
                 * @memberof Anthracite.edit.sidepanel.siteSelector.tabs.createContent
                 * @method onOpen

                 * @example
                 *
                 *
                 * Add Example here ...
                 *
                 *
                 */
                onOpen: function () {
                    // Add placeholder to text input
                    jGet.node(this).filter('input.x-form-text').setAttribute('placeholder', Anthracite.dictionary.get('filterContent'));
                }
            },
            /**
             * XXX
             * @memberof Anthracite.edit.sidepanel.siteSelector.tabs
             * @property {method} onOpen XXX
             * @namespace Anthracite.edit.sidepanel.siteSelector.tabs.searchTab
             * @type {object}
             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            searchTab: {
                /**
                 * Callback executed when the Files Tab is opened
                 * @memberof Anthracite.edit.sidepanel.siteSelector.tabs.searchTab
                 * @method onOpen

                 * @example
                 *
                 *
                 * Add Example here ...
                 *
                 *
                 */
                onOpen: function () {
                    // Force the drawer to stay open regardless of whether or not there are any results
                    if (Anthracite.edit.sidepanel.data.open) {
                        jGet.id('JahiaGxtSearchTab').addClass('show-results');
                        jGet.getCached('body').addClass('show-results');
                        Anthracite.edit.sidepanel.resizeSidePanel();
                    }

                    jGet.node(this).filter('.JahiaGxtSearchTab-results .x-grid3-body').addClass('results-column');
                    jGet('#JahiaGxtSearchTab.tab_search .JahiaGxtSearchTab-results .x-toolbar-left-row td.x-toolbar-cell:last-child > table.x-btn.x-component.x-unselectable.x-btn-icon')
                        .addClass('search-side-panel-refresh-button'); // Ask thomas to add a classname here
                }
            }
        },
        /**
         * Stores data for Object
         * @memberof Anthracite.edit.sidepanel
         * @namespace Anthracite.edit.sidepanel.siteSelector.tab
         * @type {object}
         * @deprecated This is useless. Make sure it is not referenced before deleting
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        tab: {
            /**
             * Callback executed when the user clicks on a Side Panel tab
             */
        },
        /**
         * Stores data for Object
         * @memberof Anthracite.edit.sidepanel
         * @property {method} onContext Callback executed when user clicks on tree row in Side Panel
         * @property {method} onMouseDown Callback executed when the user mouse down on a row in side panel tree
         * @namespace Anthracite.edit.sidepanel.siteSelector.row
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
             * Callback executed when user clicks on tree row in Side Panel
             *  - Determines whether or not to open the context menu
             * @memberof Anthracite.edit.sidepanel.row
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
                Anthracite.dev.log('APP ::: EDIT ::: SIDEPANEL ::: ROW ::: ONCONTEXT');

                // Open Context Menu when clicking "More" button.
                var acceptClick = jGet.node(e.target).hasClass('x-grid3-td-displayName');
                if (acceptClick) {
                    jGet.node(e.target).trigger('contextmenu', e.pageX, e.pageY);
                }
            },
            /**
             * Callback executed when the user mouse down on a row in side panel tree
             * @memberof Anthracite.edit.sidepanel.row
             * @method onMouseDown
             * @param e XXX

             * @example
             *
             *
             * Add Example here ...
             *
             *
             */
            onMouseDown: function (e) {
                var nodeJoint = jGet.node(e.target).hasClass('x-tree3-node-joint');
                if (!nodeJoint) {
                    var alreadySelected = jGet.node(this).hasClass('indigo-selected');
                    if (alreadySelected) {
                        // Toggle the drawer
                        jGet.getCached('body').toggleClass('minimise-results');
                    } else {
                        // Show drawer
                        jGet.getCached('body').removeClass('minimise-results');

                        if (jGet.id('JahiaGxtSidePanelTabs').exists()) {
                            jGet.id('JahiaGxtSidePanelTabs').filter('.indigo-selected').removeClass('indigo-selected');
                        }
                        jGet.node(this).addClass('indigo-selected');
                    }

                    Anthracite.edit.sidepanel.clipPageTitle();
                }
            }
        }
    },
     /**
      * Everything here concerns settings in Edit Mode ( not to be confused with Admin mode)
      * @memberof Anthracite.edit
      * @property {object} data XXX
      * @property {method} onTreeLoad XXX
      * @property {method} onTreeChange XXX
      * @property {method} onChange XXX
      * @property {method} onReady XXX
      * @property {method} open Callback executed when the Settings are opened
      * @property {method} close XXX
      * @namespace Anthracite.edit.settings
      * @type {object}
      * @example
      *
      *
      * Add Example here ...
      *
      *
      */
    settings: {
        /**
         * Stores data for Object
         * @memberof Anthracite.edit.settings
         * @property {boolean} opened XXX
         * @property {string} iframeCSSOverRide XXX
         * @type {object}
         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        data: {
            opened: false,
            iframeCSSOverRide: '.well{border:none!important; box-shadow: none!important;} body{background-image: none!important; background-color:#f5f5f5!important}'
        },
        /**
         * XXX
         * @memberof Anthracite.edit.settings
         * @deprecated No longer used. Check before deleting
         * @method onTreeLoad

         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        onTreeLoad: function () {},
        /**
         * XXX
         * @memberof Anthracite.edit.settings
         * @deprecated No longer used. Check before deleting
         * @method onTreeChange

         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        onTreeChange: function () {},
        /**
         * XXX
         * @memberof Anthracite.edit.settings
         * @method onChange
         * @param {string} attrKey XXX
         * @param {string} attrValue XXX

         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        onChange: function (attrKey, attrValue) {
            Anthracite.dev.log('::: APP ::: SETTINGS ::: EDIT ::: SETTINGS ::: ONCHANGE');
            if (attrKey == 'data-sitesettings' && attrValue == 'true') {
                if (Anthracite.data.currentApp == 'edit') {
                    Anthracite.edit.settings.open(null, 'directAccess');
                }
            }
        },
        /**
         * XXX
         * @memberof Anthracite.edit.settings
         * @deprecated No longer used. Check before deleting
         * @method onReady

         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        onReady: function () {
            Anthracite.dev.log('::: APP ::: EDIT ::: SETTINGS ::: ONREADY');
        },
         /**
          * Callback executed when the Settings are opened
          * @memberof Anthracite.edit.settings
          * @method onTreeLoad

          * @example
          *
          *
          * Add Example here ...
          *
          *
          */
        open: function () {
            Anthracite.dev.log('::: APP ::: EDIT ::: SETTINGS ::: OPEN');

            Anthracite.edit.sidepanel.buildSplitter();
            Anthracite.edit.sidepanel.resizeSidePanel();

            jGet.getCached('body').setAttribute('data-indigo-gwt-side-panel', 'open');
            jGet.id('JahiaGxtSidePanelTabs__JahiaGxtSettingsTab').trigger('click');
        },
        /**
         * XXX
         * @memberof Anthracite.edit.settings
         * @deprecated No longer used. Check before deleting
         * @method close

         * @example
         *
         *
         * Add Example here ...
         *
         *
         */
        close: function () {
            Anthracite.dev.log('::: APP ::: EDIT ::: SETTINGS ::: CLOSE');
        }
    }
});
