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
package org.jahia.ajax.gwt.client.module;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;

import org.jahia.ajax.gwt.client.service.actionmenu.ActionMenuService;
import org.jahia.ajax.gwt.client.util.actionmenus.ClipboardTool;
import org.jahia.ajax.gwt.client.core.JahiaModule;
import org.jahia.ajax.gwt.client.core.JahiaType;
import org.jahia.ajax.gwt.client.data.actionmenu.GWTJahiaGlobalState;
import org.jahia.ajax.gwt.client.data.actionmenu.GWTJahiaGlobalStateKey;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.util.Constants;
import org.jahia.ajax.gwt.client.util.templates.TemplatesDOMUtil;
import org.jahia.ajax.gwt.client.widget.actionmenu.StateDisplay;
import org.jahia.ajax.gwt.client.widget.actionmenu.ActionMenuDisplay;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 22 janv. 2008 - 14:54:04
 */
public class ActionMenuJahiaModule extends JahiaModule {

    private static ClipboardTool clipboardTool = null ;

    public static ClipboardTool getClipboardTool() {
        return clipboardTool;
    }

    public String getJahiaModuleType() {
        return JahiaType.ACTION_MENU;
    }

    public void onModuleLoad(final GWTJahiaPageContext page, final List<RootPanel> rootPanels) {
        // clipboard tool
        clipboardTool = new ClipboardTool(page);

        final List<GWTJahiaGlobalStateKey> keys = new ArrayList<GWTJahiaGlobalStateKey>();
        final List<RootPanel> panels = new ArrayList<RootPanel>();
        for (RootPanel actionPane : rootPanels) {
            String objectKey = DOM.getElementAttribute(actionPane.getElement(),
                    "id");
            int idSeparator = objectKey.lastIndexOf(Constants.UID_SEPARATOR);
            if (idSeparator > 0) {
                objectKey = objectKey.substring(0, idSeparator);
            }

            if (objectKey.length() > 0) {
                    keys.add(new GWTJahiaGlobalStateKey(objectKey, DOM
                            .getElementAttribute(actionPane.getElement(), "wfkey"),
                            DOM
                                    .getElementAttribute(actionPane.getElement(),
                                            "lang"), "true".equalsIgnoreCase(DOM.getElementAttribute(actionPane.getElement(), "extended"))));
                    panels.add(actionPane);
            }
        }
        if (!keys.isEmpty()) {
            ActionMenuService.App.getInstance().getGlobalStateForObject(page, keys, new AsyncCallback<List<GWTJahiaGlobalState>>() {
                public void onFailure(Throwable throwable) {
                    Log.error("Failed to retrieve object states for " + keys, throwable) ;
                }
                public void onSuccess(List<GWTJahiaGlobalState> states) {
                    Iterator<RootPanel> actionPaneIterator = panels.iterator();
                    Iterator<GWTJahiaGlobalStateKey> keyIterator = keys.iterator();
                    for (GWTJahiaGlobalState state : states) {
                        RootPanel actionPane = actionPaneIterator.next();
                        GWTJahiaGlobalStateKey key = keyIterator.next();
                        if (state != null) {
                            String statusOnly = DOM.getElementAttribute(actionPane.getElement(), "statusonly");
                            if (statusOnly.length() > 0 && key.getLanguageCode().length() > 0) {
                                actionPane.add(new StateDisplay(page, state));
                            } else {                
                                // Optional parameters (labels & co.)
                                String iconStyle = DOM.getElementAttribute(actionPane.getElement(), "iconstyle");
                                String bundleName = DOM.getElementAttribute(actionPane.getElement(), "bundlename");
                                String namePostFix = DOM.getElementAttribute(actionPane.getElement(), "namepostfix");
                                String labelKey = DOM.getElementAttribute(actionPane.getElement(), "labelkey");
                                String toolbarView = DOM.getElementAttribute(actionPane.getElement(), "toolbarview");
                                boolean disableToolbarView = (toolbarView != null && toolbarView.equalsIgnoreCase("false")) ;
                                actionPane.add(new ActionMenuDisplay(page, key.getObjectKey(), state, bundleName, namePostFix, labelKey, iconStyle, disableToolbarView));
                            }
                        }
                    }
                    keys.clear();
                    panels.clear();
                }
            });
        }
    }

    public List<RootPanel> getRootPanels() {
        List<String> actionSlotsIds = TemplatesDOMUtil.getElementsIdsByJahiaType(RootPanel.getBodyElement(), JahiaType.ACTION_MENU) ;
        List<RootPanel> panels = new ArrayList<RootPanel>() ;
        for (String actionSlotsId : actionSlotsIds) {
            panels.add(RootPanel.get(actionSlotsId));
        }
        return panels ;
    }
    
}
