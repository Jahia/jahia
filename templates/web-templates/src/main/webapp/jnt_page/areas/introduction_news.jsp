<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<template:module path="introduction" template="default"/>
<template:module path="news" template="default" autoCreateType="jnt:contentList"/>
<form action="${url.base}${currentNode.path}/news/*" method="post">
    <input type="hidden" name="nodeType" value="web_templates:newsContainer"/>
    <input type="text" name="newsTitle" value="News Title"/><br/>
    <textarea rows="10" cols="80" name="newsDesc">News content</textarea><br/>
    <input type="submit"/>
</form>
