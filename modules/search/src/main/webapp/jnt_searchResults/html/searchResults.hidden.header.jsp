<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<c:set var="hitsName" value="hits_${currentNode.identifier}"/>
<c:set var="hitsCountName" value="hitsCount_${currentNode.identifier}"/>
<c:choose>
    <c:when test='${empty searchMap[hitsName] }'>
        <s:results var="resultsHits">
            <c:set target="${moduleMap}" property="listTotalSize" value="${count}" />
            <c:set target="${moduleMap}" property="resultsHits" value="${resultsHits}" />
            <c:set target="${moduleMap}" property="displaySearchParams" value="true" />
            <c:if test='${searchMap == null}'>
                <jsp:useBean id="searchMap" class="java.util.HashMap" scope="request"/>
            </c:if>
            <c:set target="${searchMap}" property="${hitsName}" value="${resultsHits}"/>
            <c:set target="${searchMap}" property="${hitsCountName}" value="${count}"/>
        </s:results>
    </c:when>
    <c:otherwise>
        <c:set target="${moduleMap}" property="listTotalSize" value="${searchMap[hitsCountName]}" />
        <c:set target="${moduleMap}" property="resultsHits" value="${searchMap[hitsName]}" />
        <c:set target="${moduleMap}" property="displaySearchParams" value="true" />
    </c:otherwise>
</c:choose>