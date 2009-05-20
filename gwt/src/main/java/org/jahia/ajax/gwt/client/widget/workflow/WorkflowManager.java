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

    public static String getResource(String key) {
        return Messages.getResource(key) ;
    }
}
