<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<template:addResources type="css" resources="shortcuts-inline.css"/>

<div class="shortcuts-inline">
    <ul>
        <c:if test="${renderContext.loggedIn}">
            <li class="shortcuts-login">
                <a class="loginFormTopLogoutShortcuts"
                   href='${url.logout}'><span><fmt:message
                        key="logout"/></span></a>
            </li>
            <li>
                <span class="currentUser"><utility:userProperty/></span>
            </li>
            <li>
                <a href="${url.userProfile}"><fmt:message key="userProfile.link"/></a>
            </li>
            <li>
                <a href="${url.edit}"><fmt:message key="edit"/></a>
            </li>
            <li>
                <a href="${url.contribute}"><fmt:message key="contribute"/></a>
            </li>
        </c:if>
        <li><a href="base.wrapper.bodywrapper.jsp#"
                                          onclick="javascript:window.print()">
            <fmt:message key="print"/></a>
        </li>
        <li>
            <a href="javascript:ts('body',1)"><fmt:message key="font.up"/></a>
        </li>
        <li class="shortcuts-typoreduce">
            <a href="javascript:ts('body',-1)"><fmt:message key="font.down"/></a>
        </li>
        <li>
            <a href="${url.base}${rootPage.path}.html"><fmt:message key="home"/></a>
        </li>
        <li class="shortcuts-sitemap">
            <a href="${url.base}${rootPage.path}.sitemap.html"><fmt:message
                    key="sitemap"/></a>
        </li>
    </ul>
</div>
