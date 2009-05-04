<%@ include file="../../common/declarations.jspf" %>
<%--

    Jahia Enterprise Edition v6

    Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.

    Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
    with Document Management and Portal features.

    The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
    IMPLIED.

    Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
    you and Jahia (Jahia Sustainable Enterprise License - JSEL).

    If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.

--%>
<template:cache cacheKey="tagCloudCache">

    <template:containerList name="blogEntries" id="tagClouds" displayExtensions="false" displayActionMenu="false">
        <template:addDependency bean="${tagClouds}"/>
        <div class="tags">
            <h3>Tags</h3>

            <ul>
                <c:set var="tagCloudsString" value=""/>
                <template:container cacheKey="tagClouds" cache="off" id="tagCloud" displayActionMenu="false"
                                    displayExtensions="false">
                    <template:addDependency bean="${tagCloud}"/>
                    <template:metadata metadataName="keywords" contentBean="${tagCloud}" var="keywords"/>
                    <c:forTokens items="${keywords}" delims="," var="keyword">
                        <c:set var="tagCloudsString" value="${tagCloudsString},${fn:trim(keyword)}"/>
                    </c:forTokens>

                </template:container>
                <c:set var="listAllTags" value="${tagCloudsString}"/>
                <c:set var="listTags" value="${functions:removeDuplicates(tagCloudsString, ',')}"/>
                <c:forTokens items="${listTags}" delims="," var="tag">
                    <li><a class="tag${functions:countOccurences(tagCloudsString, tag)}0"
                           href="${currentPage.url}?keyword=${tag}">${tag}</a></li>
                </c:forTokens>
            </ul>
        </div>
    </template:containerList>
</template:cache>


    	

