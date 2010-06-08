<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<jcr:nodeProperty node="${currentNode}" name="image" var="newsImage"/>
<c:if test="${not empty newsImage}">
&lt;img src="http://localhost:8080${newsImage.node.url}" width="200" /&gt;
</c:if>
${functions:removeHtmlTags(currentNode.properties.datacontent.string)}