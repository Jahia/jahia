<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<jcr:nodeProperty node="${currentNode}" name="j:firstName" var="firstname"/>
<jcr:nodeProperty node="${currentNode}" name="j:lastName" var="lastname"/>

<h3><fmt:message key="welcome"/>&nbsp;<c:choose>
        <c:when test="${empty firstname.string && empty lastname.string}">
           ${fn:escapeXml(currentNode.name)}
        </c:when>
        <c:otherwise>
           ${fn:escapeXml(firstname.string)}&nbsp;${fn:escapeXml(lastname.string)}
        </c:otherwise>
</c:choose></h3>