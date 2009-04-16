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
    in Jahia's FLOSS exception. You should have recieved a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ include file="../../common/declarations.jspf" %>
<c:set var="fckUrl" value="${pageContext.request.contextPath}/htmleditors/fckeditor"/>
<script type="text/javascript" src="${fckUrl}/fckeditor.js"></script>
<script type="text/javascript">
    var oFCKeditor = null;

    window.onload = function() {
        oFCKeditor = new FCKeditor('mainContentBody', '100%', '800');
        oFCKeditor.BasePath = "${fckUrl}/";
        oFCKeditor.Config.basePath = "${fckUrl}/";

        oFCKeditor.Config.EditorAreaCSS = "${fckUrl}/editor/css/fck_editorarea.css";
        oFCKeditor.Config.StylesXmlPath = "${fckUrl}/fckstyles.xml";

        oFCKeditor.Config.ImageBrowserURL = "<c:url value='/engines/webdav/filePicker.jsp?callback=SetUrl&callbackType=url'><c:param name='filters' value='*.bmp,*.gif,*.jpe,*.jpeg,*.jpg,*.png,*.tif,*.tiff'/></c:url>";
        oFCKeditor.Config.FileBrowserURL = "<c:url value='/engines/webdav/filePicker.jsp?callback=SetUrl&callbackType=url'/>";
        oFCKeditor.Config.FlashBrowserURL = "<c:url value='/engines/webdav/filePicker.jsp?callback=SetUrl&callbackType=url&filters=*.swf'/>";
        oFCKeditor.Config.LinkBrowserURL = "${currentPage.url}";

        oFCKeditor.Config["AutoDetectLanguage"] = false;
        oFCKeditor.Config["DefaultLanguage"] = "${currentRequest.paramBean.locale}";
        oFCKeditor.Config["CustomConfigurationsPath"] = "${fckUrl}/fckconfig_jahia.js"
        oFCKeditor.ToolbarSet = "Basic";
        //oFCKeditor.ReplaceTextarea();

    }
</script>
<div class ="formPreferences">
<template:containerList name="preferences" id="preferences" displayActionMenu="false">
    <template:container id="prefContainer" cache="off">
        <%@include file="editPreferences.jspf"%>
    </template:container>
    <c:if test="${preferences.size eq 0}">
        <%@include file="editPreferences.jspf"%>    
    </c:if>
</template:containerList>
</div>