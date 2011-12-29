<%@ page contentType="text/html;charset=UTF-8" language="java" 
%><?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@page import="javax.jcr.Value"%>
<%@page import="javax.jcr.nodetype.PropertyDefinition"%>
<%@page import="org.jahia.services.content.JCRContentUtils"%>
<%@page import="org.jahia.services.content.JCRNodeWrapper"%>
<%@page import="org.jahia.services.content.JCRSessionFactory"%>
<%@page import="org.jahia.services.content.JCRSessionWrapper"%>
<%@page import="org.jahia.services.usermanager.jcr.JCRUserManagerProvider"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<%@taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr"%>
<%@taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<utility:useConstants var="jcrPropertyTypes" className="org.jahia.services.content.nodetypes.ExtendedPropertyType" scope="application"/>
<c:set var="showProperties" value="${functions:default(param.showProperties, 'false')}"/>
<c:set var="showReferences" value="${functions:default(param.showReferences, 'false')}"/>
<c:set var="showNodes" value="${functions:default(param.showNodes, 'true')}"/>
<c:set var="showActions" value="${functions:default(param.showActions, 'false')}"/>
<c:set var="workspace" value="${functions:default(param.workspace, 'default')}"/>
<c:set var="nodeId" value="${not empty param.uuid ? fn:trim(param.uuid) : 'cafebabe-cafe-babe-cafe-babecafebabe'}"/>
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
<head>
<title>JCR Browser</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" href="tools.css" type="text/css" />
<script type="text/javascript">
function go(id1, value1, id2, value2, id3, value3) {
	document.getElementById(id1).value=value1;
	if ('path' == id1) {
		document.getElementById('uuid').value='';
	}
	if (id2) {
		document.getElementById(id2).value=value2;
	}
	if (id3) {
		document.getElementById(id3).value=value3;
	}
	document.getElementById('navigateForm').submit();
}
</script>
</head>
<body>
<c:url var="mgrUrl" value="/engines/manager.jsp">
    <c:param name="selectedPaths" value="${currentNode.path}"/>
    <c:param name="workspace" value="${workspace}"/>
</c:url>
<fieldset>
    <form id="navigateForm" action="?" method="get">
        <input type="hidden" id="showProperties" name="showProperties" value="${showProperties}"/>
        <input type="hidden" id="showReferences" name="showReferences" value="${showReferences}"/>
        <input type="hidden" id="showNodes" name="showNodes" value="${showNodes}"/>
        <input type="hidden" id="showActions" name="showActions" value="${showActions}"/>
        <input type="hidden" id="workspace" name="workspace" value="${workspace}"/>
        <input type="hidden" id="path" name="path" value=""/>
        <input type="hidden" id="uuid" name="uuid" value="${nodeId}"/>
        <input type="hidden" id="value" name="value" value=""/>
        <input type="hidden" id="action" name="action" value=""/>
        <input type="hidden" id="target" name="target" value=""/>
    </form> 
    <input type="text" id="goToPath" name="goToPath" value="${fn:escapeXml(node.path)}"
        onkeypress="if ((event || window.event).keyCode == 13) go('path', this.value);" />
    &nbsp;<a href="#go"
        onclick='var path=document.getElementById("goToPath").value; if (path.length > 0) { go("path", path); } return false;' title="Go to the node with path"
        ><img src="<c:url value='/icons/refresh.png'/>" height="16" width="16" title="Go to the node with path" border="0" style="vertical-align: middle;"/></a>
    <label for="goToUuid">UUID: </label>
    <input type="text" id="goToUuid" name="goToUuid" value=""
        onkeypress="if ((event || window.event).keyCode == 13) go('uuid', this.value);" />
    &nbsp;<a href="#go" onclick='var uuid=document.getElementById("goToUuid").value; if (uuid.length > 0) { go("uuid", uuid); } return false;' title="Go to the node with UUID"><img src="<c:url value='/icons/search.png'/>" height="16" width="16" title="Go to the node with UUID" border="0" style="vertical-align: middle;"/></a>
</fieldset>
 
<fieldset>
    <c:url value="/icons/${workspace == 'default' ? 'editMode' : 'live'}.png" var="iconWorkspace"/>
    <c:url value="/icons/${workspace == 'default' ? 'live' : 'editMode'}.png" var="iconSwitchWorkspace"/>
    <c:url value="/icons/${showActions ? 'preview' : 'editContent'}.png" var="iconActions"/>
    <c:set var="anotherWorkspace" value="${workspace == 'default' ? 'live' : 'default'}"/>
    <legend><strong>${fn:escapeXml(not empty node.name ? node.name : '<root>')}</strong>&nbsp;- workspace:&nbsp;<img src="${iconWorkspace}" width="16" height="16" alt=" "/>&nbsp;<strong>${workspace}</strong>&nbsp;(<a href="#switchWorkspace" onclick="go('workspace', '${anotherWorkspace}'); return false;">switch to ${anotherWorkspace} <img src="${iconSwitchWorkspace}" width="16" height="16" alt=" "/></a>)
        &nbsp;
        <a href="${mgrUrl}" target="_blank"><img src="<c:url value='/icons/fileManager.png'/>" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px;"/>repository explorer</a>
    </legend>

    <fieldset style="position: absolute; right: 20px;">
        <legend><strong>Settings</strong></legend>
        <p>
            <input id="cbActions" type="checkbox" ${showActions ? 'checked="checked"' : ''}
                onchange="go('showActions', '${!showActions}')"/>&nbsp;<label for="cbActions">Show actions</label><br/>
            <input id="cbProperties" type="checkbox" ${showProperties ? 'checked="checked"' : ''}
                onchange="go('showProperties', '${!showProperties}')"/>&nbsp;<label for="cbProperties">Show properties</label><br/>
            <input id="cbNodes" type="checkbox" ${showNodes ? 'checked="checked"' : ''}
                onchange="go('showNodes', '${!showNodes}')"/>&nbsp;<label for="cbNodes">Show child nodes</label><br/>
            <input id="cbReferences" type="checkbox" ${showReferences ? 'checked="checked"' : ''}
                onchange="go('showReferences', '${!showReferences}')"/>&nbsp;<label for="cbReferences">Show references</label>
        </p>
    </fieldset>
    
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
            <c:when test="${param.action == 'rename' && not empty param.target && not empty param.value}">
                <% JCRNodeWrapper target = jcrSession.getNodeByIdentifier(request.getParameter("target"));
                pageContext.setAttribute("target", target);
                jcrSession.checkout(target.getParent());
                target.rename(JCRContentUtils.findAvailableNodeName(target.getParent(), request.getParameter("value")));
                jcrSession.save();
                %>
                <p style="color: blue">Node <strong>${fn:escapeXml(target.path)}</strong> renamed successfully</p>
            </c:when>
            <c:when test="${param.action == 'removeMixin' && not empty param.value}">
                <%
                jcrSession.checkout(node);
                node.removeMixin(request.getParameter("value"));
                jcrSession.save();
                %>
                <p style="color: blue">Mixin ${param.value} successfully removed from the node <strong>${fn:escapeXml(node.path)}</strong></p>
            </c:when>
            <c:when test="${param.action == 'removeProperty' && not empty param.value}">
                <%
                if (node.hasProperty(request.getParameter("value"))) {
                    jcrSession.checkout(node);
                    node.getProperty(request.getParameter("value")).remove();
                    jcrSession.save();
                %>
                <p style="color: blue">Property ${param.value} successfully removed from the node <strong>${fn:escapeXml(node.path)}</strong></p>
                <% } else { %>
                <p style="color: red">Cannot find property ${param.value} on the node <strong>${fn:escapeXml(node.path)}</strong></p>
                <% } %>
            </c:when>
            <c:when test="${param.action == 'setProperty' && not empty param.value}">
                <%
                PropertyDefinition def = JCRContentUtils.getPropertyDefinition(node.getPrimaryNodeTypeName(), request.getParameter("value"));
                if (def != null) {
                    jcrSession.checkout(node);
                    if (def.isMultiple()) {
                        String[] newValues = request.getParameterValues("propertyValue");
                        if (newValues != null) {
                            Value[] vals = new Value[newValues.length];
                            for (int i = 0; i < newValues.length; i++) {
                                vals[i] = jcrSession.getValueFactory().createValue(newValues[i]);
                            }
                            node.setProperty(request.getParameter("value"), vals);
                        } else {
                            node.setProperty(request.getParameter("value"), (Value[]) null);
                        }
                    } else {
                        node.setProperty(request.getParameter("value"), request.getParameter("propertyValue"));
                    }
                    jcrSession.save();
                %>
                <p style="color: blue">Property ${param.value} successfully set on the node <strong>${fn:escapeXml(node.path)}</strong></p>
                <% } else { %>
                <p style="color: red">Cannot find definition for property ${param.value} on the node <strong>${fn:escapeXml(node.path)} [${node.primaryNodeTypeName}]</strong></p>
                <% } %>
            </c:when>
            <c:when test="${param.action == 'addMixin' && not empty param.value}">
                <%
                jcrSession.checkout(node);
                node.addMixin(request.getParameter("value"));
                jcrSession.save();
                %>
                <p style="color: blue">Mixin ${param.value} successfully added to the node <strong>${fn:escapeXml(node.path)}</strong></p>
            </c:when>
            <c:when test="${param.action == 'lock'}">
                <%
                node.lockAndStoreToken("user");
                jcrSession.save();
                %>
                <p style="color: blue">Node <strong>${fn:escapeXml(node.path)}</strong> locked</p>
            </c:when>
            <c:when test="${param.action == 'unlock'}">
                <%
                JCRContentUtils.clearAllLocks(node.getPath(), false, jcrSession);
                jcrSession.save();
                %>
                <p style="color: blue">Locks cleared for node <strong>${fn:escapeXml(node.path)}</strong></p>
            </c:when>
            <c:when test="${param.action == 'unlockTree'}">
                <%
                JCRContentUtils.clearAllLocks(node.getPath(), true, jcrSession);
                jcrSession.save();
                %>
                <p style="color: blue">Locks cleared for node <strong>${fn:escapeXml(node.path)}</strong> and its children</p>
            </c:when>
        </c:choose>
    </c:if>
    
    <c:if test="${node.path != '/'}">
        <a href="#parent" onclick="go('uuid', '${node.parent.identifier}'); return false;">[..]</a>
        <c:set var="breadcrumbs" value=""/>
        <c:forTokens items="${node.path}" delims="/" var="pathItem"
        varStatus="loop"><c:set var="breadcrumbs" value="${breadcrumbs}/${pathItem}"
        />/<c:if test="${!loop.last}"><a href="#breadcrumbs" onclick="go('path', '${breadcrumbs}'); return false;">${fn:escapeXml(pathItem)}</a
        ></c:if><c:if test="${loop.last}">${fn:escapeXml(pathItem)}</c:if></c:forTokens>
    </c:if>
    <p>
        <c:if test="${showActions}">
            <p>
            <c:if test="${!node.locked}">
                <img src="<c:url value='/icons/lock.png'/>" height="16" width="16" border="0" style="vertical-align: middle;" alt=" "/>&nbsp;<a href="#lock" onclick="if (confirm('You are about to put a lock on this node. Continue?')) {go('action', 'lock');} return false;" title="Put a lock on this node">lock node</a>
            </c:if>
            <c:if test="${node.locked}">
                <img src="<c:url value='/icons/unlock.png'/>" height="16" width="16" border="0" style="vertical-align: middle;" alt=" "/>&nbsp;<a href="#unlock" onclick="if (confirm('You are about to remove all locks on this node. Continue?')) {go('action', 'unlock');} return false;" title="Clean all locks on this node">unlock node</a>
                <img src="<c:url value='/icons/unlock.png'/>" height="16" width="16" border="0" style="vertical-align: middle;" alr=" "/>&nbsp;<a href="#unlockTree" onclick="if (confirm('You are about to remove all locks on this node and its children. Continue?')) {go('action', 'unlockTree');} return false;" title="Clean all locks on this node and its children">unlock tree</a>
            </c:if>
            </p>
        </c:if>
        <strong>Name:&nbsp;</strong>${fn:escapeXml(not empty node.name ? node.name : '<root>')}<br/>
        <strong>Path:&nbsp;</strong>${fn:escapeXml(node.path)}<br/>
        <strong>ID:&nbsp;</strong>${fn:escapeXml(node.identifier)}<br/>
        <strong>Type:&nbsp;</strong>${fn:escapeXml(node.primaryNodeTypeName)}<br/>
        <strong>Mixins:&nbsp;</strong>[<c:forEach items="${node.mixinNodeTypes}" var="mixin" varStatus="status">${status.index > 0 ? ", " : ""}${mixin.name}<c:if test="${showActions}">&nbsp;<a href="#remove" onclick="if (confirm('You are about to remove mixin ${mixin.name} from the node. Continue?')) {go('action', 'removeMixin', 'value', '${mixin.name}');} return false;"><img src="<c:url value='/icons/delete.png'/>" height="16" width="16" title="Delete mixin" border="0" style="vertical-align: middle;"/></a></c:if></c:forEach>]
        <c:if test="${showActions}">
            <% pageContext.setAttribute("mixins", JCRContentUtils.getAssignableMixins(node)); %>
            <select id="mixins" name="mixins">
                <c:forEach items="${mixins}" var="mixin">
                    <option value="${mixin}">${mixin}</option>
                </c:forEach>
            </select>
            <button onclick="var newMixin=document.getElementById('mixins').value; if (confirm('You are about to add mixin ' + newMixin + ' to the node. Continue?')) {go('action', 'addMixin', 'value', newMixin);} return false;">add</button>
        </c:if>
        <c:if test="${jcr:isNodeType(node, 'nt:file')}">
            <br/><strong>File:&nbsp;</strong><a target="_blank" href="<c:url value='${node.url}' context='/'/>" title="download"><img src="<c:url value='/icons/download.png'/>" height="16" width="16" title="download" border="0" style="vertical-align: middle;"/></a>
        </c:if>
    </p>
    <p><strong>Properties:&nbsp;</strong><a href="#properties" onclick="go('showProperties', ${showProperties ? 'false' : 'true'}); return false;">${showProperties ? 'hide' : 'show'}</a></p>
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
    
    <p><strong>References:&nbsp;</strong><a href="#references" onclick="go('showReferences', ${showReferences ? 'false' : 'true'}); return false;">${showReferences ? 'hide' : 'show'}</a></p>
    <c:if test="${showReferences}">
        <ul>
        <c:set var="refsCount" value="${functions:length(node.references) + functions:length(node.weakReferences)}"/>
        <c:if test="${refsCount == 0}"><li>No references found</li></c:if>
        <c:if test="${refsCount > 0}">
        <c:forEach items="${node.references}" var="ref">
            <li>
                <c:if test="${not empty ref}">
                    <c:set var="refTarget" value="${ref.parent}"/>
                    <a href="#reference" onclick="go('uuid', '${refTarget.identifier}'); return false;">${fn:escapeXml(refTarget.name)}&nbsp;(${refTarget.identifier})</a>
                </c:if>
            </li>
        </c:forEach>
        <c:forEach items="${node.weakReferences}" var="ref">
            <li>
                <c:if test="${not empty ref}">
                    <c:set var="refTarget" value="${ref.parent}"/>
                    <a href="#reference" onclick="go('uuid', '${refTarget.identifier}'); return false;">${fn:escapeXml(refTarget.name)}&nbsp;(${refTarget.identifier})</a>
                </c:if>
            </li>
        </c:forEach>
        </c:if>
        </ul>
    </c:if>

    <p><strong>Child nodes:&nbsp;</strong><a href="#nodes" onclick="go('showNodes', ${showNodes ? 'false' : 'true'}); return false;">${showNodes ? 'hide' : 'show'}</a>
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
                    <a href="#child" onclick="go('uuid', '${child.identifier}'); return false;">${fn:escapeXml(child.name)}</a>&nbsp;(${child.nodeTypes})
                    <c:if test="${showActions}">
                        &nbsp;|
                        <c:if test="${jcr:isNodeType(child, 'nt:file')}">
                            &nbsp;<a target="_blank" href="<c:url value='${child.url}' context='/'/>" title="download"><img src="<c:url value='/icons/download.png'/>" height="16" width="16" title="download" border="0" style="vertical-align: middle;"/></a>
                        </c:if>
                        &nbsp;<a target="_blank" href="<c:url value='/cms/export/${workspace}${child.path}.xml?cleanup=simple'/>" title="Exaport as XML"><img src="<c:url value='/icons/import.png'/>" height="16" width="16" title="Export as XML" border="0" style="vertical-align: middle;"/></a>
                        &nbsp;<a target="_blank" href="<c:url value='/cms/export/${workspace}${child.path}.zip?cleanup=simple'/>" title="Exaport as ZIP"><img src="<c:url value='/icons/zip.png'/>" height="16" width="16" title="Export as ZIP" border="0" style="vertical-align: middle;"/></a>
                        |&nbsp;
                        &nbsp;<a href="#rename" onclick='var name=prompt("Please provide a new name for the node:", "${child.name}"); if (name != null & name != "${child.name}") { go("action", "rename", "target", "${child.identifier}", "value", name);} return false;' title="Rename"><img src="<c:url value='/icons/editContent.png'/>" height="16" width="16" title="Rename" border="0" style="vertical-align: middle;"/></a>
                        &nbsp;<a href="#delete" onclick='var nodeName="${child.name}"; if (!confirm("You are about to delete the node " + nodeName + " with all child nodes. Continue?")) return false; go("action", "delete", "target", "${child.identifier}"); return false;' title="Delete"><img src="<c:url value='/icons/delete.png'/>" height="16" width="16" title="Delete" border="0" style="vertical-align: middle;"/></a>
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
<%} catch (Exception e) {
%>
<body>
<p style="color:red;"><strong>Error: </strong><%=e %><pre style="color:red;"><% e.printStackTrace(new java.io.PrintWriter(out)); %></pre></p>
<%} finally {
    JCRSessionFactory.getInstance().setCurrentUser(null);
}%>
<p>
    <img src="<c:url value='/engines/images/icons/home_on.gif'/>" height="16" width="16" alt=" " align="top" />&nbsp;
    <a href="<c:url value='/tools/index.jsp'/>">to Jahia Tools overview</a>
</p>
</body>
</html>