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

import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.widget.tripanel.ManagerLinker;

/**
 * User: rfelden
 * Date: 7 janv. 2009 - 14:04:14
 */
// ToDO: [ManagerConfiguration-Spring] Springify the ManagerConfiguration and remove the ManagerConfigurationFactory
public class ManagerConfigurationFactory {

    public static final String MANAGER_CONFIG = "conf";
    public static final String FILEMANAGER = "filemanager";
    public static final String MASHUPMANAGER = "mashupmanager";
    public static final String CATEGORYMANAGER = "categorymanager";
    public static final String TAGMANAGER = "tagmanager";
    public static final String PORTLETDEFINITIONMANAGER = "portletdefinitionmanager";
    public static final String FILEPICKER = "filepicker";
    public static final String LINKPICKER = "linkpicker";
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
            if (config.contains(TAGMANAGER)) {
                return getTagManagerConfiguration(linker);
            }
            if (config.contains(LINKPICKER)) {
                return getPagePickerConfiguration(linker);
            }
        }
        return getCompleteManagerConfiguration(linker);
    }

    public static ManagerConfiguration getCompleteManagerConfiguration(final ManagerLinker linker) {
        ManagerConfiguration completeManagerConfig = new ManagerConfiguration();
        completeManagerConfig.setEnableTextMenu(true);

        completeManagerConfig.setToolbarGroup("content-manager");

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
        fileManagerConfig.addColumn("name");
        fileManagerConfig.addColumn("locked");
        fileManagerConfig.addColumn("path");
        fileManagerConfig.addColumn("size");
        fileManagerConfig.addColumn("lastModified");

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
        filePickerConfig.setHideLeftPanel(true);
        filePickerConfig.addAccordion(JCRClientUtils.WEBSITE_REPOSITORY);
        filePickerConfig.addAccordion(JCRClientUtils.SHARED_REPOSITORY);
        filePickerConfig.addAccordion(JCRClientUtils.MY_EXTERNAL_REPOSITORY);
        filePickerConfig.addAccordion(JCRClientUtils.MY_REPOSITORY);
        filePickerConfig.addAccordion(JCRClientUtils.USERS_REPOSITORY);
        filePickerConfig.setNodeTypes(JCRClientUtils.FILE_NODETYPES);
        filePickerConfig.setFolderTypes(JCRClientUtils.FOLDER_NODETYPES);

        return filePickerConfig;
    }

    public static ManagerConfiguration getPagePickerConfiguration(final ManagerLinker linker) {
        ManagerConfiguration filePickerConfig = new ManagerConfiguration();
        filePickerConfig.setEnableTextMenu(false);
        filePickerConfig.addAccordion(JCRClientUtils.SITE_REPOSITORY);
        filePickerConfig.setToolbarGroup("file-picker");
        filePickerConfig.setHideLeftPanel(true);
        filePickerConfig.setNodeTypes(JCRClientUtils.PAGE_NODETYPES);
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

        mashupManagerConfig.addColumn("name");
        mashupManagerConfig.addColumn("locked");
        mashupManagerConfig.addColumn("path");
        mashupManagerConfig.addColumn("lastModified");

        mashupManagerConfig.setDefaultView(JCRClientUtils.DETAILED_THUMB_VIEW);

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
        mashupPickerConfig.addColumn("picker");

        mashupPickerConfig.setDefaultView(JCRClientUtils.DETAILED_THUMB_VIEW);
        mashupPickerConfig.addAccordion(JCRClientUtils.WEBSITE_MASHUP_REPOSITORY);
        mashupPickerConfig.addAccordion(JCRClientUtils.SHARED_MASHUP_REPOSITORY);
        mashupPickerConfig.addAccordion(JCRClientUtils.MY_MASHUP_REPOSITORY);

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

        categoryManagerConfig.addColumn("ext");
        categoryManagerConfig.addColumn("name");
        categoryManagerConfig.addColumn("locked");
        categoryManagerConfig.addColumn("path");

        categoryManagerConfig.setDefaultView(JCRClientUtils.FILE_TABLE);
        categoryManagerConfig.addAccordion(JCRClientUtils.CATEGORY_REPOSITORY);

        categoryManagerConfig.setNodeTypes(JCRClientUtils.CATEGORY_NODETYPES);
        categoryManagerConfig.setFolderTypes(JCRClientUtils.CATEGORY_NODETYPES);

        return categoryManagerConfig;
    }

    public static ManagerConfiguration getTagManagerConfiguration(final ManagerLinker linker) {
        ManagerConfiguration configuration = new ManagerConfiguration();
        configuration.setHideLeftPanel(true);
        configuration.setEnableTextMenu(true);
        configuration.setDisplayExt(false);
        configuration.setDisplaySize(false);
        configuration.setDisplayDate(false);
        configuration.setToolbarGroup("tag-manager");
        configuration.setExpandRoot(true);
        configuration.addColumn("ext");
        configuration.addColumn("name");
        configuration.addColumn("count");
        configuration.addColumn("locked");
        configuration.addColumn("path");

        configuration.setDefaultView(JCRClientUtils.FILE_TABLE);
        configuration.addAccordion(JCRClientUtils.TAG_REPOSITORY);

        configuration.setNodeTypes(JCRClientUtils.TAG_NODETYPES);
        configuration.setFolderTypes(JCRClientUtils.TAG_NODETYPES);

        return configuration;
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
        categoryPickerConfig.addColumn("picker");

        categoryPickerConfig.setDefaultView(JCRClientUtils.DETAILED_THUMB_VIEW);

        // hide the left panel
        categoryPickerConfig.setHideLeftPanel(true);

        categoryPickerConfig.setNodeTypes(JCRClientUtils.CATEGORY_NODETYPES);
        categoryPickerConfig.setFolderTypes(JCRClientUtils.CATEGORY_NODETYPES);

        categoryPickerConfig.addAccordion(JCRClientUtils.CATEGORY_REPOSITORY);

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
        cfg.addColumn("name");
        cfg.addColumn("locked");
        cfg.addColumn("lastModified");
        cfg.addColumn("lastModifiedBy");
        cfg.addColumn("publicationInfo");

        cfg.setDefaultView(JCRClientUtils.FILE_TABLE);

        cfg.addAccordion(JCRClientUtils.SITE_REPOSITORY);

        cfg.setNodeTypes(JCRClientUtils.SITE_NODETYPES);
        cfg.setFolderTypes(JCRClientUtils.SITE_NODETYPES);

        // do not display collections, if they do not match node type filters
        cfg.setAllowCollections(false);

        return cfg;

    }

}