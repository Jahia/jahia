<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ page language="java" contentType="text/html;charset=UTF-8" %>

<jcr:node var="rootPage" path="/content/sites/${renderContext.site.siteKey}/home" />
<template:module path="sideMenu" />

<template:module path="columnB_box" template="default" autoCreateType="jnt:contentList" />