<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
Content type : ${currentNode.fileContent.contentType} <br>
<jcr:nodeProperty node="${currentNode}" name="jcr:created" var="created"/>
Creation date : <fmt:formatDate value="${created.time}" dateStyle="full"/> <br>

<a href ="${pageContext.request.contextPath}/files${currentNode.path}">${pageContext.request.contextPath}/files${currentNode.path}</a>