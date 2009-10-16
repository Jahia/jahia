<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://www.jahia.org/tags/jcr" prefix="jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="maincontent">
    <jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
    <jcr:nodeProperty node="${currentNode}" name="body" var="body"/>
    <jcr:nodeProperty node="${currentNode}" name="image" var="image"/>
    <h3>${title.string}</h3>
        <c:if test="${!empty image}">
            <img src="${image.node.url}" alt="${image.node.url}"/>
        </c:if>
        ${body.string}
</div>
<br class="clear"/>
