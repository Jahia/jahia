<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<c:set target="${renderContext}" property="contentType" value="text/plain;charset=UTF-8"/>
<jcr:jqom var="pages">
<query:selector nodeTypeName="jnt:page" selectorName="pagesSelector"/>
<query:descendantNode path="${currentNode.path}" selectorName="pagesSelector"/>
</jcr:jqom>
${currentNode.path}
<c:forEach items="${pages.nodes}" varStatus="status" var="page">
${page.path}
</c:forEach>
