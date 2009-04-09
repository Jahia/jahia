<%@ include file="../../common/declarations.jspf" %>
<template:cache cacheKey="tagCloudCache">

    <template:containerList name="blogEntries" id="tagClouds" displayExtensions="false" displayActionMenu="false">
    <template:addDependency bean="${tagClouds}"/>
<div class="tags">
<h3>Tags</h3>

	<ul>
        <c:set var="tagCloudsString" value=""/>
    <template:container cacheKey="tagClouds" cache="off" id="tagCloud" displayActionMenu="false" displayExtensions="false">
         <template:addDependency bean="${tagCloud}"/>
        <template:metadata metadataName="keywords" contentBean="${tagCloud}" var="keywords"/>
        <c:set var="tagCloudsString" value="${tagCloudsString},${keywords}"/>
        </template:container>
        <c:set var="listAllTags" value="${tagCloudsString}"/>
        <c:set var="listTags" value="${functions:removeDupilcates(tagCloudsString, ',')}"/>
        <c:forTokens items="${listTags}" delims="," var="tag">
            <li><a class="tag${functions:countOccurences(tagCloudsString, tag)}0" href="${currentPage.url}?keyword=${tag}">${tag}</a></li>
        </c:forTokens>
 </ul>
</div>
    </template:containerList>
    </template:cache>


    	

