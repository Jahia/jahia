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
<%@ include file="../../common/declarations.jspf" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jahia.engines.selectpage.SelectPage_Engine"%>
<%@ page import="org.jahia.params.ParamBean" %>
<%@ page import="org.jahia.registries.EnginesRegistry"%>

<template:jahiaPageForm name="formHandler" method="post">

    <fmt:message key='display.name'/>: <input type="text" name="Name"/><br/>
    <fmt:message key='display.adress'/>: <input type="text" name="Adress"/><br/>
    <fmt:message key='display.zip'/>: <input type="text" name="Zip"/><br/>
    <fmt:message key='display.city'/>: <input type="text" name="City"/><br/>
    <br/>
    <input type="hidden" name="storeContact" value="1"/>
    <input type="submit" value="<fmt:message key='display.store'/>"
           name="save"/>
    <br/>

    <template:containerList name="formHandlerCL" id="formHandlerCL" actionMenuNamePostFix="textContainers"
                            actionMenuNameLabelKey="textContainers.add">
        <template:formContentMapperHandler listName="formHandlerCL" submitMarker="storeContact"/>

        <template:container id="formC">
            Contact:<br/>
            <fmt:message key='display.name'/>: <template:field name="Name" inlineEditingActivated="false"/><br/>
            <fmt:message key='display.adress'/>: <template:field name="Adress" inlineEditingActivated="false"/><br/>
            <fmt:message key='display.zip'/>: <template:field name="Zip" inlineEditingActivated="false"/><br/>
            <fmt:message key='display.city'/>: <template:field name="City" inlineEditingActivated="false"/><br/><br/>


        </template:container>
    </template:containerList>

</template:jahiaPageForm>

<h2>Using FCKeditor</h2>
<%
    final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
    final Map params = new HashMap(4);
    params.put(SelectPage_Engine.OPERATION, "selectAnyPage");
    params.put(SelectPage_Engine.PARENT_PAGE_ID, new Integer(-1));
    params.put(SelectPage_Engine.PAGE_ID, new Integer(-1));
    params.put("callback","SetUrl");
    String selectPageURL = EnginesRegistry.getInstance().getEngineByBeanName("selectPageEngine").renderLink(jParams, params);
%>
<c:set var="fckUrl" value="${pageContext.request.contextPath}/htmleditors/fckeditor"/>
<script type="text/javascript" src="${fckUrl}/fckeditor.js"></script>
<script type="text/javascript">
    var oFCKeditor = null;

    window.onload = function() {
        oFCKeditor = new FCKeditor('mainContentBody', '100%', '150');
        oFCKeditor.BasePath = "${fckUrl}/";
        oFCKeditor.Config.basePath = "${fckUrl}/";
        
        oFCKeditor.Config.EditorAreaCSS = "${fckUrl}/editor/css/fck_editorarea.css";
        oFCKeditor.Config.StylesXmlPath = "${fckUrl}/fckstyles.xml";

        oFCKeditor.Config.ImageBrowserURL = "<c:url value='/engines/webdav/filePicker.jsp?callback=SetUrl&callbackType=url'><c:param name='filters' value='*.bmp,*.gif,*.jpe,*.jpeg,*.jpg,*.png,*.tif,*.tiff'/></c:url>";
        oFCKeditor.Config.FileBrowserURL = "<c:url value='/engines/webdav/filePicker.jsp?callback=SetUrl&callbackType=url'/>";
        oFCKeditor.Config.FlashBrowserURL = "<c:url value='/engines/webdav/filePicker.jsp?callback=SetUrl&callbackType=url&filters=*.swf'/>";
        oFCKeditor.Config.LinkBrowserURL = "<%=selectPageURL%>";
        
        oFCKeditor.Config["AutoDetectLanguage"] = false;
        oFCKeditor.Config["DefaultLanguage"] = "${currentRequest.paramBean.locale}";
        oFCKeditor.Config["CustomConfigurationsPath"] = "${fckUrl}/fckconfig_jahia.js"
        oFCKeditor.ToolbarSet = "Basic";
        oFCKeditor.ReplaceTextarea();
    }
</script>
<template:jahiaPageForm name="formHandler2" method="post">

    Title: <input type="text" name="mainContentTitle"/><br/>
    Title: <textarea name="mainContentBody" id="mainContentBody"></textarea><br/>
    <input type="hidden" name="storeParagraph" value="1"/>
    <input type="submit" value="Submit" name="save"/>
    <br/>

    <template:containerList name="paragraph" id="paragraph">
        <template:formContentMapperHandler listName="paragraph" submitMarker="storeParagraph"/>

        <template:container id="ctn">
            Paragraph:<br/>
            Title: <template:field name="mainContentTitle" inlineEditingActivated="false"/><br/>
            Content: <template:field name="mainContentBody" inlineEditingActivated="false"/><br/><br/>
        </template:container>
    </template:containerList>

</template:jahiaPageForm>