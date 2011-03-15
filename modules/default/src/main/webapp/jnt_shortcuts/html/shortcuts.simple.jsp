<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<template:addResources type="css" resources="shortcuts-simple.css"/>
<template:addResources type="javascript" resources="textsizer.js"/>

<div class="shortcuts-simple">
    <ul>
        <c:if test="${renderContext.loggedIn}">
            <li class="shortcuts-login">
                <a href='${url.logout}'><span><fmt:message key="logout"/></span></a>
            </li>
            <li>
                <span class="currentUser"><c:choose><c:when test="${not empty currentUser.properties['j:firstName']}">${currentUser.properties['j:firstName']} ${currentUser.properties['j:lastName']}</c:when><c:otherwise>${currentUser.username}</c:otherwise></c:choose><c:if test="${not empty currentAliasUser}">(&nbsp;<fmt:message key="as.user"/>&nbsp;${currentAliasUser.username})</c:if></span>
            </li>
            <c:if test="${!empty url.myProfile}">
                <li class="shortcuts-mysettings">
                    <a href="<c:url value='${url.base}${url.myProfile}'/>"><fmt:message key="userProfile.link"/></a>
                </li>
            </c:if>
            <c:if test="${jcr:hasPermission(renderContext.mainResource.node, 'editModeAccess')}">
                <li class="shortcuts-edit">
                    <a href="${url.edit}"><fmt:message key="edit"/></a>
                </li>
            </c:if>
            <c:if test="${jcr:hasPermission(renderContext.mainResource.node, 'contributeModeAccess')}">
                <li class="shortcuts-contribute">
                    <a href="${url.contribute}"><fmt:message key="contribute"/></a>
                </li>
            </c:if>
        </c:if>
        <li class="shortcuts-print"><a href="#"
                                       onclick="javascript:window.print();return false">
            <fmt:message key="print"/></a>
        </li>
        <li class="shortcuts-typoincrease">
            <a href="javascript:ts('body',1)"><fmt:message key="font.up"/></a>
        </li>
        <li class="shortcuts-typoreduce">
            <a href="javascript:ts('body',-1)"><fmt:message key="font.down"/></a>
        </li>
<%--<li class="shortcuts-home">
            <a href="<c:url value='${url.base}${rootPage.path}.html'/>"><fmt:message key="home"/></a>
        </li>
        <li class="shortcuts-sitemap">
            <a href="<c:url value='${url.base}${renderContext.site.path}/home.sitemap.html'/>"><fmt:message key="sitemap"/></a>
        </li>--%>
    </ul>
</div>
