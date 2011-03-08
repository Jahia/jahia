<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="acl" type="java.lang.String"--%>
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
<c:choose>
    <c:when test="${renderContext.loggedIn}">
        <a title="Vote +1" href="#"
           onclick="document.getElementById('jahia-forum-post-vote-${currentNode.identifier}').submit();"><span>+1 (<fmt:formatNumber
                value="${positiveVote}" pattern="##"/> Good)</span></a>
        <a title="Vote -1" href="#"
           onclick="var voteForm=document.getElementById('jahia-forum-post-vote-${currentNode.identifier}'); voteForm.elements['j:lastVote'].value='-1'; voteForm.submit();"><span>-1 (<fmt:formatNumber
                value="${negativeVote}" pattern="##"/>  Bad)</span></a>
    </c:when>
    <c:otherwise>
        <a title="Vote +1"><span>+1 (<fmt:formatNumber value="${positiveVote}" pattern="##"/> Good)</span></a>
        <a title="Vote -1"><span>-1 (<fmt:formatNumber value="${negativeVote}" pattern="##"/>  Bad)</span></a>
    </c:otherwise>
</c:choose>
