<%@ page import="org.jahia.utils.FileUtils" %>
<%@ page import="org.jahia.services.content.JCRNodeWrapper" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jcr:nodeProperty node="${currentNode}" name="jcr:created" var="created"/>
<fmt:formatDate value="${created.time}" dateStyle="full" var="displayDate"/>
<a class="<%=FileUtils.getFileIcon( ((JCRNodeWrapper)pageContext.findAttribute("currentNode")).getName()) %>" href="${pageContext.request.contextPath}/files${currentNode.path}"
   title="${displayDate}">${currentNode.name}</a>
