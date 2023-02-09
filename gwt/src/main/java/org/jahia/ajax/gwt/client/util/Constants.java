/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
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
}
