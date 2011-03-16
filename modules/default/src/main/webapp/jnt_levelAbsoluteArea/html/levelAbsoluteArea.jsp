<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<c:choose>
    <c:when test="${not empty inWrapper and inWrapper eq false}">
        <div class="absoluteArea<c:if test="${not empty currentNode.properties['j:mockupStyle']}"> ${currentNode.properties['j:mockupStyle'].string}</c:if>">
            <div class="absoluteAreaTemplate">
                <span>Absolute Area with start level : ${currentNode.name}</span>
            </div>
        </div>
    </c:when>
    <c:otherwise>
        <jcr:nodeProperty node="${currentNode}" name="j:allowedTypes" var="restrictions"/>
        <c:if test="${not empty restrictions}">
            <c:forEach items="${restrictions}" var="value">
                <c:if test="${not empty nodeTypes}">
                    <c:set var="nodeTypes" value="${nodeTypes} ${value.string}"/>
                </c:if>
                <c:if test="${empty nodeTypes}">
                    <c:set var="nodeTypes" value="${value.string}"/>
                </c:if>
            </c:forEach>
        </c:if>
        <c:set var="listLimit" value="${currentNode.properties['j:numberOfItems'].long}"/>
        <c:if test="${empty listLimit}">
            <c:set var="listLimit" value="${-1}"/>
        </c:if>

        <c:set var="node" value="${renderContext.mainResource.node}"/>
        <c:forEach var="ancestor" items="${renderContext.mainResource.node.ancestors}">
            <c:if test="${empty currentLevel}">
                <c:if test="${ancestor.path eq renderContext.site.path}">
                    <c:set var="currentLevel" value="0"/>
                </c:if>
            </c:if>
            <c:if test="${currentLevel eq (currentNode.properties['j:level'].long + 1)}">
                <c:set var="node" value="${ancestor}"/>
            </c:if>
            <c:if test="${not empty currentLevel}">
                <c:set var="currentLevel" value="${currentLevel + 1}"/>
            </c:if>
        </c:forEach>

        <c:if test="${not empty node}">
            <template:area view="${currentNode.properties['j:referenceView'].string}"
                                     path="${node.path}/${currentNode.name}"
                                     nodeTypes="${nodeTypes}" listLimit="${listLimit}" moduleType="absoluteArea">
                <c:if test="${not empty currentNode.properties['j:subNodesView'].string}">
                    <template:param name="subNodesView"
                                    value="${currentNode.properties['j:subNodesView'].string}"/>
                </c:if>
                <c:if test="${not empty currentNode.properties['j:mockupStyle'].string}">
                    <template:param name="mockupStyle" value="${currentNode.properties['j:mockupStyle'].string}"/>
                </c:if>
            </template:area>
        </c:if>
    </c:otherwise>
</c:choose>

