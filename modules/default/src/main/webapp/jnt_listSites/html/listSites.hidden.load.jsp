<%@ page contentType="text/html; UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>


<c:if test="${empty currentNode.properties['typeOfContent'] or currentNode.properties['typeOfContent'].string eq 'website'}">
    <c:set var="query" value="select * from [jnt:virtualsite] as site where isdescendantnode(site,'/sites') and localname(site) <> 'systemsite'" />
</c:if>
<c:if test="${not empty currentNode.properties['typeOfContent'] and currentNode.properties['typeOfContent'].string ne 'website'}">
    <c:set var="query" value="select * from [jnt:virtualsite] as site where isdescendantnode(site,'/sites')" />
</c:if>
<jcr:nodeProperty node="${currentNode}" name="templatesSets" var="templatesSets"/>
<c:forEach items="${templatesSets}" var="templatesSet" varStatus="status">
    <c:choose>
        <c:when test="${status.first}">
            <c:set var="query" value="${query} and (site.[j:templatesSet] = '${templatesSet.node.name}'" />
        </c:when>
        <c:otherwise>
            <c:set var="query" value="${query} or site.[j:templatesSet] = '${templatesSet.node.name}'" />
        </c:otherwise>
    </c:choose>
    <c:if test="${status.last}">
        <c:set var="query" value="${query})" />
    </c:if>
</c:forEach>
<c:set var="query" value="${query} order by site.[jcr:created] desc" />

<query:definition var="result" statement="${query}"
                  limit="${currentNode.properties['numberMaxOfSitesDisplayed'].string}"/>
<c:set target="${moduleMap}" property="listQuery" value="${result}"/>
