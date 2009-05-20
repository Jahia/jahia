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
package org.jahia.ajax.gwt.module.workflow.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.DOM;
import org.jahia.ajax.gwt.client.widget.workflow.WorkflowManager;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 17 juil. 2008 - 09:42:05
 */
public class WorkflowManagerEntryPoint implements EntryPoint {

    private static final String WORKFLOW_MANAGER_ID = "gwtworkflowmanager" ;

    public void onModuleLoad() {
        RootPanel pane = RootPanel.get(WORKFLOW_MANAGER_ID) ;
        if (pane != null) {
            String siteKey = DOM.getElementAttribute(pane.getElement(), "sitekey") ;
            String startPageId = DOM.getElementAttribute(pane.getElement(), "startpage") ;
            pane.add(new WorkflowManager(siteKey, startPageId));
        }
    }

}
