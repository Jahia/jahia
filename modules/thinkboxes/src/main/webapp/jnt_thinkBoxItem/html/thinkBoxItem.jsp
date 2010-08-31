<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<h3><fmt:message key="note.field.title"/>: ${currentNode.properties['title'].string}</h3>
<p><fmt:message key="note.field.url"/>: ${currentNode.properties['url'].string}</p>
<h4>d<fmt:message key="note.field.description"/>: </h4>
<p>${currentNode.properties['description'].string}</p>

<h4><fmt:message key="note.field.note"/>:</h4>
<p>${currentNode.properties['note'].string}</p>