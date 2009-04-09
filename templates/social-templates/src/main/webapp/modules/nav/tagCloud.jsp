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
        ${tagCloudsString}
        <c:forTokens items="${tagCloudsString}" delims="," var="tag">
            <c:set var="count" value="0"/>${tag}:
            ${(fn:split(tagCloudsString,tag ))}
            <c:set var="replaceTag" value=",${tag},"/>
            <c:set var="tagCloudsString" value="${fn:replace(tagCloudsString,replaceTag,',')}"/>
            <li><a class="tag${count}0" href="${currentPage.url}?keyword=${tag}">${tag}</a></li><br/>
            ${tagCloudsString}<br/>
        </c:forTokens>
</ul>
</div>
    </template:containerList>
    </template:cache>


    	

