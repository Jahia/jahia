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
package org.jahia.ajax.gwt.client.widget.toolbar.provider;

import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

/**
 * User: jahia
 * Date: 4 avr. 2008
 * Time: 13:43:42
 */
public class JahiaProviderFactory extends ProviderHelper {
    public static final String ORG_JAHIA_TOOLBAR_ITEM_CLIPBOARD = "org.jahia.toolbar.item.ClipBoard";
    public static final String ORG_JAHIA_TOOLBAR_ITEM_INFO = "org.jahia.toolbar.item.Inf";
    public static final String ORG_JAHIA_TOOLBAR_ITEM_OPEN_ENGINE_WINDOW = "org.jahia.toolbar.item.OpenEngineWindow";
    public static final String ORG_JAHIA_TOOLBAR_ITEM_OPEN_WINDOW = "org.jahia.toolbar.item.OpenWindow";
    public static final String ORG_JAHIA_TOOLBAR_ITEM_REDIRECT_WINDOW = "org.jahia.toolbar.item.RedirectWindow";
    public static final String ORG_JAHIA_TOOLBAR_ITEM_INFO_PANEL = "org.jahia.toolbar.item.InfoPanel";
    public static final String ORG_JAHIA_TOOLBAR_ITEM_AJAX_ACTION = "org.jahia.toolbar.item.AjaxAction";
    public static final String ORG_JAHIA_TOOLBAR_ITEM_ADVANCED_PREVIEW = "org.jahia.toolbar.item.AdvancedPreview";
    public static final String ORG_JAHIA_TOOLBAR_ITEM_ADVANCED_COMPARE = "org.jahia.toolbar.item.AdvancedCompare";
    public static final String ORG_JAHIA_TOOLBAR_ITEM_BOOKMARKS = "org.jahia.toolbar.item.bookmarks";
    public static final String ORG_JAHIA_TOOLBAR_ITEM_NOTIFICATION = "org.jahia.toolbar.item.Notification";
    public static final String ORG_JAHIA_TOOLBAR_ITEM_QUICK_WORKFLOW = "org.jahia.toolbar.item.QuickWorkflow";
    public static final String ORG_JAHIA_TOOLBAR_ITEM_OPEN_HTML_WINDOW = "org.jahia.toolbar.item.OpenHTMLWindow";
    public static final String ORG_JAHIA_TOOLBAR_ITEM_SUBSCRIPTIONS = "org.jahia.toolbar.item.Subscriptions";
    public static final String ORG_JAHIA_TOOLBAR_ITEM_LANGUAGE_SWITCHER = "org.jahia.toolbar.item.LanguageSwitcher";
    public static final String ORG_JAHIA_TOOLBAR_ITEM_TOOLBARS = "org.jahia.toolbar.item.Toolbars";


    // these type are  special ones: its handled directly by the Toolbar
    public static final String ORG_JAHIA_TOOLBAR_ITEM_SEPARATOR = "org.jahia.toolbar.item.Separator";
    public static final String ORG_JAHIA_TOOLBAR_ITEM_FILL = "org.jahia.toolbar.item.fill";


    public JahiaToolItemProvider getJahiaToolItemProvider(String type) {
        if (type == null) {
            return null;
        } else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_CLIPBOARD)) {
            return new ClipboardJahiaToolItemProvider();
        } else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_OPEN_ENGINE_WINDOW)) {
            return new OpenEngineWindowJahiaToolItemProvider();
        } else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_OPEN_WINDOW)) {
            return new OpenWindowJahiaToolItemProvider();
        } else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_REDIRECT_WINDOW)) {
            return new RedirectWindowJahiaToolItemProvider();
        }  else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_INFO_PANEL)) {
            return new InfoJahiaItemProvider();
        } else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_AJAX_ACTION)) {
            return new AjaxActionJahiaToolItemProvider();
        } else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_ADVANCED_PREVIEW)) {
            return new AdvancedPreviewJahiaToolItemProvider();
        } else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_ADVANCED_COMPARE)) {
            return new AdvancedCompareModeJahiaToolItemProvider();
        } else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_BOOKMARKS)) {
            return new BookmarkJahiaToolItemProvider();
        }else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_NOTIFICATION)) {
            return new NotificationJahiaToolItemProvider();
        }else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_QUICK_WORKFLOW)) {
            return new QuickWorkflowJahiaToolItemProvider() ;
        } else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_OPEN_HTML_WINDOW)) {
            return new OpenHTMLWindowJahiaToolItemProvider() ;
        } else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_SUBSCRIPTIONS)) {
            return new SubscriptionsJahiaToolItemProvider() ;
        } else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_LANGUAGE_SWITCHER)) {
            return new LanguageSwitcherProvider();
        } else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_TOOLBARS)) {
            return new ToobarsJahiaToolItemProvider();
        }
        return null;
    }
}
