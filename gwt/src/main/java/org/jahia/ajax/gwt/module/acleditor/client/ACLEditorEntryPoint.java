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
package org.jahia.ajax.gwt.module.acleditor.client;

import org.jahia.ajax.gwt.client.util.JahiaGWT;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.service.acl.ACLService;
import org.jahia.ajax.gwt.client.util.acleditor.AclEditor;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
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
            final String aclContext = DOM.getElementAttribute(panel.getElement(), "aclContext");
            final boolean readOnly = Boolean.parseBoolean(DOM.getElementAttribute(panel.getElement(), "readOnly"));

            aclId = Integer.parseInt(acl);

            ACLService.App.getInstance().getACL(aclId, newAcl, sessionIdentifier, new AsyncCallback<GWTJahiaNodeACL>() {
                public void onFailure(Throwable caught) {
                    Log.error("Acl load failed",caught);
                }

                public void onSuccess(GWTJahiaNodeACL result) {
                    aclEditor = new AclEditor(result, aclContext!=null?aclContext:"currentSite");
                    aclEditor.setCanBreakInheritance(true);
                    aclEditor.setReadOnly(readOnly);
                    final Widget saveButton = aclEditor.getSaveButton();
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
        $wnd.saveAcl = function (callback) { @org.jahia.ajax.gwt.module.acleditor.client.ACLEditorEntryPoint::saveAcl(Ljava/lang/String;)(callback) };
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
