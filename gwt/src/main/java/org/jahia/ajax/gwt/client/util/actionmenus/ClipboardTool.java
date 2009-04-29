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
package org.jahia.ajax.gwt.client.util.actionmenus;

//import com.google.gwt.user.client.ui.HTML;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.service.actionmenu.ActionMenuService;
import org.jahia.ajax.gwt.client.service.actionmenu.ActionMenuServiceAsync;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 22 f�vr. 2008 - 12:11:05
 */
public class ClipboardTool /*extends HTML*/ {

    private ActionMenuServiceAsync actionMenuService = ActionMenuService.App.getInstance();
    private GWTJahiaPageContext jahiaPageContext;

    public ClipboardTool(GWTJahiaPageContext jahiaPageContext) {
        /*super("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;") ;
        setSize("16px", "16px");*/
        this.jahiaPageContext = jahiaPageContext;
        checkClipboardState();
    }

    public void checkClipboardState() {
        /*actionMenuService.clipboardIsEmpty(new AsyncCallback<Boolean>() {
            public void onFailure(Throwable throwable) {
                Window.alert("Clipboard error :\n" + throwable.getLocalizedMessage()) ;
                setStyleName("clipboard-error");
            }
            public void onSuccess(Boolean empty) {
                if (empty != null) {
                    if (empty.booleanValue()) {
                        setStyleName("clipboard-empty");
                    } else {
                        setStyleName("clipboard-full");
                    }
                } else {
                    setStyleName("clipboard-error");
                }
            }
        });*/
    }

    public void toggleIcon() {
        /*if (getStyleName().equals("clipboard-empty")) {
            setStyleName("clipboard-full");
        } else {
            setStyleName("clipboard-empty");
        }*/
    }

    public void copy(String objectKey) {
        actionMenuService.clipboardCopy(jahiaPageContext, objectKey, new AsyncCallback<Boolean>() {
            public void onFailure(Throwable throwable) {
                Window.alert("Copy failed :\n" + throwable.getLocalizedMessage()) ;
            }
            public void onSuccess(Boolean success) {
                if (success != null && success.booleanValue()) {
                    //setStyleName("clipboard-full");
                } else {
                    checkClipboardState();
                }
            }
        });
    }

    public void paste(String objectKey) {
        actionMenuService.clipboardPaste(jahiaPageContext, objectKey, new AsyncCallback<Boolean>() {
            public void onFailure(Throwable throwable) {
                Window.alert("Paste failed :\n" + throwable.getLocalizedMessage()) ;
            }
            public void onSuccess(Boolean success) {
                if (success != null && success.booleanValue()) {
                    //setStyleName("clipboard-pasted");
                } else {
                    checkClipboardState();
                }
            }
        });
    }

    public void pasteReference(String objectKey) {
        actionMenuService.clipboardPasteReference(jahiaPageContext, objectKey, new AsyncCallback<Boolean>() {
            public void onFailure(Throwable throwable) {
                Window.alert("Paste failed :\n" + throwable.getLocalizedMessage()) ;
            }
            public void onSuccess(Boolean success) {
                if (success != null && success.booleanValue()) {
                    //setStyleName("clipboard-pasted");
                } else {
                    checkClipboardState();
                }
            }
        });
    }

}
