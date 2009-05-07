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
package org.jahia.ajax.gwt.client.util.nodes.actions;

import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;
import org.jahia.ajax.gwt.client.util.nodes.JCRClientUtils;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import com.extjs.gxt.ui.client.GXT;
import com.allen_sauer.gwt.log.client.Log;

/**
 * User: rfelden
 * Date: 7 janv. 2009 - 14:04:14
 */
public class ManagerConfigurationFactory {

    public static final String MANAGER_CONFIG = "conf";
    public static final String FILEMANAGER = "filemanager";
    public static final String MASHUPMANAGER = "mashupmanager";
    public static final String FILEPICKER = "filepicker";
    public static final String MASHUPPICKER = "mashuppicker";
    public static final String COMPLETE = "complete";

    public static ManagerConfiguration getConfiguration(String config, BrowserLinker linker) {
        if (config != null) {
            if (config.contains(FILEMANAGER)) {
                return getFileManagerConfiguration(linker);
            }
            if (config.contains(MASHUPMANAGER)) {
                return getMashupManagerConfiguration(linker);
            }
            if (config.contains(FILEPICKER)) {
                return getFilePickerConfiguration(linker);
            }
            if (config.contains(MASHUPPICKER)) {
                return getMashupPickerConfiguration(linker);
            }
            if (config.contains(COMPLETE)) {
                return getCompleteManagerConfiguration(linker);
            }
        }
        return getCompleteManagerConfiguration(linker);
    }

    public static ManagerConfiguration getCompleteManagerConfiguration(final BrowserLinker linker) {
        ManagerConfiguration completeManagerConfig = new ManagerConfiguration();
        completeManagerConfig.setEnableTextMenu(true);

        FileActionItemGroup file = new FileActionItemGroup(Messages.getResource("fm_fileMenu"));
        FileActionItem newFolder = ItemCreator.createNewFolderItem(linker);
        file.addItem(newFolder);
        completeManagerConfig.addItem(newFolder);
        FileActionItem newMashup = ItemCreator.createNewMashupItem(linker);
        file.addItem(newMashup);
        FileActionItem newRSS = ItemCreator.createNewRSSItem(linker);
        file.addItem(newRSS);
        FileActionItem newGadget = ItemCreator.createNewGadgetItem(linker);
        file.addItem(newGadget);
        completeManagerConfig.addItem(newMashup);
        FileActionItem upload = ItemCreator.createUploadItem(linker);
        file.addItem(upload);
        completeManagerConfig.addItem(upload);
        FileActionItem download = ItemCreator.createDownloadItem(linker);
        file.addItem(download);
        completeManagerConfig.addItem(download);
        if (GXT.isIE) {
            FileActionItem webFolder = ItemCreator.createWebfolderItem(linker);
            file.addItem(webFolder);
            completeManagerConfig.addItem(webFolder);
        }
        file.addItem(new FileActionItemSeparator());
        completeManagerConfig.addItem(new FileActionItemSeparator());
        FileActionItem lock = ItemCreator.createLockItem(linker);
        file.addItem(lock);
        FileActionItem unlock = ItemCreator.createUnlockItem(linker);
        file.addItem(unlock);
        file.addItem(new FileActionItemSeparator());
        FileActionItem zip = ItemCreator.createZipItem(linker);
        file.addItem(zip);
        FileActionItem unzip = ItemCreator.createUnzipItem(linker);
        file.addItem(unzip);

        FileActionItemGroup edit = new FileActionItemGroup(Messages.getResource("fm_editMenu"));
        FileActionItem rename = ItemCreator.createRenameItem(linker);
        edit.addItem(rename);
        completeManagerConfig.addItem(rename);
        FileActionItem remove = ItemCreator.createRemoveItem(linker);
        edit.addItem(remove);
        completeManagerConfig.addItem(remove);
        edit.addItem(new FileActionItemSeparator());
        completeManagerConfig.addItem(new FileActionItemSeparator());
        FileActionItem copy = ItemCreator.createCopyItem(linker);
        edit.addItem(copy);
        completeManagerConfig.addItem(copy);
        FileActionItem cut = ItemCreator.createCutItem(linker);
        edit.addItem(cut);
        completeManagerConfig.addItem(cut);
        FileActionItem paste = ItemCreator.createPasteItem(linker);
        edit.addItem(paste);
        completeManagerConfig.addItem(paste);

        FileActionItemGroup remote = new FileActionItemGroup(Messages.getResource("fm_remoteMenu"));
        FileActionItem mount = ItemCreator.createMountItem(linker);
        remote.addItem(mount);
        FileActionItem unmount = ItemCreator.createUnmountItem(linker);
        remote.addItem(unmount);

        FileActionItemGroup image = new FileActionItemGroup(Messages.getResource("fm_imageMenu"));
        FileActionItem crop = ItemCreator.createCropItem(linker);
        image.addItem(crop);
        FileActionItem resize = ItemCreator.createResizeItem(linker);
        image.addItem(resize);
        FileActionItem rotate = ItemCreator.createRotateItem(linker);
        image.addItem(rotate);

        // add menus to the config as well
        completeManagerConfig.addGroup(file);
        completeManagerConfig.addGroup(edit);
        completeManagerConfig.addGroup(remote);
        completeManagerConfig.addGroup(image);

        // no columns to add (default)

        // show all repository
        completeManagerConfig.addAccordion(JCRClientUtils.WEBSITE_REPOSITORY);
        completeManagerConfig.addAccordion(JCRClientUtils.SHARED_REPOSITORY);
        completeManagerConfig.addAccordion(JCRClientUtils.MY_EXTERNAL_REPOSITORY);
        completeManagerConfig.addAccordion(JCRClientUtils.MY_REPOSITORY);
        completeManagerConfig.addAccordion(JCRClientUtils.WEBSITE_MASHUP_REPOSITORY);
        completeManagerConfig.addAccordion(JCRClientUtils.SHARED_MASHUP_REPOSITORY);
        completeManagerConfig.addAccordion(JCRClientUtils.MY_MASHUP_REPOSITORY);
        completeManagerConfig.addAccordion(JCRClientUtils.GLOBAL_REPOSITORY);

        completeManagerConfig.setNodeTypes("");

        // show the current site (first) tab by default

        // do not hide the left panel (default)

        return completeManagerConfig;
    }

    public static ManagerConfiguration getFileManagerConfiguration(final BrowserLinker linker) {
        ManagerConfiguration fileManagerConfig = new ManagerConfiguration();
        fileManagerConfig.setEnableTextMenu(true);

        FileActionItemGroup file = new FileActionItemGroup(Messages.getResource("fm_fileMenu"));
        FileActionItem newFolder = ItemCreator.createNewFolderItem(linker);
        file.addItem(newFolder);
        fileManagerConfig.addItem(newFolder);
        FileActionItem upload = ItemCreator.createUploadItem(linker);
        file.addItem(upload);
        fileManagerConfig.addItem(upload);
        FileActionItem download = ItemCreator.createDownloadItem(linker);
        file.addItem(download);
        fileManagerConfig.addItem(download);
        FileActionItem preview = ItemCreator.createPreviewItem(linker);
        file.addItem(preview);
        fileManagerConfig.addItem(preview);
        if (GXT.isIE) {
            FileActionItem webFolder = ItemCreator.createWebfolderItem(linker);
            file.addItem(webFolder);
            fileManagerConfig.addItem(webFolder);
        }
        file.addItem(new FileActionItemSeparator());
        fileManagerConfig.addItem(new FileActionItemSeparator());
        FileActionItem lock = ItemCreator.createLockItem(linker);
        file.addItem(lock);
        FileActionItem unlock = ItemCreator.createUnlockItem(linker);
        file.addItem(unlock);
        file.addItem(new FileActionItemSeparator());
        FileActionItem zip = ItemCreator.createZipItem(linker);
        file.addItem(zip);
        FileActionItem unzip = ItemCreator.createUnzipItem(linker);
        file.addItem(unzip);

        FileActionItemGroup edit = new FileActionItemGroup(Messages.getResource("fm_editMenu"));
        FileActionItem rename = ItemCreator.createRenameItem(linker);
        edit.addItem(rename);
        fileManagerConfig.addItem(rename);
        FileActionItem remove = ItemCreator.createRemoveItem(linker);
        edit.addItem(remove);
        fileManagerConfig.addItem(remove);
        edit.addItem(new FileActionItemSeparator());
        fileManagerConfig.addItem(new FileActionItemSeparator());
        FileActionItem copy = ItemCreator.createCopyItem(linker);
        edit.addItem(copy);
        fileManagerConfig.addItem(copy);
        FileActionItem cut = ItemCreator.createCutItem(linker);
        edit.addItem(cut);
        fileManagerConfig.addItem(cut);
        FileActionItem paste = ItemCreator.createPasteItem(linker);
        edit.addItem(paste);
        fileManagerConfig.addItem(paste);

        FileActionItemGroup remote = new FileActionItemGroup(Messages.getResource("fm_remoteMenu"));
        FileActionItem mount = ItemCreator.createMountItem(linker);
        remote.addItem(mount);
        FileActionItem unmount = ItemCreator.createUnmountItem(linker);
        remote.addItem(unmount);

        FileActionItemGroup image = new FileActionItemGroup(Messages.getResource("fm_imageMenu"));
        FileActionItem crop = ItemCreator.createCropItem(linker);
        image.addItem(crop);
        FileActionItem resize = ItemCreator.createResizeItem(linker);
        image.addItem(resize);
        FileActionItem rotate = ItemCreator.createRotateItem(linker);
        image.addItem(rotate);

        // add menus to the config as well
        fileManagerConfig.addGroup(file);
        fileManagerConfig.addGroup(edit);
        fileManagerConfig.addGroup(remote);
        fileManagerConfig.addGroup(image);

        // no columns to add (default)

        // hide the mashup repository and the global repository
        fileManagerConfig.addAccordion(JCRClientUtils.WEBSITE_REPOSITORY);
        fileManagerConfig.addAccordion(JCRClientUtils.SHARED_REPOSITORY);
        fileManagerConfig.addAccordion(JCRClientUtils.MY_EXTERNAL_REPOSITORY);
        fileManagerConfig.addAccordion(JCRClientUtils.MY_REPOSITORY);
        fileManagerConfig.addAccordion(JCRClientUtils.USERS_REPOSITORY);

        // show the current site (first) tab by default

        // do not hide the left panel (default)
        fileManagerConfig.setNodeTypes(JCRClientUtils.FILE_NODETYPES);

        return fileManagerConfig;
    }

    public static ManagerConfiguration getFilePickerConfiguration(final BrowserLinker linker) {
        ManagerConfiguration filePickerConfig = new ManagerConfiguration();
        filePickerConfig.setEnableTextMenu(false);

        FileActionItemGroup file = new FileActionItemGroup(Messages.getResource("fm_fileMenu"));
        FileActionItem newFolder = ItemCreator.createNewFolderItem(linker);
        filePickerConfig.addItem(newFolder);
        file.addItem(newFolder);
        FileActionItem upload = ItemCreator.createUploadItem(linker);
        filePickerConfig.addItem(upload);
        file.addItem(upload);
        FileActionItem download = ItemCreator.createDownloadItem(linker);
        file.addItem(download);
        filePickerConfig.addItem(download);
        FileActionItem preview = ItemCreator.createPreviewItem(linker);
        file.addItem(preview);
        filePickerConfig.addItem(preview);
        filePickerConfig.addItem(new FileActionItemSeparator());

        FileActionItemGroup edit = new FileActionItemGroup(Messages.getResource("fm_editMenu"));
        FileActionItem rename = ItemCreator.createRenameItem(linker);
        filePickerConfig.addItem(rename);
        edit.addItem(rename);
        FileActionItem remove = ItemCreator.createRemoveItem(linker);
        filePickerConfig.addItem(remove);
        edit.addItem(remove);
        edit.addItem(new FileActionItemSeparator());
        filePickerConfig.addItem(new FileActionItemSeparator());
        FileActionItem copy = ItemCreator.createCopyItem(linker);
        filePickerConfig.addItem(copy);
        edit.addItem(copy);
        FileActionItem cut = ItemCreator.createCutItem(linker);
        filePickerConfig.addItem(cut);
        edit.addItem(cut);
        FileActionItem paste = ItemCreator.createPasteItem(linker);
        filePickerConfig.addItem(paste);
        edit.addItem(paste);

        // add menus to the config as well
        filePickerConfig.addGroup(file);
        filePickerConfig.addGroup(edit);

        // no columns to add (default)

        // no repository tabs

        // show the current site (first) tab by default

        // hide the left panel
        filePickerConfig.setHideLeftPanel(true);
        filePickerConfig.setNodeTypes(JCRClientUtils.FILE_NODETYPES);

        return filePickerConfig;
    }

    public static ManagerConfiguration getMashupManagerConfiguration(final BrowserLinker linker) {
        ManagerConfiguration mashupManagerConfig = new ManagerConfiguration();
        mashupManagerConfig.setEnableTextMenu(true);
        mashupManagerConfig.setEnableFileDoubleClick(false);
        mashupManagerConfig.setDisplayExt(false);
        mashupManagerConfig.setDisplaySize(false);
        mashupManagerConfig.setDefaultView(JCRClientUtils.DETAILED_THUMB_VIEW);
        FileActionItemGroup file = new FileActionItemGroup(Messages.getResource("fm_fileMenu"));
        FileActionItem newFolder = ItemCreator.createNewFolderItem(linker);
        file.addItem(newFolder);
        mashupManagerConfig.addItem(newFolder);
        FileActionItem newMashup = ItemCreator.createNewMashupItem(linker);
        file.addItem(newMashup);
        mashupManagerConfig.addItem(newMashup);
        FileActionItem newRSS = ItemCreator.createNewRSSItem(linker);
        file.addItem(newRSS);
        mashupManagerConfig.addItem(newRSS);
        FileActionItem newGadget = ItemCreator.createNewGadgetItem(linker);
        file.addItem(newGadget);
        mashupManagerConfig.addItem(newGadget);
        mashupManagerConfig.addItem(new FileActionItemSeparator());

        FileActionItemGroup edit = new FileActionItemGroup(Messages.getResource("fm_editMenu"));
        FileActionItem rename = ItemCreator.createRenameItem(linker);
        edit.addItem(rename);
        mashupManagerConfig.addItem(rename);
        FileActionItem remove = ItemCreator.createRemoveItem(linker);
        edit.addItem(remove);
        mashupManagerConfig.addItem(remove);
        edit.addItem(new FileActionItemSeparator());
        mashupManagerConfig.addItem(new FileActionItemSeparator());
        FileActionItem copy = ItemCreator.createCopyItem(linker);
        edit.addItem(copy);
        mashupManagerConfig.addItem(copy);
        FileActionItem cut = ItemCreator.createCutItem(linker);
        edit.addItem(cut);
        mashupManagerConfig.addItem(cut);
        FileActionItem paste = ItemCreator.createPasteItem(linker);
        edit.addItem(paste);
        mashupManagerConfig.addItem(paste);

        // add menus to the config as well
        mashupManagerConfig.addGroup(file);
        mashupManagerConfig.addGroup(edit);

        // no columns to add (default)

        // show only the mashup repository
        mashupManagerConfig.addAccordion(JCRClientUtils.WEBSITE_MASHUP_REPOSITORY);
        mashupManagerConfig.addAccordion(JCRClientUtils.SHARED_MASHUP_REPOSITORY);
        mashupManagerConfig.addAccordion(JCRClientUtils.MY_MASHUP_REPOSITORY);

        mashupManagerConfig.addTab(JCRClientUtils.ROLES_ACL);
        mashupManagerConfig.addTab(JCRClientUtils.MODES_ACL);
        mashupManagerConfig.addTab(JCRClientUtils.AUTHORIZATIONS_ACL);


        // show the mashup tab by default

        // do not hide the left panel (default)

        mashupManagerConfig.setNodeTypes(JCRClientUtils.PORTLET_NODETYPES);
        return mashupManagerConfig;
    }

    public static ManagerConfiguration getMashupPickerConfiguration(final BrowserLinker linker) {
        ManagerConfiguration mashupPickerConfig = new ManagerConfiguration();
        mashupPickerConfig.setEnableTextMenu(false);
        mashupPickerConfig.setEnableFileDoubleClick(false);
        mashupPickerConfig.setDisplayExt(false);
        mashupPickerConfig.setDisplaySize(false);

        mashupPickerConfig.setDefaultView(JCRClientUtils.DETAILED_THUMB_VIEW);

        FileActionItemGroup file = new FileActionItemGroup(Messages.getResource("fm_fileMenu"));
        FileActionItem newFolder = ItemCreator.createNewFolderItem(linker);
        file.addItem(newFolder);
        mashupPickerConfig.addItem(newFolder);
        FileActionItem newMashup = ItemCreator.createNewMashupItem(linker);
        file.addItem(newMashup);
        mashupPickerConfig.addItem(newMashup);
        mashupPickerConfig.addItem(new FileActionItemSeparator());
        FileActionItem newRSS = ItemCreator.createNewRSSItem(linker);
        file.addItem(newRSS);
        mashupPickerConfig.addItem(newRSS);
        FileActionItem newGadget = ItemCreator.createNewGadgetItem(linker);
        file.addItem(newGadget);
        mashupPickerConfig.addItem(newGadget);

        FileActionItemGroup edit = new FileActionItemGroup(Messages.getResource("fm_editMenu"));
        FileActionItem rename = ItemCreator.createRenameItem(linker);
        edit.addItem(rename);
        mashupPickerConfig.addItem(rename);
        FileActionItem remove = ItemCreator.createRemoveItem(linker);
        edit.addItem(remove);
        mashupPickerConfig.addItem(remove);
        edit.addItem(new FileActionItemSeparator());
        mashupPickerConfig.addItem(new FileActionItemSeparator());
        FileActionItem copy = ItemCreator.createCopyItem(linker);
        edit.addItem(copy);
        mashupPickerConfig.addItem(copy);
        FileActionItem cut = ItemCreator.createCutItem(linker);
        edit.addItem(cut);
        mashupPickerConfig.addItem(cut);
        FileActionItem paste = ItemCreator.createPasteItem(linker);
        edit.addItem(paste);
        mashupPickerConfig.addItem(paste);

        // add menus to the config as well
        mashupPickerConfig.addGroup(file);
        mashupPickerConfig.addGroup(edit);

        // only one column here : name
        mashupPickerConfig.addColumn("name");

        // no tab here
//        mashupPickerConfig.addTab(JCRClientUtils.MASHUP_REPOSITORY);

        // hide the left panel
        mashupPickerConfig.setHideLeftPanel(true);

        mashupPickerConfig.setNodeTypes(JCRClientUtils.PORTLET_NODETYPES);
        return mashupPickerConfig;
    }


    /**
     * Item creation methods
     */
    private static class ItemCreator {

        private static FileActionItem createRotateItem(final BrowserLinker linker) {
            FileActionItem rotate = new FileActionItem(Messages.getResource("fm_rotate"), "fm-rotate") {
                public void onSelection() {
                    FileActions.rotateImage(linker);
                }

                public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean isZip, boolean isImage, boolean isMount) {
                    setEnabled(tableSelection && parentWritable && singleFile && isImage);
                }
            };
            return rotate;
        }

        private static FileActionItem createCropItem(final BrowserLinker linker) {
            FileActionItem crop = new FileActionItem(Messages.getResource("fm_crop"), "fm-crop") {
                public void onSelection() {
                    FileActions.cropImage(linker);
                }

                public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean isZip, boolean isImage, boolean isMount) {
                    setEnabled(tableSelection && parentWritable && singleFile && isImage);
                }
            };
            return crop;
        }

        private static FileActionItem createUnzipItem(final BrowserLinker linker) {
            FileActionItem unzip = new FileActionItem(Messages.getResource("fm_unzip"), "fm-unzip") {
                public void onSelection() {
                    FileActions.unzip(linker);
                }

                public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean isZip, boolean isImage, boolean isMount) {
                    setEnabled(tableSelection && parentWritable && singleFile && isZip);
                }
            };
            return unzip;
        }

        private static FileActionItem createUnlockItem(final BrowserLinker linker) {
            FileActionItem unlock = new FileActionItem(Messages.getResource("fm_unlock"), "fm-unlock") {
                public void onSelection() {
                    FileActions.lock(false, linker);
                }

                public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean isZip, boolean isImage, boolean isMount) {
                    setEnabled(tableSelection && lockable && writable);
                }
            };
            return unlock;
        }

        private static FileActionItem createWebfolderItem(final BrowserLinker linker) {
            FileActionItem webFolder = new FileActionItem(Messages.getResource("fm_webfolder"), "fm-webfolder") {
                public void onSelection() {
                    FileActions.openWebFolder(linker);
                }

                public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean isZip, boolean isImage, boolean isMount) {
                    setEnabled(treeSelection || tableSelection && singleFolder);
                }
            };
            return webFolder;
        }

        private static FileActionItem createNewFolderItem(final BrowserLinker linker) {
            FileActionItem newFolder = new FileActionItem(Messages.getResource("fm_newdir"), "fm-newfolder") {
                public void onSelection() {
                    FileActions.createFolder(linker);
                }

                public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean isZip, boolean isImage, boolean isMount) {
                    setEnabled(treeSelection && parentWritable || tableSelection && singleFolder && writable);
                }
            };
            return newFolder;
        }

        private static FileActionItem createCutItem(final BrowserLinker linker) {
            FileActionItem cut = new FileActionItem(Messages.getResource("fm_cut"), "fm-cut") {
                public void onSelection() {
                    FileActions.cut(linker);
                }

                public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean isZip, boolean isImage, boolean isMount) {
                    setEnabled(tableSelection && writable);
                }
            };
            return cut;
        }

        private static FileActionItem createRemoveItem(final BrowserLinker linker) {
            FileActionItem remove = new FileActionItem(Messages.getResource("fm_remove"), "fm-remove") {
                public void onSelection() {
                    FileActions.remove(linker);
                }

                public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean isZip, boolean isImage, boolean isMount) {
                    setEnabled(tableSelection && writable && !isMount);
                }
            };
            return remove;
        }

        private static FileActionItem createPasteItem(final BrowserLinker linker) {
            FileActionItem paste = new FileActionItem(Messages.getResource("fm_paste"), "fm-paste") {
                public void onSelection() {
                    FileActions.paste(linker);
                }

                public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean isZip, boolean isImage, boolean isMount) {
                    setEnabled(treeSelection && parentWritable && pasteAllowed || tableSelection && writable && pasteAllowed);
                }
            };
            return paste;
        }

        private static FileActionItem createCopyItem(final BrowserLinker linker) {
            FileActionItem copy = new FileActionItem(Messages.getResource("fm_copy"), "fm-copy") {
                public void onSelection() {
                    FileActions.copy(linker);
                }

                public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean isZip, boolean isImage, boolean isMount) {
                    setEnabled(tableSelection);
                }
            };
            return copy;
        }

        private static FileActionItem createRenameItem(final BrowserLinker linker) {
            FileActionItem rename = new FileActionItem(Messages.getResource("fm_rename"), "fm-rename") {
                public void onSelection() {
                    FileActions.rename(linker);
                }

                public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean isZip, boolean isImage, boolean isMount) {
                    setEnabled(tableSelection && writable && (singleFile || singleFolder));
                }
            };
            return rename;
        }

        private static FileActionItem createResizeItem(final BrowserLinker linker) {
            FileActionItem resize = new FileActionItem(Messages.getResource("fm_resize"), "fm-resize") {
                public void onSelection() {
                    FileActions.resizeImage(linker);
                }

                public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean isZip, boolean isImage, boolean isMount) {
                    setEnabled(tableSelection && parentWritable && singleFile && isImage);
                }
            };
            return resize;
        }

        private static FileActionItem createMountItem(final BrowserLinker linker) {
            FileActionItem mount = new FileActionItem(Messages.getResource("fm_mount"), "fm-mount") {
                public void onSelection() {
                    FileActions.mountFolder(linker);
                }

                public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean isZip, boolean isImage, boolean isMount) {
                    setEnabled("root".equals(JahiaGWTParameters.getCurrentUser())); // TODO dirty code (to refactor using server side configuration and roles)
                }
            };
            return mount;
        }

        private static FileActionItem createUnmountItem(final BrowserLinker linker) {
            FileActionItem mount = new FileActionItem(Messages.getResource("fm_unmount"), "fm-unmount") {
                public void onSelection() {
                    FileActions.unmountFolder(linker);
                }

                public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean isZip, boolean isImage, boolean isMount) {
                    setEnabled(tableSelection && writable && isMount);
                }
            };
            return mount;
        }

        private static FileActionItem createZipItem(final BrowserLinker linker) {
            FileActionItem zip = new FileActionItem(Messages.getResource("fm_zip"), "fm-zip") {
                public void onSelection() {
                    FileActions.zip(linker);
                }

                public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean isZip, boolean isImage, boolean isMount) {
                    setEnabled(tableSelection && parentWritable);
                }
            };
            return zip;
        }

        private static FileActionItem createLockItem(final BrowserLinker linker) {
            FileActionItem lock = new FileActionItem(Messages.getResource("fm_lock"), "fm-lock") {
                public void onSelection() {
                    FileActions.lock(true, linker);
                }

                public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean isZip, boolean isImage, boolean isMount) {
                    setEnabled(tableSelection && lockable && writable);
                }
            };
            return lock;
        }

        private static FileActionItem createDownloadItem(final BrowserLinker linker) {
            FileActionItem download = new FileActionItem(Messages.getResource("fm_download"), "fm-download") {
                public void onSelection() {
                    FileActions.download(linker);
                }

                public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean isZip, boolean isImage, boolean isMount) {
                    setEnabled(tableSelection && singleFile);
                }
            };
            return download;
        }

        private static FileActionItem createPreviewItem(final BrowserLinker linker) {
            FileActionItem download = new FileActionItem(Messages.getResource("fm_preview"), "fm-preview") {
                public void onSelection() {
                    FileActions.preview(linker);
                }

                public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean isZip, boolean isImage, boolean isMount) {
                    setEnabled(tableSelection && singleFile && isImage);
                }
            };
            return download;
        }

        private static FileActionItem createUploadItem(final BrowserLinker linker) {
            FileActionItem upload = new FileActionItem(Messages.getResource("fm_upload"), "fm-upload") {
                public void onSelection() {
                    FileActions.upload(linker);
                }

                public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean isZip, boolean isImage, boolean isMount) {
                    setEnabled(treeSelection && parentWritable || tableSelection && singleFolder && writable);
                }
            };
            return upload;
        }

        private static FileActionItem createNewMashupItem(final BrowserLinker linker) {
            FileActionItem newMashup = new FileActionItem(Messages.getResource("fm_newmashup"), "fm-newmashup") {
                public void onSelection() {
                    FileActions.showMashupWizard(linker);
                }

                public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean isZip, boolean isImage, boolean isMount) {
                    setEnabled(treeSelection && parentWritable || tableSelection && singleFolder && writable);
                }
            };
            return newMashup;
        }

        private static FileActionItem createNewRSSItem(final BrowserLinker linker) {
            FileActionItem newMashup = new FileActionItem(Messages.getResource("fm_newrssmashup"), "fm-newrssmashup") {
                public void onSelection() {
                    FileActions.showRSSForm(linker);
                }

                public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean isZip, boolean isImage, boolean isMount) {
                    setEnabled(treeSelection && parentWritable || tableSelection && singleFolder && writable);
                }
            };
            return newMashup;
        }

        private static FileActionItem createNewGadgetItem(final BrowserLinker linker) {
            FileActionItem newMashup = new FileActionItem(Messages.getResource("fm_newgadgetmashup"), "fm-newgooglegadgetmashup") {
                public void onSelection() {
                    FileActions.showGoogleGadgetForm(linker);
                }

                public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean isZip, boolean isImage, boolean isMount) {
                    setEnabled(treeSelection && parentWritable || tableSelection && singleFolder && writable);
                }
            };
            return newMashup;
        }

    }

}