<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<utility:setBundle basename="JahiaInternalResources" useUILocale="true"/>
<div class="leftMenu">
    <ul>
        <li class="section first"><span>Main Menu</span></li>
    <c:forEach items="${param.mode == 'server' ? administrationServerModules : administrationSiteModules}" var="item">
        <li class="item">
            <fmt:message key="${item.label}" var="label"/>
            <c:if test="${fn:contains(label, '???')}">
                <fmt:message key="${item.label}" var="label" bundle="${item.localizationContext}"/>
            </c:if>
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
                    <a href="${item.link}" ${fn:indexOf(item.link, 'http://') == 0 || fn:indexOf(item.link, 'https://') == 0 ? 'target="_blank"' : ''} class="${item.selected ? ' selected' : ''}" style="background: ${item.selected ? '#DBEDF4' : ''} url(${iconUrl}) no-repeat; background-position: 2px 2px;"><c:out value="${label}"/></a>
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