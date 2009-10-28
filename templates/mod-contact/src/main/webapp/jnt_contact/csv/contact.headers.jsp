<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<c:forEach items="${currentNode.properties}" var="property" varStatus="statusItem"><c:if test="${!(property.multiple || fn:contains(property.name,':'))}">${fn:escapeXml(property.name)}<c:if test="${!statusItem.last}">;</c:if></c:if></c:forEach>
