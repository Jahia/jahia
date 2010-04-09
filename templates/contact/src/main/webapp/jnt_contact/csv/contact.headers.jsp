<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<c:set var="props" value="${currentNode.properties}"/><c:forTokens items="firstname,lastname,title,age,birthdate,gender,profession,maritalStatus,hobbies,contact,address,city,state,zip,country,remarks" delims="," var="propName" varStatus="status">${fn:escapeXml(jcr:label(props[propName].definition))}${not status.last ? ';' : ''}</c:forTokens>
