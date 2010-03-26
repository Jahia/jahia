<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<jcr:nodeProperty name="jcr:title" node="${currentNode}" var="title"/>
<jcr:nodeProperty name="j:startLevel" node="${currentNode}" var="startLevel"/>
<jcr:navigationMenu node="${jcr:isNodeType(renderContext.mainResource.node, 'jnt:page') ? renderContext.mainResource.node : jcr:getParentOfType(renderContext.mainResource.node, 'jnt:page')}" var="menu" startLevel="${startLevel.long}" 
    maxDepth="${currentNode.properties['j:maxDepth'].long}" relativeToCurrentNode="${currentNode.properties['j:relativeToCurrentNode'].boolean}"/>
<c:if test="${not empty menu}">
	<c:if test="${not empty title.string}">
		<span><c:out value="${title.string}"/></span>
	</c:if>
    <c:forEach items="${menu}" var="navMenuBean">
        <%-- TODO improve this check --%>
        <c:if test="${navMenuBean.firstInLevel && (navMenuBean.level > (startLevel.long + 1))}">
            <ul class="navmenu level_${navMenuBean.level - startLevel.long}">
        </c:if>
        <jcr:nodeProperty node="${navMenuBean.node}" name="jcr:title" var="title"/>
        <li><a href='<c:url value="${navMenuBean.node.path}.html" context="${url.base}"/>'>${fn:escapeXml(title.string)}</a></li>
        <c:if test="${navMenuBean.lastInLevel && (navMenuBean.level > (startLevel.long + 1))}">
            </ul>
        </c:if>
    </c:forEach>
</c:if>
<c:if test="${empty menu && renderContext.editMode}">
	<fieldset>
		<legend>${fn:escapeXml(not empty title.string ? title.string : jcr:label(currentNode.primaryNodeType))}</legend>
        <span><fmt:message key="search.results.no.results"/></span>
	</fieldset>
</c:if>