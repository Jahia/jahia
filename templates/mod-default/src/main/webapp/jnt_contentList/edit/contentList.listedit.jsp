<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<template:addResources type="javascript" resources="ajaxreplace.js"/>
<c:if test="${empty param.ajaxcall}">

<script type="text/javascript" >
    function invert(source,target) {
        var data = {};
        data["action"] = "moveBefore";
        data["target"]=target;
        data["source"]=source;
        url = '${url.base}'+source;
        $.post(url+".move.do", data, function(result) {
            replace('${currentNode.UUID}', '${url.current}','');
        }, "json");
    }

    function deleteNode(source) {
        var data = {};
        data["methodToCall"] = "delete";
        url = '${url.base}'+source;
        $.post(url, data, function(result) {
            replace('${currentNode.UUID}', '${url.current}','');
        }, "json");
    }
</script>

<input type="button" value="Edit" onclick="replace('${currentNode.UUID}', '${url.current}?ajaxcall=true', '')"/>
<input type="button" value="Preview" onclick="replace('${currentNode.UUID}', '${url.base}${currentNode.path}.html?ajaxcall=true', '')"/>
</c:if>

<template:include templateType="html" template="hidden.header"/>

<c:forEach items="${currentList}" var="child" begin="${begin}" end="${end}" varStatus="status">
    <%-- inline edit --%>
    <template:module node="${child}" templateType="edit" forcedTemplate="edit" >
        <c:if test="${not empty forcedSkin}">
            <template:param name="forcedSkin" value="${forcedSkin}"/>
        </c:if>
        <c:if test="${not empty renderOptions}">
            <template:param name="renderOptions" value="${renderOptions}"/>
        </c:if>
    </template:module>
    <%-- buttons --%>
    <div>
        <c:if test="${status.index gt 0}">
            <input id="moveUp-${currentNode.identifier}-${status.index}" type="button" value="move up" onclick="invert('${child.path}','${previousChild.path}')" />
        </c:if>
        <c:if test="${status.index lt listTotalSize-1}">
            <input type="button" value="move down" onclick="document.getElementById('moveUp-${currentNode.identifier}-${status.index+1}').onclick()" />
        </c:if>
        <input type="button" value="delete" onclick="deleteNode('${child.path}')" />
        <c:set var="previousChild" value="${child}"/>
    </div>
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
            <jcr:nodeType name="${type.string}" var="nodeType"/>
            <a href="#" onclick="hideAdd('${currentNode.identifier}',${status.index})">${jcr:labelForLocale(nodeType, renderContext.mainResourceLocale)}</a>
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
