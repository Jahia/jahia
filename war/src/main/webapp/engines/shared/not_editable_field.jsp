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
<%@ page language="java" %>
<%@ page import="org.jahia.data.fields.JahiaField" %>
<%@ page import="java.util.*" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<p class="error">
    <fmt:message key="org.jahia.engines.shared.fileNotModified.label"/>
</p>

<%
	final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
	final String fieldsEditCallingEngineName = (String) engineMap.get("fieldsEditCallingEngineName");
	final JahiaField theField = (JahiaField) engineMap.get(fieldsEditCallingEngineName + ".theField");
%>
<table border="0" cellpadding="0" cellspacing="0" width="90%">
<tr>
	<td class="text" align="left" nowrap>
<fmt:message key="org.jahia.engines.value.label"/> :
</td>
</tr>
<tr>
	<td class="text" align="left" nowrap>
		<%= theField.getValue()%>
</td>
</tr>
</table>