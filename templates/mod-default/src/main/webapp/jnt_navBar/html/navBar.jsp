<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<c:choose>
    <c:when test="${jcr:isNodeType(renderContext.mainResource.node, 'jnt:page')}">
        <c:set var="page" value="${renderContext.mainResource.node}" />
    </c:when>
    <c:otherwise>
        <c:set var="page" value="${jcr:getParentOfType(renderContext.mainResource.node, 'jnt:page')}" />
    </c:otherwise>
</c:choose>
<c:set var="outerMenu" value="${jcr:getParentOfType(currentNode, 'jnt:navMenu')}"/>

<jcr:nodeProperty name="jcr:title" node="${currentNode}" var="title"/>
<jcr:navigationMenu node="${page}" var="menu" startLevel="${currentNode.properties['j:startLevel'].long}" maxDepth="${currentNode.properties['j:maxDepth'].long}" />
<c:if test="${not empty menu}">
	<c:if test="${not empty title.string && empty outerMenu}">
		<h2>${fn:escapeXml(title.string)}</h2>
	</c:if>
	<c:if test="${empty outerMenu}">	
    <div id="navigationN1">
    </c:if>
        <ul class="level_1">

        <c:forEach items="${menu}" var="navMenuBean">
            <jcr:nodeProperty node="${navMenuBean.node}" name="jcr:title" var="title"/>

            <li class="item_${navMenuBean.itemCount + 2} standard ">

            <a href='<c:url value="${navMenuBean.node.path}.html" context="${url.base}"/>'>${title.string}</a>
            </li>

        </c:forEach>
        </ul>
    <c:if test="${empty outerMenu}">
    </div>
    </c:if>
</c:if>
<c:if test="${empty menu && renderContext.editMode}">
	<fieldset>
		<legend>${fn:escapeXml(not empty title.string ? title.string : jcr:label(currentNode.primaryNodeType))}</legend>
	</fieldset>
</c:if>