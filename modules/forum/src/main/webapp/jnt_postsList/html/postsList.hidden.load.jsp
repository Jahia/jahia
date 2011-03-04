<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="css" resources="forum.css"/>
<template:addResources type="javascript" resources="jquery.js,jquery.cuteTime.js,jquery.jeditable.mini.js"/>

<jcr:nodeProperty node="${currentNode}" name="limit" var="limit"/>
<jcr:nodeProperty node="${currentNode}" name="reverseOrder" var="reverseOrder"/>

<c:set var="linked" value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>

<c:if test="${not empty linked}">

<c:set var="postsStatement" value="select * from [jnt:post] as post where ISCHILDNODE(post,'${linked.path}') order by post.['jcr:created'] ${reverseOrder.boolean ? 'desc' : ''}"/>
    <query:definition var="listQuery" statement="${postsStatement}" limit="${limit.long}"  />

    <c:set target="${moduleMap}" property="emptyListMessage" value="No post found" />
    <c:set target="${moduleMap}" property="listQuery" value="${listQuery}" />
    <c:set target="${moduleMap}" property="subNodesView" value="${currentNode.properties['j:subNodesView'].string}" />
</c:if>