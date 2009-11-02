<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<jcr:nodeProperty node="${currentNode}" name="j:nbOfVotes" var="nbVotes"/>
<jcr:nodeProperty node="${currentNode}" name="j:sumOfVotes" var="sumVotes"/>
<c:set var="positiveVote" value="0"/>
<c:set var="negativeVote" value="0"/>
<c:if test="${nbVotes.long > 0}">
    <c:if test="${sumVotes.long > 0}">
        <c:set var="positiveVote" value="${((sumVotes.long)+(nbVotes.long - sumVotes.long)/2)}"/>
        <c:set var="negativeVote" value="${(nbVotes.long - sumVotes.long)/2}"/>
    </c:if>
    <c:if test="${sumVotes.long == 0}">
        <c:set var="positiveVote" value="${nbVotes.long/2}"/>
        <c:set var="negativeVote" value="${nbVotes.long/2}"/>
    </c:if>
    <c:if test="${sumVotes.long < 0}">
        <c:set var="positiveVote" value="${(nbVotes.long + sumVotes.long)/2}"/>
        <c:set var="negativeVote" value="${((-sumVotes.long)+(nbVotes.long + sumVotes.long)/2)}"/>
    </c:if>
</c:if>
<a title="Vote +1" href="#"
   onclick="document.getElementById('jahia-forum-post-vote-${currentNode.UUID}').submit();"><span>+1 (<fmt:formatNumber value="${positiveVote}" pattern="##"/> Good)</span></a>
<a title="Vote -1" href="#"
   onclick="var voteForm=document.getElementById('jahia-forum-post-vote-${currentNode.UUID}'); voteForm.elements['j:lastVote'].value='-1'; voteForm.submit();"><span>-1 (<fmt:formatNumber value="${negativeVote}" pattern="##"/>  Bad)</span></a>