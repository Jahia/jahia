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

<fmt:message key="note.boxItemList.totalElements"/> : ${listTotalSize}
<br /> 
<c:if test="${listTotalSize == 0}">
<fmt:message key="note.boxItemList.noNotes"/>
</c:if>
<c:if test="${listTotalSize > 0}">
<ul>
    <c:forEach items="${result.nodes}" var="itemNode">
    	
    <li><a href="${url.base}${itemNode.path}.html">${itemNode.properties['title'].string}</a> <fmt:message key="note.boxItemList.updatedOn"/> <fmt:formatDate value="${itemNode.properties['jcr:lastModified'].date.time}" dateStyle="short" type="both"/>.
    </li>
    	
    </c:forEach>
 <ul>
</c:if>
