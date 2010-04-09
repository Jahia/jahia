<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>

	<div class="publicationListItem" style="border-bottom:1px dotted #999999"><!--start publicationListItem -->
		<div class="publicationListSpace"><!--start publicationListSpace -->
		<p><span class="publicationDate"><c:if test="${!empty date && date !=''}">${currentNode.properties.date.string}</c:if></span>
            <span><a class="publicationDownload" href="${file.node.url}" ><jcr:nodeProperty node="${currentNode}" name="jcr:title"/></a> <c:if test="${!empty author}"><fmt:message key="by"/>: ${currentNode.properties.author.string}</c:if> (<c:if test="${file.node.fileContent.contentLength > 0}">(${num} KB)</c:if></span>  </p>
        <p style="font-size:0.8em; color:#999999;">${currentNode.properties.body.string}</p>
			<div class="clear"> </div>
		</div><!--stop publicationListSpace -->
		<div class="clear"> </div>
	</div><!--stop publicationListItem -->
