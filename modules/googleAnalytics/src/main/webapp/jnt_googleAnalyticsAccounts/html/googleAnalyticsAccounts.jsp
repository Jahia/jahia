<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
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

<template:include template="hidden.header"/>
<c:if test="${jcr:hasPermission(currentNode,'jcr:write')}">
    <c:if test="${empty renderContext.site.properties['webPropertyID'].string}">
        <p><fmt:message key="label.gaIntro"/></p>
        <form action="${url.base}${currentNode.path}.setGoogleAccount.do">
            <input type="hidden" name="action" value="addWebPropertyID"/>
            <input type="hidden" name="redirectTo" value="${url.base}${renderContext.mainResource.node.path}"/>
            <fmt:message key="label.enterWebPropertyID"/> : <input type="text" name="webPropertyID"/>
            <input type="submit"/>
            <c:if test="${! empty addWebPropertyError}">
                <span class="error"><fmt:message key="label.addWebPropertyError"/></span>
            </c:if>
        </form>
    </c:if>
    <c:if test="${!empty renderContext.site.properties['webPropertyID'].string}">
        <fmt:message key="label.YourWebPropertyID"/> : ${renderContext.site.properties['webPropertyID'].string}
        <form action="${url.base}${currentNode.path}.setGoogleAccount.do">
            <input type="hidden" name="redirectTo" value="${url.base}${renderContext.mainResource.node.path}"/>
            <input type="hidden" name="action" value="deleteWebPropertyID"/>
            <input type="submit" value="<fmt:message key='label.delete'/>"/>
        </form>

        <br>
        <fmt:message key="label.GAAccountsList"/>
        <table class="table">
            <thead>
            <tr>
                <td><fmt:message key="label.account"/></td>
                <td>&nbsp;</td>
                <td>&nbsp;</td>
                <td>&nbsp;</td>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${currentNode.nodes}" var="node">
                <template:module node="${node}" editable="true"/>
            </c:forEach>
            <form action="${url.base}${currentNode.path}.setGoogleAccount.do">
                <input type="hidden" name="redirectTo" value="${url.base}${renderContext.mainResource.node.path}"/>
                <input type="hidden" name="action" value="addGoogleAccount"/>
                <tr>
                    <td><fmt:message key="label.addAccount"/></td>
                    <td><fmt:message key="label.login"/>:<input type="text" name="login"/></td>
                    <td><fmt:message key="label.password"/>:<input type="password" name="password"/></td>
                    <td><input type="submit"/></td>
                    <c:if test="${!empty addGoogleAccountError}">
                        <span class="error"><fmt:message key="label.addGoogleAccountError"/></span>
                    </c:if>

                </tr>
            </form>
            </tbody>
        </table>
    </c:if>
</c:if>
<c:if test="${!jcr:hasPermission(currentNode,'jcr:write')}">
    <fmt:message key="label.GAAccountsList"/>
    <table class="table">
        <thead>
        <tr>
            <td><fmt:message key="label.account"/></td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${currentNode.nodes}" var="node">
            <template:module node="${node}" editable="false"/>
        </c:forEach>
        </tbody>
    </table>

</c:if>
<template:include template="hidden.footer"/>