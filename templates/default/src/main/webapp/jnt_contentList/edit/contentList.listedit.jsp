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
<template:include templateType="html" template="hidden.header"/>
<c:set var="animatedTasks" value=""/>
<c:set var="animatedWFs" value=""/>
<c:choose>
    <c:when test="${jcr:getParentOfType(currentNode, 'jnt:page') eq null && !jcr:isNodeType(currentNode,'jnt:page')}">
<table width="100%" cellspacing="0" cellpadding="5" border="0" class="evenOddTable">
    <thead>
    <tr>
        <th width="5%" align="center">
            <c:if test="${jcr:isNodeType(currentNode.parent,'jnt:contentList') || jcr:isNodeType(currentNode.parent,'jnt:folder')}">
                <a title="parent" href="${url.base}${currentNode.parent.path}.html"><img height="16" width="16" border="0" style="cursor: pointer;" title="parent" alt="parent" src="${url.currentModule}/images/icons/folder_up.png"></a></div></th>
            </c:if>
        </th>
        <th width="5%"><fmt:message key="label.type"/> </th>
        <th width="35%"><fmt:message key="label.title"/> </th>
        <th width="10%" style="white-space: nowrap;"><fmt:message key="jmix_contentmetadata.j_creationDate"/> </th>
        <th width="10%" style="white-space: nowrap;"><fmt:message key="jmix_contentmetadata.j_lastModificationDate"/></th>
        <th width="5%"><fmt:message key="jmix_contentmetadata.j_lastPublishingDate"/></th>
        <th width="5%" style="white-space: nowrap;"><fmt:message key="label.workflow"/></th>
        <th width="5%"><fmt:message key="label.lock"/></th>
        <th width="10%" class="lastCol"><fmt:message key="label.action"/> </th>
    </tr>
    </thead>
    <tbody>
        <c:forEach items="${currentList}" var="child" begin="${begin}" end="${end}" varStatus="status">
            <tr class="evenLine">
                <td align="center">
                    
                </td>
                <td >
                    <c:if test="${jcr:isNodeType(child, 'jnt:contentList')}">
                        <img  height="24" width="24" border="0" style="cursor: pointer;" src="${url.currentModule}/images/icons/folder-contenu.png"/>
                    </c:if>
                    <c:if test="${!jcr:isNodeType(child, 'jnt:contentList')}">
                        ${fn:escapeXml(child.primaryNodeType.name)}
                    </c:if>
                </td>
                <td>
                    <a
                        href="${url.base}${child.path}.html"> <c:if
                        test="${!empty child.properties['jcr:title'].string}">
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
                <td>
                   <%@include file="workflow.jspf" %>
                </td>
                <td>
                    <c:if test="${child.locked}">
                        <img height="16" width="16" border="0" style="cursor: pointer;" title="Locked" alt="Locked"
                             src="${url.currentModule}/images/icons/locked.gif">
                    </c:if>
                </td>
                <td class="lastCol">
                        <%--
                                <a title="Editer" href="#"><img height="16" width="16" border="0" style="cursor: pointer;" title="Editer" alt="Editer" src="${url.currentModule}/images/icons/edit.png"></a>&nbsp;
                        --%>
                    <%@include file="edit.jspf" %>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>
        <template:include templateType="html" template="hidden.footer"/>
        <table width="100%" cellspacing="0" cellpadding="5" border="0" class="evenOddTable">
        <tr><td colspan="9">
    </c:when>
    <c:otherwise>
        <c:set var="inSite" value="true"/>
        <c:forEach items="${currentList}" var="child" begin="${begin}" end="${end}" varStatus="status">
            <%@include file="edit.jspf" %>
            <%@include file="workflow.jspf" %>
            <div id="edit-${child.identifier}">
                <template:module templateType="html" node="${child}"/>
            </div>
            <hr/>
        </c:forEach>
        <div class="clear"></div>
        <c:if test="${editable and renderContext.editMode}">
            <template:module path="*"/>
        </c:if>
        <template:include templateType="html" template="hidden.footer"/>
    </c:otherwise>
</c:choose>


<c:if test="${empty param.ajaxcall}">
    <%-- include add nodes forms --%>
    <jcr:nodeProperty node="${currentNode}" name="j:allowedTypes" var="types"/>
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
            <div style="display:none;" id="add${currentNode.identifier}-${status.index}">
                <template:module node="${currentNode}" templateType="edit" template="add">
                    <template:param name="resourceNodeType" value="${type.string}"/>
                    <template:param name="currentListURL" value="${url.current}?ajaxcall=true"/>
                </template:module>
            </div>
        </c:forEach>

    </c:if>
</c:if>

<c:if test="${jcr:getParentOfType(currentNode, 'jnt:page') eq null && !jcr:isNodeType(currentNode,'jnt:page')}">
    </td> </tr>
    </tbody>
</table>

</c:if>