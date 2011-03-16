<%@ page contentType="text/html; UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<template:addResources type="css" resources="wiki.css"/>

<div class="wiki">
    <h2>${currentNode.properties["jcr:title"].string}</h2>
    <template:module node="${currentNode}" view="syntax"/>
    <div class="clear"></div>
</div>

