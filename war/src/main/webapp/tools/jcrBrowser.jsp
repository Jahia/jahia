<%@ page contentType="text/html;charset=UTF-8" language="java" 
%><?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@page import="org.jahia.services.usermanager.jcr.JCRUserManagerProvider"%>
<%@page import="org.jahia.services.content.JCRContentUtils"%>
<%@page import="org.jahia.services.content.JCRNodeWrapper"%>
<%@page import="org.jahia.services.content.JCRSessionFactory"%>
<%@page import="org.jahia.services.content.JCRSessionWrapper"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<%@taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr"%>
<%@taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" href="tools.css" type="text/css" />
<title>JCR Browser</title>
<script type="text/javascript">
function doNavigate(what, whereToGo) {
	document.getElementById(what).value=whereToGo; 
	document.getElementById('navigateForm').submit();
}
</script>
</head>
<utility:useConstants var="jcrPropertyTypes" className="org.jahia.services.content.nodetypes.ExtendedPropertyType" scope="application"/>
<c:set var="showProperties" value="${functions:default(param.showProperties, 'false')}"/>
<c:set var="showReferences" value="${functions:default(param.showReferences, 'false')}"/>
<c:set var="showNodes" value="${functions:default(param.showNodes, 'true')}"/>
<c:set var="showActions" value="${functions:default(param.showActions, 'false')}"/>
<c:set var="workspace" value="${functions:default(param.workspace, 'default')}"/>
<c:set var="nodeId" value="${not empty param.uuid ? param.uuid : 'cafebabe-cafe-babe-cafe-babecafebabe'}"/>
<%
JCRSessionFactory.getInstance().setCurrentUser(JCRUserManagerProvider.getInstance().lookupRootUser());
JCRSessionWrapper jcrSession = JCRSessionFactory.getInstance().getCurrentUserSession((String) pageContext.getAttribute("workspace"));
try {
JCRNodeWrapper node = null;
if (request.getParameter("path") != null && request.getParameter("path").length() > 0) {
    node = jcrSession.getNode(JCRContentUtils.escapeNodePath(request.getParameter("path")));
    pageContext.setAttribute("nodeId", node.getIdentifier());
} else {
    node = jcrSession.getNodeByIdentifier((String) pageContext.getAttribute("nodeId"));
}
pageContext.setAttribute("node", node);
pageContext.setAttribute("currentNode", pageContext.getAttribute("node"));
%>
<body>
<c:url var="switchWorkspaceUrl" value="?">
    <c:param name="uuid" value="${node.identifier}"/>
    <c:param name="showProperties" value="${showProperties}"/>
    <c:param name="showReferences" value="${showReferences}"/>
    <c:param name="showNodes" value="${showNodes}"/>
    <c:param name="showActions" value="${showActions}"/>
    <c:param name="workspace" value="${workspace == 'default' ? 'live' : 'default'}"/>
</c:url>
<c:url var="mgrUrl" value="/engines/manager.jsp">
    <c:param name="selectedPaths" value="${currentNode.path}"/>
    <c:param name="workspace" value="${workspace}"/>
</c:url>
<fieldset>
    <form id="navigateForm" action="?" method="get">
        <input type="hidden" name="showProperties" value="${showProperties}"/>
        <input type="hidden" name="showReferences" value="${showProperties}"/>
        <input type="hidden" name="showNodes" value="${showNodes}"/>
        <input type="hidden" name="showActions" value="${showActions}"/>
        <input type="hidden" name="workspace" value="${workspace}"/>
        <input type="hidden" id="path" name="path" value=""/>
        <input type="hidden" id="uuid" name="uuid" value=""/>
    </form> 
    <input type="text" id="goToPath" name="goToPath" value="${fn:escapeXml(node.path)}"
        onkeypress="if ((event || window.event).keyCode == 13) doNavigate('path', this.value);" />
    &nbsp;<a href="#go"
        onclick='var path=document.getElementById("goToPath").value; if (path.length > 0) { doNavigate("path", path); } return false;' title="Got to the node with path">
        <img src="${pageContext.request.contextPath}/icons/refresh.png" height="16" width="16" title="Got to the node with path" border="0" style="vertical-align: middle;"/>
    </a>
    <label for="goToUuid">UUID: </label>
    <input type="text" id="goToUuid" name="goToUuid" value=""
        onkeypress="if ((event || window.event).keyCode == 13) doNavigate('uuid', this.value);" />
    &nbsp;<a href="#go" onclick='var uuid=document.getElementById("goToUuid").value; if (uuid.length > 0) { doNavigate("uuid", uuid); } return false;' title="Got to the node with UUID"><img src="${pageContext.request.contextPath}/icons/search.png" height="16" width="16" title="Got to the node with UUID" border="0" style="vertical-align: middle;"/></a>
</fieldset>
<fieldset>
    <legend><strong>${fn:escapeXml(not empty node.name ? node.name : '<root>')}</strong>&nbsp;- workspace:&nbsp;<strong>${workspace}</strong>&nbsp;(<a href="${switchWorkspaceUrl}">switch to ${workspace == 'default' ? 'live' : 'default'}</a>)
        <a href="${mgrUrl}" target="_blank"><img src="<c:url value='/icons/fileManager.png'/>" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; ">Repository Explorer</a>
    </legend>
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
            <c:when test="${param.action == 'rename' && not empty param.target && not empty param.name}">
                <% JCRNodeWrapper target = jcrSession.getNodeByIdentifier(request.getParameter("target"));
                pageContext.setAttribute("target", target);
                jcrSession.checkout(target.getParent());
                target.rename(JCRContentUtils.findAvailableNodeName(target.getParent(), request.getParameter("name")));
                jcrSession.save();
                %>
                <p style="color: blue">Node <strong>${fn:escapeXml(target.path)}</strong> renamed successfully</p>
            </c:when>
        </c:choose>
    </c:if>
    <c:if test="${node.path != '/'}">
        <c:url var="parentUrl" value="?">
            <c:param name="uuid" value="${node.parent.identifier}"/>
            <c:param name="showProperties" value="${showProperties}"/>
            <c:param name="showReferences" value="${showReferences}"/>
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
           <c:param name="showReferences" value="${showReferences}"/>
           <c:param name="showNodes" value="${showNodes}"/>
           <c:param name="showActions" value="${showActions}"/>
           <c:param name="workspace" value="${workspace}"/>
       </c:url>
    <p><strong>Properties:&nbsp;</strong><a href="${propsUrl}">${showProperties ? 'hide' : 'show'}</a></p>
    <c:if test="${showProperties}">
        <ul>
        <c:set var="properties" value="${node.properties}"/>
        <c:set var="propCount" value="${fn:length(node.properties)}"/>
        <c:if test="${propCount == 0}"><li>No properties present</li></c:if>
        <c:if test="${propCount > 0}">
            <c:forEach items="${properties}" var="property">
                <li>
                <strong>${fn:escapeXml(property.name)}:&nbsp;</strong>
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
        </c:if>
        </ul>
    </c:if>
    
    <c:url var="refsUrl" value="?">
        <c:param name="uuid" value="${node.identifier}"/>
        <c:param name="showProperties" value="${showProperties}"/>
        <c:param name="showReferences" value="${showReferences ? 'false' : 'true'}"/>
        <c:param name="showNodes" value="${showNodes}"/>
        <c:param name="showActions" value="${showActions}"/>
        <c:param name="workspace" value="${workspace}"/>
    </c:url>
    <p><strong>References:&nbsp;</strong><a href="${refsUrl}">${showReferences ? 'hide' : 'show'}</a></p>
    <c:if test="${showReferences}">
        <ul>
        <c:set var="refsCount" value="${functions:length(node.references) + functions:length(node.weakReferences)}"/>
        <c:if test="${refsCount == 0}"><li>No references found</li></c:if>
        <c:if test="${refsCount > 0}">
        <c:forEach items="${node.references}" var="ref">
            <li>
                <c:if test="${not empty ref}">
                    <c:set var="refTarget" value="${ref.parent}"/>
                    <c:url var="refUrl" value="?">
                        <c:param name="uuid" value="${refTarget.identifier}"/>
                        <c:param name="showProperties" value="${showProperties}"/>
                        <c:param name="showReferences" value="${showReferences}"/>
                        <c:param name="showNodes" value="${showNodes}"/>
                        <c:param name="showActions" value="${showActions}"/>
                        <c:param name="workspace" value="${workspace}"/>
                    </c:url>
                    <a href="${refUrl}">${fn:escapeXml(refTarget.name)}&nbsp;(${refTarget.identifier})</a>
                </c:if>
            </li>
        </c:forEach>
        <c:forEach items="${node.weakReferences}" var="ref">
            <li>
                <c:if test="${not empty ref}">
                    <c:set var="refTarget" value="${ref.parent}"/>
                    <c:url var="refUrl" value="?">
                        <c:param name="uuid" value="${refTarget.identifier}"/>
                        <c:param name="showProperties" value="${showProperties}"/>
                        <c:param name="showReferences" value="${showReferences}"/>
                        <c:param name="showNodes" value="${showNodes}"/>
                        <c:param name="showActions" value="${showActions}"/>
                        <c:param name="workspace" value="${workspace}"/>
                    </c:url>
                    <a href="${refUrl}">${fn:escapeXml(refTarget.name)}&nbsp;(${refTarget.identifier})</a>
                </c:if>
            </li>
        </c:forEach>
        </c:if>
        </ul>
    </c:if>

    <c:url var="nodesUrl" value="?">
        <c:param name="uuid" value="${node.identifier}"/>
        <c:param name="showProperties" value="${showProperties}"/>
        <c:param name="showReferences" value="${showReferences}"/>
        <c:param name="showNodes" value="${showNodes ? 'false' : 'true'}"/>
        <c:param name="showActions" value="${showActions}"/>
        <c:param name="workspace" value="${workspace}"/>
    </c:url>
    <p><strong>Child nodes:&nbsp;</strong><a href="${nodesUrl}">${showNodes ? 'hide' : 'show'}</a>
        <c:if test="${showNodes}">
            <c:url var="actionsUrl" value="?">
                <c:param name="uuid" value="${node.identifier}"/>
                <c:param name="showProperties" value="${showProperties}"/>
                <c:param name="showReferences" value="${showReferences}"/>
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
                        <c:param name="showReferences" value="${showReferences}"/>
                        <c:param name="showNodes" value="${showNodes}"/>
                        <c:param name="showActions" value="${showActions}"/>
                        <c:param name="workspace" value="${workspace}"/>
                    </c:url>
                    <a href="${childUrl}">${fn:escapeXml(child.name)}</a>&nbsp;(${child.nodeTypes})
                    <c:if test="${showActions}">
                        <c:url var="deleteUrl" value="?">
                            <c:param name="uuid" value="${node.identifier}"/>
                            <c:param name="showProperties" value="${showProperties}"/>
                            <c:param name="showReferences" value="${showReferences}"/>
                            <c:param name="showNodes" value="${showNodes}"/>
                            <c:param name="showActions" value="${showActions}"/>
                            <c:param name="workspace" value="${workspace}"/>
                            <c:param name="action" value="delete"/>
                            <c:param name="target" value="${child.identifier}"/>
                        </c:url>
                        <c:url var="renameUrl" value="?">
                            <c:param name="uuid" value="${node.identifier}"/>
                            <c:param name="showProperties" value="${showProperties}"/>
                            <c:param name="showReferences" value="${showReferences}"/>
                            <c:param name="showNodes" value="${showNodes}"/>
                            <c:param name="showActions" value="${showActions}"/>
                            <c:param name="workspace" value="${workspace}"/>
                            <c:param name="action" value="rename"/>
                            <c:param name="target" value="${child.identifier}"/>
                        </c:url>
                        &nbsp;|
                        <c:if test="${jcr:isNodeType(child, 'nt:file')}">
                            &nbsp;<a target="_blank" href="<c:url value='${child.url}' context='/'/>" title="download"><img src="${pageContext.request.contextPath}/icons/download.png" height="16" width="16" title="download" border="0" style="vertical-align: middle;"/></a>
                        </c:if>
                        &nbsp;<a target="_blank" href="<c:url value='/cms/export/${workspace}${child.path}.xml?cleanup=simple'/>" title="Exaport as XML"><img src="${pageContext.request.contextPath}/icons/import.png" height="16" width="16" title="Export as XML" border="0" style="vertical-align: middle;"/></a>
                        &nbsp;<a target="_blank" href="<c:url value='/cms/export/${workspace}${child.path}.zip?cleanup=simple'/>" title="Exaport as ZIP"><img src="${pageContext.request.contextPath}/icons/zip.png" height="16" width="16" title="Export as ZIP" border="0" style="vertical-align: middle;"/></a>
                        |&nbsp;
                        &nbsp;<a href="${renameUrl}" onclick='var name=prompt("Please provide a new name for the node:", "${child.name}"); if (name != null & name != "${child.name}") { this.href = this.href + "name=" + name; return true; } else { return false; }' title="Rename"><img src="${pageContext.request.contextPath}/icons/editContent.png" height="16" width="16" title="Rename" border="0" style="vertical-align: middle;"/></a>
                        &nbsp;<a href="${deleteUrl}" onclick='var nodeName="${child.name}"; return confirm("You are about to delete the node " + nodeName + " with all child nodes. Continue?")' title="Delete"><img src="${pageContext.request.contextPath}/icons/delete.png" height="16" width="16" title="Delete" border="0" style="vertical-align: middle;"/></a>
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
    <c:param name="showReferences" value="${showReferences}"/>
    <c:param name="showNodes" value="${showNodes}"/>
    <c:param name="showActions" value="${showActions}"/>
    <c:param name="workspace" value="${workspace == 'default' ? 'live' : 'default'}"/>
</c:url>
<body>
<p>Item with UUID <strong>${nodeId}</strong> does not exist in the '${workspace}' workspace</p>
<p>Actions:
&nbsp;<a href="${switchWorkspaceUrl}">switch to ${workspace == 'default' ? 'live' : 'default'} workspace</a>
&nbsp;<a href="javascript:history.back()">go back</a>
</p>
<%} catch (javax.jcr.PathNotFoundException e) {
%>
<body>
<c:url var="switchWorkspaceUrl" value="?">
    <c:param name="path" value="${param.path}"/>
    <c:param name="showProperties" value="${showProperties}"/>
    <c:param name="showReferences" value="${showReferences}"/>
    <c:param name="showNodes" value="${showNodes}"/>
    <c:param name="showActions" value="${showActions}"/>
    <c:param name="workspace" value="${workspace == 'default' ? 'live' : 'default'}"/>
</c:url>
<p>Item with the path <strong>${param.path}</strong> does not exist in the '${workspace}' workspace</p>
<p>Actions:
&nbsp;<a href="${switchWorkspaceUrl}">switch to ${workspace == 'default' ? 'live' : 'default'} workspace</a>
&nbsp;<a href="javascript:history.back()">go back</a>
</p>
<%} finally {
    JCRSessionFactory.getInstance().setCurrentUser(null);
}%>
</body>
</html>