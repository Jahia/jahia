<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="flowRequestContext" type="org.springframework.webflow.execution.RequestContext"--%>

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
            $('#userContent_table').dataTable({
                "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
                "iDisplayLength":10,
                "sPaginationType": "bootstrap",
                "aaSorting": [] //this option disable sort by default, the user steal can use column names to sort the table
            });
        });
    </script>
</template:addResources>

<template:include view="hidden.header"/>
<c:if test="${not empty moduleMap.currentList}">
    <table cellpadding="0" cellspacing="0" border="0" class="table table-striped table-bordered" id="userContent_table">
        <thead>
        <tr>
            <th><fmt:message key='label.name'/></th>
            <th><fmt:message key='label.site'/></th>
            <th><fmt:message key='mix_created'/></th>
            <th><fmt:message key='jmix_contentmetadata.j_lastModificationDate'/></th>
            <th><fmt:message key='jmix_contentmetadata.j_lastPublishingDate'/></th>
        </tr>
        </thead>
        <tbody>
            <c:forEach items="${moduleMap.currentList}" var="subchild" begin="${moduleMap.begin}" end="${moduleMap.end}">
                <tr>
                    <td>
                        <jcr:nodeProperty node="${subchild}" name="jcr:title" var="title"/>
                        <c:choose>
                            <c:when test='${jcr:isNodeType(subchild, "nt:file")}'>
                                <a href="<c:url value='${url.files}${subchild.path}'/>"><c:out
                                        value="${not empty subchild && not empty subchild.displayableName ? subchild.string : subchild.name}"/></a>
                            </c:when>
                            <c:when test='${jcr:isNodeType(subchild, "jmix:nodeReference")}'>
                                <jcr:nodeProperty node="${subchild.properties['j:node'].node}" name="jcr:title" var="title"/>
                                <a href="<c:url value='${url.base}${subchild.properties["j:node"].node.path}.html'/>">
                                    <c:out value="${not empty subchild && not empty subchild.displayableName ? subchild.displayableName : subchild.properties['j:node'].node.name}"/></a>
                            </c:when>
                            <c:otherwise>
                                <a href="<c:url value='${url.base}${subchild.path}.html'/>"><c:out
                                        value="${not empty subchild && not empty subchild.displayableName ? subchild.displayableName : subchild.name}"/></a>
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <c:set var="siteNode" value="${subchild.resolveSite}"/>
                                <c:out value="${not empty siteNode && not empty siteNode.displayableName ? siteNode.displayableName : subchild.name}"/>
                    </td>
                    <td>
                        <jcr:nodeProperty node="${subchild}" name="jcr:created" var="created"/>
                        <jcr:nodeProperty node="${subchild}" name="jcr:createdBy" var="createdBy"/>
                                <fmt:message key="metadata.createdBy"/>&nbsp;
                                <c:out value="${not empty createdBy && not empty createdBy.string ? createdBy.string : subchild.name}"/>&nbsp;
                                <fmt:message key="metadata.on"/>&nbsp;
                                <fmt:formatDate value="${not empty created && not empty created.time ? created.time : subchild.name}" pattern="dd, MMMM yyyy HH:mm"/>&nbsp;
                    </td>
                    <td>
                        <jcr:nodeProperty node="${subchild}" name="jcr:lastModified" var="modified"/>
                        <jcr:nodeProperty node="${subchild}" name="jcr:lastModifiedBy" var="modifiedBy"/>
                                <fmt:message key="metadata.modifiedBy"/>&nbsp;
                                <c:out value="${not empty modifiedBy && not empty modifiedBy.string ? modifiedBy.string : subchild.name}"/>&nbsp;
                                <fmt:message key="metadata.on"/>&nbsp;
                                <fmt:formatDate value="${not empty modified && not empty modified.time ? modified.time : subchild.name}" pattern="dd, MMMM yyyy HH:mm"/>&nbsp;
                    </td>
                    <td>
                        <jcr:nodeProperty node="${subchild}" name="jcr:lastModified" var="published"/>
                        <jcr:nodeProperty node="${subchild}" name="jcr:lastModifiedBy" var="publishedBy"/>
                                <fmt:message key="metadata.publishedBy"/>&nbsp;
                                <c:out value="${not empty publishedBy && not empty publishedBy.string ? publishedBy.string : subchild.name}"/>&nbsp;
                                <fmt:message key="metadata.on"/>&nbsp;
                                <fmt:formatDate value="${not empty published && not empty published.time ? published.time : subchild.name}" pattern="dd, MMMM yyyy HH:mm"/>&nbsp;
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${functions:length(moduleMap.currentList) == 0 and not empty moduleMap.emptyListMessage}">
                ${moduleMap.emptyListMessage}
            </c:if>
        </tbody>
    </table>
</c:if>
<c:if test="${moduleMap.editable and renderContext.editMode && !resourceReadOnly}">
    <template:module path="*"/>
</c:if>
<template:include view="hidden.footer"/>
