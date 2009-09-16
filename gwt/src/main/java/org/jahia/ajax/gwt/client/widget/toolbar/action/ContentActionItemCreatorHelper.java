package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.widget.toolbar.action.ContentActionItem;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters; /**
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
 **/

/**
 * User: ktlili
 * Date: Sep 16, 2009
 * Time: 9:49:14 AM
 */
public class ContentActionItemCreatorHelper {
    public static ContentActionItem createRotateItem(final BrowserLinker linker) {
        ContentActionItem rotate = new ContentActionItem(Messages.getResource("fm_rotate"), "fm-rotate") {
            public void onSelection() {
                ContentActions.rotateImage(linker);
            }

            public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
                setEnabled(tableSelection && parentWritable && singleFile && isImage);
            }
        };
        return rotate;
    }

    public static ContentActionItem createCropItem(final BrowserLinker linker) {
        ContentActionItem crop = new ContentActionItem(Messages.getResource("fm_crop"), "fm-crop") {
            public void onSelection() {
                ContentActions.cropImage(linker);
            }

            public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
                setEnabled(tableSelection && parentWritable && singleFile && isImage);
            }
        };
        return crop;
    }

    public static ContentActionItem createUnzipItem(final BrowserLinker linker) {
        ContentActionItem unzip = new ContentActionItem(Messages.getResource("fm_unzip"), "fm-unzip") {
            public void onSelection() {
                ContentActions.unzip(linker);
            }

            public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
                setEnabled(tableSelection && parentWritable && singleFile && isZip);
            }
        };
        return unzip;
    }

    public static ContentActionItem createUnlockItem(final BrowserLinker linker) {
        ContentActionItem unlock = new ContentActionItem(Messages.getResource("fm_unlock"), "fm-unlock") {
            public void onSelection() {
                ContentActions.lock(false, linker);
            }

            public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
                setEnabled(tableSelection && lockable && writable);
            }
        };
        return unlock;
    }

    public static ContentActionItem createWebfolderItem(final BrowserLinker linker) {
        ContentActionItem webFolder = new ContentActionItem(Messages.getResource("fm_webfolder"), "fm-webfolder") {
            public void onSelection() {
                ContentActions.openWebFolder(linker);
            }

            public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
                setEnabled(treeSelection || tableSelection && singleFolder);
            }
        };
        return webFolder;
    }

    public static ContentActionItem createNewFolderItem(final BrowserLinker linker) {
        ContentActionItem newFolder = new ContentActionItem(Messages.getResource("fm_newdir"), "fm-newfolder") {
            public void onSelection() {
                ContentActions.createFolder(linker);
            }

            public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
                setEnabled(treeSelection && parentWritable || tableSelection && singleFolder && writable);
            }
        };
        return newFolder;
    }

    public static ContentActionItem createCutItem(final BrowserLinker linker) {
        ContentActionItem cut = new ContentActionItem(Messages.getResource("fm_cut"), "fm-cut") {
            public void onSelection() {
                ContentActions.cut(linker);
            }

            public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
                setEnabled(tableSelection && writable);
            }
        };
        return cut;
    }

    public static ContentActionItem createRemoveItem(final BrowserLinker linker) {
        ContentActionItem remove = new ContentActionItem(Messages.getResource("fm_remove"), "fm-remove") {
            public void onSelection() {
                ContentActions.remove(linker);
            }

            public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
                setEnabled(tableSelection && deleteable && !isMount);
            }
        };
        return remove;
    }

    public static ContentActionItem createPasteItem(final BrowserLinker linker) {
        ContentActionItem paste = new ContentActionItem(Messages.getResource("fm_paste"), "fm-paste") {
            public void onSelection() {
                ContentActions.paste(linker);
            }

            public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
                setEnabled(treeSelection && parentWritable && pasteAllowed || tableSelection && writable && pasteAllowed);
            }
        };
        return paste;
    }

    public static ContentActionItem createPasteReferenceItem(final BrowserLinker linker) {
        ContentActionItem paste = new ContentActionItem(Messages.getResource("fm_pasteref"), "fm-pasteref") {
            public void onSelection() {
                ContentActions.pasteReference(linker);
            }

            public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
                setEnabled(treeSelection && parentWritable && pasteAllowed || tableSelection && writable && pasteAllowed);
            }
        };
        return paste;
    }

    public static ContentActionItem createCopyItem(final BrowserLinker linker) {
        ContentActionItem copy = new ContentActionItem(Messages.getResource("fm_copy"), "fm-copy") {
            public void onSelection() {
                ContentActions.copy(linker);
            }

            public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
                setEnabled(tableSelection);
            }
        };
        return copy;
    }

    public static ContentActionItem createRenameItem(final BrowserLinker linker) {
        ContentActionItem rename = new ContentActionItem(Messages.getResource("fm_rename"), "fm-rename") {
            public void onSelection() {
                ContentActions.rename(linker);
            }

            public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
                setEnabled(tableSelection && writable && (singleFile || singleFolder));
            }
        };
        return rename;
    }

    public static ContentActionItem createResizeItem(final BrowserLinker linker) {
        ContentActionItem resize = new ContentActionItem(Messages.getResource("fm_resize"), "fm-resize") {
            public void onSelection() {
                ContentActions.resizeImage(linker);
            }

            public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
                setEnabled(tableSelection && parentWritable && singleFile && isImage);
            }
        };
        return resize;
    }

    public static ContentActionItem createMountItem(final BrowserLinker linker) {
        ContentActionItem mount = new ContentActionItem(Messages.getResource("fm_mount"), "fm-mount") {
            public void onSelection() {
                ContentActions.mountFolder(linker);
            }

            public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
                setEnabled("root".equals(JahiaGWTParameters.getCurrentUser())); // TODO dirty code (to refactor using server side configuration and roles)
            }
        };
        return mount;
    }

    public static ContentActionItem createUnmountItem(final BrowserLinker linker) {
        ContentActionItem mount = new ContentActionItem(Messages.getResource("fm_unmount"), "fm-unmount") {
            public void onSelection() {
                ContentActions.unmountFolder(linker);
            }

            public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
                setEnabled(tableSelection && writable && isMount);
            }
        };
        return mount;
    }

    public static ContentActionItem createZipItem(final BrowserLinker linker) {
        ContentActionItem zip = new ContentActionItem(Messages.getResource("fm_zip"), "fm-zip") {
            public void onSelection() {
                ContentActions.zip(linker);
            }

            public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
                setEnabled(tableSelection && parentWritable);
            }
        };
        return zip;
    }

    public static ContentActionItem createLockItem(final BrowserLinker linker) {
        ContentActionItem lock = new ContentActionItem(Messages.getResource("fm_lock"), "fm-lock") {
            public void onSelection() {
                ContentActions.lock(true, linker);
            }

            public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
                setEnabled(tableSelection && lockable && writable);
            }
        };
        return lock;
    }

    public static ContentActionItem createDownloadItem(final BrowserLinker linker) {
        ContentActionItem download = new ContentActionItem(Messages.getResource("fm_download"), "fm-download") {
            public void onSelection() {
                ContentActions.download(linker);
            }

            public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
                setEnabled(tableSelection && singleFile);
            }
        };
        return download;
    }

    public static ContentActionItem createPreviewItem(final BrowserLinker linker) {
        ContentActionItem download = new ContentActionItem(Messages.getResource("fm_preview"), "fm-preview") {
            public void onSelection() {
                ContentActions.preview(linker);
            }

            public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
                setEnabled(tableSelection && singleFile && isImage);
            }
        };
        return download;
    }

    public static ContentActionItem createUploadItem(final BrowserLinker linker) {
        ContentActionItem upload = new ContentActionItem(Messages.getResource("fm_upload"), "fm-upload") {
            public void onSelection() {
                ContentActions.upload(linker);
            }

            public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
                setEnabled(treeSelection && parentWritable || tableSelection && singleFolder && writable);
            }
        };
        return upload;
    }

    /**
     * Item that creates a new mashup
     *
     * @param linker
     * @return
     */
    public static ContentActionItem createNewMashupItem(final BrowserLinker linker) {
        ContentActionItem newMashup = new ContentActionItem(Messages.getResource("fm_newmashup"), "fm-newmashup") {
            public void onSelection() {
                ContentActions.showMashupWizard(linker);
            }

            public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
                setEnabled(treeSelection && parentWritable || tableSelection && singleFolder && writable);
            }
        };
        return newMashup;
    }

    /**
     * Item that creates a new RSS item
     *
     * @param linker
     * @return
     */
    public static ContentActionItem createNewRSSItem(final BrowserLinker linker) {
        ContentActionItem newMashup = new ContentActionItem(Messages.getResource("fm_newrssmashup"), "fm-newrssmashup") {
            public void onSelection() {
                ContentActions.showRSSForm(linker);
            }

            public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
                setEnabled(treeSelection && parentWritable || tableSelection && singleFolder && writable);
            }
        };
        return newMashup;
    }

    /**
     * Item that creates a new Google Gadget item
     *
     * @param linker
     * @return
     */
    public static ContentActionItem createNewGadgetItem(final BrowserLinker linker) {
        ContentActionItem newMashup = new ContentActionItem(Messages.getResource("fm_newgadgetmashup"), "fm-newgooglegadgetmashup") {
            public void onSelection() {
                ContentActions.showGoogleGadgetForm(linker);
            }

            public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
                setEnabled(treeSelection && parentWritable || tableSelection && singleFolder && writable);
            }
        };
        return newMashup;
    }

    /**
     * Item that creates a new category
     *
     * @param linker
     * @return
     */
    public static ContentActionItem createNewCategoryItem(final BrowserLinker linker) {
        ContentActionItem newCategory = new ContentActionItem(Messages.getResource("fm_newcategory"), "fm-newcategory") {
            public void onSelection() {
                ContentActions.showContentWizard(linker, "jnt:category");
            }

            public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
                setEnabled(treeSelection && parentWritable || tableSelection && singleFolder && writable);
            }
        };
        return newCategory;
    }

    /**
     * Item that open a Portlet Upload manager
     *
     * @param linker
     * @return
     */
    public static ContentActionItem createDeployPortletDefinition(final BrowserLinker linker) {
        ContentActionItem actionItem = new ContentActionItem(Messages.getNotEmptyResource("fm_deployPortlet", "Deploy Portlet"), "fm-newmashup") {
            public void onSelection() {
                ContentActions.showDeployPortletForm(linker);
            }

            public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
                setEnabled(treeSelection && parentWritable || tableSelection && singleFolder && writable);
            }
        };
        return actionItem;
    }

    /**
     * Item that creates a new content item action
     *
     * @param linker
     * @return
     */
    public static ContentActionItem createNewContentItem(final BrowserLinker linker) {
        ContentActionItem newContent = new ContentActionItem(Messages.getResource("fm_newcontent"), "fm-newcontent") {
            public void onSelection() {
                ContentActions.showContentWizard(linker);
            }

            public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
                setEnabled(treeSelection && parentWritable || tableSelection && singleFolder && writable);
            }
        };
        return newContent;
    }

    /**
     * Item that  loows creating a new page
     *
     * @param linker
     * @return
     */
    public static ContentActionItem createNewPageContentItem(final BrowserLinker linker) {

        ContentActionItem newPageContent = new ContentActionItem(Messages.getNotEmptyResource("fm_newpagecontent", "New page"), "fm-newcontent") {
            public void onSelection() {
                ContentActions.showContentWizard(linker, "jnt:page");
            }

            public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
                setEnabled(treeSelection && parentWritable || tableSelection && singleFolder && writable);
            }
        };
        return newPageContent;
    }

    /**
     * Item that  loows creating a new page
     *
     * @param linker
     * @return
     */
    public static ContentActionItem createNewContentListItem(final BrowserLinker linker) {

        ContentActionItem newContentListItem = new ContentActionItem(Messages.getNotEmptyResource("fm_newcontentlist", "New content list"), "fm-newfolder") {
            public void onSelection() {
                ContentActions.showContentWizard(linker, "jnt:contentList");
            }

            public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
                setEnabled(treeSelection && parentWritable || tableSelection && singleFolder && writable);
            }
        };
        return newContentListItem;
    }

    public static ContentActionItem createExportItem(final BrowserLinker linker) {
        ContentActionItem exportItem = new ContentActionItem(Messages.getResource("fm_export"), "fm-export") {
            public void onSelection() {
                ContentActions.exportContent(linker);
            }

            public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
                setEnabled(tableSelection || treeSelection);
            }
        };
        return exportItem;
    }

    public static ContentActionItem createImportItem(final BrowserLinker linker) {
        ContentActionItem importItem = new ContentActionItem(Messages.getResource("fm_import"), "fm-import") {
            public void onSelection() {
                ContentActions.importContent(linker);
            }

            public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
                setEnabled(treeSelection && parentWritable);
            }
        };
        return importItem;
    }
}
