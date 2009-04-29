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
package org.jahia.ajax.gwt.module.admin.client;

import org.jahia.ajax.gwt.client.util.acleditor.AclNameEditor;
import org.jahia.ajax.gwt.client.util.JahiaGWT;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.core.client.EntryPoint;

/**
 * Entry point class for GWT integration into Jahia Administration.
 * 
 * @author Sergiy Shyrkov
 */
public class AdminEntryPoint implements EntryPoint {

    public void onModuleLoad() {
        JahiaGWT.init();
        AclNameEditor.initACLNameEditors();
        exposeFunctions();
    }

    private native void exposeFunctions() /*-{
        if (!$wnd.jahia) {
            $wnd.jahia = new Object();
        }
        $wnd.jahia.alert = function (title, message) {@org.jahia.ajax.gwt.module.admin.client.AdminEntryPoint::alert(Ljava/lang/String;Ljava/lang/String;)(title, message); };
    }-*/;
    
    static void alert(String title, String message) {
        MessageBox.alert(title != null ? title : "Info", message, null);
    } 
}
