<%@ page contentType="text/html; UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<template:addResources type="css" resources="wiki.css"/>

<div class="wiki">
    <template:module node="${currentNode}" forcedTemplate="syntax"/>
    <div class="bottomanchor">
        <a href="wikiPage.jsp#bodywrapper"><fmt:message key="pageTop"/></a>
    </div>
    <div class="clear"></div>
</div>

