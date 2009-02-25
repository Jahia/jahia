<%--


    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
<%@ taglib uri="http://www.jahia.org/tags/templateLib" prefix="template" %>
<%@ taglib prefix="internal" uri="http://www.jahia.org/tags/internalLib" %>
<%@ attribute name="rootPath" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>
<%@ attribute name="startPath" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>
<%@ attribute name="enginemode" required="false" rtexprvalue="true" type="java.lang.Boolean" description="text" %>
<%@ attribute name="filters" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>
<%@ attribute name="nodeTypes" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>
<%@ attribute name="mimeTypes" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>
<%@ attribute name="conf" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>
<%@ attribute name="callback" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>

<template:gwtJahiaModule id="filemanager" jahiaType="filemanager" rootPath="<%=rootPath%>" startPath="<%=startPath%>"
                         enginemode="<%=enginemode%>" nodeTypes="<%=nodeTypes%>" filters="<%=filters%>"
                         mimeTypes="<%=mimeTypes%>" callback="<%=callback%>" config="<%=conf%>"/>

<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.newDir.label"
                            aliasResourceName="fm_newdir"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.newMashup.label"
                            aliasResourceName="fm_newmashup"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.copyFile.label"
                            aliasResourceName="fm_copy"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.cutFile.label"
                            aliasResourceName="fm_cut"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.pasteFile.label"
                            aliasResourceName="fm_paste"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.lockFile.label"
                            aliasResourceName="fm_lock"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.unlockFile.label"
                            aliasResourceName="fm_unlock"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.deleteFile.label"
                            aliasResourceName="fm_remove"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.renameFile.label"
                            aliasResourceName="fm_rename"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.zipFile.label"
                            aliasResourceName="fm_zip"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.unzipFile.label"
                            aliasResourceName="fm_unzip"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.downloadFile.label"
                            aliasResourceName="fm_download"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.upload.label"
                            aliasResourceName="fm_upload"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.openIEfolder.label"
                            aliasResourceName="fm_webfolder"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.icons.label"
                            aliasResourceName="fm_icons"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.icons.detailed.label"
                            aliasResourceName="fm_icons_detailed"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.list.label"
                            aliasResourceName="fm_list"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.crop.label"
                            aliasResourceName="fm_crop"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.resize.label"
                            aliasResourceName="fm_resize"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.rotate.label"
                            aliasResourceName="fm_rotate"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.refresh.label"
                            aliasResourceName="fm_refresh"/>

<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.selection.label"
                            aliasResourceName="fm_selection"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.deselect.label"
                            aliasResourceName="fm_deselect"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.width.label"
                            aliasResourceName="fm_width"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.height.label"
                            aliasResourceName="fm_height"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.ratio.label"
                            aliasResourceName="fm_ratio"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.newname.label"
                            aliasResourceName="fm_newname"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.mount.label"
                            aliasResourceName="fm_mount"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.unmount.label"
                            aliasResourceName="fm_unmount"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.mountpoint.label"
                            aliasResourceName="fm_mountpoint"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.serveraddress.label"
                            aliasResourceName="fm_serveraddress"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.fileMenu.label"
                            aliasResourceName="fm_fileMenu"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.editMenu.label"
                            aliasResourceName="fm_editMenu"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.remoteMenu.label"
                            aliasResourceName="fm_remoteMenu"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.imageMenu.label"
                            aliasResourceName="fm_imageMenu"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.viewMenu.label"
                            aliasResourceName="fm_viewMenu"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.my.label"
                            aliasResourceName="myRepository"/>
<internal:gwtResourceBundle
        resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.myExternal.label"
        aliasResourceName="myExternalRepository"/>
<internal:gwtResourceBundle
        resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.shared.label"
        aliasResourceName="sharedRepository"/>
<internal:gwtResourceBundle
        resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.currentSite.label"
        aliasResourceName="websiteRepository"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.my.label"
                            aliasResourceName="myMashupRepository"/>
<internal:gwtResourceBundle
        resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.shared.label"
        aliasResourceName="sharedMashupRepository"/>
<internal:gwtResourceBundle
        resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.currentSite.label"
        aliasResourceName="websiteMashupRepository"/>
<internal:gwtResourceBundle
        resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.mashup.label"
        aliasResourceName="mashupRepository"/>
<internal:gwtResourceBundle
        resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.global.label"
        aliasResourceName="globalRepository"/>
<internal:gwtResourceBundle
        resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.savedSearch.label"
        aliasResourceName="savedSearch"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.saveSearch.label"
                            aliasResourceName="fm_saveSearch"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.search.label"
                            aliasResourceName="fm_search"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.filters.label"
                            aliasResourceName="fm_filters"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.mimes.label"
                            aliasResourceName="fm_mimes"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.nodes.label"
                            aliasResourceName="fm_nodes"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.information.label"
                            aliasResourceName="fm_information"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.properties.label"
                            aliasResourceName="fm_properties"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.roles.label"
                            aliasResourceName="fm_roles"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.modes.label"
                            aliasResourceName="fm_modes"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.authorizations.label"
                            aliasResourceName="fm_authorizations"/>

<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.usages.label"
                            aliasResourceName="fm_usages"/>

<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.info.name.label"
                            aliasResourceName="fm_info_name"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.info.path.label"
                            aliasResourceName="fm_info_path"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.info.size.label"
                            aliasResourceName="fm_info_size"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.info.lastModif.label"
                            aliasResourceName="fm_info_lastModif"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.info.lock.label"
                            aliasResourceName="fm_info_lock"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.info.nbFiles.label"
                            aliasResourceName="fm_info_nbFiles"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.info.nbFolders.label"
                            aliasResourceName="fm_info_nbFolders"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.info.totalSize.label"
                            aliasResourceName="fm_info_totalSize"/>

<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.properties.save.label"
                            aliasResourceName="fm_save"/>
<internal:gwtResourceBundle
        resourceName="org.jahia.engines.filemanager.Filemanager_Engine.properties.restore.label"
        aliasResourceName="fm_restore"/>

<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.usages.page.label"
                            aliasResourceName="fm_page"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.usages.language.label"
                            aliasResourceName="fm_language"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.usages.workflow.label"
                            aliasResourceName="fm_workflow"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.usages.versioned.label"
                            aliasResourceName="fm_versioned"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.usages.live.label"
                            aliasResourceName="fm_live"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.usages.staging.label"
                            aliasResourceName="fm_staging"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.usages.notify.label"
                            aliasResourceName="fm_notify"/>

<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.uploadFile.label"
                            aliasResourceName="fm_uploadFiles"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.autoUnzip.label"
                            aliasResourceName="fm_autoUnzip"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.addFile.label"
                            aliasResourceName="fm_addFile"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.cancel.label"
                            aliasResourceName="fm_cancel"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.ok.label"
                            aliasResourceName="fm_ok"/>

<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.thumbFilter.label"
                            aliasResourceName="fm_thumbFilter"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.thumbSort.label"
                            aliasResourceName="fm_thumbSort"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.thumbSortName.label"
                            aliasResourceName="fm_thumbSortName"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.thumbSortSize.label"
                            aliasResourceName="fm_thumbSortSize"/>
<internal:gwtResourceBundle
        resourceName="org.jahia.engines.filemanager.Filemanager_Engine.thumbSortLastModif.label"
        aliasResourceName="fm_thumbSortLastModif"/>

<internal:gwtResourceBundle resourceName="org.jahia.engines.rights.ManageRights.principal.label"
                            aliasResourceName="ae_principal"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.rights.ManageRights.restoreInheritance.label"
                            aliasResourceName="ae_restore_inheritance"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.rights.ManageRights.inheritedFrom.label"
                            aliasResourceName="ae_inherited_from"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.rights.ManageRights.restoreAllInheritance.label"
                            aliasResourceName="ae_restore_all_inheritance"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.rights.ManageRights.breakAllInheritance.label"
                            aliasResourceName="ae_break_all_inheritance"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.rights.ManageRights.remove.label"
                            aliasResourceName="ae_remove"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.rights.ManageRights.save.label"
                            aliasResourceName="ae_save"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.rights.ManageRights.restore.label"
                            aliasResourceName="ae_restore"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.users.SelectUG_Engine.newUsers.label"
                            aliasResourceName="um_adduser"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.users.SelectUG_Engine.newGroups.label"
                            aliasResourceName="um_addgroup"/>

<internal:gwtResourceBundle resourceName="org.jahia.engines.MashupsManager.mashups.label"
                            aliasResourceName="mw_mashups"/>

<internal:gwtResourceBundle resourceName="org.jahia.engines.MashupsManager.wizard.portletdef.label"
                            aliasResourceName="mw_select_portlet_def"/>

<internal:gwtResourceBundle resourceName="org.jahia.engines.MashupsManager.wizard.ok.label"
                            aliasResourceName="mw_ok"/>

<internal:gwtResourceBundle resourceName="org.jahia.engines.MashupsManager.wizard.parameters.label"
                            aliasResourceName="mw_params"/>

<internal:gwtResourceBundle resourceName="org.jahia.engines.MashupsManager.wizard.parameters.edit.label"
                            aliasResourceName="mw_edit_params"/>

<internal:gwtResourceBundle resourceName="org.jahia.engines.MashupsManager.wizard.props.load.error.label"
                            aliasResourceName="mw_prop_load_error"/>

<internal:gwtResourceBundle resourceName="org.jahia.engines.MashupsManager.wizard.modesperm.label"
                            aliasResourceName="mw_modes_permissions"/>

<internal:gwtResourceBundle resourceName="org.jahia.engines.MashupsManager.wizard.modesperm.description.label"
                            aliasResourceName="mw_modes_permissions_description"/>

<internal:gwtResourceBundle resourceName="org.jahia.engines.MashupsManager.wizard.modes.adduser.label"
                            aliasResourceName="mw_modes_adduser"/>

<internal:gwtResourceBundle resourceName="org.jahia.engines.MashupsManager.wizard.modes.addgroup.label"
                            aliasResourceName="mw_modes_addgroup"/>

<internal:gwtResourceBundle resourceName="org.jahia.engines.MashupsManager.wizard.roles.adduser.label"
                            aliasResourceName="mw_roles_adduser"/>

<internal:gwtResourceBundle resourceName="org.jahia.engines.MashupsManager.wizard.roles.addgroup.label"
                            aliasResourceName="mw_roles_addgroup"/>

<internal:gwtResourceBundle resourceName="org.jahia.engines.MashupsManager.wizard.rolesperm.label"
                            aliasResourceName="mw_roles_perm"/>

<internal:gwtResourceBundle resourceName="org.jahia.engines.MashupsManager.wizard.rolesperm.description.label"
                            aliasResourceName="mw_roles_description"/>

<internal:gwtResourceBundle resourceName="org.jahia.engines.MashupsManager.wizard.finish.label"
                            aliasResourceName="mw_finish"/>

<internal:gwtResourceBundle resourceName="org.jahia.engines.MashupsManager.wizard.saveas.label"
                            aliasResourceName="mw_save_as"/>


