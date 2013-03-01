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

<%--<c:set var="mainTemplate" value="${renderContext.mainResource.template}"/>--%>
<%--<c:if test="${(not empty currentNode.properties['j:mainResourceView'].string) and (not (currentNode.properties['j:mainResourceView'].string eq 'default'))}">--%>
    <c:set var="mainTemplate" value="${currentNode.properties['j:mainResourceView'].string}"/>
<%--</c:if>--%>

<c:choose>
    <c:when test="${not empty inWrapper and inWrapper eq false}">
        <div class="mainResourceDisplay<c:if test="${not empty currentNode.properties['j:mockupStyle']}"> ${currentNode.properties['j:mockupStyle'].string} mainResourceDisplay ${currentNode.properties['j:mockupStyle'].string}</c:if>">
            <c:if test="${not empty currentNode.properties['j:mainResourceView'].string}">
                <div class="mainResourceDisplayTemplate">
                    <span>${currentNode.properties['j:mainResourceView'].string}</span>
                </div>
            </c:if>
        </div>
    </c:when>
    <c:otherwise>
        <template:area view="${mainTemplate}" mockupStyle="${currentNode.properties['j:mockupStyle'].string}" templateType="${renderContext.mainResource.templateType}" />
    </c:otherwise>
</c:choose>

