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

}
