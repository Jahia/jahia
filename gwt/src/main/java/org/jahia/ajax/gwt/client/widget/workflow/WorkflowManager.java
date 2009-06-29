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
package org.jahia.ajax.gwt.client.widget.workflow;

import org.jahia.ajax.gwt.client.widget.tripanel.*;
import org.jahia.ajax.gwt.client.service.JahiaService;
import org.jahia.ajax.gwt.client.service.workflow.WorkflowService;
import org.jahia.ajax.gwt.client.messages.Messages;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.WindowCloseListener;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 16 juil. 2008 - 16:09:38
 */
public class WorkflowManager extends TriPanelBrowserViewport {

    public WorkflowManager(String siteKey, String startPageId) {
        // superclass constructor (define linker)
        super() ;

        // construction of the UI components
        LeftComponent tree = new WorkflowTree(siteKey, startPageId);
        TopRightComponent table = new WorkflowTable() ;
        BottomRightComponent details = new WorkflowDetails() ;
        final WorkflowToolbar toolbar = new WorkflowToolbar() ;
        BottomBar statusBar = new WorkflowStatusBar() ;
        
        // setup widgets in layout
        initWidgets(tree.getComponent(),
                    table.getComponent(),
                    details.getComponent(),
                    toolbar.getComponent(),
                    statusBar.getComponent());

        // linker initializations
        linker.registerComponents(tree, table, details, toolbar, statusBar) ;
        table.initContextMenu();
        linker.handleNewSelection();

        // add lock release when quitting
        Window.addWindowCloseListener(new WindowCloseListener() {
            public String onWindowClosing() {
                return null ;
            }
            public void onWindowClosed() {
                JahiaService.App.getInstance().releaseLocks("workflowLocks", new AsyncCallback() {
                    public void onFailure(Throwable caught) {
                        Window.alert("Could not release locks...\n\n" + caught.getLocalizedMessage()) ;
                    }

                    public void onSuccess(Object result) {
                    }
                });

                WorkflowService.App.getInstance().storeBatch(toolbar.getBatch(), new AsyncCallback() {
                    public void onFailure(Throwable caught) {
                        Window.alert("Could not store batch...\n\n" + caught.getLocalizedMessage()) ;
                    }

                    public void onSuccess(Object result) {
                    }
                });
            }
        });
    }

}
