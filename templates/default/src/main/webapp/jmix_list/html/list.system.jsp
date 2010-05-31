<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="workflow" uri="http://www.jahia.org/tags/workflow" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<template:addResources type="css" resources="960-fluid-admin-jahia.css"/>
<template:addResources type="css" resources="jahia-admin.css"/>

<template:include template="hidden.header"/>

<table width="100%" cellspacing="0" cellpadding="5" border="0" class="evenOddTable">
    <thead>
    <tr>
        <th width="5%" align="center">
            <c:if test="${jcr:isNodeType(currentNode.parent,'jmix:list')}">
                <a title="parent" href="${url.base}${currentNode.parent.path}.system.html"><img height="16" width="16" border="0" style="cursor: pointer;" title="parent" alt="parent" src="${url.currentModule}/images/icons/folder_up.png"></a>
            </c:if>
        </th>
        <th width="5%"><fmt:message key="label.type"/></th>
        <th width="35%"><fmt:message key="label.title"/></th>
        <th width="10%" style="white-space: nowrap;"><fmt:message key="jmix_contentmetadata.j_creationDate"/></th>
        <th width="10%" style="white-space: nowrap;"><fmt:message
                key="jmix_contentmetadata.j_lastModificationDate"/></th>
        <th width="10%"><fmt:message key="jmix_contentmetadata.j_lastPublishingDate"/></th>
        <th width="10%" style="white-space: nowrap;"><fmt:message key="label.workflow"/></th>
        <th width="5%" class="lastCol"><fmt:message key="label.lock"/></th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${currentList}" var="subchild" begin="${begin}" end="${end}" varStatus="">
            <c:choose>
                <c:when test="${status.count % 2 == 0}">
                    <tr class="evenLine">
                </c:when>
                <c:otherwise>
                    <tr class="oddLine">
                </c:otherwise>
            </c:choose>
            <td align="center">

            </td>
            <td >
                ${fn:escapeXml(subchild.primaryNodeType.name)}
            </td>
            <td>

                <c:choose>
                    <c:when test="${jcr:isNodeType(subchild,'jmix:list')}"><a href="${url.base}${subchild.path}.system.html"></c:when>
                    <c:otherwise><a href="${url.base}${subchild.path}.html"></c:otherwise>
                </c:choose>
                <c:if test="${!empty subchild.properties['jcr:title'].string}">
                ${fn:escapeXml(subchild.properties['jcr:title'].string)}
                </c:if>
                <c:if test="${empty subchild.properties['jcr:title'].string}">
                ${fn:escapeXml(subchild.name)}
            </c:if></a>
            </td>
            <td>
                <fmt:formatDate value="${subchild.properties['jcr:created'].date.time}" pattern="yyyy-MM-dd HH:mm"/>
            </td>
            <td>
                <fmt:formatDate value="${subchild.properties['jcr:lastModified'].date.time}" pattern="yyyy-MM-dd HH:mm"/>
            </td align="center">
            <td>
                <fmt:formatDate value="${subchild.properties['j:lastPublished'].date.time}" pattern="yyyy-MM-dd HH:mm"/>
            </td>
            <td>
                <workflow:activeWorkflow node="${subchild}" var="wfs"/>
                <c:forEach items="${wfs}" var="wf">
                        ${wf.id}
                </c:forEach>
            </td>
            <td class="lastCol">
            <c:if test="${subchild.locked}">
                <img height="16" width="16" border="0" style="cursor: pointer;" title="Locked" alt="Supprimer" src="${url.currentModule}/images/icons/locked.gif">
            </c:if>
            </td>
        </tr>
    </c:forEach>

    <c:if test="${not omitFormatting}"><div class="clear"></div></c:if>
    </tbody>
</table>
<c:if test="${editable and renderContext.editMode}">
    <template:module path="*"/>
</c:if>

<template:include template="hidden.footer"/>
