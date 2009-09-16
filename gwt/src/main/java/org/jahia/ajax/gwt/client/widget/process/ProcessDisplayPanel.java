/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
        linker = new ProcessdisplayManagerLinker(gwtJahiaProcessJobPreference, filterPanel, tablePanel, messagePanel, processJobTopBar);

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
