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

<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.newDir.label"
                                  aliasResourceName="fm_newdir"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.newMashup.label"
                                  aliasResourceName="fm_newmashup"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.copyFile.label"
                                  aliasResourceName="fm_copy"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.cutFile.label"
                                  aliasResourceName="fm_cut"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.pasteFile.label"
                                  aliasResourceName="fm_paste"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.lockFile.label"
                                  aliasResourceName="fm_lock"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.unlockFile.label"
                                  aliasResourceName="fm_unlock"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.deleteFile.label"
                                  aliasResourceName="fm_remove"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.renameFile.label"
                                  aliasResourceName="fm_rename"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.zipFile.label"
                                  aliasResourceName="fm_zip"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.unzipFile.label"
                                  aliasResourceName="fm_unzip"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.downloadFile.label"
                                  aliasResourceName="fm_download"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.upload.label"
                                  aliasResourceName="fm_upload"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.openIEfolder.label"
                                  aliasResourceName="fm_webfolder"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.icons.label"
                                  aliasResourceName="fm_icons"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.icons.detailed.label"
                                  aliasResourceName="fm_icons_detailed"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.list.label"
                                  aliasResourceName="fm_list"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.crop.label"
                                  aliasResourceName="fm_crop"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.resize.label"
                                  aliasResourceName="fm_resize"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.rotate.label"
                                  aliasResourceName="fm_rotate"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.refresh.label"
                                  aliasResourceName="fm_refresh"/>

<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.selection.label"
                                  aliasResourceName="fm_selection"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.deselect.label"
                                  aliasResourceName="fm_deselect"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.width.label"
                                  aliasResourceName="fm_width"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.height.label"
                                  aliasResourceName="fm_height"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.ratio.label"
                                  aliasResourceName="fm_ratio"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.newname.label"
                                  aliasResourceName="fm_newname"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.mount.label"
                                  aliasResourceName="fm_mount"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.unmount.label"
                                  aliasResourceName="fm_unmount"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.mountpoint.label"
                                  aliasResourceName="fm_mountpoint"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.serveraddress.label"
                                  aliasResourceName="fm_serveraddress"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.fileMenu.label"
                                  aliasResourceName="fm_fileMenu"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.editMenu.label"
                                  aliasResourceName="fm_editMenu"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.remoteMenu.label"
                                  aliasResourceName="fm_remoteMenu"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.imageMenu.label"
                                  aliasResourceName="fm_imageMenu"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.viewMenu.label"
                                  aliasResourceName="fm_viewMenu"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.my.label"
                                  aliasResourceName="myRepository"/>
<internal:gwtEngineResourceBundle
        resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.myExternal.label"
        aliasResourceName="myExternalRepository"/>
<internal:gwtEngineResourceBundle
        resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.shared.label"
        aliasResourceName="sharedRepository"/>
<internal:gwtEngineResourceBundle
        resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.currentSite.label"
        aliasResourceName="websiteRepository"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.my.label"
                                  aliasResourceName="myMashupRepository"/>
<internal:gwtEngineResourceBundle
        resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.shared.label"
        aliasResourceName="sharedMashupRepository"/>
<internal:gwtEngineResourceBundle
        resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.currentSite.label"
        aliasResourceName="websiteMashupRepository"/>
<internal:gwtEngineResourceBundle
        resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.mashup.label"
        aliasResourceName="mashupRepository"/>
<internal:gwtEngineResourceBundle
        resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.global.label"
        aliasResourceName="globalRepository"/>
<internal:gwtEngineResourceBundle
        resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.savedSearch.label"
        aliasResourceName="savedSearch"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.saveSearch.label"
                                  aliasResourceName="fm_saveSearch"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.search.label"
                                  aliasResourceName="fm_search"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.filters.label"
                                  aliasResourceName="fm_filters"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.mimes.label"
                                  aliasResourceName="fm_mimes"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.nodes.label"
                                  aliasResourceName="fm_nodes"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.information.label"
                                  aliasResourceName="fm_information"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.properties.label"
                                  aliasResourceName="fm_properties"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.roles.label"
                                  aliasResourceName="fm_roles"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.modes.label"
                                  aliasResourceName="fm_modes"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.authorizations.label"
                                  aliasResourceName="fm_authorizations"/>

<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.usages.label"
                                  aliasResourceName="fm_usages"/>

<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.info.name.label"
                                  aliasResourceName="fm_info_name"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.info.path.label"
                                  aliasResourceName="fm_info_path"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.info.size.label"
                                  aliasResourceName="fm_info_size"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.info.lastModif.label"
                                  aliasResourceName="fm_info_lastModif"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.info.lock.label"
                                  aliasResourceName="fm_info_lock"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.info.nbFiles.label"
                                  aliasResourceName="fm_info_nbFiles"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.info.nbFolders.label"
                                  aliasResourceName="fm_info_nbFolders"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.info.totalSize.label"
                                  aliasResourceName="fm_info_totalSize"/>

<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.properties.save.label"
                                  aliasResourceName="fm_save"/>
<internal:gwtEngineResourceBundle
        resourceName="org.jahia.engines.filemanager.Filemanager_Engine.properties.restore.label"
        aliasResourceName="fm_restore"/>

<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.usages.page.label"
                                  aliasResourceName="fm_page"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.usages.language.label"
                                  aliasResourceName="fm_language"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.usages.workflow.label"
                                  aliasResourceName="fm_workflow"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.usages.versioned.label"
                                  aliasResourceName="fm_versioned"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.usages.live.label"
                                  aliasResourceName="fm_live"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.usages.staging.label"
                                  aliasResourceName="fm_staging"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.usages.notify.label"
                                  aliasResourceName="fm_notify"/>

<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.uploadFile.label"
                                  aliasResourceName="fm_uploadFiles"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.autoUnzip.label"
                                  aliasResourceName="fm_autoUnzip"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.addFile.label"
                                  aliasResourceName="fm_addFile"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.cancel.label"
                                  aliasResourceName="fm_cancel"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.ok.label"
                                  aliasResourceName="fm_ok"/>

<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.thumbFilter.label"
                                  aliasResourceName="fm_thumbFilter"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.thumbSort.label"
                                  aliasResourceName="fm_thumbSort"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.thumbSortName.label"
                                  aliasResourceName="fm_thumbSortName"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.thumbSortSize.label"
                                  aliasResourceName="fm_thumbSortSize"/>
<internal:gwtEngineResourceBundle
        resourceName="org.jahia.engines.filemanager.Filemanager_Engine.thumbSortLastModif.label"
        aliasResourceName="fm_thumbSortLastModif"/>

<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.rights.ManageRights.principal.label"
                                  aliasResourceName="ae_principal"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.rights.ManageRights.restoreInheritance.label"
                                  aliasResourceName="ae_restore_inheritance"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.rights.ManageRights.inheritedFrom.label"
                                  aliasResourceName="ae_inherited_from"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.rights.ManageRights.restoreAllInheritance.label"
                                  aliasResourceName="ae_restore_all_inheritance"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.rights.ManageRights.breakAllInheritance.label"
                                  aliasResourceName="ae_break_all_inheritance"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.rights.ManageRights.remove.label"
                                  aliasResourceName="ae_remove"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.rights.ManageRights.save.label"
                                  aliasResourceName="ae_save"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.rights.ManageRights.restore.label"
                                  aliasResourceName="ae_restore"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.users.SelectUG_Engine.newUsers.label"
                                  aliasResourceName="um_adduser"/>
<internal:gwtEngineResourceBundle resourceName="org.jahia.engines.users.SelectUG_Engine.newGroups.label"
                                  aliasResourceName="um_addgroup"/>
