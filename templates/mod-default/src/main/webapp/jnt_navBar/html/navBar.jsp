<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<c:if test="${jcr:isNodeType(renderContext.mainResource.node, 'jnt:page')}">
    <c:set var="page" value="${renderContext.mainResource.node}" />
</c:if>
<c:if test="${!jcr:isNodeType(renderContext.mainResource.node, 'jnt:page')}">
    <c:set var="page" value="${jcr:getParentOfType(renderContext.mainResource.node, 'jnt:page')}" />
</c:if>

<jcr:nodeProperty name="jcr:title" node="${currentNode}" var="title"/>
<jcr:navigationMenu node="${page}" var="menu" startLevel="${currentNode.properties['j:startLevel'].long}" maxDepth="${currentNode.properties['j:maxDepth'].long}" />
<c:if test="${not empty menu}">
	<c:if test="${not empty title.string}">
		<h2>${fn:escapeXml(title.string)}</h2>
	</c:if>
    <div id="navigationN1">
        <ul class="level_1">

        <c:forEach items="${menu}" var="navMenuBean">
            <jcr:nodeProperty node="${navMenuBean.node}" name="jcr:title" var="title"/>

            <li class="item_${navMenuBean.itemCount + 2} standard ">
            <a href="${url.base}${navMenuBean.node.path}.html">${title.string}</a>
            </li>

        </c:forEach>
        </ul>
    </div>
</c:if>
<c:if test="${empty menu && renderContext.editMode}">
	<fieldset>
		<legend>${fn:escapeXml(not empty title.string ? title.string : jcr:label(currentNode.primaryNodeType))}</legend>
	</fieldset>
</c:if>