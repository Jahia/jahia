/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.subengines.acleditor.client;

import org.jahia.ajax.gwt.commons.client.util.JahiaGWT;
import org.jahia.ajax.gwt.aclmanagement.client.model.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.aclmanagement.client.ui.AclEditor;
import org.jahia.ajax.gwt.aclmanagement.client.ACLService;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.allen_sauer.gwt.log.client.Log;

/**
 *
 * 
 * User: toto
 * Date: Nov 26, 2008 - 7:55:04 PM
 */
public class ACLEditorEntryPoint {
    private static final String ACLEDITOR_ID = "gwtacleditor" ;

    private static int aclId;

    private static boolean newAcl;
    private static String sessionIdentifier;

    private static AclEditor aclEditor;

    public void onModuleLoad() {
        JahiaGWT.init();
        initJavaScriptApi();
        final RootPanel panel = RootPanel.get(ACLEDITOR_ID);
        if (panel != null) {
            String acl = DOM.getElementAttribute(panel.getElement(), "aclid");
            newAcl = Boolean.parseBoolean(DOM.getElementAttribute(panel.getElement(), "newAcl"));
            sessionIdentifier = DOM.getElementAttribute(panel.getElement(), "sessionIdentifier");
            final boolean readOnly = Boolean.parseBoolean(DOM.getElementAttribute(panel.getElement(), "readOnly"));

            aclId = Integer.parseInt(acl);

            ACLService.App.getInstance().getACL(aclId, newAcl, sessionIdentifier, new AsyncCallback<GWTJahiaNodeACL>() {
                public void onFailure(Throwable caught) {
                    Log.error("Acl load failed",caught);
                }

                public void onSuccess(GWTJahiaNodeACL result) {
                    aclEditor = new AclEditor(result, false);
                    aclEditor.setCanBreakInheritance(true);
                    aclEditor.setReadOnly(readOnly);
                    final Button saveButton = aclEditor.getSaveButton();
                    saveButton.setVisible(false);
                    ContentPanel w = aclEditor.renderNewAclPanel();
                    w.setHeight("500px");
                    w.setWidth("730px");
                    panel.add(w);
                }
            });
        }
    }

    private native void initJavaScriptApi() /*-{
        // define a static JS function with a friendly name
        $wnd.saveAcl = function (callback) { @org.jahia.ajax.gwt.subengines.acleditor.client.ACLEditorEntryPoint::saveAcl(Ljava/lang/String;)(callback) };
    }-*/;

    public static void saveAcl(final String callback) {
        ACLService.App.getInstance().setACL(aclId, newAcl, sessionIdentifier, aclEditor.getAcl(), new AsyncCallback() {
            public void onFailure(Throwable caught) {
                Log.error("Acl save failed",caught);
            }

            public void onSuccess(Object result) {
                Log.debug("Acl saved, callbacking");
                callback(callback);
            }
        });
    }

    public static native void callback(String callback) /*-{
        try {
            eval('$wnd.' + callback)();
        } catch (e) {};
    }-*/;

}
