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
    public static final String ROLESMANAGER = "rolesmanager";

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
            if (config.contains(ROLESMANAGER)) {
                return getRolesManagerConfiguration(linker);
            }
            if (config.contains(LINKPICKER)) {
                return getPagePickerConfiguration(linker);
            }
        }
        return getCompleteManagerConfiguration(linker);
    }

    public static ManagerConfiguration getCompleteManagerConfiguration(final ManagerLinker linker) {
        ManagerConfiguration configuration = new ManagerConfiguration();
        configuration.setEnableTextMenu(true);

        configuration.setToolbarGroup("content-manager");

        // no columns to add (default)

        // show root repository
        configuration.addAccordion(JCRClientUtils.GLOBAL_REPOSITORY);

        configuration.setNodeTypes("");
        configuration.setFolderTypes("");

        // show the current site (first) tab by default

        // do not hide the left panel (default)

        configuration.addTab(JCRClientUtils.ROLES_ACL);
        configuration.addTab(JCRClientUtils.MODES_ACL);

        return configuration;
    }

    public static ManagerConfiguration getFileManagerConfiguration(final ManagerLinker linker) {
        ManagerConfiguration configuration = new ManagerConfiguration();
        configuration.setEnableTextMenu(true);
        configuration.setDisplayProvider(true);

        configuration.setToolbarGroup("document-manager");

        configuration.addColumn("providerKey");
        configuration.addColumn("ext");
        configuration.addColumn("name");
        configuration.addColumn("locked");
        configuration.addColumn("path");
        configuration.addColumn("size");
        configuration.addColumn("lastModified");

        configuration.addTab(JCRClientUtils.AUTHORIZATIONS);
        configuration.addTab(JCRClientUtils.USAGE);
        configuration.addTab(JCRClientUtils.VERSIONING);

        // no columns to add (default)

        // hide the mashup repository and the global repository
        configuration.addAccordion(JCRClientUtils.WEBSITE_REPOSITORY);
        configuration.addAccordion(JCRClientUtils.SHARED_REPOSITORY);
        configuration.addAccordion(JCRClientUtils.MY_EXTERNAL_REPOSITORY);
        configuration.addAccordion(JCRClientUtils.MY_REPOSITORY);
        configuration.addAccordion(JCRClientUtils.USERS_REPOSITORY);

        // show the current site (first) tab by default

        // do not hide the left panel (default)
        configuration.setNodeTypes(JCRClientUtils.FILE_NODETYPES);
        configuration.setFolderTypes(JCRClientUtils.FOLDER_NODETYPES);

        return configuration;
    }

    public static ManagerConfiguration getFilePickerConfiguration(final ManagerLinker linker) {
        ManagerConfiguration configuration = new ManagerConfiguration();
        configuration.setEnableTextMenu(false);
        configuration.setToolbarGroup("file-picker");
        configuration.setHideLeftPanel(true);
        configuration.addTab(JCRClientUtils.AUTHORIZATIONS);
        configuration.addTab(JCRClientUtils.USAGE);
        configuration.addTab(JCRClientUtils.VERSIONING);
        configuration.addAccordion(JCRClientUtils.WEBSITE_REPOSITORY);
        configuration.addAccordion(JCRClientUtils.SHARED_REPOSITORY);
        configuration.addAccordion(JCRClientUtils.MY_EXTERNAL_REPOSITORY);
        configuration.addAccordion(JCRClientUtils.MY_REPOSITORY);
        configuration.addAccordion(JCRClientUtils.USERS_REPOSITORY);
        configuration.setNodeTypes(JCRClientUtils.FILE_NODETYPES);
        configuration.setFolderTypes(JCRClientUtils.FOLDER_NODETYPES);

        return configuration;
    }

    public static ManagerConfiguration getPagePickerConfiguration(final ManagerLinker linker) {
        ManagerConfiguration configuration = new ManagerConfiguration();
        configuration.setEnableTextMenu(false);
        configuration.addAccordion(JCRClientUtils.SITE_REPOSITORY);
        configuration.setToolbarGroup("file-picker");
        configuration.addTab(JCRClientUtils.AUTHORIZATIONS);
        configuration.addTab(JCRClientUtils.USAGE);
        configuration.addTab(JCRClientUtils.VERSIONING);
        configuration.setHideLeftPanel(true);
        configuration.setNodeTypes(JCRClientUtils.PAGE_NODETYPES);
        configuration.setFolderTypes(JCRClientUtils.FOLDER_NODETYPES);

        return configuration;
    }

    public static ManagerConfiguration getMashupManagerConfiguration(final ManagerLinker linker) {
        ManagerConfiguration configuration = new ManagerConfiguration();
        configuration.setEnableTextMenu(true);
        configuration.setEnableFileDoubleClick(false);
        configuration.setDisplayExt(false);
        configuration.setDisplaySize(false);
        configuration.addTab(JCRClientUtils.AUTHORIZATIONS);
        configuration.addTab(JCRClientUtils.USAGE);

        configuration.setToolbarGroup("mashup-manager");

        configuration.addColumn("name");
        configuration.addColumn("locked");
        configuration.addColumn("path");
        configuration.addColumn("lastModified");

        configuration.setDefaultView(JCRClientUtils.DETAILED_THUMB_VIEW);

        // show only the mashup repository
        configuration.addAccordion(JCRClientUtils.WEBSITE_MASHUP_REPOSITORY);
        configuration.addAccordion(JCRClientUtils.SHARED_MASHUP_REPOSITORY);
        configuration.addAccordion(JCRClientUtils.MY_MASHUP_REPOSITORY);

        configuration.addTab(JCRClientUtils.ROLES_ACL);
        configuration.addTab(JCRClientUtils.MODES_ACL);


        // show the mashup tab by default

        // do not hide the left panel (default)

        configuration.setNodeTypes(JCRClientUtils.PORTLET_NODETYPES);
        configuration.setFolderTypes(JCRClientUtils.FOLDER_NODETYPES);

        return configuration;
    }

    public static ManagerConfiguration getMashupPickerConfiguration(final ManagerLinker linker) {
        ManagerConfiguration configuration = new ManagerConfiguration();
        configuration.setEnableTextMenu(false);
        configuration.setEnableFileDoubleClick(false);
        configuration.setDisplayExt(false);
        configuration.setDisplaySize(false);
        configuration.addTab(JCRClientUtils.AUTHORIZATIONS);
        configuration.addTab(JCRClientUtils.USAGE);

        configuration.setToolbarGroup("mashup-picker");

        // only one column here : name
        configuration.addColumn("name");
        configuration.addColumn("picker");

        configuration.setDefaultView(JCRClientUtils.DETAILED_THUMB_VIEW);
        configuration.addAccordion(JCRClientUtils.WEBSITE_MASHUP_REPOSITORY);
        configuration.addAccordion(JCRClientUtils.SHARED_MASHUP_REPOSITORY);
        configuration.addAccordion(JCRClientUtils.MY_MASHUP_REPOSITORY);

        // hide the left panel
        configuration.setHideLeftPanel(true);

        configuration.setNodeTypes(JCRClientUtils.PORTLET_NODETYPES);
        configuration.setFolderTypes(JCRClientUtils.FOLDER_NODETYPES);

        return configuration;
    }

    public static ManagerConfiguration getCategoryManagerConfiguration(final ManagerLinker linker) {
        ManagerConfiguration configuration = new ManagerConfiguration();
        configuration.setEnableTextMenu(true);
        configuration.setDisplayExt(false);
        configuration.setDisplaySize(false);
        configuration.setDisplayDate(false);

        configuration.setToolbarGroup("category-manager");

        configuration.addColumn("ext");
        configuration.addColumn("name");
        configuration.addColumn("locked");
        configuration.addColumn("path");

        configuration.setDefaultView(JCRClientUtils.FILE_TABLE);
        configuration.addAccordion(JCRClientUtils.CATEGORY_REPOSITORY);

        configuration.setNodeTypes(JCRClientUtils.CATEGORY_NODETYPES);
        configuration.setFolderTypes(JCRClientUtils.CATEGORY_NODETYPES);

        configuration.addTab(JCRClientUtils.AUTHORIZATIONS);
        configuration.addTab(JCRClientUtils.USAGE);

        return configuration;
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

        configuration.addTab(JCRClientUtils.AUTHORIZATIONS);
        configuration.addTab(JCRClientUtils.USAGE);

        return configuration;
    }

    public static ManagerConfiguration getRolesManagerConfiguration(final ManagerLinker linker) {
        ManagerConfiguration configuration = new ManagerConfiguration();
        configuration.setHideLeftPanel(true);
        configuration.setEnableTextMenu(true);
        configuration.setDisplayExt(false);
        configuration.setDisplaySize(false);
        configuration.setDisplayDate(false);
        configuration.setToolbarGroup("role-manager");
        configuration.setExpandRoot(true);
        configuration.addColumn("name");
        configuration.addTab(JCRClientUtils.PRINCIPAL_ROLES_MAPPING);

        configuration.setDefaultView(JCRClientUtils.FILE_TABLE);
        configuration.addAccordion(JCRClientUtils.TAG_REPOSITORY);

        configuration.setNodeTypes(JCRClientUtils.TAG_NODETYPES);
        configuration.setFolderTypes(JCRClientUtils.TAG_NODETYPES);

        return configuration;
    }

    public static ManagerConfiguration getCategoryPickerConfiguration(final ManagerLinker linker) {
        ManagerConfiguration configuration = new ManagerConfiguration();
        configuration.setEnableTextMenu(false);
        configuration.setEnableFileDoubleClick(false);
        configuration.setDisplayExt(false);
        configuration.setDisplaySize(false);

        configuration.setToolbarGroup("category-picker");

        // only one column here : name
        configuration.addColumn("name");
        configuration.addColumn("picker");

        configuration.setDefaultView(JCRClientUtils.DETAILED_THUMB_VIEW);

        // hide the left panel
        configuration.setHideLeftPanel(true);

        configuration.setNodeTypes(JCRClientUtils.CATEGORY_NODETYPES);
        configuration.setFolderTypes(JCRClientUtils.CATEGORY_NODETYPES);

        configuration.addAccordion(JCRClientUtils.CATEGORY_REPOSITORY);

        configuration.addTab(JCRClientUtils.AUTHORIZATIONS);
        configuration.addTab(JCRClientUtils.USAGE);

        return configuration;
    }

    public static ManagerConfiguration getPortletDefinitionManagerConfiguration(final ManagerLinker linker) {
        ManagerConfiguration configuration = new ManagerConfiguration();

        configuration.setEnableTextMenu(false);
        configuration.setEnableFileDoubleClick(false);
        configuration.setDisplayExt(false);
        configuration.setDisplaySize(false);

        configuration.setToolbarGroup("portlet-definition-manager");

        // only one column here : name
        configuration.addColumn("name");

        // hide the left panel
        configuration.setHideLeftPanel(true);

        configuration.addAccordion(JCRClientUtils.PORTLET_DEFINITIONS_REPOSITORY);

        configuration.setNodeTypes(JCRClientUtils.PORTLET_DEFINITIONS_NODETYPES);
        configuration.setFolderTypes(JCRClientUtils.FOLDER_NODETYPES);

        configuration.addTab("portlets");
        configuration.addTab(JCRClientUtils.AUTHORIZATIONS);
        configuration.addTab(JCRClientUtils.USAGE);

        return configuration;
    }

    public static ManagerConfiguration getSiteManagerConfiguration(ManagerLinker linker) {
        ManagerConfiguration configuration = new ManagerConfiguration();
        configuration.setEnableTextMenu(true);
        configuration.setDisplaySize(false);
        configuration.setDisplayDate(false);

        configuration.setToolbarGroup("site-manager");

        configuration.addColumn("ext");
        configuration.addColumn("name");
        configuration.addColumn("locked");
        configuration.addColumn("lastModified");
        configuration.addColumn("lastModifiedBy");
        configuration.addColumn("publicationInfo");

        configuration.setDefaultView(JCRClientUtils.FILE_TABLE);

        configuration.addAccordion(JCRClientUtils.SITE_REPOSITORY);

        configuration.setNodeTypes(JCRClientUtils.SITE_NODETYPES);
        configuration.setFolderTypes(JCRClientUtils.SITE_NODETYPES);

        // do not display collections, if they do not match node type filters
        configuration.setAllowCollections(false);
        configuration.addTab(JCRClientUtils.AUTHORIZATIONS);
        configuration.addTab(JCRClientUtils.USAGE);
        configuration.addTab(JCRClientUtils.VERSIONING);

        return configuration;

    }

}