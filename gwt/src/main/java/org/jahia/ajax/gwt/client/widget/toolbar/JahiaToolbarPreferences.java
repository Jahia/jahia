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
package org.jahia.ajax.gwt.client.widget.toolbar;

import org.jahia.ajax.gwt.client.widget.toolbar.ToolbarManager;

import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * User: ktlili
 * Date: 16 oct. 2008
 * Time: 11:26:08
 */
public class JahiaToolbarPreferences {
    private PopupPanel popupPanel;
    private Menu menu;
    private ToolbarManager toolbarManager;

    public JahiaToolbarPreferences(ToolbarManager toolbarManager) {
        this.toolbarManager = toolbarManager;
    }

    public void show(final int left, final int top) {
        popupPanel.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
            public void setPosition(int i, int i1) {
                popupPanel.setPopupPosition(left, top);
            }
        });
    }

    public void hide() {
        popupPanel.hide();
    }

}
