<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<template:include templateType="html" template="hidden.header"/>
<c:forEach items="${currentList}" var="subchild" begin="${begin}" end="${end}">
    <template:module node="${subchild}" templateType="edit" forcedTemplate="edit" >
        <c:if test="${not empty forcedSkin}">
            <template:param name="forcedSkin" value="${forcedSkin}"/>
        </c:if>
        <c:if test="${not empty renderOptions}">
            <template:param name="renderOptions" value="${renderOptions}"/>
        </c:if>
    </template:module>
</c:forEach>
<div class="clear"></div>
<c:if test="${editable and renderContext.editMode}">
    <template:module path="*"/>
</c:if>
<template:include templateType="html" template="hidden.footer"/>

<c:if test="${empty param.ajaxcall}">
    <%-- include add nodes forms --%>
    <jcr:nodeProperty node="${currentNode}" name="j:allowedTypes" var="types"/>

<script type="text/javascript">
    function hideAdd(id, index) {
    <c:forEach items="${types}" var="type" varStatus="status">
        if (index == ${status.index}) {
            document.getElementById('add'+id+'-${status.index}').style.display = 'block';
        } else {
            document.getElementById('add'+id+'-${status.index}').style.display = 'none';
        }
    </c:forEach>
    }
</script>
    <c:if test="${types != null}">
        Add :
        <c:forEach items="${types}" var="type" varStatus="status">
            <a href="#" onclick="hideAdd('${currentNode.identifier}',${status.index})">${type.string}</a>
        </c:forEach>

        <c:forEach items="${types}" var="type" varStatus="status">
            <div style="display:none;" id="add${currentNode.identifier}-${status.index}"/>
            <template:module node="${currentNode}" templateType="edit" template="add">
                <template:param name="resourceNodeType" value="${type.string}"/>
            </template:module>
            </div>
        </c:forEach>
    </c:if>
</c:if>
