<%@ page contentType="text/html;charset=UTF-8" language="java" 
%><?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@page import="org.jahia.services.usermanager.jcr.JCRUserManagerProvider"%>
<%@page import="org.jahia.services.content.JCRNodeWrapper"%>
<%@page import="org.jahia.services.content.JCRSessionFactory"%>
<%@page import="org.jahia.services.content.JCRSessionWrapper"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core"  prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions"  prefix="fn"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt"%>
<%@taglib uri="http://www.jahia.org/tags/functions" prefix="functions"%>
<%@taglib uri="http://www.jahia.org/tags/jcr" prefix="jcr"%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>JCR Browser</title>
</head>
<c:set var="showProperties" value="${functions:default(param.showProperties, 'false')}"/>
<c:set var="showNodes" value="${functions:default(param.showNodes, 'true')}"/>
<c:set var="showActions" value="${functions:default(param.showActions, 'false')}"/>
<c:set var="workspace" value="${functions:default(param.workspace, 'default')}"/>
<c:set var="nodeId" value="${not empty param.uuid ? param.uuid : 'cafebabe-cafe-babe-cafe-babecafebabe'}"/>
<%
JCRSessionFactory.getInstance().setCurrentUser(JCRUserManagerProvider.getInstance().lookupRootUser());
JCRSessionWrapper jcrSession = JCRSessionFactory.getInstance().getCurrentUserSession((String) pageContext.getAttribute("workspace"));
try {
pageContext.setAttribute("node", jcrSession.getNodeByIdentifier((String) pageContext.getAttribute("nodeId")));
%>
<body style="color:#36393D; font-family:Arial,Helvetica,sans-serif; font-size:80%; line-height:160%;">
<c:url var="switchWorkspaceUrl" value="?">
    <c:param name="uuid" value="${node.identifier}"/>
    <c:param name="showProperties" value="${showProperties}"/>
    <c:param name="showNodes" value="${showNodes}"/>
    <c:param name="showActions" value="${showActions}"/>
    <c:param name="workspace" value="${workspace == 'default' ? 'live' : 'default'}"/>
</c:url>
<fieldset>
    <legend><strong>${fn:escapeXml(not empty node.name ? node.name : '<root>')}</strong>&nbsp;- workspace:&nbsp;<strong>${workspace}</strong>&nbsp;(<a href="${switchWorkspaceUrl}">switch to ${workspace == 'default' ? 'live' : 'default'}</a>)</legend>
    <c:if test="${not empty param.action}">
        <c:choose>
            <c:when test="${param.action == 'delete' && not empty param.target}">
                <% JCRNodeWrapper target = jcrSession.getNodeByIdentifier(request.getParameter("target"));
                pageContext.setAttribute("target", target);
                jcrSession.checkout(target.getParent());    
                target.remove();
                jcrSession.save();
                %>
                <p style="color: blue">Node <strong>${fn:escapeXml(target.path)}</strong> deleted successfully</p>
            </c:when>
        </c:choose>
    </c:if>
    <c:if test="${node.path != '/'}">
        <c:url var="parentUrl" value="?">
            <c:param name="uuid" value="${node.parent.identifier}"/>
            <c:param name="showProperties" value="${showProperties}"/>
            <c:param name="showNodes" value="${showNodes}"/>
            <c:param name="showActions" value="${showActions}"/>
            <c:param name="workspace" value="${workspace}"/>
        </c:url>
    <a href="${parentUrl}">[..]&nbsp;${fn:escapeXml(node.parent.name)}</a>
    </c:if>
    <p>
        <strong>Name:&nbsp;</strong>${fn:escapeXml(not empty node.name ? node.name : '<root>')}<br/>
        <strong>Path:&nbsp;</strong>${fn:escapeXml(node.path)}<br/>
        <strong>ID:&nbsp;</strong>${fn:escapeXml(node.identifier)}<br/>
        <strong>Types:&nbsp;</strong>${fn:escapeXml(node.nodeTypes)}<br/>
        <strong>Mixins:&nbsp;</strong>[<c:forEach items="${node.mixinNodeTypes}" var="mixin" varStatus="status">${status.index > 0 ? ", " : ""}${fn:escapeXml(mixin.name)}</c:forEach>]
        <c:if test="${jcr:isNodeType(node, 'nt:file')}">
            <br/><strong>File:&nbsp;</strong><a target="_blank" href="<c:url value='${node.url}' context='/'/>" title="download"><img src="${pageContext.request.contextPath}/icons/download.png" height="16" width="16" title="download" border="0" style="vertical-align: middle;"/></a>
        </c:if>
    </p>
       <c:url var="propsUrl" value="?">
           <c:param name="uuid" value="${node.identifier}"/>
           <c:param name="showProperties" value="${showProperties ? 'false' : 'true'}"/>
           <c:param name="showNodes" value="${showNodes}"/>
           <c:param name="showActions" value="${showActions}"/>
           <c:param name="workspace" value="${workspace}"/>
       </c:url>
    <p><strong>Properties:&nbsp;</strong><a href="${propsUrl}">${showProperties ? 'hide' : 'show'}</a></p>
    <c:if test="${showProperties}">
        <ul>
        <c:set var="properties" value="${node.properties}"/>
        <c:if test="${fn:length(properties) == 0}"><li>No properties present</li></c:if>
        <c:if test="${fn:length(properties) > 0}">
            <c:forEach items="${properties}" var="property">
                <li>
                <strong>${fn:escapeXml(property.name)}:&nbsp;</strong>
                <c:if test="${property.multiple}" var="multiple">
                    <ul>
                        <c:if test="${empty property.values}">
                            <li>[]</li>
                        </c:if>
                        <c:forEach items="${property.values}" var="value">
                            <li><%@include file="/modules/default/nt_base/raw/value.jspf" %></li>
                        </c:forEach>
                    </ul>
                </c:if>
                <c:if test="${!multiple}">
                    <c:set var="value" value="${property.value}"/>
                    <%@include file="/modules/default/nt_base/raw/value.jspf" %>
                </c:if>
                </li>
            </c:forEach>
        </c:if>
        </ul>
    </c:if>
    
    <c:url var="nodesUrl" value="?">
        <c:param name="uuid" value="${node.identifier}"/>
        <c:param name="showProperties" value="${showProperties}"/>
        <c:param name="showNodes" value="${showNodes ? 'false' : 'true'}"/>
        <c:param name="showActions" value="${showActions}"/>
        <c:param name="workspace" value="${workspace}"/>
    </c:url>
    <p><strong>Child nodes:&nbsp;</strong><a href="${nodesUrl}">${showNodes ? 'hide' : 'show'}</a>
        <c:if test="${showNodes}">
            <c:url var="actionsUrl" value="?">
                <c:param name="uuid" value="${node.identifier}"/>
                <c:param name="showProperties" value="${showProperties}"/>
                <c:param name="showNodes" value="${showNodes}"/>
                <c:param name="showActions" value="${showActions ? 'false' : 'true'}"/>
            </c:url>
            (<a href="${actionsUrl}">${showActions ? 'hide actions' : 'show actions'}</a>)
        </c:if>
    <c:if test="${showNodes}">
        <c:set var="nodes" value="${node.nodes}"/>
        <c:set var="childrenCount" value="${functions:length(nodes)}"/>
        <c:if test="${childrenCount > 0}">- ${childrenCount} nodes found</c:if>
        </p>
        <ul>
            <c:if test="${not empty parentUrl}">
                <li><a href="${parentUrl}">[..]</a></li>
            </c:if>
        <c:if test="${childrenCount == 0}"><li>No child nodes present</li></c:if>
        <c:if test="${childrenCount > 0}">
               <c:forEach items="${nodes}" var="child">
                <li>
                    <c:url var="childUrl" value="?">
                        <c:param name="uuid" value="${child.identifier}"/>
                        <c:param name="showProperties" value="${showProperties}"/>
                        <c:param name="showNodes" value="${showNodes}"/>
                        <c:param name="showActions" value="${showActions}"/>
                        <c:param name="workspace" value="${workspace}"/>
                    </c:url>
                    <a href="${childUrl}">${fn:escapeXml(child.name)}</a>&nbsp;(${child.nodeTypes})
                    <c:if test="${showActions}">
                        <c:url var="deleteUrl" value="?">
                            <c:param name="uuid" value="${node.identifier}"/>
                            <c:param name="showProperties" value="${showProperties}"/>
                            <c:param name="showNodes" value="${showNodes}"/>
                            <c:param name="showActions" value="${showActions}"/>
                            <c:param name="workspace" value="${workspace}"/>
                            <c:param name="action" value="delete"/>
                            <c:param name="target" value="${child.identifier}"/>
                        </c:url>
                        &nbsp;<a href="${deleteUrl}" onclick='var nodeName="${child.name}"; return confirm("You are about to delete the node " + nodeName + " with all child nodes. Continue?")' title="delete"><img src="${pageContext.request.contextPath}/icons/delete.png" height="16" width="16" title="delete" border="0" style="vertical-align: middle;"/></a>
                        <c:if test="${jcr:isNodeType(child, 'nt:file')}">
                            &nbsp;<a target="_blank" href="<c:url value='${child.url}' context='/'/>" title="download"><img src="${pageContext.request.contextPath}/icons/download.png" height="16" width="16" title="download" border="0" style="vertical-align: middle;"/></a>
                        </c:if>
                    </c:if>
                </li>
            </c:forEach>
        </c:if>
        </ul>
    </c:if>
    <c:if test="${empty showNodes || not showNodes}">
        </p>
    </c:if>
</fieldset>
</body>
<%} catch (javax.jcr.ItemNotFoundException e) {
%>
<c:url var="switchWorkspaceUrl" value="?">
    <c:param name="uuid" value="${node.identifier}"/>
    <c:param name="showProperties" value="${showProperties}"/>
    <c:param name="showNodes" value="${showNodes}"/>
    <c:param name="showActions" value="${showActions}"/>
    <c:param name="workspace" value="${workspace == 'default' ? 'live' : 'default'}"/>
</c:url>
<p>Item with UUID <strong>${nodeId}</strong> does not exist in the '${workspace}' workspace:
&nbsp;<a href="${switchWorkspaceUrl}">switch to ${workspace == 'default' ? 'live' : 'default'}</a>
</p>
<%} finally {
    JCRSessionFactory.getInstance().setCurrentUser(null);
}%>
</html>