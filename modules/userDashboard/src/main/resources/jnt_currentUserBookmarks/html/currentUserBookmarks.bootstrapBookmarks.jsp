<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>

<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="flowRequestContext" type="org.springframework.webflow.execution.RequestContext"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>

<template:addResources type="javascript" resources="jquery.min.js,admin-bootstrap.js,jquery.blockUI.js,bootstrap-filestyle.min.js,jquery.metadata.js,workInProgress.js"/>
<template:addResources type="javascript" resources="datatables/jquery.dataTables.js,i18n/jquery.dataTables-${currentResource.locale}.js,datatables/dataTables.bootstrap-ext.js"/>
<template:addResources type="css" resources="admin-bootstrap.css,datatables/css/bootstrap-theme.css,tablecloth.css"/>
<fmt:message key="label.workInProgressTitle" var="i18nWaiting"/><c:set var="i18nWaiting" value="${functions:escapeJavaScript(i18nWaiting)}"/>

<template:addResources>
    <script type="text/javascript">
        $(document).ready(function () {
            $(":file").filestyle({classButton: "btn",classIcon: "icon-folder-open"/*,buttonText:"Translation"*/});
        });
    </script>
    <script type="text/javascript">
        $(document).ready(function () {
            $('#bookmarks_table').dataTable({
                "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
                "iDisplayLength":10,
                "sPaginationType": "bootstrap",
                "aaSorting": [] //this option disable sort by default, the user steal can use column names to sort the table
            });
        });
    </script>
</template:addResources>

<template:include view="hidden.header"/>
<template:addResources type="css" resources="bookmarks.css"/>
<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="javascript" resources="ajaxreplace.js"/>
<c:set var="user" value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>

<c:if test="${empty user or not jcr:isNodeType(user, 'jnt:user')}">
    <jcr:node var="user" path="${renderContext.user.localPath}"/>
</c:if>
<c:set var="ps" value="?pagerUrl=${url.mainResource}"/>
<c:if test="${!empty param.pageUrl}">
    <c:set var="ps" value="?pagerUrl=${param.pageUrl}"/>
</c:if>
<c:forEach items="${param}" var="p" varStatus="status">
    <c:if test="${p.key != 'pagerUrl' && p.key != 'jsite'}">
        <c:set var="ps" value="${ps}&${p.key}=${p.value}" />
    </c:if>
</c:forEach>
<c:set target="${moduleMap}" property="pagerUrl" value="${param.pagerUrl}"/>
<template:include view="hidden.header"/>
<div id="bookmarkList${user.identifier}">
    <template:initPager totalSize="${moduleMap.end}" pageSize="${currentNode.properties['numberOfBookmarksPerPage'].string}" id="${renderContext.mainResource.node.identifier}"/>
    <template:displayPagination id="${renderContext.mainResource.node.identifier}"/>

    <c:if test="${currentResource.workspace eq 'default'}">
        <script type="text/javascript">
            $('#bookmarkList${user.identifier}').load('<c:url value="${url.baseLive}${currentNode.path}.html.ajax${ps}"/>');
        </script>
    </c:if>
        <table cellpadding="0" cellspacing="0" border="0" class="table table-striped table-bordered" id="bookmarks_table">
            <thead>
            <tr>
                <th><fmt:message key='label.name'/></th>
                <th><fmt:message key='label.site'/></th>
                <th><fmt:message key='mix_created'/></th>
            </tr>
            </thead>
            <tbody>
                <%@include file="bookmarksTableRow.jspf" %>
            </tbody>
        </table>
</div>