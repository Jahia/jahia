<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<c:set var="resourceReadOnly" value="${currentResource.moduleParams.readOnly}"/>
<template:include view="hidden.header"/>
<c:set var="isEmpty" value="true"/>

<table style="width: 100%;" cellpadding="0" cellspacing="0" border="1">
    <tr>
        <th><b><fmt:message key='label.urlmapping.mapping'/></b></th>
        <th><b><fmt:message key='label.page'/></b></th>
        <th><b><fmt:message key='label.urlmapping.lang'/></b></th>
        <th><b><fmt:message key='label.urlmapping.active'/></b></th>
        <th><b><fmt:message key='label.urlmapping.default'/></b></th>
    </tr>

    <c:forEach items="${moduleMap.currentList}" var="subchild" begin="${moduleMap.begin}" end="${moduleMap.end}"
               varStatus="status">
        <tr class="${status.index % 2 == 0 ? 'evenLine' : 'oddLine'}">
            <template:module node="${subchild}" view="${moduleMap.subNodesView}"
                             editable="${moduleMap.editable && !resourceReadOnly}"/>
        </tr>
        <c:set var="isEmpty" value="false"/>
    </c:forEach>
</table>
<c:if test="${not omitFormatting}">
    <div class="clear"></div>
</c:if>
<c:if test="${not empty moduleMap.emptyListMessage and (renderContext.editMode or moduleMap.forceEmptyListMessageDisplay) and isEmpty}">
    ${moduleMap.emptyListMessage}
</c:if>
<template:include view="hidden.footer"/>
