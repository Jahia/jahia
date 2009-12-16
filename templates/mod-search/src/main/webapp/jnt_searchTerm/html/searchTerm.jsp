<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="search" uri="http://www.jahia.org/tags/search" %>

<c:set var="props" value="${currentNode.propertiesAsString}"/>
<s:term id="${props.id}" css="${props.css}" style="${props.style}"/>