<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://www.jahia.org/tags/jcr" prefix="jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jcr:nodeProperty node="${currentNode}" name="image" var="image"/>

<div class="maincontent">
    <h3 class="title"><jcr:nodeProperty node="${currentNode}" name="jcr:title"/></h3>
        <c:if test="${!empty image}">
            <img src="${image.node.url}" alt="${image.node.url}" align="${currentNode.properties.align.string}"/>
        </c:if>
		 ${currentNode.properties.body.string}
</div>
<br class="clear"/>