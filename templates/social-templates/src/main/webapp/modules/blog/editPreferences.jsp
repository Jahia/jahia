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
<%@ include file="../../common/declarations.jspf" %>
<c:if test="${requestScope.currentRequest.hasWriteAccess}">
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
</c:if>
<c:if test="${!requestScope.currentRequest.hasWriteAccess}">
    <fmt:message key="blog.noAccess"/>
</c:if>