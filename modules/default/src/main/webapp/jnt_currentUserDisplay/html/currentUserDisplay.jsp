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
<c:set var="mainTemplate" value="${currentNode.properties['j:userView'].string}"/>
<c:choose>
    <c:when test="${not empty inWrapper and inWrapper eq false}">
        <div class="area <c:if test="${not empty currentNode.properties['j:mockupStyle']}">${currentNode.properties['j:mockupStyle'].string}</c:if>">
            <c:if test="${not empty currentNode.properties['j:userView'].string}">
                <div class="areaTemplate">
                    <span>${currentNode.properties['j:userView'].string}</span>
                </div>
            </c:if>
            <div class="AreaInformation">
                Current user component</br>
                Displayed information: Username
            </div>
        </div>
    </c:when>
    <c:when test="${not empty mainTemplate}">
         <template:module path="${renderContext.user.localPath}" view="${mainTemplate}"/>
     </c:when>
    <c:otherwise>
        <div class="AreaInformation">
               <span>${currentNode.user.name}</span>
            </div>

    </c:otherwise>
</c:choose>

