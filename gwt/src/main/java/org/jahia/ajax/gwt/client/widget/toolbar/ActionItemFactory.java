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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.toolbar;

import org.jahia.ajax.gwt.client.widget.toolbar.action.*;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.toolbar.action.ActionItemItf;
import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Sep 7, 2009
 * Time: 1:51:40 PM
 */
public class ActionItemFactory extends ActionItemFactoryItf {
    private EditLinker editLinker;
    private BrowserLinker browserLinker;

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

    // edit action button
    public static final String ORG_JAHIA_TOOLBAR_ITEM_EDIT_ACTION_CREATE_PAGE = "org.jahia.toolbar.item.EditAction.createPage";
    public static final String ORG_JAHIA_TOOLBAR_ITEM_EDIT_ACTION_PUBLISH = "org.jahia.toolbar.item.EditAction.publish";
    public static final String ORG_JAHIA_TOOLBAR_ITEM_EDIT_ACTION_UNPUBLISH = "org.jahia.toolbar.item.EditAction.unpublish";
    public static final String ORG_JAHIA_TOOLBAR_ITEM_EDIT_ACTION_VIEW_PUBLISH_STATUS = "org.jahia.toolbar.item.EditAction.viewPublishStatus";
    public static final String ORG_JAHIA_TOOLBAR_ITEM_EDIT_ACTION_LOCK = "org.jahia.toolbar.item.EditAction.lock";
    public static final String ORG_JAHIA_TOOLBAR_ITEM_EDIT_ACTION_UNLOCK = "org.jahia.toolbar.item.EditAction.unlock";
    public static final String ORG_JAHIA_TOOLBAR_ITEM_EDIT_ACTION_EDIT = "org.jahia.toolbar.item.EditAction.edit";
    public static final String ORG_JAHIA_TOOLBAR_ITEM_EDIT_ACTION_DELETE = "org.jahia.toolbar.item.EditAction.delete";
    public static final String ORG_JAHIA_TOOLBAR_ITEM_EDIT_ACTION_STATUS = "org.jahia.toolbar.item.EditAction.status";

    // content action button
    public static final String ORG_JAHIA_TOOLBAR_ITEM_CONTENT_ACTION_COPY = "org.jahia.toolbar.item.ContentAction.copy";
    public static final String ORG_JAHIA_TOOLBAR_ITEM_CONTENT_ACTION_DELETE = "org.jahia.toolbar.item.ContentAction.delete";



    public ActionItemFactory(EditLinker editLinker) {
        this.editLinker = editLinker;
    }

    public ActionItemFactory(BrowserLinker browserLinker) {
        this.browserLinker = browserLinker;
    }

    public ActionItemFactory(EditLinker editLinker, BrowserLinker browserLinker) {
        this.editLinker = editLinker;
        this.browserLinker = browserLinker;
    }

    public ActionItemItf createActionItem(GWTJahiaToolbarItem gwtToolbarItem) {
        String type = gwtToolbarItem.getType();
        ActionItemItf actionItem = null;
        if (type == null) {
            return null;
        } else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_CLIPBOARD)) {
            actionItem = new ClipboardActionItem();
        } else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_OPEN_ENGINE_WINDOW)) {
            actionItem = new OpenEngineWindowActionItem();
        } else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_OPEN_WINDOW)) {
            actionItem = new OpenWindowActionItem();
        } else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_REDIRECT_WINDOW)) {
            actionItem = new RedirectWindowActionItem();
        }  else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_INFO_PANEL)) {
            actionItem = new InfoActionItem();
        } else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_AJAX_ACTION)) {
            actionItem = new AjaxActionActionItem();
        } else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_ADVANCED_PREVIEW)) {
            actionItem = new AdvancedPreviewActionItem();
        } else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_ADVANCED_COMPARE)) {
            actionItem = new AdvancedCompareModeActionItem();
        } else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_BOOKMARKS)) {
            actionItem = new BookmarkActionItem();
        }else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_NOTIFICATION)) {
            actionItem = new NotificationActionItem();
        }else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_QUICK_WORKFLOW)) {
            actionItem = new QuickWorkflowActionItem() ;
        } else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_OPEN_HTML_WINDOW)) {
            actionItem = new OpenHTMLWindowActionItem() ;
        } else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_SUBSCRIPTIONS)) {
            actionItem = new SubscriptionsActionItem() ;
        } else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_LANGUAGE_SWITCHER)) {
            actionItem = new LanguageSwitcherActionItem();
        } else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_EDIT_ACTION_CREATE_PAGE)) {
            actionItem = EditActionItemCreatorHelper.createEditCreateActionItem(gwtToolbarItem,editLinker);
        }else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_EDIT_ACTION_DELETE)) {
            actionItem = EditActionItemCreatorHelper.createEditDeleteActionItem(gwtToolbarItem,editLinker);
        }else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_EDIT_ACTION_EDIT)) {
            actionItem = EditActionItemCreatorHelper.createEditEditActionItem(gwtToolbarItem,editLinker);
        }else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_EDIT_ACTION_LOCK)) {
            actionItem = EditActionItemCreatorHelper.createEditLockActionItem(gwtToolbarItem,editLinker);
        }else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_EDIT_ACTION_PUBLISH)) {
            actionItem = EditActionItemCreatorHelper.createEditPublishActionItem(gwtToolbarItem,editLinker);
        }else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_EDIT_ACTION_STATUS)) {
            actionItem = EditActionItemCreatorHelper.createEditStatusActionItem(gwtToolbarItem,editLinker);
        }else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_EDIT_ACTION_UNLOCK)) {
            actionItem = EditActionItemCreatorHelper.createEditUnlockActionItem(gwtToolbarItem,editLinker);
        }  else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_EDIT_ACTION_UNPUBLISH)) {
            actionItem = EditActionItemCreatorHelper.createEditUnpublishActionItem(gwtToolbarItem,editLinker);
        }else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_CONTENT_ACTION_COPY)) {
            actionItem = ContentActionItemCreatorHelper.createCopyItem(browserLinker);
        }else if (type.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_CONTENT_ACTION_DELETE)) {
            actionItem = ContentActionItemCreatorHelper.createRemoveItem(browserLinker);
        }
        if(actionItem != null){
            actionItem.setGwtToolbarItem(gwtToolbarItem);
        }
        return actionItem;
    }

}
