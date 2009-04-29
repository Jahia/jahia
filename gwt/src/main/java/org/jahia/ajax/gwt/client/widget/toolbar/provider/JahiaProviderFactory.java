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
package org.jahia.ajax.gwt.client.widget.toolbar.provider;

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
        }
        return null;
    }
}
