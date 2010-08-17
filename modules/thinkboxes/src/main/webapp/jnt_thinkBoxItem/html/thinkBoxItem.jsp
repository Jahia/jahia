<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<h3>Title: ${currentNode.properties['title'].string}</h3>
<p>url: ${currentNode.properties['url'].string}</p>
<h4>description: </h4>
<p>${currentNode.properties['description'].string}</p>

<h4>note:</h4>
<p>${currentNode.properties['note'].string}</p>