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
            <c:if test="${not empty item.iconSmall}" var="smallIconAvailable">
                <c:if test="${fn:contains(item.iconSmall, '/') || fn:contains(item.iconSmall, '.')}" var="externalIcon">
                    <c:set var="iconUrl" value="${item.iconSmall}"/>
                    <c:set var="iconUrlDisabled" value="${item.iconSmall}"/>
                </c:if>
                <c:if test="${!externalIcon}">
                    <c:set var="iconUrl">${pageContext.request.contextPath}/engines/images/icons/admin/adromeda/${item.iconSmall}.png</c:set>
                    <c:set var="iconUrlDisabled">${pageContext.request.contextPath}/engines/images/icons/admin/adromeda/${item.iconSmall}_grey.png</c:set>
                </c:if>
            </c:if>
            <c:if test="${item.enabled}">
                <c:if test="${smallIconAvailable}">
                    <a href="${item.link}" ${fn:indexOf(item.link, 'http://') == 0 || fn:indexOf(item.link, 'https://') == 0 ? 'target="_blank"' : ''} class="${item.selected ? ' selected' : ''}" style="background: url(${iconUrl}) no-repeat; background-position: 2px 2px;"><c:out value="${label}"/></a>
                </c:if>
                <c:if test="${!smallIconAvailable}">
                    <a href="${item.link}" ${fn:indexOf(item.link, 'http://') == 0 || fn:indexOf(item.link, 'https://') == 0 ? 'target="_blank"' : ''} class="set-${param.mode} ico-${param.mode}-${item.name}${item.selected ? ' selected' : ''}"><c:out value="${label}"/></a>
                </c:if>
            </c:if>
            <c:if test="${not item.enabled}">
                <c:if test="${smallIconAvailable}">
                    <span style="background: url(${iconUrlDisabled}) no-repeat; background-position: 2px 2px;"><c:out value="${label}"/></span>
                </c:if>
                <c:if test="${!smallIconAvailable}">
                    <span class="set-${param.mode}-disabled ico-${param.mode}-${item.name}"><c:out value="${label}"/></span>
                </c:if>
            </c:if>
        </li>
    </c:forEach>
    </ul>
</div>