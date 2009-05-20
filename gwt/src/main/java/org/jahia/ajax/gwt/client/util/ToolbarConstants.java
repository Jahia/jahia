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
package org.jahia.ajax.gwt.client.util;

/**
 * User: jahia
 * Date: 4 avr. 2008
 * Time: 14:28:08
 */
public abstract class ToolbarConstants {
    public static final int AREA_TOP = 0;
    public static final int AREA_RIGHT = 1;

    // Toolbar state
    public static final int TOOLBAR_TOP = 0;
    public static final int TOOLBAR_RIGHT = 1;
    public static final int TOOLBAR_HORIZONTAL_BOX = 2;
    public static final int TOOLBAR_VERTICAL_BOX = 3;
    public static final int TOOLBAR_RIGHT_CLOSE = 11;

    public static final int TOOLBAR_RIGHT_BOX_SIZE = 250;

    // items group layout
    public static final int ITEMSGROUP_BUTTON = 0;
    public static final int ITEMSGROUP_LABEL = 1;
    public static final int ITEMSGROUP_BUTTON_LABEL = 2;
    public static final int ITEMSGROUP_MENU = 3;
    public static final int ITEMSGROUP_MENU_RADIO = 4;
    public static final int ITEMSGROUP_MENU_CHECKBOX = 5;
    public static final int ITEMSGROUP_SELECT = 6;
    public static final int ITEMSGROUP_BOX = 7;
    public static final int ITEMSGROUP_TABS = 8;

    // special type
    public static final String ITEMSGROUP_FILL = "org.jahia.toolbar.itemsgroup.Fill";
    public static final String ITEMS_TOOLBARLABEL = "org.jahia.toolbar.item.Toolbars";

    // prop. parmeter
    public static final String URL = "url";
    public static final String CLASS_ACTION = "classAction";
    public static final String ACTION = "action";
    public static final String INFO = "info";
    public static final String HEIGHT = "height";
    public static final String WIDTH = "width";
    public static final String NOTIFICATION_REFRESH_TIME = "refresh";
    public static final String HISTORY_LINKS = "history-links";
    public static final String HISTORY_LINKS_SIZE = "history-size";
    public static final String HTML = "html";
    public static final String WINDOW_NAME = "windowName";
    public static final String PARAMETERS = "parameters";

}
