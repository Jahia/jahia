<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<template:addResources type="css" resources="shortcuts-simple.css"/>
<template:addResources type="javascript" resources="textsizer.js"/>

<div class="shortcuts-simple">
    <ul>
        <c:if test="${renderContext.loggedIn}">
            <li class="shortcuts-login">
                <a href='${url.logout}'><span><fmt:message key="logout"/></span></a>
            </li>
            <li>
                <span class="currentUser"><utility:userProperty/></span>
            </li>
            <li class="shortcuts-mysettings">
                <a href="${url.base}${renderContext.site.path}/my-profile.html"><fmt:message key="userProfile.link"/></a>
            </li>
            <li class="shortcuts-edit">
                <a href="${url.edit}"><fmt:message key="edit"/></a>
            </li>
            <li class="shortcuts-contribute">
                <a href="${url.contribute}"><fmt:message key="contribute"/></a>
            </li>
        </c:if>
        <li class="shortcuts-print"><a href="base.wrapper.bodywrapper.jsp#"
                                          onclick="javascript:window.print()">
            <fmt:message key="print"/></a>
        </li>
        <li class="shortcuts-typoincrease">
            <a href="javascript:ts('body',1)"><fmt:message key="font.up"/></a>
        </li>
        <li class="shortcuts-typoreduce">
            <a href="javascript:ts('body',-1)"><fmt:message key="font.down"/></a>
        </li>
        <li class="shortcuts-home">
            <a href="${url.base}${rootPage.path}.html"><fmt:message key="home"/></a>
        </li>
        <li class="shortcuts-sitemap">
            <a href="${url.base}${renderContext.site.path}/home.sitemap.html"><fmt:message key="sitemap"/></a>
        </li>
    </ul>
</div>
