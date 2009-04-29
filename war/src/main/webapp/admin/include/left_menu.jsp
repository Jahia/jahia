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
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<utility:setBundle basename="JahiaInternalResources"/>
<div class="leftMenu">
    <ul>
        <li class="section first"><span>Main Menu</span></li>
    <c:forEach items="${param.mode == 'server' ? administrationServerModules : administrationSiteModules}" var="item">
        <li class="item">
            <fmt:message key="${item.label}" var="label"/>
            <c:set var="label" value="${fn:contains(label, '???') ? item.label : label}"/>
            <c:if test="${item.enabled}">
                <a href="${item.link}" ${fn:indexOf(item.link, 'http://') == 0 || fn:indexOf(item.link, 'https://') == 0 ? 'target="_blank"' : ''} class="set-${param.mode} ico-${param.mode}-${item.name}${item.selected ? ' selected' : ''}"><c:out value="${label}"/></a>
            </c:if>
            <c:if test="${not item.enabled}">
                <span class="set-${param.mode}-disabled ico-${param.mode}-${item.name}"><c:out value="${label}"/></span>
            </c:if>
        </li>
    </c:forEach>
    </ul>
</div>