<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>

<query:definition var="listQuery"
         statement="select * from [jmix:rating] as rating inner join [${currentNode.properties['j:typeOfContent'].string}] as rated on issamenode(rating,rated)
             where rating.[j:nbOfVotes] > ${currentNode.properties['j:minNbOfVotes'].long}
             order by rating.[j:sumOfVotes] asc"/>
<c:set target="${moduleMap}" property="editable" value="false" />
<c:set target="${moduleMap}" property="listQuery" value="${listQuery}" />
