<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<div class="mainContent">
    <div class="peopleBody">
    <c:forEach items="${currentNode.properties}" var="property">
        <c:if test="${!(property.multiple || fn:contains(property.name,':'))}">
            <p><span class="peopleLabel">${property.name}:</span>&nbsp;${fn:escapeXml(property.string)}</p>
        </c:if>
    </c:forEach>
    </div>
</div>    