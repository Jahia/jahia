<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>


<jcr:sql var="result"
         sql="select * from [jnt:thinkBoxItem] as tbitem where isdescendantnode(tbitem, ['/users/${renderContext.user.name}/thinkbox/']) order by tbitem.[jcr:lastModified] desc"/>

<c:set var="currentList" value="${result.nodes}" scope="request"/>
<c:set var="listTotalSize" value="${functions:length(result.nodes)}" scope="request"/>

Total elements: ${listTotalSize} 
<br /> 
<c:if test="${listTotalSize == 0}">
Pas de notes
</c:if>
<c:if test="${listTotalSize > 0}">
    <c:forEach items="${result.nodes}" var="itemNode">
    	
    <a href="${url.base}${itemNode.path}.html">${itemNode.properties['jcr:title'].string}</a> - Modifi�e le <fmt:formatDate value="${itemNode.properties['jcr:lastModified'].date.time}" dateStyle="short" type="both"/>.
    <br />
    	
    </c:forEach>
</c:if>
