<%@ page contentType="text/html; UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<li>
	<a class="atopblogcontents" href="${url.base}${currentNode.path}.html"><jcr:nodeProperty node="${currentNode}" name="jcr:title"/></a>
    <jcr:nodeProperty node="${currentNode}" name="jcr:lastModified" var="lastModified"/>
    <span class="bloglistinfo"><fmt:formatDate value="${lastModified.time}" pattern="yyyy/MM/dd HH:mm"/></span>
</li>
