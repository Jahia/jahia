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
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ page import="org.jahia.data.fields.JahiaField" %>
<%@ page import="org.jahia.data.files.JahiaFileField" %>
<%@ page import="org.jahia.engines.EngineLanguageHelper" %>
<%@ page import="org.jahia.engines.JahiaEngine" %>
<%@ page import="org.jahia.params.ProcessingContext"%>
<%@ page import="java.util.*" %>
<%@ page import="org.jahia.utils.FileUtils" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%
    final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
    final ProcessingContext jParams = (ProcessingContext) request.getAttribute("org.jahia.params.ParamBean");
    EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
    if (elh != null) {
        jParams.setCurrentLocale(elh.getCurrentLocale());
    }
    final String fieldsEditCallingEngineName = (String) engineMap.get("fieldsEditCallingEngineName");
    final JahiaField theField = (JahiaField) engineMap.get(fieldsEditCallingEngineName + "." + "theField");
    final JahiaFileField fField = (JahiaFileField) theField.getObject();
    final String url;
    if (fField == null) {
        url = null;
    } else {
        final String tmp = fField.getRealName();
        if (tmp == null || tmp.trim().length() == 0) {
            url = null;
        } else {
            url = fField.getDownloadUrl();
        }
    }
%>
<utility:setBundle basename="JahiaInternalResources"/>
<fmt:message key="org.jahia.engines.shared.File_Field.file.label"/>&nbsp;:&nbsp;
<% if (fField == null || url == null) { %>
<fmt:message key="org.jahia.engines.shared.File_Field.none.label"/>
<% } else {

    if (fField.isImage()) { %>
<%=fField.getTitle()%>
<br/>
<fmt:message key="org.jahia.engines.filemanager.Filemanager_Engine.path.label"/>:
<%=fField.getRealName()%>
<br/>&nbsp;<br/>
<img src="<%=url%>" alt="<%=fField.getTitle()%>" border="0"/>
<% } else { %>
<%
    final String ext = FileUtils.getFileIcon(url);
%>
<img alt="<%=ext%>" src="${pageContext.request.contextPath}/engines/images/types/<%=ext%>.gif"/>
<a href="<%=url%>" target="_blank"><%=fField.getTitle()%></a><br/>
<fmt:message key="org.jahia.engines.filemanager.Filemanager_Engine.path.label"/>:
<%=fField.getRealName()%>
<% } %>
<% } %>

