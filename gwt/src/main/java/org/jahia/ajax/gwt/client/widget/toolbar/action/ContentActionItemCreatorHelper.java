package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.toolbar.handler.ManagerSelectionHandler;

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
 **/

/**
 * User: ktlili
 * Date: Sep 16, 2009
 * Time: 9:49:14 AM
 */
public class ContentActionItemCreatorHelper {
    public static ContentActionItem createRotateItem(final Linker linker) {
        return new RotateActionItem(linker);
    }

    public static ContentActionItem createCropItem(final Linker linker) {
        return new CropActionItem(linker);
    }

    public static ContentActionItem createUnzipItem(final Linker linker) {
        return new UnzipActionItem(linker);
    }

    public static ContentActionItem createUnlockItem(final Linker linker) {
        return new UnlockActionItem(linker);
    }

    public static ContentActionItem createWebfolderItem(final Linker linker) {
        return new WebfolderActionItem(linker);
    }

    public static ContentActionItem createNewFolderItem(final Linker linker) {
        return new NewFolderActionItem(linker);
    }

    public static ContentActionItem createCutItem(final Linker linker) {
        return new CutActionItem(linker);
    }

    public static ContentActionItem createRemoveItem(final Linker linker) {
        return new RemoveActionItem(linker);
    }

    public static ContentActionItem createPasteItem(final Linker linker) {
        return new PasteActionItem(linker);
    }

    public static ContentActionItem createPasteReferenceItem(final Linker linker) {
        return new PasteReferenceActionItem(linker);
    }

    public static ContentActionItem createCopyItem(final Linker linker) {
        return new CopyActionItem(linker);
    }

    public static ContentActionItem createRenameItem(final Linker linker) {
        return new RenameActionItem(linker);
    }

    public static ContentActionItem createResizeItem(final Linker linker) {
        return new ResizeActionItem(linker);
    }

    public static ContentActionItem createMountItem(final Linker linker) {
        return new MountActionItem(linker);
    }

    public static ContentActionItem createUnmountItem(final Linker linker) {
        return new UnmountActionItem(linker);
    }

    public static ContentActionItem createZipItem(final Linker linker) {
        return new ZipActionItem(linker);
    }

    public static ContentActionItem createLockItem(final Linker linker) {
        return new LockActionItem(linker);
    }

    public static ContentActionItem createDownloadItem(final Linker linker) {
        return new DownloadActionItem(linker);
    }

    public static ContentActionItem createPreviewItem(final Linker linker) {
        return new PreviewActionItem(linker);
    }

    public static ContentActionItem createUploadItem(final Linker linker) {
        return new UploadActionItem(linker);
    }

    /**
     * Item that creates a new mashup
     *
     * @param linker
     * @return
     */
    public static ContentActionItem createNewMashupItem(final Linker linker) {
        return new NewMashupActionItem(linker);
    }

    /**
     * Item that creates a new RSS item
     *
     * @param linker
     * @return
     */
    public static ContentActionItem createNewRSSItem(final Linker linker) {
        return new NewRssActionItem(linker);
    }

    /**
     * Item that creates a new Google Gadget item
     *
     * @param linker
     * @return
     */
    public static ContentActionItem createNewGadgetItem(final Linker linker) {
        return new NewGadgetActionItem(linker);
    }

    /**
     * Item that creates a new category
     *
     * @param linker
     * @return
     */
    public static ContentActionItem createNewCategoryItem(final Linker linker) {
        return new NewCategoryActionItem(linker);
    }

    /**
     * Item that open a Portlet Upload manager
     *
     * @param linker
     * @return
     */
    public static ContentActionItem createDeployPortletDefinition(final Linker linker) {
        return new DeployPortletDefinitionActionItem(linker);
    }

    /**
     * Item that creates a new content item action
     *
     * @param linker
     * @return
     */
    public static ContentActionItem createNewContentItem(final Linker linker) {
        return new NewContentActionItem(linker);
    }

    /**
     * Item that  loows creating a new page
     *
     * @param linker
     * @return
     */
    public static ContentActionItem createNewPageContentItem(final Linker linker) {

        return new NewPageActionItem(linker);
    }

    /**
     * Item that allows creating a new page
     *
     * @param linker
     * @return
     */
    public static ContentActionItem createNewContentListItem(final Linker linker) {
        return new NewContentListActionItem(linker);
    }

    public static ContentActionItem createExportItem(final Linker linker) {
        return new ExportActionItem(linker);
    }

    public static ContentActionItem createImportItem(final Linker linker) {
        return new ImportActionItem(linker);
    }

    public static ContentActionItem createRefreshItem(final Linker linker) {
    	return new ContentActionItem(Messages.getResource("fm_refresh"), "fm-refresh") {
            public void onSelection() {
                linker.refresh();
            }
        };
    }
    
    private static class RotateActionItem extends ContentActionItem implements ManagerSelectionHandler {
        private final Linker linker;

        public RotateActionItem(Linker linker) {
            super(Messages.getResource("fm_rotate"), "fm-rotate");
            this.linker = linker;
        }

        public void onSelection() {
            ContentActions.rotateImage(linker);
        }

        public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
            setEnabled(tableSelection && parentWritable && singleFile && isImage);
        }
    }

    private static class CropActionItem extends ContentActionItem  implements ManagerSelectionHandler {
        private final Linker linker;

        public CropActionItem(Linker linker) {
            super(Messages.getResource("fm_crop"), "fm-crop");
            this.linker = linker;
        }

        public void onSelection() {
            ContentActions.cropImage(linker);
        }

        public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
            setEnabled(tableSelection && parentWritable && singleFile && isImage);
        }
    }

    private static class UnzipActionItem extends ContentActionItem  implements ManagerSelectionHandler {
        private final Linker linker;

        public UnzipActionItem(Linker linker) {
            super(Messages.getResource("fm_unzip"), "fm-unzip");
            this.linker = linker;
        }

        public void onSelection() {
            ContentActions.unzip(linker);
        }

        public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
            setEnabled(tableSelection && parentWritable && singleFile && isZip);
        }
    }

    private static class UnlockActionItem extends ContentActionItem  implements ManagerSelectionHandler {
        private final Linker linker;

        public UnlockActionItem(Linker linker) {
            super(Messages.getResource("fm_unlock"), "fm-unlock");
            this.linker = linker;
        }

        public void onSelection() {
            ContentActions.lock(false, linker);
        }

        public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
            setEnabled(tableSelection && lockable && writable);
        }
    }

    private static class WebfolderActionItem extends ContentActionItem  implements ManagerSelectionHandler {
        private final Linker linker;

        public WebfolderActionItem(Linker linker) {
            super(Messages.getResource("fm_webfolder"), "fm-webfolder");
            this.linker = linker;
        }

        public void onSelection() {
            ContentActions.openWebFolder(linker);
        }

        public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
            setEnabled(treeSelection || tableSelection && singleFolder);
        }
    }

    private static class NewFolderActionItem extends ContentActionItem  implements ManagerSelectionHandler {
        private final Linker linker;

        public NewFolderActionItem(Linker linker) {
            super(Messages.getResource("fm_newdir"), "fm-newfolder");
            this.linker = linker;
        }

        public void onSelection() {
            ContentActions.createFolder(linker);
        }

        public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
            setEnabled(treeSelection && parentWritable || tableSelection && singleFolder && writable);
        }
    }

    private static class CutActionItem extends ContentActionItem  implements ManagerSelectionHandler {
        private final Linker linker;

        public CutActionItem(Linker linker) {
            super(Messages.getResource("fm_cut"), "fm-cut");
            this.linker = linker;
        }

        public void onSelection() {
            ContentActions.cut(linker);
        }

        public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
            setEnabled(tableSelection && writable);
        }
    }

    private static class RemoveActionItem extends ContentActionItem  implements ManagerSelectionHandler {
        private final Linker linker;

        public RemoveActionItem(Linker linker) {
            super(Messages.getResource("fm_remove"), "fm-remove");
            this.linker = linker;
        }

        public void onSelection() {
            ContentActions.remove(linker);
        }

        public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
            setEnabled(tableSelection && deleteable && !isMount);
        }
    }

    private static class PasteActionItem extends ContentActionItem  implements ManagerSelectionHandler {
        private final Linker linker;

        public PasteActionItem(Linker linker) {
            super(Messages.getResource("fm_paste"), "fm-paste");
            this.linker = linker;
        }

        public void onSelection() {
            ContentActions.paste(linker);
        }

        public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
            setEnabled(treeSelection && parentWritable && pasteAllowed || tableSelection && writable && pasteAllowed);
        }
    }

    private static class PasteReferenceActionItem extends ContentActionItem  implements ManagerSelectionHandler {
        private final Linker linker;

        public PasteReferenceActionItem(Linker linker) {
            super(Messages.getResource("fm_pasteref"), "fm-pasteref");
            this.linker = linker;
        }

        public void onSelection() {
            ContentActions.pasteReference(linker);
        }

        public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
            setEnabled(treeSelection && parentWritable && pasteAllowed || tableSelection && writable && pasteAllowed);
        }
    }

    private static class CopyActionItem extends ContentActionItem  implements ManagerSelectionHandler {
        private final Linker linker;

        public CopyActionItem(Linker linker) {
            super(Messages.getResource("fm_copy"), "fm-copy");
            this.linker = linker;
        }

        public void onSelection() {
            ContentActions.copy(linker);
        }

        public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
            setEnabled(tableSelection);
        }
    }

    private static class RenameActionItem extends ContentActionItem  implements ManagerSelectionHandler {
        private final Linker linker;

        public RenameActionItem(Linker linker) {
            super(Messages.getResource("fm_rename"), "fm-rename");
            this.linker = linker;
        }

        public void onSelection() {
            ContentActions.rename(linker);
        }

        public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
            setEnabled(tableSelection && writable && (singleFile || singleFolder));
        }
    }

    private static class ResizeActionItem extends ContentActionItem  implements ManagerSelectionHandler {
        private final Linker linker;

        public ResizeActionItem(Linker linker) {
            super(Messages.getResource("fm_resize"), "fm-resize");
            this.linker = linker;
        }

        public void onSelection() {
            ContentActions.resizeImage(linker);
        }

        public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
            setEnabled(tableSelection && parentWritable && singleFile && isImage);
        }
    }

    private static class MountActionItem extends ContentActionItem  implements ManagerSelectionHandler {
        private final Linker linker;

        public MountActionItem(Linker linker) {
            super(Messages.getResource("fm_mount"), "fm-mount");
            this.linker = linker;
        }

        public void onSelection() {
            ContentActions.mountFolder(linker);
        }

        public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
            setEnabled("root".equals(JahiaGWTParameters.getCurrentUser())); // TODO dirty code (to refactor using server side configuration and roles)
        }
    }

    private static class UnmountActionItem extends ContentActionItem  implements ManagerSelectionHandler {
        private final Linker linker;

        public UnmountActionItem(Linker linker) {
            super(Messages.getResource("fm_unmount"), "fm-unmount");
            this.linker = linker;
        }

        public void onSelection() {
            ContentActions.unmountFolder(linker);
        }

        public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
            setEnabled(tableSelection && writable && isMount);
        }
    }

    private static class ZipActionItem extends ContentActionItem  implements ManagerSelectionHandler {
        private final Linker linker;

        public ZipActionItem(Linker linker) {
            super(Messages.getResource("fm_zip"), "fm-zip");
            this.linker = linker;
        }

        public void onSelection() {
            ContentActions.zip(linker);
        }

        public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
            setEnabled(tableSelection && parentWritable);
        }
    }

    private static class LockActionItem extends ContentActionItem  implements ManagerSelectionHandler {
        private final Linker linker;

        public LockActionItem(Linker linker) {
            super(Messages.getResource("fm_lock"), "fm-lock");
            this.linker = linker;
        }

        public void onSelection() {
            ContentActions.lock(true, linker);
        }

        public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
            setEnabled(tableSelection && lockable && writable);
        }
    }

    private static class DownloadActionItem extends ContentActionItem  implements ManagerSelectionHandler {
        private final Linker linker;

        public DownloadActionItem(Linker linker) {
            super(Messages.getResource("fm_download"), "fm-download");
            this.linker = linker;
        }

        public void onSelection() {
            ContentActions.download(linker);
        }

        public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
            setEnabled(tableSelection && singleFile);
        }
    }

    private static class PreviewActionItem extends ContentActionItem implements ManagerSelectionHandler {
        private final Linker linker;

        public PreviewActionItem(Linker linker) {
            super(Messages.getResource("fm_preview"), "fm-preview");
            this.linker = linker;
        }

        public void onSelection() {
            ContentActions.preview(linker);
        }

        public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
            setEnabled(tableSelection && singleFile && isImage);
        }
    }

    private static class UploadActionItem extends ContentActionItem  implements ManagerSelectionHandler {
        private final Linker linker;

        public UploadActionItem(Linker linker) {
            super(Messages.getResource("fm_upload"), "fm-upload");
            this.linker = linker;
        }

        public void onSelection() {
            ContentActions.upload(linker);
        }

        public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
            setEnabled(treeSelection && parentWritable || tableSelection && singleFolder && writable);
        }
    }

    private static class NewMashupActionItem extends ContentActionItem  implements ManagerSelectionHandler {
        private final Linker linker;

        public NewMashupActionItem(Linker linker) {
            super(Messages.getResource("fm_newmashup"), "fm-newmashup");
            this.linker = linker;
        }

        public void onSelection() {
            ContentActions.showMashupWizard(linker);
        }

        public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
            setEnabled(treeSelection && parentWritable || tableSelection && singleFolder && writable);
        }
    }

    private static class NewRssActionItem extends ContentActionItem implements ManagerSelectionHandler {
        private final Linker linker;

        public NewRssActionItem(Linker linker) {
            super(Messages.getResource("fm_newrssmashup"), "fm-newrssmashup");
            this.linker = linker;
        }

        public void onSelection() {
            ContentActions.showRSSForm(linker);
        }

        public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
            setEnabled(treeSelection && parentWritable || tableSelection && singleFolder && writable);
        }
    }

    private static class NewGadgetActionItem extends ContentActionItem implements ManagerSelectionHandler {
        private final Linker linker;

        public NewGadgetActionItem(Linker linker) {
            super(Messages.getResource("fm_newgadgetmashup"), "fm-newgooglegadgetmashup");
            this.linker = linker;
        }

        public void onSelection() {
            ContentActions.showGoogleGadgetForm(linker);
        }

        public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
            setEnabled(treeSelection && parentWritable || tableSelection && singleFolder && writable);
        }
    }

    private static class NewCategoryActionItem extends ContentActionItem  implements ManagerSelectionHandler {
        private final Linker linker;

        public NewCategoryActionItem(Linker linker) {
            super(Messages.getResource("fm_newcategory"), "fm-newcategory");
            this.linker = linker;
        }

        public void onSelection() {
            ContentActions.showContentWizard(linker, "jnt:category");
        }

        public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
            setEnabled(treeSelection && parentWritable || tableSelection && singleFolder && writable);
        }
    }

    private static class DeployPortletDefinitionActionItem extends ContentActionItem implements ManagerSelectionHandler {
        private final Linker linker;

        public DeployPortletDefinitionActionItem(Linker linker) {
            super(Messages.getNotEmptyResource("fm_deployPortlet", "Deploy Portlet"), "fm-newmashup");
            this.linker = linker;
        }

        public void onSelection() {
            ContentActions.showDeployPortletForm(linker);
        }

        public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
            setEnabled(treeSelection && parentWritable || tableSelection && singleFolder && writable);
        }
    }

    private static class NewContentActionItem extends ContentActionItem  implements ManagerSelectionHandler {
        private final Linker linker;

        public NewContentActionItem(Linker linker) {
            super(Messages.getResource("fm_newcontent"), "fm-newcontent");
            this.linker = linker;
        }

        public void onSelection() {
            ContentActions.showContentWizard(linker);
        }

        public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
            setEnabled(treeSelection && parentWritable || tableSelection && singleFolder && writable);
        }
    }

    private static class NewPageActionItem extends ContentActionItem  implements ManagerSelectionHandler {
        private final Linker linker;

        public NewPageActionItem(Linker linker) {
            super(Messages.getNotEmptyResource("fm_newpagecontent", "New page"), "fm-newcontent");
            this.linker = linker;
        }

        public void onSelection() {
            ContentActions.showContentWizard(linker, "jnt:page");
        }

        public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
            setEnabled(treeSelection && parentWritable || tableSelection && singleFolder && writable);
        }
    }

    private static class NewContentListActionItem extends ContentActionItem  implements ManagerSelectionHandler {
        private final Linker linker;

        public NewContentListActionItem(Linker linker) {
            super(Messages.getNotEmptyResource("fm_newcontentlist", "New content list"), "fm-newfolder");
            this.linker = linker;
        }

        public void onSelection() {
            ContentActions.showContentWizard(linker, "jnt:contentList");
        }

        public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
            setEnabled(treeSelection && parentWritable || tableSelection && singleFolder && writable);
        }
    }

    private static class ExportActionItem extends ContentActionItem  implements ManagerSelectionHandler {
        private final Linker linker;

        public ExportActionItem(Linker linker) {
            super(Messages.getResource("fm_export"), "fm-export");
            this.linker = linker;
        }

        public void onSelection() {
            ContentActions.exportContent(linker);
        }

        public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
            setEnabled(tableSelection || treeSelection);
        }
    }

    private static class ImportActionItem extends ContentActionItem  implements ManagerSelectionHandler {
        private final Linker linker;

        public ImportActionItem(Linker linker) {
            super(Messages.getResource("fm_import"), "fm-import");
            this.linker = linker;
        }

        public void onSelection() {
            ContentActions.importContent(linker);
        }

        public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
            setEnabled(treeSelection && parentWritable);
        }
    }
}
