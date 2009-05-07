<%--

    Jahia Enterprise Edition v6

    Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.

    Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
    with Document Management and Portal features.

    The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
    IMPLIED.

    Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
    you and Jahia (Jahia Sustainable Enterprise License - JSEL).

    If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.

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
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.newDirName.label"
                            aliasResourceName="fm_newdirname"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.newMashup.label"
                            aliasResourceName="fm_newmashup"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.newRssMashup.label"
                            aliasResourceName="fm_newrssmashup"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.newGoogleGadgetMashup.label"
                            aliasResourceName="fm_newgadgetmashup"/>
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
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.previewFile.label"
                            aliasResourceName="fm_preview"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.downloadMessage.label"
                            aliasResourceName="fm_downloadMessage"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.upload.label"
                            aliasResourceName="fm_upload"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.openIEfolder.label"
                            aliasResourceName="fm_webfolder"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.webFolderMessage.label"
                            aliasResourceName="fm_webfolderMessage"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.thumbs.label"
                            aliasResourceName="fm_thumbs"/>
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
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.rotateLeft.label"
                            aliasResourceName="fm_rotateLeft"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.rotateRight.label"
                            aliasResourceName="fm_rotateRight"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.refresh.label"
                            aliasResourceName="fm_refresh"/>

<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.statusbar.copying.label"
                            aliasResourceName="fm_copying"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.statusbar.cutting.label"
                            aliasResourceName="fm_cutting"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.statusbar.downloading.label"
                            aliasResourceName="fm_downloading"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.statusbar.locking.label"
                            aliasResourceName="fm_locking"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.statusbar.mounting.label"
                            aliasResourceName="fm_mounting"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.statusbar.newfoldering.label"
                            aliasResourceName="fm_newfoldering"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.statusbar.pasting.label"
                            aliasResourceName="fm_pasting"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.statusbar.removing.label"
                            aliasResourceName="fm_removing"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.statusbar.renaming.label"
                            aliasResourceName="fm_renaming"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.statusbar.unlocking.label"
                            aliasResourceName="fm_unlocking"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.statusbar.unmounting.label"
                            aliasResourceName="fm_unmounting"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.statusbar.unzipping.label"
                            aliasResourceName="fm_unzipping"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.statusbar.webfoldering.label"
                            aliasResourceName="fm_webfoldering"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.statusbar.zipping.label"
                            aliasResourceName="fm_zipping"/>

<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.confirm.archiveName.label"
                            aliasResourceName="fm_confArchiveName"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.confirm.multiRemove.label"
                            aliasResourceName="fm_confMultiRemove"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.confirm.newName.label"
                            aliasResourceName="fm_confNewName"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.confirm.overwrite.label"
                            aliasResourceName="fm_confOverwrite"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.confirm.remove.label"
                            aliasResourceName="fm_confRemove"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.confirm.unlock.label"
                            aliasResourceName="fm_confUnlock"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.confirm.unmount.label"
                            aliasResourceName="fm_confUnmount"/>

<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.failure.copy.label"
                            aliasResourceName="fm_failCopy"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.failure.crop.label"
                            aliasResourceName="fm_failCrop"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.failure.cut.label"
                            aliasResourceName="fm_failCut"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.failure.delete.label"
                            aliasResourceName="fm_failDelete"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.failure.download.label"
                            aliasResourceName="fm_failDownload"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.failure.mount.label"
                            aliasResourceName="fm_failMount"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.failure.newDir.label"
                            aliasResourceName="fm_failNewdir"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.failure.paste.label"
                            aliasResourceName="fm_failPaste"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.failure.rename.label"
                            aliasResourceName="fm_failRename"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.failure.resize.label"
                            aliasResourceName="fm_failResize"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.failure.rotate.label"
                            aliasResourceName="fm_failRotate"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.failure.unlock.label"
                            aliasResourceName="fm_failUnlock"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.failure.unmount.label"
                            aliasResourceName="fm_failUnmount"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.failure.unmountLock1.label"
                            aliasResourceName="fm_failUnmountLock1"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.failure.unmountLock2.label"
                            aliasResourceName="fm_failUnmountLock2"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.failure.unzip.label"
                            aliasResourceName="fm_failUnzip"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.failure.webfolder.label"
                            aliasResourceName="fm_failWebfolder"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.failure.zip.label"
                            aliasResourceName="fm_failZip"/>

<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.warning.lock.label"
                            aliasResourceName="fm_warningLock"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.warning.systemLock.label"
                            aliasResourceName="fm_warningSystemLock"/>

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
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.browse.label"
                            aliasResourceName="fm_browse"/>
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
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.properties.restore.label"
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
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.checkUploads.label"
                            aliasResourceName="fm_checkUploads"/>

<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.thumbFilter.label"
                            aliasResourceName="fm_thumbFilter"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.thumbSort.label"
                            aliasResourceName="fm_thumbSort"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.thumbSortName.label"
                            aliasResourceName="fm_thumbSortName"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.thumbSortSize.label"
                            aliasResourceName="fm_thumbSortSize"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.thumbSortLastModif.label"
                            aliasResourceName="fm_thumbSortLastModif"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.invertSort.label"
                            aliasResourceName="fm_invertSort"/>

<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.column.type.label"
                            aliasResourceName="fm_column_type"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.column.locked.label"
                            aliasResourceName="fm_column_locked"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.column.name.label"
                            aliasResourceName="fm_column_name"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.column.path.label"
                            aliasResourceName="fm_column_path"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.column.size.label"
                            aliasResourceName="fm_column_size"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.column.date.label"
                            aliasResourceName="fm_column_date"/>

<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.myRepository.label"
                            aliasResourceName="fm_repository_myRepository"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.myExternalRepository.label"
                            aliasResourceName="fm_repository_myExternalRepository"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.sharedRepository.label"
                            aliasResourceName="fm_repository_sharedRepository"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.websiteRepository.label"
                            aliasResourceName="fm_repository_websiteRepository"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.myMashupRepository.label"
                            aliasResourceName="fm_repository_myMashupRepository"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.sharedMashupRepository.label"
                            aliasResourceName="fm_repository_sharedMashupRepository"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.websiteMashupRepository.label"
                            aliasResourceName="fm_repository_websiteMashupRepository"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.globalRepository.label"
                            aliasResourceName="fm_repository_globalRepository"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.savedSearch.label"
                            aliasResourceName="fm_repository_savedSearch"/>

<internal:gwtResourceBundle resourceName="org.jahia.engines.rights.ManageRights.principal.label"
                            aliasResourceName="ae_principal"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.rights.ManageRights.restoreInheritance.label"
                            aliasResourceName="ae_restore_inheritance"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.rights.ManageRights.inheritedFrom.label"
                            aliasResourceName="ae_inherited_from"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.rights.ManageRights.inherited.label"
                            aliasResourceName="ae_inherited"/>
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

<internal:gwtResourceBundle resourceName="org.jahia.engines.MashupsManager.wizard.portletdef.label"
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

<internal:gwtResourceBundle resourceName="org.jahia.admin.components.ManageComponents.applicationName.label"
                            aliasResourceName="mw_name"/>
<internal:gwtResourceBundle resourceName="org.jahia.admin.components.ManageComponents.applicationDesc.label"
                            aliasResourceName="mw_description"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.MashupsManager.wizard.saveas.label"
                            aliasResourceName="mw_roles_perm_desc"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.MashupsManager.wizard.saveas.label"
                            aliasResourceName="mw_finish_description"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.MashupsManager.wizard.roles.any"
                            aliasResourceName="mw_no_role"/>



