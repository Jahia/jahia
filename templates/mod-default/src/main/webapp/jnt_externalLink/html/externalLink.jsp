<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<jcr:nodeProperty node="${currentNode}" name="j:url" var="url"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
<a href="${url.string}" target="_new">${title.string}</a>