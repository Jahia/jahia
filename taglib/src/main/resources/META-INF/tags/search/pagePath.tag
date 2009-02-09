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

<%@ tag body-content="empty" description="Renders page selection control." 
    import="javax.servlet.jsp.PageContext,
            org.jahia.data.beans.JahiaBean,
            org.jahia.params.ProcessingContext,
            org.jahia.services.pages.ContentPage" %>
<%@include file="declaration.tagf" %>
<%@ attribute name="value" required="false" type="java.lang.String" description="Initial value for the page path." %>
<%@ attribute name="includeChildren" required="false" type="java.lang.Boolean"
              description="Initial value for the include children field." %>
<c:set target="${attributes}" property="type" value="hidden"/>
<c:set target="${attributes}" property="name" value="src_pagePath.value"/>
<c:set target="${attributes}" property="id" value="src_pagePath.value"/>
<c:set var="value" value="${h:default(param['src_pagePath.value'], value)}"/>
<%-- by default set includeChildren to 'true' to search in subpages --%>
<c:set var="includeChildren" value="${not empty includeChildren ? includeChildren : 'true'}"/>
<%-- resolve includeChildren either from request parameter or from the default value (note that the 'false' value is not submitted for checkbox) --%>
<c:set var="includeChildren"
       value="${h:default(param['src_pagePath.includeChildren'], empty paramValues['src_pagePath.value'] ? includeChildren : 'false')}"/>
<input ${h:attributes(attributes)} value="${fn:escapeXml(value)}"/>
<c:if test="${display}">
    <c:set target="${attributes}" property="type" value="text"/>
    <c:set target="${attributes}" property="name" value="src_pagePath.valueView"/>
    <c:set target="${attributes}" property="id" value="src_pagePath.valueView"/>
    <c:if test="${not empty value}">
        <% String pageTitle = ContentPage.getPage(Integer.parseInt((String)jspContext.getAttribute("value")), false, false).getTitle(((JahiaBean)jspContext.getAttribute("jahia", PageContext.REQUEST_SCOPE)).getProcessingContext().getEntryLoadRequest(), false);
           if (pageTitle != null && pageTitle.length() > 0) {
               jspContext.setAttribute("pageTitle", pageTitle);
           } else { 
           %><c:set var="pageTitle"><utility:resourceBundle resourceBundle="JahiaEnginesResources" resourceName="org.jahia.engines.workflow.display.notitle" defaultValue="n.a."/></c:set><%
           }
        %>
    </c:if>
    <input ${h:attributes(attributes)} value="${fn:escapeXml(pageTitle)}"/>
    <ui:pageSelector fieldId="src_pagePath.value" fieldIdIncludeChildren="src_pagePath.includeChildren"
        onSelect="function (pid, url, title) { document.getElementById('src_pagePath.valueView').value=title; return true; }"/>
</c:if>
<c:if test="${!display && includeChildren}">
    <input type="hidden" name="src_pagePath.includeChildren" value="true"/>
</c:if>