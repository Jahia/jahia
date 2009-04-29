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
<%@ tag body-content="empty" dynamic-attributes="attributes"
        description="Renders file type selection control with all file type groups configured in the applicationcontext-basejahiaconfig.xml file." %>
<%@ tag import="org.jahia.services.content.JCRContentUtils" %>
<%@ taglib prefix="h" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ attribute name="value" required="false" type="java.lang.String" %>
<%@ attribute name="display" required="false" type="java.lang.Boolean"
              description="Should we display an input control for this query element or create a hidden one? In case of the hidden input field, the value should be provided."
        %>
<c:set var="display" value="${h:default(display, true)}"/>
<c:set var="fileTypes" value="<%= JCRContentUtils.getInstance().getMimeTypes() %>"/>
<c:if test="${not empty value}">
    <% if (!JCRContentUtils.getInstance().getMimeTypes().containsKey(jspContext.getAttribute("value"))) {
        throw new IllegalArgumentException("Unsupported file type '" + jspContext.getAttribute("value") + "'. See applicationcontext-basejahiaconfig.xml file for configured file types.");
    } %>
</c:if>
<c:set var="value" value="${h:default(param.src_fileType, value)}"/>
<c:if test="${display}">
    <c:set target="${attributes}" property="name" value="src_fileType"/>
    <select ${h:attributes(attributes)}>
        <option value=""><fmt:message key="searchForm.any"/></option>
        <c:forEach items="${fileTypes}" var="type">
            <option value="${type.key}" ${value == type.key ? 'selected="selected"' : ''}><fmt:message key="searchForm.fileType.${type.key}"/></option>
        </c:forEach>
    </select>
</c:if>
<c:if test="${!display}"><input type="hidden" name="src_fileType" value="${value}"/></c:if>