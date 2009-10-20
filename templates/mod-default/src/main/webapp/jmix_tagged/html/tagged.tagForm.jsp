<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<c:if test="${renderContext.user.name != 'guest'}">
    <form action="${url.base}${currentNode.path}" method="get">
        <input type="hidden" name="stayOnNode" value="${url.base}${renderContext.mainResource.node.path}"/>
        <input type="hidden" name="newNodeOutputFormat" value="html"/>
        <input type="hidden" name="methodToCall" value="put"/>
        <jcr:node path="/content/tags" var="tagsNode"/>
        <c:set var="tags" value="${jcr:getNodes(tagsNode,'jnt:tag')}"/>
        <jcr:nodeProperty node="${currentNode}" name="j:tags" var="asssignedTags"/>
		<c:forEach items="${asssignedTags}" var="tag">
			<c:set var="asssignedTagsAsString" value="${asssignedTagsAsString}|${tag.node.identifier}|"/>
		</c:forEach>
        <select name="j:tags" multiple="multiple">
        	<c:forEach items="${tags}" var="tag">
        		<option value="${tag.identifier}" ${fn:contains(asssignedTagsAsString, tag.identifier) ? 'selected="selected"' : ''}>${tag.name}&nbsp;(${tag.references.size})</option>
        	</c:forEach>
        </select>
        <input type="submit" title="Tag it!" value="Tag it!" class="button"/>
    </form>
</c:if>