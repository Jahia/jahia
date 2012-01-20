<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="workflow" uri="http://www.jahia.org/tags/workflow" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="utils" uri="http://www.jahia.org/tags/utilityLib" %>
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
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js"/>
<template:addResources type="javascript" resources="timepicker.js"/>
<template:addResources type="javascript" resources="jquery.form.js"/>
<template:addResources type="javascript" resources="ajaxreplace.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.js"/>
<template:addResources type="javascript" resources="ckeditor/ckeditor.js,ckeditor/adapters/jquery.js"/>
<template:addResources type="javascript" resources="jquery.ajaxfileupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ajaxupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ckeditor.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.datepicker.js"/>
<template:addResources type="javascript"
                       resources="jquery.treeview.min.js,jquery.treeview.async.jahia.js,jquery.colorbox.js,jquery.fancybox.js"/>
<template:addResources type="javascript" resources="jquery.defer.js"/>
<template:addResources type="javascript" resources="treeselector.js"/>
<%--
<template:addResources type="css" resources="contentlist.css"/>
<template:addResources type="css" resources="timepicker.css,datepicker.css,jquery.treeview.css,jquery.fancybox.css,contentlist.css,formcontribute.css,jquery.colorbox.css,jquery.fancybox.css"/>
--%>
<template:addResources type="css" resources="contribute.min.css"/>
<template:addResources type="javascript" resources="jquery.jeditable.treeItemSelector.js"/>
<template:addResources type="javascript" resources="contributedefault.js"/>
<template:addResources type="javascript" resources="i18n/contributedefault-${renderContext.UILocale}.js"/>
<utils:setBundle basename="JahiaContributeMode" useUILocale="true"/>
<%@include file="../../include/contributeCKEditorToolbar.jspf" %>
<div id="${currentNode.UUID}">
    <template:include templateType="html" view="hidden.header"/>
    <c:set var="animatedTasks" value=""/>
    <c:set var="animatedWFs" value=""/>
    <table width="100%" cellspacing="0" cellpadding="5" border="0" class="table">
        <thead>
        <tr>
            <th width="1%">&nbsp;</th>
            <th width="5%">
                <c:if test="${jcr:isNodeType(currentNode.parent,'jnt:contentFolder,jnt:folder')}">
                    <a title="parent" href="<c:url value='${url.base}${currentNode.parent.path}.html'/>"><img height="32" width="32"
                                                                                             border="0"
                                                                                             style="cursor: pointer;"
                                                                                             title="parent" alt="parent"
                                                                                             src="<c:url value='${url.templatesPath}/default/images/icons/folder_up.png'/>"></a>
                </c:if>
            </th>
            <th width="35%"><fmt:message key="label.title"/></th>
            <th width="15%" style="white-space: nowrap;"><fmt:message key="mix_created.jcr_created"/></th>
            <th width="15%" style="white-space: nowrap;"><fmt:message key="mix_lastModified.jcr_lastModified"/></th>
            <th width="15%"><fmt:message key="jmix_lastPublished.j_lastPublished"/></th>
            <th width="15%" style="white-space: nowrap;" class="lastCol"><fmt:message key="label.workflow"/></th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${moduleMap.currentList}" var="child" begin="${moduleMap.begin}" end="${moduleMap.end}"
                   varStatus="status">
            <c:set var="markedForDeletion" value="${jcr:isNodeType(child, 'jmix:markedForDeletion')}"/>
            <c:set var="markedForDeletionRoot" value="${jcr:isNodeType(child, 'jmix:markedForDeletionRoot')}"/>
            <c:set var="nodeName" value="${!empty child.propertiesAsString['jcr:title'] ? child.propertiesAsString['jcr:title'] : child.name}"/>
            <tr class="${status.count % 2 == 0 ? 'even' : 'odd'}">
                <td align="center">
                    <input type="checkbox" class="jahiaCBoxContributeContent" name="${child.identifier}" ${child.locked ? 'disabled=true':''}/>
                </td>
                <td>
                    <jcr:icon var="icon" node="${child}"/>
                    <img src="<c:url value='${url.templatesPath}/${icon}_large.png'/>"/>
                </td>
                <td>
                    <c:if test="${child.locked}">
                        <img height="16" width="16" border="0" title="<fmt:message key='label.locked'/>" alt="<fmt:message key='label.locked'/>"
                             src="<c:url value='${url.templatesPath}/default/images/icons/locked.gif'/>">
                        <c:if test="${markedForDeletionRoot && fn:length(child.properties['j:lockTypes']) <= 1}">
                            <fmt:message key="message.undelete.confirm" var="i18nUndeleteConfirm">
                                <fmt:param value="${nodeName}"/>
                            </fmt:message>
                            <img height="16" width="16" border="0" style="cursor:pointer;" title="<fmt:message key='label.undelete'/>" alt="<fmt:message key='label.undelete'/>"
                                 src="<c:url value='/icons/undelete.png'/>"
                                onclick="deleteSingleNode('${child.identifier}',false);">
                            <jcr:nodeProperty node="${child}" name="j:published" var="childPublished"/>
                            <c:if test="${empty childPublished}">
                                <fmt:message key="message.remove.single.confirm" var="i18nDeleteConfirm">
                                    <fmt:param value="${nodeName}"/>
                                </fmt:message>
                                <img height="16" width="16" border="0" style="cursor:pointer;" title="<fmt:message key='label.deletePermanently'/>" alt="<fmt:message key='label.deletePermanently'/>"
                                     src="<c:url value='/icons/delete.png'/>"
                                onclick="deleteSingleNode('${child.identifier}');">
                            </c:if>
                            <c:if test="${not empty childPublished}">
                                <fmt:message key="message.requestDeletionApproval.confirm" var="i18nPublishConfirm">
                                    <fmt:param value="${nodeName}"/>
                                </fmt:message>
                                <img height="16" width="16" border="0" style="cursor:pointer;" title="<fmt:message key='label.requestDeletionApproval'/>" alt="<fmt:message key='label.requestDeletionApproval'/>"
                                     src="<c:url value='/icons/publish.png'/>"
                                onclick="publishNodes(new Array('${child.identifier}'), '${functions:escapeJavaScript(i18nPublishConfirm)}');">
                            </c:if>
                        </c:if>
                    </c:if>

                    <c:if test="${markedForDeletion}">
                        <span class="markedForDeletion">
                    </c:if>
                    <c:url value="${url.base}${child.path}.${not jcr:isNodeType(child, 'jnt:contentFolder') ? 'editContent.' : ''}html" var="childUrl"/>
                    <a href="${childUrl}">${fn:escapeXml(nodeName)}</a>
                    <c:if test="${markedForDeletion}">
                        </span>
                    </c:if>
                </td>
                <td>
                    <fmt:formatDate value="${child.properties['jcr:created'].date.time}" pattern="yyyy-MM-dd HH:mm"/>
                </td>
                <td>
                    <fmt:formatDate value="${child.properties['jcr:lastModified'].date.time}"
                                    pattern="yyyy-MM-dd HH:mm"/>
                </td>
                <td>
                    <fmt:formatDate value="${child.properties['j:lastPublished'].date.time}"
                                    pattern="yyyy-MM-dd HH:mm"/>
                </td>
                <td class="lastCol">
                    <workflow:activeWorkflow node="${child}" var="wfs"/>
                    <c:forEach items="${wfs}" var="wf">
                        ${fn:escapeXml(wf.workflowDefinition.displayName)}
                    </c:forEach>
                </td>
                <%--<td>--%>
                    <%--<%@include file="../../jnt_contentList/edit/workflow.jspf" %>--%>
                <%--</td>--%>
                <%--<td>--%>
                    <%--<c:if test="${child.locked}">--%>
                        <%--<img height="16" width="16" border="0" style="cursor: pointer;" title="Locked" alt="Locked"--%>
                             <%--src="${url.currentModule}/images/icons/locked.gif">--%>
                    <%--</c:if>--%>
                <%--</td>--%>
                <%--<td class="lastCol">--%>
                    <%--<%@include file="../../jnt_contentList/edit/edit.jspf" %>--%>
                <%--</td>--%>
            </tr>
        </c:forEach>
        </tbody>
    </table>
	<c:if test="${moduleMap.editable and renderContext.editMode}">
		<template:module path="*"/>
	</c:if>

    <template:include templateType="html" view="hidden.footer"/>
</div>


<c:if test="${not renderContext.ajaxRequest and jcr:hasPermission(currentNode, 'jcr:addChildNodes_default')}">
    <%@include file="../../jnt_contentList/html/addcontent.jspf" %>
</c:if>
