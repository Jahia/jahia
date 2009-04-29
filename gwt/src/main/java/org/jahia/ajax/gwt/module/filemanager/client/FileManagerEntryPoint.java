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
package org.jahia.ajax.gwt.module.filemanager.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.DOM;
import org.jahia.ajax.gwt.client.widget.node.FileManager;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 10 juil. 2008 - 16:58:16
 */
public class FileManagerEntryPoint implements EntryPoint {

    public void onModuleLoad() {
        RootPanel panel = RootPanel.get("filemanager") ;
        if (panel != null) {
            String conf = DOM.getElementAttribute(panel.getElement(), "config") ;
            panel.add(new FileManager(
                    DOM.getElementAttribute(panel.getElement(), "nodeTypes"),
                    DOM.getElementAttribute(panel.getElement(), "filters"),
                    DOM.getElementAttribute(panel.getElement(), "mimeTypes"),
                    conf)) ;
        }
    }

}
