<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<jcr:nodeProperty node="${currentNode}" name="j:theme" var="theme"/>
<template:addResources type="css" resources="${theme.string}.css" nodetype="jmix:theme"/>
