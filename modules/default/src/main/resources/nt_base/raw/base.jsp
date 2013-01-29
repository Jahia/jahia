<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="propertyDefinition" type="org.jahia.services.content.nodetypes.ExtendedPropertyDefinition"--%>
<%--@elvariable id="type" type="org.jahia.services.content.nodetypes.ExtendedNodeType"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<utility:useConstants var="jcrPropertyTypes" className="org.jahia.services.content.nodetypes.ExtendedPropertyType" scope="application"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
<title>${fn:escapeXml(currentNode.name)}</title>
</head>
<body style="color:#36393D; font-family:Arial,Helvetica,sans-serif; font-size:80%; line-height:160%;">
<fieldset>
    <legend><strong>${fn:escapeXml(currentNode.name)}</strong></legend>
    <c:if test="${not empty currentNode.parent}">
        <c:url var="urlValue" value="${url.base}${currentNode.parent.path}.raw?${pageContext.request.queryString}"/>
        <a href="${urlValue}">[..]</a>
    </c:if>
    <c:url var="mgrUrl" value="/engines/manager.jsp">
        <c:param name="site" value="${renderContext.site.identifier}"/>
        <c:param name="selectedPaths" value="${currentNode.path}"/>
        <c:param name="workspace" value="${renderContext.mainResource.workspace}"/>
    </c:url>
    <a href="${mgrUrl}" target="_blank"><img src="<c:url value='/icons/fileManager.png'/>" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; "><fmt:message
            key="label.repositoryexplorer"/></a>

    <p>
        <strong>Path :&nbsp;</strong>${fn:escapeXml(currentNode.path)}<br/>
        <strong>ID :&nbsp;</strong>${fn:escapeXml(currentNode.identifier)}<br/>
        <strong>Types :&nbsp;</strong>${fn:escapeXml(currentNode.nodeTypes)}<br/>
        <strong>Mixins :&nbsp;</strong><c:forEach items="${currentNode.mixinNodeTypes}" var="mixin">${fn:escapeXml(mixin.name)}</c:forEach>
        <c:if test="${jcr:isNodeType(currentNode, 'nt:file')}">
            <br/><strong>File:&nbsp;</strong><a href="<c:url value="${currentNode.url}" context='/'/>">download</a>
        </c:if>
    </p>
    <p><strong>Properties:&nbsp;</strong><a href="?showProperties=${not param.showProperties}&amp;showNodes=${param.showNodes}">${param.showProperties ? 'hide' : 'show'}</a></p>
    <c:if test="${param.showProperties}">
        <ul>
            <c:if test="${functions:length(currentNode.properties) == 0}"><li>No properties present</li></c:if>
            <c:forEach items="${currentNode.properties}" var="property">
                <li><strong>${fn:escapeXml(property.name)}:&nbsp;</strong>
                    <c:if test="${property.multiple}" var="multiple">
                        <ul>
                            <c:if test="${empty property.values}">
                                <li>[]</li>
                            </c:if>
                            <c:forEach items="${property.values}" var="value">
                                <li><%@include file="value.jspf" %></li>
                            </c:forEach>
                        </ul>
                    </c:if>
                    <c:if test="${!multiple}">
                        <c:set var="value" value="${property.value}"/>
                        <%@include file="value.jspf" %>
                    </c:if>
                </li>
            </c:forEach>
        </ul>
    </c:if>
    <p><strong>Child nodes:&nbsp;</strong><a href="?showProperties=${param.showProperties}&amp;showNodes=${not param.showNodes}">${param.showNodes ? 'hide' : 'show'}</a></p>
    <c:if test="${param.showNodes}">
        <ul>
            <c:if test="${functions:length(currentNode.nodes) == 0}"><li>No child nodes present</li></c:if>
            <c:forEach items="${currentNode.nodes}" var="child">
                <c:url var="urlValue" value="${url.base}${child.path}.raw?${pageContext.request.queryString}"/>
                <li><a href="${urlValue}">${fn:escapeXml(child.name)}</a> - types : ${fn:escapeXml(child.nodeTypes)}</li>
            </c:forEach>
        </ul>
    </c:if>
</fieldset>
</body>
</html>