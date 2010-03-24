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
                        key="web_templates.logout"/></span></a>
            </li>
            <li>
                <span class="currentUser"><utility:userProperty/></span>
            </li>
            <li class="shortcuts-mysettings">
                <a href="${url.userProfile}"><fmt:message key="web_templates.mySettings.title"/></a>
            </li>
            <li class="shortcuts-edit">
                <a href="${url.edit}"><fmt:message key="edit"/></a>
            </li>
            <li class="shortcuts-edit">
                <a href="${url.contribute}"><fmt:message key="contribute"/></a>
            </li>
        </c:if>
        <li class="shortcuts-print"><a href="base.wrapper.bodywrapper.jsp#"
                                          onclick="javascript:window.print()">
            <fmt:message key="web_templates.print"/></a>
        </li>
        <li class="shortcuts-typoincrease">
            <a href="javascript:ts('body',1)"><fmt:message key="web_templates.up"/></a>
        </li>
        <li class="shortcuts-typoreduce">
            <a href="javascript:ts('body',-1)"><fmt:message key="web_templates.down"/></a>
        </li>
        <li class="shortcuts-home">
            <a href="${url.base}${rootPage.path}.html"><fmt:message key="web_templates.home"/></a>
        </li>
        <li class="shortcuts-sitemap">
            <a href="${url.base}${rootPage.path}.sitemap.html"><fmt:message
                    key="web_templates.sitemap"/></a>
        </li>
    </ul>
</div>
