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
package org.jahia.ajax.gwt.client.util.content.actions;

import org.jahia.ajax.gwt.client.widget.tripanel.ManagerLinker;
import org.jahia.ajax.gwt.client.widget.toolbar.action.ContentActionItem;
import org.jahia.ajax.gwt.client.widget.toolbar.action.SeparatorActionItem;
import org.jahia.ajax.gwt.client.widget.toolbar.action.ContentActionItemCreatorHelper;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.messages.Messages;
import com.extjs.gxt.ui.client.GXT;

/**
 * User: rfelden
 * Date: 7 janv. 2009 - 14:04:14
 */
public class ManagerConfigurationFactory {

    public static final String MANAGER_CONFIG = "conf";
    public static final String FILEMANAGER = "filemanager";
    public static final String MASHUPMANAGER = "mashupmanager";
    public static final String CATEGORYMANAGER = "categorymanager";
    public static final String PORTLETDEFINITIONMANAGER = "portletdefinitionmanager";
    public static final String FILEPICKER = "filepicker";
    public static final String MASHUPPICKER = "mashuppicker";
    public static final String CATEGORYPICKER = "categorypicker";
    public static final String COMPLETE = "complete";
    public static final String SITEMANAGER = "sitemanager";

    public static ManagerConfiguration getConfiguration(String config, ManagerLinker linker) {
        if (config != null) {
            if (config.contains(FILEMANAGER)) {
                return getFileManagerConfiguration(linker);
            }
            if (config.contains(MASHUPMANAGER)) {
                return getMashupManagerConfiguration(linker);
            }
            if (config.contains(CATEGORYMANAGER)) {
                return getCategoryManagerConfiguration(linker);
            }
            if (config.contains(FILEPICKER)) {
                return getFilePickerConfiguration(linker);
            }
            if (config.contains(MASHUPPICKER)) {
                return getMashupPickerConfiguration(linker);
            }
            if (config.contains(CATEGORYPICKER)) {
                return getCategoryPickerConfiguration(linker);
            }
            if (config.contains(PORTLETDEFINITIONMANAGER)) {
                return getPortletDefinitionManagerConfiguration(linker);
            }
            if (config.contains(COMPLETE)) {
                return getCompleteManagerConfiguration(linker);
            }
            if (config.contains(SITEMANAGER)) {
                return getSiteManagerConfiguration(linker);
            }
        }
        return getCompleteManagerConfiguration(linker);
    }

	public static ManagerConfiguration getCompleteManagerConfiguration(final ManagerLinker linker) {
        ManagerConfiguration completeManagerConfig = new ManagerConfiguration();
        completeManagerConfig.setEnableTextMenu(true);
        
        completeManagerConfig.setToolbarGroup("content-manager");
        
        ContentActionItemGroup file = new ContentActionItemGroup(Messages.getResource("fm_fileMenu"));
        ContentActionItem newFolder = ContentActionItemCreatorHelper.createNewFolderItem(linker);
        file.addItem(newFolder);
        completeManagerConfig.addItem(newFolder);
        ContentActionItem newContentListContent = ContentActionItemCreatorHelper.createNewContentListItem(linker);
        file.addItem(newContentListContent);
        completeManagerConfig.addItem(newContentListContent);
        ContentActionItem newContent = ContentActionItemCreatorHelper.createNewContentItem(linker);
        file.addItem(newContent);
        completeManagerConfig.addItem(newContent);
        ContentActionItem newPageContent = ContentActionItemCreatorHelper.createNewPageContentItem(linker);
        file.addItem(newPageContent);
        completeManagerConfig.addItem(newPageContent);
        ContentActionItem newMashup = ContentActionItemCreatorHelper.createNewMashupItem(linker);
        file.addItem(newMashup);
        ContentActionItem newRSS = ContentActionItemCreatorHelper.createNewRSSItem(linker);
        file.addItem(newRSS);
        ContentActionItem newGadget = ContentActionItemCreatorHelper.createNewGadgetItem(linker);
        file.addItem(newGadget);
        completeManagerConfig.addItem(newMashup);
        ContentActionItem upload = ContentActionItemCreatorHelper.createUploadItem(linker);
        file.addItem(upload);
        completeManagerConfig.addItem(upload);
        ContentActionItem download = ContentActionItemCreatorHelper.createDownloadItem(linker);
        file.addItem(download);
        completeManagerConfig.addItem(download);
        if (GXT.isIE) {
            ContentActionItem webFolder = ContentActionItemCreatorHelper.createWebfolderItem(linker);
            file.addItem(webFolder);
            completeManagerConfig.addItem(webFolder);
        }
        file.addItem(new SeparatorActionItem());
        completeManagerConfig.addItem(new SeparatorActionItem());
        ContentActionItem lock = ContentActionItemCreatorHelper.createLockItem(linker);
        file.addItem(lock);
        ContentActionItem unlock = ContentActionItemCreatorHelper.createUnlockItem(linker);
        file.addItem(unlock);
        file.addItem(new SeparatorActionItem());
        ContentActionItem zip = ContentActionItemCreatorHelper.createZipItem(linker);
        file.addItem(zip);
        ContentActionItem unzip = ContentActionItemCreatorHelper.createUnzipItem(linker);
        file.addItem(unzip);

        ContentActionItemGroup edit = new ContentActionItemGroup(Messages.getResource("fm_editMenu"));
        ContentActionItem rename = ContentActionItemCreatorHelper.createRenameItem(linker);
        edit.addItem(rename);
        completeManagerConfig.addItem(rename);
        ContentActionItem remove = ContentActionItemCreatorHelper.createRemoveItem(linker);
        edit.addItem(remove);
        completeManagerConfig.addItem(remove);
        edit.addItem(new SeparatorActionItem());
        completeManagerConfig.addItem(new SeparatorActionItem());
        ContentActionItem copy = ContentActionItemCreatorHelper.createCopyItem(linker);
        edit.addItem(copy);
        completeManagerConfig.addItem(copy);
        ContentActionItem cut = ContentActionItemCreatorHelper.createCutItem(linker);
        edit.addItem(cut);
        completeManagerConfig.addItem(cut);
        ContentActionItem paste = ContentActionItemCreatorHelper.createPasteItem(linker);
        edit.addItem(paste);
        completeManagerConfig.addItem(paste);
        ContentActionItem pasteRef = ContentActionItemCreatorHelper.createPasteReferenceItem(linker);
        edit.addItem(pasteRef);
        completeManagerConfig.addItem(pasteRef);

        ContentActionItemGroup remote = new ContentActionItemGroup(Messages.getResource("fm_remoteMenu"));
        ContentActionItem mount = ContentActionItemCreatorHelper.createMountItem(linker);
        remote.addItem(mount);
        ContentActionItem unmount = ContentActionItemCreatorHelper.createUnmountItem(linker);
        remote.addItem(unmount);

        ContentActionItemGroup image = new ContentActionItemGroup(Messages.getResource("fm_imageMenu"));
        ContentActionItem crop = ContentActionItemCreatorHelper.createCropItem(linker);
        image.addItem(crop);
        ContentActionItem resize = ContentActionItemCreatorHelper.createResizeItem(linker);
        image.addItem(resize);
        ContentActionItem rotate = ContentActionItemCreatorHelper.createRotateItem(linker);
        image.addItem(rotate);

        // add menus to the config as well
        completeManagerConfig.addGroup(file);
        completeManagerConfig.addGroup(edit);
        completeManagerConfig.addGroup(remote);
        completeManagerConfig.addGroup(image);

        // no columns to add (default)

        // show root repository
        completeManagerConfig.addAccordion(JCRClientUtils.GLOBAL_REPOSITORY);

        completeManagerConfig.setNodeTypes("");
        completeManagerConfig.setFolderTypes("");

        // show the current site (first) tab by default

        // do not hide the left panel (default)

        completeManagerConfig.addTab(JCRClientUtils.ROLES_ACL);
        completeManagerConfig.addTab(JCRClientUtils.MODES_ACL);

        return completeManagerConfig;
    }

    public static ManagerConfiguration getFileManagerConfiguration(final ManagerLinker linker) {
        ManagerConfiguration fileManagerConfig = new ManagerConfiguration();
        fileManagerConfig.setEnableTextMenu(true);
        fileManagerConfig.setDisplayProvider(true);
        
        fileManagerConfig.setToolbarGroup("document-manager");
        
        fileManagerConfig.addColumn("providerKey");
        fileManagerConfig.addColumn("ext");
        fileManagerConfig.addColumn("locked");
        fileManagerConfig.addColumn("name");
        fileManagerConfig.addColumn("path");
        fileManagerConfig.addColumn("size");
        fileManagerConfig.addColumn("lastModified");
        
        ContentActionItemGroup file = new ContentActionItemGroup(Messages.getResource("fm_fileMenu"));
        ContentActionItem newFolder = ContentActionItemCreatorHelper.createNewFolderItem(linker);
        file.addItem(newFolder);
        fileManagerConfig.addItem(newFolder);
        ContentActionItem upload = ContentActionItemCreatorHelper.createUploadItem(linker);
        file.addItem(upload);
        fileManagerConfig.addItem(upload);
        ContentActionItem download = ContentActionItemCreatorHelper.createDownloadItem(linker);
        file.addItem(download);
        fileManagerConfig.addItem(download);
        ContentActionItem preview = ContentActionItemCreatorHelper.createPreviewItem(linker);
        file.addItem(preview);
        fileManagerConfig.addItem(preview);
        if (GXT.isIE) {
            ContentActionItem webFolder = ContentActionItemCreatorHelper.createWebfolderItem(linker);
            file.addItem(webFolder);
            fileManagerConfig.addItem(webFolder);
        }
        file.addItem(new SeparatorActionItem());
        fileManagerConfig.addItem(new SeparatorActionItem());
        ContentActionItem lock = ContentActionItemCreatorHelper.createLockItem(linker);
        file.addItem(lock);
        ContentActionItem unlock = ContentActionItemCreatorHelper.createUnlockItem(linker);
        file.addItem(unlock);
        file.addItem(new SeparatorActionItem());
        ContentActionItem zip = ContentActionItemCreatorHelper.createZipItem(linker);
        file.addItem(zip);
        ContentActionItem unzip = ContentActionItemCreatorHelper.createUnzipItem(linker);
        file.addItem(unzip);

        ContentActionItemGroup edit = new ContentActionItemGroup(Messages.getResource("fm_editMenu"));
        ContentActionItem rename = ContentActionItemCreatorHelper.createRenameItem(linker);
        edit.addItem(rename);
        fileManagerConfig.addItem(rename);
        ContentActionItem remove = ContentActionItemCreatorHelper.createRemoveItem(linker);
        edit.addItem(remove);
        fileManagerConfig.addItem(remove);
        edit.addItem(new SeparatorActionItem());
        fileManagerConfig.addItem(new SeparatorActionItem());
        ContentActionItem copy = ContentActionItemCreatorHelper.createCopyItem(linker);
        edit.addItem(copy);
        fileManagerConfig.addItem(copy);
        ContentActionItem cut = ContentActionItemCreatorHelper.createCutItem(linker);
        edit.addItem(cut);
        fileManagerConfig.addItem(cut);
        ContentActionItem paste = ContentActionItemCreatorHelper.createPasteItem(linker);
        edit.addItem(paste);
        fileManagerConfig.addItem(paste);
        ContentActionItem pasteRef = ContentActionItemCreatorHelper.createPasteReferenceItem(linker);
        edit.addItem(pasteRef);
        fileManagerConfig.addItem(pasteRef);

        ContentActionItemGroup remote = new ContentActionItemGroup(Messages.getResource("fm_remoteMenu"));
        ContentActionItem mount = ContentActionItemCreatorHelper.createMountItem(linker);
        remote.addItem(mount);
        ContentActionItem unmount = ContentActionItemCreatorHelper.createUnmountItem(linker);
        remote.addItem(unmount);

        ContentActionItemGroup image = new ContentActionItemGroup(Messages.getResource("fm_imageMenu"));
        ContentActionItem crop = ContentActionItemCreatorHelper.createCropItem(linker);
        image.addItem(crop);
        ContentActionItem resize = ContentActionItemCreatorHelper.createResizeItem(linker);
        image.addItem(resize);
        ContentActionItem rotate = ContentActionItemCreatorHelper.createRotateItem(linker);
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
        fileManagerConfig.setFolderTypes(JCRClientUtils.FOLDER_NODETYPES);

        return fileManagerConfig;
    }

    public static ManagerConfiguration getFilePickerConfiguration(final ManagerLinker linker) {
        ManagerConfiguration filePickerConfig = new ManagerConfiguration();
        filePickerConfig.setEnableTextMenu(false);

        filePickerConfig.setToolbarGroup("file-picker");
        
        ContentActionItemGroup file = new ContentActionItemGroup(Messages.getResource("fm_fileMenu"));
        ContentActionItem newFolder = ContentActionItemCreatorHelper.createNewFolderItem(linker);
        filePickerConfig.addItem(newFolder);
        file.addItem(newFolder);
        ContentActionItem upload = ContentActionItemCreatorHelper.createUploadItem(linker);
        filePickerConfig.addItem(upload);
        file.addItem(upload);
        ContentActionItem download = ContentActionItemCreatorHelper.createDownloadItem(linker);
        file.addItem(download);
        filePickerConfig.addItem(download);
        ContentActionItem preview = ContentActionItemCreatorHelper.createPreviewItem(linker);
        file.addItem(preview);
        filePickerConfig.addItem(preview);
        filePickerConfig.addItem(new SeparatorActionItem());

        ContentActionItemGroup edit = new ContentActionItemGroup(Messages.getResource("fm_editMenu"));
        ContentActionItem rename = ContentActionItemCreatorHelper.createRenameItem(linker);
        filePickerConfig.addItem(rename);
        edit.addItem(rename);
        ContentActionItem remove = ContentActionItemCreatorHelper.createRemoveItem(linker);
        filePickerConfig.addItem(remove);
        edit.addItem(remove);
        edit.addItem(new SeparatorActionItem());
        filePickerConfig.addItem(new SeparatorActionItem());
        ContentActionItem copy = ContentActionItemCreatorHelper.createCopyItem(linker);
        filePickerConfig.addItem(copy);
        edit.addItem(copy);
        ContentActionItem cut = ContentActionItemCreatorHelper.createCutItem(linker);
        filePickerConfig.addItem(cut);
        edit.addItem(cut);
        ContentActionItem paste = ContentActionItemCreatorHelper.createPasteItem(linker);
        edit.addItem(paste);
        filePickerConfig.addItem(paste);
        ContentActionItem pasteRef = ContentActionItemCreatorHelper.createPasteReferenceItem(linker);
        edit.addItem(pasteRef);
        filePickerConfig.addItem(pasteRef);

        ContentActionItemGroup image = new ContentActionItemGroup(Messages.getResource("fm_imageMenu"));
        ContentActionItem crop = ContentActionItemCreatorHelper.createCropItem(linker);
        image.addItem(crop);
        ContentActionItem resize = ContentActionItemCreatorHelper.createResizeItem(linker);
        image.addItem(resize);
        ContentActionItem rotate = ContentActionItemCreatorHelper.createRotateItem(linker);
        image.addItem(rotate);

        // add menus to the config as well
        filePickerConfig.addGroup(file);
        filePickerConfig.addGroup(edit);
        filePickerConfig.addGroup(image);

        // no columns to add (default)

        // no repository tabs

        // show the current site (first) tab by default

        // hide the left panel
        filePickerConfig.setHideLeftPanel(true);
        filePickerConfig.setNodeTypes(JCRClientUtils.FILE_NODETYPES);
        filePickerConfig.setFolderTypes(JCRClientUtils.FOLDER_NODETYPES);

        return filePickerConfig;
    }

    public static ManagerConfiguration getMashupManagerConfiguration(final ManagerLinker linker) {
        ManagerConfiguration mashupManagerConfig = new ManagerConfiguration();
        mashupManagerConfig.setEnableTextMenu(true);
        mashupManagerConfig.setEnableFileDoubleClick(false);
        mashupManagerConfig.setDisplayExt(false);
        mashupManagerConfig.setDisplaySize(false);
        
        mashupManagerConfig.setToolbarGroup("mashup-manager");
        
        mashupManagerConfig.addColumn("locked");
        mashupManagerConfig.addColumn("name");
        mashupManagerConfig.addColumn("path");
        mashupManagerConfig.addColumn("lastModified");
        
        mashupManagerConfig.setDefaultView(JCRClientUtils.DETAILED_THUMB_VIEW);
        ContentActionItemGroup file = new ContentActionItemGroup(Messages.getResource("fm_fileMenu"));
        ContentActionItem newFolder = ContentActionItemCreatorHelper.createNewFolderItem(linker);
        file.addItem(newFolder);
        mashupManagerConfig.addItem(newFolder);
        ContentActionItem newMashup = ContentActionItemCreatorHelper.createNewMashupItem(linker);
        file.addItem(newMashup);
        mashupManagerConfig.addItem(newMashup);
        ContentActionItem newRSS = ContentActionItemCreatorHelper.createNewRSSItem(linker);
        file.addItem(newRSS);
        mashupManagerConfig.addItem(newRSS);
        ContentActionItem newGadget = ContentActionItemCreatorHelper.createNewGadgetItem(linker);
        file.addItem(newGadget);
        mashupManagerConfig.addItem(newGadget);
        mashupManagerConfig.addItem(new SeparatorActionItem());

        ContentActionItemGroup edit = new ContentActionItemGroup(Messages.getResource("fm_editMenu"));
        ContentActionItem rename = ContentActionItemCreatorHelper.createRenameItem(linker);
        edit.addItem(rename);
        mashupManagerConfig.addItem(rename);
        ContentActionItem remove = ContentActionItemCreatorHelper.createRemoveItem(linker);
        edit.addItem(remove);
        mashupManagerConfig.addItem(remove);
        edit.addItem(new SeparatorActionItem());
        mashupManagerConfig.addItem(new SeparatorActionItem());
        ContentActionItem copy = ContentActionItemCreatorHelper.createCopyItem(linker);
        edit.addItem(copy);
        mashupManagerConfig.addItem(copy);
        ContentActionItem cut = ContentActionItemCreatorHelper.createCutItem(linker);
        edit.addItem(cut);
        mashupManagerConfig.addItem(cut);
        ContentActionItem paste = ContentActionItemCreatorHelper.createPasteItem(linker);
        edit.addItem(paste);
        mashupManagerConfig.addItem(paste);
        ContentActionItem pasteRef = ContentActionItemCreatorHelper.createPasteReferenceItem(linker);
        edit.addItem(pasteRef);
        mashupManagerConfig.addItem(pasteRef);

        // add menus to the config as well
        mashupManagerConfig.addGroup(file);
        mashupManagerConfig.addGroup(edit);

        // show only the mashup repository
        mashupManagerConfig.addAccordion(JCRClientUtils.WEBSITE_MASHUP_REPOSITORY);
        mashupManagerConfig.addAccordion(JCRClientUtils.SHARED_MASHUP_REPOSITORY);
        mashupManagerConfig.addAccordion(JCRClientUtils.MY_MASHUP_REPOSITORY);

        mashupManagerConfig.addTab(JCRClientUtils.ROLES_ACL);
        mashupManagerConfig.addTab(JCRClientUtils.MODES_ACL);


        // show the mashup tab by default

        // do not hide the left panel (default)

        mashupManagerConfig.setNodeTypes(JCRClientUtils.PORTLET_NODETYPES);
        mashupManagerConfig.setFolderTypes(JCRClientUtils.FOLDER_NODETYPES);

        return mashupManagerConfig;
    }

    public static ManagerConfiguration getMashupPickerConfiguration(final ManagerLinker linker) {
        ManagerConfiguration mashupPickerConfig = new ManagerConfiguration();
        mashupPickerConfig.setEnableTextMenu(false);
        mashupPickerConfig.setEnableFileDoubleClick(false);
        mashupPickerConfig.setDisplayExt(false);
        mashupPickerConfig.setDisplaySize(false);

        mashupPickerConfig.setToolbarGroup("mashup-picker");
        
        // only one column here : name
        mashupPickerConfig.addColumn("name");

        mashupPickerConfig.setDefaultView(JCRClientUtils.DETAILED_THUMB_VIEW);

        ContentActionItemGroup file = new ContentActionItemGroup(Messages.getResource("fm_fileMenu"));
        ContentActionItem newFolder = ContentActionItemCreatorHelper.createNewFolderItem(linker);
        file.addItem(newFolder);
        mashupPickerConfig.addItem(newFolder);
        ContentActionItem newMashup = ContentActionItemCreatorHelper.createNewMashupItem(linker);
        file.addItem(newMashup);
        mashupPickerConfig.addItem(newMashup);
        mashupPickerConfig.addItem(new SeparatorActionItem());
        ContentActionItem newRSS = ContentActionItemCreatorHelper.createNewRSSItem(linker);
        file.addItem(newRSS);
        mashupPickerConfig.addItem(newRSS);
        ContentActionItem newGadget = ContentActionItemCreatorHelper.createNewGadgetItem(linker);
        file.addItem(newGadget);
        mashupPickerConfig.addItem(newGadget);

        ContentActionItemGroup edit = new ContentActionItemGroup(Messages.getResource("fm_editMenu"));
        ContentActionItem rename = ContentActionItemCreatorHelper.createRenameItem(linker);
        edit.addItem(rename);
        mashupPickerConfig.addItem(rename);
        ContentActionItem remove = ContentActionItemCreatorHelper.createRemoveItem(linker);
        edit.addItem(remove);
        mashupPickerConfig.addItem(remove);
        edit.addItem(new SeparatorActionItem());
        mashupPickerConfig.addItem(new SeparatorActionItem());
        ContentActionItem copy = ContentActionItemCreatorHelper.createCopyItem(linker);
        edit.addItem(copy);
        mashupPickerConfig.addItem(copy);
        ContentActionItem cut = ContentActionItemCreatorHelper.createCutItem(linker);
        edit.addItem(cut);
        mashupPickerConfig.addItem(cut);
        ContentActionItem paste = ContentActionItemCreatorHelper.createPasteItem(linker);
        edit.addItem(paste);
        mashupPickerConfig.addItem(paste);
        ContentActionItem pasteRef = ContentActionItemCreatorHelper.createPasteReferenceItem(linker);
        edit.addItem(pasteRef);
        mashupPickerConfig.addItem(pasteRef);
        // add menus to the config as well
        mashupPickerConfig.addGroup(file);
        mashupPickerConfig.addGroup(edit);

        // no tab here
//        mashupPickerConfig.addTab(JCRClientUtils.MASHUP_REPOSITORY);

        // hide the left panel
        mashupPickerConfig.setHideLeftPanel(true);

        mashupPickerConfig.setNodeTypes(JCRClientUtils.PORTLET_NODETYPES);
        mashupPickerConfig.setFolderTypes(JCRClientUtils.FOLDER_NODETYPES);

        return mashupPickerConfig;
    }

    public static ManagerConfiguration getCategoryManagerConfiguration(final ManagerLinker linker) {
        ManagerConfiguration categoryManagerConfig = new ManagerConfiguration();
        categoryManagerConfig.setEnableTextMenu(true);
        categoryManagerConfig.setDisplayExt(false);
        categoryManagerConfig.setDisplaySize(false);
        categoryManagerConfig.setDisplayDate(false);
        
        categoryManagerConfig.setToolbarGroup("category-manager");
        
        categoryManagerConfig.addColumn("locked");
        categoryManagerConfig.addColumn("name");
        categoryManagerConfig.addColumn("path");
        
        categoryManagerConfig.setDefaultView(JCRClientUtils.FILE_TABLE);
        ContentActionItemGroup file = new ContentActionItemGroup(Messages.getResource("fm_fileMenu"));
        ContentActionItem newCategory = ContentActionItemCreatorHelper.createNewCategoryItem(linker);
        file.addItem(newCategory);
        categoryManagerConfig.addItem(newCategory);
        ContentActionItem exportItem = ContentActionItemCreatorHelper.createExportItem(linker);
        file.addItem(exportItem);
        categoryManagerConfig.addItem(exportItem);
        ContentActionItem importItem = ContentActionItemCreatorHelper.createImportItem(linker);
        file.addItem(importItem);
        categoryManagerConfig.addItem(importItem);
        categoryManagerConfig.addItem(new SeparatorActionItem());

        ContentActionItemGroup edit = new ContentActionItemGroup(Messages.getResource("fm_editMenu"));
        ContentActionItem rename = ContentActionItemCreatorHelper.createRenameItem(linker);
        edit.addItem(rename);
        categoryManagerConfig.addItem(rename);
        ContentActionItem remove = ContentActionItemCreatorHelper.createRemoveItem(linker);
        edit.addItem(remove);
        categoryManagerConfig.addItem(remove);
        edit.addItem(new SeparatorActionItem());
        categoryManagerConfig.addItem(new SeparatorActionItem());
        ContentActionItem copy = ContentActionItemCreatorHelper.createCopyItem(linker);
        edit.addItem(copy);
        categoryManagerConfig.addItem(copy);
        ContentActionItem cut = ContentActionItemCreatorHelper.createCutItem(linker);
        edit.addItem(cut);
        categoryManagerConfig.addItem(cut);
        ContentActionItem paste = ContentActionItemCreatorHelper.createPasteItem(linker);
        edit.addItem(paste);
        categoryManagerConfig.addItem(paste);
        ContentActionItem pasteRef = ContentActionItemCreatorHelper.createPasteReferenceItem(linker);
        edit.addItem(pasteRef);
        categoryManagerConfig.addItem(pasteRef);

        // add menus to the config as well
        categoryManagerConfig.addGroup(file);
        categoryManagerConfig.addGroup(edit);

        categoryManagerConfig.addAccordion(JCRClientUtils.CATEGORY_REPOSITORY);

        categoryManagerConfig.setNodeTypes(JCRClientUtils.CATEGORY_NODETYPES);
        categoryManagerConfig.setFolderTypes(JCRClientUtils.CATEGORY_NODETYPES);

        return categoryManagerConfig;
    }

    public static ManagerConfiguration getCategoryPickerConfiguration(final ManagerLinker linker) {
        ManagerConfiguration categoryPickerConfig = new ManagerConfiguration();
        categoryPickerConfig.setEnableTextMenu(false);
        categoryPickerConfig.setEnableFileDoubleClick(false);
        categoryPickerConfig.setDisplayExt(false);
        categoryPickerConfig.setDisplaySize(false);

        categoryPickerConfig.setToolbarGroup("category-picker");
        
        // only one column here : name
        categoryPickerConfig.addColumn("name");

        categoryPickerConfig.setDefaultView(JCRClientUtils.DETAILED_THUMB_VIEW);

        ContentActionItemGroup file = new ContentActionItemGroup(Messages.getResource("fm_fileMenu"));
        ContentActionItem newCategory = ContentActionItemCreatorHelper.createNewCategoryItem(linker);
        file.addItem(newCategory);
        categoryPickerConfig.addItem(newCategory);

        ContentActionItemGroup edit = new ContentActionItemGroup(Messages.getResource("fm_editMenu"));
        ContentActionItem rename = ContentActionItemCreatorHelper.createRenameItem(linker);
        edit.addItem(rename);
        categoryPickerConfig.addItem(rename);
        ContentActionItem remove = ContentActionItemCreatorHelper.createRemoveItem(linker);
        edit.addItem(remove);
        categoryPickerConfig.addItem(remove);
        edit.addItem(new SeparatorActionItem());
        categoryPickerConfig.addItem(new SeparatorActionItem());
        ContentActionItem copy = ContentActionItemCreatorHelper.createCopyItem(linker);
        edit.addItem(copy);
        categoryPickerConfig.addItem(copy);
        ContentActionItem cut = ContentActionItemCreatorHelper.createCutItem(linker);
        edit.addItem(cut);
        categoryPickerConfig.addItem(cut);
        ContentActionItem paste = ContentActionItemCreatorHelper.createPasteItem(linker);
        edit.addItem(paste);
        categoryPickerConfig.addItem(paste);
        ContentActionItem pasteRef = ContentActionItemCreatorHelper.createPasteReferenceItem(linker);
        edit.addItem(pasteRef);
        categoryPickerConfig.addItem(pasteRef);
        // add menus to the config as well
        categoryPickerConfig.addGroup(file);
        categoryPickerConfig.addGroup(edit);

        ContentActionItem exportItem = ContentActionItemCreatorHelper.createExportItem(linker);
        file.addItem(exportItem);
        categoryPickerConfig.addItem(exportItem);
        ContentActionItem importItem = ContentActionItemCreatorHelper.createImportItem(linker);
        file.addItem(importItem);
        categoryPickerConfig.addItem(importItem);


        // hide the left panel
        categoryPickerConfig.setHideLeftPanel(true);

        categoryPickerConfig.setNodeTypes(JCRClientUtils.CATEGORY_NODETYPES);
        categoryPickerConfig.setFolderTypes(JCRClientUtils.CATEGORY_NODETYPES);

        return categoryPickerConfig;
    }

    public static ManagerConfiguration getPortletDefinitionManagerConfiguration(final ManagerLinker linker) {
        ManagerConfiguration portletDefinitionManagerConf = new ManagerConfiguration();

        portletDefinitionManagerConf.setEnableTextMenu(false);
        portletDefinitionManagerConf.setEnableFileDoubleClick(false);
        portletDefinitionManagerConf.setDisplayExt(false);
        portletDefinitionManagerConf.setDisplaySize(false);

        portletDefinitionManagerConf.setToolbarGroup("portlet-definition-manager");
        
        // only one column here : name
        portletDefinitionManagerConf.addColumn("name");

        // file item group
        ContentActionItemGroup file = new ContentActionItemGroup(Messages.getResource("fm_fileMenu"));
        ContentActionItem deployPortletDefinition = ContentActionItemCreatorHelper.createDeployPortletDefinition(linker);
        file.addItem(deployPortletDefinition);
        portletDefinitionManagerConf.addItem(deployPortletDefinition);

        // edit item group
        ContentActionItemGroup edit = new ContentActionItemGroup(Messages.getResource("fm_editMenu"));

        // remove item
        ContentActionItem remove = ContentActionItemCreatorHelper.createRemoveItem(linker);
        edit.addItem(remove);
        portletDefinitionManagerConf.addItem(remove);
        portletDefinitionManagerConf.addItem(new SeparatorActionItem());


        // add menus to the config as well
        portletDefinitionManagerConf.addGroup(file);
        portletDefinitionManagerConf.addGroup(edit);

        // hide the left panel
        portletDefinitionManagerConf.setHideLeftPanel(true);

        portletDefinitionManagerConf.addAccordion(JCRClientUtils.PORTLET_DEFINITIONS_REPOSITORY);

        portletDefinitionManagerConf.setNodeTypes(JCRClientUtils.PORTLET_DEFINITIONS_NODETYPES);
        portletDefinitionManagerConf.setFolderTypes(JCRClientUtils.FOLDER_NODETYPES);

        portletDefinitionManagerConf.addTab("portlets");
        
        return portletDefinitionManagerConf;
    }

    public static ManagerConfiguration getSiteManagerConfiguration(ManagerLinker linker) {
        ManagerConfiguration cfg = new ManagerConfiguration();
        cfg.setEnableTextMenu(true);
        cfg.setDisplaySize(false);
        cfg.setDisplayDate(false);
        
        cfg.setToolbarGroup("site-manager");
        
        cfg.addColumn("ext");
        cfg.addColumn("locked");
        cfg.addColumn("name");
    	cfg.addColumn("lastModified");
    	cfg.addColumn("lastModifiedBy");
        cfg.addColumn("publicationInfo");
        
        cfg.setDefaultView(JCRClientUtils.FILE_TABLE);
        
        ContentActionItemGroup file = new ContentActionItemGroup(Messages.getResource("fm_fileMenu"));
        ContentActionItem newPage = ContentActionItemCreatorHelper.createNewPageContentItem(linker);
        file.addItem(newPage);
        cfg.addItem(newPage);
        
        file.addItem(new SeparatorActionItem());
        file.addItem(ContentActionItemCreatorHelper.createLockItem(linker));
        file.addItem(ContentActionItemCreatorHelper.createUnlockItem(linker));
        cfg.addItem(new SeparatorActionItem());
        
        ContentActionItemGroup edit = new ContentActionItemGroup(Messages.getResource("fm_editMenu"));
        ContentActionItem rename = ContentActionItemCreatorHelper.createRenameItem(linker);
        edit.addItem(rename);
        cfg.addItem(rename);
        ContentActionItem remove = ContentActionItemCreatorHelper.createRemoveItem(linker);
        edit.addItem(remove);
        cfg.addItem(remove);
        edit.addItem(new SeparatorActionItem());
        cfg.addItem(new SeparatorActionItem());
        ContentActionItem copy = ContentActionItemCreatorHelper.createCopyItem(linker);
        edit.addItem(copy);
        cfg.addItem(copy);
        ContentActionItem cut = ContentActionItemCreatorHelper.createCutItem(linker);
        edit.addItem(cut);
        cfg.addItem(cut);
        ContentActionItem paste = ContentActionItemCreatorHelper.createPasteItem(linker);
        edit.addItem(paste);
        cfg.addItem(paste);
        ContentActionItem pasteRef = ContentActionItemCreatorHelper.createPasteReferenceItem(linker);
        edit.addItem(pasteRef);
        cfg.addItem(pasteRef);

        // add menus to the config as well
        cfg.addGroup(file);
        cfg.addGroup(edit);

        cfg.addAccordion(JCRClientUtils.SITE_REPOSITORY);

        cfg.setNodeTypes(JCRClientUtils.SITE_NODETYPES);
        cfg.setFolderTypes(JCRClientUtils.SITE_NODETYPES);
        
        // do not display collections, if they do not match node type filters
        cfg.setAllowCollections(false);

        return cfg;

    }

}