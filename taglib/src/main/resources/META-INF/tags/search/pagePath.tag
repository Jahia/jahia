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
<%@ tag body-content="empty" description="Renders page selection control." 
    import="javax.servlet.jsp.PageContext,
            org.jahia.data.beans.JahiaBean,
            org.jahia.services.pages.ContentPage" %>
<%@ tag dynamic-attributes="attributes"%>
<%@ attribute name="display" required="false" type="java.lang.Boolean"
              description="Should we display an input control for this query element or create a hidden one? In case of the hidden input field, the value should be provided."
        %>
<%@ attribute name="value" required="false" type="java.lang.String" description="Initial value for the page path." %>
<%@ attribute name="includeChildren" required="false" type="java.lang.Boolean"
              description="Initial value for the include children field." %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="h" uri="http://www.jahia.org/tags/functions"%>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<c:set var="display" value="${h:default(display, true)}"/>
<c:set var="givenId" value="${attributes.id}"/>
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
    <c:set target="${attributes}" property="id" value="${h:default(givenId, 'src_pagePath.valueView')}"/>
    <c:if test="${not empty value}">
        <% String pageTitle = ContentPage.getPage(Integer.parseInt((String)jspContext.getAttribute("value")), false, false).getTitle(((JahiaBean)jspContext.getAttribute("jahia", PageContext.REQUEST_SCOPE)).getProcessingContext().getEntryLoadRequest(), false);
           if (pageTitle != null && pageTitle.length() > 0) {
               jspContext.setAttribute("pageTitle", pageTitle);
           } else { 
           %><c:set var="pageTitle"><fmt:message key="searchForm.pagePicker.noTitle"/></c:set><%
           }
        %>
    </c:if>
    <input ${h:attributes(attributes)} value="${fn:escapeXml(pageTitle)}"/>
    <ui:pageSelector fieldId="src_pagePath.value" fieldIdIncludeChildren="src_pagePath.includeChildren"
        onSelect="function (pid, url, title) { document.getElementById('${attributes.id}').value=title; return true; }"/>
</c:if>
<c:if test="${!display && includeChildren}">
    <input type="hidden" name="src_pagePath.includeChildren" value="true"/>
</c:if>