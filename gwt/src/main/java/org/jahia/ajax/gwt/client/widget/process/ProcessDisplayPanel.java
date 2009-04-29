/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.process;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.service.process.ProcessDisplayService;
import org.jahia.ajax.gwt.client.data.process.GWTJahiaProcessJobPreference;
import org.jahia.ajax.gwt.client.widget.tripanel.TriPanelBrowserViewport;
import org.jahia.ajax.gwt.client.messages.Messages;


/**
 * User: jahia
 * Date: 14 juil. 2008
 * Time: 15:57:20
 */
public class ProcessDisplayPanel extends TriPanelBrowserViewport {
    //job options
    public static final String[] TYPE_OPTIONS_NAMES = {
            getResource("pd_type_workflow"),
            getResource("pd_type_import"),
            getResource("pd_type_production"),
            getResource("pd_type_copypaste"),
            getResource("pd_type_timebasepublishing")};
    public static final String[] TYPE_OPTIONS_VALUES = {"workflow", "import", "production", "copypaste", "timebasedpublishing"};
    public static final String[] STATUS_OPTIONS_NAMES = {
            getResource("pd_status_wainting"),
            getResource("pd_status_executing"),
            getResource("pd_status_failed"),
            getResource("pd_status_partial"),
            getResource("pd_status_successful"),
            getResource("pd_status_pooled")};
    public static final String[] STATUS_OPTIONS_VALUES = {"waiting","executing", "failed", "partial", "successful", "pooled"};
    public static final String[] OWNER_OPTIONS_NAMES = {
            getResource("pd_owner_currentuser")};
    public static final String[] OWNER_OPTIONS_VALUES = {"1"};

    public ProcessDisplayPanel() {
        // superclass constructor (define linker)
        super();

        ProcessDisplayService.App.getInstance().getPreferences(new AsyncCallback<GWTJahiaProcessJobPreference>() {
            public void onFailure(Throwable throwable) {
                Log.error("Unable to load preference", throwable);
                GWTJahiaProcessJobPreference gwtJahiaProcessJobPreference = new GWTJahiaProcessJobPreference();
                gwtJahiaProcessJobPreference.setMaxJobs(100);
                createUI(gwtJahiaProcessJobPreference);
                layout();
            }

            public void onSuccess(GWTJahiaProcessJobPreference gwtJahiaProcessJobPreference) {
                createUI(gwtJahiaProcessJobPreference);
                layout();
            }
        });
    }

    private void createUI(GWTJahiaProcessJobPreference gwtJahiaProcessJobPreference) {
        // construction of the UI components
        ProcessJobOptionsComponent filterPanel = new ProcessJobOptionsComponent();

        // table
        ProcessJobGrid tablePanel = new ProcessJobGrid();

        //message
        ProcessJobMessagesTab messagePanel = new ProcessJobMessagesTab();

        // top bar
        ProcessJobTopBar processJobTopBar = new ProcessJobTopBar();

        // this is the linker that will have the components communicate with each other
        linker = new ProcessdisplayBrowserLinker(gwtJahiaProcessJobPreference, filterPanel, tablePanel, messagePanel, processJobTopBar);

        // important: create ui after creating linker
        filterPanel.createUI();
        tablePanel.createUI();
        messagePanel.createUI();
        processJobTopBar.createUI();

        // setup widgets in layout
        initWidgets(filterPanel.getComponent(), tablePanel.getComponent(), messagePanel.getComponent(), processJobTopBar.getComponent(), null);

        // linker initializations
        tablePanel.initContextMenu();
        linker.handleNewSelection();
        layout();
    }

    public static String getResource(String key) {
        return Messages.getResource(key);
    }

}
