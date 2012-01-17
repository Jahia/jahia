/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.util;

/**
 * User: jahia
 * Date: 4 avr. 2008
 * Time: 14:28:08
 */
public abstract class Constants {

    // items group layout
    public static final int LAYOUT_BUTTON = 0;
    public static final int LAYOUT_ONLY_LABEL = 1;
    public static final int LAYOUT_BUTTON_LABEL = 2;
    public static final int LAYOUT_ITEMSGROUP_MENU = 3;
    public static final int LAYOUT_ITEMSGROUP_MENU_RADIO = 4;
    public static final int LAYOUT_ITEMSGROUP_MENU_CHECKBOX = 5;

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
    public static final String NO_OPTIONS = "no-options";
    public static final String NOTIFICATION_REFRESH_TIME = "refresh";
    public static final String HISTORY_LINKS = "history-links";
    public static final String HISTORY_LINKS_SIZE = "history-size";
    public static final String HTML = "html";
    public static final String WINDOW_NAME = "windowName";
    public static final String PARAMETERS = "parameters";

    public static final int MODE_LIVE = 0;
    public static final int MODE_PREVIEW = 1;
    public static final int MODE_STAGING = 2;

    public static final String MODULE_TYPE_SYSTEM = "system";
    public static final String MODULE_TYPE_TEMPLATES_SET = "templatesSet";
    public static final String MODULE_TYPE_PROFILE_MODULE = "profileModule";
}
