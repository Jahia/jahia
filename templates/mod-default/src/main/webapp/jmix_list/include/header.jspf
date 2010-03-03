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
<div id="${currentNode.UUID}">

    <c:remove var="currentList" scope="request"/>
    <c:choose>
        <c:when test="${jcr:isNodeType(currentNode, 'jmix:pager')}">
            <c:set scope="request" var="paginationActive" value="true"/>
        </c:when>
        <c:otherwise>
            <c:set var="begin" value="0" scope="request"/>
        </c:otherwise>
    </c:choose>
    <template:module node="${currentNode}" forcedTemplate="hidden.load" editable="false">
        <template:param name="forcedSkin" value="none"/>
    </template:module>

    <c:if test="${empty editable}">
        <c:set var="editable" value="false"/>
    </c:if>
    <c:if test="${not empty paginationActive}">
        <template:option node="${currentNode}" nodetype="jmix:pager" template="hidden.init"/>
    </c:if>