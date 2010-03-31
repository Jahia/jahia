<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<jcr:nodeProperty node="${currentNode}" name="j:node" var="reference"/>

<c:set var="startLevel" value="${not empty param.startLevel ? param.startLevel : currentNode.properties['j:startLevel'].long}"/>
<c:set var="mainNode" value="${not empty reference ? reference.node : renderContext.mainResource.node}"/>

<jcr:navigationMenu var="menu" menuNode="${currentNode}" startLevel="${startLevel}" 
    node="${jcr:isNodeType(mainNode, 'jnt:page') ? mainNode : jcr:getParentOfType(mainNode, 'jnt:page')}" 
    maxDepth="${not empty param.maxDepth ? param.maxDepth : currentNode.properties['j:maxDepth'].long}" 
    relativeToCurrentNode="${not empty param.relativeToCurrentNode ? param.relativeToCurrentNode : currentNode.properties['j:relativeToCurrentNode'].boolean}"/>
    
<c:forEach items="${menu}" var="navMenuBean">
    <jcr:nodeProperty node="${navMenuBean.node}" name="jcr:title" var="title"/>
    <c:set var="useStyle">${navMenuBean.hasChildren ? ' hasChildren' : ' noChildren'}${not empty navMenuBean.parentItem ? ' hasParent' : ' noParent'}${navMenuBean.firstInLevel ? ' firstInLevel' : ''}${navMenuBean.lastInLevel ? ' lastInLevel' : ''}${navMenuBean.selected ? ' selected' : ''}${navMenuBean.inPath ? ' inPath' : ''}</c:set>
    <li class="${fn:trim(useStyle)}">
    <a href='<c:url value="${navMenuBean.node.path}.html" context="${url.base}"/>'>${fn:escapeXml(title.string)}</a>
    <c:choose>
        <c:when test="${navMenuBean.hasChildren}">
            <div class="box-inner">
            <ul class="navmenu submenu level_${navMenuBean.level + 1}">
        </c:when>
        <c:otherwise>
            </li>
        </c:otherwise>
    </c:choose>
    <c:if test="${navMenuBean.lastInLevel && not empty navMenuBean.parentItem}">
        </ul>
        </div>
    </c:if>
</c:forEach>

<c:if test="${empty menu && renderContext.editMode}">
	<fieldset>
		<legend>${fn:escapeXml(not empty title.string ? title.string : jcr:label(currentNode.primaryNodeType))}</legend>
	</fieldset>
</c:if>