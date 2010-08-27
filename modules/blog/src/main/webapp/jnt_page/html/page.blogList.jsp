<%@ page contentType="text/html; UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<li>
    <h5>
        <a class="atopblog" href="${url.base}${currentNode.path}.html">
            <jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
            <c:if test="${!empty title}">${title.string}</c:if>
            <c:if test="${empty title}">no title</c:if>
        </a>
    </h5>
</li>