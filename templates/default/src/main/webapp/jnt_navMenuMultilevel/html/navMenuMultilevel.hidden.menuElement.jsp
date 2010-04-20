<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<jcr:nodeProperty node="${currentNode}" name="j:baselineNode" var="baselineNode"/>

<c:set var="startLevel" value="${not empty param.startLevel ? param.startLevel : currentNode.properties['j:startLevel'].long}"/>
<c:set var="useVisitedNodeAsBasline" value="${not empty param.useVisitedNodeAsBaseline ? param.useVisitedNodeAsBaseline : currentNode.properties['j:useVisitedNodeAsBaseline'].boolean}"/>
<c:set var="mainNode" value="${not empty baselineNode ? baselineNode.node : (useVisitedNodeAsBaseline or jcr:isNodeType(renderContext.mainResource.node, 'jnt:page') ? renderContext.mainResource.node : jcr:getParentOfType(renderContext.mainResource.node, 'jnt:page'))}"/>

<c:set var="firstInLevel" value="${statusNavMenu.first}"/>
<c:set var="lastInLevel" value="${statusNavMenu.last}"/>

<jcr:navigationMenu var="menu" menuNode="${currentNode}" startLevel="${startLevel}" node="${mainNode}" 
    maxDepth="${not empty param.maxDepth ? param.maxDepth : currentNode.properties['j:maxDepth'].long}" 
    relativeToCurrentNode="${useVisitedNodeAsBasline}"/>
    
<c:forEach items="${menu}" var="navMenuBean">
    <jcr:nodeProperty node="${navMenuBean.node}" name="jcr:title" var="title"/>
    <c:set var="useStyle">${navMenuBean.hasChildren ? ' hasChildren' : ' noChildren'}${not empty navMenuBean.parentItem ? ' hasParent' : ' noParent'}${(navMenuBean.firstInLevel and firstInLevel) ? ' firstInLevel' : ''}${(navMenuBean.lastInLevel and lastInLevel) ? ' lastInLevel' : ''}${navMenuBean.selected ? ' selected' : ''}${navMenuBean.inPath ? ' inPath' : ''}</c:set>
    <li class="${fn:trim(useStyle)}">
    <a href='<c:url value="${navMenuBean.node.path}.html" context="${url.base}"/>'>${fn:escapeXml(title.string)}</a>
    <c:choose>
        <c:when test="${navMenuBean.hasChildren}">
            <ul class="navmenu submenu level_${navMenuBean.level + 1}">
        </c:when>
        <c:otherwise>
            </li>
        </c:otherwise>
    </c:choose>
    <c:if test="${navMenuBean.lastInLevel && not empty navMenuBean.parentItem}">
        </ul>
    </c:if>
</c:forEach>

<c:if test="${empty menu && renderContext.editMode}">
	<fieldset>
		<legend>${fn:escapeXml(not empty title.string ? title.string : jcr:label(currentNode.primaryNodeType, currentResource.locale))}</legend>
	</fieldset>
</c:if>