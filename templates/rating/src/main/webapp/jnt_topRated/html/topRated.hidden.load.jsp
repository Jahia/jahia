<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>

<jcr:sql var="result"
         sql="select * from [jmix:rating] as rating inner join [${currentNode.properties['j:typeOfContent'].string}] as rated on issamenode(rating,rated)
             where rating.[j:nbOfVotes] > ${currentNode.properties['j:minNbOfVotes'].long}
             order by rating.[j:sumOfVotes] asc"/>
<c:set var="forcedSkin" value="none"/>
<c:set var="renderOptions" value="none"/>
<c:set var="currentList" value="${result.nodes}" scope="request"/>
<c:set var="end" value="${fn:length(result.nodes)}" scope="request"/>
<c:set var="listTotalSize" value="${end}" scope="request"/>
