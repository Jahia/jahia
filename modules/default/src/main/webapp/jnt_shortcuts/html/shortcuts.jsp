<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<template:addResources type="javascript" resources="jquery.min.js, textsizer.js"/>

<!-- shortcuts -->
<script type="text/javascript">
    $(document).ready(function() {
        $('#shortcuts').children('ul').hide();
        $('#shortcuts').mouseover(function() {
            $(this).children('ul').show();
        }).mouseout(function() {
            $(this).children('ul').hide();
        });
    });
</script>
<div id="shortcuts">
    <h3><a title="Shortcuts" href="#">Shortcuts</a></h3>
    <ul>
        <c:if test="${renderContext.loggedIn}">
            <li>
                <a class="loginFormTopLogoutShortcuts"
                   href='${url.logout}'><span><fmt:message
                        key="logout"/></span></a>
            </li>
            <li>
                <span class="currentUser"><utility:userProperty/></span>
            </li>
            <li class="topshortcuts-mysettings">
                <a href="${url.base}${renderContext.site.path}/my-profile.html"><fmt:message key="userProfile.link"/></a>
            </li>
            <li>
                <a href="${url.edit}"><fmt:message key="edit"/></a>
            </li>
            <li>
                <a href="${url.contribute}"><fmt:message key="contribute"/></a>
            </li>
        </c:if>
        <li><a href="#"
                                          onclick="javascript:window.print();return false">
            <fmt:message key="print"/></a>
        </li>
        <li>
            <a href="javascript:ts('body',1)"><fmt:message key="font.up"/></a>
        </li>
        <li>
            <a href="javascript:ts('body',-1)"><fmt:message key="font.down"/></a>
        </li>
        <li>
            <a href="${url.base}${renderContext.site.path}/home.html"><fmt:message key="home"/></a>
        </li>
        <li>
            <a href="${url.base}${renderContext.site.path}/home.sitemap.html"><fmt:message key="sitemap"/></a>
        </li>
    </ul>
</div>
