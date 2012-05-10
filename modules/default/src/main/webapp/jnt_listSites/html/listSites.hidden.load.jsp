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
    <query:definition var="result"
             statement="select * from [jnt:virtualsite] as site where isdescendantnode(site,'/sites') and localname(site) <> 'systemsite' order by site.[jcr:created] desc"
             limit="${currentNode.properties['numberMaxOfSitesDisplayed'].string}"/>
</c:if>
<c:if test="${not empty currentNode.properties['typeOfContent'] and currentNode.properties['typeOfContent'].string ne 'website'}">
    <query:definition var="result"
             statement="select * from [jnt:virtualsite] as site where isdescendantnode(site,'/sites') order by site.[jcr:created] desc"
             limit="${currentNode.properties['numberMaxOfSitesDisplayed'].string}"/>
</c:if>

<c:set target="${moduleMap}" property="listQuery" value="${result}"/>
