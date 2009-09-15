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

import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
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

    public static ManagerConfiguration getConfiguration(String config, BrowserLinker linker) {
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

	public static ManagerConfiguration getCompleteManagerConfiguration(final BrowserLinker linker) {
        ManagerConfiguration completeManagerConfig = new ManagerConfiguration();
        completeManagerConfig.setEnableTextMenu(true);

        ContentActionItemGroup file = new ContentActionItemGroup(Messages.getResource("fm_fileMenu"));
        ContentActionItem newFolder = ItemCreator.createNewFolderItem(linker);
        file.addItem(newFolder);
        completeManagerConfig.addItem(newFolder);
        ContentActionItem newContentListContent = ItemCreator.createNewContentListItem(linker);
        file.addItem(newContentListContent);
        completeManagerConfig.addItem(newContentListContent);
        ContentActionItem newContent = ItemCreator.createNewContentItem(linker);
        file.addItem(newContent);
        completeManagerConfig.addItem(newContent);
        ContentActionItem newPageContent = ItemCreator.createNewPageContentItem(linker);
        file.addItem(newPageContent);
        completeManagerConfig.addItem(newPageContent);
        ContentActionItem newMashup = ItemCreator.createNewMashupItem(linker);
        file.addItem(newMashup);
        ContentActionItem newRSS = ItemCreator.createNewRSSItem(linker);
        file.addItem(newRSS);
        ContentActionItem newGadget = ItemCreator.createNewGadgetItem(linker);
        file.addItem(newGadget);
        completeManagerConfig.addItem(newMashup);
        ContentActionItem upload = ItemCreator.createUploadItem(linker);
        file.addItem(upload);
        completeManagerConfig.addItem(upload);
        ContentActionItem download = ItemCreator.createDownloadItem(linker);
        file.addItem(download);
        completeManagerConfig.addItem(download);
        if (GXT.isIE) {
            ContentActionItem webFolder = ItemCreator.createWebfolderItem(linker);
            file.addItem(webFolder);
            completeManagerConfig.addItem(webFolder);
        }
        file.addItem(new ContentActionItemSeparator());
        completeManagerConfig.addItem(new ContentActionItemSeparator());
        ContentActionItem lock = ItemCreator.createLockItem(linker);
        file.addItem(lock);
        ContentActionItem unlock = ItemCreator.createUnlockItem(linker);
        file.addItem(unlock);
        file.addItem(new ContentActionItemSeparator());
        ContentActionItem zip = ItemCreator.createZipItem(linker);
        file.addItem(zip);
        ContentActionItem unzip = ItemCreator.createUnzipItem(linker);
        file.addItem(unzip);

        ContentActionItemGroup edit = new ContentActionItemGroup(Messages.getResource("fm_editMenu"));
        ContentActionItem rename = ItemCreator.createRenameItem(linker);
        edit.addItem(rename);
        completeManagerConfig.addItem(rename);
        ContentActionItem remove = ItemCreator.createRemoveItem(linker);
        edit.addItem(remove);
        completeManagerConfig.addItem(remove);
        edit.addItem(new ContentActionItemSeparator());
        completeManagerConfig.addItem(new ContentActionItemSeparator());
        ContentActionItem copy = ItemCreator.createCopyItem(linker);
        edit.addItem(copy);
        completeManagerConfig.addItem(copy);
        ContentActionItem cut = ItemCreator.createCutItem(linker);
        edit.addItem(cut);
        completeManagerConfig.addItem(cut);
        ContentActionItem paste = ItemCreator.createPasteItem(linker);
        edit.addItem(paste);
        completeManagerConfig.addItem(paste);
        ContentActionItem pasteRef = ItemCreator.createPasteReferenceItem(linker);
        edit.addItem(pasteRef);
        completeManagerConfig.addItem(pasteRef);

        ContentActionItemGroup remote = new ContentActionItemGroup(Messages.getResource("fm_remoteMenu"));
        ContentActionItem mount = ItemCreator.createMountItem(linker);
        remote.addItem(mount);
        ContentActionItem unmount = ItemCreator.createUnmountItem(linker);
        remote.addItem(unmount);

        ContentActionItemGroup image = new ContentActionItemGroup(Messages.getResource("fm_imageMenu"));
        ContentActionItem crop = ItemCreator.createCropItem(linker);
        image.addItem(crop);
        ContentActionItem resize = ItemCreator.createResizeItem(linker);
        image.addItem(resize);
        ContentActionItem rotate = ItemCreator.createRotateItem(linker);
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

    public static ManagerConfiguration getFileManagerConfiguration(final BrowserLinker linker) {
        ManagerConfiguration fileManagerConfig = new ManagerConfiguration();
        fileManagerConfig.setEnableTextMenu(true);
        fileManagerConfig.setDisplayProvider(true);
        ContentActionItemGroup file = new ContentActionItemGroup(Messages.getResource("fm_fileMenu"));
        ContentActionItem newFolder = ItemCreator.createNewFolderItem(linker);
        file.addItem(newFolder);
        fileManagerConfig.addItem(newFolder);
        ContentActionItem upload = ItemCreator.createUploadItem(linker);
        file.addItem(upload);
        fileManagerConfig.addItem(upload);
        ContentActionItem download = ItemCreator.createDownloadItem(linker);
        file.addItem(download);
        fileManagerConfig.addItem(download);
        ContentActionItem preview = ItemCreator.createPreviewItem(linker);
        file.addItem(preview);
        fileManagerConfig.addItem(preview);
        if (GXT.isIE) {
            ContentActionItem webFolder = ItemCreator.createWebfolderItem(linker);
            file.addItem(webFolder);
            fileManagerConfig.addItem(webFolder);
        }
        file.addItem(new ContentActionItemSeparator());
        fileManagerConfig.addItem(new ContentActionItemSeparator());
        ContentActionItem lock = ItemCreator.createLockItem(linker);
        file.addItem(lock);
        ContentActionItem unlock = ItemCreator.createUnlockItem(linker);
        file.addItem(unlock);
        file.addItem(new ContentActionItemSeparator());
        ContentActionItem zip = ItemCreator.createZipItem(linker);
        file.addItem(zip);
        ContentActionItem unzip = ItemCreator.createUnzipItem(linker);
        file.addItem(unzip);

        ContentActionItemGroup edit = new ContentActionItemGroup(Messages.getResource("fm_editMenu"));
        ContentActionItem rename = ItemCreator.createRenameItem(linker);
        edit.addItem(rename);
        fileManagerConfig.addItem(rename);
        ContentActionItem remove = ItemCreator.createRemoveItem(linker);
        edit.addItem(remove);
        fileManagerConfig.addItem(remove);
        edit.addItem(new ContentActionItemSeparator());
        fileManagerConfig.addItem(new ContentActionItemSeparator());
        ContentActionItem copy = ItemCreator.createCopyItem(linker);
        edit.addItem(copy);
        fileManagerConfig.addItem(copy);
        ContentActionItem cut = ItemCreator.createCutItem(linker);
        edit.addItem(cut);
        fileManagerConfig.addItem(cut);
        ContentActionItem paste = ItemCreator.createPasteItem(linker);
        edit.addItem(paste);
        fileManagerConfig.addItem(paste);
        ContentActionItem pasteRef = ItemCreator.createPasteReferenceItem(linker);
        edit.addItem(pasteRef);
        fileManagerConfig.addItem(pasteRef);

        ContentActionItemGroup remote = new ContentActionItemGroup(Messages.getResource("fm_remoteMenu"));
        ContentActionItem mount = ItemCreator.createMountItem(linker);
        remote.addItem(mount);
        ContentActionItem unmount = ItemCreator.createUnmountItem(linker);
        remote.addItem(unmount);

        ContentActionItemGroup image = new ContentActionItemGroup(Messages.getResource("fm_imageMenu"));
        ContentActionItem crop = ItemCreator.createCropItem(linker);
        image.addItem(crop);
        ContentActionItem resize = ItemCreator.createResizeItem(linker);
        image.addItem(resize);
        ContentActionItem rotate = ItemCreator.createRotateItem(linker);
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

    public static ManagerConfiguration getFilePickerConfiguration(final BrowserLinker linker) {
        ManagerConfiguration filePickerConfig = new ManagerConfiguration();
        filePickerConfig.setEnableTextMenu(false);

        ContentActionItemGroup file = new ContentActionItemGroup(Messages.getResource("fm_fileMenu"));
        ContentActionItem newFolder = ItemCreator.createNewFolderItem(linker);
        filePickerConfig.addItem(newFolder);
        file.addItem(newFolder);
        ContentActionItem upload = ItemCreator.createUploadItem(linker);
        filePickerConfig.addItem(upload);
        file.addItem(upload);
        ContentActionItem download = ItemCreator.createDownloadItem(linker);
        file.addItem(download);
        filePickerConfig.addItem(download);
        ContentActionItem preview = ItemCreator.createPreviewItem(linker);
        file.addItem(preview);
        filePickerConfig.addItem(preview);
        filePickerConfig.addItem(new ContentActionItemSeparator());

        ContentActionItemGroup edit = new ContentActionItemGroup(Messages.getResource("fm_editMenu"));
        ContentActionItem rename = ItemCreator.createRenameItem(linker);
        filePickerConfig.addItem(rename);
        edit.addItem(rename);
        ContentActionItem remove = ItemCreator.createRemoveItem(linker);
        filePickerConfig.addItem(remove);
        edit.addItem(remove);
        edit.addItem(new ContentActionItemSeparator());
        filePickerConfig.addItem(new ContentActionItemSeparator());
        ContentActionItem copy = ItemCreator.createCopyItem(linker);
        filePickerConfig.addItem(copy);
        edit.addItem(copy);
        ContentActionItem cut = ItemCreator.createCutItem(linker);
        filePickerConfig.addItem(cut);
        edit.addItem(cut);
        ContentActionItem paste = ItemCreator.createPasteItem(linker);
        edit.addItem(paste);
        filePickerConfig.addItem(paste);
        ContentActionItem pasteRef = ItemCreator.createPasteReferenceItem(linker);
        edit.addItem(pasteRef);
        filePickerConfig.addItem(pasteRef);

        ContentActionItemGroup image = new ContentActionItemGroup(Messages.getResource("fm_imageMenu"));
        ContentActionItem crop = ItemCreator.createCropItem(linker);
        image.addItem(crop);
        ContentActionItem resize = ItemCreator.createResizeItem(linker);
        image.addItem(resize);
        ContentActionItem rotate = ItemCreator.createRotateItem(linker);
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

    public static ManagerConfiguration getMashupManagerConfiguration(final BrowserLinker linker) {
        ManagerConfiguration mashupManagerConfig = new ManagerConfiguration();
        mashupManagerConfig.setEnableTextMenu(true);
        mashupManagerConfig.setEnableFileDoubleClick(false);
        mashupManagerConfig.setDisplayExt(false);
        mashupManagerConfig.setDisplaySize(false);
        mashupManagerConfig.setDefaultView(JCRClientUtils.DETAILED_THUMB_VIEW);
        ContentActionItemGroup file = new ContentActionItemGroup(Messages.getResource("fm_fileMenu"));
        ContentActionItem newFolder = ItemCreator.createNewFolderItem(linker);
        file.addItem(newFolder);
        mashupManagerConfig.addItem(newFolder);
        ContentActionItem newMashup = ItemCreator.createNewMashupItem(linker);
        file.addItem(newMashup);
        mashupManagerConfig.addItem(newMashup);
        ContentActionItem newRSS = ItemCreator.createNewRSSItem(linker);
        file.addItem(newRSS);
        mashupManagerConfig.addItem(newRSS);
        ContentActionItem newGadget = ItemCreator.createNewGadgetItem(linker);
        file.addItem(newGadget);
        mashupManagerConfig.addItem(newGadget);
        mashupManagerConfig.addItem(new ContentActionItemSeparator());

        ContentActionItemGroup edit = new ContentActionItemGroup(Messages.getResource("fm_editMenu"));
        ContentActionItem rename = ItemCreator.createRenameItem(linker);
        edit.addItem(rename);
        mashupManagerConfig.addItem(rename);
        ContentActionItem remove = ItemCreator.createRemoveItem(linker);
        edit.addItem(remove);
        mashupManagerConfig.addItem(remove);
        edit.addItem(new ContentActionItemSeparator());
        mashupManagerConfig.addItem(new ContentActionItemSeparator());
        ContentActionItem copy = ItemCreator.createCopyItem(linker);
        edit.addItem(copy);
        mashupManagerConfig.addItem(copy);
        ContentActionItem cut = ItemCreator.createCutItem(linker);
        edit.addItem(cut);
        mashupManagerConfig.addItem(cut);
        ContentActionItem paste = ItemCreator.createPasteItem(linker);
        edit.addItem(paste);
        mashupManagerConfig.addItem(paste);
        ContentActionItem pasteRef = ItemCreator.createPasteReferenceItem(linker);
        edit.addItem(pasteRef);
        mashupManagerConfig.addItem(pasteRef);

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


        // show the mashup tab by default

        // do not hide the left panel (default)

        mashupManagerConfig.setNodeTypes(JCRClientUtils.PORTLET_NODETYPES);
        mashupManagerConfig.setFolderTypes(JCRClientUtils.FOLDER_NODETYPES);

        return mashupManagerConfig;
    }

    public static ManagerConfiguration getMashupPickerConfiguration(final BrowserLinker linker) {
        ManagerConfiguration mashupPickerConfig = new ManagerConfiguration();
        mashupPickerConfig.setEnableTextMenu(false);
        mashupPickerConfig.setEnableFileDoubleClick(false);
        mashupPickerConfig.setDisplayExt(false);
        mashupPickerConfig.setDisplaySize(false);

        mashupPickerConfig.setDefaultView(JCRClientUtils.DETAILED_THUMB_VIEW);

        ContentActionItemGroup file = new ContentActionItemGroup(Messages.getResource("fm_fileMenu"));
        ContentActionItem newFolder = ItemCreator.createNewFolderItem(linker);
        file.addItem(newFolder);
        mashupPickerConfig.addItem(newFolder);
        ContentActionItem newMashup = ItemCreator.createNewMashupItem(linker);
        file.addItem(newMashup);
        mashupPickerConfig.addItem(newMashup);
        mashupPickerConfig.addItem(new ContentActionItemSeparator());
        ContentActionItem newRSS = ItemCreator.createNewRSSItem(linker);
        file.addItem(newRSS);
        mashupPickerConfig.addItem(newRSS);
        ContentActionItem newGadget = ItemCreator.createNewGadgetItem(linker);
        file.addItem(newGadget);
        mashupPickerConfig.addItem(newGadget);

        ContentActionItemGroup edit = new ContentActionItemGroup(Messages.getResource("fm_editMenu"));
        ContentActionItem rename = ItemCreator.createRenameItem(linker);
        edit.addItem(rename);
        mashupPickerConfig.addItem(rename);
        ContentActionItem remove = ItemCreator.createRemoveItem(linker);
        edit.addItem(remove);
        mashupPickerConfig.addItem(remove);
        edit.addItem(new ContentActionItemSeparator());
        mashupPickerConfig.addItem(new ContentActionItemSeparator());
        ContentActionItem copy = ItemCreator.createCopyItem(linker);
        edit.addItem(copy);
        mashupPickerConfig.addItem(copy);
        ContentActionItem cut = ItemCreator.createCutItem(linker);
        edit.addItem(cut);
        mashupPickerConfig.addItem(cut);
        ContentActionItem paste = ItemCreator.createPasteItem(linker);
        edit.addItem(paste);
        mashupPickerConfig.addItem(paste);
        ContentActionItem pasteRef = ItemCreator.createPasteReferenceItem(linker);
        edit.addItem(pasteRef);
        mashupPickerConfig.addItem(pasteRef);
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
        mashupPickerConfig.setFolderTypes(JCRClientUtils.FOLDER_NODETYPES);

        return mashupPickerConfig;
    }

    public static ManagerConfiguration getCategoryManagerConfiguration(final BrowserLinker linker) {
        ManagerConfiguration categoryManagerConfig = new ManagerConfiguration();
        categoryManagerConfig.setEnableTextMenu(true);
        categoryManagerConfig.setDisplayExt(false);
        categoryManagerConfig.setDisplaySize(false);
        categoryManagerConfig.setDisplayDate(false);
        categoryManagerConfig.setDefaultView(JCRClientUtils.FILE_TABLE);
        ContentActionItemGroup file = new ContentActionItemGroup(Messages.getResource("fm_fileMenu"));
        ContentActionItem newCategory = ItemCreator.createNewCategoryItem(linker);
        file.addItem(newCategory);
        categoryManagerConfig.addItem(newCategory);
        ContentActionItem exportItem = ItemCreator.createExportItem(linker);
        file.addItem(exportItem);
        categoryManagerConfig.addItem(exportItem);
        ContentActionItem importItem = ItemCreator.createImportItem(linker);
        file.addItem(importItem);
        categoryManagerConfig.addItem(importItem);
        categoryManagerConfig.addItem(new ContentActionItemSeparator());

        ContentActionItemGroup edit = new ContentActionItemGroup(Messages.getResource("fm_editMenu"));
        ContentActionItem rename = ItemCreator.createRenameItem(linker);
        edit.addItem(rename);
        categoryManagerConfig.addItem(rename);
        ContentActionItem remove = ItemCreator.createRemoveItem(linker);
        edit.addItem(remove);
        categoryManagerConfig.addItem(remove);
        edit.addItem(new ContentActionItemSeparator());
        categoryManagerConfig.addItem(new ContentActionItemSeparator());
        ContentActionItem copy = ItemCreator.createCopyItem(linker);
        edit.addItem(copy);
        categoryManagerConfig.addItem(copy);
        ContentActionItem cut = ItemCreator.createCutItem(linker);
        edit.addItem(cut);
        categoryManagerConfig.addItem(cut);
        ContentActionItem paste = ItemCreator.createPasteItem(linker);
        edit.addItem(paste);
        categoryManagerConfig.addItem(paste);
        ContentActionItem pasteRef = ItemCreator.createPasteReferenceItem(linker);
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

    public static ManagerConfiguration getCategoryPickerConfiguration(final BrowserLinker linker) {
        ManagerConfiguration categoryPickerConfig = new ManagerConfiguration();
        categoryPickerConfig.setEnableTextMenu(false);
        categoryPickerConfig.setEnableFileDoubleClick(false);
        categoryPickerConfig.setDisplayExt(false);
        categoryPickerConfig.setDisplaySize(false);

        categoryPickerConfig.setDefaultView(JCRClientUtils.DETAILED_THUMB_VIEW);

        ContentActionItemGroup file = new ContentActionItemGroup(Messages.getResource("fm_fileMenu"));
        ContentActionItem newCategory = ItemCreator.createNewCategoryItem(linker);
        file.addItem(newCategory);
        categoryPickerConfig.addItem(newCategory);

        ContentActionItemGroup edit = new ContentActionItemGroup(Messages.getResource("fm_editMenu"));
        ContentActionItem rename = ItemCreator.createRenameItem(linker);
        edit.addItem(rename);
        categoryPickerConfig.addItem(rename);
        ContentActionItem remove = ItemCreator.createRemoveItem(linker);
        edit.addItem(remove);
        categoryPickerConfig.addItem(remove);
        edit.addItem(new ContentActionItemSeparator());
        categoryPickerConfig.addItem(new ContentActionItemSeparator());
        ContentActionItem copy = ItemCreator.createCopyItem(linker);
        edit.addItem(copy);
        categoryPickerConfig.addItem(copy);
        ContentActionItem cut = ItemCreator.createCutItem(linker);
        edit.addItem(cut);
        categoryPickerConfig.addItem(cut);
        ContentActionItem paste = ItemCreator.createPasteItem(linker);
        edit.addItem(paste);
        categoryPickerConfig.addItem(paste);
        ContentActionItem pasteRef = ItemCreator.createPasteReferenceItem(linker);
        edit.addItem(pasteRef);
        categoryPickerConfig.addItem(pasteRef);
        // add menus to the config as well
        categoryPickerConfig.addGroup(file);
        categoryPickerConfig.addGroup(edit);

        ContentActionItem exportItem = ItemCreator.createExportItem(linker);
        file.addItem(exportItem);
        categoryPickerConfig.addItem(exportItem);
        ContentActionItem importItem = ItemCreator.createImportItem(linker);
        file.addItem(importItem);
        categoryPickerConfig.addItem(importItem);


        // only one column here : name
        categoryPickerConfig.addColumn("name");

        // hide the left panel
        categoryPickerConfig.setHideLeftPanel(true);

        categoryPickerConfig.setNodeTypes(JCRClientUtils.CATEGORY_NODETYPES);
        categoryPickerConfig.setFolderTypes(JCRClientUtils.CATEGORY_NODETYPES);

        return categoryPickerConfig;
    }

    public static ManagerConfiguration getPortletDefinitionManagerConfiguration(final BrowserLinker linker) {
        ManagerConfiguration portletDefinitionManagerConf = new ManagerConfiguration();

        portletDefinitionManagerConf.setEnableTextMenu(false);
        portletDefinitionManagerConf.setEnableFileDoubleClick(false);
        portletDefinitionManagerConf.setDisplayExt(false);
        portletDefinitionManagerConf.setDisplaySize(false);


        // file item group
        ContentActionItemGroup file = new ContentActionItemGroup(Messages.getResource("fm_fileMenu"));
        ContentActionItem deployPortletDefinition = ItemCreator.createDeployPortletDefinition(linker);
        file.addItem(deployPortletDefinition);
        portletDefinitionManagerConf.addItem(deployPortletDefinition);

        // edit item group
        ContentActionItemGroup edit = new ContentActionItemGroup(Messages.getResource("fm_editMenu"));

        // remove item
        ContentActionItem remove = ItemCreator.createRemoveItem(linker);
        edit.addItem(remove);
        portletDefinitionManagerConf.addItem(remove);
        portletDefinitionManagerConf.addItem(new ContentActionItemSeparator());


        // add menus to the config as well
        portletDefinitionManagerConf.addGroup(file);
        portletDefinitionManagerConf.addGroup(edit);

        // only one column here : name
        portletDefinitionManagerConf.addColumn("name");

        // hide the left panel
        portletDefinitionManagerConf.setHideLeftPanel(true);

        portletDefinitionManagerConf.addAccordion(JCRClientUtils.PORTLET_DEFINITIONS_REPOSITORY);

        portletDefinitionManagerConf.setNodeTypes(JCRClientUtils.PORTLET_DEFINITIONS_NODETYPES);
        portletDefinitionManagerConf.setFolderTypes(JCRClientUtils.FOLDER_NODETYPES);

        portletDefinitionManagerConf.addTab("portlets");
        
        return portletDefinitionManagerConf;
    }

    /**
     * Item creation methods
     */
    private static class ItemCreator {

        private static ContentActionItem createRotateItem(final BrowserLinker linker) {
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

        private static ContentActionItem createCropItem(final BrowserLinker linker) {
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

        private static ContentActionItem createUnzipItem(final BrowserLinker linker) {
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

        private static ContentActionItem createUnlockItem(final BrowserLinker linker) {
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

        private static ContentActionItem createWebfolderItem(final BrowserLinker linker) {
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

        private static ContentActionItem createNewFolderItem(final BrowserLinker linker) {
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

        private static ContentActionItem createCutItem(final BrowserLinker linker) {
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

        private static ContentActionItem createRemoveItem(final BrowserLinker linker) {
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

        private static ContentActionItem createPasteItem(final BrowserLinker linker) {
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

        private static ContentActionItem createPasteReferenceItem(final BrowserLinker linker) {
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

        private static ContentActionItem createCopyItem(final BrowserLinker linker) {
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

        private static ContentActionItem createRenameItem(final BrowserLinker linker) {
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

        private static ContentActionItem createResizeItem(final BrowserLinker linker) {
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

        private static ContentActionItem createMountItem(final BrowserLinker linker) {
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

        private static ContentActionItem createUnmountItem(final BrowserLinker linker) {
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

        private static ContentActionItem createZipItem(final BrowserLinker linker) {
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

        private static ContentActionItem createLockItem(final BrowserLinker linker) {
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

        private static ContentActionItem createDownloadItem(final BrowserLinker linker) {
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

        private static ContentActionItem createPreviewItem(final BrowserLinker linker) {
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

        private static ContentActionItem createUploadItem(final BrowserLinker linker) {
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
        private static ContentActionItem createNewMashupItem(final BrowserLinker linker) {
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
        private static ContentActionItem createNewRSSItem(final BrowserLinker linker) {
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
        private static ContentActionItem createNewGadgetItem(final BrowserLinker linker) {
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
        private static ContentActionItem createNewCategoryItem(final BrowserLinker linker) {
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
        private static ContentActionItem createDeployPortletDefinition(final BrowserLinker linker) {
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
        private static ContentActionItem createNewContentItem(final BrowserLinker linker) {
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
         * Item that allows creation of a new page
         *
         * @param linker
         * @return
         */
        private static ContentActionItem createNewPageContentItem(final BrowserLinker linker) {

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
         * Item that allows creation of a new content list
         *
         * @param linker
         * @return
         */
        private static ContentActionItem createNewContentListItem(final BrowserLinker linker) {

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

        private static ContentActionItem createExportItem(final BrowserLinker linker) {
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

        private static ContentActionItem createImportItem(final BrowserLinker linker) {
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

        private static ContentActionItem createNewSiteItem(final BrowserLinker linker) {
            ContentActionItem newSite = new ContentActionItem(Messages.get("fm_newsite", "New site"), "fm-newsite") {
                public void onSelection() {
                    ContentActions.showContentWizard(linker, "jnt:virtualsite");
                }

                public void enableOnConditions(boolean treeSelection, boolean tableSelection, boolean writable, boolean deleteable, boolean parentWritable, boolean singleFile, boolean singleFolder, boolean pasteAllowed, boolean lockable, boolean locked, boolean isZip, boolean isImage, boolean isMount) {
                    setEnabled(treeSelection && parentWritable || tableSelection && singleFolder && writable);
                }
            };
            return newSite;
        }

    }

    public static ManagerConfiguration getSiteManagerConfiguration(BrowserLinker linker) {
        ManagerConfiguration cfg = new ManagerConfiguration();
        cfg.setEnableTextMenu(true);
        cfg.setDisplaySize(false);
        cfg.setDisplayDate(false);
        cfg.setDefaultView(JCRClientUtils.FILE_TABLE);
        
        ContentActionItemGroup file = new ContentActionItemGroup(Messages.getResource("fm_fileMenu"));
        ContentActionItem newFolder = ItemCreator.createNewFolderItem(linker);
        file.addItem(newFolder);
        cfg.addItem(newFolder);
        ContentActionItem newSite = ItemCreator.createNewSiteItem(linker);
        file.addItem(newSite);
        cfg.addItem(newSite);
        ContentActionItem newPage = ItemCreator.createNewPageContentItem(linker);
        file.addItem(newPage);
        cfg.addItem(newPage);
        ContentActionItem exportItem = ItemCreator.createExportItem(linker);
        file.addItem(exportItem);
        cfg.addItem(exportItem);
        ContentActionItem importItem = ItemCreator.createImportItem(linker);
        file.addItem(importItem);
        cfg.addItem(importItem);
        cfg.addItem(new ContentActionItemSeparator());

        ContentActionItemGroup edit = new ContentActionItemGroup(Messages.getResource("fm_editMenu"));
        ContentActionItem rename = ItemCreator.createRenameItem(linker);
        edit.addItem(rename);
        cfg.addItem(rename);
        ContentActionItem remove = ItemCreator.createRemoveItem(linker);
        edit.addItem(remove);
        cfg.addItem(remove);
        edit.addItem(new ContentActionItemSeparator());
        cfg.addItem(new ContentActionItemSeparator());
        ContentActionItem copy = ItemCreator.createCopyItem(linker);
        edit.addItem(copy);
        cfg.addItem(copy);
        ContentActionItem cut = ItemCreator.createCutItem(linker);
        edit.addItem(cut);
        cfg.addItem(cut);
        ContentActionItem paste = ItemCreator.createPasteItem(linker);
        edit.addItem(paste);
        cfg.addItem(paste);
        ContentActionItem pasteRef = ItemCreator.createPasteReferenceItem(linker);
        edit.addItem(pasteRef);
        cfg.addItem(pasteRef);

        // add menus to the config as well
        cfg.addGroup(file);
        cfg.addGroup(edit);

        cfg.addAccordion(JCRClientUtils.SITE_REPOSITORY);

        cfg.setNodeTypes(JCRClientUtils.SITE_NODETYPES);
        cfg.setFolderTypes(JCRClientUtils.SITE_NODETYPES);

        return cfg;

    }

}