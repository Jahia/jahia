<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="css" resources="jquery.treeview.css"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery.treeview.min.js"/>
<script type="text/javascript">
    $(document).ready(function() {
        $("#tree${currentNode.identifier}").treeview();
    });
</script>
<c:set var="pageNode" value="${renderContext.mainResource.node}"/>
<c:set var="nodeTypeForTree" value="${currentNode.properties.nodeType.string}" scope="request"/>
<c:set var="templateForTree" value="${currentNode.properties.templateForLink.string}" scope="request"/>
<ul id="tree${currentNode.identifier}" class="filetree">
    <c:forEach var="node" items="${jcr:getChildrenOfType(pageNode,nodeTypeForTree)}">
        <template:module node="${node}" view="hidden.tree" editable="false"/>
    </c:forEach>
</ul>
<c:remove var="nodeTypeForTree" scope="request"/>
<c:remove var="templateForTree" scope="request"/>
