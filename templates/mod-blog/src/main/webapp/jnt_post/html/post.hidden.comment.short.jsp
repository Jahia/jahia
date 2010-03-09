<%@ page contentType="text/html; UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<li><a href="${url.base}${currentNode.parent.parent.path}.html"><jcr:nodeProperty node="${currentNode}" name="jcr:title"/></a>
    <div class="small"><jcr:nodeProperty node="${currentNode}" name="jcr:lastModified" var="lastModified"/><span class="timestamp"><fmt:formatDate
value="${lastModified.time}" pattern="yyyy/MM/dd HH:mm"/></span></div>
</li>

