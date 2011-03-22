<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="propertyDefinition" type="org.jahia.services.content.nodetypes.ExtendedPropertyDefinition"--%>
<%--@elvariable id="type" type="org.jahia.services.content.nodetypes.ExtendedNodeType"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="css" resources="wiki.css"/>

<div id="${currentNode.UUID}-history">
    <h2>${currentNode.properties["jcr:title"].string}</h2>
    <form name="diff" method="get" >
        <c:forEach items="${param}" var="p">
            <c:if test="${p.key ne 'diff' and p.key ne 'oldid'}">
                <input type="hidden" name="${p.key}" value="${p.value}"/>
            </c:if>
        </c:forEach>

        <h4 class="boxwiki-title"><fmt:message key="jnt_wiki.pageHistoryTitle"/></h4>
        <table width="100%" class="table tablecompare " summary="Table Compare">
            <caption class=" hidden">
                <fmt:message key="jnt_wiki.pageHistoryTitle"/>
            </caption>
            <colgroup>
                <col span="1" width="10%" class="col1"/>
                <col span="1" width="45%" class="col2"/>
                <col span="1" width="15%" class="col3"/>
                <col span="1" width="30%" class="col4"/>
            </colgroup>
            <thead>
            <tr>
                <th class="center" id="Selection" scope="col"><fmt:message
                        key="jnt_wiki.label.select"/></th>
                <th id="Title" scope="col"><fmt:message key="jnt_wiki.label.title"/></th>
                <th id="Author" scope="col"><fmt:message key="jnt_wiki.label.author"/></th>
                <th id="Date" scope="col"><fmt:message key="jnt_wiki.label.date"/></th>
            </tr>
            </thead>

            <tbody>
            <c:set var="result" value="${currentNode.versionHistory.allLinearFrozenNodes}"/>
            <c:set var="currentList" value="${result}" scope="request"/>
            <c:set var="listTotalSize" value="${functions:length(result)}" scope="request"/>
            <template:initPager totalSize="${listTotalSize}" pageSize="25" id="${currentNode.identifier}"/>
            <c:forEach items="${currentList}" var="version" varStatus="status" begin="${moduleMap.begin}" end="${moduleMap.end}" >
                <c:choose>
                    <c:when test="${status.count % 2 == 0}">
                        <tr class="odd">
                    </c:when>
                    <c:otherwise>
                        <tr class="even">
                    </c:otherwise>
                </c:choose>

                <td class="center" headers="Selection">
                    <input type="radio" value="${version.parent.name}" name="oldid"
                           id="w-oldid-${version.parent.name}" <c:if test="${status.last}">disabled="true"</c:if> <c:if test="${param.oldid eq version.parent.name}">checked="true"</c:if>/>
                    &nbsp;<input type="radio" value="${version.parent.name}" name="diff"
                           id="w-diff-${version.parent.name}" <c:if test="${status.first}">disabled="true"</c:if> <c:if test="${param.diff eq version.parent.name}">checked="true"</c:if>/>
                </td>
                <td headers="Title"><a href="#">${version.properties['lastComment'].string} </a></td>
                <td headers="Author">${version.properties['jcr:lastModifiedBy'].string}</td>
                <td headers="Date"><fmt:formatDate
                        value="${version.properties['jcr:lastModified'].date.time}"
                        dateStyle="short" type="both"/></td>
                </tr>
            </c:forEach>
            </tbody>
        </table>

        <div class="divButton">
            <a class="aButton" href="javascript:document.forms['diff'].submit();"><span><fmt:message
                    key="jnt_wiki.buttonLabel.compare"/></span></a>

            <div class="clear"></div>
        </div>
        <template:displayPagination/>
        <!--stop pagination-->
        <template:removePager id="${currentNode.identifier}"/>
    </form>

</div>
<!--stop boxwiki -->
