<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="css" resources="forum.css"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery.cuteTime.js,jquery.jeditable.mini.js"/>

<template:linker property="j:bindedComponent"/>
<template:addResources type="css" resources="forum.css"/>
<c:set var="linked" value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>

<script>
    function initCuteTime() {
        $('.timestamp').cuteTime({ refresh: 60000 });
    }
    $(document).ready(function () {
        $('.timestamp').cuteTime({ refresh: 60000 });

        $(".editablePost").editable(function (value, settings) {
            var data = {'methodToCall':'put'};
            var submitId = $(this).attr('jcr:id');
            data[submitId] = value;
            $.post($(this).attr('jcr:url'), data, null, "json");
            return(value);
        }, {
            type    : 'textarea',
            onblur : 'ignore',
            submit : '<button type="submit"><span class="icon-contribute icon-accept"></span>Ok</button>',
            cancel : '<button type="submit"><span class="icon-contribute icon-cancel"></span>Cancel</button>',
            tooltip : 'Edit'
        });
    });
</script>
<div id="forum-body">
    <div class="posts" id="${currentNode.UUID}">
        <h3>${linked.properties["topicSubject"].string}</h3>
        <c:if test="${currentNode.properties.reverseOrder.boolean}">
            <c:set target="${moduleMap}" property="currentList" value="${functions:reverseIterator(linked.nodes)}" />
        </c:if>
        <c:if test="${!currentNode.properties.reverseOrder.boolean}">
            <c:set target="${moduleMap}" property="currentList" value="${linked.nodes}" />
        </c:if>
        <c:set target="${moduleMap}" property="listTotalSize" value="${fn:length(linked.nodes)}" />
        <c:set var="end" value="${fn:length(linked.nodes)}"/>
        <c:if test="${currentNode.properties.limit.long > 0}">
            <c:set var="end" value="${currentNode.properties.limit.long -1}"/>
        </c:if>
        <c:forEach items="${moduleMap.currentList}" var="subchild" varStatus="status" end="${end}">
            <c:if test="${jcr:isNodeType(subchild, 'jnt:post')}">
            <div class="forum-box forum-box-style${(status.index mod 2)+1}">
                <template:module node="${subchild}" template="forum"/>
            </div>
            </c:if>
        </c:forEach>
    </div>
</div>