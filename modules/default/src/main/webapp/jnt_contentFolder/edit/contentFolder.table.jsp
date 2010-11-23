<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="workflow" uri="http://www.jahia.org/tags/workflow" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
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
<template:addResources type="css" resources="contentlist.css"/>
<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="javascript" resources="ajaxreplace.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.js"/>
<template:addResources type="javascript"
                       resources="${url.context}/gwt/resources/${url.ckEditor}/ckeditor.js"/>
<template:addResources type="javascript" resources="jquery.ajaxfileupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ajaxupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ckeditor.js"/>
<template:addResources type="javascript" resources="datepicker.js,jquery.jeditable.datepicker.js"/>
<template:addResources type="javascript" resources="contributedefault.js"/>
<template:addResources type="javascript" resources="i18n/contributedefault-${renderContext.mainResource.locale}.js"/>
<template:addResources type="javascript" resources="animatedcollapse.js"/>
<div id="${currentNode.UUID}">
    <template:include templateType="html" template="hidden.header"/>
    <c:set var="animatedTasks" value=""/>
    <c:set var="animatedWFs" value=""/>

    <table width="100%" cellspacing="0" cellpadding="5" border="0" class="table">
        <thead>
        <tr>
            <th width="1%">&nbsp;</th>
            <th width="5%">
                <c:if test="${jcr:isNodeType(currentNode.parent,'jnt:contentFolder') || jcr:isNodeType(currentNode.parent,'jnt:folder')}">
                    <a title="parent" href="${url.base}${currentNode.parent.path}.html"><img height="16" width="16"
                                                                                             border="0"
                                                                                             style="cursor: pointer;"
                                                                                             title="parent" alt="parent"
                                                                                             src="${url.currentModule}/images/icons/folder_up.png"></a>
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
            <tr class="${status.count % 2 == 0 ? 'even' : 'odd'}">
                <td align="center">
                    <input type="checkbox" name="${child.identifier}"/>
                </td>
                <td>
                    <c:if test="${not empty child.primaryNodeType.templatePackage.rootFolder}">
                        <img src="${url.templatesPath}/${child.primaryNodeType.templatePackage.rootFolder}/icons/${fn:replace(fn:escapeXml(child.primaryNodeType.name),":","_")}_large.png"/>
                    </c:if>
                    <c:if test="${empty child.primaryNodeType.templatePackage.rootFolder}">
                        <img src="${url.templatesPath}/default/icons/${fn:replace(fn:escapeXml(child.primaryNodeType.name),":","_")}_large.png"/>
                    </c:if>
                </td>
                <td>
                    <c:choose>
                    <c:when test="${jcr:isNodeType(child, 'jnt:contentFolder')}">
                    <a href="${url.base}${child.path}.html">
                        </c:when>
                        <c:otherwise>
                        <a href="${url.base}${child.path}.edit">
                            </c:otherwise>
                            </c:choose>
                            <c:if test="${!empty child.properties['jcr:title'].string}">
                                ${fn:escapeXml(child.properties['jcr:title'].string)}
                            </c:if>
                            <c:if test="${empty child.properties['jcr:title'].string}">
                                ${fn:escapeXml(child.name)}
                            </c:if></a>
                </td>
                <td>
                    <fmt:formatDate value="${child.properties['jcr:created'].date.time}" pattern="yyyy-MM-dd HH:mm"/>
                </td>
                <td>
                    <fmt:formatDate value="${child.properties['jcr:lastModified'].date.time}"
                                    pattern="yyyy-MM-dd HH:mm"/>
                </td align="center">
                <td>
                    <fmt:formatDate value="${child.properties['j:lastPublished'].date.time}"
                                    pattern="yyyy-MM-dd HH:mm"/>
                </td>
                <td class="lastCol">
                    <workflow:activeWorkflow node="${child}" var="wfs"/>
                    <c:forEach items="${wfs}" var="wf">
                        ${wf.workflowDefinition.displayName}
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
    <template:include templateType="html" template="hidden.footer"/>
</div>


<c:if test="${not renderContext.ajaxRequest}">
    <%-- include add nodes forms --%>
    <c:choose>
        <c:when test="${empty restrictions}">
            <jcr:nodeProperty node="${currentNode}" name="j:contributeTypes" var="types"/>
        </c:when>
        <c:otherwise>
            <c:set var="types" value="${restrictions}"/>
        </c:otherwise>
    </c:choose>
    <h3 class="titleaddnewcontent">
        <img title="" alt="" src="${url.currentModule}/images/add.png"/><fmt:message key="label.add.new.content"/>
    </h3>
    <script language="JavaScript">
        <c:forEach items="${types}" var="type" varStatus="status">
        animatedcollapse.addDiv('add${currentNode.identifier}-${status.index}', 'fade=1,speed=700,group=newContent');
        </c:forEach>
        animatedcollapse.init();
    </script>
    <c:if test="${types != null}">
        <div class="listEditToolbar">
            <c:forEach items="${types}" var="type" varStatus="status">
                <jcr:nodeType name="${type.string}" var="nodeType"/>
                <button onclick="animatedcollapse.toggle('add${currentNode.identifier}-${status.index}');"><span
                        class="icon-contribute icon-add"></span>${jcr:label(nodeType, renderContext.mainResourceLocale)}
                </button>
            </c:forEach>
        </div>

        <c:forEach items="${types}" var="type" varStatus="status">
            <div style="display:none;" id="add${currentNode.identifier}-${status.index}" class="addContentContributeDiv">
                <template:module node="${currentNode}" templateType="edit" template="add">
                    <template:param name="resourceNodeType" value="${type.string}"/>
                    <template:param name="currentListURL" value="${url.current}.ajax"/>
                    <template:param name="addContentCallbackJS"
                                            value="$('.addContentContributeDiv').each(function(index,value){animatedcollapse.addDiv($(this).attr('id'), 'fade=1,speed=700,group=tasks');});animatedcollapse.reinit();"/>
                </template:module>
            </div>
        </c:forEach>

    </c:if>
</c:if>
