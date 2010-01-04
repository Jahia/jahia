<%@ tag import="org.jahia.params.ProcessingContext" %>
<%@ tag import="org.jahia.registries.ServicesRegistry" %>
<%@ tag import="org.jahia.services.content.JCRNodeWrapper" %>
<%@ tag import="org.jahia.services.content.JCRStoreService" %>
<%@ tag import="javax.jcr.RepositoryException" %>
<%@ tag import="java.util.ArrayList" %>
<%@ tag import="java.util.Iterator" %>
<%@ tag import="org.jahia.services.content.JCRSessionFactory" %>
<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

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
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
<%@ taglib uri="http://www.jahia.org/tags/templateLib" prefix="template" %>
<%@ taglib prefix="internal" uri="http://www.jahia.org/tags/internalLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ attribute name="jahiaServletPath" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>
<%@ attribute name="jahiaContextPath" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>
<%@ attribute name="rootPath" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>
<%@ attribute name="startPath" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>
<%@ attribute name="enginemode" required="false" rtexprvalue="true" type="java.lang.Boolean" description="text" %>
<%@ attribute name="filters" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>
<%@ attribute name="nodeTypes" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>
<%@ attribute name="mimeTypes" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>
<%@ attribute name="conf" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>
<%@ attribute name="embedded" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>
<%@ attribute name="callback" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>
<%@ attribute name="selectedNodeUUIds" required="false" rtexprvalue="true" type="java.util.List" description="text" %>
<%@ attribute name="multiple" required="false" rtexprvalue="true" type="java.lang.Boolean" description="text" %>


<link rel="stylesheet" type="text/css" media="screen"
      href="<%= request.getContextPath() %>/engines/gwtfilemanager/javascript/uvumi-crop.css"/>
<style type="text/css">
    .yellowSelection {
        border: 2px dotted #FFB82F;
    }

    .blueMask {
        background-color: #00f;
        cursor: pointer;
    }
</style>

<script type="text/javascript"
        src="<%= request.getContextPath() %>/engines/gwtfilemanager/javascript/mootools-for-crop.js"></script>
<script type="text/javascript"
        src="<%= request.getContextPath() %>/engines/gwtfilemanager/javascript/UvumiCrop-compressed.js"></script>
<script type="text/javascript"> var crop = 0;     </script>
<%
    final ProcessingContext jParams = (ProcessingContext) request.getAttribute("org.jahia.params.ParamBean");
    final JCRSessionFactory sessionFactory = JCRSessionFactory.getInstance();

%>

<script type="text/javascript">
    var sContentNodes = [
        <%
if (multiple == null) {
    multiple = false;
}
if(selectedNodeUUIds == null){
 // case of single selection
 selectedNodeUUIds = new ArrayList<String>();
}

final Iterator selectedNodeIter = selectedNodeUUIds.iterator();
while (selectedNodeIter.hasNext()) {
   final String uuid = (String) selectedNodeIter.next();
   JCRNodeWrapper jcrNodeWrapper = null ;
   try {
        jcrNodeWrapper = sessionFactory.getCurrentUserSession().getNodeByUUID(uuid);
   } catch (RepositoryException e) {
        jcrNodeWrapper = null;
   }
   if (jcrNodeWrapper != null) {
        %>
        {
            uuid:"<%=jcrNodeWrapper.getUUID()%>",
            name:"<%=jcrNodeWrapper.getName()%>",
            displayName:"<%=jcrNodeWrapper.getName()%>",
            path:"<%=jcrNodeWrapper.getPath()%>"
        }<%if(selectedNodeIter.hasNext()){%>,
        <%}%>
        <%
     }

}%>
    ];
    var sLocale = '${locale}';

    var sAutoSelectParent = '${autoSelectParent}';

</script>
<template:gwtJahiaModule id="contentpicker" jahiaType="contentpicker"  jahiaServletPath="${fn:escapeXml(jahiaServletPath)}" jahiaContextPath="${fn:escapeXml(jahiaContextPath)}" rootPath="<%=rootPath%>"
                         startPath="<%=startPath%>"
                         nodeTypes="<%=nodeTypes%>" filters="<%=filters%>"
                         mimeTypes="${fn:escapeXml(mimeTypes)}" callback="${fn:escapeXml(callback)}" config="${fn:escapeXml(conf)}"
                         embedded="<%=embedded%>" multiple="<%=multiple%>"/>

<internal:gwtResourceBundle resourceName="toolbar.manager.button.createFolder"
                            aliasResourceName="fm_newdir"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.newDirName.label"
                            aliasResourceName="fm_newdirname"/>
<internal:gwtResourceBundle resourceName="toolbar.manager.button.newContent"
                            aliasResourceName="fm_newcontent"/>
<internal:gwtResourceBundle resourceName="toolbar.manager.button.newPage"
                            aliasResourceName="fm_newpage"/>
<internal:gwtResourceBundle resourceName="toolbar.manager.button.newMashup"
                            aliasResourceName="fm_newmashup"/>
<internal:gwtResourceBundle resourceName="toolbar.manager.button.newRssMashup"
                            aliasResourceName="fm_newrssmashup"/>
<internal:gwtResourceBundle resourceName="toolbar.manager.button.newGoogleGadgetMashup"
                            aliasResourceName="fm_newgadgetmashup"/>
<internal:gwtResourceBundle resourceName="toolbar.manager.button.copy"
                            aliasResourceName="fm_copy"/>
<internal:gwtResourceBundle resourceName="toolbar.manager.button.cut"
                            aliasResourceName="fm_cut"/>
<internal:gwtResourceBundle resourceName="toolbar.manager.button.paste"
                            aliasResourceName="fm_paste"/>
<internal:gwtResourceBundle resourceName="toolbar.manager.button.pasteReference"
                            aliasResourceName="fm_pasteref"/>
<internal:gwtResourceBundle resourceName="toolbar.manager.button.lock"
                            aliasResourceName="fm_lock"/>
<internal:gwtResourceBundle resourceName="toolbar.manager.button.unlock"
                            aliasResourceName="fm_unlock"/>
<internal:gwtResourceBundle resourceName="toolbar.manager.button.delete"
                            aliasResourceName="fm_remove"/>
<internal:gwtResourceBundle resourceName="toolbar.manager.button.rename"
                            aliasResourceName="fm_rename"/>
<internal:gwtResourceBundle resourceName="toolbar.manager.button.zip"
                            aliasResourceName="fm_zip"/>
<internal:gwtResourceBundle resourceName="toolbar.manager.button.unzip"
                            aliasResourceName="fm_unzip"/>
<internal:gwtResourceBundle resourceName="toolbar.manager.button.download"
                            aliasResourceName="fm_download"/>
<internal:gwtResourceBundle resourceName="toolbar.manager.button.preview"
                            aliasResourceName="fm_preview"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.downloadMessage.label"
                            aliasResourceName="fm_downloadMessage"/>
<internal:gwtResourceBundle resourceName="toolbar.manager.button.upload"
                            aliasResourceName="fm_upload"/>
<internal:gwtResourceBundle resourceName="toolbar.manager.button.openIEFolder"
                            aliasResourceName="fm_webfolder"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.webFolderMessage.label"
                            aliasResourceName="fm_webfolderMessage"/>
<internal:gwtResourceBundle resourceName="toolbar.manager.button.thumbs"
                            aliasResourceName="fm_thumbs"/>
<internal:gwtResourceBundle resourceName="toolbar.manager.button.icons.detailed"
                            aliasResourceName="fm_icons_detailed"/>
<internal:gwtResourceBundle resourceName="toolbar.manager.button.list"
                            aliasResourceName="fm_list"/>
<internal:gwtResourceBundle resourceName="toolbar.manager.button.crop"
                            aliasResourceName="fm_crop"/>
<internal:gwtResourceBundle resourceName="toolbar.manager.button.resize"
                            aliasResourceName="fm_resize"/>
<internal:gwtResourceBundle resourceName="toolbar.manager.button.rotate"
                            aliasResourceName="fm_rotate"/>
<internal:gwtResourceBundle resourceName="toolbar.manager.button.rotateLeft"
                            aliasResourceName="fm_rotateLeft"/>
<internal:gwtResourceBundle resourceName="toolbar.manager.button.rotateRight"
                            aliasResourceName="fm_rotateRight"/>
<internal:gwtResourceBundle resourceName="toolbar.manager.button.refresh"
                            aliasResourceName="fm_refresh"/>
<internal:gwtResourceBundle resourceName="toolbar.manager.button.newCategory"
                            aliasResourceName="fm_newcategory"/>

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
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.statusbar.pastingref.label"
                            aliasResourceName="fm_pastingref"/>
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
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.failure.pasteref.label"
                            aliasResourceName="fm_failPasteref"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.failure.rename.label"
                            aliasResourceName="fm_failRename"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.failure.resize.label"
                            aliasResourceName="fm_failResize"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.failure.rotate.label"
                            aliasResourceName="fm_failRotate"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.failure.saveSearch.label"
                            aliasResourceName="fm_failSaveSearch"/>
<internal:gwtResourceBundle
        resourceName="org.jahia.engines.filemanager.Filemanager_Engine.failure.inUseSaveSearch.label"
        aliasResourceName="fm_inUseSaveSearch"/>
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
<internal:gwtResourceBundle resourceName="toolbar.manager.button.rename"
                            aliasResourceName="fm_newname"/>
<internal:gwtResourceBundle resourceName="toolbar.manager.button.mount"
                            aliasResourceName="fm_mount"/>
<internal:gwtResourceBundle resourceName="toolbar.manager.button.unmount"
                            aliasResourceName="fm_unmount"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.mountpoint.label"
                            aliasResourceName="fm_mountpoint"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.mount.disclaimer.label"
                            aliasResourceName="fm_mountDisclaimerLabel"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.mount.disclaimer"
                            aliasResourceName="fm_mountDisclaimer"/>
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
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.saveSearchName.label"
                            aliasResourceName="fm_saveSearchName"/>
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
<internal:gwtResourceBundle resourceName="org.jahia.admin.components.ManageComponents.applicationsList.label"
                            aliasResourceName="fm_portlets"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.roles.label"
                            aliasResourceName="fm_roles"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.modes.label"
                            aliasResourceName="fm_modes"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.authorizations.label"
                            aliasResourceName="fm_authorizations"/>

<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.alreadyExists.label"
                            aliasResourceName="fm_alreadyExists"/>

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
<internal:gwtResourceBundle resourceName="org.jahia.engines.version.version"
                            aliasResourceName="fm_version"/>

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
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.column.provider.label"
                            aliasResourceName="fm_column_provider"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.column.created.label"
                            aliasResourceName="fm_column_created"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.column.createdBy.label"
                            aliasResourceName="fm_column_created_by"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.column.modified.label"
                            aliasResourceName="fm_column_modified"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.column.modifiedBy.label"
                            aliasResourceName="fm_column_modified_by"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.column.published.label"
                            aliasResourceName="fm_column_published"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.column.publishedBy.label"
                            aliasResourceName="fm_column_published_by"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.column.publicationInfo.label"
                            aliasResourceName="fm_column_publication_info"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.column.count.label"
                            aliasResourceName="fm_column_count"/>

<internal:gwtResourceBundle
        resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.myRepository.label"
        aliasResourceName="fm_repository_myRepository"/>
<internal:gwtResourceBundle
        resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.usersRepository.label"
        aliasResourceName="fm_repository_usersRepository"/>
<internal:gwtResourceBundle
        resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.myExternalRepository.label"
        aliasResourceName="fm_repository_myExternalRepository"/>
<internal:gwtResourceBundle
        resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.sharedRepository.label"
        aliasResourceName="fm_repository_sharedRepository"/>
<internal:gwtResourceBundle
        resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.websiteRepository.label"
        aliasResourceName="fm_repository_websiteRepository"/>
<internal:gwtResourceBundle
        resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.myMashupRepository.label"
        aliasResourceName="fm_repository_myMashupRepository"/>
<internal:gwtResourceBundle
        resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.sharedMashupRepository.label"
        aliasResourceName="fm_repository_sharedMashupRepository"/>
<internal:gwtResourceBundle
        resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.websiteMashupRepository.label"
        aliasResourceName="fm_repository_websiteMashupRepository"/>
<internal:gwtResourceBundle
        resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.siteRepository.label"
        aliasResourceName="fm_repository_siteRepository"/>
<internal:gwtResourceBundle
        resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.globalRepository.label"
        aliasResourceName="fm_repository_globalRepository"/>
<internal:gwtResourceBundle
        resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.categoryRepository.label"
        aliasResourceName="fm_repository_categoryRepository"/>
<internal:gwtResourceBundle
        resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.tagRepository.label"
        aliasResourceName="fm_repository_tagRepository"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.repository.savedSearch.label"
                            aliasResourceName="fm_repository_savedSearch"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.MashupsManager.wizard.portletdef.label"
                            aliasResourceName="fm_repository_portletDefinitionRepository"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.MashupsManager.wizard.portletdef.description.label"
                            aliasResourceName="fm_select_portlet"/>


<internal:gwtResourceBundle resourceName="org.jahia.admin.components.ManageComponents.portletReady.label"
                            aliasResourceName="fm_portlet_ready"/>
<internal:gwtResourceBundle resourceName="org.jahia.admin.components.ManageComponents.deployNewComponents.label"
                            aliasResourceName="fm_portlet_deploy"/>
<internal:gwtResourceBundle resourceName="org.jahia.admin.components.ManageComponents.deploy.preparewar.label"
                            aliasResourceName="fm_portlet_preparewar"/>


<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.login.label"
                            aliasResourceName="fm_login"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.logout.label"
                            aliasResourceName="fm_logout"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.username.label"
                            aliasResourceName="fm_username"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.password.label"
                            aliasResourceName="fm_password"/>
<internal:gwtResourceBundle resourceName="toolbar.manager.button.import"
                            aliasResourceName="fm_import"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.importfile.label"
                            aliasResourceName="fm_importfile"/>
<internal:gwtResourceBundle resourceName="toolbar.manager.button.export"
                            aliasResourceName="fm_export"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.exportlink.label"
                            aliasResourceName="fm_exportlink"/>

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
                            aliasResourceName="mw_roles_perm_desc"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.MashupsManager.wizard.finish.label"
                            aliasResourceName="mw_finish"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.MashupsManager.wizard.saveas.label"
                            aliasResourceName="mw_save_as"/>

<internal:gwtResourceBundle resourceName="org.jahia.admin.components.ManageComponents.applicationName.label"
                            aliasResourceName="mw_name"/>
<internal:gwtResourceBundle resourceName="org.jahia.admin.components.ManageComponents.applicationDesc.label"
                            aliasResourceName="mw_description"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.MashupsManager.wizard.saveas.label"
                            aliasResourceName="mw_finish_description"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.MashupsManager.wizard.roles.any"
                            aliasResourceName="mw_no_role"/>

<%-- common wizard --%>
<internal:gwtResourceBundle resourceName="org.jahia.engines.wizard.button.cancel"
                            aliasResourceName="wizard_button_cancel"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.wizard.button.finish"
                            aliasResourceName="wizard_button_finish"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.wizard.button.next"
                            aliasResourceName="wizard_button_next"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.wizard.button.prev"
                            aliasResourceName="wizard_button_prev"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.wizard.button.steps.of"
                            aliasResourceName="wizard_steps_of"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.wizard.button.steps.current"
                            aliasResourceName="wizard_steps_current"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.wizard.title"
                            aliasResourceName="wizard_header_title"/>

<%-- Add Content wizard --%>
<internal:gwtResourceBundle resourceName="org.jahia.engines.contentmanager.addContentWizard.column.label"
                            aliasResourceName="add_content_wizard_column_label"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.contentmanager.addContentWizard.column.name"
                            aliasResourceName="add_content_wizard_column_name"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.contentmanager.addContentWizard.defsCard.text"
                            aliasResourceName="add_content_wizard_card_defs_text"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.contentmanager.addContentWizard.defsCard.title"
                            aliasResourceName="add_content_wizard_card_defs_title"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.contentmanager.addContentWizard.formCard.error.props"
                            aliasResourceName="add_content_wizard_card_form_error_props"/>
<internal:gwtResourceBundle resourceName="org.jahia.admin.error.label"
                            aliasResourceName="add_content_wizard_card_form_error_title"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.contentmanager.addContentWizard.formCard.error.save"
                            aliasResourceName="add_content_wizard_card_form_error_save"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.contentmanager.addContentWizard.formCard.success"
                            aliasResourceName="add_content_wizard_card_form_success_title"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.contentmanager.addContentWizard.formCard.success.save"
                            aliasResourceName="add_content_wizard_card_form_success_save"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.contentmanager.addContentWizard.formCard.text"
                            aliasResourceName="add_content_wizard_card_form_text"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.contentmanager.addContentWizard.formCard.title"
                            aliasResourceName="add_content_wizard_card_form_title"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.contentmanager.addContentWizard.nameCard.nodeName"
                            aliasResourceName="add_content_wizard_card_name_node_name"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.contentmanager.addContentWizard.nameCard.nodeType"
                            aliasResourceName="add_content_wizard_card_name_node_type"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.contentmanager.addContentWizard.nameCard.text"
                            aliasResourceName="add_content_wizard_card_name_text"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.contentmanager.addContentWizard.nameCard.title"
                            aliasResourceName="add_content_wizard_card_name_title"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.contentmanager.addContentWizard.title"
                            aliasResourceName="add_content_wizard_title"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.contentmanager.addContentWizard.title"
                            aliasResourceName="add_content_wizard_title"/>

<internal:gwtResourceBundle resourceName="toolbar.manager.button.newMashup" aliasResourceName="mw_title"/>
