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

<%@ tag body-content="empty" dynamic-attributes="attributes"
        description="Renders file type selection control with all file type groups configured in the applicationcontext-basejahiaconfig.xml file." %>
<%@ tag import="org.jahia.services.content.JCRContentUtils" %>
<%@ taglib prefix="h" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
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
        <option value=""><utility:resourceBundle resourceName="searchForm.any" defaultValue="any"/></option>
        <c:forEach items="${fileTypes}" var="type">
            <option value="${type.key}" ${value == type.key ? 'selected="selected"' : ''}><utility:resourceBundle resourceName="searchForm.fileType.${type.key}" defaultValue="${type.key}"/></option>
        </c:forEach>
    </select>
</c:if>
<c:if test="${!display}"><input type="hidden" name="src_fileType" value="${value}"/></c:if>