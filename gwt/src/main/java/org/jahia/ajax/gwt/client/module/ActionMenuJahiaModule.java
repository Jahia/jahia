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
package org.jahia.ajax.gwt.client.module;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.RootPanel;
import org.jahia.ajax.gwt.client.util.actionmenus.ClipboardTool;
import org.jahia.ajax.gwt.client.core.JahiaModule;
import org.jahia.ajax.gwt.client.core.JahiaType;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.util.Constants;
import org.jahia.ajax.gwt.client.util.templates.TemplatesDOMUtil;
import org.jahia.ajax.gwt.client.widget.actionmenu.StateDisplay;
import org.jahia.ajax.gwt.client.widget.actionmenu.ActionMenuDisplay;

import java.util.ArrayList;
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

        for (RootPanel actionPane : rootPanels) {
            String objectKey = DOM.getElementAttribute(actionPane.getElement(), "id") ;
            int idSeparator = objectKey.lastIndexOf(Constants.UID_SEPARATOR);
            if (idSeparator > 0) {
                objectKey = objectKey.substring(0, idSeparator);
            }

            if (objectKey.length() > 0) {
                String wfKey = DOM.getElementAttribute(actionPane.getElement(), "wfkey");

                // Check for status only display
                String statusOnly = DOM.getElementAttribute(actionPane.getElement(), "statusonly");
                String languageCode = DOM.getElementAttribute(actionPane.getElement(), "lang");
                if (statusOnly.length() > 0 && languageCode.length() > 0) {
                    boolean extended = "true".equalsIgnoreCase(DOM.getElementAttribute(actionPane.getElement(), "extended"));
                    actionPane.add(new StateDisplay(page, languageCode, objectKey, wfKey, extended)) ;
                } else {
                    // Optional parameters (labels & co.)
                    String iconStyle = DOM.getElementAttribute(actionPane.getElement(), "iconstyle");
                    String bundleName = DOM.getElementAttribute(actionPane.getElement(), "bundlename");
                    String namePostFix = DOM.getElementAttribute(actionPane.getElement(), "namepostfix");
                    String labelKey = DOM.getElementAttribute(actionPane.getElement(), "labelkey");
                    String toolbarView = DOM.getElementAttribute(actionPane.getElement(), "toolbarview");
                    boolean disableToolbarView = (toolbarView != null && toolbarView.equalsIgnoreCase("false")) ;
                    actionPane.add(new ActionMenuDisplay(page, objectKey, wfKey, bundleName, namePostFix, labelKey, iconStyle, disableToolbarView));
                }
            }
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
