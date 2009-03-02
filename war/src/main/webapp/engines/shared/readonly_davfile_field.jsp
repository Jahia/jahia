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
<utility:setBundle basename="JahiaInternalResources"/>
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

