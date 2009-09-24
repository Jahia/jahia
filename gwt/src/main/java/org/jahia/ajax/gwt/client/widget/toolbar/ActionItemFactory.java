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
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.toolbar;

import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.toolbar.action.*;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.messages.Messages;

/**
 * Toolbar action item factory service.
 * User: ktlili
 * Date: Sep 7, 2009
 * Time: 1:51:40 PM
 */
public class ActionItemFactory extends ActionItemFactoryItf {
    private Linker linker;

    public static final String CLIPBOARD = "org.jahia.toolbar.item.ClipBoard";
    public static final String INFO = "org.jahia.toolbar.item.Inf";
    public static final String OPEN_ENGINE_WINDOW = "org.jahia.toolbar.item.OpenEngineWindow";
    public static final String OPEN_WINDOW = "org.jahia.toolbar.item.OpenWindow";
    public static final String REDIRECT_WINDOW = "org.jahia.toolbar.item.RedirectWindow";
    public static final String INFO_PANEL = "org.jahia.toolbar.item.InfoPanel";
    public static final String AJAX_ACTION = "org.jahia.toolbar.item.AjaxAction";
    public static final String ADVANCED_PREVIEW = "org.jahia.toolbar.item.AdvancedPreview";
    public static final String ADVANCED_COMPARE = "org.jahia.toolbar.item.AdvancedCompare";
    public static final String BOOKMARKS = "org.jahia.toolbar.item.bookmarks";
    public static final String NOTIFICATION = "org.jahia.toolbar.item.Notification";
    public static final String QUICK_WORKFLOW = "org.jahia.toolbar.item.QuickWorkflow";
    public static final String OPEN_HTML_WINDOW = "org.jahia.toolbar.item.OpenHTMLWindow";
    public static final String SUBSCRIPTIONS = "org.jahia.toolbar.item.Subscriptions";
    public static final String LANGUAGE_SWITCHER = "org.jahia.toolbar.item.LanguageSwitcher";


    // these type are  special ones: its handled directly by the Toolbar
    public static final String SEPARATOR = "org.jahia.toolbar.item.Separator";
    public static final String FILL = "org.jahia.toolbar.item.fill";

    // edit action button
    public static final String EDIT_ACTION_CREATE_PAGE = "org.jahia.toolbar.item.EditAction.createPage";
    public static final String EDIT_ACTION_PUBLISH = "org.jahia.toolbar.item.EditAction.publish";
    public static final String EDIT_ACTION_UNPUBLISH = "org.jahia.toolbar.item.EditAction.unpublish";
    public static final String EDIT_ACTION_VIEW_PUBLISH_STATUS = "org.jahia.toolbar.item.EditAction.viewPublishStatus";
    public static final String EDIT_ACTION_LOCK = "org.jahia.toolbar.item.EditAction.lock";
    public static final String EDIT_ACTION_UNLOCK = "org.jahia.toolbar.item.EditAction.unlock";
    public static final String EDIT_ACTION_EDIT = "org.jahia.toolbar.item.EditAction.edit";
    public static final String EDIT_ACTION_DELETE = "org.jahia.toolbar.item.EditAction.delete";
    public static final String EDIT_ACTION_STATUS = "org.jahia.toolbar.item.EditAction.status";

    // content action button
    public static final String CONTENT_ACTION_COPY = "org.jahia.toolbar.item.ContentAction.copy";
    public static final String CONTENT_ACTION_CREATE_CATEGORY = "org.jahia.toolbar.item.ContentAction.createCategory";
    public static final String CONTENT_ACTION_CREATE_CONTENT = "org.jahia.toolbar.item.ContentAction.createContent";
    public static final String CONTENT_ACTION_CREATE_CONTENT_LIST = "org.jahia.toolbar.item.ContentAction.createContentList";
    public static final String CONTENT_ACTION_CREATE_FOLDER = "org.jahia.toolbar.item.ContentAction.createFolder";
    public static final String CONTENT_ACTION_CREATE_GOOGLE_GADGET_MASHUP = "org.jahia.toolbar.item.ContentAction.createGoogleGadgetMashup";
    public static final String CONTENT_ACTION_CREATE_MASHUP = "org.jahia.toolbar.item.ContentAction.createMashup";
    public static final String CONTENT_ACTION_CREATE_RSS_MASHUP = "org.jahia.toolbar.item.ContentAction.createRssMashup";
    public static final String CONTENT_ACTION_CROP = "org.jahia.toolbar.item.ContentAction.crop";
    public static final String CONTENT_ACTION_CUT = "org.jahia.toolbar.item.ContentAction.cut";
    public static final String CONTENT_ACTION_DELETE = "org.jahia.toolbar.item.ContentAction.delete";
    public static final String CONTENT_ACTION_DOWNLOAD = "org.jahia.toolbar.item.ContentAction.download";
    public static final String CONTENT_ACTION_EXPORT = "org.jahia.toolbar.item.ContentAction.export";
    public static final String CONTENT_ACTION_IMPORT = "org.jahia.toolbar.item.ContentAction.import";
    public static final String CONTENT_ACTION_LOCK = "org.jahia.toolbar.item.ContentAction.lock";
    public static final String CONTENT_ACTION_MOUNT = "org.jahia.toolbar.item.ContentAction.mount";
    public static final String CONTENT_ACTION_PASTE = "org.jahia.toolbar.item.ContentAction.paste";
    public static final String CONTENT_ACTION_PASTE_REF = "org.jahia.toolbar.item.ContentAction.pasteReference";
    public static final String CONTENT_ACTION_PREVIEW = "org.jahia.toolbar.item.ContentAction.preview";
    public static final String CONTENT_ACTION_REFRESH = "org.jahia.toolbar.item.ContentAction.refresh";
    public static final String CONTENT_ACTION_RENAME = "org.jahia.toolbar.item.ContentAction.rename";
    public static final String CONTENT_ACTION_RESIZE = "org.jahia.toolbar.item.ContentAction.resize";
    public static final String CONTENT_ACTION_ROTATE = "org.jahia.toolbar.item.ContentAction.rotate";
    public static final String CONTENT_ACTION_UNLOCK = "org.jahia.toolbar.item.ContentAction.unlock";
    public static final String CONTENT_ACTION_UNMOUNT = "org.jahia.toolbar.item.ContentAction.unmount";
    public static final String CONTENT_ACTION_UNZIP = "org.jahia.toolbar.item.ContentAction.unzip";
    public static final String CONTENT_ACTION_UPLOAD = "org.jahia.toolbar.item.ContentAction.upload";
    public static final String CONTENT_ACTION_VIEW_DETAILS = "org.jahia.toolbar.item.ContentAction.viewDetails";
    public static final String CONTENT_ACTION_VIEW_LIST = "org.jahia.toolbar.item.ContentAction.viewList";
    public static final String CONTENT_ACTION_VIEW_THUMBS = "org.jahia.toolbar.item.ContentAction.viewThumbs";
    public static final String CONTENT_ACTION_WEBFOLDER = "org.jahia.toolbar.item.ContentAction.webfolder";
    public static final String CONTENT_ACTION_ZIP = "org.jahia.toolbar.item.ContentAction.zip";


    public ActionItemFactory(Linker linker) {
        this.linker = linker;
    }

    public ActionItemItf createActionItem(GWTJahiaToolbarItem gwtToolbarItem) {
        String type = gwtToolbarItem.getType();
        ActionItemItf actionItem = null;
        if (type == null) {
            return null;
        } else if (type.equalsIgnoreCase(CLIPBOARD)) {
            actionItem = new ClipboardActionItem();
        } else if (type.equalsIgnoreCase(OPEN_ENGINE_WINDOW)) {
            actionItem = new OpenEngineWindowActionItem();
        } else if (type.equalsIgnoreCase(OPEN_WINDOW)) {
            actionItem = new OpenWindowActionItem();
        } else if (type.equalsIgnoreCase(REDIRECT_WINDOW)) {
            actionItem = new RedirectWindowActionItem();
        }  else if (type.equalsIgnoreCase(INFO_PANEL)) {
            actionItem = new InfoActionItem();
        } else if (type.equalsIgnoreCase(AJAX_ACTION)) {
            actionItem = new AjaxActionActionItem();
        } else if (type.equalsIgnoreCase(ADVANCED_PREVIEW)) {
            actionItem = new AdvancedPreviewActionItem();
        } else if (type.equalsIgnoreCase(ADVANCED_COMPARE)) {
            actionItem = new AdvancedCompareModeActionItem();
        } else if (type.equalsIgnoreCase(BOOKMARKS)) {
            actionItem = new BookmarkActionItem();
        }else if (type.equalsIgnoreCase(NOTIFICATION)) {
            actionItem = new NotificationActionItem();
        }else if (type.equalsIgnoreCase(QUICK_WORKFLOW)) {
            actionItem = new QuickWorkflowActionItem() ;
        } else if (type.equalsIgnoreCase(OPEN_HTML_WINDOW)) {
            actionItem = new OpenHTMLWindowActionItem() ;
        } else if (type.equalsIgnoreCase(SUBSCRIPTIONS)) {
            actionItem = new SubscriptionsActionItem() ;
        } else if (type.equalsIgnoreCase(LANGUAGE_SWITCHER)) {
            actionItem = new LanguageSwitcherActionItem();
        } else if (type.equalsIgnoreCase(EDIT_ACTION_CREATE_PAGE)) {
            actionItem = EditActionItemCreatorHelper.createEditCreateActionItem(gwtToolbarItem,linker);
        }else if (type.equalsIgnoreCase(EDIT_ACTION_DELETE)) {
            actionItem = EditActionItemCreatorHelper.createEditDeleteActionItem(gwtToolbarItem,linker);
        }else if (type.equalsIgnoreCase(EDIT_ACTION_EDIT)) {
            actionItem = EditActionItemCreatorHelper.createEditEditActionItem(gwtToolbarItem,linker);
        }else if (type.equalsIgnoreCase(EDIT_ACTION_LOCK)) {
            actionItem = EditActionItemCreatorHelper.createEditLockActionItem(gwtToolbarItem,linker);
        }else if (type.equalsIgnoreCase(EDIT_ACTION_PUBLISH)) {
            actionItem = EditActionItemCreatorHelper.createEditPublishActionItem(gwtToolbarItem,linker);
        }else if (type.equalsIgnoreCase(EDIT_ACTION_STATUS)) {
            actionItem = EditActionItemCreatorHelper.createEditViewPublishStatusActionItem(gwtToolbarItem,linker);
        }else if (type.equalsIgnoreCase(EDIT_ACTION_UNLOCK)) {
            actionItem = EditActionItemCreatorHelper.createEditUnlockActionItem(gwtToolbarItem,linker);
        }  else if (type.equalsIgnoreCase(EDIT_ACTION_UNPUBLISH)) {
            actionItem = EditActionItemCreatorHelper.createEditUnpublishActionItem(gwtToolbarItem,linker);
        }else if (type.equalsIgnoreCase(CONTENT_ACTION_COPY)) {
            actionItem = ContentActionItemCreatorHelper.createCopyItem(linker);
        }else if (type.equalsIgnoreCase(CONTENT_ACTION_CREATE_CATEGORY)) {
            actionItem = ContentActionItemCreatorHelper.createNewCategoryItem(linker);
        }else if (type.equalsIgnoreCase(CONTENT_ACTION_CREATE_CONTENT)) {
            actionItem = ContentActionItemCreatorHelper.createNewContentItem(linker);
        }else if (type.equalsIgnoreCase(CONTENT_ACTION_CREATE_CONTENT_LIST)) {
            actionItem = ContentActionItemCreatorHelper.createNewContentListItem(linker);
        }else if (type.equalsIgnoreCase(CONTENT_ACTION_CREATE_FOLDER)) {
            actionItem = ContentActionItemCreatorHelper.createNewFolderItem(linker);
        }else if (type.equalsIgnoreCase(CONTENT_ACTION_CREATE_GOOGLE_GADGET_MASHUP)) {
            actionItem = ContentActionItemCreatorHelper.createNewGadgetItem(linker);
        }else if (type.equalsIgnoreCase(CONTENT_ACTION_CREATE_MASHUP)) {
            actionItem = ContentActionItemCreatorHelper.createNewMashupItem(linker);
        }else if (type.equalsIgnoreCase(CONTENT_ACTION_CREATE_RSS_MASHUP)) {
            actionItem = ContentActionItemCreatorHelper.createNewRSSItem(linker);
        }else if (type.equalsIgnoreCase(CONTENT_ACTION_CROP)) {
            actionItem = ContentActionItemCreatorHelper.createCropItem(linker);
        }else if (type.equalsIgnoreCase(CONTENT_ACTION_CUT)) {
            actionItem = ContentActionItemCreatorHelper.createCutItem(linker);
        }else if (type.equalsIgnoreCase(CONTENT_ACTION_DELETE)) {
            actionItem = ContentActionItemCreatorHelper.createRemoveItem(linker);
        }else if (type.equalsIgnoreCase(CONTENT_ACTION_DOWNLOAD)) {
            actionItem = ContentActionItemCreatorHelper.createDownloadItem(linker);
        }else if (type.equalsIgnoreCase(CONTENT_ACTION_EXPORT)) {
            actionItem = ContentActionItemCreatorHelper.createExportItem(linker);
        }else if (type.equalsIgnoreCase(CONTENT_ACTION_IMPORT)) {
            actionItem = ContentActionItemCreatorHelper.createImportItem(linker);
        }else if (type.equalsIgnoreCase(CONTENT_ACTION_LOCK)) {
            actionItem = ContentActionItemCreatorHelper.createLockItem(linker);
        }else if (type.equalsIgnoreCase(CONTENT_ACTION_MOUNT)) {
            actionItem = ContentActionItemCreatorHelper.createMountItem(linker);
        }else if (type.equalsIgnoreCase(CONTENT_ACTION_PASTE)) {
            actionItem = ContentActionItemCreatorHelper.createPasteItem(linker);
        }else if (type.equalsIgnoreCase(CONTENT_ACTION_PASTE_REF)) {
            actionItem = ContentActionItemCreatorHelper.createPasteReferenceItem(linker);
        }else if (type.equalsIgnoreCase(CONTENT_ACTION_PREVIEW)) {
            actionItem = ContentActionItemCreatorHelper.createPreviewItem(linker);
        }else if (type.equalsIgnoreCase(CONTENT_ACTION_REFRESH)) {
            actionItem = ContentActionItemCreatorHelper.createRefreshItem(linker);
        }else if (type.equalsIgnoreCase(CONTENT_ACTION_RENAME)) {
            actionItem = ContentActionItemCreatorHelper.createRenameItem(linker);
        }else if (type.equalsIgnoreCase(CONTENT_ACTION_RESIZE)) {
            actionItem = ContentActionItemCreatorHelper.createResizeItem(linker);
        }else if (type.equalsIgnoreCase(CONTENT_ACTION_ROTATE)) {
            actionItem = ContentActionItemCreatorHelper.createRotateItem(linker);
        }else if (type.equalsIgnoreCase(CONTENT_ACTION_UNLOCK)) {
            actionItem = ContentActionItemCreatorHelper.createUnlockItem(linker);
        }else if (type.equalsIgnoreCase(CONTENT_ACTION_UNMOUNT)) {
            actionItem = ContentActionItemCreatorHelper.createUnmountItem(linker);
        }else if (type.equalsIgnoreCase(CONTENT_ACTION_UNZIP)) {
            actionItem = ContentActionItemCreatorHelper.createUnzipItem(linker);
        }else if (type.equalsIgnoreCase(CONTENT_ACTION_UPLOAD)) {
            actionItem = ContentActionItemCreatorHelper.createUploadItem(linker);
        }else if (type.equalsIgnoreCase(CONTENT_ACTION_WEBFOLDER)) {
            actionItem = ContentActionItemCreatorHelper.createWebfolderItem(linker);
        }else if (type.equalsIgnoreCase(CONTENT_ACTION_ZIP)) {
            actionItem = ContentActionItemCreatorHelper.createZipItem(linker);
        }else if (type.equalsIgnoreCase(SEPARATOR)) {
            actionItem = new SeparatorActionItem();
        }
        if(actionItem != null){
            actionItem.setGwtToolbarItem(gwtToolbarItem);
        }
        return actionItem;
    }

}
