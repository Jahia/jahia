<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<% pageContext.setAttribute("newLineChar", "\n"); %> 
<c:set target="${renderContext}" property="contentType" value="text/plain;charset=UTF-8"/>
<jcr:jqom var="files">
<query:selector nodeTypeName="jnt:file" selectorName="filesSelector"/>
<query:descendantNode path="${currentNode.path}" selectorName="filesSelector"/>
</jcr:jqom>
${currentNode.path}
<c:forEach items="${files.nodes}" varStatus="status" var="file">
${file.path}.html${fn:escapeXml(newLineChar)}
</c:forEach>
