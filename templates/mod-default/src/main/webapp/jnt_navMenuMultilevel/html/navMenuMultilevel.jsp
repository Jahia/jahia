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
    <%-- TODO improve this check --%>
    <c:if test="${navMenuBean.firstInLevel && (navMenuBean.level > (startLevel + 1))}">
        <ul class="navmenu level_${navMenuBean.level - startLevel}">
    </c:if>
    <jcr:nodeProperty node="${navMenuBean.node}" name="jcr:title" var="title"/>
    <li><a href='<c:url value="${navMenuBean.node.path}.html" context="${url.base}"/>'>${fn:escapeXml(title.string)}</a></li>
    <c:if test="${navMenuBean.lastInLevel && (navMenuBean.level > (startLevel + 1))}">
        </ul>
    </c:if>
</c:forEach>

<c:if test="${empty menu && renderContext.editMode}">
	<fieldset>
		<legend>${fn:escapeXml(not empty title.string ? title.string : jcr:label(currentNode.primaryNodeType))}</legend>
        <span><fmt:message key="search.results.no.results"/></span>
	</fieldset>
</c:if>