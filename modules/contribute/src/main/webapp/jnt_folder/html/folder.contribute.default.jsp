<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="workflow" uri="http://www.jahia.org/tags/workflow" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
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
<%--
<template:addResources type="css" resources="contentlist.css"/>
--%>
<template:addResources type="css" resources="contribute.min.css"/>
<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="javascript" resources="ajaxreplace.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.js"/>
<template:addResources type="javascript" resources="ckeditor/ckeditor.js"/>
<template:addResources type="javascript" resources="jquery.ajaxfileupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ajaxupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ckeditor.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.datepicker.js"/>
<template:addResources type="javascript" resources="contributedefault.js"/>
<template:addResources type="javascript" resources="i18n/contributedefault-${renderContext.UILocale}.js"/>
<template:addResources type="javascript" resources="animatedcollapse.js"/>
<utility:setBundle basename="JahiaContributeMode" useUILocale="true"/>
<%@include file="../../include/contributeCKEditorToolbar.jspf" %>
<c:set var="animatedTasks" value=""/>
<c:set var="animatedWFs" value=""/>
<table width="100%" cellspacing="0" cellpadding="5" border="0" class="evenOddTable">
    <thead>
    <tr>
        <th width="5%" align="center">
            <c:if test="${jcr:isNodeType(currentNode.parent,'jnt:contentList') || jcr:isNodeType(currentNode.parent,'jnt:folder')}">
            <a title="parent" href="<c:url value='${url.base}${currentNode.parent.path}.html'/>"><img height="32"
                                                                                                      width="32"
                                                                                                      border="0"
                                                                                                      style="cursor: pointer;"
                                                                                                      title="parent"
                                                                                                      alt="parent"
                                                                                                      src="<c:url value='${url.currentModule}/images/icons/folder_up.png'/>"></a>
        </c:if>
        </th>
        <th width="5%"><fmt:message key="label.type"/></th>
        <th width="35%"><fmt:message key="label.title"/></th>
        <th width="10%" style="white-space: nowrap;"><fmt:message key="jmix_contentmetadata.j_creationDate"/></th>
        <th width="5%" style="white-space: nowrap;"><fmt:message
                key="jmix_contentmetadata.j_lastModificationDate"/></th>
        <th width="5%" style="white-space: nowrap;"><fmt:message key="jmix_contentmetadata.j_lastPublishingDate"/></th>
        <th width="20%" style="white-space: nowrap;"><fmt:message key="label.workflow"/></th>
        <th width="5%"><fmt:message key="label.lock"/></th>
        <th width="20%" class="lastCol"><fmt:message key="label.action"/></th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${currentNode.nodes}" var="child" varStatus="status">
        <tr class="evenLine">
            <td align="center">
            </td>
            <td>
                <c:if test="${jcr:isNodeType(child, 'jnt:folder')}">
                    <img height="24" width="24" border="0" style="cursor: pointer;"
                         src="<c:url value='${url.currentModule}/images/icons/folder-files.png'/>"/>
                </c:if>
                <c:if test="${!jcr:isNodeType(child, 'jnt:folder')}">
                    ${fn:escapeXml(child.fileContent.contentType)}
                </c:if>
            </td>
            <td><c:if test="${jcr:isNodeType(child, 'jnt:folder')}">
                <a href="<c:url value='${url.base}${child.path}.html'/>"><c:if
                        test="${!empty child.properties['jcr:title'].string}">
                    ${fn:escapeXml(child.properties['jcr:title'].string)}
                </c:if>
                    <c:if test="${empty child.properties['jcr:title'].string}">
                        ${fn:escapeXml(child.name)}
                    </c:if></a>
            </c:if>
                <c:if test="${!jcr:isNodeType(child, 'jnt:folder')}">
                    <a href="${child.url}"><c:if test="${!empty child.properties['jcr:title'].string}">
                        ${fn:escapeXml(child.properties['jcr:title'].string)}
                    </c:if>
                        <c:if test="${empty child.properties['jcr:title'].string}">
                            ${fn:escapeXml(child.name)}
                        </c:if></a>
                </c:if>

            </td>
            <td>
                <fmt:formatDate value="${child.properties['jcr:created'].date.time}" pattern="yyyy-MM-dd HH:mm"/>
            </td>
            <td>
                <fmt:formatDate value="${child.properties['jcr:lastModified'].date.time}" pattern="yyyy-MM-dd HH:mm"/>
            </td>
            <td>
                <fmt:formatDate value="${child.properties['j:lastPublished'].date.time}" pattern="yyyy-MM-dd HH:mm"/>
            </td>
            <td>
                <%@include file="workflow.jspf" %>
            </td>
            <td>
                <c:if test="${child.locked}">
                    <img height="16" width="16" border="0" style="cursor: pointer;" title="Locked" alt="Supprimer"
                         src="<c:url value='${url.templatesPath}/default/images/icons/locked.gif'/>">
                </c:if>
            </td>
            <td class="lastCol">
                    <%--
                            <a title="Editer" href="#"><img height="16" width="16" border="0" style="cursor: pointer;" title="Editer" alt="Editer" src="${url.currentModule}/images/icons/edit.png"></a>&nbsp;
                    --%>
                <c:if test="${jcr:hasPermission(currentNode, 'jcr:modifyProperties_default')}">
                    <%@include file="edit.jspf" %>
                </c:if>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>
<div class="addcontent">

    <c:if test="${not renderContext.ajaxRequest and jcr:hasPermission(currentNode, 'jcr:addChildNodes_default')}">
        <%-- include add nodes forms --%>
        <c:set var="types" value="jnt:folder,jnt:file"/>
        <h3 class="titleaddnewcontent">
            <img title="" alt="" src="<c:url value='${url.currentModule}/images/add.png'/>"/><fmt:message
                key="label.add.new.content"/>
        </h3>
        <script language="JavaScript">
            <c:forTokens items="${types}" delims="," var="type" varStatus="status">
            animatedcollapse.addDiv('add${currentNode.identifier}-${status.index}',
                    'fade=1,speed=700,group=newContent');
            </c:forTokens>
            animatedcollapse.init();
        </script>
        <c:if test="${types != null}">
            <div class="listEditToolbar">
                <c:forTokens items="${types}" delims="," var="type" varStatus="status">
                    <jcr:nodeType name="${type}" var="nodeType"/>
                    <button onclick="animatedcollapse.toggle('add${currentNode.identifier}-${status.index}');"><span
                            class="icon-contribute icon-add"></span>${jcr:label(nodeType, renderContext.UILocale)}
                    </button>
                </c:forTokens>
            </div>
            <c:url var="myUrl" value="${url.current}"/>
            <c:forTokens items="${types}" delims="," var="type" varStatus="status">
                <div style="display:none;" id="add${currentNode.identifier}-${status.index}">
                    <template:module node="${currentNode}" view="contribute.add">
                        <template:param name="resourceNodeType" value="${type}"/>
                        <template:param name="currentListURL" value="${myUrl}"/>
                    </template:module>
                </div>
            </c:forTokens>
        </c:if>
    </c:if>

</div>
