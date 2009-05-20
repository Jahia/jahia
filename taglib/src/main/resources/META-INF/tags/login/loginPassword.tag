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
<%@ tag body-content="empty" description="Renders the entry field for allowing to enter the user's password." %>
<%@ tag dynamic-attributes="attributes"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="h" uri="http://www.jahia.org/tags/functions"%>
<c:if test="${!currentRequest.logged}">
  <c:set target="${attributes}" property="type" value="password"/>
  <c:set target="${attributes}" property="name" value="password"/>
  <c:set var="value" value="${h:default(param['username'], 'voyeur')}"/>
  <input ${h:attributes(attributes)} value="${fn:escapeXml(value)}" onfocus="this.value=''"/>
</c:if>  