<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="css" resources="job.css"/>
<!--start jobsSearchForm -->
<div class="jobsSearchForm">
    <form action="${url.base}${renderContext.mainResource.node.path}.html" method="get">
        <fieldset>
            <legend><fmt:message key='jnt_jobOffers.search.form.label'/></legend>
            <p class="field jobsSearchKeyword">
                <label for="jobsSearchKeyword"><fmt:message key='label.keywordSearch'/></label>
                <input type="text" name="jobsSearchKeyword" id="jobsSearchKeyword" class="field jobsSearchKeyword"
                       value="${param.jobsSearchKeyword}" tabindex="4"/>
            </p>

            <div class="divButton">
                <input type="submit" name="submit" id="submit" class="button"
                       value="<fmt:message key="jnt_jobOffers.search.form.label"/>"
                       tabindex="5"/>
            </div>
        </fieldset>
    </form>
</div>
<jcr:jqom var="results">
    <query:selector nodeTypeName="jnt:job"/>
    <jcr:nodeProperty node="${currentNode}" name="searchPath" var="path"/>
    <c:if test="${not empty param.jobsSearchKeyword}">
        <query:fullTextSearch searchExpression="${param.jobsSearchKeyword}"
                              propertyName="description"/>
    </c:if>
    <query:sortBy propertyName="jcr:created" order="desc"/>
</jcr:jqom>
<!--stop jobsSearchForm -->
<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
<c:set var="listTitle" value="${fn:escapeXml(title.string)}"/>
<c:if test="${empty listTitle}"><c:set var="listTitle"><fmt:message key="label.joblist"/></c:set></c:if>

<h3>${listTitle}</h3>

<table width="100%" class="table" summary="">
    <colgroup>
        <col span="1" width="60%" class="col1"/>
        <col span="1" width="20%" class="col2"/>
        <col span="1" width="20%" class="col3"/>
    </colgroup>
    <thead>
    <tr>
        <th id="job" scope="col"><fmt:message key="label.jobtitle"/></th>
        <th id="location" scope="col"><fmt:message key="label.location"/></th>
        <th id="businessUnit" scope="col"><fmt:message key="label.businessUnit"/></th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${results.nodes}" var="job" varStatus="status">
        <c:choose>
            <c:when test="${status.last}">
                <template:module node="${job.placeholder}" template="list" editable="true"/>
            </c:when>
            <c:otherwise>
                <template:module node="${job}" template="list"/>
            </c:otherwise>
        </c:choose>
    </c:forEach>
    <c:if test="${functions:length(results.nodes) == 0 && empty param.jobsSearchKeyword}">
        <c:forEach items="${currentNode.nodes}" var="job">
            <template:module node="${job}" template="list" editable="true"/>
        </c:forEach>
    </c:if>
    <c:if test="${functions:length(results.nodes) == 0 && not empty param.jobsSearchKeyword}">
        <tr>
            <td><fmt:message key="jnt_jobOffers.no.job.offers.found"/></td>
        </tr>
    </c:if>
    </tbody>
</table>