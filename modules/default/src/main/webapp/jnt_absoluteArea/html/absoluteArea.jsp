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
<c:set var="level" value="${currentNode.properties['j:level'].long}" />
<c:choose>
    <c:when test="${not empty inWrapper and inWrapper eq false}">
        <div class="absoluteArea<c:if test="${not empty currentNode.properties['j:mockupStyle']}"> ${currentNode.properties['j:mockupStyle'].string}</c:if>">
            <div class="absoluteAreaTemplate">
                <c:if test="${empty level}" >
                    <span>Absolute Area : ${currentNode.resolveSite.home.name}</span>
                </c:if>
                <c:if test="${not empty level}" >
                    <span>Absolute Area : ${currentNode.name} - Level ${level}</span>
                </c:if>
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

        <c:if test="${empty level}" >
            <template:area view="${currentNode.properties['j:referenceView'].string}"
                           path="${currentNode.name}"
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
        <c:if test="${not empty level}" >
            <template:area level="${level}" view="${currentNode.properties['j:referenceView'].string}"
                           path="${currentNode.name}"
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

