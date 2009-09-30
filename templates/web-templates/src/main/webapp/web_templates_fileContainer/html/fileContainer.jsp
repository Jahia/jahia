<%@ page import="org.jahia.utils.FileUtils" %>
<%@ page import="org.jahia.services.content.JCRNodeWrapper" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

 <jcr:nodeProperty node="${currentNode}" name="title" var="title"/>
 <jcr:nodeProperty node="${currentNode}" name="file" var="file"/>
 <jcr:nodeProperty node="${currentNode}" name="fileDisplayDetails" var="fileDisplayDetails"/>
 <jcr:nodeProperty node="${currentNode}" name="fileDesc" var="fileDesc"/>
    <fmt:formatDate value="${created.time}" dateStyle="full" var="displayDate"/>
<li class="document <%=FileUtils.getFileIcon( ((JCRNodeWrapper)pageContext.findAttribute("currentNode")).getName()) %>">
    <c:if test="${!empty title.string}">
        <a href="${pageContext.request.contextPath}/files${file.node.path}">${title.string}</a>
    </c:if>
    <c:if test="${empty title.string}">
    <a href="${pageContext.request.contextPath}/files${file.node.path}">${file.node.path}</a>
    </c:if>
    <c:if test="${fileDisplayDetails.boolean}">
       <span class="docsize"><fmt:formatNumber var="num" pattern="### ### ###.##" type="number" value="${(file.node.fileContent.contentLength/1024)}"/>
        (${num} Ko) &nbsp; ${displayDate}</span>
    </c:if>
        <span class="resume">${fileDesc.string}
        </span>
</li>
