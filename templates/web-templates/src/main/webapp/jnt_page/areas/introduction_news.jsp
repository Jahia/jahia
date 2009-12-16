<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<template:area path="introduction" template="default"/>
<template:area path="news" template="default" areaType="jnt:contentList">
    <template:param name="forcedSubNodesTemplate" value="large" />
</template:area>