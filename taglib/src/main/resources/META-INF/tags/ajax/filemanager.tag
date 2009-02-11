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
                         mimeTypes="<%=mimeTypes%>" callback="<%=callback%>" config="<%=conf%>">

    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.newDir.label" aliasResourceName="fm_newdir"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.newMashup.label" aliasResourceName="fm_newmashup"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.copyFile.label" aliasResourceName="fm_copy"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.cutFile.label" aliasResourceName="fm_cut"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.pasteFile.label" aliasResourceName="fm_paste"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.lockFile.label" aliasResourceName="fm_lock"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.unlockFile.label" aliasResourceName="fm_unlock"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.deleteFile.label" aliasResourceName="fm_remove"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.renameFile.label" aliasResourceName="fm_rename"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.zipFile.label" aliasResourceName="fm_zip"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.unzipFile.label" aliasResourceName="fm_unzip"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.downloadFile.label" aliasResourceName="fm_download"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.upload.label" aliasResourceName="fm_upload"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.openIEfolder.label" aliasResourceName="fm_webfolder"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.icons.label" aliasResourceName="fm_icons"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.icons.detailed.label" aliasResourceName="fm_icons_detailed"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.list.label" aliasResourceName="fm_list"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.crop.label" aliasResourceName="fm_crop"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.resize.label" aliasResourceName="fm_resize"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.rotate.label" aliasResourceName="fm_rotate"/>

    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.selection.label" aliasResourceName="fm_selection"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.deselect.label" aliasResourceName="fm_deselect"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.width.label" aliasResourceName="fm_width"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.height.label" aliasResourceName="fm_height"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.ratio.label" aliasResourceName="fm_ratio"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.newname.label" aliasResourceName="fm_newname"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.mount.label" aliasResourceName="fm_mount"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.unmount.label" aliasResourceName="fm_unmount"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.mountpoint.label" aliasResourceName="fm_mountpoint"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.serveraddress.label" aliasResourceName="fm_serveraddress"/>

    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.fileMenu.label" aliasResourceName="fm_fileMenu"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.editMenu.label" aliasResourceName="fm_editMenu"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.remoteMenu.label" aliasResourceName="fm_remoteMenu"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.imageMenu.label" aliasResourceName="fm_imageMenu"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.viewMenu.label" aliasResourceName="fm_viewMenu"/>

    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.my.label" aliasResourceName="myRepository"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.myExternal.label" aliasResourceName="myExternalRepository"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.shared.label" aliasResourceName="sharedRepository"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.currentSite.label" aliasResourceName="websiteRepository"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.my.label" aliasResourceName="myMashupRepository"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.shared.label" aliasResourceName="sharedMashupRepository"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.currentSite.label" aliasResourceName="websiteMashupRepository"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.mashup.label" aliasResourceName="mashupRepository"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.global.label" aliasResourceName="globalRepository"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.savedSearch.label" aliasResourceName="savedSearch"/>

    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.saveSearch.label" aliasResourceName="fm_saveSearch"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.search.label" aliasResourceName="fm_search"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.filters.label" aliasResourceName="fm_filters"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.mimes.label" aliasResourceName="fm_mimes"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.nodes.label" aliasResourceName="fm_nodes"/>

    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.information.label" aliasResourceName="fm_information"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.properties.label" aliasResourceName="fm_properties"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.roles.label" aliasResourceName="fm_roles"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.modes.label" aliasResourceName="fm_modes"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.authorizations.label" aliasResourceName="fm_authorizations"/>

    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.usages.label" aliasResourceName="fm_usages"/>

    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.info.name.label" aliasResourceName="fm_info_name"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.info.path.label" aliasResourceName="fm_info_path"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.info.size.label" aliasResourceName="fm_info_size"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.info.lastModif.label" aliasResourceName="fm_info_lastModif"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.info.lock.label" aliasResourceName="fm_info_lock"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.info.nbFiles.label" aliasResourceName="fm_info_nbFiles"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.info.nbFolders.label" aliasResourceName="fm_info_nbFolders"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.info.totalSize.label" aliasResourceName="fm_info_totalSize"/>

    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.properties.save.label" aliasResourceName="fm_save"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.properties.restore.label" aliasResourceName="fm_restore"/>

    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.usages.page.label" aliasResourceName="fm_page"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.usages.language.label" aliasResourceName="fm_language"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.usages.workflow.label" aliasResourceName="fm_workflow"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.usages.versioned.label" aliasResourceName="fm_versioned"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.usages.live.label" aliasResourceName="fm_live"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.usages.staging.label" aliasResourceName="fm_staging"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.usages.notify.label" aliasResourceName="fm_notify"/>

    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.uploadFile.label" aliasResourceName="fm_uploadFiles"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.autoUnzip.label" aliasResourceName="fm_autoUnzip"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.addFile.label" aliasResourceName="fm_addFile"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.cancel.label" aliasResourceName="fm_cancel"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.ok.label" aliasResourceName="fm_ok"/>

    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.thumbFilter.label" aliasResourceName="fm_thumbFilter"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.thumbSort.label" aliasResourceName="fm_thumbSort"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.thumbSortName.label" aliasResourceName="fm_thumbSortName"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.thumbSortSize.label" aliasResourceName="fm_thumbSortSize"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.thumbSortLastModif.label" aliasResourceName="fm_thumbSortLastModif"/>
</template:gwtJahiaModule>

<utility:gwtResourceBundleDictionary elementId="gwtacleditor" moduleType="gwtacleditor">
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.rights.ManageRights.principal.label" aliasResourceName="ae_principal"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.rights.ManageRights.restoreInheritance.label" aliasResourceName="ae_restore_inheritance"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.rights.ManageRights.inheritedFrom.label" aliasResourceName="ae_inherited_from"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.rights.ManageRights.restoreAllInheritance.label" aliasResourceName="ae_restore_all_inheritance"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.rights.ManageRights.breakAllInheritance.label" aliasResourceName="ae_break_all_inheritance"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.rights.ManageRights.remove.label" aliasResourceName="ae_remove"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.rights.ManageRights.save.label" aliasResourceName="ae_save"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.rights.ManageRights.restore.label" aliasResourceName="ae_restore"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.users.SelectUG_Engine.newUsers.label" aliasResourceName="um_adduser"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.users.SelectUG_Engine.newGroups.label" aliasResourceName="um_addgroup"/>
</utility:gwtResourceBundleDictionary>