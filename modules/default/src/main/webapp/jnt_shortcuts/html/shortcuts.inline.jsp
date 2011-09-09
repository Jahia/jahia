<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<template:addResources type="css" resources="shortcuts-inline.css"/>
<template:addResources type="javascript" resources="textsizer.js"/>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="currentUser" type="org.jahia.services.usermanager.JahiaUser"--%>
<%--@elvariable id="currentAliasUser" type="org.jahia.services.usermanager.JahiaUser"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<div class="shortcuts-inline">
    <ul>
        <c:if test="${renderContext.loggedIn}">
            <li>
                <span class="currentUser"><fmt:message key="welcome"/>, ${functions:fullName(currentUser)}<c:if test="${not empty currentAliasUser}">&nbsp;(&nbsp;<fmt:message key="as.user"/>&nbsp;${functions:displayName(currentAliasUser)}&nbsp;)</c:if>:</span>
            </li>
            <li class="shortcuts-login">
                <a href='<c:url value="${url.logout}"/>'><span><fmt:message key="logout"/></span></a>
            </li>
            <c:if test="${!empty url.myProfile}">
                <li class="shortcuts-mysettings">
                    <a href="<c:url value='${url.myProfile}'/>"><fmt:message key="mySpace.link"/></a>
                </li>
            </c:if>
<%--
            <c:if test="${jcr:hasPermission(renderContext.mainResource.node, 'editModeAccess')}">
                <li class="shortcuts-edit">
                    <a href="<c:url value='${url.edit}'/>"><fmt:message key="edit"/></a>
                </li>
            </c:if>
            <c:if test="${jcr:hasPermission(renderContext.mainResource.node, 'contributeModeAccess')}">
                <li class="shortcuts-contribute">
                    <a href="<c:url value='${url.contribute}'/>"><fmt:message key="contribute"/></a>
                </li>
            </c:if>
--%>
        </c:if>
        <li class="shortcuts-print"><a href="#"
                                       onclick="javascript:window.print(); return false">
            <fmt:message key="print"/></a>
        </li>
        <li class="shortcuts-typoincrease">
            <a href="javascript:ts('body',1)"><fmt:message key="font.up"/></a>
        </li>
        <li class="shortcuts-typoreduce">
            <a href="javascript:ts('body',-1)"><fmt:message key="font.down"/></a>
        </li>
<%-- <li class="shortcuts-home">
            <a href="<c:url value='${url.base}${renderContext.site.path}/home.html'/>"><fmt:message key="home"/></a>
        </li>
        <li class="shortcuts-sitemap">
            <a href="<c:url value='${url.base}${renderContext.site.path}/home.sitemap.html'/>"><fmt:message key="sitemap"/></a>
        </li>--%>
    </ul>
</div>
